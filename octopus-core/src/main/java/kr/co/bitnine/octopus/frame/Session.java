/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.bitnine.octopus.frame;

import kr.co.bitnine.octopus.libpg.*;
import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.queryengine.ExecutableStatement;
import kr.co.bitnine.octopus.queryengine.ParsedStatement;
import kr.co.bitnine.octopus.queryengine.QueryEngine;
import kr.co.bitnine.octopus.queryengine.QueryResult;
import kr.co.bitnine.octopus.schema.SchemaManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.Properties;
import java.util.Random;

class Session implements Runnable
{
    private static final Log LOG = LogFactory.getLog(Session.class);

    private final SocketChannel clientChannel;
    private final int sessionId; // secret key

    interface EventHandler
    {
        void onClose(Session session);
        void onCancel(int sessionId);
    }
    private final EventHandler eventHandler;

    private final MessageStream messageStream;
    private final MetaContext metaContext;
    private final SchemaManager schemaManager;

    QueryEngine queryEngine;

    Session(SocketChannel clientChannel, EventHandler eventHandler, MetaContext metaContext, SchemaManager schemaManager)
    {
        this.clientChannel = clientChannel;
        sessionId = new Random(this.hashCode()).nextInt();

        this.eventHandler = eventHandler;

        messageStream = new MessageStream(clientChannel);
        this.metaContext = metaContext;
        this.schemaManager = schemaManager;
    }

    int getId()
    {
        return sessionId;
    }

    @Override
    public void run()
    {
        try {
            Message imsg = messageStream.getInitialMessage();
            int i = imsg.peekInt();

            if (i == PostgresConstants.CANCEL_REQUEST_CODE) {
                handleCancelRequest(imsg);
                return;
            }

            if (i == PostgresConstants.SSL_REQUEST_CODE) {
                handleSSLRequest(imsg);
                return;
            }

            // Start-up Phase
            handleStartupMessage(imsg);
            doAuthentication();

            queryEngine = new QueryEngine(metaContext, schemaManager);

            messageLoop();
        } catch (Exception e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        }

        close();
    }

    void reject()
    {
        try {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.TOO_MANY_CONNECTIONS,
                    "too many clients already, rejected");
            PostgresExceptions.report(messageStream, pge);
        } catch (Exception e) { }

        close();
    }

    private final String CLIENT_PARAM_USER = "user";
    private final String CLIENT_PARAM_DATABASE = "database";
    private final String CLIENT_PARAM_ENCODING = "client_encoding";

    private Properties clientParams;
    private ParsedStatement parsedStatement = null;
    private ExecutableStatement executableStatement = null;

    private void handleCancelRequest(Message imsg)
    {
        imsg.getInt(); // cancel request code
        imsg.getInt(); // process ID
        int cancelKey = imsg.getInt();

        // cancelKey is the same as sessionId
        eventHandler.onCancel(cancelKey);
    }

    // TODO
    private void handleSSLRequest(Message imsg) throws Exception
    {
        PostgresException pge = new PostgresException(
                PostgresException.Severity.FATAL,
                PostgresException.SQLSTATE.FEATURE_NOT_SUPPORTED,
                "unsupported frontend protocol");
        PostgresExceptions.report(messageStream, pge);
    }

    private void handleStartupMessage(Message imsg) throws Exception
    {
        int version = imsg.getInt();
        if (version != PostgresConstants.PROTOCOL_VERSION(3, 0)) {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.FEATURE_NOT_SUPPORTED,
                    "unsupported frontend protocol");
            PostgresExceptions.report(messageStream, pge);
        }

        Properties params = new Properties();
        while (true) {
            String paramName = imsg.getCString();
            if (paramName.length() == 0)
                break;
            String paramValue = imsg.getCString();
            params.setProperty(paramName, paramValue);
        }

        if (LOG.isDebugEnabled()) {
            for (String key : params.stringPropertyNames()) {
                String val = params.getProperty(key);
                LOG.debug(key + "=" + val);
            }
        }

        clientParams = params;
    }

    // NOTE: Now, cleartext only
    private void doAuthentication() throws Exception
    {
        // AuthenticationCleartextPassword
        Message msg = Message.builder('R')
                .putInt(3)
                .build();
        messageStream.putMessageAndFlush(msg);

        // receive PasswordMessage
        msg = messageStream.getMessage();
        if (msg.getType() != 'p') {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                    "expected password response, got message type '" + msg.getType() + "'");
            PostgresExceptions.report(messageStream, pge);
        }

        // verify password
        String username = clientParams.getProperty(CLIENT_PARAM_USER);
        try {
            String password = msg.getCString();
            String currentPassword = metaContext.getUserPasswordByName(username);
            if (!password.equals(currentPassword)) {
                PostgresException pge = new PostgresException(
                        PostgresException.Severity.FATAL,
                        PostgresException.SQLSTATE.INVALID_PASSWORD,
                        "password authentication failed for user " + username);
                PostgresExceptions.report(messageStream, pge);
            }
        } catch (MetaException e) {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                    "invalid user name '" + username + "'", e);
            PostgresExceptions.report(messageStream, pge);
        }

        // AuthenticationOk
        msg = Message.builder('R')
                .putInt(0)
                .build();
        messageStream.putMessage(msg);

        // TODO: initialize user schema

        // TODO: ParameterStatus

        // BackendKeyData
        msg = Message.builder('K')
                .putInt(0)          // process ID, not used
                .putInt(sessionId)
                .build();
        messageStream.putMessage(msg);

        LOG.info("BackendKeyData");
    }

    private void messageLoop() throws Exception
    {
        /*
         * Normal Phase
         */

        boolean ready = true;
        while (true) {
            if (ready) {
                // ReadyForQuery
                Message msg = Message.builder('Z')
                        .putChar(TransactionStatus.IDLE.getIndicator())
                        .build();
                messageStream.putMessageAndFlush(msg);
                ready = false;
            }

            Message msg = messageStream.getMessage();

            char type = msg.getType();
            switch (type) {
                case 'Q':
                    handleQuery(msg);
                    ready = true;
                    break;
                case 'P':
                    handleParse(msg);
                    break;
                case 'B':
                    handleBind(msg);
                    break;
                case 'E':
                    handleExecute(msg);
                    break;
                case 'C':
                    handleClose(msg);
                    break;
                case 'D':
                    handleDescribe(msg);
                    break;
                case 'H':
                    messageStream.flush();
                    break;
                case 'S':
                    LOG.debug("sync");
                    ready = true;
                    break;
                case 'X':
                    LOG.info("Terminate received");
                    return;
                case 'd':   // copy data
                case 'c':   // copy done
                case 'f':   // copy fail
                    break;  // ignore these messages
                default:
                    PostgresException pge = new PostgresException(
                            PostgresException.Severity.FATAL,
                            PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                            "invalid frontend message type '" + type + "'");
                    PostgresExceptions.report(messageStream, pge);
            }
        }
    }

    private void sendEmptyQueryResponse() throws IOException
    {
        messageStream.putMessage(Message.builder('I').build());
    }

    private void sendCommandComplete(String tag) throws IOException
    {
        // FIXME: tag format
        if (tag == null)
            tag = "SELECT 0";

        Message msg = Message.builder('C')
                .putCString(tag)
                .build();
        messageStream.putMessage(msg);
    }

    private void sendRowDescription(QueryResult qr) throws Exception
    {
        ResultSet rs = qr.unwrap(ResultSet.class);
        ResultSetMetaData rsmd = rs.getMetaData();

        int colCnt = rsmd.getColumnCount();

        // RowDescription
        Message.Builder msgBld = Message.builder('T').putShort((short) colCnt);
        for (int i = 1; i <= colCnt; i++) {
            String colName = rsmd.getColumnName(i);
            int colType = rsmd.getColumnType(i);
            int oid = Oid.fromTypes(colType);

            msgBld.putCString(colName)
                    .putInt(0)              // table OID
                    .putShort((short) 0)    // attribute number
                    .putInt(oid)            // data type OID
                    .putShort((short) -1)   // data type size (variable-width)
                    .putInt(0)              // type-specific type modifier
                    .putShort((short) 0);   // not yet known
        }
        messageStream.putMessage(msgBld.build());
    }

    private int sendDataRow(QueryResult qr) throws Exception
    {
        ResultSet rs = qr.unwrap(ResultSet.class);
        ResultSetMetaData rsmd = rs.getMetaData();

        int colCnt = rsmd.getColumnCount();

        // DataRow
        int rowCnt = 0;
        while (rs.next()) {
            Message.Builder msgBld = Message.builder('D').putShort((short) colCnt);
            for (int i = 1; i <= colCnt; i++) {
                switch (rsmd.getColumnType(i)) {
                    case Types.INTEGER:
                        byte[] iBytes = String.valueOf(rs.getInt(i)).getBytes(StandardCharsets.US_ASCII);
                        msgBld.putInt(iBytes.length)
                                .putBytes(iBytes);
                        break;
                    case Types.VARCHAR:
                        byte[] vBytes = rs.getString(i).getBytes(StandardCharsets.US_ASCII);
                        msgBld.putInt(vBytes.length)
                                .putBytes(vBytes);
                        break;
                    default:
                        throw new RuntimeException("not supported type");
                }
            }
            messageStream.putMessage(msgBld.build());

            rowCnt++;
        }

        qr.close();

        return rowCnt;
    }

    private void handleQuery(Message msg) throws Exception
    {
        String query = msg.getCString();
        LOG.debug("query: " + query);

        parsedStatement = queryEngine.parse(query, null);
        executableStatement = queryEngine.bind(parsedStatement, null, null, null);
        QueryResult qr = queryEngine.execute(executableStatement, 0);

        // DDL
        if (qr == null) {
            sendCommandComplete("");
            return;
        }

        // Query

//        sendEmptyQueryResponse();

        sendRowDescription(qr);
        int rowCnt = sendDataRow(qr);

        sendCommandComplete("SELECT " + rowCnt);
    }

    private void handleParse(Message msg) throws Exception
    {
        String stmtName = msg.getCString();
        String query = msg.getCString();
        short numParam = msg.getShort();
        int[] oids = (numParam > 0 ? new int[numParam] : null);
        for (short i = 0; i < numParam; i++)
            oids[i] = msg.getInt();

        LOG.debug("stmtName=" + stmtName + ", query='" + query + "'");
        if (LOG.isDebugEnabled()) {
            for (short i = 0; i < numParam; i++)
                LOG.debug("OID[" + i + "]=" + oids[i]);
        }

        if (!stmtName.isEmpty()) {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                    "named prepared statement is not supported");
            PostgresExceptions.report(messageStream, pge);
        }

        parsedStatement = queryEngine.parse(query, oids);

        // ParseComplete
        messageStream.putMessage(Message.builder('1').build());
    }

    private void handleBind(Message msg) throws Exception
    {
        String portalName = msg.getCString();
        String stmtName = msg.getCString();

        LOG.debug("bind (portalName=" + portalName + ", stmtName=" + stmtName + ")");

        short numParamFormat = msg.getShort();
        short[] paramFormats = new short[numParamFormat];
        for (short i = 0; i < numParamFormat; i++)
            paramFormats[i] = msg.getShort();

        short numParamValue = msg.getShort();
        byte[][] paramValues = new byte[numParamValue][];
        for (short i = 0; i < numParamValue; i++) {
            int paramLen = msg.getInt(); // -1 indicates NULL parameter
            paramValues[i] = (paramLen > -1 ? msg.getBytes(paramLen) : null);
        }

        short numResult = msg.getShort();
        short[] resultFormats = new short[numResult];
        for (short i = 0; i < numResult; i++)
            resultFormats[i] = msg.getShort();

        if (LOG.isDebugEnabled()) {
            for (short i = 0; i < numParamFormat; i++)
                LOG.debug("paramFormats[" + i + "]=" + paramFormats[i]);
            for (short i = 0; i < numParamValue; i++)
                LOG.debug("paramValues[" + i + "]=" + paramValues[i]);
            for (short i = 0; i < numResult; i++)
                LOG.debug("resultFormats[" + i + "]=" + resultFormats[i]);
        }

        if (!portalName.isEmpty() || !stmtName.isEmpty()) {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                    "named prepared statement is not supported");
            PostgresExceptions.report(messageStream, pge);
        }

        executableStatement = queryEngine.bind(parsedStatement, paramFormats, paramValues, resultFormats);

        // BindComplete
        messageStream.putMessage(Message.builder('2').build());
    }

    // FIXME: describe and execute (refactor code)
    QueryResult queryResult;

    private void handleExecute(Message msg) throws Exception
    {
        String portalName = msg.getCString();
        int numRows = msg.getInt();

        LOG.debug("execute (portalName=" + portalName + ", numRows=" + numRows + ")");

        if (!portalName.isEmpty()) {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                    "named prepared statement is not supported");
            PostgresExceptions.report(messageStream, pge);
        }

        if (queryResult == null) { // DDL
            sendCommandComplete("");
            return;
        }

//        sendEmptyQueryResponse();

        int rowCnt = sendDataRow(queryResult);
        queryResult = null;

        sendCommandComplete("SELECT " + rowCnt);
    }

    private void handleClose(Message msg) throws IOException
    {
        char type = msg.getChar(); // 'S' for a prepared statement, 'P' for a portal
        String name = msg.getCString();

        LOG.debug("close (type='" + type + "', name=" + name + ")");

        // FIXME
        // CloseComplete
        messageStream.putMessage(Message.builder('3').build());
    }

    private void handleDescribe(Message msg) throws Exception
    {
        char type = msg.getChar(); // 'S' for a prepared statement, 'P' for a portal
        String name = msg.getCString();

        LOG.debug("describe (type='" + type + "', name=" + name + ")");

        QueryResult qr = queryEngine.execute(executableStatement, 0);
        if (qr == null && parsedStatement.isDdl()) {
            // NoData
            messageStream.putMessage(Message.builder('n').build());
            return;
        }

        queryResult = qr;
        sendRowDescription(queryResult);
    }

    void close()
    {
        try {
            clientChannel.close();
        } catch (IOException e) { }

        metaContext.close();

        eventHandler.onClose(this);
    }

    void cancel()
    {
        // TODO
    }
}

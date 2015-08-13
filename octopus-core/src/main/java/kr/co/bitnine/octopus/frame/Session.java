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

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.libpq.Message;
import kr.co.bitnine.octopus.postgres.libpq.MessageStream;
import kr.co.bitnine.octopus.postgres.libpq.ProtocolConstants;
import kr.co.bitnine.octopus.postgres.utils.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.xact.TransactionStatus;
import kr.co.bitnine.octopus.queryengine.ExecutableStatement;
import kr.co.bitnine.octopus.queryengine.ParsedStatement;
import kr.co.bitnine.octopus.queryengine.QueryEngine;
import kr.co.bitnine.octopus.queryengine.QueryResult;
import kr.co.bitnine.octopus.schema.SchemaManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.EOFException;
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
    private final QueryEngine queryEngine;

    Session(SocketChannel clientChannel, EventHandler eventHandler, MetaContext metaContext, SchemaManager schemaManager)
    {
        this.clientChannel = clientChannel;
        sessionId = new Random(this.hashCode()).nextInt();

        this.eventHandler = eventHandler;

        messageStream = new MessageStream(clientChannel);
        this.metaContext = metaContext;
        this.schemaManager = schemaManager;
        queryEngine = new QueryEngine(metaContext, schemaManager);
    }

    int getId()
    {
        return sessionId;
    }

    private static final ThreadLocal<Session> localSession = new ThreadLocal<>();

    static Session currentSession()
    {
        return localSession.get();
    }

    @Override
    public void run()
    {
        localSession.set(this);
        try {
            boolean proceed = doStartup();
            if (proceed) {
                doAuthentication();

                messageLoop();
            }
        } catch (Exception e) {
            LOG.fatal(ExceptionUtils.getStackTrace(e));
        }
        localSession.remove();

        close();
    }

    void emitErrorReport(PostgresErrorData errorData) throws IOException
    {
        messageStream.putMessageAndFlush(errorData.toMessage());
    }

    void reject()
    {
        try {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.FATAL,
                    PostgresSQLState.TOO_MANY_CONNECTIONS,
                    "too many clients already, rejected");
            emitErrorReport(edata);
        } catch (IOException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        }

        close();
    }

    private final String CLIENT_PARAM_USER = "user";
    private final String CLIENT_PARAM_DATABASE = "database";
    private final String CLIENT_PARAM_ENCODING = "client_encoding";

    private Properties clientParams;
    private ParsedStatement parsedStatement = null;
    private ExecutableStatement executableStatement = null;

    private boolean doStartup() throws IOException, OctopusException
    {
        Message imsg = messageStream.getInitialMessage();
        int i = imsg.peekInt();

        if (i == ProtocolConstants.CANCEL_REQUEST_CODE) {
            handleCancelRequest(imsg);
            return false;
        }

        if (i == ProtocolConstants.SSL_REQUEST_CODE) {
            handleSSLRequest(imsg);
            return doStartup();
        }

        handleStartupMessage(imsg);
        return true;
    }

    private void handleCancelRequest(Message imsg)
    {
        imsg.getInt(); // cancel request code
        imsg.getInt(); // process ID, not used
        int cancelKey = imsg.getInt();

        // cancelKey is the same as sessionId
        eventHandler.onCancel(cancelKey);
    }

    private void handleSSLRequest(Message imsg) throws IOException, OctopusException
    {
        // TODO: SSL

        PostgresErrorData edata = new PostgresErrorData(
                PostgresSeverity.FATAL,
                PostgresSQLState.FEATURE_NOT_SUPPORTED,
                "unsupported frontend protocol");
        new OctopusException(edata).emitErrorReport();
    }

    private void handleStartupMessage(Message imsg) throws IOException, OctopusException
    {
        int version = imsg.getInt();
        if (version != ProtocolConstants.PROTOCOL_VERSION(3, 0)) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.FATAL,
                    PostgresSQLState.FEATURE_NOT_SUPPORTED,
                    "unsupported frontend protocol");
            new OctopusException(edata).emitErrorReport();
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
    private void doAuthentication() throws IOException, OctopusException
    {
        // AuthenticationCleartextPassword
        Message msg = Message.builder('R')
                .putInt(3)
                .build();
        messageStream.putMessageAndFlush(msg);

        // receive PasswordMessage
        msg = messageStream.getMessage();
        if (msg.getType() != 'p') {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.FATAL,
                    PostgresSQLState.PROTOCOL_VIOLATION,
                    "expected password response, got message type '" + msg.getType() + "'");
            new OctopusException(edata).emitErrorReport();
        }

        // verify password
        String username = clientParams.getProperty(CLIENT_PARAM_USER);
        try {
            String password = msg.getCString();
            String currentPassword = metaContext.getUserPasswordByName(username);
            if (!password.equals(currentPassword)) {
                PostgresErrorData edata = new PostgresErrorData(
                        PostgresSeverity.FATAL,
                        PostgresSQLState.INVALID_PASSWORD,
                        "password authentication failed for user " + username);
                new OctopusException(edata).emitErrorReport();
            }
        } catch (MetaException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.FATAL,
                    PostgresSQLState.PROTOCOL_VIOLATION,
                    "invalid user name '" + username + "'");
            new OctopusException(edata, e).emitErrorReport();
        }

        // AuthenticationOk
        msg = Message.builder('R')
                .putInt(0)
                .build();
        messageStream.putMessage(msg);

        // TODO: ParameterStatus

        // BackendKeyData
        msg = Message.builder('K')
                .putInt(0)          // process ID, not used
                .putInt(sessionId)
                .build();
        messageStream.putMessage(msg);
    }

    private void messageLoop() throws Exception
    {
        boolean doingExtendedQueryMessage = false;
        boolean ignoreTillSync = false;
        boolean sendReadyForQuery = true;

        while (true) {
            try {
                doingExtendedQueryMessage = false;

                if (sendReadyForQuery) {
                    Message msg = Message.builder('Z')
                            .putChar(TransactionStatus.IDLE.getIndicator())
                            .build();
                    messageStream.putMessageAndFlush(msg);
                    sendReadyForQuery = false;
                }

                Message msg = messageStream.getMessage();

                if (ignoreTillSync)
                    continue;

                char type = msg.getType();
                switch (type) {
                    case 'Q':
                        handleQuery(msg);
                        sendReadyForQuery = true;
                        break;
                    case 'P':
                        doingExtendedQueryMessage = true;
                        handleParse(msg);
                        break;
                    case 'B':
                        doingExtendedQueryMessage = true;
                        handleBind(msg);
                        break;
                    case 'E':
                        doingExtendedQueryMessage = true;
                        handleExecute(msg);
                        break;
                    case 'C':
                        doingExtendedQueryMessage = true;
                        handleClose(msg);
                        break;
                    case 'D':
                        doingExtendedQueryMessage = true;
                        handleDescribe(msg);
                        break;
                    case 'H':
                        doingExtendedQueryMessage = true;
                        messageStream.flush();
                        break;
                    case 'S':
                        ignoreTillSync = false;
                        sendReadyForQuery = true;
                        break;
                    case 'X':
                        ignoreTillSync = false;
                        LOG.info("Terminate received");
                        return;
                    case 'd':   // copy data
                    case 'c':   // copy done
                    case 'f':   // copy fail
                        break;  // ignore these messages
                    default:
                        PostgresErrorData edata = new PostgresErrorData(
                                PostgresSeverity.FATAL,
                                PostgresSQLState.PROTOCOL_VIOLATION,
                                "invalid frontend message type '" + type + "'");
                        new OctopusException(edata).emitErrorReport();
                }
            } catch (OctopusException oe) {
                switch (oe.getErrorData().severity) {
                    case PANIC: // exit Octopus
                        LOG.fatal(ExceptionUtils.getStackTrace(oe));
                        System.exit(0);
                    case FATAL: // exit Session
                        throw oe;
                }

                // ERROR
                if (doingExtendedQueryMessage)
                    ignoreTillSync = true;
                if (!ignoreTillSync)
                    sendReadyForQuery = true;
            } catch (EOFException eofe) {
                PostgresErrorData edata = new PostgresErrorData(
                        PostgresSeverity.FATAL,
                        PostgresSQLState.PROTOCOL_VIOLATION,
                        eofe.getMessage());
                emitErrorReport(edata);
                throw eofe;
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

    private void sendRowDescription(QueryResult qr, FormatCode[] formats) throws Exception
    {
        ResultSet rs = qr.unwrap(ResultSet.class);
        ResultSetMetaData rsmd = rs.getMetaData();

        int colCnt = rsmd.getColumnCount();

        // RowDescription
        Message.Builder msgBld = Message.builder('T').putShort((short) colCnt);
        for (int i = 1; i <= colCnt; i++) {
            String colName = rsmd.getColumnName(i);
            int colType = rsmd.getColumnType(i);
            PostgresType type = TypeInfo.postresTypeOfJdbcType(colType);

            msgBld.putCString(colName)
                    .putInt(0)                                  // table OID
                    .putShort((short) 0)                        // attribute number
                    .putInt(type.oid())                         // data type OID
                    .putShort((short) type.typeLength())        // data type size
                    .putInt(-1)                                 // type-specific type modifier
                    .putShort((short) FormatCode.TEXT.code());  // FIXME: not yet known
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
                        PostgresErrorData edata = new PostgresErrorData(
                                PostgresSeverity.ERROR,
                                PostgresSQLState.FEATURE_NOT_SUPPORTED,
                                "currently data type " + rsmd.getColumnType(i) + " is not supported");
                        new OctopusException(edata).emitErrorReport();
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
        String queryString = msg.getCString();
        LOG.debug("queryString: " + queryString);

        QueryResult qr = queryEngine.query(queryString);

        // DDL
        if (qr == null) {
            sendCommandComplete("");
            return;
        }

        // Query

//        sendEmptyQueryResponse();

        sendRowDescription(qr, new FormatCode[0]);
        int rowCnt = sendDataRow(qr);

        sendCommandComplete("SELECT " + rowCnt);
    }

    private void handleParse(Message msg) throws Exception
    {
        String stmtName = msg.getCString();
        String queryString = msg.getCString();
        short numParams = msg.getShort();
        PostgresType[] paramTypes = new PostgresType[numParams];
        for (short i = 0; i < numParams; i++)
            paramTypes[i] = PostgresType.ofOid(msg.getInt());

        LOG.debug("stmtName=" + stmtName + ", queryString='" + queryString + "'");
        if (LOG.isDebugEnabled()) {
            for (short i = 0; i < paramTypes.length; i++)
                LOG.debug("paramTypes[" + i + "]=" + paramTypes[i].name());
        }

        /*
         * Format of PostgreSQL's parameter is $n (starts from 1)
         * Format of Calcite's parameter is ? (same as JDBC)
         */
        queryString = queryString.replaceAll("\\$\\d+", "?");
        LOG.debug("refined queryString='" + queryString + "'");

        // FIXME: CachedPlanSource (parsed query)
        if (!stmtName.isEmpty()) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.FEATURE_NOT_SUPPORTED,
                    "named prepared statement is not supported");
            new OctopusException(edata).emitErrorReport();
        }

        parsedStatement = queryEngine.parse(queryString, stmtName, paramTypes);

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
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.FATAL,
                    PostgresSQLState.PROTOCOL_VIOLATION,
                    "named prepared statement is not supported");
            new OctopusException(edata).emitErrorReport();
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
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.FATAL,
                    PostgresSQLState.PROTOCOL_VIOLATION,
                    "named prepared statement is not supported");
            new OctopusException(edata).emitErrorReport();
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
        sendRowDescription(queryResult, new FormatCode[0]);
    }

    void close()
    {
        try {
            clientChannel.close();
        } catch (IOException e) {
            LOG.info(ExceptionUtils.getStackTrace(e));
        }

        metaContext.close();

        eventHandler.onClose(this);
    }

    void cancel()
    {
        // TODO
    }
}

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

import kr.co.bitnine.octopus.engine.QueryEngine;
import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.postgres.access.common.TupleDesc;
import kr.co.bitnine.octopus.postgres.access.transam.TransactionStatus;
import kr.co.bitnine.octopus.postgres.catalog.PostgresAttribute;
import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.executor.Tuple;
import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import kr.co.bitnine.octopus.postgres.libpq.Message;
import kr.co.bitnine.octopus.postgres.libpq.MessageStream;
import kr.co.bitnine.octopus.postgres.libpq.ProtocolConstants;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.utils.adt.Datum;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.cache.CachedQuery;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;
import kr.co.bitnine.octopus.schema.SchemaManager;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.sql.Types;
import java.util.Properties;
import java.util.Random;

public class Session implements Runnable
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

    public static Session currentSession()
    {
        Session currSess = localSession.get();
        if (currSess == null)
            throw new RuntimeException("current session does not exist");

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

    public static final String CLIENT_PARAM_USER = "user";
    public static final String CLIENT_PARAM_DATABASE = "database";
    public static final String CLIENT_PARAM_ENCODING = "client_encoding";

    private Properties clientParams;

    public String getClientParam(String key)
    {
        return clientParams.getProperty(key);
    }

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
            String currentPassword = metaContext.getUser(username).getPassword();
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
                char type = msg.getType();

                switch (type) {
                    case 'Q':
                        break;
                    case 'P':
                    case 'B':
                    case 'E':
                    case 'C':
                    case 'D':
                    case 'H':
                        doingExtendedQueryMessage = true;
                        break;
                    case 'S':
                    case 'X':
                        ignoreTillSync = false;
                        break;
                    case 'd':
                    case 'c':
                    case 'f':
                        break;
                    default:
                        PostgresErrorData edata = new PostgresErrorData(
                                PostgresSeverity.FATAL,
                                PostgresSQLState.PROTOCOL_VIOLATION,
                                "invalid frontend message type '" + type + "'");
                        new OctopusException(edata).emitErrorReport();
                }

                if (ignoreTillSync)
                    continue;

                switch (type) {
                    case 'Q':
                        handleQuery(msg);
                        sendReadyForQuery = true;
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
                        sendReadyForQuery = true;
                        break;
                    case 'X':
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

    private void sendRowDescription(TupleDesc tupDesc, FormatCode[] resultFormats) throws IOException
    {
        PostgresAttribute[] attrs = tupDesc.getAttributes();

        // RowDescription
        Message.Builder msgBld = Message.builder('T').putShort((short) attrs.length);
        for (int i = 0; i < attrs.length; i++) {
            msgBld.putCString(attrs[i].name)
                    .putInt(0)                                      // table OID
                    .putShort((short) 0)                            // attribute number
                    .putInt(attrs[i].type.oid())                    // data type OID
                    .putShort((short) attrs[i].type.typeLength())   // data type size
                    .putInt(-1);                                    // type-specific type modifier
            if (resultFormats.length > 0)
                msgBld.putShort((short) resultFormats[i].code());
            else
                msgBld.putShort((short) FormatCode.TEXT.code());
        }
        messageStream.putMessage(msgBld.build());
    }

    private void sendDataRow(TupleSet ts, int numRows) throws IOException, PostgresException
    {
        TupleDesc td = ts.getTupleDesc();

        int numAttrs = td.getNumAttributes();
        FormatCode[] resultFormats = td.getResultFormats();

        // DataRow
        while (true) {
            Tuple t = ts.next();
            if (t == null)
                break;

            Message.Builder msgBld = Message.builder('D').putShort((short) numAttrs);
            Datum[] datums = t.getDatums();
            for (int i = 0; i < datums.length; i++) {
                byte[] bytes;

                if (resultFormats[i] == FormatCode.TEXT)
                    bytes = datums[i].out().getBytes(StandardCharsets.US_ASCII);
                else
                    bytes = datums[i].send();

                msgBld.putInt(bytes.length)
                        .putBytes(bytes);
            }
            messageStream.putMessage(msgBld.build());
        }

        ts.close(); // FIXME: remove
    }

    private void handleQuery(Message msg) throws IOException, OctopusException
    {
        String queryString = msg.getCString();
        LOG.debug("queryString: " + queryString);

        // TODO: support multiple queries in a single queryString

        try {
            Portal p = queryEngine.query(queryString);
            if (p.getCachedQuery().getCommandTag() == null) {
                sendEmptyQueryResponse();
                return;
            }

            TupleSet ts = p.run(0);
            if (ts != null) {
                sendRowDescription(ts.getTupleDesc(), ts.getTupleDesc().getResultFormats());
                sendDataRow(ts, 0);
            }

            sendCommandComplete(p.getCompletionTag());
        } catch (PostgresException e) {
            new OctopusException(e.getErrorData()).emitErrorReport();
        }
    }

    private void handleParse(Message msg) throws IOException, OctopusException
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

        try {
            queryEngine.parse(queryString, stmtName, paramTypes);
        } catch (PostgresException e) {
            new OctopusException(e.getErrorData()).emitErrorReport();
        }

        // ParseComplete
        messageStream.putMessage(Message.builder('1').build());
    }

    private void handleBind(Message msg) throws IOException, OctopusException
    {
        String portalName = msg.getCString();
        String stmtName = msg.getCString();

        LOG.debug("bind (portalName=" + portalName + ", stmtName=" + stmtName + ")");

        short numParamFormat = msg.getShort();
        FormatCode[] paramFormats = new FormatCode[numParamFormat];
        for (short i = 0; i < numParamFormat; i++)
            paramFormats[i] = FormatCode.ofCode((int) msg.getShort());

        short numParamValue = msg.getShort();
        byte[][] paramValues = new byte[numParamValue][];
        for (short i = 0; i < numParamValue; i++) {
            int paramLen = msg.getInt(); // -1 indicates NULL parameter
            paramValues[i] = (paramLen > -1 ? msg.getBytes(paramLen) : null);
        }

        short numResult = msg.getShort();
        FormatCode[] resultFormats = new FormatCode[numResult];
        for (short i = 0; i < numResult; i++)
            resultFormats[i] = FormatCode.ofCode((int) msg.getShort());

        if (LOG.isDebugEnabled()) {
            for (short i = 0; i < numParamFormat; i++)
                LOG.debug("paramFormats[" + i + "]=" + paramFormats[i].name());
            for (short i = 0; i < numParamValue; i++)
                LOG.debug("paramValues[" + i + "]=" + paramValues[i]);
            for (short i = 0; i < numResult; i++)
                LOG.debug("resultFormats[" + i + "]=" + resultFormats[i].name());
        }

        try {
            queryEngine.bind(stmtName, portalName, paramFormats, paramValues, resultFormats);
        } catch (PostgresException e) {
            new OctopusException(e.getErrorData()).emitErrorReport();
        }

        // BindComplete
        messageStream.putMessage(Message.builder('2').build());
    }

    private void handleExecute(Message msg) throws IOException, OctopusException
    {
        String portalName = msg.getCString();
        int numRows = msg.getInt();

        LOG.debug("execute (portalName=" + portalName + ", numRows=" + numRows + ")");

        try {
            Portal p = queryEngine.getPortal(portalName);
            if (p.getCachedQuery().getCommandTag() == null) {
                sendEmptyQueryResponse();
                return;
            }

            TupleSet ts = p.run(numRows);
            if (ts != null)
                sendDataRow(ts, numRows);

            sendCommandComplete(p.getCompletionTag());
        } catch (PostgresException e) {
            new OctopusException(e.getErrorData()).emitErrorReport();
        }
    }

    private void handleClose(Message msg) throws IOException, OctopusException
    {
        char type = msg.getChar(); // 'S' for a prepared statement, 'P' for a portal
        String name = msg.getCString();

        LOG.debug("close (type='" + type + "', name=" + name + ")");

        try {
            switch (type) {
                case 'S':
                    queryEngine.closeCachedQuery(name);
                    break;
                case 'P':
                    queryEngine.closePortal(name);
                    break;
                default:
                    PostgresErrorData edata = new PostgresErrorData(
                            PostgresSeverity.ERROR,
                            PostgresSQLState.PROTOCOL_VIOLATION,
                            "invalid CLOSE message subtype '" + type + "'");
                    new OctopusException(edata).emitErrorReport();
            }
        } catch (PostgresException e) {
            new OctopusException(e.getErrorData()).emitErrorReport();
        }

        // CloseComplete
        messageStream.putMessage(Message.builder('3').build());
    }

    private void sendParameterDescription(PostgresType[] paramTypes) throws IOException
    {
        Message.Builder msgBld = Message.builder('t').putShort((short) paramTypes.length);
        for (PostgresType type : paramTypes)
            msgBld.putInt(type.oid());
        messageStream.putMessage(msgBld.build());
    }

    private void sendNoData() throws IOException
    {
        messageStream.putMessage(Message.builder('n').build());
    }

    private void handleDescribe(Message msg) throws IOException, OctopusException
    {
        char type = msg.getChar(); // 'S' for a prepared statement, 'P' for a portal
        String name = msg.getCString();

        LOG.debug("describe (type='" + type + "', name=" + name + ")");

        try {
            TupleDesc tupDesc;
            switch (type) {
                case 'S':
/*
                    PostgresErrorData edata = new PostgresErrorData(
                            PostgresSeverity.FATAL,
                            PostgresSQLState.FEATURE_NOT_SUPPORTED,
                            "unsupported frontend protocol");
                    new OctopusException(edata).emitErrorReport();
 */
                    CachedQuery cq = queryEngine.getCachedQuery(name);
                    sendParameterDescription(cq.getParamTypes());
                    tupDesc = cq.describe();
                    if (tupDesc == null)
                        sendNoData();
                    else
                        sendRowDescription(tupDesc, new FormatCode[0]);
                    break;
                case 'P':
                    Portal p = queryEngine.getPortal(name);
                    tupDesc = p.describe();
                    if (tupDesc == null)
                        sendNoData();
                    else
                        sendRowDescription(tupDesc, tupDesc.getResultFormats());
                    break;
                default:
                    PostgresErrorData edata = new PostgresErrorData(
                            PostgresSeverity.ERROR,
                            PostgresSQLState.PROTOCOL_VIOLATION,
                            "invalid DESCRIBE message subtype '" + type + "'");
                    new OctopusException(edata).emitErrorReport();
            }
        } catch (PostgresException e) {
            new OctopusException(e.getErrorData()).emitErrorReport();
        }
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

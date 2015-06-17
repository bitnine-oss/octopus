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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

public class Session implements Runnable
{
    private static final Log LOG = LogFactory.getLog(Session.class);

    private final SocketChannel clientChannel;
    private final int sessionId; // secret key

    public interface EventHandler
    {
        void onClose(Session session);
        void onCancel(int sessionId);
    }
    private final EventHandler eventHandler;

    private final MessageStream messageStream;

    public Session(SocketChannel clientChannel, EventHandler eventHandler)
    {
        this.clientChannel = clientChannel;
        sessionId = new Random(this.hashCode()).nextInt();

        this.eventHandler = eventHandler;

        messageStream = new MessageStream(clientChannel);
    }

    public int getId()
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

            messageLoop();
        } catch (Exception e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        }

        close();
    }

    public void reject()
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
        messageStream.putMessage(msg);

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
        String password = msg.getCString();
        if (!password.equals("bitnine")) { // FIXME
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.INVALID_PASSWORD,
                    "password authentication failed for user " + clientParams.getProperty(CLIENT_PARAM_USER));
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
                messageStream.putMessage(msg);
                ready = false;
            }

            Message msg = messageStream.getMessage();

            char type = msg.getType();
            switch (type) {
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
/*
    private void handleQuery(Message msg) throws IOException
    {
        String query = msg.getCString();
        LOG.debug("query: " + query);

        // TODO: execute query, get results

        // FIXME
        sendEmptyQueryResponse();
    }
 */
    private void handleParse(Message msg) throws Exception
    {
        String name = msg.getCString();
        String query = msg.getCString();
        short numParam = msg.getShort();
        int[] oids = (numParam > 0 ? new int[numParam] : null);
        for (short i = 0; i < numParam; i++)
            oids[i] = msg.getInt();

        LOG.debug("name=" + name + ", query='" + query + "'");
        if (LOG.isDebugEnabled()) {
            for (short i = 0; i < numParam; i++)
                LOG.debug("OID[" + i + "]=" + oids[i]);
        }

        if (!name.isEmpty()) {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                    "named prepared statement is not supported");
            PostgresExceptions.report(messageStream, pge);
        }

        // TODO: prepare

        // ParseComplete
        messageStream.putMessage(Message.builder('1').build());
    }

    private void handleBind(Message msg) throws Exception
    {
        String portalName = msg.getCString();
        String stmtName = msg.getCString();

        LOG.debug("bind (portalName=" + portalName + ", stmtName=" + stmtName + ")");

        short numParamFormat = msg.getShort();
        short[] paramFormat = new short[numParamFormat];
        for (short i = 0; i < numParamFormat; i++)
            paramFormat[i] = msg.getShort();

        short numParamValue = msg.getShort();
        byte[][] paramValue = new byte[numParamValue][];
        for (short i = 0; i < numParamValue; i++) {
            int paramLen = msg.getInt(); // -1 indicates NULL parameter
            paramValue[i] = (paramLen > -1 ? msg.getBytes(paramLen) : null);
        }

        short numResult = msg.getShort();
        short[] resultFormat = new short[numResult];
        for (short i = 0; i < numResult; i++)
            resultFormat[i] = msg.getShort();

        if (!portalName.isEmpty() || !stmtName.isEmpty()) {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                    "named prepared statement is not supported");
            PostgresExceptions.report(messageStream, pge);
        }

        // TODO: bind

        // BindComplete
        messageStream.putMessage(Message.builder('2').build());
    }

    private void handleExecute(Message msg) throws Exception
    {
        String portalName = msg.getCString();
        int numRows = msg.getInt();

        LOG.debug("execute (portalName=" + portalName + ")");

        if (!portalName.isEmpty()) {
            PostgresException pge = new PostgresException(
                    PostgresException.Severity.FATAL,
                    PostgresException.SQLSTATE.PROTOCOL_VIOLATION,
                    "named prepared statement is not supported");
            PostgresExceptions.report(messageStream, pge);
        }

        // TODO: execute
/*
        // FIXME
        sendEmptyQueryResponse();
 */
        // FIXME
        // DataRow
        String testName = "jsyang";
        byte[] testBytes = testName.getBytes(StandardCharsets.US_ASCII);
        msg = Message.builder('D')
                .putShort((short) 1)
                .putInt(testBytes.length)
                .putBytes(testBytes)
                .build();
        messageStream.putMessage(msg);

        sendCommandComplete("SELECT 1");
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

    private void handleDescribe(Message msg) throws IOException
    {
        char type = msg.getChar(); // 'S' for a prepared statement, 'P' for a portal
        String name = msg.getCString();

        LOG.debug("describe (type='" + type + "', name=" + name + ")");
/*
        // FIXME
        // NoData
        messageStream.putMessage(Message.builder('n').build());
 */
        // FIXME
        // RowDescription
        msg = Message.builder('T')
                .putShort((short) 1)
                .putCString("name")
                .putInt(0)              // table OID
                .putShort((short) 0)    // attribute number
                .putInt(1043)           // data type OID (VARCHAR)
                .putShort((short) -1)   // data type size (variable-width)
                .putInt(0)              // type-specific type modifier
                .putShort((short) 0)    // not yet known
                .build();
        messageStream.putMessage(msg);
    }

    private void close()
    {
        try {
            clientChannel.close();
        } catch (IOException e) { }

        eventHandler.onClose(this);
    }

    public void cancel()
    {
        // TODO
    }
}

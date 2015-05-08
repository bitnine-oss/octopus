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

package kr.co.bitnine.octopus.pgproto;

import static kr.co.bitnine.octopus.pgproto.Exceptions.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Properties;

public class BackendProtocol
{
    private static final Log LOG = LogFactory.getLog(BackendProtocol.class);

    private final BackendHandler eventHandler;
    private MessageStream messageStream;

    public enum TransactionStatus
    {
        IDLE('I'),          // not in transaction
        TRANSACTION('T'),   // in transaction
        ERROR('E');         // in failed transaction

        private final char indicator;

        TransactionStatus(char indicator)
        {
            this.indicator = indicator;
        }

        public char getIndicator()
        {
            return indicator;
        }
    }
    TransactionStatus txStatus;

    public BackendProtocol(BackendHandler eventHandler)
    {
        this.eventHandler = eventHandler;

        txStatus = TransactionStatus.IDLE;
    }

    public void setTransactionStatus(TransactionStatus txStatus)
    {
        this.txStatus = txStatus;
    }

    public TransactionStatus getTransactionStatus()
    {
        return txStatus;
    }

    public void execute(MessageStream messageStream) throws IOException
    {
        this.messageStream = messageStream;

        Message imsg = messageStream.getInitialMessage();
        int i = imsg.peekInt();

        if (i == 80877102) { // 0x12345678
            handleCancelRequest(imsg);
            return;
        }

        if (i == 80877103) { // 0x12345679
            handleSSLRequest(imsg);
            return;
        }

        execute(imsg);
    }

    public void emitErrorReport(ErrorData edata) throws IOException
    {
        char type;
        ErrorData.Severity severity = edata.getSeverity();

        switch (severity) {
        case PANIC:
        case FATAL:
        case ERROR:
            type = 'E'; // ErrorResponse
            break;
        default:
            type = 'N'; // NoticeResponse
        }

        Message.Builder builder = new Message.Builder(type)
                .putChar('S')
                .putCString(severity.toString())
                .putChar('C')
                .putCString(edata.getSQLState())
                .putChar('M')
                .putCString(edata.getMessage());

        // TODO: add optional fields

        builder.putChar('\0');
        messageStream.putMessage(builder.build());
    }

    private void sendReadyForQuery() throws IOException
    {
        Message msg = new Message.Builder('Z')
                .putChar(getTransactionStatus().getIndicator())
                .build();
        messageStream.putMessage(msg);
    }

    private void execute(Message imsg) throws IOException
    {
        // Start-up Phase
        handleStartupMessage(imsg);
        doAuthentication();

        /*
         * Normal Phase
         */

        boolean ready = true;
        while (true) {
            if (ready) {
                sendReadyForQuery();
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
            case 'E':
            case 'F':
            case 'C':
            case 'D':
            case 'H':
            case 'S':
                LOG.warn("message(type='" + msg.getType() + "') received, discard");
                // FIXME
                ErrorData edata = new ErrorData(ErrorData.Severity.ERROR);
                emitErrorReport(edata);
                break;
            case 'X':
                LOG.info("Terminate message received");
                return;
            case 'd':   // copy data
            case 'c':   // copy done
            case 'f':   // copy fail
                break;  // ignore these messages
            default:
                // FIXME
                edata = new ErrorData(ErrorData.Severity.FATAL);
                emitErrorReport(edata);
                throw new ProtocolViolationException("invalid message type " + type);
            }
        }
    }

    private void handleStartupMessage(Message imsg) throws IOException
    {
        int version = imsg.getInt();
        if (version != 196608) // 0x00030000 (3.0)
            throw new UnsupportedProtocolException("client protocol version " + version);

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
                LOG.debug("paramName=" + key + ", paramValue=" + val);
            }
        }

        eventHandler.onStartupMessage(params);
    }

    private void doAuthentication() throws IOException
    {
        doCleartextAuth();
    }

    private void doCleartextAuth() throws IOException
    {
        // AuthenticationCleartextPassword
        Message msg = new Message.Builder('R')
                .putInt(3)
                .build();
        messageStream.putMessage(msg);

        msg = messageStream.getMessage();
        if (msg.getType() != 'p')
            throw new ProtocolViolationException("PasswordMessage expected (type='" + msg.getType() + "')");

        String password = msg.getCString();
        if (!eventHandler.onPasswordMessage(password)) {
            // FIXME
            ErrorData edata = new ErrorData(ErrorData.Severity.FATAL);
            emitErrorReport(edata);
            throw new IOException("authentication failed");
        }

        // AuthenticationOk
        msg = new Message.Builder('R')
                .putInt(0)
                .build();
        messageStream.putMessage(msg);

        int secretKey = eventHandler.onAuthenticationOk();

        // BackendKeyData
        msg = new Message.Builder('K')
                .putInt(0)          // process ID, not used
                .putInt(secretKey)
                .build();
        messageStream.putMessage(msg);
    }

    private void handleQuery(Message msg) throws IOException
    {
        String query = msg.getCString();

        LOG.debug("query: " + query);

        eventHandler.onQuery(query);
    }

    private void handleParse(Message msg) throws IOException
    {
        String name = msg.getCString();
        String query = msg.getCString();
        short numParam = msg.getShort();
        int[] oids = new int[numParam];
        for (short i = 0; i < numParam; i++)
            oids[i] = msg.getInt();

        LOG.debug("name=" + name + ", query='" + query + "'");
        if (LOG.isDebugEnabled()) {
            for (short i = 0; i < numParam; i++)
                LOG.debug("OID[" + i + "]=" + oids[i]);
        }

        eventHandler.onParse(name, query, oids);
    }

    private void handleCancelRequest(Message imsg) throws IOException
    {
        imsg.getInt(); // cancel request code
        imsg.getInt(); // process ID
        int cancelKey = imsg.getInt();

        eventHandler.onCancelRequest(cancelKey);
    }

    // TODO
    private void handleSSLRequest(Message imsg) throws IOException
    {
        throw new UnsupportedProtocolException("unsupported protocol: SSLRequest");
    }
}

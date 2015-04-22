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
import java.nio.channels.SocketChannel;
import java.util.Properties;

public class BackendProtocol
{
    private static final Log LOG = LogFactory.getLog(BackendProtocol.class);

    private final MessageStream msgStream;
    private final BackendListener msgListener;

    public BackendProtocol(SocketChannel clientChannel, BackendListener msgListener)
    {
        msgStream = new MessageStream(clientChannel);
        this.msgListener = msgListener;
    }

    public void execute() throws IOException
    {
        startupPhase();
        normalPhase();
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
        msgStream.putMessage(builder.build());
    }

    private void startupPhase() throws IOException
    {
        doStartup();
        doAuthentication();
    }

    private void doStartup() throws IOException
    {
        Message msg = msgStream.getInitialMessage();

        int version = msg.getInt();
        if (version != 196608) // 0x00030000 (3.0)
            throw new UnsupportedProtocolException("client protocol version " + version);

        Properties params = new Properties();
        while (true) {
            String paramName = msg.getCString();
            if (paramName.length() == 0)
                break;
            String paramValue = msg.getCString();
            params.setProperty(paramName, paramValue);
        }

        if (LOG.isDebugEnabled()) {
            for (String key : params.stringPropertyNames()) {
                String val = params.getProperty(key);
                LOG.debug("paramName=" + key + ", paramValue=" + val);
            }
        }

        msgListener.startupMessage(this, params);
    }

    private void doAuthentication() throws IOException
    {
        Message msg = new Message.Builder('R')
                .putInt(3) // AuthenticationCleartextPassword
                .build();
        msgStream.putMessage(msg);

        msg = msgStream.getMessage(); // TODO: EOFException, no password
        if (msg.getType() != 'p')
            throw new ProtocolViolationException("PasswordMessage expected (type='" + msg.getType() + "')");
        String password = msg.getCString();
        if (!msgListener.passwordMessage(this, password)) {
            ErrorData edata = new ErrorData(ErrorData.Severity.FATAL);
            emitErrorReport(edata);
            throw new IOException("authentication failed");
        }

        msg = new Message.Builder('R')
                .putInt(0) // AuthenticationOk
                .build();
        msgStream.putMessage(msg);
    }

    // FIXME
    private void normalPhase() throws IOException
    {
        while (true) {
            Message msg = new Message.Builder('Z') // ReadyForQuery
                    .putChar('I') // idle
                    .build();
            msgStream.putMessage(msg);

            msg = msgStream.getMessage();
            if (msg.getType() == 'X') {
                LOG.info("Terminate message received");
                break;
            } else {
                LOG.warn("message(type='" + msg.getType() + "') received, discard");
                ErrorData edata = new ErrorData(ErrorData.Severity.ERROR);
                emitErrorReport(edata);
            }
        }
    }
}

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

import static kr.co.bitnine.octopus.pgproto.Exceptions.*;

import kr.co.bitnine.octopus.pgproto.BackendHandler;
import kr.co.bitnine.octopus.pgproto.BackendProtocol;
import kr.co.bitnine.octopus.pgproto.ErrorData;
import kr.co.bitnine.octopus.pgproto.MessageStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.Random;

public class Session implements Runnable
{
    private static final Log LOG = LogFactory.getLog(Session.class);

    private final SocketChannel clientChannel;
    private final int secretKey;

    public interface Callback
    {
        void onClose(Session session);
        void onCancelRequest(int secretKey);
    }

    private Callback callback;

    private final MessageStream messageStream;
    private final BackendProtocol protocol;

    public Session(SocketChannel clientChannel, Callback callback)
    {
        this.clientChannel = clientChannel;
        secretKey = new Random(this.hashCode()).nextInt();

        this.callback = callback;

        messageStream = new MessageStream(clientChannel);
        protocol = new BackendProtocol(new SessionBackendHandler());
    }

    public int getSecretKey()
    {
        return secretKey;
    }

    @Override
    public void run()
    {
        try {
            protocol.execute(messageStream);
        } catch (Exception e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        }

        close();
    }

    public void reject()
    {
        try {
            // FIXME
            ErrorData edata = new ErrorData(ErrorData.Severity.FATAL);
            protocol.emitErrorReport(edata);
        } catch (IOException e) { }

        close();
    }

    private void close()
    {
        try {
            clientChannel.close();
        } catch (IOException e) { }

        callback.onClose(this);
    }

    public void cancel()
    {
        // TODO
    }

    private final String CLIENT_PARAM_USER = "user";
    private final String CLIENT_PARAM_DATABASE = "database";
    private final String CLIENT_PARAM_ENCODING = "client_encoding";

    private Properties clientParams;

    private class SessionBackendHandler implements BackendHandler
    {
        @Override
        public void onStartupMessage(Properties params)
        {
            clientParams = params;
        }

        @Override
        public void onCancelRequest(int secretKey)
        {
            callback.onCancelRequest(secretKey);
        }

        @Override
        public boolean onPasswordMessage(String password)
        {
            // FIXME: error report ErrorData FATAL
            return password.equals("bitnine");
        }

        @Override
        public int onAuthenticationOk()
        {
            return secretKey;
        }

        @Override
        public void onQuery(String query)
        {
            // TODO
        }

        @Override
        public void onParse(String name, String query, int[] oids) throws IOException
        {
            if (!name.isEmpty())
                throw new UnsupportedProtocolException("named prepared statement is not supported");
        }
    }
}

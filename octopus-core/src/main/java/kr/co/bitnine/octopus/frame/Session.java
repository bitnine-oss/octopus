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

import kr.co.bitnine.octopus.pgproto.BackendListener;
import kr.co.bitnine.octopus.pgproto.BackendProtocol;
import kr.co.bitnine.octopus.pgproto.ErrorData;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Properties;

public class Session implements Runnable
{
    private static final Log LOG = LogFactory.getLog(Session.class);

    private final SocketChannel clientChannel;
    private final BackendProtocol proto;

    private String user;
    private String database;
    private String clientEncoding;

    public Session(SocketChannel clientChannel)
    {
        this.clientChannel = clientChannel;
        proto = new BackendProtocol(clientChannel, new SessionBackendListener());

        user = null;
        database = null;
        clientEncoding = null;
    }

    @Override
    public void run()
    {
        try {
            proto.execute();
        } catch (Exception e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
            // TODO: cleanup session
        }
    }

    public void reject()
    {
        try {
            ErrorData edata = new ErrorData(ErrorData.Severity.FATAL);
            proto.emitErrorReport(edata); // FIXME
            clientChannel.close();
        } catch (IOException e) { }
    }

    private class SessionBackendListener implements BackendListener
    {
        @Override
        public void startupMessage(BackendProtocol proto, Properties params)
        {
            // FIXME
            user = params.getProperty("user");
            database = params.getProperty("database");
            clientEncoding = params.getProperty("client_encoding");
        }

        @Override
        public boolean passwordMessage(BackendProtocol proto, String password)
        {
            // FIXME: error report ErrorData FATAL
            return password.equals("bitnine");
        }
    }
}

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

import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.util.NetUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.AbstractService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class SessionServer extends AbstractService {
    private static final Log LOG = LogFactory.getLog(SessionServer.class);

    private static final int EXECUTOR_MAX_DEFAULT = 8;
    private static final long EXECUTOR_KEEPALIVE_DEFAULT = 60;
    private static final long EXECUTOR_SHUTDOWN_TIMEOUT_DEFAULT = 5;

    private final SessionFactory sessionFactory;

    private Map<Integer, Session> sessions;
    private ThreadPoolExecutor executor;
    private Listener listener;
    private volatile boolean running;

    public SessionServer(SessionFactory sessionFactory) {
        super(SessionServer.class.getSimpleName());

        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {
        super.serviceInit(conf);

        LOG.info("initialize service - " + getName());

        sessions = new ConcurrentHashMap<>();

        int sessMax = conf.getInt(OctopusConfiguration.MASTER_SESSION_MAX,
                EXECUTOR_MAX_DEFAULT);
        LOG.debug("create ThreadPoolExecutor for sessions (" + OctopusConfiguration.MASTER_SERVER_ADDRESS + '=' + sessMax + ')');
        executor = new ThreadPoolExecutor(0, sessMax,
                EXECUTOR_KEEPALIVE_DEFAULT, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>());

        listener = new Listener();
        LOG.debug("thread " + listener.getName() + " is created");
    }

    @Override
    protected void serviceStart() throws Exception {
        LOG.info("start service - " + getName());

        running = true;
        LOG.debug("start " + listener.getName());
        listener.start();

        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        LOG.info("stop service - " + getName());

        LOG.debug("interrupt " + listener.getName());
        running = false;
        listener.interrupt();
        listener.join();
        listener = null;

        LOG.debug("shutdown ThreadPoolExecutor of sessions");
        executor.shutdownNow();
        boolean terminated = executor.awaitTermination(
                EXECUTOR_SHUTDOWN_TIMEOUT_DEFAULT, TimeUnit.SECONDS);
        if (!terminated)
            LOG.warn("there was remaining sessions still running, killed forcibly");
        executor = null;

        super.serviceStop();
    }

    private class Listener extends Thread {
        private final InetSocketAddress bindAddress;
        private final ServerSocketChannel acceptChannel;

        Listener() throws IOException {
            setName("SessionServer Listener");

            String addr = getConfig().get(
                    OctopusConfiguration.MASTER_SERVER_ADDRESS);
            bindAddress = NetUtils.createSocketAddr(addr);

            acceptChannel = ServerSocketChannel.open();
            acceptChannel.bind(bindAddress);
            LOG.debug("server socket is bound to " + bindAddress);
        }

        @Override
        public void run() {
            Session.EventHandler sessEvtHandler = new Session.EventHandler() {
                @Override
                public void onClose(Session session) {
                    unregisterSession(session);
                }

                @Override
                public void onCancel(int sessionId) {
                    cancelSession(sessionId);
                }
            };

            LOG.info("start listening on " + bindAddress);
            while (running) {
                SocketChannel clientChannel = null;
                try {
                    clientChannel = acceptChannel.accept();
                } catch (IOException e) {
                    if (running)
                        LOG.error("accept failed\n" + ExceptionUtils.getStackTrace(e));
                }
                if (clientChannel == null)
                    continue;

                String clientAddress = "/unknown";
                try {
                    clientAddress = clientChannel.getRemoteAddress().toString();
                } catch (IOException ignore) { }
                LOG.info("connection from " + clientAddress + " is accepted");

                Session sess = sessionFactory.createSession(
                        clientChannel, sessEvtHandler);
                registerSession(sess);
                try {
                    executor.execute(sess);
                } catch (RejectedExecutionException e) {
                    sess.reject();
                    LOG.error("session full: connection from " + clientAddress + " is rejected");
                }
            }
            LOG.info("stop listening from " + bindAddress);

            LOG.debug("close server socket bound to " + bindAddress);
            try {
                acceptChannel.close();
            } catch (IOException ignore) { }
        }
    }

    private void registerSession(Session session) {
        sessions.put(session.getId(), session);
    }

    private void unregisterSession(Session session) {
        sessions.remove(session.getId());
    }

    private void cancelSession(int sessionId) {
        Session sess = sessions.get(sessionId);
        if (sess != null)
            sess.cancel();
    }
}

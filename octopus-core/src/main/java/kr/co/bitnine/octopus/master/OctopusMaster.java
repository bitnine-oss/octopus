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

package kr.co.bitnine.octopus.master;

import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.util.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.AbstractService;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.util.ShutdownHookManager;

public class OctopusMaster extends CompositeService
{
    public static final int SHUTDOWN_HOOK_PRIORITY = 0;

    private static final Log LOG = LogFactory.getLog(OctopusMaster.class);

    public OctopusMaster()
    {
        super(OctopusMaster.class.getName());
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception
    {
        addIfService(
                new AbstractService(OctopusMaster.class.getSimpleName())
                {
                    private final Runnable spin = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try {
                                while (!Thread.interrupted()) {
                                    Thread.sleep(5 * 1000);
                                    LOG.info("spinning");
                                }
                            } catch (InterruptedException e) {
                                LOG.info("interrupted");
                            }
                        }
                    };

                    private Thread thr = null;

                    @Override
                    protected void serviceStart() throws Exception
                    {
                        thr = new Thread(spin);
                        thr.start();
                        LOG.info("spin started");

                        super.serviceStart();
                    }

                    @Override
                    protected void serviceStop() throws Exception
                    {
                        thr.interrupt();
                        thr.join();
                        LOG.info("spin stopped");

                        super.serviceStop();
                    }
                });

        super.serviceInit(conf);
    }

    public static void main(String[] args)
    {
        StringUtils.startupShutdownMessage(OctopusMaster.class, args, LOG);

        OctopusMaster master = new OctopusMaster();

        CompositeServiceShutdownHook hook = new CompositeServiceShutdownHook(master);
        ShutdownHookManager.get().addShutdownHook(hook, SHUTDOWN_HOOK_PRIORITY);

        Configuration conf = new OctopusConfiguration();
        master.init(conf);
        master.start();
        LOG.info("started");
    }
}

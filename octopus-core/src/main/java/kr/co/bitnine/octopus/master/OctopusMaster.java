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
import kr.co.bitnine.octopus.frame.ConnectionManager;
import kr.co.bitnine.octopus.frame.SessionFactory;
import kr.co.bitnine.octopus.frame.SessionFactoryImpl;
import kr.co.bitnine.octopus.frame.SessionServer;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.MetaStoreService;
import kr.co.bitnine.octopus.meta.MetaStores;
import kr.co.bitnine.octopus.schema.SchemaManager;
import kr.co.bitnine.octopus.util.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.CompositeService;
import org.apache.hadoop.util.ShutdownHookManager;

public final class OctopusMaster extends CompositeService {
    public static final int SHUTDOWN_HOOK_PRIORITY = 30;

    private static final Log LOG = LogFactory.getLog(OctopusMaster.class);

    public OctopusMaster() {
        super(OctopusMaster.class.getName());
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {
        MetaStore metaStore = MetaStores.newInstance(
                conf.get(OctopusConfiguration.METASTORE_CLASS));
        MetaStoreService metaStoreService = new MetaStoreService(metaStore);
        addService(metaStoreService);

        ConnectionManager connectionManager = new ConnectionManager(metaStore);
        addService(connectionManager);

        SchemaManager schemaManager = SchemaManager.getSingletonInstance(metaStore);
        addService(schemaManager);

        SessionFactory sessFactory = new SessionFactoryImpl(
                metaStore, connectionManager, schemaManager);
        SessionServer sessServer = new SessionServer(sessFactory);
        addService(sessServer);

        super.serviceInit(conf);
    }

    @Override
    public String getName() {
        return "OctopusMaster";
    }

    private void initAndStart(Configuration conf) {
        CompositeServiceShutdownHook hook =
                new CompositeServiceShutdownHook(this);
        ShutdownHookManager.get().addShutdownHook(hook, SHUTDOWN_HOOK_PRIORITY);

        init(conf);
        start();
    }

    public static void main(String[] args) {
        StringUtils.startupShutdownMessage(OctopusMaster.class, args, LOG);

        OctopusMaster master = new OctopusMaster();
        master.initAndStart(new OctopusConfiguration());
    }
}

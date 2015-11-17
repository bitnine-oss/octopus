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

package kr.co.bitnine.octopus.meta;

import kr.co.bitnine.octopus.meta.logs.UpdateLoggerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.AbstractService;

import java.util.Map;
import java.util.Properties;

public class MetaStoreService extends AbstractService {
    private static final Log LOG = LogFactory.getLog(MetaStoreService.class);

    private final Properties props = new Properties();
    private final MetaStore metaStore;
    private final UpdateLoggerFactory updateLoggerFactory;

    public MetaStoreService(MetaStore metaStore,
                            UpdateLoggerFactory updateLoggerFactory) {
        super(metaStore.getClass().getName());

        this.metaStore = metaStore;
        this.updateLoggerFactory = updateLoggerFactory;
    }

    @Override
    protected final void serviceInit(Configuration conf) throws Exception {
        LOG.info("initialize service - " + getName());

        props.clear();
        for (Map.Entry<String, String> e : conf) {
            String key = e.getKey();
            if (key.startsWith("metastore."))
                props.put(key, e.getValue());
        }

        super.serviceInit(conf);
    }

    @Override
    protected final void serviceStart() throws Exception {
        LOG.info("start service - " + getName());

        metaStore.start(props, updateLoggerFactory);
        MetaStores.initialize(metaStore);

        super.serviceStart();
    }

    @Override
    protected final void serviceStop() throws Exception {
        LOG.info("stop service - " + getName());

        metaStore.stop();

        super.serviceStop();
    }
}

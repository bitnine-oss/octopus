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

package kr.co.bitnine.octopus.meta.jdo;

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.meta.MetaStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import java.util.Properties;

public class JDOMetaStore implements MetaStore
{
    private static final Log LOG = LogFactory.getLog(JDOMetaStore.class);

    private static PersistenceManagerFactory pmf = null;

    @Override
    public void start(Properties conf) throws MetaException
    {
        Properties props = new Properties();
        props.setProperty("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
        props.setProperty("datanucleus.ConnectionDriverName", conf.getProperty("metastore.jdo.connection.drivername"));
        props.setProperty("datanucleus.ConnectionURL", conf.getProperty("metastore.jdo.connection.URL"));
        props.setProperty("datanucleus.ConnectionUserName", conf.getProperty("metastore.jdo.connection.username"));
        props.setProperty("datanucleus.ConnectionPassword", conf.getProperty("metastore.jdo.connection.password"));
        props.setProperty("datanucleus.schema.autoCreateAll", "true");

        if (conf.getProperty("metastore.jdo.connection.drivername").equals("org.sqlite.JDBC")) {
            // for sequences
            props.setProperty("datanucleus.valuegeneration.transactionAttribute", "UsePM");
            // connection pooling occurs NullPointerException
            props.setProperty("datanucleus.connectionPoolingType", "None");
            props.setProperty("datanucleus.connectionPoolingType.nontx", "None");
        }

        pmf = JDOHelper.getPersistenceManagerFactory(props);
    }

    @Override
    public void stop()
    {
        pmf.close();
    }

    @Override
    public MetaContext getMetaContext()
    {
        return new JDOMetaContext(pmf.getPersistenceManager());
    }
}

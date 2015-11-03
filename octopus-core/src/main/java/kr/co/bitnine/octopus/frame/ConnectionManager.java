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

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.service.AbstractService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionManager extends AbstractService {
    private static final Log LOG = LogFactory.getLog(ConnectionManager.class);
    private static final String DRIVER_PREFIX = "jdbc:apache:commons:dbcp:";

    private final MetaStore metaStore;

    private PoolingDriver poolingDriver;

    public ConnectionManager(MetaStore metaStore) {
        super(ConnectionManager.class.getSimpleName());

        this.metaStore = metaStore;
    }

    @Override
    protected void serviceInit(Configuration conf) throws Exception {
        LOG.info("initialize service - " + getName());

        Class.forName("org.apache.commons.dbcp2.PoolingDriver");
        poolingDriver = (PoolingDriver) DriverManager.getDriver(DRIVER_PREFIX);

        super.serviceInit(conf);
    }

    @Override
    protected void serviceStart() throws Exception {
        LOG.info("start service - " + getName());

        MetaContext mc = metaStore.getMetaContext();
        for (MetaDataSource ds : mc.getDataSources()) {
            registerPool(ds.getName(), ds.getDriverName(),
                    ds.getConnectionString());
        }
        mc.close();

        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        LOG.info("stop service - " + getName());

        DriverManager.deregisterDriver(poolingDriver);
        for (String poolName : poolingDriver.getPoolNames())
            poolingDriver.closePool(poolName);

        super.serviceStop();
    }

    public void registerPool(String dataSourceName, String driverName,
                             String connectionString)
            throws ClassNotFoundException {
        LOG.debug("register connection pool of the data source '" + dataSourceName + '"');

        Class.forName(driverName);
        ObjectPool<PoolableConnection> connectionPool =
                createPool(connectionString);
        poolingDriver.registerPool(dataSourceName, connectionPool);
    }

    private ObjectPool<PoolableConnection> createPool(String connectionString) {
        // A ConnectionFactory that the pool will use to create Connections.
        ConnectionFactory connectionFactory =
                new DriverManagerConnectionFactory(connectionString, null);
        // PoolableConnectionFactory wraps the real Connections with the
        // classes that implement the pooling functionality.
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);
        poolableConnectionFactory.setValidationQuery("SELECT 1");

        // Actual pool of connections.
        GenericObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);
        connectionPool.setMaxTotal(16);
        connectionPool.setTestOnBorrow(true);
        // Set the factory's pool property to the owning pool.
        poolableConnectionFactory.setPool(connectionPool);

        return connectionPool;
    }

    public void closePool(String dataSourceName) {
        LOG.debug("close connection pool of the data source '" + dataSourceName + '"');

        try {
            poolingDriver.closePool(dataSourceName);
        } catch (SQLException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public static Connection getConnection(String dataSourceName)
            throws SQLException {
        LOG.debug("try to get a connection to the data source '" + dataSourceName + '"');
        return DriverManager.getConnection(DRIVER_PREFIX + dataSourceName);
    }
}

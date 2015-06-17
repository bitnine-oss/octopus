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

package kr.co.bitnine.octopus.schema;

import kr.co.bitnine.octopus.schema.model.MUser;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Properties;

/**
 * Extracts metadata from source databases
 * and store the metadata in Octopus MetaStore
 */
public class OctopusSchemaManager
{
    private static final Log LOG = LogFactory.getLog(OctopusSchemaManager.class);

    private final String DBINFO_SUFFIX = "-dbinfo.xml";

    private final MetaStore metastore;
    private final String username;

    public OctopusSchemaManager(String username)
    {
        metastore = new MetaStore();
        this.username = username;

        Properties prop = new Properties();
        prop.setProperty("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
//        prop.setProperty("javax.jdo.option.ConnectionURL", "jdbc:postgresql://localhost:5436/octopus");
//        prop.setProperty("javax.jdo.option.ConnectionDriverName", "org.postgresql.Driver");
//        prop.setProperty("javax.jdo.option.ConnectionUserName", "kisung");
//        prop.setProperty("javax.jdo.option.ConnectionPassword", "bitnine123");
        prop.setProperty("datanucleus.ConnectionURL", "jdbc:postgresql://localhost:5436/octopus");
        prop.setProperty("datanucleus.ConnectionDriverName", "org.postgresql.Driver");
        prop.setProperty("datanucleus.ConnectionUserName", "kisung");
        prop.setProperty("datanucleus.ConnectionPassword", "bitnine123");
        prop.setProperty("datanucleus.schema.autoCreateAll", "true");

        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            MUser user = new MUser();
            pm.makePersistent(user);
            tx.commit();
        }
        finally {
            if (tx.isActive())
                tx.rollback();
            pm.close();
        }

    }

    public void load() throws Exception
    {
        String filename = username + DBINFO_SUFFIX;

        try {
            XMLConfiguration config = new XMLConfiguration(filename);
            config.setSchemaValidation(true);
            config.validate();

            List<HierarchicalConfiguration> databases = config.configurationsAt("database");
            for (HierarchicalConfiguration database : databases) {
                String name = database.getString("name", null);
                String type = database.getString("type");
                HierarchicalConfiguration conn = database.configurationAt("connection");

                switch (type) {
                case "jdbc":
                    loadJdbc(conn, name);
                    break;
                default:
                    throw new RuntimeException("invalid type");
                }
            }
        } catch (ConfigurationException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
            throw new Exception(e);
        }
    }

    private void loadJdbc(HierarchicalConfiguration connection, String name) throws Exception
    {
        String driver = connection.getString("driver");
        String url = connection.getString("connectionString");

        Database db = new Database(getConnection(driver, url));
        metastore.add(name, db);
    }

    private Connection getConnection(String driver, String url) throws Exception
    {
        Class.forName(driver);
        return DriverManager.getConnection(url);
    }

    public SchemaPlus getSchema()
    {
        return metastore.getSchema();
    }
}

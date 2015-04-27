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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.*;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.DataSetTableModel;
import org.apache.metamodel.jdbc.JdbcDataContext;
import org.apache.metamodel.query.FromItem;
import org.apache.metamodel.query.JoinType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.schema.Relationship;
import org.apache.metamodel.schema.Schema;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.TableType;

/* extract schema from source databases and store the metadata in Octopus Metastore */
public class OctopusSchemaManager {

    private MetaStore metastore;

    public static void main(String[] args) {
        OctopusSchemaManager osm = new OctopusSchemaManager();
        /* Read octopus configuration file */

        /* Initialize Octopus-catalog */

        /* Read data source input file
           and extract meta data information from data sources */

        osm.loadDBInfoFile("kskim");
        //osm.extractAndStoreSchemaInformation();
    }


    protected Connection getConnection(String driver, String connectionString) {
        Connection connection;

        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(connectionString);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create JDBC connection", e);
        }

        return connection;
    }

    protected void loadDBInfoFile(String user) {
        // 설정 파일의 위치 디렉토리

        String filename = user + "_dbinfo.xml";

        filename = "/home/kisung/kskim_dbinfo.xml";

        metastore = new MetaStore();

        try {
            XMLConfiguration config = new XMLConfiguration(filename);

            List<HierarchicalConfiguration> databases
                    = config.configurationsAt("database");

            for (HierarchicalConfiguration conf_db : databases) {
                System.out.println("database");
                String type = conf_db.getString("type");
                if (type.equals("jdbc")) {
                    HierarchicalConfiguration connection
                            = conf_db.configurationAt("connection");
                    String name, driver, connectionString;

                    name = connection.getString("name");
                    driver = connection.getString("driver");
                    connectionString = connection.getString("connectionString");

                    Database.DBConnInfo dbconninfo = new Database.DBConnInfo(driver, connectionString);

                    /* try to connect to the database using the connect info. */
                    DataContext dc = DataContextFactory.createJdbcDataContext(
                            getConnection(driver, connectionString));

                    metastore.insertDatabase(name, type, dbconninfo, dc);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

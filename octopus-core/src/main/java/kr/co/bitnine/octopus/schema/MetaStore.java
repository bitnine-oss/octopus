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

import com.google.common.collect.ImmutableMap;
import kr.co.bitnine.octopus.schema.model.MColumn;
import kr.co.bitnine.octopus.schema.model.MDataSource;
import kr.co.bitnine.octopus.schema.model.MTable;
import kr.co.bitnine.octopus.schema.model.MUser;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.calcite.tools.Frameworks;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.metamodel.DataContext;
import org.apache.metamodel.DataContextFactory;
import org.apache.metamodel.schema.Schema;

import javax.activation.DataSource;
import javax.jdo.*;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Octopus Meta-store
 *
 * Octopus metastore has schema information; tables, columns and views.
 * It should support fast search on the schema information.
 */
public class MetaStore
{
    private PersistenceManager pm;
    private final SchemaPlus rootSchema = Frameworks.createRootSchema(false);

    public MetaStore () {

    }

    public MetaStore (Configuration conf) {
        /* initialize datanucleus */
        /* TODO: get information from octopus-site.xml */
        Properties prop = new Properties();
        prop.setProperty("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
        prop.setProperty("datanucleus.ConnectionURL", conf.get("metastore.connection.URL"));
        prop.setProperty("datanucleus.ConnectionDriverName", conf.get("metastore.connection.drivername"));
        prop.setProperty("datanucleus.ConnectionUserName", conf.get("metastore.connection.username"));
        prop.setProperty("datanucleus.ConnectionPassword", conf.get("metastore.connection.password"));
        prop.setProperty("datanucleus.schema.autoCreateAll", "true");

        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
        pm = pmf.getPersistenceManager();
    }

    public void add(String name, MDataSource dataSource) {
        rootSchema.add(name,
                new org.apache.calcite.schema.impl.AbstractSchema() {
                    private final ImmutableMap<String, org.apache.calcite.schema.Schema> subSchemaMap;

                    {
                        ImmutableMap.Builder<String, org.apache.calcite.schema.Schema> builder = ImmutableMap.builder();

//                        List<OctopusSchema> schemas = new List<OctopusSchema>();
//                        schemas.add(new OctopusSchema(dataSource));

                        for (OctopusSchema schema : schemas) {
                            String name = schema.getName();
                            if (name == null)
                                name = "__DEFAULT"; // FIXME
                            builder.put(name, schema);
                        }

                        subSchemaMap = builder.build();
                    }

                    @Override
                    public boolean isMutable()
                    {
                        return false;
                    }

                    @Override
                    protected Map<String, org.apache.calcite.schema.Schema> getSubSchemaMap()
                    {
                        return subSchemaMap;
                    }
                } );
    }

    /* return Calcite schema object */
    public SchemaPlus getSchema()
    {
        return rootSchema;
    }

    public MTable getTable(String datasource, String schema, String table) {
        return null;
    }

    public MTable getTable(String schema, String table) {
        return null;
    }

    public MTable getTable(String table) {
        Query query = pm.newQuery("javax.jdo.query.SQL", "SELECT * FROM \"MTABLE\" WHERE \"NAME\" = '" + table +"'");
        query.setClass(MTable.class);
        List<MTable> results = (List<MTable>) query.execute();
        return results.get(0);
    }

    public List<MDataSource> getDatasources()
    {
        Query query = pm.newQuery("javax.jdo.query.SQL", "SELECT * FROM \"MDATASOURCE\"");
        query.setClass(MDataSource.class);
        List<MDataSource> results = (List<MDataSource>) query.execute();
        return results;
    }

    public void addDataSource(String name, String jdbc_driver, String jdbc_connectionString, String description) throws Exception {
        // Get schema information using Metamodel

        Class.forName(jdbc_driver);
        Connection conn = DriverManager.getConnection(jdbc_connectionString);
        DataContext dc = DataContextFactory.createJdbcDataContext(conn);

        int type = 0; // TODO: make connection type enum (e.g. JDBC)
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            // create MDataSource
            MDataSource mds = new MDataSource(name, type, jdbc_driver, jdbc_connectionString, description);
            pm.makePersistent(mds);

            // read schema, table, column information and make corresponding model classes
            for (Schema schema : dc.getSchemas()) {
                String schemaName = schema.getName();
                System.out.println("schema:" + schemaName);

                for (org.apache.metamodel.schema.Table table : schema.getTables()) {
                    String tableName = table.getName();
                    System.out.println("table:" + tableName);

                    MTable mtable = new MTable(tableName, 0, "", schemaName, mds);
                    pm.makePersistent(mtable);

                    for (org.apache.metamodel.schema.Column col : table.getColumns()) {
                        String colName = col.getName();
                        System.out.println("col:" + colName);

                        int jdbcType = col.getType().getJdbcType();
                        SqlTypeName typeName = SqlTypeName.getNameForJdbcType(jdbcType);
                        MColumn mcolumn = new MColumn(colName, jdbcType, "", mtable);
                        pm.makePersistent(mcolumn);
                    }
                }
            }
            tx.commit();
            add(name, mds);
        }
        catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
        finally {
            if (tx.isActive())
                tx.rollback();
        }

    }
}

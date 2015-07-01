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
import kr.co.bitnine.octopus.schema.model.*;
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
import javax.jdo.spi.PersistenceCapable;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

/**
 * Octopus Meta-store
 *
 * Octopus metastore has schema information; tables, columns and views.
 * It should support fast search on the schema information.
 */
public class MetaStore
{
    private PersistenceManager pm;
    private PersistenceManagerFactory pmf;
    private final SchemaPlus rootSchema = Frameworks.createRootSchema(false);

    public MetaStore () {

    }

    public void finalize() {
        pm.close();
        pmf.close();
    }

    public MetaStore (Configuration conf) {
        /* initialize datanucleus */
        Properties prop = new Properties();
        prop.setProperty("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
        prop.setProperty("datanucleus.ConnectionURL", conf.get("metastore.connection.URL"));
        prop.setProperty("datanucleus.ConnectionDriverName", conf.get("metastore.connection.drivername"));
        prop.setProperty("datanucleus.ConnectionUserName", conf.get("metastore.connection.username"));
        prop.setProperty("datanucleus.ConnectionPassword", conf.get("metastore.connection.password"));
        prop.setProperty("datanucleus.schema.autoCreateAll", "true");
        /* this property is added for Sqlite */
        prop.setProperty("datanucleus.valuegeneration.transactionAttribute", "UsePM");
        prop.setProperty("datanucleus.connectionPoolingType", "None");
        prop.setProperty("datanucleus.connectionPoolingType.nontx", "None");

        pmf = JDOHelper.getPersistenceManagerFactory(prop);
        pm = pmf.getPersistenceManager();
    }

    public void add(String name, final MDataSource dataSource) {
        rootSchema.add(name,
                new org.apache.calcite.schema.impl.AbstractSchema() {
                    private final ImmutableMap<String, org.apache.calcite.schema.Schema> subSchemaMap;

                    {
                        ImmutableMap.Builder<String, org.apache.calcite.schema.Schema> builder = ImmutableMap.builder();

                        for (MSchema schema : dataSource.getSchemas()) {
                            String name = schema.getName();
                            builder.put(name,  new OctopusSchema(schema));
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
        /* fixme: not found exception */
        return results.get(0);
    }

    public List<MDataSource> getDatasources()
    {
        Query query = pm.newQuery("javax.jdo.query.SQL", "SELECT * FROM \"MDATASOURCE\"");
        query.setClass(MDataSource.class);
        List<MDataSource> results = (List<MDataSource>) query.execute();
        return results;
    }

    /* add DataSource using previously-made connection
       this method is for unit test using in-memory sqlite */
    public void addDataSource(String name, String jdbc_driver, String jdbc_connectionString, Connection conn, String description) throws Exception {
        // Get schema information using Metamodel

        DataContext dc = DataContextFactory.createJdbcDataContext(conn);

        Transaction tx = pm.currentTransaction();
        int type = 0; // TODO: make connection type enum (e.g. JDBC)
        try {
            tx.begin();
            // create MDataSource
            MDataSource mds = new MDataSource(name, type, jdbc_driver, jdbc_connectionString, description);
            pm.makePersistent(mds);

            // read schema, table, column information and make corresponding model classes
            for (Schema schema : dc.getSchemas()) {
                String schemaName = schema.getName();
                if (schemaName == null)
                    schemaName = "__DEFAULT"; // FIXME

                MSchema mschema = new MSchema(schemaName, mds);
                pm.makePersistent(mschema);

                for (org.apache.metamodel.schema.Table table : schema.getTables()) {
                    String tableName = table.getName();

                    MTable mtable = new MTable(tableName, 0, "", mschema);
                    pm.makePersistent(mtable);

                    for (org.apache.metamodel.schema.Column col : table.getColumns()) {
                        String colName = col.getName();

                        int jdbcType = col.getType().getJdbcType();
                        SqlTypeName typeName = SqlTypeName.getNameForJdbcType(jdbcType);
                        MColumn mcolumn = new MColumn(colName, jdbcType, "", mtable);
                        pm.makePersistent(mcolumn);
                    }
                }
            }
            add(name, mds);
            tx.commit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (tx.isActive())
            {
                tx.rollback();
            }
        }
    }

    public void addDataSource(String name, String jdbc_driver, String jdbc_connectionString, String description) throws Exception {
        Class.forName(jdbc_driver);
        Connection conn = DriverManager.getConnection(jdbc_connectionString);
        addDataSource(name, jdbc_driver, jdbc_connectionString, conn, description);
    }

    public MDataSource getDatasource(String datasource) {
        Query query = pm.newQuery("javax.jdo.query.SQL", "SELECT * FROM \"MDATASOURCE\" WHERE \"NAME\" = '" + datasource +"'");
        query.setClass(MDataSource.class);
        List<MDataSource> results = (List<MDataSource>) query.execute();
        /* fixme: not found exception */
        return results.get(0);
    }

    /* find a datasource having the specified table */
    /*
    public MDataSource getDatasource(String schema, String tablename) {
        Query query = pm.newQuery("javax.jdo.query.SQL",
                                  "SELECT count(*) FROM \"MDATASOURCE\" WHERE \"NAME\" = '" + datasource +"'");
        query.setClass(MDataSource.class);
        List<MDataSource> results = (List<MDataSource>) query.execute();
        // fixme: not found exception
        return results.get(0);
    }
    */
}

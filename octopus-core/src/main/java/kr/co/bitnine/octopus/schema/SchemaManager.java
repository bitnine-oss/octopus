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

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.meta.MetaStore;
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.schema.jdbc.JdbcUtils;
import kr.co.bitnine.octopus.schema.jdbc.OctopusJdbcDataSource;
import kr.co.bitnine.octopus.schema.jdbc.OctopusJdbcSchema;
import org.apache.calcite.jdbc.CalciteSchema;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.tools.Frameworks;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.service.AbstractService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Constructs in-memory schema from metadata in MetaStore
 */
public final class SchemaManager extends AbstractService {
    private static final Log LOG = LogFactory.getLog(SchemaManager.class);
    private static SchemaManager singletonInstance = null;

    private final MetaStore metaStore;
    private SchemaPlus rootSchema;

    private final Map<String, List<OctopusDataSource>> dataSourceMap;
    private final Map<String, List<OctopusSchema>> schemaMap;
    private final Map<String, List<OctopusTable>> tableMap;

    // to synchronize requests for Calcite Schema
    private final ReadWriteLock lock = new ReentrantReadWriteLock(false);
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    private SchemaManager(MetaStore metaStore) {
        super(SchemaManager.class.getName());

        this.metaStore = metaStore;
        rootSchema = Frameworks.createRootSchema(false);

        dataSourceMap = new HashMap<>();
        schemaMap = new HashMap<>();
        tableMap = new HashMap<>();
    }

    public static SchemaManager getSingletonInstance(MetaStore metaStore) {
        if (singletonInstance == null) {
            assert metaStore != null;
            singletonInstance = new SchemaManager(metaStore);
        }
        return singletonInstance;
    }

    @Override
    protected void serviceStart() throws Exception {
        loadMeta();

        super.serviceStart();
    }

    @Override
    protected void serviceStop() throws Exception {
        super.serviceStop();
        resetDataSourcePool();
        singletonInstance = null;
    }

    public void resetDataSourcePool() throws Exception {
        JdbcUtils.DataSourcePool.INSTANCE.closeAll();
    }

    private void loadMeta() throws MetaException {
        MetaContext mc = metaStore.getMetaContext();

        for (MetaDataSource dataSource : mc.getDataSources())
            addDataSource(dataSource);

        mc.close();
    }

    public void addDataSource(MetaDataSource metaDataSource) {
        // TODO: create DataSource classes according to the type of the DataSource (eg. JDBC, Catalog, MetaModel, ...)
        LOG.info("Add DataSource to Calcite Schema. DataSourceName: " + metaDataSource.getName());
        OctopusDataSource octopusDataSource = new OctopusJdbcDataSource(rootSchema, metaDataSource);
        addDataSource(octopusDataSource);
    }

    public void addDataSource(OctopusDataSource octopusDataSource) {
        writeLock.lock();
        try {
            // FIXME: all or nothing
            addToListMap(dataSourceMap, octopusDataSource.getName(), octopusDataSource);
            for (Schema cSchema : octopusDataSource.getSubSchemaMap().values()) {
                OctopusSchema schema = (OctopusSchema) cSchema;
                addToListMap(schemaMap, schema.getName(), schema);
                for (Table cTable : schema.getTableMap().values()) {
                    OctopusTable table = (OctopusTable) cTable;
                    addToListMap(tableMap, table.getName(), table);
                }
            }
            rootSchema.add(octopusDataSource.getName(), octopusDataSource);
        } finally {
            writeLock.unlock();
        }
    }

    public OctopusDataSource dropDataSource(String dataSourceName) {
        OctopusDataSource octopusDataSource = null;

        writeLock.lock();
        try {
            SchemaPlus newRootSchema = Frameworks.createRootSchema(false);
            for (String schemaName : rootSchema.getSubSchemaNames()) {
                OctopusDataSource tmp = (OctopusDataSource) CalciteSchema.from(rootSchema.getSubSchema(schemaName)).schema;
                if (schemaName.equals(dataSourceName))
                    octopusDataSource = tmp;
                else
                    newRootSchema.add(schemaName, tmp);
            }
            rootSchema = newRootSchema;

            Iterator<Map.Entry<String, List<OctopusTable>>> ti = tableMap.entrySet().iterator();
            while (ti.hasNext()) {
                Map.Entry<String, List<OctopusTable>> entry = ti.next();
                List<OctopusTable> tables = entry.getValue();
                for (Iterator<OctopusTable> tj = tables.iterator(); tj.hasNext(); ) {
                    if (tj.next().getSchema().getDataSource().getName().equals(dataSourceName))
                        tj.remove();
                }
                if (tables.isEmpty())
                    ti.remove();
            }

            Iterator<Map.Entry<String, List<OctopusSchema>>> si = schemaMap.entrySet().iterator();
            while (si.hasNext()) {
                Map.Entry<String, List<OctopusSchema>> entry = si.next();
                List<OctopusSchema> schemas = entry.getValue();
                for (Iterator<OctopusSchema> sj = schemas.iterator(); sj.hasNext(); ) {
                    if (sj.next().getDataSource().getName().equals(dataSourceName))
                        sj.remove();
                }
                if (schemas.isEmpty())
                    si.remove();
            }

            Iterator<Map.Entry<String, List<OctopusDataSource>>> di = dataSourceMap.entrySet().iterator();
            while (di.hasNext()) {
                Map.Entry<String, List<OctopusDataSource>> entry = di.next();
                List<OctopusDataSource> dataSources = entry.getValue();
                for (Iterator<OctopusDataSource> dj = dataSources.iterator(); dj.hasNext(); ) {
                    if (dj.next().getName().equals(dataSourceName))
                        dj.remove();
                }
                if (dataSources.isEmpty())
                    di.remove();
            }
        } finally {
            writeLock.unlock();
        }

        return octopusDataSource;
    }

    private <T> void addToListMap(Map<String, List<T>> map, String key, T value) {
        List<T> values = map.get(key);
        if (values == null) {
            values = new ArrayList<>();
            map.put(key, values);
        }
        values.add(value);
    }

    public SchemaPlus getCurrentSchema() {
        return rootSchema;
    }

    public List<String> toFullyQualifiedTableName(List<String> names) throws PostgresException {
        OctopusDataSource dataSource;
        OctopusSchema schema = null;
        OctopusTable table = null;
        int namesIdx = 0;

        switch (names.size()) {
        case 1: // table
            table = getUniqueTable(names.get(namesIdx));
            schema = table.getSchema();
            dataSource = schema.getDataSource();
            break;
        case 2: // schema.table
            schema = getUniqueSchema(names.get(namesIdx));
            dataSource = schema.getDataSource();
            break;
        case 3: // dataSource.schema.table
            dataSource = getUniqueDataSource(names.get(namesIdx));
            break;
        default:
            throw new RuntimeException("invalid name size: " + names.size());
        }
        namesIdx++;

        switch (names.size()) {
        case 3:
            schema = (OctopusSchema) dataSource.getSubSchema(names.get(namesIdx));
            namesIdx++;
            // fall through
        case 2:
            table = (OctopusTable) schema.getTable(names.get(namesIdx));
            // fall through
        case 1:
            break;
        default:
            throw new RuntimeException("invalid name size: " + names.size());
        }

        List<String> fqn = new ArrayList<>();
        fqn.add(dataSource.getName());
        fqn.add(schema.getName());
        fqn.add(table.getName());
        return fqn;
    }

    private OctopusTable getUniqueTable(String tableName) throws PostgresException {
        List<OctopusTable> tables = tableMap.get(tableName);
        if (tables == null || tables.size() < 1) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.UNDEFINED_TABLE,
                    "table \"" + tableName + "\" does not exist");
            throw new PostgresException(edata);
        }
        if (tables.size() > 1) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.DUPLICATE_TABLE,
                    "table \"" + tableName + "\" is ambiguous");
            throw new PostgresException(edata);
        }

        return tables.get(0);
    }

    private OctopusSchema getUniqueSchema(String schemaName) throws PostgresException {
        List<OctopusSchema> schemas = schemaMap.get(schemaName);
        if (schemas == null || schemas.size() < 1) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.UNDEFINED_SCHEMA,
                    "schema \"" + schemaName + "\" does not exist");
            throw new PostgresException(edata);
        }
        if (schemas.size() > 1) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.DUPLICATE_SCHEMA,
                    "schema \"" + schemaName + "\" is ambiguous");
            throw new PostgresException(edata);
        }

        return schemas.get(0);
    }

    private OctopusDataSource getUniqueDataSource(String dataSourceName) throws PostgresException {
        List<OctopusDataSource> dataSources = dataSourceMap.get(dataSourceName);
        if (dataSources == null || dataSources.size() < 1) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.UNDEFINED_DATABASE,
                    "data source \"" + dataSourceName + "\" does not exist");
            throw new PostgresException(edata);
        }
        if (dataSources.size() > 1) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.DUPLICATE_DATABASE,
                    "data source \"" + dataSourceName + "\" is ambiguous");
            throw new PostgresException(edata);
        }

        return dataSources.get(0);
    }

    /*
     * lockRead()/unlockRead() are used to protect rootSchema returned by
     * getCurrentSchema() because we don't know how long the schema will be used
     */

    public void lockRead() {
        readLock.lock();
    }

    public void unlockRead() {
        readLock.unlock();
    }
}

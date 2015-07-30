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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Constructs in-memory schema from metadata in MetaStore
 */
public class SchemaManager extends AbstractService
{
    private static final Log LOG = LogFactory.getLog(SchemaManager.class);

    private final MetaStore metaStore;
    private final SchemaPlus rootSchema;

    private final HashMap<String, List<OctopusDataSource>> dataSourceMap;
    private final HashMap<String, List<OctopusSchema>> schemaMap;
    private final HashMap<String, List<OctopusTable>> tableMap;

    // to synchronize requests for Calcite Schema
    private final ReadWriteLock lock = new ReentrantReadWriteLock(false);
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public SchemaManager(MetaStore metaStore)
    {
        super(SchemaManager.class.getName());

        this.metaStore = metaStore;
        rootSchema = Frameworks.createRootSchema(false);

        dataSourceMap = new HashMap();
        schemaMap = new HashMap();
        tableMap = new HashMap();
    }

    @Override
    protected void serviceStart() throws Exception
    {
        loadMeta();

        super.serviceStart();
    }

    private void loadMeta() throws MetaException
    {
        MetaContext mc = metaStore.getMetaContext();

        for (MetaDataSource dataSource : mc.getDataSources())
            addDataSource(dataSource);

        mc.close();
    }

    public void addDataSource(MetaDataSource metaDataSource)
    {
        writeLock.lock();

        // FIXME: all or nothing
        OctopusDataSource dataSource = new OctopusDataSource(metaDataSource);
        addToListMap(dataSourceMap, dataSource.getName(), dataSource);
        for (Schema cSchema : dataSource.getSubSchemaMap().values()) {
            OctopusSchema schema = (OctopusSchema) cSchema;
            addToListMap(schemaMap, schema.getName(), schema);
            for (Table cTable : schema.getTableMap().values()) {
                OctopusTable table = (OctopusTable) cTable;
                addToListMap(tableMap, table.getName(), table);
            }
        }
        rootSchema.add(metaDataSource.getName(), dataSource);

        writeLock.unlock();
    }

    private <T> void addToListMap(Map<String, List<T>> map, String key, T value)
    {
        List<T> values = map.get(key);
        if (values == null) {
            values = new ArrayList();
            map.put(key, values);
        }
        values.add(value);
    }

    public SchemaPlus getCurrentSchema()
    {
        return rootSchema;
    }

    public List<String> toFullyQualifiedTableName(List<String> names)
    {
        OctopusDataSource dataSource;
        OctopusSchema schema = null;
        OctopusTable table = null;
        int namesIdx = 0;

        switch (names.size()) {
            case 1: // table
                table = getUniqueObject(tableMap, names.get(namesIdx));
                schema = table.getSchema();
                dataSource = schema.getDataSource();
                break;
            case 2: // schema.table
                schema = getUniqueObject(schemaMap, names.get(namesIdx));
                dataSource = schema.getDataSource();
                break;
            case 3: // dataSource.schema.table
                dataSource = getUniqueObject(dataSourceMap, names.get(namesIdx));
                break;
            default:
                throw new RuntimeException("invalid name size: " + names.size());
        }
        namesIdx++;

        switch (names.size()) {
            case 3:
                schema = (OctopusSchema) dataSource.getSubSchema(names.get(namesIdx));
                namesIdx++;
                // fall-through
            case 2:
                table = (OctopusTable) schema.getTable(names.get(namesIdx));
                // fall-through
            case 1:
                break;
        }

        List<String> fqn = new ArrayList();
        fqn.add(dataSource.getName());
        fqn.add(schema.getName());
        fqn.add(table.getName());
        return fqn;
    }

    private <T> T getUniqueObject(Map<String, List<T>> map, String key)
    {
        List<T> values = map.get(key);

        if (values == null)
            ; // FIXME: not found
        if (values.size() == 0)
            ; // FIXME: not found
        else if (values.size() > 1)
            ; // FIXME: ambiguous

        return values.get(0);
    }

    /*
     * lockRead()/unlockRead() are used to protect rootSchema returned by
     * getCurrentSchema() because we don't know how long the schema will be used
     */

    public void lockRead()
    {
        readLock.lock();
    }

    public void unlockRead()
    {
        readLock.unlock();
    }
}

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

package kr.co.bitnine.octopus.schema.metamodel;

import kr.co.bitnine.octopus.meta.model.MetaTable;
import kr.co.bitnine.octopus.schema.OctopusTable;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Schemas;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTableQueryable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class OctopusMetaModelTable extends OctopusTable
        implements TranslatableTable {
    private static final Log LOG = LogFactory.getLog(OctopusMetaModelTable.class);
    private OctopusMetaModelDataSource dataSource;
    private OctopusMetaModelTableScan octopusMetaModelTableScan;

    public OctopusMetaModelTable(MetaTable metaTable, OctopusMetaModelSchema schema) {
        super(metaTable, schema);

        dataSource = (OctopusMetaModelDataSource) schema.getDataSource();

        LOG.debug("create OctopusMetaModelTable. tableName: " + getName());
    }

    @Override
    public String toString() {
        return "MetaModelTable {" + getName() + "}";
    }

    @Override
    public <T> Queryable<T> asQueryable(QueryProvider queryProvider,
                                        SchemaPlus schema, String tableName) {
        return new OctopusMetaModelQueryable<T>(queryProvider, schema, this, tableName);
    }

    @Override
    public RelNode toRel(RelOptTable.ToRelContext context, RelOptTable relOptTable) {
        octopusMetaModelTableScan = new OctopusMetaModelTableScan(context.getCluster(), relOptTable);
        return octopusMetaModelTableScan;
    }

    public Expression getExpression(SchemaPlus schema, String tableName,
                                    Class clazz) {
        return Schemas.tableExpression(schema, getElementType(), tableName, clazz);
    }

    public Enumerable<Object> project() {
        return new AbstractEnumerable<Object>() {
            public Enumerator<Object> enumerator() {
                return new OctopusMetaModelEnumerator(dataSource, getName(), octopusMetaModelTableScan);
            }
        };
    }

    private class OctopusMetaModelQueryable<T> extends AbstractTableQueryable<T> {
        OctopusMetaModelQueryable(QueryProvider queryProvider, SchemaPlus schema,
                                  OctopusMetaModelTable table, String tableName) {
            super(queryProvider, schema, table, tableName);
        }

        @Override
        public Enumerator<T> enumerator() {
            final Enumerable<T> enumerable =
                    (Enumerable<T>) getTable().project();
            return enumerable.enumerator();
        }

        private OctopusMetaModelTable getTable() {
            return (OctopusMetaModelTable) table;
        }
    }
}

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

package kr.co.bitnine.octopus.engine.calcite;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.function.Function1;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import static kr.co.bitnine.octopus.engine.calcite.CalciteMetaImpl.MetaColumn;
import static kr.co.bitnine.octopus.engine.calcite.CalciteMetaImpl.MetaTable;

/**
 * Schema that contains metadata tables such as "TABLES" and "COLUMNS".
 */
final class MetadataSchema extends AbstractSchema {
    private static final Map<String, Table> TABLE_MAP =
            ImmutableMap.<String, Table>of(
                    "COLUMNS",
                    new CalciteMetaImpl.MetadataTable<MetaColumn>(MetaColumn.class) {
                        public Enumerator<MetaColumn> enumerator(
                                final CalciteMetaImpl meta) {
                            final String catalog = meta.getConnection().getCatalog();
                            return meta.tables(catalog).selectMany(
                                    new Function1<MetaTable, Enumerable<MetaColumn>>() {
                                        public Enumerable<MetaColumn> apply(MetaTable table) {
                                            return meta.columns(table);
                                        }
                                    }).enumerator();
                        }
                    },
                    "TABLES",
                    new CalciteMetaImpl.MetadataTable<MetaTable>(MetaTable.class) {
                        public Enumerator<MetaTable> enumerator(CalciteMetaImpl meta) {
                            final String catalog = meta.getConnection().getCatalog();
                            return meta.tables(catalog).enumerator();
                        }
                    });

    public static final Schema INSTANCE = new MetadataSchema();

    /**
     * Creates the data dictionary, also called the information schema. It is a
     * schema called "metadata" that contains tables "TABLES", "COLUMNS" etc.
     */
    private MetadataSchema() {
    }

    @Override
    protected Map<String, Table> getTableMap() {
        return TABLE_MAP;
    }
}

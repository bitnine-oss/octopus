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

package kr.co.bitnine.octopus.schema.jdbc;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kr.co.bitnine.octopus.meta.model.MetaTable;
import kr.co.bitnine.octopus.schema.OctopusSchema;
import kr.co.bitnine.octopus.schema.OctopusTable;

import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.linq4j.tree.Primitive;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.rel.RelNode;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeField;
import org.apache.calcite.runtime.ResultSetEnumerable;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.TranslatableTable;
import org.apache.calcite.schema.impl.AbstractTableQueryable;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.pretty.SqlPrettyWriter;
import org.apache.calcite.sql.util.SqlString;
import org.apache.calcite.util.Pair;
import org.apache.calcite.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class OctopusJdbcTable extends OctopusTable
        implements TranslatableTable, ScannableTable {
    private OctopusJdbcDataSource dataSource;
    private static final Log LOG = LogFactory.getLog(OctopusJdbcTable.class);

    public OctopusJdbcTable(MetaTable metaTable, OctopusSchema schema) {
        super(metaTable, schema);

        dataSource = (OctopusJdbcDataSource) schema.getDataSource();
        LOG.debug("create OctopusJdbcTable. tableName: " + metaTable.getName());
    }

    @Override
    public String toString() {
        return "JdbcTable {" + getName() + "}";
    }

    SqlString generateSql() {
        final SqlNodeList selectList =
                new SqlNodeList(
                        Collections.singletonList(
                                new SqlIdentifier("*", SqlParserPos.ZERO)),
                        SqlParserPos.ZERO);
        SqlSelect node =
                new SqlSelect(SqlParserPos.ZERO, SqlNodeList.EMPTY, selectList,
                        tableName(), null, null, null, null, null, null, null);
        final SqlPrettyWriter writer = new SqlPrettyWriter(dataSource.getDialect());
        node.unparse(writer, 0, 0);
        return writer.toSqlString();
    }

    SqlIdentifier tableName() {
        final List<String> strings = new ArrayList<>();
        strings.add(dataSource.getName());
        strings.add(getSchema().getName());
        strings.add(getName());
        return new SqlIdentifier(strings, SqlParserPos.ZERO);
    }

    public RelNode toRel(RelOptTable.ToRelContext context,
                         RelOptTable relOptTable) {
        return new JdbcTableScan(context.getCluster(), relOptTable, this, dataSource.getConvention());
    }

    public <T> Queryable<T> asQueryable(QueryProvider queryProvider,
                                        SchemaPlus schema, String tableName) {
        return new JdbcTableQueryable<>(queryProvider, schema, tableName);
    }

    private List<Pair<Primitive, Integer>> fieldClasses(
            final JavaTypeFactory typeFactory) {
        final RelDataType rowType = getProtoRowType().apply(typeFactory);
        return Lists.transform(rowType.getFieldList(),
                new Function<RelDataTypeField, Pair<Primitive, Integer>>() {
                    public Pair<Primitive, Integer> apply(RelDataTypeField field) {
                        RelDataType type = field.getType();
                        Class clazz = (Class) typeFactory.getJavaClass(type);
                        return Pair.of(Util.first(Primitive.of(clazz), Primitive.OTHER),
                                type.getSqlTypeName().getJdbcOrdinal());
                    }
                });
    }

    public Enumerable<Object[]> scan(DataContext root) {
        final JavaTypeFactory typeFactory = root.getTypeFactory();
        final SqlString sql = generateSql();
        return ResultSetEnumerable.of(dataSource.getDataSource(), sql.getSql(),
                JdbcUtils.ObjectArrayRowBuilder.factory(fieldClasses(typeFactory)));
    }

    private class JdbcTableQueryable<T> extends AbstractTableQueryable<T> {
        JdbcTableQueryable(QueryProvider queryProvider, SchemaPlus schema,
                                  String tableName) {
            super(queryProvider, schema, OctopusJdbcTable.this, tableName);
        }

        @Override
        public String toString() {
            return "JdbcTableQueryable {table: " + tableName + "}";
        }

        public Enumerator<T> enumerator() {
            final JavaTypeFactory typeFactory =
                    ((CalciteConnection) queryProvider).getTypeFactory();
            final SqlString sql = generateSql();
            //noinspection unchecked
            final Enumerable<T> enumerable = (Enumerable<T>) ResultSetEnumerable.of(
                    dataSource.getDataSource(), sql.getSql(),
                    JdbcUtils.ObjectArrayRowBuilder.factory(fieldClasses(typeFactory)));
            return enumerable.enumerator();
        }
    }
}

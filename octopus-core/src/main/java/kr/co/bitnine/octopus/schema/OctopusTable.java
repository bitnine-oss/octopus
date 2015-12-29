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

import kr.co.bitnine.octopus.meta.model.MetaColumn;
import kr.co.bitnine.octopus.meta.model.MetaTable;
import org.apache.calcite.adapter.java.AbstractQueryableTable;
import org.apache.calcite.linq4j.QueryProvider;
import org.apache.calcite.linq4j.Queryable;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;

public abstract class OctopusTable extends AbstractQueryableTable {
    private Schema.TableType tableType;
    private RelProtoDataType protoRowType;
    private final OctopusSchema schema;
    private final String name;

    public OctopusTable(MetaTable metaTable, OctopusSchema schema) {
        super(Object[].class);
        name = metaTable.getName();

        try {
            //tableType = Schema.TableType.valueOf(table.getType().name());
            tableType = Schema.TableType.TABLE; // FIXME
        } catch (IllegalArgumentException e) {
            tableType = Schema.TableType.TABLE;
        }

        RelDataTypeFactory typeFactory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
        RelDataTypeFactory.FieldInfoBuilder fieldInfo = typeFactory.builder();
        for (MetaColumn metaColumn : metaTable.getColumns()) {
            String columnName = metaColumn.getName();

            //int jdbcType = metaColumn.getType().getJdbcType();
            int jdbcType = metaColumn.getType(); //FIXME
            SqlTypeName typeName = SqlTypeName.getNameForJdbcType(jdbcType);
            RelDataType sqlType = typeFactory.createSqlType(typeName);

            fieldInfo.add(columnName, sqlType);
        }
        protoRowType = RelDataTypeImpl.proto(fieldInfo.build());

        this.schema = schema;
    }

    public final String getName() {
        return name;
    }

    @Override
    public final RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
        return protoRowType.apply(relDataTypeFactory);
    }

    @Override
    public final Schema.TableType getJdbcTableType() {
        return tableType;
    }

    public final OctopusSchema getSchema() {
        return schema;
    }

    public final RelProtoDataType getProtoRowType() {
        return protoRowType;
    }

    @Override
    public abstract <T> Queryable<T> asQueryable(QueryProvider queryProvider, SchemaPlus schemaPlus, String tableName);
}

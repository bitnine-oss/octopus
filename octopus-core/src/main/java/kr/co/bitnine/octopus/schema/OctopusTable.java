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
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rel.type.RelDataTypeImpl;
import org.apache.calcite.rel.type.RelDataTypeSystem;
import org.apache.calcite.rel.type.RelProtoDataType;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;

public final class OctopusTable extends AbstractTable {
    private final String name;
    private Schema.TableType tableType;
    private final RelProtoDataType protoRowType;
    private final OctopusSchema schema;

    public OctopusTable(MetaTable metaTable, OctopusSchema schema) {
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

    public String getName() {
        return name;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
        return protoRowType.apply(relDataTypeFactory);
    }

    @Override
    public Schema.TableType getJdbcTableType() {
        return tableType;
    }

    public OctopusSchema getSchema() {
        return schema;
    }
}

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

import org.apache.calcite.rel.type.*;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeFactoryImpl;
import org.apache.calcite.sql.type.SqlTypeName;

public class OctopusTable extends AbstractTable
{
    private Schema.TableType tableType;
    private RelProtoDataType protoRowType;

    public OctopusTable(org.apache.metamodel.schema.Table table)
    {
        try {
            tableType = Schema.TableType.valueOf(table.getType().name());
        } catch (IllegalArgumentException e) {
            tableType = Schema.TableType.TABLE;
        }

        RelDataTypeFactory typeFactory = new SqlTypeFactoryImpl(RelDataTypeSystem.DEFAULT);
        RelDataTypeFactory.FieldInfoBuilder fieldInfo = typeFactory.builder();
        for (org.apache.metamodel.schema.Column col : table.getColumns()) {
            String name = col.getName();

            int jdbcType = col.getType().getJdbcType();
            SqlTypeName typeName = SqlTypeName.getNameForJdbcType(jdbcType);
            RelDataType sqlType = typeFactory.createSqlType(typeName);

            fieldInfo.add(name, sqlType);
        }
        protoRowType = RelDataTypeImpl.proto(fieldInfo.build());
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory)
    {
        return protoRowType.apply(relDataTypeFactory);
    }

    @Override
    public Schema.TableType getJdbcTableType()
    {
        return tableType;
    }
}

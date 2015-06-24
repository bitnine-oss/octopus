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
import kr.co.bitnine.octopus.schema.model.MDataSource;
import kr.co.bitnine.octopus.schema.model.MTable;
import org.apache.calcite.schema.Table;
import org.apache.calcite.schema.impl.AbstractSchema;

import java.util.Map;

public class OctopusSchema extends AbstractSchema
{
    private final String name;
    private ImmutableMap<String, Table> tableMap;

    public OctopusSchema(MDataSource datasource)
    {
        name = datasource.getName();

        ImmutableMap.Builder<String, Table> builder = ImmutableMap.builder();
        for (MTable table : datasource.getTables()) {
            String name = table.getName();
            OctopusTable octopusTable = new OctopusTable(table);
            builder.put(name, octopusTable);
        }
        tableMap = builder.build();
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean isMutable()
    {
        return false;
    }

    @Override
    protected Map<String, Table> getTableMap()
    {
        return tableMap;
    }
}

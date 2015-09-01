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

package kr.co.bitnine.octopus.sql;

public class OctopusSqlShowTables extends OctopusSqlCommand
{
    private String dataSource;
    private String schemaPattern;
    private String tablePattern;

    OctopusSqlShowTables(String dataSource, String schemaPattern, String tablePattern)
    {
        this.dataSource = dataSource;
        this.schemaPattern = schemaPattern;
        this.tablePattern = tablePattern;
    }

    String getDataSource()
    {
        return dataSource;
    }

    String getSchemaPattern()
    {
        return schemaPattern;
    }

    String getTablePattern()
    {
        return tablePattern;
    }

    @Override
    public OctopusSqlCommand.Type getType()
    {
        return OctopusSqlCommand.Type.SHOW_TABLES;
    }
}

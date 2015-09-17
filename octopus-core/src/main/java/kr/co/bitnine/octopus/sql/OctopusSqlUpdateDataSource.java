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

class OctopusSqlUpdateDataSource extends OctopusSqlCommand
{
    private final OctopusSqlCommentTarget target;

    OctopusSqlUpdateDataSource(String dataSourceName)
    {
        target = new OctopusSqlCommentTarget();
        target.type = OctopusSqlCommentTarget.Type.DATASOURCE;
        target.dataSource = dataSourceName;
    }

    OctopusSqlUpdateDataSource(String dataSourceName, String schemaPattern)
    {
        target = new OctopusSqlCommentTarget();
        target.type = OctopusSqlCommentTarget.Type.SCHEMA;
        target.dataSource = dataSourceName;
        target.schema = schemaPattern;
    }

    OctopusSqlUpdateDataSource(String dataSourceName, String schemaName, String tablePattern)
    {
        target = new OctopusSqlCommentTarget();
        target.type = OctopusSqlCommentTarget.Type.TABLE;
        target.dataSource = dataSourceName;
        target.schema = schemaName;
        target.table = tablePattern;
    }

    OctopusSqlCommentTarget getTarget()
    {
        return this.target;
    }

    @Override
    public Type getType()
    {
        return Type.UPDATE_DATASOURCE;
    }
}

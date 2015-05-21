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

package kr.co.bitnine.octopus.queryengine;

import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;

public class QueryEngine
{
    public void executeQuery(String sql) throws Exception
    {
        final SchemaPlus rootSchema = Frameworks.createRootSchema(true);
        final FrameworkConfig config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .build();
        final Planner planner = Frameworks.getPlanner(config);
        SqlNode parse = planner.parse(sql);
        SqlNode val = planner.validate(parse);

        String valStr =
                val.toSqlString(SqlDialect.DUMMY, false).getSql();

        System.out.println(valStr);
    }
}

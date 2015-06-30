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
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class QueryEngine
{
    private static final Log LOG = LogFactory.getLog(QueryEngine.class);

    // unnamed portal?
    private String unnamedSql;
    private int[] unnamedOids;

    Planner planner;

    public QueryEngine(SchemaPlus rootSchema)
    {
        FrameworkConfig config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .build();

        planner = Frameworks.getPlanner(config);
    }

    public void executeQuery(String sql) throws Exception
    {
        unnamedSql = null;
        unnamedOids = null;

        SqlNode parse = planner.parse(sql);
        LOG.debug(parse);

        SqlNode validated = planner.validate(parse);

        // TODO: interpret rel, return results
        //planner.reset();
    }

    public void prepare(String sql, int[] oids)
    {
        unnamedSql = sql;
        unnamedOids = oids;
    }

    public void bind(short[] paramFormat, byte[][] paramValue, short[] resultFormat) throws Exception
    {
        if (unnamedSql == null || unnamedOids == null)
            throw new IOException("prepared statement does not exist");

        planner.reset();

        SqlNode parse = planner.parse(unnamedSql);
        LOG.debug(parse);

        SqlNode validated = planner.validate(parse);
    }

    public void execute(int numRows) throws IOException
    {
        if (unnamedSql == null || unnamedOids == null)
            throw new IOException("prepared statement does not exist");

        // TODO: interpret rel, return results
    }
}

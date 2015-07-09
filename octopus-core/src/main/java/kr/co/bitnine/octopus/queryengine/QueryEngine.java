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
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class QueryEngine
{
    private static final Log LOG = LogFactory.getLog(QueryEngine.class);

    // unnamed portal?
    private String unnamedSql;
    private int[] unnamedOids;

    Planner planner;

    DataSourceManager dsm;

    public QueryEngine(SchemaPlus rootSchema)
    {
        FrameworkConfig config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .build();

        planner = Frameworks.getPlanner(config);
    }

    public boolean isByPassQuery(SqlNode query) {
        final Set<String> dsSet = new HashSet<String>();
        query.accept(
                new SqlShuttle() {
                    @Override
                    public SqlNode visit(SqlIdentifier identifier) {
                        // check whether this is fully qualified table name
                        if (identifier.names.size() == 3) {
                            dsSet.add(identifier.names.get(0));
                            //System.out.println("DS:" + identifier.names.get(0));
                        }
                        return identifier;
                    }
                }
        );

        return (dsSet.size() == 1);
    }

    public void executeQuery(String sql) throws Exception
    {
        unnamedSql = null;
        unnamedOids = null;

        SqlNode parse = planner.parse(sql);
        LOG.debug(parse);

        /* TODO: translate each table name to fully qualified table name */

        SqlNode validated = planner.validate(parse);

        // TODO: interpret rel, return results

        TableNameTranslator tnt = new TableNameTranslator();
        tnt.toFQN(validated);

        if (isByPassQuery(validated)) {


        }
        else {
            // query on multiple datasources
            // TODO: throw not-implemented feature
        }

        //planner.reset();
    }

    public void executeByPassQuery(SqlNode plan) {
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

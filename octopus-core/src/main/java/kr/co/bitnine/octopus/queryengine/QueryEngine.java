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

import kr.co.bitnine.octopus.schema.MetaStore;
import kr.co.bitnine.octopus.sql.OctopusSql;
import kr.co.bitnine.octopus.sql.OctopusSqlCommand;
import kr.co.bitnine.octopus.sql.OctopusSqlRunner;
import org.antlr.v4.runtime.RecognitionException;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.ResultSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryEngine
{
    private static final Log LOG = LogFactory.getLog(QueryEngine.class);

    MetaStore metaStore;

    DataSourceManager dsm;

    public QueryEngine(MetaStore metaStore)
    {
        this.metaStore = metaStore;

/*
        FrameworkConfig config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .build();

        planner = Frameworks.getPlanner(config);
 */
    }

    public ParsedStatement parse(String query, int[] oids) throws Exception
    {
        // DDL

        List<OctopusSqlCommand> commands = null;
        try {
            commands = OctopusSql.parse(query);
        } catch (RecognitionException e) {
            LOG.info(ExceptionUtils.getStackTrace(e));
        }

        if (commands != null)
            return new ParsedStatement(commands);

        // DML

        SchemaPlus rootSchema = metaStore.getCurrentSchema();

        FrameworkConfig config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .build();
        Planner planner = Frameworks.getPlanner(config);

        SqlNode parse = planner.parse(query);
        LOG.debug(parse);

        SqlNode validated = planner.validate(parse);

        return new ParsedStatement(validated, oids);
    }

    public ExecutableStatement bind(ParsedStatement parsedStatement, short[] paramFormats, byte[][] paramValues, short[] resultFormats)
    {
        return new ExecutableStatement(parsedStatement, paramFormats, paramValues, resultFormats);
    }

    private OctopusSqlRunner ddlRunner = new OctopusSqlRunner() {
        @Override
        public void createUser(String name, String password) throws Exception
        {
            metaStore.createUser(name, password);
        }

        @Override
        public void addDatasource(String datasourceName, String jdbcConnectionString) throws Exception
        {
            System.out.println("name=" + datasourceName + ", jdbcConnectionString=" + jdbcConnectionString);
        }
    };

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

    public void executeByPassQuery(SqlNode validatedQuery)
    {
        // TODO: translate each table name to fully qualified table name
        TableNameTranslator tnt = new TableNameTranslator();
        tnt.toFQN(validated);

        // TODO: interpret rel, return results
    }

    public ResultSet execute(ExecutableStatement executableStatement, int numRows) throws Exception
    {
        ParsedStatement ps = executableStatement.getParsedStatement();
        if (ps.isDdl()) {
            for (OctopusSqlCommand c : ps.getDdlCommands())
                OctopusSql.run(c, ddlRunner);

            return null;
        }

        SqlNode validatedQuery = ps.getValidatedQuery();
        if (!isByPassQuery(validatedQuery))
            throw new Exception("only by-pass query is supported");

        executeByPassQuery(validatedQuery);

        // TODO: query on multiple datasources (throw not-implemented feature)

        return null; // FIXME
    }

    public void prepare(String sql, int[] oids)
    {
    }
}

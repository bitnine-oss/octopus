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
import kr.co.bitnine.octopus.schema.model.MDataSource;
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

import java.sql.*;
import java.util.ArrayList;
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

        // Query

        SchemaPlus rootSchema = metaStore.getCurrentSchema();

        FrameworkConfig config = Frameworks.newConfigBuilder()
                .defaultSchema(rootSchema)
                .build();
        Planner planner = Frameworks.getPlanner(config);

        SqlNode parse = planner.parse(query);
        LOG.debug(parse);

        TableNameTranslator tnt = new TableNameTranslator(metaStore);
        tnt.toFQN(parse);
        LOG.debug("FQN translated: " + parse.toString());

        metaStore.getReadLock();
        SqlNode validated = planner.validate(parse);
        metaStore.releaseReadLock();

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
            String driverName = null;
            if (jdbcConnectionString.startsWith("jdbc:sqlite:")) {
                driverName = "org.sqlite.JDBC";
            } else {
                throw new RuntimeException("not supported");
            }

            metaStore.addDataSource(datasourceName, driverName, jdbcConnectionString, "");
        }
    };

    public List<String> getDatasourceNames(SqlNode query) {
        final Set<String> dsSet = new HashSet();
        query.accept(new SqlShuttle() {
            @Override
            public SqlNode visit(SqlIdentifier identifier) {
                // check whether this is fully qualified table name
                if (identifier.names.size() == 3) {
                    dsSet.add(identifier.names.get(0));
                    //System.out.println("DS:" + identifier.names.get(0));
                }
                return identifier;
            }
        });

        return new ArrayList(dsSet);
    }

    public QueryResult executeByPassQuery(SqlNode validatedQuery, String datasourceName)
    {
        MDataSource ds = metaStore.getDatasource(datasourceName);

        TableNameTranslator tnt = new TableNameTranslator(metaStore);
        tnt.toDSN(validatedQuery);
        LOG.debug("by-pass query: " + validatedQuery.toString());

        ResultSet rs = null;
        try {
            Class.forName(ds.getDriver());
            Connection conn = DriverManager.getConnection(ds.getConnectionString());
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(validatedQuery.toString());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new QueryResult(rs);
    }

    public QueryResult execute(ExecutableStatement executableStatement, int numRows) throws Exception
    {
        ParsedStatement ps = executableStatement.getParsedStatement();
        if (ps.isDdl()) {
            for (OctopusSqlCommand c : ps.getDdlCommands())
                OctopusSql.run(c, ddlRunner);

            return null;
        }

        SqlNode validatedQuery = ps.getValidatedQuery();
        List<String> dsNames = getDatasourceNames(validatedQuery);
        if (dsNames.size() > 1) // by-pass
            throw new Exception("only by-pass query is supported");

        // TODO: query on multiple datasources (throw not-implemented feature)
        return executeByPassQuery(validatedQuery, dsNames.get(0));
    }

    public void prepare(String sql, int[] oids)
    {
    }
}

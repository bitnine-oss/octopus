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

package kr.co.bitnine.octopus.engine;

import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.tcop.AbstractQueryProcessor;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.cache.CachedQuery;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;
import kr.co.bitnine.octopus.schema.SchemaManager;
import kr.co.bitnine.octopus.sql.OctopusSql;
import kr.co.bitnine.octopus.sql.OctopusSqlCommand;
import kr.co.bitnine.octopus.sql.OctopusSqlRunner;
import org.antlr.v4.runtime.RecognitionException;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.ValidationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryEngine extends AbstractQueryProcessor
{
    private static final Log LOG = LogFactory.getLog(QueryEngine.class);

    private final MetaContext metaContext;
    private final SchemaManager schemaManager;

    public QueryEngine(MetaContext metaContext, SchemaManager schemaManager)
    {
        this.metaContext = metaContext;
        this.schemaManager = schemaManager;
    }

    @Override
    protected CachedQuery processParse(String queryString, PostgresType[] paramTypes) throws PostgresException
    {
        /*
         * Format of PostgreSQL's parameter is $n (starts from 1)
         * Format of Calcite's parameter is ? (same as JDBC)
         */
        queryString = queryString.replaceAll("\\$\\d+", "?");
        LOG.debug("refined queryString='" + queryString + "'");

        // DDL

        List<OctopusSqlCommand> commands = null;
        try {
            commands = OctopusSql.parse(queryString);
        } catch (RecognitionException e) {
            LOG.debug(ExceptionUtils.getStackTrace(e));
        }

        if (commands != null)
            return new CachedStatement(commands);

        // Query

        try {
            SchemaPlus rootSchema = schemaManager.getCurrentSchema();

            FrameworkConfig config = Frameworks.newConfigBuilder()
                    .defaultSchema(rootSchema)
                    .build();
            Planner planner = Frameworks.getPlanner(config);

            SqlNode parse = planner.parse(queryString);

            TableNameTranslator.toFQN(schemaManager, parse);
            LOG.debug("FQN translated: " + parse.toString());

            schemaManager.lockRead();
            SqlNode validated = planner.validate(parse);
            schemaManager.unlockRead();

            return new CachedStatement(validated, queryString, paramTypes);
        } catch (SqlParseException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.SYNTAX_ERROR);
            throw new PostgresException(edata, e);
        } catch (ValidationException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "validation failed");
            throw new PostgresException(edata, e);
        }
    }

    @Override
    protected Portal processBind(CachedQuery cachedQuery, FormatCode[] paramFormats, byte[][] paramValues, FormatCode[] resultFormats) throws PostgresException
    {
        CachedStatement cStmt = (CachedStatement) cachedQuery;

        if (cStmt.isDdl())
            return new CursorDdl(cStmt, ddlRunner);

        SqlNode validatedQuery = cStmt.getValidatedQuery();
        List<String> dsNames = getDatasourceNames(validatedQuery);
        if (dsNames.size() > 1) {   // by-pass
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "only by-pass query is supported");
            throw new PostgresException(edata);
        }
        LOG.debug("by-pass query: " + validatedQuery.toString());

        // TODO: query on multiple data sources (throw not-implemented feature)

        String jdbcDriver;
        String jdbcConnectionString;
        try {
            MetaDataSource datasource = metaContext.getDataSourceByName(dsNames.get(0));
            jdbcDriver = datasource.getDriverName();
            jdbcConnectionString = datasource.getConnectionString();
        } catch (MetaException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to get datasource");
            throw new PostgresException(edata, e);
        }

        return new CursorByPass(cStmt, paramFormats, paramValues, resultFormats, jdbcDriver, jdbcConnectionString);
    }

    private OctopusSqlRunner ddlRunner = new OctopusSqlRunner() {
        @Override
        public void addDataSource(String dataSourceName, String jdbcConnectionString) throws Exception
        {
            String driverName;
            if (jdbcConnectionString.startsWith("jdbc:sqlite:")) {
                driverName = "org.sqlite.JDBC";
            } else {
                throw new RuntimeException("not supported");
            }

            // FIXME: all or nothing
            MetaDataSource dataSource = metaContext.addJdbcDataSource(driverName, jdbcConnectionString, dataSourceName);
            schemaManager.addDataSource(dataSource);
        }

        @Override
        public void createUser(String name, String password) throws Exception
        {
            metaContext.createUser(name, password);
        }

        @Override
        public void alterUser(String name, String password, String oldPassword) throws Exception
        {
            metaContext.alterUser(name, password);
        }

        @Override
        public void dropUser(String name) throws Exception
        {
            metaContext.dropUser(name);
        }

        public void createRole(String role) throws Exception
        {
            metaContext.createRole(role);
        }

        @Override
        public void dropRole(String role) throws Exception
        {
            metaContext.dropRoleByName(role);
        }
    };

    public List<String> getDatasourceNames(SqlNode query)
    {
        final Set<String> dsSet = new HashSet<>();
        query.accept(new SqlShuttle() {
            @Override
            public SqlNode visit(SqlIdentifier identifier)
            {
                // check whether this is fully qualified table name
                if (identifier.names.size() == 3) {
                    dsSet.add(identifier.names.get(0));
                }
                return identifier;
            }
        });

        return new ArrayList<>(dsSet);
    }
}

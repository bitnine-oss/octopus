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

import kr.co.bitnine.octopus.frame.OctopusException;
import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.meta.model.*;
import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.utils.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.schema.SchemaManager;
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

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class QueryEngine
{
    private static final Log LOG = LogFactory.getLog(QueryEngine.class);

    private final MetaContext metaContext;
    private final SchemaManager schemaManager;

    private ParsedStatement unnamedStatement = null;
    private ExecutableStatement unnamedExStatement = null;

    // for catalog view pattern matching
    private static final char SEARCH_STRING_ESCAPE = '\\';

    public QueryEngine(MetaContext metaContext, SchemaManager schemaManager)
    {
        this.metaContext = metaContext;
        this.schemaManager = schemaManager;
    }

    public QueryResult query(String queryString) throws Exception
    {
        parse(queryString, "", new PostgresType[0]);
        bind("", "", null, null, null);
        QueryResult qr = execute("", 0);
        return qr;
    }

    public void parse(String queryString, String stmtName, PostgresType[] paramTypes) throws Exception
    {
        // TODO: use stmtName to cache ParsedStatement
        if (!stmtName.isEmpty()) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.FEATURE_NOT_SUPPORTED,
                    "named prepared statement is not supported");
            new OctopusException(edata).emitErrorReport();
        }

        // DDL

        List<OctopusSqlCommand> commands = null;
        try {
            commands = OctopusSql.parse(queryString);
        } catch (RecognitionException e) {
            LOG.debug(ExceptionUtils.getStackTrace(e));
        }

        if (commands != null) {
            unnamedStatement = new ParsedStatement(commands);
            return;
        }

        // tag = CreateCommandTag()

        // Query

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

        unnamedStatement = new ParsedStatement(validated, queryString, paramTypes);
    }

    public void bind(String stmtName, String portalName, FormatCode[] paramFormats, byte[][] paramValues, FormatCode[] resultFormats) throws IOException, OctopusException
    {
        ParsedStatement curStmt = null;
        if (stmtName.isEmpty()) {
            curStmt = unnamedStatement;
        } else {
            // TODO: use stmtName to get cached ParsedStatement
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.FEATURE_NOT_SUPPORTED,
                    "named prepared statement is not supported");
            new OctopusException(edata).emitErrorReport();
        }

        // TODO: use portalName to cache ExecutableStatement
        if (!portalName.isEmpty()) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.FEATURE_NOT_SUPPORTED,
                    "named prepared statement is not supported");
            new OctopusException(edata).emitErrorReport();
        }

        unnamedExStatement = new ExecutableStatement(curStmt, paramFormats, paramValues, resultFormats);
    }

    private OctopusSqlRunner ddlRunner = new OctopusSqlRunner() {
        @Override
        public void addDataSource(String dataSourceName, String jdbcConnectionString) throws Exception
        {
            String driverName;
            if (jdbcConnectionString.startsWith("jdbc:hive2:")) {
                driverName = "org.apache.hive.jdbc.HiveDriver";
            } else if (jdbcConnectionString.startsWith("jdbc:sqlite:")) {
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

        @Override
        public ResultSet showDataSources() throws Exception
        {
            CatalogViewResultSet resultSet = new CatalogViewResultSet();
            for (MetaDataSource mdatasource : metaContext.getDataSources()) {
                String datasource_name = mdatasource.getName();
                resultSet.addTuple(Arrays.asList(datasource_name));
            }
            return resultSet;
        }

        @Override
        public ResultSet showSchemas(String datasource, String schemapattern) throws Exception
        {
            CatalogViewResultSet resultSet = new CatalogViewResultSet();
            String regSchemaNamePattern = convertPattern(schemapattern);

            for (MetaDataSource mdatasource : metaContext.getDataSources()) {
                String datasource_name = mdatasource.getName();

                if (datasource != null && !datasource.equals(datasource_name))
                    continue;

                for (MetaSchema mschema : mdatasource.getSchemas()) {
                    String schema_name = mschema.getName();

                    if (regSchemaNamePattern != null && !schema_name.matches(regSchemaNamePattern))
                        continue;

                    /* result columns:
                        1. TABLE_SCHEM String
                        2. TABLE_CATALOG String
                     */
                    resultSet.addTuple(Arrays.asList(datasource_name, schema_name));
                }
            }
            resultSet.sort(new Comparator<CatalogViewResultSet.Tuple>() {
                @Override
                public int compare(CatalogViewResultSet.Tuple t1, CatalogViewResultSet.Tuple t2) {
                    /* sort by TABLE_SCHEM and TABLE_CATALOG */
                    int r = t1.get(0).compareTo(t2.get(0));
                    if (r == 0) {
                        r = t1.get(1).compareTo(t2.get(1));
                    }
                    return r;
                }
            });

            return resultSet;
        }

        @Override
        public ResultSet showTables(String datasource, String schemapattern, String tablepattern) throws Exception {
            CatalogViewResultSet resultSet = new CatalogViewResultSet();
            String regTableNamePattern = convertPattern(tablepattern);
            String regSchemaNamePattern = convertPattern(schemapattern);

            for (MetaDataSource mdatasource : metaContext.getDataSources()) {
                String datasource_name = mdatasource.getName();

                if (datasource != null && !datasource.equals(datasource_name))
                    continue;

                for (MetaSchema mschema : mdatasource.getSchemas()) {
                    String schema_name = mschema.getName();

                    if (regSchemaNamePattern != null && !schema_name.matches(regSchemaNamePattern))
                        continue;

                    for (MetaTable mtable : mschema.getTables()) {
                        String table_name = mtable.getName();

                        if (regTableNamePattern != null && !table_name.matches(regTableNamePattern))
                            continue;

                        /* result columns:
                            1. TABLE_CAT String
                            2. TABLE_SCHEM String
                            3. TABLE_NAME String
                            4. TABLE_TYPE String "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM"
                            5. REMARKS String (description in Octopus)
                            6. TYPE_CAT String
                            7. TYPE_SCHEMA String
                            8. TYPE_NAME String
                            9. SELE_REFERENCING_COL_NAME String
                            10. REF_GENERATION String
                         */
                        resultSet.addTuple(Arrays.asList(datasource_name, schema_name, table_name,
                                "TABLE",  /* TODO: set TABLE TYPE */
                                mtable.getDescription(),
                                "NULL", "NULL", "NULL", "NULL", "NULL"));
                    }
                }
            }
            // Order by TABLE_TYPE, TABLE_CAT, TABLE_SCHEM, TABLE_NAME
            resultSet.sort(new Comparator<CatalogViewResultSet.Tuple>() {
                @Override
                public int compare(CatalogViewResultSet.Tuple t1, CatalogViewResultSet.Tuple t2) {
                    int r = t1.get(3).compareTo(t2.get(3));
                    if (r == 0) {
                        r = t1.get(0).compareTo(t2.get(0));
                    }
                    if (r == 0) {
                        r = t1.get(1).compareTo(t2.get(1));
                    }
                    if (r == 0) {
                        r = t1.get(2).compareTo(t2.get(2));
                    }
                    return r;
                }
            });
            return resultSet;
        }

        @Override
        public ResultSet showColumns(String datasource, String schemapattern, String tablepattern, String columnpattern) throws Exception
        {
            CatalogViewResultSet resultSet = new CatalogViewResultSet();
            String regSchemaNamePattern = convertPattern(schemapattern);
            String regTableNamePattern = convertPattern(tablepattern);
            String regColumnNamePattern = convertPattern(columnpattern);

            for (MetaDataSource mdatasource : metaContext.getDataSources()) {
                String datasource_name = mdatasource.getName();

                if (datasource != null && !datasource.equals(datasource_name))
                    continue;

                for (MetaSchema mschema : mdatasource.getSchemas()) {
                    String schema_name = mschema.getName();

                    if (regSchemaNamePattern != null && !schema_name.matches(regSchemaNamePattern))
                        continue;

                    for (MetaTable mtable : mschema.getTables()) {
                        String table_name = mtable.getName();

                        if (regTableNamePattern != null && !table_name.matches(regTableNamePattern))
                            continue;

                        for (MetaColumn mcolumn : mtable.getColumns()) {
                            String column_name = mcolumn.getName();

                            if (regColumnNamePattern != null && !table_name.matches(regColumnNamePattern))
                                continue;

                            /* 1. TABLE_CAT String
                               2. TABLE_SCHEM String
                               3. TABLE_NAME String
                               4. COLUMN_NAME String
                               5. DATA_TYPE int
                               6. TYPE_NAME String
                               7. COLUMN_SIZE int
                               8. BUFFER_LENGTH ('not used' in JDBC spec.)
                               9. DECIMAL_DIGIT int
                               10. NUM_PREC_RADIX
                               11. NULLABLE int
                               12. REMARKS (description in Octopus)
                               13. COLUMN_DEF String
                               14. SQL_DATA_TYPE int ('not used' in JDBC spec.)
                               15. SQL_DATETIME_SUB int ('not used' in JDBC spec.)
                               16. CHAR_OCTET_LENGTH int
                               17. ORDINAL_POSITION int
                               18. IS_NULLABLE String
                               19. SCOPE_CATALOG String
                               20. SCOPE_SCHEMA String
                               21. SCOPE_TABLE String
                               22. SOURCE_DATA_TYPE short
                               23. IS_AUTOINCREMENT String
                               24. IS_GENERATEDCOLUMN
                             */
                            resultSet.addTuple(Arrays.asList(datasource_name, schema_name, table_name,
                                    column_name, String.valueOf(mcolumn.getType()), "NON AVAILABLE" /* FIXME: */,
                                    "NULL", "NULL", "NULL", "NULL", "NULL",
                                    mcolumn.getDescription(),
                                    "NULL", "NULL", "NULL", "NULL", "NULL",
                                    "NULL", "NULL", "NULL", "NULL", "NULL",
                                    "NULL", "NULL", "NULL"));
                        }
                    }
                }
            }
            // Order by TABLE_CAT, TABLE_SCHEM, TABLE_NAME and ORDINAL_POSITION
            resultSet.sort(new Comparator<CatalogViewResultSet.Tuple>() {
                @Override
                public int compare(CatalogViewResultSet.Tuple t1, CatalogViewResultSet.Tuple t2) {
                    int r = t1.get(16).compareTo(t2.get(16));
                    if (r == 0) {
                        r = t1.get(0).compareTo(t2.get(0));
                    }
                    if (r == 0) {
                        r = t1.get(1).compareTo(t2.get(1));
                    }
                    if (r == 0) {
                        r = t1.get(2).compareTo(t2.get(2));
                    }
                    return r;
                }
            });
            return resultSet;
        }

        @Override
        public ResultSet showTablePrivileges(String datasource, String schemapattern, String tablepattern) throws Exception
        {
            //TODO
            return null;
        }

        @Override
        public ResultSet showColumnPrivileges(String datasource, String schemapattern, String tablepattern, String columnpattern) throws Exception
        {
            // TODO
            return null;
        }

        @Override
        public ResultSet showUsers() throws Exception
        {
            for (MetaUser muser: metaContext.getUsers()) {

            }
            return null;
        }
    };

    public List<String> getDatasourceNames(SqlNode query) {
        final Set<String> dsSet = new HashSet<>();
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

        return new ArrayList<>(dsSet);
    }

    public QueryResult executeByPassQuery(SqlNode validatedQuery, String dataSourceName) throws MetaException
    {
        MetaDataSource dataSource = metaContext.getDataSourceByName(dataSourceName);

        TableNameTranslator.toDSN(validatedQuery);
        LOG.debug("by-pass query: " + validatedQuery.toString());

        ResultSet rs = null;
        try {
            Class.forName(dataSource.getDriverName());
            Connection conn = DriverManager.getConnection(dataSource.getConnectionString());
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(validatedQuery.toString());
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(ExceptionUtils.getStackTrace(e));
        }

        return new QueryResult(rs);
    }

    public QueryResult execute(String portalName, int numRows) throws Exception
    {
        ExecutableStatement curExStmt = null;
        if (portalName.isEmpty()) {
            curExStmt = unnamedExStatement;
        } else {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.FATAL,
                    PostgresSQLState.PROTOCOL_VIOLATION,
                    "named prepared statement is not supported");
            new OctopusException(edata).emitErrorReport();
        }

        ParsedStatement ps = curExStmt.getParsedStatement();
        if (ps.isDdl()) {
            for (OctopusSqlCommand c : ps.getDdlCommands()) {
                ResultSet result = OctopusSql.run(c, ddlRunner);
                if (result != null) {
                    return new QueryResult(result);
                }
            }

            return null;
        }

        SqlNode validatedQuery = ps.getValidatedQuery();
        List<String> dsNames = getDatasourceNames(validatedQuery);
        if (dsNames.size() > 1) // by-pass
            throw new Exception("only by-pass query is supported");

        // TODO: query on multiple data sources (throw not-implemented feature)
        return executeByPassQuery(validatedQuery, dsNames.get(0));
    }

    /**
     * Convert a pattern containing JDBC catalog search wildcards into
     * Java regex patterns.
     *
     * @param pattern input which may contain '%' or '_' wildcard characters
     * @return replace %/_ with regex search characters, also handle escaped
     * characters.
     *
     * From tajo code
     */
    static private String convertPattern(final String pattern) {
        if (pattern == null) {
            return ".*";
        } else {
            StringBuilder result = new StringBuilder(pattern.length());

            boolean escaped = false;
            for (int i = 0, len = pattern.length(); i < len; i++) {
                char c = pattern.charAt(i);
                if (escaped) {
                    if (c != SEARCH_STRING_ESCAPE) {
                        escaped = false;
                    }
                    result.append(c);
                } else {
                    if (c == SEARCH_STRING_ESCAPE) {
                        escaped = true;
                        continue;
                    } else if (c == '%') {
                        result.append(".*");
                    } else if (c == '_') {
                        result.append('.');
                    } else {
                        result.append(c);
                    }
                }
            }

            return result.toString();
        }
    }

    public void prepare(String sql, int[] oids)
    {
    }
}

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

import kr.co.bitnine.octopus.frame.Session;
import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.meta.model.*;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;
import kr.co.bitnine.octopus.postgres.access.common.TupleDesc;
import kr.co.bitnine.octopus.postgres.catalog.PostgresAttribute;
import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.executor.Tuple;
import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import kr.co.bitnine.octopus.postgres.tcop.AbstractQueryProcessor;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.utils.adt.DatumVarchar;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.cache.CachedQuery;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;
import kr.co.bitnine.octopus.schema.SchemaManager;
import kr.co.bitnine.octopus.sql.*;
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

import java.util.*;

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

        if (commands != null) {
            TupleDesc tupDesc;
            switch (commands.get(0).getType()) {
                case SHOW_DATASOURCES:
                    PostgresAttribute[] attrs  = new PostgresAttribute[] {
                            new PostgresAttribute("TABLE_CAT", PostgresType.VARCHAR),
                            new PostgresAttribute("REMARKS", PostgresType.VARCHAR)
                    };
                    FormatCode[] resultFormats = new FormatCode[attrs.length];
                    Arrays.fill(resultFormats, FormatCode.TEXT);
                    tupDesc = new TupleDesc(attrs, resultFormats);
                    break;
                case SHOW_SCHEMAS:
                    attrs  = new PostgresAttribute[] {
                            new PostgresAttribute("TABLE_SCHEM", PostgresType.VARCHAR),
                            new PostgresAttribute("TABLE_CATALOG", PostgresType.VARCHAR),
                            new PostgresAttribute("REMARKS", PostgresType.VARCHAR)
                    };
                    resultFormats = new FormatCode[attrs.length];
                    Arrays.fill(resultFormats, FormatCode.TEXT);
                    tupDesc = new TupleDesc(attrs, resultFormats);
                    break;
                case SHOW_TABLES:
                    attrs  = new PostgresAttribute[] {
                            new PostgresAttribute("TABLE_CAT", PostgresType.VARCHAR),
                            new PostgresAttribute("TABLE_SCHEM", PostgresType.VARCHAR),
                            new PostgresAttribute("TABLE_NAME", PostgresType.VARCHAR),
                            new PostgresAttribute("TABLE_TYPE", PostgresType.VARCHAR),
                            new PostgresAttribute("REMARKS", PostgresType.VARCHAR),
                            new PostgresAttribute("TYPE_CAT", PostgresType.VARCHAR),
                            new PostgresAttribute("TYPE_SCHEM", PostgresType.VARCHAR),
                            new PostgresAttribute("TYPE_NAME", PostgresType.VARCHAR),
                            new PostgresAttribute("SELF_REFERENCING_COL_NAME", PostgresType.VARCHAR),
                            new PostgresAttribute("REF_GENERATION", PostgresType.VARCHAR)
                    };
                    resultFormats = new FormatCode[attrs.length];
                    Arrays.fill(resultFormats, FormatCode.TEXT);
                    tupDesc = new TupleDesc(attrs, resultFormats);
                    break;
                case SHOW_COLUMNS:
                    attrs  = new PostgresAttribute[] {
                            new PostgresAttribute("TABLE_CAT", PostgresType.VARCHAR),
                            new PostgresAttribute("TABLE_SCHEM", PostgresType.VARCHAR),
                            new PostgresAttribute("TABLE_NAME", PostgresType.VARCHAR),
                            new PostgresAttribute("COLUMN_NAME", PostgresType.VARCHAR),
                            new PostgresAttribute("DATA_TYPE", PostgresType.VARCHAR),
                            new PostgresAttribute("TYPE_NAME", PostgresType.VARCHAR),
                            new PostgresAttribute("COLUMN_SIZE", PostgresType.VARCHAR),
                            new PostgresAttribute("BUFFER_LENGTH", PostgresType.VARCHAR),
                            new PostgresAttribute("DECIMAL_DIGITS", PostgresType.VARCHAR),
                            new PostgresAttribute("NUM_PREC_RADIX", PostgresType.VARCHAR),
                            new PostgresAttribute("NULLABLE", PostgresType.VARCHAR),
                            new PostgresAttribute("REMARKS", PostgresType.VARCHAR),
                            new PostgresAttribute("COLUMN_DEF", PostgresType.VARCHAR),
                            new PostgresAttribute("SQL_DATA_TYPE", PostgresType.VARCHAR),
                            new PostgresAttribute("SQL_DATETIME_SUB", PostgresType.VARCHAR),
                            new PostgresAttribute("CHAR_OCTET_LENGTH", PostgresType.VARCHAR),
                            new PostgresAttribute("ORDINAL_POSITION", PostgresType.VARCHAR),
                            new PostgresAttribute("IS_NULLABLE", PostgresType.VARCHAR),
                            new PostgresAttribute("SCOPE_CATALOG", PostgresType.VARCHAR),
                            new PostgresAttribute("SCOPE_SCHEMA", PostgresType.VARCHAR),
                            new PostgresAttribute("SCOPE_TABLE", PostgresType.VARCHAR),
                            new PostgresAttribute("SOURCE_DATA_TYPE", PostgresType.VARCHAR),
                            new PostgresAttribute("IS_AUTOINCREMENT", PostgresType.VARCHAR),
                            new PostgresAttribute("IS_GENERATEDCOLUMN", PostgresType.VARCHAR),
                            new PostgresAttribute("DATA_CATEGORY", PostgresType.VARCHAR)
                    };
                    resultFormats = new FormatCode[attrs.length];
                    Arrays.fill(resultFormats, FormatCode.TEXT);
                    tupDesc = new TupleDesc(attrs, resultFormats);
                    break;
                case SHOW_USERS:
                    attrs  = new PostgresAttribute[] {
                            new PostgresAttribute("USER_NAME", PostgresType.VARCHAR),
                            new PostgresAttribute("REMARKS", PostgresType.VARCHAR)
                    };
                    resultFormats = new FormatCode[attrs.length];
                    Arrays.fill(resultFormats, FormatCode.TEXT);
                    tupDesc = new TupleDesc(attrs, resultFormats);
                    break;
                default:
                    tupDesc = null;
            }
            return new CachedStatement(commands, tupDesc);
        }

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

        // TODO: query on multiple data sources
        SqlNode validatedQuery = cStmt.getValidatedQuery();
        List<String> dsNames = getDatasourceNames(validatedQuery);
        if (dsNames.size() > 1) {   // by-pass
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.FEATURE_NOT_SUPPORTED,
                    "only by-pass query is supported");
            throw new PostgresException(edata);
        }

        LOG.debug("by-pass query: " + validatedQuery.toString());

        String jdbcDriver;
        String jdbcConnectionString;
        try {
            MetaDataSource dataSource = metaContext.getDataSource(dsNames.get(0));
            jdbcDriver = dataSource.getDriverName();
            jdbcConnectionString = dataSource.getConnectionString();
        } catch (MetaException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to get DataSource");
            throw new PostgresException(edata, e);
        }

        return new CursorByPass(cStmt, paramFormats, paramValues, resultFormats, jdbcDriver, jdbcConnectionString);
    }

    private void checkSystemPrivilege(SystemPrivilege sysPriv) throws PostgresException
    {
        Set<SystemPrivilege> userSysPrivs;
        try {
            String userName = Session.currentSession().getClientParam(Session.CLIENT_PARAM_USER);
            userSysPrivs = metaContext.getUser(userName).getSystemPrivileges();
        } catch (MetaException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to get user's system privilege");
            throw new PostgresException(edata, e);
        }

        if (!userSysPrivs.contains(sysPriv)) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.INSUFFICIENT_PRIVILEGE,
                    "must have " + sysPriv.name() + " privilege");
            throw new PostgresException(edata);
        }
    }

    private OctopusSqlRunner ddlRunner = new OctopusSqlRunner() {
        @Override
        public void addDataSource(String dataSourceName, String jdbcConnectionString) throws Exception
        {
            checkSystemPrivilege(SystemPrivilege.ALTER_SYSTEM);

            String driverName;
            if (jdbcConnectionString.startsWith("jdbc:hive2:")) {
                driverName = "org.apache.hive.jdbc.HiveDriver";
            } else if (jdbcConnectionString.startsWith("jdbc:sqlite:")) {
                driverName = "org.sqlite.JDBC";
            } else {
                PostgresErrorData edata = new PostgresErrorData(
                        PostgresSeverity.ERROR,
                        PostgresSQLState.FEATURE_NOT_SUPPORTED,
                        "JDBC connection string \"" + jdbcConnectionString + "\" not supported");
                throw new PostgresException(edata);
            }

            // FIXME: all or nothing
            MetaDataSource dataSource = metaContext.addJdbcDataSource(driverName, jdbcConnectionString, dataSourceName);
            schemaManager.addDataSource(dataSource);
        }

        @Override
        public void createUser(String name, String password) throws Exception
        {
            checkSystemPrivilege(SystemPrivilege.CREATE_USER);
            metaContext.createUser(name, password);
        }

        @Override
        public void alterUser(String name, String password, String oldPassword) throws Exception
        {
            checkSystemPrivilege(SystemPrivilege.ALTER_USER);
            metaContext.alterUser(name, password);
        }

        @Override
        public void dropUser(String name) throws Exception
        {
            checkSystemPrivilege(SystemPrivilege.DROP_USER);
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
        public void grantSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> grantees) throws Exception
        {
            checkSystemPrivilege(SystemPrivilege.GRANT_ANY_PRIVILEGE);
            metaContext.addSystemPrivileges(sysPrivs, grantees);
        }

        @Override
        public void revokeSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> revokees) throws Exception
        {
            checkSystemPrivilege(SystemPrivilege.GRANT_ANY_PRIVILEGE);
            metaContext.addSystemPrivileges(sysPrivs, revokees);
        }

        @Override
        public TupleSet showDataSources() throws Exception
        {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            for (MetaDataSource mDs : metaContext.getDataSources()) {
                Tuple t = new Tuple(2);
                t.setDatum(0, new DatumVarchar(mDs.getName()));
                t.setDatum(1, new DatumVarchar(mDs.getComment()));

                tuples.add(t);
            }
            Collections.sort(tuples, new Comparator<Tuple>()
            {
                @Override
                public int compare(Tuple tl, Tuple tr)
                {
                    return tl.getDatum(0).out().compareTo(tr.getDatum(0).out());
                }
            });

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public TupleSet showSchemas(String dataSourceName, String schemaPattern) throws Exception
        {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            final String pattern = convertPattern(schemaPattern);
            for (MetaDataSource mDs : metaContext.getDataSources()) {
                String dsName = mDs.getName();
                if (dataSourceName != null && !dataSourceName.equals(dsName))
                    continue;

                for (MetaSchema mSchema : mDs.getSchemas()) {
                    String schemaName = mSchema.getName();
                    if (!schemaName.matches(pattern))
                        continue;

                    Tuple t = new Tuple(3);
                    t.setDatum(0, new DatumVarchar(schemaName));
                    t.setDatum(1, new DatumVarchar(dsName));
                    t.setDatum(2, new DatumVarchar(mSchema.getComment()));

                    tuples.add(t);
                }
            }
            // ordered by TABLE_CATALOG and TABLE_SCHEM
            Collections.sort(tuples, new Comparator<Tuple>()
            {
                @Override
                public int compare(Tuple tl, Tuple tr)
                {
                    int r = tl.getDatum(1).out().compareTo(tr.getDatum(1).out());
                    if (r == 0)
                        return tl.getDatum(0).out().compareTo(tr.getDatum(0).out());
                    else
                        return r;
                }
            });

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public TupleSet showTables(String dataSourceName, String schemaPattern, String tablePattern) throws Exception
        {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            final String sPattern = convertPattern(schemaPattern);
            final String tPattern = convertPattern(tablePattern);
            for (MetaDataSource mDs : metaContext.getDataSources()) {
                String dsName = mDs.getName();
                if (dataSourceName != null && !dataSourceName.equals(dsName))
                    continue;

                for (MetaSchema mSchema : mDs.getSchemas()) {
                    String schemaName = mSchema.getName();
                    if (!schemaName.matches(sPattern))
                        continue;

                    for (MetaTable mTable : mSchema.getTables()) {
                        String tableName = mTable.getName();
                        if (!tableName.matches(tPattern))
                            continue;

                        Tuple t = new Tuple(10);
                        t.setDatum(0, new DatumVarchar(dsName));
                        t.setDatum(1, new DatumVarchar(schemaName));
                        t.setDatum(2, new DatumVarchar(tableName));
                        t.setDatum(3, new DatumVarchar(mTable.getType()));
                        t.setDatum(4, new DatumVarchar(mTable.getComment()));
                        t.setDatum(5, new DatumVarchar("NULL"));
                        t.setDatum(6, new DatumVarchar("NULL"));
                        t.setDatum(7, new DatumVarchar("NULL"));
                        t.setDatum(8, new DatumVarchar("NULL"));
                        t.setDatum(9, new DatumVarchar("NULL"));

                        tuples.add(t);
                    }
                }
            }
            // ordered by TABLE_TYPE, TABLE_CAT, TABLE_SCHEM and TABLE_NAME
            Collections.sort(tuples, new Comparator<Tuple>() {
                @Override
                public int compare(Tuple tl, Tuple tr)
                {
                    int r = tl.getDatum(3).out().compareTo(tr.getDatum(3).out());
                    if (r == 0)
                        r = tl.getDatum(0).out().compareTo(tr.getDatum(0).out());
                    if (r == 0)
                        r = tl.getDatum(1).out().compareTo(tr.getDatum(1).out());
                    if (r == 0)
                        r = tl.getDatum(2).out().compareTo(tr.getDatum(2).out());
                    return r;
                }
            });

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public TupleSet showColumns(String dataSourceName, String schemaPattern, String tablePattern, String columnPattern) throws Exception
        {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            final String sPattern = convertPattern(schemaPattern);
            final String tPattern = convertPattern(tablePattern);
            final String cPattern = convertPattern(columnPattern);
            for (MetaDataSource mDs : metaContext.getDataSources()) {
                String dsName = mDs.getName();
                if (dataSourceName != null && !dataSourceName.equals(dsName))
                    continue;

                for (MetaSchema mSchema : mDs.getSchemas()) {
                    String schemaName = mSchema.getName();
                    if (!schemaName.matches(sPattern))
                        continue;

                    for (MetaTable mTable : mSchema.getTables()) {
                        String tableName = mTable.getName();
                        if (!tableName.matches(tPattern))
                            continue;

                        for (MetaColumn mColumn : mTable.getColumns()) {
                            String colName = mColumn.getName();

                            if (!tableName.matches(cPattern))
                                continue;

                            Tuple t = new Tuple(25);
                            t.setDatum(0, new DatumVarchar(dsName));
                            t.setDatum(1, new DatumVarchar(schemaName));
                            t.setDatum(2, new DatumVarchar(tableName));
                            t.setDatum(3, new DatumVarchar(colName));
                            t.setDatum(4, new DatumVarchar(String.valueOf(mColumn.getType())));
                            t.setDatum(5, new DatumVarchar(TypeInfo.postresTypeOfJdbcType(mColumn.getType()).typeName()));
                            t.setDatum(6, new DatumVarchar("NULL"));
                            t.setDatum(7, new DatumVarchar("NULL"));
                            t.setDatum(8, new DatumVarchar("NULL"));
                            t.setDatum(9, new DatumVarchar("NULL"));
                            t.setDatum(10, new DatumVarchar("NULL"));
                            t.setDatum(11, new DatumVarchar(mColumn.getComment()));
                            t.setDatum(12, new DatumVarchar("NULL"));
                            t.setDatum(13, new DatumVarchar("NULL"));
                            t.setDatum(14, new DatumVarchar("NULL"));
                            t.setDatum(15, new DatumVarchar("NULL"));
                            t.setDatum(16, new DatumVarchar("NULL"));
                            t.setDatum(17, new DatumVarchar("NULL"));
                            t.setDatum(18, new DatumVarchar("NULL"));
                            t.setDatum(19, new DatumVarchar("NULL"));
                            t.setDatum(20, new DatumVarchar("NULL"));
                            t.setDatum(21, new DatumVarchar("NULL"));
                            t.setDatum(22, new DatumVarchar("NULL"));
                            t.setDatum(23, new DatumVarchar("NULL"));
                            t.setDatum(24, new DatumVarchar(mColumn.getDataCategory()));

                            tuples.add(t);
                        }
                    }
                }
            }
            // ordered by TABLE_CAT, TABLE_SCHEM, TABLE_NAME and ORDINAL_POSITION
            Collections.sort(tuples, new Comparator<Tuple>()
            {
                @Override
                public int compare(Tuple tl, Tuple tr)
                {
                    int r = tl.getDatum(0).out().compareTo(tr.getDatum(0).out());
                    if (r == 0)
                        r = tl.getDatum(1).out().compareTo(tr.getDatum(1).out());
                    if (r == 0)
                        r = tl.getDatum(2).out().compareTo(tr.getDatum(2).out());
                    if (r == 0)
                        r = tl.getDatum(16).out().compareTo(tr.getDatum(16).out());
                    return r;
                }
            });

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public TupleSet showTablePrivileges(String dataSourceName, String schemapattern, String tablepattern) throws Exception
        {
            //TODO
            return null;
        }

        @Override
        public TupleSet showColumnPrivileges(String dataSourceName, String schemapattern, String tablepattern, String columnpattern) throws Exception
        {
            // TODO
            return null;
        }

        @Override
        public TupleSet showUsers() throws Exception
        {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            for (MetaUser mUser: metaContext.getUsers()) {
                Tuple t = new Tuple(2);
                t.setDatum(0, new DatumVarchar(mUser.getName()));
                t.setDatum(1, new DatumVarchar(mUser.getComment()));
                tuples.add(t);
            }

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public void commentOn(OctopusSqlCommentTarget target, String comment) throws Exception
        {
            if (true /* TODO: if user does not have object privilege */)
                checkSystemPrivilege(SystemPrivilege.COMMENT_ANY);

            switch (target.type) {
                case DATASOURCE:
                    metaContext.commentOnDataSource(comment, target.dataSource);
                    break;
                case SCHEMA:
                    metaContext.commentOnSchema(comment, target.dataSource, target.schema);
                    break;
                case TABLE:
                    metaContext.commentOnTable(comment, target.dataSource, target.schema, target.table);
                    break;
                case COLUMN:
                    metaContext.commentOnColumn(comment, target.dataSource, target.schema, target.table, target.column);
                    break;
                case USER:
                    metaContext.commentOnUser(comment, target.user);
                    break;
            }
        }

        @Override
        public void setDataCategoryOn(String dataSource, String schema, String table, String column, String category) throws Exception
        {
            if (true /* TODO: if user does not have object privilege */)
                checkSystemPrivilege(SystemPrivilege.COMMENT_ANY);

            metaContext.setDataCategoryOn(category, dataSource, schema, table, column);
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

    /**
     * Convert a pattern containing JDBC catalog search wildcards into
     * Java regex patterns.
     *
     * @param pattern input which may contain '%' or '_' wildcard characters
     * @return replace %/_ with regex search characters, also handle escaped
     * characters.
     *
     * Borrowed from Tajo
     */
    private String convertPattern(final String pattern)
    {
        final char SEARCH_STRING_ESCAPE = '\\';

        if (pattern == null) {
            return ".*";
        } else {
            StringBuilder result = new StringBuilder(pattern.length());

            boolean escaped = false;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                if (escaped) {
                    if (c != SEARCH_STRING_ESCAPE)
                        escaped = false;
                    result.append(c);
                } else {
                    if (c == SEARCH_STRING_ESCAPE)
                        escaped = true;
                    else if (c == '%')
                        result.append(".*");
                    else if (c == '_')
                        result.append('.');
                    else
                        result.append(c);
                }
            }

            return result.toString();
        }
    }
}

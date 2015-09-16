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
import kr.co.bitnine.octopus.meta.privilege.ObjectPrivilege;
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
import kr.co.bitnine.octopus.postgres.utils.adt.IoVarchar;
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
                case SHOW_ALL_USERS:
                    attrs  = new PostgresAttribute[] {
                            new PostgresAttribute("USER_NAME", PostgresType.VARCHAR),
                            new PostgresAttribute("REMARKS", PostgresType.VARCHAR)
                    };
                    resultFormats = new FormatCode[attrs.length];
                    Arrays.fill(resultFormats, FormatCode.TEXT);
                    tupDesc = new TupleDesc(attrs, resultFormats);
                    break;
                case SHOW_OBJ_PRIVS_FOR:
                    attrs  = new PostgresAttribute[] {
                            new PostgresAttribute("TABLE_CAT", PostgresType.VARCHAR),
                            new PostgresAttribute("TABLE_SCHEM", PostgresType.VARCHAR),
                            new PostgresAttribute("PRIVILEGE", PostgresType.VARCHAR)
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
                    PostgresSQLState.SYNTAX_ERROR,
                    "syntax error " + e.getMessage());
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

        if (!checkSystemPrivilege(SystemPrivilege.SELECT_ANY_TABLE))
            checkSelectPrivilegeThrow(validatedQuery);

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

    private List<String> getDatasourceNames(SqlNode query)
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

    private void checkSelectPrivilegeThrow(SqlNode query) throws PostgresException
    {
        final PostgresException[] e = {null};

        query.accept(new SqlShuttle() {
            @Override
            public SqlNode visit(SqlIdentifier identifier)
            {
                if (identifier.names.size() == 3 && e[0] == null) {
                    String[] schemaName = {identifier.names.get(0), identifier.names.get(1)};
                    e[0] = checkObjectPrivilegeInternal(ObjectPrivilege.SELECT, schemaName);
                }
                return identifier;
            }
        });

        if (e[0] != null)
            throw e[0];
    }

    private boolean checkSystemPrivilege(SystemPrivilege sysPriv)
    {
        return checkSystemPrivilegeInternal(sysPriv) == null;
    }

    private void checkSystemPrivilegeThrow(SystemPrivilege sysPriv) throws PostgresException
    {
        PostgresException e = checkSystemPrivilegeInternal(sysPriv);
        if (e != null)
            throw e;
    }

    private PostgresException checkSystemPrivilegeInternal(SystemPrivilege sysPriv)
    {
        Set<SystemPrivilege> userSysPrivs;

        try {
            String userName = Session.currentSession().getClientParam(Session.CLIENT_PARAM_USER);
            userSysPrivs = metaContext.getUser(userName).getSystemPrivileges();
        } catch (MetaException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to get user's system privilege");
            return new PostgresException(edata, e);
        }

        if (!userSysPrivs.contains(sysPriv)) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.INSUFFICIENT_PRIVILEGE,
                    "must have " + sysPriv.name() + " privilege");
            return new PostgresException(edata);
        }

        return null;
    }

    private void checkObjectPrivilegeThrow(ObjectPrivilege objPriv, String[] schemaName) throws PostgresException
    {
        PostgresException e = checkObjectPrivilegeInternal(objPriv, schemaName);
        if (e != null)
            throw e;
    }

    private PostgresException checkObjectPrivilegeInternal(ObjectPrivilege objPriv, String[] schemaName)
    {
        Set<ObjectPrivilege> schemaObjPrivs;
        String userName = Session.currentSession().getClientParam(Session.CLIENT_PARAM_USER);

        try {
            MetaSchemaPrivilege schemaPrivs = metaContext.getSchemaPrivileges(schemaName, userName);
            schemaObjPrivs = schemaPrivs == null ? null : schemaPrivs.getObjectPrivileges();
        } catch (MetaException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to get object privileges on " + schemaName[0] + "." + schemaName[1] + " of user '" + userName + "'");
            return new PostgresException(edata, e);
        }

        if (schemaObjPrivs == null || !schemaObjPrivs.contains(objPriv)) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.INSUFFICIENT_PRIVILEGE,
                    "must have " + objPriv.name() + " privilege");
            return new PostgresException(edata);
        }

        return null;
    }

    private OctopusSqlRunner ddlRunner = new OctopusSqlRunner() {
        @Override
        public void addDataSource(String dataSourceName, String jdbcConnectionString) throws Exception
        {
            checkSystemPrivilegeThrow(SystemPrivilege.ALTER_SYSTEM);

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
        public void updateDataSource(String dataSourceName) throws Exception
        {
            checkSystemPrivilegeThrow(SystemPrivilege.ALTER_SYSTEM);
            schemaManager.dropDataSource(dataSourceName);
            MetaDataSource dataSource = metaContext.updateJdbcDataSource(dataSourceName);
            schemaManager.addDataSource(dataSource);
        }

        @Override
        public void dropDataSource(String dataSourceName) throws Exception
        {
            checkSystemPrivilegeThrow(SystemPrivilege.ALTER_SYSTEM);
            schemaManager.dropDataSource(dataSourceName);
            metaContext.dropJdbcDataSource(dataSourceName);
        }

        @Override
        public void createUser(String name, String password) throws Exception
        {
            checkSystemPrivilegeThrow(SystemPrivilege.CREATE_USER);
            metaContext.createUser(name, password);
        }

        @Override
        public void alterUser(String name, String password, String oldPassword) throws Exception
        {
            checkSystemPrivilegeThrow(SystemPrivilege.ALTER_USER);
            metaContext.alterUser(name, password);
        }

        @Override
        public void dropUser(String name) throws Exception
        {
            checkSystemPrivilegeThrow(SystemPrivilege.DROP_USER);
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
            checkSystemPrivilegeThrow(SystemPrivilege.GRANT_ANY_PRIVILEGE);
            metaContext.addSystemPrivileges(sysPrivs, grantees);
        }

        @Override
        public void revokeSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> revokees) throws Exception
        {
            checkSystemPrivilegeThrow(SystemPrivilege.GRANT_ANY_PRIVILEGE);
            metaContext.removeSystemPrivileges(sysPrivs, revokees);
        }

        @Override
        public void grantObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] objName, List<String> grantees) throws Exception
        {
            checkSystemPrivilegeThrow(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE);
            metaContext.addObjectPrivileges(objPrivs, objName, grantees);
        }

        @Override
        public void revokeObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] objName, List<String> revokees) throws Exception
        {
            checkSystemPrivilegeThrow(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE);
            metaContext.removeObjectPrivileges(objPrivs, objName, revokees);
        }

        @Override
        public TupleSet showDataSources() throws Exception
        {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            for (MetaDataSource mDs : metaContext.getDataSources()) {
                Tuple t = new Tuple(2);
                t.setDatum(0, mDs.getName());
                t.setDatum(1, mDs.getComment());

                tuples.add(t);
            }
            Collections.sort(tuples, new Comparator<Tuple>()
            {
                @Override
                public int compare(Tuple tl, Tuple tr)
                {
                    return ((String )tl.getDatum(0)).compareTo((String) tr.getDatum(0));
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
                    t.setDatum(0, schemaName);
                    t.setDatum(1, dsName);
                    t.setDatum(2, mSchema.getComment());

                    tuples.add(t);
                }
            }
            // ordered by TABLE_CATALOG and TABLE_SCHEM
            Collections.sort(tuples, new Comparator<Tuple>()
            {
                @Override
                public int compare(Tuple tl, Tuple tr)
                {
                    int r = ((String) tl.getDatum(1)).compareTo(((String) tr.getDatum(1)));
                    if (r == 0)
                        return ((String) tl.getDatum(0)).compareTo(((String) tr.getDatum(0)));
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
                        t.setDatum(0, dsName);
                        t.setDatum(1, schemaName);
                        t.setDatum(2, tableName);
                        t.setDatum(3, mTable.getType());
                        t.setDatum(4, mTable.getComment());
                        t.setDatum(5, "NULL");
                        t.setDatum(6, "NULL");
                        t.setDatum(7, "NULL");
                        t.setDatum(8, "NULL");
                        t.setDatum(9, "NULL");

                        tuples.add(t);
                    }
                }
            }
            // ordered by TABLE_TYPE, TABLE_CAT, TABLE_SCHEM and TABLE_NAME
            Collections.sort(tuples, new Comparator<Tuple>() {
                @Override
                public int compare(Tuple tl, Tuple tr)
                {
                    int r = ((String) tl.getDatum(3)).compareTo(((String) tr.getDatum(3)));
                    if (r == 0)
                        r = ((String) tl.getDatum(0)).compareTo(((String) tr.getDatum(0)));
                    if (r == 0)
                        r = ((String) tl.getDatum(1)).compareTo(((String) tr.getDatum(1)));
                    if (r == 0)
                        r = ((String) tl.getDatum(2)).compareTo(((String) tr.getDatum(2)));
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

                            if (!colName.matches(cPattern))
                                continue;

                            Tuple t = new Tuple(25);
                            t.setDatum(0, dsName);
                            t.setDatum(1, schemaName);
                            t.setDatum(2, tableName);
                            t.setDatum(3, colName);
                            t.setDatum(4, String.valueOf(mColumn.getType()));
                            t.setDatum(5, TypeInfo.postresTypeOfJdbcType(mColumn.getType()).typeName());
                            t.setDatum(6, "NULL");
                            t.setDatum(7, "NULL");
                            t.setDatum(8, "NULL");
                            t.setDatum(9, "NULL");
                            t.setDatum(10, "NULL");
                            t.setDatum(11, mColumn.getComment());
                            t.setDatum(12, "NULL");
                            t.setDatum(13, "NULL");
                            t.setDatum(14, "NULL");
                            t.setDatum(15, "NULL");
                            t.setDatum(16, "NULL");
                            t.setDatum(17, "NULL");
                            t.setDatum(18, "NULL");
                            t.setDatum(19, "NULL");
                            t.setDatum(20, "NULL");
                            t.setDatum(21, "NULL");
                            t.setDatum(22, "NULL");
                            t.setDatum(23, "NULL");
                            t.setDatum(24, mColumn.getDataCategory());

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
                    int r = ((String) tl.getDatum(0)).compareTo(((String) tr.getDatum(0)));
                    if (r == 0)
                        r = ((String) tl.getDatum(1)).compareTo(((String) tr.getDatum(1)));
                    if (r == 0)
                        r = ((String) tl.getDatum(2)).compareTo(((String) tr.getDatum(2)));
                    if (r == 0)
                        r = ((String) tl.getDatum(16)).compareTo(((String) tr.getDatum(16)));
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
        public TupleSet showAllUsers() throws Exception
        {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            for (MetaUser mUser: metaContext.getUsers()) {
                Tuple t = new Tuple(2);
                t.setDatum(0, mUser.getName());
                t.setDatum(1, mUser.getComment());
                tuples.add(t);
            }

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public TupleSet showObjPrivsFor(String userName) throws Exception
        {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            for (MetaSchemaPrivilege mSchemaPriv: metaContext.getSchemaPrivilegesByUser(userName)) {
                Tuple t = new Tuple(3);

                MetaSchema mSchema = mSchemaPriv.getSchema();
                t.setDatum(0, mSchema.getDataSource().getName());
                t.setDatum(1, mSchema.getName());

                Set<ObjectPrivilege> objPrivs = mSchemaPriv.getObjectPrivileges();
                assert objPrivs.size() > 0;
                Iterator<ObjectPrivilege> iter = objPrivs.iterator();
                StringBuilder builder = new StringBuilder();
                builder.append(iter.next().name());
                while (iter.hasNext())
                    builder.append(",").append(iter.next().name());
                t.setDatum(2, builder.toString());

                tuples.add(t);
            }

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public void commentOn(OctopusSqlCommentTarget target, String comment) throws Exception
        {
            switch (target.type) {
                case DATASOURCE:
                case USER:
                    checkSystemPrivilegeThrow(SystemPrivilege.COMMENT_ANY);
                    break;
                case SCHEMA:
                case TABLE:
                case COLUMN:
                    if (!checkSystemPrivilege(SystemPrivilege.COMMENT_ANY))
                        checkObjectPrivilegeThrow(ObjectPrivilege.COMMENT, new String[] {target.dataSource, target.schema});
                    break;
            }

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
            if (!checkSystemPrivilege(SystemPrivilege.COMMENT_ANY))
                checkObjectPrivilegeThrow(ObjectPrivilege.COMMENT, new String[] {dataSource, schema});

            metaContext.setDataCategoryOn(category, dataSource, schema, table, column);
        }
    };

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

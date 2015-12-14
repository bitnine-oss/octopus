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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import kr.co.bitnine.octopus.frame.ConnectionManager;
import kr.co.bitnine.octopus.frame.Session;
import kr.co.bitnine.octopus.meta.MetaContext;
import kr.co.bitnine.octopus.meta.MetaException;
import kr.co.bitnine.octopus.meta.result.ResultOfGetColumns;
import kr.co.bitnine.octopus.meta.model.MetaColumn;
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.meta.model.MetaSchema;
import kr.co.bitnine.octopus.meta.model.MetaSchemaPrivilege;
import kr.co.bitnine.octopus.meta.model.MetaTable;
import kr.co.bitnine.octopus.meta.model.MetaUser;
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
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.cache.CachedQuery;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;
import kr.co.bitnine.octopus.postgres.utils.misc.PostgresConfiguration;
import kr.co.bitnine.octopus.schema.SchemaManager;
import kr.co.bitnine.octopus.sql.OctopusSql;
import kr.co.bitnine.octopus.sql.OctopusSqlCommand;
import kr.co.bitnine.octopus.sql.OctopusSqlObjectTarget;
import kr.co.bitnine.octopus.sql.OctopusSqlRunner;
import kr.co.bitnine.octopus.sql.TupleSetSql;
import org.antlr.v4.runtime.RecognitionException;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.tools.Planner;
import org.apache.calcite.tools.ValidationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

public final class QueryEngine extends AbstractQueryProcessor {
    private static final Log LOG = LogFactory.getLog(QueryEngine.class);

    private final MetaContext metaContext;
    private final ConnectionManager connectionManager;
    private final SchemaManager schemaManager;
    private final Configuration conf;

    public QueryEngine(MetaContext metaContext,
                       ConnectionManager connectionManager,
                       SchemaManager schemaManager,
                       Configuration conf) {
        this.metaContext = metaContext;
        this.connectionManager = connectionManager;
        this.schemaManager = schemaManager;
        this.conf = conf;
    }

    @Override
    protected CachedQuery processParse(String queryString, PostgresType[] paramTypes) throws PostgresException {
        /*
         * Format of PostgreSQL's parameter is $n (starts from 1)
         * Format of Calcite's parameter is ? (same as JDBC)
         */
        String refinedQuery = queryString.replaceAll("\\$\\d+", "?");
        LOG.debug("refined queryString='" + refinedQuery + "'");

        // DDL

        List<OctopusSqlCommand> commands = null;
        try {
            commands = OctopusSql.parse(refinedQuery);
        } catch (RecognitionException e) {
            LOG.debug(ExceptionUtils.getStackTrace(e));
        }

        if (commands != null && commands.size() > 0) {
            TupleDesc tupDesc;
            switch (commands.get(0).getType()) {
            case SHOW_TX_ISOLATION_LEVEL:
                PostgresAttribute[] attrs  = new PostgresAttribute[] {
                    new PostgresAttribute("transaction_isolation", PostgresType.VARCHAR, 32),
                };
                FormatCode[] resultFormats = new FormatCode[attrs.length];
                Arrays.fill(resultFormats, FormatCode.TEXT);
                tupDesc = new TupleDesc(attrs, resultFormats);
                break;
            case SHOW_DATASOURCES:
                attrs  = new PostgresAttribute[] {
                    new PostgresAttribute("TABLE_CAT", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("REMARKS", PostgresType.VARCHAR, 1024)
                };
                resultFormats = new FormatCode[attrs.length];
                Arrays.fill(resultFormats, FormatCode.TEXT);
                tupDesc = new TupleDesc(attrs, resultFormats);
                break;
            case SHOW_SCHEMAS:
                attrs  = new PostgresAttribute[] {
                    new PostgresAttribute("TABLE_SCHEM", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_CATALOG", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("REMARKS", PostgresType.VARCHAR, 1024),
                    new PostgresAttribute("TABLE_CAT_REMARKS", PostgresType.VARCHAR, 1024)
                };
                resultFormats = new FormatCode[attrs.length];
                Arrays.fill(resultFormats, FormatCode.TEXT);
                tupDesc = new TupleDesc(attrs, resultFormats);
                break;
            case SHOW_TABLES:
                attrs  = new PostgresAttribute[] {
                    new PostgresAttribute("TABLE_CAT", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_SCHEM", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_NAME", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_TYPE", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("REMARKS", PostgresType.VARCHAR, 1024),
                    new PostgresAttribute("TYPE_CAT", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("TYPE_SCHEM", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("TYPE_NAME", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("SELF_REFERENCING_COL_NAME", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("REF_GENERATION", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("TABLE_CAT_REMARKS", PostgresType.VARCHAR, 1024),
                    new PostgresAttribute("TABLE_SCHEM_REMARKS", PostgresType.VARCHAR, 1024)
                };
                resultFormats = new FormatCode[attrs.length];
                Arrays.fill(resultFormats, FormatCode.TEXT);
                tupDesc = new TupleDesc(attrs, resultFormats);
                break;
            case SHOW_COLUMNS:
                attrs  = new PostgresAttribute[] {
                    new PostgresAttribute("TABLE_CAT", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_SCHEM", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_NAME", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("COLUMN_NAME", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("DATA_TYPE", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("TYPE_NAME", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("COLUMN_SIZE", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("BUFFER_LENGTH", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("DECIMAL_DIGITS", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("NUM_PREC_RADIX", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("NULLABLE", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("REMARKS", PostgresType.VARCHAR, 1024),
                    new PostgresAttribute("COLUMN_DEF", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("SQL_DATA_TYPE", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("SQL_DATETIME_SUB", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("CHAR_OCTET_LENGTH", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("ORDINAL_POSITION", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("IS_NULLABLE", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("SCOPE_CATALOG", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("SCOPE_SCHEMA", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("SCOPE_TABLE", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("SOURCE_DATA_TYPE", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("IS_AUTOINCREMENT", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("IS_GENERATEDCOLUMN", PostgresType.VARCHAR, 16),
                    new PostgresAttribute("DATA_CATEGORY", PostgresType.VARCHAR, 64),
                    new PostgresAttribute("TABLE_CAT_REMARKS", PostgresType.VARCHAR, 1024),
                    new PostgresAttribute("TABLE_SCHEM_REMARKS", PostgresType.VARCHAR, 1024),
                    new PostgresAttribute("TABLE_NAME_REMARKS", PostgresType.VARCHAR, 1024)
                };
                resultFormats = new FormatCode[attrs.length];
                Arrays.fill(resultFormats, FormatCode.TEXT);
                tupDesc = new TupleDesc(attrs, resultFormats);
                break;
            case SHOW_ALL_USERS:
                attrs  = new PostgresAttribute[] {
                    new PostgresAttribute("USER_NAME", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("REMARKS", PostgresType.VARCHAR, 1024)
                };
                resultFormats = new FormatCode[attrs.length];
                Arrays.fill(resultFormats, FormatCode.TEXT);
                tupDesc = new TupleDesc(attrs, resultFormats);
                break;
            case SHOW_OBJ_PRIVS_FOR:
                attrs  = new PostgresAttribute[] {
                    new PostgresAttribute("TABLE_CAT", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_SCHEM", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("PRIVILEGE", PostgresType.VARCHAR, 16)
                };
                resultFormats = new FormatCode[attrs.length];
                Arrays.fill(resultFormats, FormatCode.TEXT);
                tupDesc = new TupleDesc(attrs, resultFormats);
                break;
            case SHOW_COMMENTS:
                attrs  = new PostgresAttribute[] {
                    new PostgresAttribute("OBJECT_TYPE", PostgresType.VARCHAR, 8),
                    new PostgresAttribute("TABLE_CAT", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_SCHEM", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_NAME", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("COLUMN_NAME", PostgresType.VARCHAR, 128),
                    new PostgresAttribute("TABLE_CAT_REMARKS", PostgresType.VARCHAR, 1024),
                    new PostgresAttribute("TABLE_SCHEM_REMARKS", PostgresType.VARCHAR, 1024),
                    new PostgresAttribute("TABLE_NAME_REMARKS", PostgresType.VARCHAR, 1024),
                    new PostgresAttribute("COLUMN_NAME_REMARKS", PostgresType.VARCHAR, 1024)
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

            SqlParser.Config parserConf = SqlParser.configBuilder()
                    .setUnquotedCasing(Casing.TO_LOWER)
                    .build();
            FrameworkConfig config = Frameworks.newConfigBuilder()
                    .defaultSchema(rootSchema)
                    .parserConfig(parserConf)
                    .build();
            Planner planner = Frameworks.getPlanner(config);

            SqlNode parse = planner.parse(refinedQuery);

            TableNameTranslator.toFQN(schemaManager, parse);
            LOG.debug("FQN translated: " + parse.toString());

            schemaManager.lockRead();
            try {
                SqlNode validated = planner.validate(parse);
                return new CachedStatement(validated, refinedQuery, paramTypes);
            } finally {
                schemaManager.unlockRead();
            }
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
    protected Portal processBind(CachedQuery cachedQuery, String portalName,
                                 FormatCode[] paramFormats,
                                 byte[][] paramValues,
                                 FormatCode[] resultFormats)
            throws PostgresException {
        CachedStatement cStmt = (CachedStatement) cachedQuery;

        if (cStmt.isDdl())
            return new CursorDdl(cStmt, portalName, ddlRunner);

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

        String dataSourceName = dsNames.get(0);
        String connectionString;
        try {
            MetaDataSource dataSource = metaContext.getDataSource(dataSourceName);
            connectionString = dataSource.getConnectionString();
        } catch (MetaException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to get DataSource");
            throw new PostgresException(edata, e);
        }

        LOG.info("create portal '" + portalName + "' for by-pass (session=" + Session.currentSession().getId() + ')');
        Portal p;
        if (connectionString.startsWith("jdbc:hive2:"))
            p = new CursorHive(cStmt, portalName, paramFormats, paramValues,
                    resultFormats, dataSourceName);
        else {
            p = new CursorByPass(cStmt, portalName, paramFormats, paramValues,
                    resultFormats, dataSourceName);
        }
        return p;
    }

    private List<String> getDatasourceNames(SqlNode query) {
        final Set<String> dsSet = new HashSet<>();
        query.accept(new SqlShuttle() {
            @Override
            public SqlNode visit(SqlIdentifier identifier) {
                // check whether this is fully qualified table name
                if (identifier.names.size() == 3) {
                    dsSet.add(identifier.names.get(0));
                }
                return identifier;
            }
        });

        return new ArrayList<>(dsSet);
    }

    private void checkSelectPrivilegeThrow(SqlNode query) throws PostgresException {
        final PostgresException[] e = {null};

        query.accept(new SqlShuttle() {
            @Override
            public SqlNode visit(SqlIdentifier identifier) {
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

    private boolean checkSystemPrivilege(SystemPrivilege sysPriv) {
        PostgresException e = checkSystemPrivilegeInternal(sysPriv);
        if (e == null)
            return true;

        LOG.error(ExceptionUtils.getStackTrace(e));
        return false;
    }

    private void checkSystemPrivilegeThrow(SystemPrivilege sysPriv) throws PostgresException {
        PostgresException e = checkSystemPrivilegeInternal(sysPriv);
        if (e != null)
            throw e;
    }

    private PostgresException checkSystemPrivilegeInternal(SystemPrivilege sysPriv) {
        Set<SystemPrivilege> userSysPrivs;

        try {
            String userName = Session.currentSession().getClientParam(PostgresConfiguration.PARAM_USER);
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

    private void checkObjectPrivilegeThrow(ObjectPrivilege objPriv, String[] schemaName) throws PostgresException {
        PostgresException e = checkObjectPrivilegeInternal(objPriv, schemaName);
        if (e != null)
            throw e;
    }

    private PostgresException checkObjectPrivilegeInternal(ObjectPrivilege objPriv, String[] schemaName) {
        Set<ObjectPrivilege> schemaObjPrivs;
        String userName = Session.currentSession().getClientParam(PostgresConfiguration.PARAM_USER);

        try {
            MetaSchemaPrivilege schemaPriv = metaContext.getSchemaPrivilege(schemaName, userName);
            schemaObjPrivs = schemaPriv == null ? null : schemaPriv.getObjectPrivileges();
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
        public void set(String confParam, String confValue) throws Exception {
            Session.currentSession().setClientParam(confParam, confValue);
        }

        @Override
        public void addDataSource(String dataSourceName, String jdbcConnectionString, String jdbcDriverName) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.ALTER_SYSTEM);

            // FIXME: all or nothing
            MetaDataSource dataSource = metaContext.addJdbcDataSource(jdbcDriverName, jdbcConnectionString, dataSourceName);
            connectionManager.registerPool(dataSourceName,
                    jdbcDriverName, jdbcConnectionString);
            schemaManager.addDataSource(dataSource);
        }

        @Override
        public void updateDataSource(OctopusSqlObjectTarget target) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.ALTER_SYSTEM);

            /* TODO: reloading schemaManager could be inefficient! */

            String dataSourceName = target.getDataSource();
            final String schemaRegex = target.getSchema() == null ? null : convertPattern(target.getSchema());
            final String tableRegex = target.getTable() == null ? null : convertPattern(target.getTable());
            MetaDataSource dataSource = metaContext.updateJdbcDataSource(dataSourceName, schemaRegex, tableRegex);
            schemaManager.dropDataSource(dataSourceName);
            schemaManager.addDataSource(dataSource);
        }

        @Override
        public void dropDataSource(String dataSourceName) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.ALTER_SYSTEM);

            metaContext.dropJdbcDataSource(dataSourceName);
            schemaManager.dropDataSource(dataSourceName);
            connectionManager.closePool(dataSourceName);
        }

        @Override
        public void createUser(String name, String password) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.CREATE_USER);
            metaContext.createUser(name, password);
        }

        @Override
        public void alterUser(String name, String password, String oldPassword) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.ALTER_USER);
            metaContext.alterUser(name, password);
        }

        @Override
        public void dropUser(String name) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.DROP_USER);
            metaContext.dropUser(name);
        }

        public void createRole(String role) throws Exception {
            metaContext.createRole(role);
        }

        @Override
        public void dropRole(String role) throws Exception {
            metaContext.dropRoleByName(role);
        }

        @Override
        public void grantSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> grantees) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.GRANT_ANY_PRIVILEGE);
            metaContext.addSystemPrivileges(sysPrivs, grantees);
        }

        @Override
        public void revokeSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> revokees) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.GRANT_ANY_PRIVILEGE);
            metaContext.removeSystemPrivileges(sysPrivs, revokees);
        }

        @Override
        public void grantObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] objName, List<String> grantees) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE);
            metaContext.addObjectPrivileges(objPrivs, objName, grantees);
        }

        @Override
        public void revokeObjectPrivileges(List<ObjectPrivilege> objPrivs, String[] objName, List<String> revokees) throws Exception {
            checkSystemPrivilegeThrow(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE);
            metaContext.removeObjectPrivileges(objPrivs, objName, revokees);
        }

        @Override
        public TupleSet showTxIsolationLevel() throws Exception {
            Tuple t = new Tuple(1);
            t.setDatum(0, "read committed");

            TupleSetSql ts = new TupleSetSql();
            ts.addTuple(t);
            return ts;
        }

        @Override
        public TupleSet showDataSources() throws Exception {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            for (MetaDataSource mDs : metaContext.getDataSources()) {
                Tuple t = new Tuple(2);
                t.setDatum(0, mDs.getName());
                t.setDatum(1, mDs.getComment());

                tuples.add(t);
            }
            Collections.sort(tuples, new Comparator<Tuple>() {
                @Override
                public int compare(Tuple tl, Tuple tr) {
                    return ((String) tl.getDatum(0)).compareTo((String) tr.getDatum(0));
                }
            });

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public TupleSet showSchemas(String dataSourceName, String schemaPattern) throws Exception {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            final String regex = schemaPattern == null ? null : convertPattern(schemaPattern);
            for (MetaDataSource mDs : metaContext.getDataSources()) {
                String dsName = mDs.getName();
                if (dataSourceName != null && !dataSourceName.equals(dsName))
                    continue;

                for (MetaSchema mSchema : mDs.getSchemas()) {
                    String schemaName = mSchema.getName();
                    if (regex != null && !schemaName.matches(regex))
                        continue;

                    Tuple t = new Tuple(4);
                    t.setDatum(0, schemaName);
                    t.setDatum(1, dsName);
                    t.setDatum(2, mSchema.getComment());
                    t.setDatum(3, mDs.getComment());

                    tuples.add(t);
                }
            }
            // ordered by TABLE_CATALOG and TABLE_SCHEM
            Collections.sort(tuples, new Comparator<Tuple>() {
                @Override
                public int compare(Tuple tl, Tuple tr) {
                    int r = ((String) tl.getDatum(1)).compareTo((String) tr.getDatum(1));
                    if (r == 0)
                        return ((String) tl.getDatum(0)).compareTo((String) tr.getDatum(0));
                    else
                        return r;
                }
            });

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public TupleSet showTables(String dataSourceName, String schemaPattern, String tablePattern) throws Exception {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            final String schemaRegex = schemaPattern == null ? null : convertPattern(schemaPattern);
            final String tableRegex = tablePattern == null ? null : convertPattern(tablePattern);
            for (MetaDataSource mDs : metaContext.getDataSources()) {
                String dsName = mDs.getName();
                if (dataSourceName != null && !dataSourceName.equals(dsName))
                    continue;

                for (MetaSchema mSchema : mDs.getSchemas()) {
                    String schemaName = mSchema.getName();
                    if (schemaRegex != null && !schemaName.matches(schemaRegex))
                        continue;

                    for (MetaTable mTable : mSchema.getTables()) {
                        String tableName = mTable.getName();
                        if (tableRegex != null && !tableName.matches(tableRegex))
                            continue;

                        Tuple t = new Tuple(12);
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
                        t.setDatum(10, mDs.getComment());
                        t.setDatum(11, mSchema.getComment());

                        tuples.add(t);
                    }
                }
            }
            // ordered by TABLE_TYPE, TABLE_CAT, TABLE_SCHEM and TABLE_NAME
            Collections.sort(tuples, new Comparator<Tuple>() {
                @Override
                public int compare(Tuple tl, Tuple tr) {
                    int r = ((String) tl.getDatum(3)).compareTo((String) tr.getDatum(3));
                    if (r == 0)
                        r = ((String) tl.getDatum(0)).compareTo((String) tr.getDatum(0));
                    if (r == 0)
                        r = ((String) tl.getDatum(1)).compareTo((String) tr.getDatum(1));
                    if (r == 0)
                        r = ((String) tl.getDatum(2)).compareTo((String) tr.getDatum(2));
                    return r;
                }
            });

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public TupleSet showColumns(String dataSourceName, String schemaPattern, String tablePattern, String columnPattern) throws Exception {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            /* TODO: convert '%' to null to avoid performing useless matching */
            final String schemaRegex = schemaPattern == null || "%".equals(schemaPattern) ? null : convertPattern(schemaPattern);
            final String tableRegex = tablePattern == null || "%".equals(tablePattern) ? null : convertPattern(tablePattern);
            final String columnRegex = columnPattern == null || "%".equals(columnPattern) ? null : convertPattern(columnPattern);

            Collection<ResultOfGetColumns> results = metaContext.getColumns(dataSourceName, schemaRegex, tableRegex, columnRegex);
            for (ResultOfGetColumns result : results) {
                Tuple t = new Tuple(28);
                t.setDatum(0, result.getDataSourceName());
                t.setDatum(1, result.getSchemaName());
                t.setDatum(2, result.getTableName());
                t.setDatum(3, result.getColumnName());
                t.setDatum(4, String.valueOf(result.getColumnType()));
                t.setDatum(5, TypeInfo.postresTypeOfJdbcType(result.getColumnType()).typeName());
                t.setDatum(6, "NULL");
                t.setDatum(7, "NULL");
                t.setDatum(8, "NULL");
                t.setDatum(9, "NULL");
                t.setDatum(10, "NULL");
                t.setDatum(11, result.getComment());
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
                t.setDatum(24, result.getDataCategory());
                t.setDatum(25, result.getDataSourceComment());
                t.setDatum(26, result.getSchemaComment());
                t.setDatum(27, result.getTableComment());
                tuples.add(t);
            }

            ts.addTuples(tuples);
            return ts;
        }

        @Override
        public TupleSet showTablePrivileges(String dataSourceName, String schemapattern, String tablepattern) throws Exception {
            //TODO
            return null;
        }

        @Override
        public TupleSet showColumnPrivileges(String dataSourceName, String schemapattern, String tablepattern, String columnpattern) throws Exception {
            // TODO
            return null;
        }

        @Override
        public TupleSet showAllUsers() throws Exception {
            TupleSetSql ts = new TupleSetSql();

            List<Tuple> tuples = new ArrayList<>();
            for (MetaUser mUser : metaContext.getUsers()) {
                Tuple t = new Tuple(2);
                t.setDatum(0, mUser.getName());
                t.setDatum(1, mUser.getComment());
                tuples.add(t);
            }

            ts.addTuples(tuples);
            return ts;
        }

        class IdsComparator implements Comparator<String[]> {
            @Override
            public int compare(String[] idsL, String[] idsR) {
                assert idsL.length == 2 && idsR.length == 2;
                int r = idsL[0].compareTo(idsR[0]);
                if (r != 0)
                    return r;
                return idsL[1].compareTo(idsR[1]);
            }
        }

        @Override
        public TupleSet showObjPrivsFor(String userName) throws Exception {
            Map<String[], Set<ObjectPrivilege>> objPrivsMap = new TreeMap<>(new IdsComparator());
            for (MetaSchemaPrivilege schemaPriv : metaContext.getSchemaPrivilegesByUser(userName)) {
                MetaSchema schema = schemaPriv.getSchema();
                String[] ids = {schema.getDataSource().getName(), schema.getName()};
                objPrivsMap.put(ids, schemaPriv.getObjectPrivileges());
            }

            TupleSetSql ts = new TupleSetSql();

            MetaUser user = metaContext.getUser(userName);
            Set<SystemPrivilege> userSysPrivs = user.getSystemPrivileges();
            boolean hasSelectAny = userSysPrivs.contains(SystemPrivilege.SELECT_ANY_TABLE);
            boolean hasCommentAny = userSysPrivs.contains(SystemPrivilege.COMMENT_ANY);
            if (hasSelectAny || hasCommentAny) {
                String selectPrivStr = ObjectPrivilege.SELECT.name();
                String commentPrivStr = ObjectPrivilege.COMMENT.name();
                for (MetaDataSource ds : metaContext.getDataSources()) {
                    for (MetaSchema schema : ds.getSchemas()) {
                        String[] ids = {ds.getName(), schema.getName()};
                        Set<ObjectPrivilege> objPrivs = objPrivsMap.get(ids);
                        if (objPrivs == null) {
                            Tuple t = new Tuple(3);
                            t.setDatum(0, ds.getName());
                            t.setDatum(1, schema.getName());

                            String objPrivStr;
                            if (hasSelectAny && hasCommentAny)
                                objPrivStr = selectPrivStr + ',' + commentPrivStr;
                            else if (hasSelectAny)
                                objPrivStr = selectPrivStr;
                            else
                                objPrivStr = commentPrivStr;
                            t.setDatum(2, objPrivStr);

                            ts.addTuple(t);
                        } else {
                            if (hasSelectAny)
                                objPrivs.add(ObjectPrivilege.SELECT);
                            if (hasCommentAny)
                                objPrivs.add(ObjectPrivilege.COMMENT);
                        }
                    }
                }
            }

            for (Map.Entry<String[], Set<ObjectPrivilege>> e : objPrivsMap.entrySet()) {
                String[] ids = e.getKey();
                assert ids.length == 2;

                Tuple t = new Tuple(3);
                t.setDatum(0, ids[0]);
                t.setDatum(1, ids[1]);

                Set<ObjectPrivilege> objPrivs = e.getValue();
                assert objPrivs.size() > 0;
                Iterator<ObjectPrivilege> iter = objPrivs.iterator();
                StringBuilder builder = new StringBuilder();
                builder.append(iter.next().name());
                while (iter.hasNext())
                    builder.append(',').append(iter.next().name());
                t.setDatum(2, builder.toString());

                ts.addTuple(t);
            }

            return ts;
        }

        private Tuple makeTupleForShowcomments(String type, String dataSource, String schema, String table, String column,
                                               String dsComment, String schemComment, String tblComment, String colComment) {
            Tuple t = new Tuple(9);
            t.setDatum(0, type);
            t.setDatum(1, dataSource == null ? "NULL" : dataSource);
            t.setDatum(2, schema == null ? "NULL" : schema);
            t.setDatum(3, table == null ? "NULL" : table);
            t.setDatum(4, column == null ? "NULL" : column);
            t.setDatum(5, dsComment == null ? "NULL" : dsComment);
            t.setDatum(6, schemComment == null ? "NULL" : schemComment);
            t.setDatum(7, tblComment == null ? "NULL" : tblComment);
            t.setDatum(8, colComment == null ? "NULL" : colComment);
            return t;
        }

        @Override
        public TupleSet showComments(String commentPattern, String dataSourcePattern,
                                     String schemaPattern, String tablePattern, String columnPattern) throws Exception {
            TupleSetSql ts = new TupleSetSql();

            final String dsType = "CATALOG";
            final String schemaType = "SCHEMA";
            final String tableType = "TABLE";
            final String columnType = "COLUMN";

            List<Tuple> tuples = new ArrayList<>();
            final String commentRegex = convertPattern(commentPattern);
            final String dataSourceRegex = convertPattern(dataSourcePattern);
            final String schemaRegex = convertPattern(schemaPattern);
            final String tableRegex = convertPattern(tablePattern);
            final String columnRegex = convertPattern(columnPattern);
            for (MetaDataSource mDs : metaContext.getDataSources()) {
                String dsName = mDs.getName();
                if (!dsName.matches(dataSourceRegex))
                    continue;

                if (mDs.getComment().matches(commentRegex))
                    tuples.add(makeTupleForShowcomments(dsType, dsName, null, null, null, mDs.getComment(), null, null, null));

                for (MetaSchema mSchema : mDs.getSchemas()) {
                    String schemaName = mSchema.getName();
                    if (!schemaName.matches(schemaRegex))
                        continue;

                    if (mSchema.getComment().matches(commentRegex))
                        tuples.add(makeTupleForShowcomments(schemaType, dsName, schemaName, null, null, mDs.getComment(), mSchema.getComment(), null, null));

                    for (MetaTable mTable : mSchema.getTables()) {
                        String tableName = mTable.getName();
                        if (!tableName.matches(tableRegex))
                            continue;

                        if (mTable.getComment().matches(commentRegex))
                            tuples.add(makeTupleForShowcomments(tableType, dsName, schemaName, tableName, null, mDs.getComment(), mSchema.getComment(), mTable.getComment(), null));

                        for (MetaColumn mColumn : mTable.getColumns()) {
                            String colName = mColumn.getName();
                            if (!colName.matches(columnRegex))
                                continue;

                            if (mColumn.getComment().matches(commentRegex))
                                tuples.add(makeTupleForShowcomments(columnType, dsName, schemaName, tableName, colName, mDs.getComment(), mSchema.getComment(), mTable.getComment(), mColumn.getComment()));
                        }
                    }
                }
            }

            Collections.sort(tuples, new Comparator<Tuple>() {
                @Override
                public int compare(Tuple tl, Tuple tr) {
                    int r = ((String) tl.getDatum(0)).compareTo((String) tr.getDatum(0));
                    if (r == 0)
                        r = ((String) tl.getDatum(1)).compareTo((String) tr.getDatum(1));
                    if (r == 0)
                        r = ((String) tl.getDatum(2)).compareTo((String) tr.getDatum(2));
                    if (r == 0)
                        r = ((String) tl.getDatum(3)).compareTo((String) tr.getDatum(3));
                    if (r == 0)
                        r = ((String) tl.getDatum(4)).compareTo((String) tr.getDatum(4));
                    return r;
                }
            });

            ts.addTuples(tuples);

            return ts;
        }

        @Override
        public void commentOn(OctopusSqlObjectTarget target, String comment) throws Exception {
            switch (target.getType()) {
            case DATASOURCE:
            case USER:
                checkSystemPrivilegeThrow(SystemPrivilege.COMMENT_ANY);
                break;
            case SCHEMA:
            case TABLE:
            case COLUMN:
                if (!checkSystemPrivilege(SystemPrivilege.COMMENT_ANY))
                    checkObjectPrivilegeThrow(ObjectPrivilege.COMMENT, new String[] {target.getDataSource(), target.getSchema()});
                break;
            default:
                throw new RuntimeException("could not reach here");
            }

            switch (target.getType()) {
            case DATASOURCE:
                metaContext.commentOnDataSource(comment, target.getDataSource());
                break;
            case SCHEMA:
                metaContext.commentOnSchema(comment, target.getDataSource(), target.getSchema());
                break;
            case TABLE:
                metaContext.commentOnTable(comment, target.getDataSource(), target.getSchema(), target.getTable());
                break;
            case COLUMN:
                metaContext.commentOnColumn(comment, target.getDataSource(), target.getSchema(), target.getTable(), target.getColumn());
                break;
            case USER:
                metaContext.commentOnUser(comment, target.getUser());
                break;
            default:
                throw new RuntimeException("could not reach here");
            }
        }

        @Override
        public void setDataCategoryOn(OctopusSqlObjectTarget target, String category) throws Exception {
            if (!checkSystemPrivilege(SystemPrivilege.COMMENT_ANY))
                checkObjectPrivilegeThrow(ObjectPrivilege.COMMENT, new String[] {target.getDataSource(), target.getSchema()});

            metaContext.setDataCategoryOn(category, target.getDataSource(), target.getSchema(), target.getTable(), target.getColumn());
        }
    };

    /**
     * Convert a pattern containing JDBC catalog search wildcards into
     * Java regex patterns.
     *
     * @param pattern input which may contain '%' or '_' wildcard characters
     * @return replace %/_ with regex search characters, also handle escaped
     * characters.
     * <p/>
     * Borrowed from Tajo
     */
    private String convertPattern(final String pattern) {
        final boolean ignoreCase = conf.getBoolean("master.query.ddl.like.ignorecase", false);
        final char searchStringEscape = '\\';

        String convertedPattern;
        if (pattern == null) {
            convertedPattern = ".*";
        } else {
            StringBuilder result = new StringBuilder(pattern.length());

            boolean escaped = false;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                if (escaped) {
                    if (c != searchStringEscape)
                        escaped = false;
                    result.append(c);
                } else {
                    if (c == searchStringEscape)
                        escaped = true;
                    else if (c == '%')
                        result.append(".*");
                    else if (c == '_')
                        result.append('.');
                    else
                        result.append(c);
                }
            }

            convertedPattern = result.toString();
        }

        if (ignoreCase)
            convertedPattern = "(?i)" + convertedPattern;

        return convertedPattern;
    }
}

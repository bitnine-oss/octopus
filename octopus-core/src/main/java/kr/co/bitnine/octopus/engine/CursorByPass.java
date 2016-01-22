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

import java.sql.DriverManager;
import kr.co.bitnine.octopus.frame.ConnectionManager;
import kr.co.bitnine.octopus.frame.Session;
import kr.co.bitnine.octopus.postgres.access.common.TupleDesc;
import kr.co.bitnine.octopus.postgres.catalog.PostgresAttribute;
import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.adt.IoFunction;
import kr.co.bitnine.octopus.postgres.utils.adt.IoFunctions;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.util.SqlShuttle;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public final class CursorByPass extends Portal {
    private static final Log LOG = LogFactory.getLog(CursorByPass.class);

    private final int sessionId;
    private final String dataSourceName;
    private final String queryString;

    private Connection conn;
    private PreparedStatement stmt;
    private TupleSetByPass tupSetByPass;
    private TupleDesc tupDesc;

    public CursorByPass(CachedStatement cachedStatement, String name,
                        FormatCode[] paramFormats, byte[][] paramValues,
                        FormatCode[] resultFormats, String dataSourceName)
            throws PostgresException {
        super(cachedStatement, name, paramFormats, paramValues, resultFormats);

        sessionId = Session.currentSession().getId();
        this.dataSourceName = dataSourceName;

        CachedStatement cStmt = (CachedStatement) getCachedQuery();
        if (dataSourceName == null) {
            /*
             * Complex or MetaModel queries are processed by Calcite via Avatica JDBC driver.
             * So they are not converted to DSN form.
             */
            SqlDialect.DatabaseProduct dp = SqlDialect.DatabaseProduct.POSTGRESQL;
            queryString = cStmt.getValidatedQuery().toSqlString(dp.getDialect()).getSql();
        } else {
            /*
             * NOTE: Deep-copy validatedQuery because TableNameTranslator.toDSN()
             *       changes identifiers of validatedQuery itself.
             *       When this Portal runs again without copied one,
             *       the by-pass test in processBind() which uses the validatedQuery
             *       will produce an error.
             *       To reduce number of copies, cache queryString.
             */
            SqlNode cloned = cStmt.getValidatedQuery().accept(new SqlShuttle() {
                @Override
                public SqlNode visit(SqlIdentifier id) {
                    return id.clone(id.getParserPosition());
                }
            });
            TableNameTranslator.toDSN(cloned);
            SqlDialect.DatabaseProduct dp = SqlDialect.DatabaseProduct.POSTGRESQL;
            queryString = cloned.toSqlString(dp.getDialect()).getSql();
        }
    }

    private void prepareStatement() throws PostgresException {
        if (getState() != State.NEW)
            return;

        CachedStatement cStmt = (CachedStatement) getCachedQuery();
        PostgresType[] types = cStmt.getParamTypes();
        FormatCode[] formats = getParamFormats();
        byte[][] values = getParamValues();

        try {
            if (dataSourceName == null) { // complex query
                conn = DriverManager.getConnection("jdbc:octopus-calcite:");
                LOG.info("Avatica JDBC connection for session(" + sessionId + ')');
            } else {
                conn = ConnectionManager.getConnection(dataSourceName);
                LOG.info("borrow connection to " + dataSourceName + " for session(" + sessionId + ')');
            }

            LOG.info("By Pass Query: " + queryString);

            stmt = conn.prepareStatement(queryString);
            if (types.length > 0) {
                for (int i = 0; i < types.length; i++) {
                    if (values[i] == null) {
                        switch (types[i]) {
                        case INT4:
                        case INT8:
                        case FLOAT4:
                        case FLOAT8:
                        case VARCHAR:
                            stmt.setNull(i + 1, TypeInfo.jdbcTypeOfPostgresType(types[i]));
                            break;
                        case NUMERIC:   // TODO
                        case DATE:      // TODO
                        case TIMESTAMP: // TODO
                        default:
                            setState(State.FAILED);

                            close();

                            PostgresErrorData edata = new PostgresErrorData(
                                    PostgresSeverity.ERROR,
                                    PostgresSQLState.FEATURE_NOT_SUPPORTED,
                                    "parameter type " + types[i].name() + "not supported");
                            throw new PostgresException(edata);
                        }
                        continue;
                    }

                    IoFunction io = IoFunctions.ofType(types[i]);
                    switch (types[i]) {
                    case INT4:
                        if (formats[i] == FormatCode.TEXT)
                            stmt.setInt(i + 1, (Integer) io.in(values[i]));
                        else
                            stmt.setInt(i + 1, (Integer) io.recv(values[i]));
                        break;
                    case INT8:
                        if (formats[i] == FormatCode.TEXT)
                            stmt.setLong(i + 1, (Long) io.in(values[i]));
                        else
                            stmt.setLong(i + 1, (Long) io.recv(values[i]));
                        break;
                    case FLOAT4:
                        if (formats[i] == FormatCode.TEXT)
                            stmt.setFloat(i + 1, (Float) io.in(values[i]));
                        else
                            stmt.setFloat(i + 1, (Float) io.recv(values[i]));
                        break;
                    case FLOAT8:
                        if (formats[i] == FormatCode.TEXT)
                            stmt.setDouble(i + 1, (Double) io.in(values[i]));
                        else
                            stmt.setDouble(i + 1, (Double) io.recv(values[i]));
                        break;
                    case VARCHAR:
                        if (formats[i] == FormatCode.TEXT)
                            stmt.setString(i + 1, (String) io.in(values[i]));
                        else
                            stmt.setString(i + 1, (String) io.recv(values[i]));
                        break;
                    case NUMERIC:   // TODO
                    case DATE:      // TODO
                    case TIMESTAMP: // TODO
                    default:
                        PostgresErrorData edata = new PostgresErrorData(
                                PostgresSeverity.ERROR,
                                PostgresSQLState.FEATURE_NOT_SUPPORTED,
                                "parameter type " + types[i].name() + "not supported");
                        throw new PostgresException(edata);
                    }
                }
            }

            setState(State.READY);
        } catch (SQLException e) {
            setState(State.FAILED);

            close();

            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to prepare by-pass query: " + e.getMessage());
            throw new PostgresException(edata, e);
        }
    }

    private void checkCancel() throws PostgresException {
        if (Session.currentSession().isCanceled()) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    PostgresSQLState.QUERY_CANCELED,
                    "canceling statement for session(" + sessionId + ") due to user request");
            throw new PostgresException(edata);
        }
    }

    private void execute(int numRows) throws PostgresException {
        if (getState() == State.DONE || getState() == State.FAILED)
            setState(State.READY);

        if (getState() != State.READY)
            return;

        LOG.debug("execute CursorByPass (rows=" + numRows + ")");

        try {
            // NOTE: some JDBC drivers do not ignore setFetchSize(0)
            if (numRows > 0)
                stmt.setFetchSize(numRows);

            checkCancel();
            ResultSet rs = stmt.executeQuery();
            checkCancel();

            ResultSetMetaData rsmd = rs.getMetaData();
            int colCnt = rsmd.getColumnCount();
            PostgresAttribute[] attrs = new PostgresAttribute[colCnt];
            for (int i = 0; i < colCnt; i++) {
                String colName = rsmd.getColumnName(i + 1);
                int colType = rsmd.getColumnType(i + 1);
                LOG.info("JDBC type of column '" + colName + "' is " + colType);
                PostgresType type = TypeInfo.postresTypeOfJdbcType(colType);
                int typeInfo = -1;
                if (type == PostgresType.VARCHAR)
                    typeInfo = rsmd.getColumnDisplaySize(i + 1);
                attrs[i] = new PostgresAttribute(colName, type, typeInfo);
            }

            tupDesc = new TupleDesc(attrs, getResultFormats());
            tupSetByPass = new TupleSetByPass(this, rs, tupDesc);

            setState(State.ACTIVE);
        } catch (SQLException e) {
            setState(State.FAILED);

            close();

            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to execute by-pass query: " + e.getMessage());
            throw new PostgresException(edata, e);
        }
    }

    @Override
    public TupleDesc describe() throws PostgresException {
        prepareStatement();
        execute(0);

        assert tupDesc != null;
        return tupDesc;
    }

    @Override
    public TupleSet run(int numRows) throws PostgresException {
        prepareStatement();
        execute(numRows);

        tupSetByPass.resetFetchSize(numRows);

        return tupSetByPass;
    }

    @Override
    public String generateCompletionTag(String commandTag) {
        return commandTag;
    }

    @Override
    public void close() {
        if (conn == null)
            return;

        if (stmt != null) {
            try {
                stmt.cancel();
            } catch (SQLException | UnsupportedOperationException e) {
                LOG.error("failed to cancel statement for session(" + sessionId + ")\n" + ExceptionUtils.getStackTrace(e));
            } finally {
                try {
                    stmt.close();
                } catch (SQLException ignore) { }
                stmt = null;
            }
        }

        try {
            LOG.info("return connection to \"" + dataSourceName + "\" for session(" + sessionId + ')');
            conn.close();
        } catch (SQLException ignore) { }
        conn = null;

        tupDesc = null;
        tupSetByPass = null;    // how about ResultSet?
    }
}

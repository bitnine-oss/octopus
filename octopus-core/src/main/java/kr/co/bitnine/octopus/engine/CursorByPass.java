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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

public class CursorByPass extends Portal
{
    private final Log LOG = LogFactory.getLog(CursorByPass.class);

    private final String jdbcDriver;
    private final String jdbcConnectionString;

    private final String queryString;

    private PreparedStatement stmt;
    private TupleSetByPass tupSetByPass;
    private TupleDesc tupDesc;

    public CursorByPass(CachedStatement cachedStatement, FormatCode[] paramFormats, byte[][] paramValues, FormatCode[] resultFormats, String jdbcDriver, String jdbcConnectionString) throws PostgresException
    {
        super(cachedStatement, paramFormats, paramValues, resultFormats);

        this.jdbcDriver = jdbcDriver;
        this.jdbcConnectionString = jdbcConnectionString;

        /*
         * NOTE: Deep-copy validatedQuery because TableNameTranslator.toDSN()
         *       changes identifiers of validatedQuery itself.
         *       When this Portal runs again without copied one,
         *       the by-pass test in processBind() which uses the validatedQuery
         *       will produce an error.
         *       To reduce number of copies, cache queryString.
         */
        CachedStatement cStmt = (CachedStatement) getCachedQuery();
        SqlNode cloned = cStmt.getValidatedQuery().accept(new SqlShuttle() {
            @Override
            public SqlNode visit(SqlIdentifier id)
            {
                return id.clone(id.getParserPosition());
            }
        });
        TableNameTranslator.toDSN(cloned);
        queryString = cloned.toSqlString(SqlDialect.DatabaseProduct.ORACLE.getDialect()).getSql();

        stmt = null;
        tupSetByPass = null;
        tupDesc = null;
    }

    private void prepareStatement() throws PostgresException
    {
        if (getState() != State.NEW)
            return;

        CachedStatement cStmt = (CachedStatement) getCachedQuery();
        PostgresType[] types = cStmt.getParamTypes();
        FormatCode[] formats = getParamFormats();
        byte[][] values = getParamValues();

        try {
            Class.forName(jdbcDriver);
            Connection conn = DriverManager.getConnection(jdbcConnectionString);
            stmt = conn.prepareStatement(queryString);
            if (types.length > 0) {

                for (int i = 0; i < types.length; i++) {
                    IoFunction io = IoFunctions.ofType(types[i]);
                    switch (types[i]) {
                        case INT4:
                            if (formats[i] == FormatCode.TEXT)
                                stmt.setInt(i + 1, (Integer) io.in(values[i]));
                            else
                                stmt.setInt(i + 1, (Integer) io.recv(values[i]));
                            break;
                        case VARCHAR:
                            if (formats[i] == FormatCode.TEXT)
                                stmt.setString(i + 1, (String) io.in(values[i]));
                            else
                                stmt.setString(i + 1, (String) io.recv(values[i]));
                            break;
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
        } catch (ClassNotFoundException | SQLException e) {
            /*
             * NOTE: setting state to State.FAILED is meaningless here
             *       because this cursor will not be cached
             */

            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to run by-pass query");
            throw new PostgresException(edata, e);
        }
    }

    private void execute(int numRows) throws PostgresException
    {
        if (getState() == State.DONE || getState() == State.FAILED)
            setState(State.READY);

        if (getState() != State.READY)
            return;

        try {
            stmt.setFetchSize(numRows);

            ResultSet rs = stmt.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            int colCnt = rsmd.getColumnCount();
            PostgresAttribute[] attrs = new PostgresAttribute[colCnt];
            for (int i = 0; i < colCnt; i++) {
                String colName = rsmd.getColumnName(i + 1);
                int colType = rsmd.getColumnType(i + 1);
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

            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to run by-pass query");
            throw new PostgresException(edata, e);
        }
    }

    @Override
    public TupleDesc describe() throws PostgresException
    {
        prepareStatement();
        execute(0);

        assert tupDesc != null;
        return tupDesc;
    }

    @Override
    public TupleSet run(int numRows) throws PostgresException
    {
        prepareStatement();
        execute(numRows);

        tupSetByPass.resetFetchSize(numRows);

        return tupSetByPass;
    }

    @Override
    public String generateCompletionTag(String commandTag)
    {
        return commandTag;
    }

    @Override
    public void close()
    {
        if (stmt == null)
            return;

        try {
            Connection conn = stmt.getConnection();
            stmt.close();
            conn.close();
        } catch (SQLException e) { }
    }
}

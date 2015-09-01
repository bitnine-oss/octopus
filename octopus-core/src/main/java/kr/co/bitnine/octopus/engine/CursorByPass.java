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
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

public class CursorByPass extends Portal
{
    private final Log LOG = LogFactory.getLog(CursorByPass.class);

    private final String jdbcDriver;
    private final String jdbcConnectionString;

    private TupleSet tupSet;
    private TupleDesc tupDesc;

    public CursorByPass(CachedStatement cachedStatement, FormatCode[] paramFormats, byte[][] paramValues, FormatCode[] resultFormats, String jdbcDriver, String jdbcConnectionString)
    {
        super(cachedStatement, paramFormats, paramValues, resultFormats);

        this.jdbcDriver = jdbcDriver;
        this.jdbcConnectionString = jdbcConnectionString;

        tupSet = null;
    }

    private void prepare() throws PostgresException
    {
        if (state == State.ACTIVE)
            return;

        assert tupSet == null;

        try {
            Class.forName(jdbcDriver);
            Connection conn = DriverManager.getConnection(jdbcConnectionString);
            Statement stmt = conn.createStatement();

            CachedStatement cStmt = (CachedStatement) getCachedQuery();

            // FIXME: side effect
            TableNameTranslator.toDSN(cStmt.getValidatedQuery());

            ResultSet rs = stmt.executeQuery(cStmt.getValidatedQuery().toString());
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCnt = rsmd.getColumnCount();
            PostgresAttribute[] attrs = new PostgresAttribute[colCnt];
            for (int i = 0; i < colCnt; i++) {
                String colName = rsmd.getColumnName(i + 1);
                int colType = rsmd.getColumnType(i + 1);
                PostgresType type = TypeInfo.postresTypeOfJdbcType(colType);
                attrs[i] = new PostgresAttribute(colName, type);
            }
            tupDesc = new TupleDesc(attrs, getResultFormats());
            tupSet = new TupleSetByPass(rs, tupDesc);

            state = State.ACTIVE;
        } catch (ClassNotFoundException | SQLException e) {
            state = State.FAILED;

            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to run by-pass query");
            throw new PostgresException(edata, e);
        }
    }

    @Override
    public TupleDesc describe() throws PostgresException
    {
        prepare();

        return tupDesc;
    }

    @Override
    public TupleSet run(int numRows) throws PostgresException
    {
        prepare();

        // TODO: change state to DONE
        return tupSet;
    }

    @Override
    public String generateCompletionTag(String commandTag)
    {
        return commandTag;
    }

    @Override
    public void close() { }
}

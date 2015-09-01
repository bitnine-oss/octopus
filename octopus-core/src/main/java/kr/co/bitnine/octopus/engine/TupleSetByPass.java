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
import kr.co.bitnine.octopus.postgres.executor.Tuple;
import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.PostgresSQLState;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.utils.adt.Datum;
import kr.co.bitnine.octopus.postgres.utils.adt.DatumInt;
import kr.co.bitnine.octopus.postgres.utils.adt.DatumVarchar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TupleSetByPass implements TupleSet
{
    private ResultSet resultSet;
    private TupleDesc tupDesc;

    TupleSetByPass(ResultSet resultSet, TupleDesc tupDesc)
    {
        this.resultSet = resultSet;
        this.tupDesc = tupDesc;
    }

    @Override
    public TupleDesc getTupleDesc()
    {
        return tupDesc;
    }

    @Override
    public Tuple next() throws PostgresException
    {
        try {
            if (!resultSet.next())
                return null;

            PostgresAttribute[] attrs = tupDesc.getAttributes();
            Tuple t = new Tuple(attrs.length);
            for (int i = 0; i < attrs.length; i++) {
                Datum datum;
                switch (attrs[i].type) {
                    case INT4:
                        datum = new DatumInt(resultSet.getInt(i + 1));
                        break;
                    case VARCHAR:
                        datum = new DatumVarchar(resultSet.getString(i + 1));
                        break;
                    default:
                        PostgresErrorData edata = new PostgresErrorData(
                                PostgresSeverity.ERROR,
                                PostgresSQLState.FEATURE_NOT_SUPPORTED,
                                "currently data type " + attrs[i].type.name() + " is not supported");
                        throw new PostgresException(edata);
                }

                t.setDatum(i, datum);
            }
            return t;
        } catch (SQLException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to fetch a row");
            throw new PostgresException(edata, e);
        }
    }

    @Override
    public void close() throws PostgresException
    {
        try {
            Statement stmt = resultSet.getStatement();
            /*
             * If the result set was generated some other way,
             * such as by a DatabaseMetaData method, this method may return null.
             */
            if (stmt == null) {
                resultSet.close();
                return;
            }

            Connection conn = stmt.getConnection();

            resultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to close TupleSetByPass");
            throw new PostgresException(edata, e);
        }
    }
}

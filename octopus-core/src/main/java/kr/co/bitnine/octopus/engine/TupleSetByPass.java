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
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class TupleSetByPass implements TupleSet {
    private static final Log LOG = LogFactory.getLog(TupleSetByPass.class);

    private final Portal portal;
    private final ResultSet resultSet;
    private final TupleDesc tupDesc;

    private int fetchSize;
    private int fetchCount;

    TupleSetByPass(Portal portal, ResultSet resultSet, TupleDesc tupDesc) {
        this.portal = portal;
        this.resultSet = resultSet;
        this.tupDesc = tupDesc;

        fetchSize = 0;
        fetchCount = 0;
    }

    @Override
    public TupleDesc getTupleDesc() {
        return tupDesc;
    }

    @Override
    public Tuple next() throws PostgresException {
        if (fetchSize > 0 && fetchCount >= fetchSize)
            return null;

        try {
            if (!resultSet.next()) {
                portal.setState(Portal.State.DONE);
                return null;
            }

            PostgresAttribute[] attrs = tupDesc.getAttributes();
            Tuple t = new Tuple(attrs.length);
            for (int i = 0; i < attrs.length; i++) {
                Object datum;
                switch (attrs[i].getType()) {
                case INT4:
                    datum = resultSet.getInt(i + 1);
                    break;
                case INT8:
                    datum = resultSet.getLong(i + 1);
                    break;
                case FLOAT4:
                    datum = resultSet.getFloat(i + 1);
                    break;
                case FLOAT8:
                    datum = resultSet.getDouble(i + 1);
                    break;
                case NUMERIC:
                    datum = resultSet.getBigDecimal(i + 1);
                    break;
                case VARCHAR:
                    datum = resultSet.getString(i + 1);
                    break;
                case TIMESTAMP:
                    datum = resultSet.getTimestamp(i + 1);
                    break;
                default:
                    PostgresErrorData edata = new PostgresErrorData(
                            PostgresSeverity.ERROR,
                            PostgresSQLState.FEATURE_NOT_SUPPORTED,
                            "currently data type " + attrs[i].getType().name() + " is not supported");
                    throw new PostgresException(edata);
                }

                if (resultSet.wasNull())
                    datum = null;
                t.setDatum(i, datum);
            }
            fetchCount++;
            return t;
        } catch (SQLException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to fetch a row");
            throw new PostgresException(edata, e);
        }
    }

    @Override
    public void close() throws PostgresException {
        try {
            resultSet.close();
        } catch (SQLException e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to close TupleSetByPass");
            throw new PostgresException(edata, e);
        }
    }

    void resetFetchSize(int numRows) {
        fetchSize = numRows;
        fetchCount = 0;
    }
}

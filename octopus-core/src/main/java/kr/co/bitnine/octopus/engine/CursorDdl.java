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
import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import kr.co.bitnine.octopus.postgres.utils.PostgresErrorData;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.PostgresSeverity;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;
import kr.co.bitnine.octopus.postgres.utils.cache.Portal;
import kr.co.bitnine.octopus.sql.OctopusSql;
import kr.co.bitnine.octopus.sql.OctopusSqlCommand;
import kr.co.bitnine.octopus.sql.OctopusSqlRunner;
import kr.co.bitnine.octopus.sql.TupleSetSql;

public class CursorDdl extends Portal
{
    private final OctopusSqlRunner sqlRunner;

    CursorDdl(CachedStatement cachedStatement, OctopusSqlRunner sqlRunner)
    {
        super(cachedStatement, new FormatCode[0], new byte[0][], new FormatCode[0]);

        this.sqlRunner = sqlRunner;
    }

    @Override
    public TupleDesc describe() throws PostgresException
    {
        return getCachedQuery().describe();
    }

    @Override
    public TupleSet run(int numRows) throws PostgresException
    {
        CachedStatement cStmt = (CachedStatement) getCachedQuery();
        OctopusSqlCommand c = cStmt.getDdlCommands().get(0);
        try {
            TupleSetSql tsSql = (TupleSetSql) OctopusSql.run(c, sqlRunner);
            if (tsSql != null)
                tsSql.setTupleDesc(describe());
            return tsSql;
        } catch (PostgresException e) {
            throw e;
        } catch (Exception e) {
            PostgresErrorData edata = new PostgresErrorData(
                    PostgresSeverity.ERROR,
                    "failed to run DDL");
            throw new PostgresException(edata, e);
        }
    }

    @Override
    public String generateCompletionTag(String commandTag)
    {
        return commandTag;
    }

    @Override
    public void close() { }
}

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
import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.cache.CachedQuery;
import kr.co.bitnine.octopus.sql.OctopusSqlCommand;
import org.apache.calcite.sql.SqlNode;

import java.util.List;

public class CachedStatement extends CachedQuery
{
    private boolean isDdl;
    private final SqlNode validatedQuery;
    private List<OctopusSqlCommand> ddlCommands;
    private final String commandTag;

    public CachedStatement(SqlNode validatedQuery, String queryString, PostgresType[] paramTypes)
    {
        super(queryString, paramTypes);

        isDdl = false;
        this.validatedQuery = validatedQuery;
        ddlCommands = null;
        commandTag = "SELECT";
    }

    public CachedStatement(List<OctopusSqlCommand> commands)
    {
        super(null, new PostgresType[0]);

        isDdl = true;
        validatedQuery = null;
        ddlCommands = commands;
        commandTag = "???";
    }

    public boolean isDdl()
    {
        return isDdl;
    }

    public SqlNode getValidatedQuery()
    {
        return validatedQuery;
    }

    public List<OctopusSqlCommand> getDdlCommands()
    {
        return ddlCommands;
    }

    @Override
    public String getCommandTag()
    {
        return commandTag;
    }

    @Override
    public TupleDesc describe() throws PostgresException
    {
        return null;
    }

    @Override
    public void close() { }
}

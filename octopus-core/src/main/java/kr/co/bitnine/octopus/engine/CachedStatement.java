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

import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.utils.cache.CachedQuery;
import kr.co.bitnine.octopus.sql.OctopusSqlCommand;
import org.apache.calcite.sql.SqlNode;

import java.util.List;

public class CachedStatement extends CachedQuery
{
    private boolean isDdl;

    private final SqlNode validatedQuery;
    private final String commandTag;

    private List<OctopusSqlCommand> ddlCommands;

    public CachedStatement(SqlNode validatedQuery, String queryString, PostgresType[] paramTypes)
    {
        super(queryString, paramTypes);

        isDdl = false;

        this.validatedQuery = validatedQuery;
        commandTag = null;
    }

    public CachedStatement(List<OctopusSqlCommand> commands)
    {
        super(null, new PostgresType[0]);

        validatedQuery = null;
        commandTag = null;

        isDdl = true;

        ddlCommands = commands;
    }

    public boolean isDdl()
    {
        return isDdl;
    }

    public SqlNode getValidatedQuery()
    {
        return validatedQuery;
    }

    public String getCommandTag()
    {
        return commandTag;
    }

    public List<OctopusSqlCommand> getDdlCommands()
    {
        return ddlCommands;
    }
}

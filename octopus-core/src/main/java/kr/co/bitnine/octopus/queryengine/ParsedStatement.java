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

package kr.co.bitnine.octopus.queryengine;

import kr.co.bitnine.octopus.sql.OctopusSqlCommand;
import org.apache.calcite.sql.SqlNode;

import java.util.Arrays;
import java.util.List;

/*
 * prepared statement
 */
public class ParsedStatement
{
    private boolean isDdl;

    private SqlNode validatedQuery;
    private int[] oids;

    private List<OctopusSqlCommand> ddlCommands;

    public ParsedStatement(SqlNode validatedQuery, int[] oids)
    {
        isDdl = false;

        this.validatedQuery = validatedQuery;
        this.oids = oids;
    }

    public ParsedStatement(List<OctopusSqlCommand> commands)
    {
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

    public int[] getOids()
    {
        return Arrays.copyOf(oids, oids.length);
    }

    public List<OctopusSqlCommand> getDdlCommands()
    {
        return ddlCommands;
    }
}

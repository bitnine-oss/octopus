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

package kr.co.bitnine.octopus.postgres.utils.cache;

import kr.co.bitnine.octopus.postgres.access.common.TupleDesc;
import kr.co.bitnine.octopus.postgres.executor.TupleSet;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;
import kr.co.bitnine.octopus.postgres.utils.adt.FormatCode;

import java.util.Arrays;

public abstract class Portal
{
    private final CachedQuery cachedQuery;
    private final FormatCode[] paramFormats;
    private final byte[][] paramValues;
    private final FormatCode[] resultFormats;

    public enum State
    {
        NEW,
        READY,
        ACTIVE,
        DONE,
        FAILED
    }
    private State state;

    public Portal(CachedQuery cachedQuery, FormatCode[] paramFormats, byte[][] paramValues, FormatCode[] resultFormats)
    {
        this.cachedQuery = cachedQuery;
        this.paramFormats = paramFormats;
        this.paramValues = paramValues;

        // TODO: resultFormats.length == PostgresAttribute.length
        this.resultFormats = resultFormats;

        state = State.NEW;
    }

    public CachedQuery getCachedQuery()
    {
        return cachedQuery;
    }

    public FormatCode[] getParamFormats()
    {
        return paramFormats;
    }

    public byte[][] getParamValues()
    {
        return paramValues;
    }

    public FormatCode[] getResultFormats()
    {
        return resultFormats;
    }

    public State getState()
    {
        return state;
    }

    public void setState(State state)
    {
         this.state = state;
    }

    public String getCompletionTag()
    {
        String cmdTag = cachedQuery.getCommandTag();
        return cmdTag == null ? null : generateCompletionTag(cmdTag);
    }

    public abstract TupleDesc describe() throws PostgresException;
    public abstract TupleSet run(int numRows) throws PostgresException;
    public abstract String generateCompletionTag(String commandTag);
    public abstract void close();
}

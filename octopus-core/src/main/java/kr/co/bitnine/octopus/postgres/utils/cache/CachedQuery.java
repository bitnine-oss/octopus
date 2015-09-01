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
import kr.co.bitnine.octopus.postgres.catalog.PostgresType;
import kr.co.bitnine.octopus.postgres.utils.PostgresException;

import java.util.Arrays;

public abstract class CachedQuery
{
    private final String queryString;
    private final PostgresType[] paramTypes;

    public CachedQuery(String queryString, PostgresType[] paramTypes)
    {
        this.queryString = queryString;
        this.paramTypes = paramTypes;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public PostgresType[] getParamTypes()
    {
        return Arrays.copyOf(paramTypes, paramTypes.length);
    }

    public abstract String getCommandTag();
    public abstract TupleDesc describe() throws PostgresException;
    public abstract void close();
}

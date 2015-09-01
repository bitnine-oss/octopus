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

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class TypeInfo
{
    private static final Map<Integer, PostgresType> jdbcToPostgres = new HashMap<>();

    static
    {
        jdbcToPostgres.put(Types.BIGINT, PostgresType.INT8);
        jdbcToPostgres.put(Types.BINARY, PostgresType.BYTEA);
        jdbcToPostgres.put(Types.BIT, PostgresType.BIT);
        jdbcToPostgres.put(Types.BOOLEAN, PostgresType.BOOL);
        jdbcToPostgres.put(Types.CHAR, PostgresType.CHAR);
        jdbcToPostgres.put(Types.DATE, PostgresType.DATE);
        jdbcToPostgres.put(Types.DOUBLE, PostgresType.FLOAT8);
        jdbcToPostgres.put(Types.INTEGER, PostgresType.INT4);
        jdbcToPostgres.put(Types.NUMERIC, PostgresType.NUMERIC);
        jdbcToPostgres.put(Types.REAL, PostgresType.FLOAT4);
        jdbcToPostgres.put(Types.SMALLINT, PostgresType.INT2);
        jdbcToPostgres.put(Types.TIME, PostgresType.TIME);
        jdbcToPostgres.put(Types.TIMESTAMP, PostgresType.TIMESTAMP);
        jdbcToPostgres.put(Types.VARCHAR, PostgresType.VARCHAR);
    }

    public static PostgresType postresTypeOfJdbcType(int jdbcType)
    {
        PostgresType type = jdbcToPostgres.get(jdbcType);
        return type == null ? PostgresType.UNSPECIFIED : type;
    }
}

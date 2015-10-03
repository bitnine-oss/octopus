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
    private static final Map<PostgresType, Integer> postgresToJdbc = new HashMap<>();

    static
    {
        jdbcToPostgres.put(Types.BIGINT, PostgresType.INT8);
        jdbcToPostgres.put(Types.BINARY, PostgresType.BYTEA);
        jdbcToPostgres.put(Types.BIT, PostgresType.BIT);
        jdbcToPostgres.put(Types.BOOLEAN, PostgresType.BOOL);
        jdbcToPostgres.put(Types.CHAR, PostgresType.CHAR);
        jdbcToPostgres.put(Types.DATE, PostgresType.DATE);
        jdbcToPostgres.put(Types.DOUBLE, PostgresType.FLOAT8);
        jdbcToPostgres.put(Types.FLOAT, PostgresType.FLOAT4);
        jdbcToPostgres.put(Types.INTEGER, PostgresType.INT4);
        jdbcToPostgres.put(Types.NUMERIC, PostgresType.NUMERIC);
        jdbcToPostgres.put(Types.REAL, PostgresType.FLOAT4);
        jdbcToPostgres.put(Types.SMALLINT, PostgresType.INT2);
        jdbcToPostgres.put(Types.TIME, PostgresType.TIME);
        jdbcToPostgres.put(Types.TIMESTAMP, PostgresType.TIMESTAMP);
        jdbcToPostgres.put(Types.VARCHAR, PostgresType.VARCHAR);

        postgresToJdbc.put(PostgresType.BIT, Types.BIT);
        postgresToJdbc.put(PostgresType.BOOL, Types.BOOLEAN);
        postgresToJdbc.put(PostgresType.BYTEA, Types.BINARY);
        postgresToJdbc.put(PostgresType.CHAR, Types.CHAR);
        postgresToJdbc.put(PostgresType.DATE, Types.DATE);
        postgresToJdbc.put(PostgresType.FLOAT4, Types.FLOAT);
        postgresToJdbc.put(PostgresType.FLOAT4, Types.REAL);
        postgresToJdbc.put(PostgresType.FLOAT8, Types.DOUBLE);
        postgresToJdbc.put(PostgresType.INT2, Types.SMALLINT);
        postgresToJdbc.put(PostgresType.INT4, Types.INTEGER);
        postgresToJdbc.put(PostgresType.INT8, Types.BIGINT);
        postgresToJdbc.put(PostgresType.NUMERIC, Types.NUMERIC);
        postgresToJdbc.put(PostgresType.TIME, Types.TIME);
        postgresToJdbc.put(PostgresType.TIMESTAMP, Types.TIMESTAMP);
        postgresToJdbc.put(PostgresType.VARCHAR, Types.VARCHAR);
    }

    public static PostgresType postresTypeOfJdbcType(int jdbcType)
    {
        PostgresType type = jdbcToPostgres.get(jdbcType);
        return type == null ? PostgresType.UNSPECIFIED : type;
    }

    public static int jdbcTypeOfPostgresType(PostgresType postgresType)
    {
        Integer type = postgresToJdbc.get(postgresType);
        return type == null ? Types.NULL : type;
    }
}

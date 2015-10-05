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

public final class TypeInfo {
    private static final Map<Integer, PostgresType> JDBC_TO_POSTGRES = new HashMap<>();
    private static final Map<PostgresType, Integer> POSTGRES_TO_JDBC = new HashMap<>();

    static {
        JDBC_TO_POSTGRES.put(Types.BIGINT, PostgresType.INT8);
        JDBC_TO_POSTGRES.put(Types.BINARY, PostgresType.BYTEA);
        JDBC_TO_POSTGRES.put(Types.BIT, PostgresType.BIT);
        JDBC_TO_POSTGRES.put(Types.BOOLEAN, PostgresType.BOOL);
        JDBC_TO_POSTGRES.put(Types.CHAR, PostgresType.CHAR);
        JDBC_TO_POSTGRES.put(Types.DATE, PostgresType.DATE);
        JDBC_TO_POSTGRES.put(Types.DOUBLE, PostgresType.FLOAT8);
        JDBC_TO_POSTGRES.put(Types.FLOAT, PostgresType.FLOAT4);
        JDBC_TO_POSTGRES.put(Types.INTEGER, PostgresType.INT4);
        JDBC_TO_POSTGRES.put(Types.NUMERIC, PostgresType.NUMERIC);
        JDBC_TO_POSTGRES.put(Types.REAL, PostgresType.FLOAT4);
        JDBC_TO_POSTGRES.put(Types.SMALLINT, PostgresType.INT2);
        JDBC_TO_POSTGRES.put(Types.TIME, PostgresType.TIME);
        JDBC_TO_POSTGRES.put(Types.TIMESTAMP, PostgresType.TIMESTAMP);
        JDBC_TO_POSTGRES.put(Types.VARCHAR, PostgresType.VARCHAR);

        POSTGRES_TO_JDBC.put(PostgresType.BIT, Types.BIT);
        POSTGRES_TO_JDBC.put(PostgresType.BOOL, Types.BOOLEAN);
        POSTGRES_TO_JDBC.put(PostgresType.BYTEA, Types.BINARY);
        POSTGRES_TO_JDBC.put(PostgresType.CHAR, Types.CHAR);
        POSTGRES_TO_JDBC.put(PostgresType.DATE, Types.DATE);
        POSTGRES_TO_JDBC.put(PostgresType.FLOAT4, Types.FLOAT);
        POSTGRES_TO_JDBC.put(PostgresType.FLOAT4, Types.REAL);
        POSTGRES_TO_JDBC.put(PostgresType.FLOAT8, Types.DOUBLE);
        POSTGRES_TO_JDBC.put(PostgresType.INT2, Types.SMALLINT);
        POSTGRES_TO_JDBC.put(PostgresType.INT4, Types.INTEGER);
        POSTGRES_TO_JDBC.put(PostgresType.INT8, Types.BIGINT);
        POSTGRES_TO_JDBC.put(PostgresType.NUMERIC, Types.NUMERIC);
        POSTGRES_TO_JDBC.put(PostgresType.TIME, Types.TIME);
        POSTGRES_TO_JDBC.put(PostgresType.TIMESTAMP, Types.TIMESTAMP);
        POSTGRES_TO_JDBC.put(PostgresType.VARCHAR, Types.VARCHAR);
    }

    private TypeInfo() { }

    public static PostgresType postresTypeOfJdbcType(int jdbcType) {
        PostgresType type = JDBC_TO_POSTGRES.get(jdbcType);
        return type == null ? PostgresType.UNSPECIFIED : type;
    }

    public static int jdbcTypeOfPostgresType(PostgresType postgresType) {
        Integer type = POSTGRES_TO_JDBC.get(postgresType);
        return type == null ? Types.NULL : type;
    }
}

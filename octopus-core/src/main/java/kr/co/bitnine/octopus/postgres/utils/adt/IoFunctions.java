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

package kr.co.bitnine.octopus.postgres.utils.adt;

import kr.co.bitnine.octopus.postgres.catalog.PostgresType;

import java.util.HashMap;
import java.util.Map;

public final class IoFunctions {
    private static final Map<PostgresType, IoFunction> TYPE_TO_IO = new HashMap<>();

    static {
        TYPE_TO_IO.put(PostgresType.INT4, new IoInt4());
        TYPE_TO_IO.put(PostgresType.INT8, new IoInt8());
        TYPE_TO_IO.put(PostgresType.FLOAT4, new IoFloat4());
        TYPE_TO_IO.put(PostgresType.FLOAT8, new IoFloat8());
        TYPE_TO_IO.put(PostgresType.VARCHAR, new IoVarchar());
        TYPE_TO_IO.put(PostgresType.NUMERIC, new IoNumeric());
        TYPE_TO_IO.put(PostgresType.TIMESTAMP, new IoTimestamp());
    }

    private IoFunctions() { }

    public static IoFunction ofType(PostgresType type) {
        return TYPE_TO_IO.get(type);
    }
}

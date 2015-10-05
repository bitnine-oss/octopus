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

package kr.co.bitnine.octopus.postgres.catalog;

import java.util.HashMap;
import java.util.Map;

public enum PostgresType {
    UNSPECIFIED         (0,     0),

    BOOL                (16,    1),
    BYTEA               (17,    -1),
    CHAR                (18,    1),
    NAME                (19,    64),
    INT8                (20,    8),
    INT2                (21,    2),
    INT4                (23,    4),
    TEXT                (25,    -1),
    OID                 (26,    4),

    XML                 (142,   -1),
    XML_ARRAY           (143,   -1),

    POINT               (600,   16),
    BOX                 (603,   32),

    FLOAT4              (700,   4),
    FLOAT8              (701,   8),
    MONEY               (790,   8),
    MONEY_ARRAY         (791,   -1),

    BOOL_ARRAY          (1000,  -1),
    BYTEA_ARRAY         (1001,  -1),
    CHAR_ARRAY          (1002,  -1),
    NAME_ARRAY          (1003,  -1),
    INT2_ARRAY          (1005,  -1),
    INT4_ARRAY          (1007,  -1),
    TEXT_ARRAY          (1009,  -1),
    BPCHAR_ARRAY        (1014,  -1),
    VARCHAR_ARRAY       (1015,  -1),
    INT8_ARRAY          (1016,  -1),
    FLOAT4_ARRAY        (1021,  -1),
    FLOAT8_ARRAY        (1022,  -1),
    OID_ARRAY           (1028,  -1),
    BPCHAR              (1042,  -1),
    VARCHAR             (1043,  -1),
    DATE                (1082,  4),
    TIME                (1083,  8),

    TIMESTAMP           (1114,  8),
    TIMESTAMP_ARRAY     (1115,  -1),
    DATE_ARRAY          (1182,  -1),
    TIME_ARRAY          (1183,  -1),
    TIMESTAMPTZ         (1184,  8),
    TIMESTAMPTZ_ARRAY   (1185,  -1),
    INTERVAL            (1186,  16),
    INTERVAL_ARRAY      (1187,  -1),

    NUMERIC_ARRAY       (1231,  -1),
    TIMETZ              (1266,  12),
    TIMETZ_ARRAY        (1270,  -1),

    BIT                 (1560,  -1),
    BIT_ARRAY           (1561,  -1),
    VARBIT              (1562,  -1),
    VARBIT_ARRAY        (1563,  -1),

    NUMERIC             (1700,  -1),

    VOID                (2278,  4),

    UUID                (2950,  16),
    UUID_ARRAY          (2951,  -1);

    private final int oid;
    private final String typeName;

    /*
     * -1 indicates a "varlena" type (one that has a length word)
     * -2 indicates a null-terminated C string
     */
    private final int typeLength;

    PostgresType(int oid, int typeLength) {
        this.oid = oid;

        String tmpTypeName = name().toLowerCase();
        if (tmpTypeName.endsWith("_array"))
            tmpTypeName = "_" + tmpTypeName.substring(0, tmpTypeName.lastIndexOf('_'));
        this.typeName = tmpTypeName;

        this.typeLength = typeLength;
    }

    public int oid() {
        return oid;
    }

    public String typeName() {
        return typeName;
    }

    public int typeLength() {
        return typeLength;
    }

    private static final Map<Integer, PostgresType> OID_TO_TYPE = new HashMap<>();

    static {
        for (PostgresType type : PostgresType.values())
            OID_TO_TYPE.put(type.oid(), type);
    }

    public static PostgresType ofOid(int oid) {
        PostgresType type = OID_TO_TYPE.get(oid);
        return type == null ? UNSPECIFIED : type;
    }
}

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

/*
 * Borrowed from PostgreSQL JDBC driver
 */

package kr.co.bitnine.octopus.postgres.catalog;

public enum PostgresType
{
    UNSPECIFIED(0), // InvalidOid

    BOOL(16),
    BYTEA(17),
    CHAR(18),
    NAME(19),
    INT8(20),
    INT2(21),
    INT4(23),
    TEXT(25),
    OID(26),

    FLOAT4(700),
    FLOAT8(701),
    MONEY(790),
    MONEY_ARRAY(791),

    BOOL_ARRAY(1000),
    BYTEA_ARRAY(1001),
    NAME_ARRAY(1003),
    INT2_ARRAY(1005),
    INT4_ARRAY(1007),
    TEXT_ARRAY(1009),
    BPCHAR_ARRAY(1014),
    VARCHAR_ARRAY(1015),
    INT8_ARRAY(1016),
    FLOAT4_ARRAY(1021),
    FLOAT8_ARRAY(1022),
    OID_ARRAY(1028),
    BPCHAR(1042),
    VARCHAR(1043),
    DATE(1082),
    TIME(1083),

    TIMESTAMP(1114),
    TIMESTAMP_ARRAY(1115),
    DATE_ARRAY(1182),
    TIME_ARRAY(1183),
    TIMESTAMPTZ(1184),
    TIMESTAMPTZ_ARRAY(1185),
    INTERVAL(1186),
    INTERVAL_ARRAY(1187),

    NUMERIC_ARRAY(1231),
    TIMETZ(1266),
    TIMETZ_ARRAY(1270),

    BIT(1560),
    BIT_ARRAY(1561),

    NUMERIC(1700),

    VOID(2278);

    private final int oid;

    PostgresType(int oid)
    {
        this.oid = oid;
    }

    public int getOid()
    {
        return oid;
    }
}

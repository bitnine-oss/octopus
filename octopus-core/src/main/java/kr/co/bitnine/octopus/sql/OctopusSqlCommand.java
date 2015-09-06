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

package kr.co.bitnine.octopus.sql;

public abstract class OctopusSqlCommand
{
    public enum Type
    {
        ADD_DATASOURCE,
        CREATE_USER,
        ALTER_USER,
        DROP_USER,
        CREATE_ROLE,
        DROP_ROLE,
        GRANT_SYS_PRIVS,
        REVOKE_SYS_PRIVS,
        GRANT_OBJ_PRIVS,
        REVOKE_OBJ_PRIVS,
        SHOW_DATASOURCES,
        SHOW_SCHEMAS,
        SHOW_TABLES,
        SHOW_COLUMNS,
        SHOW_ALL_USERS,
        SHOW_OBJ_PRIVS_FOR,
        COMMENT_ON,
        SET_DATACATEGORY_ON,
        OTHER
    }

    public Type getType()
    {
        return Type.OTHER;
    }
}

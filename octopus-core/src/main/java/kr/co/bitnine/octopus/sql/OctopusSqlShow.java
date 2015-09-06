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

class OctopusSqlShow extends OctopusSqlCommand
{
    protected String dataSourceName;
    protected String schemaPattern;
    protected String tablePattern;
    protected String columnPattern;
    protected String userName;

    protected OctopusSqlShow(String dataSourceName, String schemaPattern, String tablePattern, String columnPattern, String userName)
    {
        this.dataSourceName = dataSourceName;
        this.schemaPattern = schemaPattern;
        this.tablePattern = tablePattern;
        this.columnPattern = columnPattern;
        this.userName = userName;
    }

    String getDataSourceName()
    {
        return dataSourceName;
    }

    String getSchemaPattern()
    {
        return schemaPattern;
    }

    String getTablePattern()
    {
        return tablePattern;
    }

    String getcolumnPattern()
    {
        return columnPattern;
    }

    String getUserName()
    {
        return userName;
    }

    static class DataSources extends OctopusSqlShow
    {
        DataSources()
        {
            super(null, null, null, null, null);
        }

        @Override
        public Type getType()
        {
            return Type.SHOW_DATASOURCES;
        }
    }

    static class Schemas extends OctopusSqlShow
    {
        Schemas(String dataSourceName, String schemaPattern)
        {
            super(dataSourceName, schemaPattern, null, null, null);
        }

        @Override
        public Type getType()
        {
            return Type.SHOW_SCHEMAS;
        }
    }

    static class Tables extends OctopusSqlShow
    {
        Tables(String dataSourceName, String schemaPattern, String tablePattern)
        {
            super(dataSourceName, schemaPattern, tablePattern, null, null);
        }

        @Override
        public Type getType()
        {
            return Type.SHOW_TABLES;
        }
    }

    static class Columns extends OctopusSqlShow
    {
        Columns(String dataSourceName, String schemaPattern, String tablePattern, String columnPattern)
        {
            super(dataSourceName, schemaPattern, tablePattern, columnPattern, null);
        }

        @Override
        public Type getType()
        {
            return Type.SHOW_COLUMNS;
        }
    }

    static class AllUsers extends OctopusSqlShow
    {
        AllUsers()
        {
            super(null, null, null, null, null);
        }

        @Override
        public Type getType()
        {
            return Type.SHOW_ALL_USERS;
        }
    }

    static class ObjPrivsFor extends OctopusSqlShow
    {
        ObjPrivsFor(String userName)
        {
            super(null, null, null, null, userName);
        }

        @Override
        public Type getType()
        {
            return Type.SHOW_OBJ_PRIVS_FOR;
        }
    }
}

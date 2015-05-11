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

package kr.co.bitnine.octopus.schema;

import java.util.Collection;
import java.util.HashMap;

public class Database {

    static public class DBConnInfo {
        /* for JDBC connection */

        protected String _driver;
        protected String _connectionString;

        public DBConnInfo(String driver, String connectionString) {
            _driver = driver;
            _connectionString = connectionString;
        }
    }

    private HashMap<String, Table> _tables = new HashMap<String, Table>();

    protected String _name;
    protected String _type;

    /* Connection information */
    protected DBConnInfo _dbc;

    public Database (String name)
    {
        _name = name;
    }

    /* for JDBC connection */
    public Database (String name, String type, DBConnInfo dbc)
    {
        _name = name;
        _type = type;
        _dbc = dbc;
    }

    public void insertTable(Table tbl) {
        _tables.put(tbl.getName(), tbl);
    }

    public void insertTable(String name, Table tbl) {
        _tables.put(name, tbl);
    }

    public String getName() {
        return _name;
    }

    public Collection<Table> getTables() {
        return _tables.values();
    }
}

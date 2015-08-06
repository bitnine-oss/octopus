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

package kr.co.bitnine.octopus.queryengine;

import kr.co.bitnine.octopus.schema.SchemaManager;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;

import java.util.ArrayList;
import java.util.List;

public final class TableNameTranslator
{
    // translate FQN(Fully Qualified Name) to DSN(DataSource Name)
    public static void toDSN(SqlNode query)
    {
        ArrayList<SqlIdentifier> tableIds = new ArrayList<>();
        query.accept(new SqlTableIdentifierFindVisitor(tableIds));

        for (SqlIdentifier tableID : tableIds) {
            List<String> dsn = new ArrayList<>();
            dsn.add(tableID.names.get(2));
            tableID.setNames(dsn, null);
        }
    }

    // translate DSN to FQN
    public static void toFQN(SchemaManager schemaManager, SqlNode query)
    {
        ArrayList<SqlIdentifier> tableIds = new ArrayList<>();
        query.accept(new SqlTableIdentifierFindVisitor(tableIds));

        for (SqlIdentifier tableId : tableIds) {
            List<String> fqn = schemaManager.toFullyQualifiedTableName(tableId.names);
            tableId.setNames(fqn, null);
        }
    }
}

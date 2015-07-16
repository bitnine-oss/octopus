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

import kr.co.bitnine.octopus.schema.MetaStore;
import kr.co.bitnine.octopus.schema.model.MTable;
import org.apache.calcite.sql.*;

import java.util.ArrayList;
import java.util.List;

public class TableNameTranslator
{
    MetaStore metaStore;

    public TableNameTranslator(MetaStore metastore)
    {
       this.metaStore = metastore;
    }

    /* translate FQN(Fully Qualified Name) to DSN(DataSource Name) */
    public void toDSN(SqlNode query)
    {
        ArrayList<SqlIdentifier> tableIDs = new ArrayList();
        query.accept(new SqlTableIdentifierFindVisitor(tableIDs));

        for (SqlIdentifier tableID : tableIDs) {
            List<String> newName = new ArrayList();
            newName.add(tableID.names.get(2));
            tableID.setNames(newName, null);
        }
    }

    /* translate DSN to FQN */
    public void toFQN(SqlNode query)
    {
        ArrayList<SqlIdentifier> tableIDs = new ArrayList();
        query.accept(new SqlTableIdentifierFindVisitor(tableIDs));

        for (SqlIdentifier tableID : tableIDs) {
            MTable mtable = metaStore.getTable(tableID);
            List<String> newName = new ArrayList();
            newName.add(mtable.getSchema().getDatasource().getName());
            newName.add(mtable.getSchema().getName());
            newName.add(mtable.getName());
            tableID.setNames(newName, null);
        }
    }
}

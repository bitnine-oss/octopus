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

package kr.co.bitnine.octopus.schema.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PersistenceCapable
public class MTable
{
    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
    long ID;

    String name;
    int type;
    String description;
    String schema_name;
    MSchema schema;

    @Persistent(mappedBy="table")
    Collection<MColumn> columns;

    public MTable(String name, int type, String description, MSchema schema)
    {
        this.name = name;
        this.type = type;
        this.description = description;
        this.schema = schema;
    }

    public int getColumnCnt()
    {
        return columns.size();
    }

    public List<MColumn> getColumns()
    {
        return new ArrayList(columns);
    }

    public String getName()
    {
        return name;
    }

    public MSchema getSchema() {
        return schema;
    }
}

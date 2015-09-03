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

package kr.co.bitnine.octopus.meta.jdo.model;

import kr.co.bitnine.octopus.meta.model.MetaColumn;
import kr.co.bitnine.octopus.meta.model.MetaSchema;
import kr.co.bitnine.octopus.meta.model.MetaTable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.ArrayList;
import java.util.Collection;

@PersistenceCapable
public class MTable implements MetaTable
{
    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
    private long ID;

    private String name;
    private String type;
    private MSchema schema;
    private String comment;

    @Persistent(mappedBy="table")
    private Collection<MColumn> columns;

    public MTable(String name, String type, MSchema schema)
    {
        this.name = name;
        this.type = type;
        this.schema = schema;
        comment = "";
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getType()
    {
        return type;
    }

    @Override
    public MetaSchema getSchema()
    {
        return schema;
    }

    @Override
    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    @Override
    public Collection<MetaColumn> getColumns()
    {
        return new ArrayList<MetaColumn>(columns);
    }
}

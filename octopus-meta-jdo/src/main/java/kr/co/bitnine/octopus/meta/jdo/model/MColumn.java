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
import kr.co.bitnine.octopus.meta.model.MetaTable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class MColumn implements MetaColumn
{
    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
    long ID;
    String name;
    int type;
    String description;
    MTable table;
    int dataCategory;

    public MColumn(String name, int type, String description, int dataCategory, MTable table)
    {
        this.name = name;
        this.type = type;
        this.description = description;
        this.table = table;
        this.dataCategory = dataCategory;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getType()
    {
        return type;
    }

    @Override
    public MetaTable getTable()
    {
        return table;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public int getDataCategory()
    {
        return dataCategory;
    }

    @Override
    public void setDataCategory(int dataCategory)
    {
        this.dataCategory = dataCategory;
    }
}

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

import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.meta.model.MetaSchema;
import kr.co.bitnine.octopus.meta.model.MetaTable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.ArrayList;
import java.util.Collection;

@PersistenceCapable
public class MSchema implements MetaSchema
{
    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
    long ID;

    String name;
    String comment;
    MDataSource dataSource;

    @Persistent(mappedBy="schema")
    Collection<MTable> tables;

    public MSchema(String name, MDataSource dataSource)
    {
        this.name = name;
        this.dataSource = dataSource;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public Collection<MetaTable> getTables()
    {
        return new ArrayList(tables);
    }

    @Override
    public MetaDataSource getDataSource()
    {
        return dataSource;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }
}

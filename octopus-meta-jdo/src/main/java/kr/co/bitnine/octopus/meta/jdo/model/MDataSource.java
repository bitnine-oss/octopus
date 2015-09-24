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

import javax.jdo.annotations.*;
import java.util.ArrayList;
import java.util.Collection;

@PersistenceCapable
public class MDataSource implements MetaDataSource
{
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
    private long ID;

    @Persistent
    @Column(length = 128)
    private String name;

    private int type;

    @Persistent
    @Column(length = 128)
    private String jdbcDriverName;

    @Persistent
    @Column(length = 256)
    private String jdbcConnectionString;

    @Persistent
    @Column(length = 1024)
    private String comment;

    @Persistent(mappedBy = "dataSource")
    private Collection<MSchema> schemas;

    public MDataSource(String name, int type, String jdbcDriverName, String jdbcConnectionString)
    {
        this.name = name;
        this.type = type;
        this.jdbcDriverName = jdbcDriverName;
        this.jdbcConnectionString = jdbcConnectionString;
        comment = "";
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getDriverName()
    {
        return jdbcDriverName;
    }

    @Override
    public String getConnectionString()
    {
        return jdbcConnectionString;
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
    public Collection<MetaSchema> getSchemas()
    {
        return new ArrayList<MetaSchema>(schemas);
    }

    public void setSchemas(Collection<MSchema> newSchemas)
    {
        schemas = newSchemas;
    }
}

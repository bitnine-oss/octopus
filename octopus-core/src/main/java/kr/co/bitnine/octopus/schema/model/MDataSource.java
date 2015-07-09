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
import java.util.Collection;

@PersistenceCapable
public class MDataSource {
    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
    long ID;

    String name;
    int type;
    String jdbc_driver;
    String jdbc_connectionString;
    String description;

    @Persistent(mappedBy="datasource")
    Collection<MSchema> schemas;

    public MDataSource(String name, int type, String jdbc_driver, String jdbc_connectionString, String description)
    {
        this.name = name;
        this.type = type;
        this.jdbc_driver = jdbc_driver;
        this.jdbc_connectionString = jdbc_connectionString;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public Collection<MSchema> getSchemas()
    {
        return schemas;
    }

    public String getDriver()
    {
        return jdbc_driver;
    }

    public String getConnectionString()
    {
        return jdbc_connectionString;
    }
}

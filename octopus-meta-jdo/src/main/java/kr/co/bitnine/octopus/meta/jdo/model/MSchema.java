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

import kr.co.bitnine.octopus.meta.model.MetaConstants;
import kr.co.bitnine.octopus.meta.model.MetaDataSource;
import kr.co.bitnine.octopus.meta.model.MetaSchema;
import kr.co.bitnine.octopus.meta.model.MetaTable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.ArrayList;
import java.util.Collection;

@PersistenceCapable
public final class MSchema implements MetaSchema {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
    private long id;

    @Persistent
    @Column(length = MetaConstants.IDENTIFIER_MAX)
    private String name;

    @Persistent(dependent = "false")
    private MDataSource dataSource;

    @Persistent
    @Column(length = MetaConstants.COMMENT_MAX)
    private String comment;

    @Persistent(mappedBy = "schema", dependentElement = "true")
    private Collection<MTable> tables;

    @Persistent(mappedBy = "schema", dependentElement = "true")
    private Collection<MSchemaPrivilege> schemaPrivileges;

    public MSchema(String name, MDataSource dataSource) {
        this.name = name;
        this.dataSource = dataSource;
        comment = "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MetaDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public Collection<MetaTable> getTables() {
        return new ArrayList<MetaTable>(tables);
    }
}

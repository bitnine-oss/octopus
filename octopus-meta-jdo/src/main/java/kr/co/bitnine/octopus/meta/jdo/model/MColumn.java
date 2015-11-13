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
import kr.co.bitnine.octopus.meta.model.MetaConstants;
import kr.co.bitnine.octopus.meta.model.MetaTable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public final class MColumn implements MetaColumn {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
    private long id;

    @Persistent
    @Column(length = MetaConstants.IDENTIFIER_MAX)
    private String name;

    private int type;
    private int typeInfo;

    @Persistent(dependent = "false")
    private MTable table;

    @Persistent
    @Column(length = MetaConstants.COMMENT_MAX)
    private String comment;

    @Persistent
    @Column(length = MetaConstants.DATACATEGORY_MAX)
    private String dataCategory;

    public MColumn(String name, int type, int typeInfo, MTable table) {
        this.name = name;
        this.type = type;
        this.typeInfo = typeInfo;
        this.table = table;
        comment = "";
        dataCategory = "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getTypeInfo() {
        return typeInfo;
    }

    @Override
    public MetaTable getTable() {
        return table;
    }

    @Override
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getDataCategory() {
        return dataCategory;
    }

    public void setDataCategory(String dataCategory) {
        this.dataCategory = dataCategory;
    }
}

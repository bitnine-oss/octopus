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

import kr.co.bitnine.octopus.meta.model.MetaSchema;
import kr.co.bitnine.octopus.meta.model.MetaSchemaPrivilege;
import kr.co.bitnine.octopus.meta.privilege.ObjectPrivilege;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;
import java.util.HashSet;
import java.util.Set;

@PersistenceCapable
@Unique(name = "SCHEMA_USER_IDX", members = {"schema", "user"})
public final class MSchemaPrivilege implements MetaSchemaPrivilege {
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.INCREMENT)
    private long id;

    private Set<ObjectPrivilege> objPrivs;

    @Column(name = "MSCHEMA_ID")
    private MSchema schema;

    @Column(name = "MUSER_ID")
    private MUser user;

    public MSchemaPrivilege(MSchema schema, MUser user) {
        objPrivs = new HashSet<>();
        this.schema = schema;
        this.user = user;
    }

    @Override
    public MetaSchema getSchema() {
        return schema;
    }

    @Override
    public Set<ObjectPrivilege> getObjectPrivileges() {
        return new HashSet<>(objPrivs);
    }

    public boolean addObjectPrivilege(ObjectPrivilege objPriv) {
        return objPrivs.add(objPriv);
    }

    public boolean removeObjectPrivilege(ObjectPrivilege objPriv) {
        return objPrivs.remove(objPriv);
    }

    public boolean isEmpty() {
        return objPrivs.isEmpty();
    }
}

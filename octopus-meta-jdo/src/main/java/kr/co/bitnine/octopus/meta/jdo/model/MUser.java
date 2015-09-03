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

import kr.co.bitnine.octopus.meta.model.MetaUser;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;

import javax.jdo.annotations.*;
import java.util.HashSet;
import java.util.Set;

@PersistenceCapable
public class MUser implements MetaUser
{
    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.INCREMENT)
    private long id;

    @Persistent
    @Unique(name="NAME_IDX")
    private String name;

    private String password;
    private Set<SystemPrivilege> sysPrivs;
    private String comment;

    public MUser(String name, String password)
    {
        this.name = name;
        this.password = password;
        sysPrivs = new HashSet<>();
        comment = "";
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public Set<SystemPrivilege> getSystemPrivileges()
    {
        return new HashSet<>(sysPrivs);
    }

    public boolean addSystemPrivilege(SystemPrivilege sysPriv)
    {
        return sysPrivs.add(sysPriv);
    }

    public boolean removeSystemPrivilege(SystemPrivilege sysPriv)
    {
        return sysPrivs.remove(sysPriv);
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
}

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

package kr.co.bitnine.octopus.meta;

import kr.co.bitnine.octopus.meta.model.MetaUser;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;

import java.lang.reflect.Constructor;

public final class MetaStores
{
    private MetaStores() { }

    public static MetaStore newInstance(String className) throws ReflectiveOperationException
    {
        Class<?> clazz = Class.forName(className);
        Constructor<?> ctor = clazz.getConstructor();
        return (MetaStore) ctor.newInstance();
    }

    public static void initialize(MetaStore metaStore) throws MetaException
    {
        MetaContext mc = metaStore.getMetaContext();

        if (mc.userExists("octopus"))
            return;

        MetaUser user = mc.createUser("octopus", "bitnine");

        mc.addSystemPrivilege(SystemPrivilege.ALTER_SYSTEM, user.getName());
        mc.addSystemPrivilege(SystemPrivilege.SELECT_ANY_TABLE, user.getName());
        mc.addSystemPrivilege(SystemPrivilege.CREATE_USER, user.getName());
        mc.addSystemPrivilege(SystemPrivilege.ALTER_USER, user.getName());
        mc.addSystemPrivilege(SystemPrivilege.DROP_USER, user.getName());
        mc.addSystemPrivilege(SystemPrivilege.COMMENT_ANY, user.getName());
        mc.addSystemPrivilege(SystemPrivilege.GRANT_ANY_OBJECT_PRIVILEGE, user.getName());
        mc.addSystemPrivilege(SystemPrivilege.GRANT_ANY_PRIVILEGE, user.getName());

        mc.close();
    }
}

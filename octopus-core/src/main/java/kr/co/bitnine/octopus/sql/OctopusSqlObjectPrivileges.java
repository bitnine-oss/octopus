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

package kr.co.bitnine.octopus.sql;

import kr.co.bitnine.octopus.meta.privilege.ObjectPrivilege;

import java.util.List;

abstract class OctopusSqlObjectPrivileges extends OctopusSqlCommand
{
    private final List<ObjectPrivilege> objPrivs;
    private final String objName;
    private final List<String> grantees;

    OctopusSqlObjectPrivileges(List<ObjectPrivilege> objPrivs, String objName, List<String> grantees)
    {
        this.objPrivs = objPrivs;
        this.objName = objName;
        this.grantees = grantees;
    }

    List<ObjectPrivilege> getObjPrivs()
    {
        return objPrivs;
    }

    String getObjName()
    {
        return objName;
    }

    List<String> getGrantees()
    {
        return grantees;
    }

    static class OctopusSqlGrantObjPrivs extends OctopusSqlObjectPrivileges
    {
        OctopusSqlGrantObjPrivs(List<ObjectPrivilege> objPrivs, String objName, List<String> grantees)
        {
            super(objPrivs, objName, grantees);
        }

        @Override
        public Type getType()
        {
            return Type.GRANT_OBJ_PRIVS;
        }
    }

    static class OctopusSqlRevokeObjPrivs extends OctopusSqlObjectPrivileges
    {
        OctopusSqlRevokeObjPrivs(List<ObjectPrivilege> objPrivs, String objName, List<String> revokees)
        {
            super(objPrivs, objName, revokees);
        }

        @Override
        public Type getType()
        {
            return Type.REVOKE_OBJ_PRIVS;
        }
    }
}

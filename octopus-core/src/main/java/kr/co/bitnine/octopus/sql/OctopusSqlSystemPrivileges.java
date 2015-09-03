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

import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;

import java.util.List;

abstract class OctopusSqlSystemPrivileges extends OctopusSqlCommand
{
    private final List<SystemPrivilege> sysPrivs;
    private final List<String> grantees;

    OctopusSqlSystemPrivileges(List<SystemPrivilege> sysPrivs, List<String> grantees)
    {
        this.sysPrivs = sysPrivs;
        this.grantees = grantees;
    }

    List<SystemPrivilege> getSysPrivs()
    {
        return sysPrivs;
    }

    List<String> getGrantees()
    {
        return grantees;
    }

    static class OctopusSqlGrantSysPrivs extends OctopusSqlSystemPrivileges
    {
        OctopusSqlGrantSysPrivs(List<SystemPrivilege> sysPrivs, List<String> grantees)
        {
            super(sysPrivs, grantees);
        }

        @Override
        public Type getType()
        {
            return Type.GRANT_SYS_PRIVS;
        }
    }

    static class OctopusSqlRevokeSysPrivs extends OctopusSqlSystemPrivileges
    {
        OctopusSqlRevokeSysPrivs(List<SystemPrivilege> sysPrivs, List<String> revokees)
        {
            super(sysPrivs, revokees);
        }

        @Override
        public Type getType()
        {
            return Type.REVOKE_SYS_PRIVS;
        }
    }
}

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
        OctopusSqlRevokeSysPrivs(List<SystemPrivilege> sysPrivs, List<String> grantees)
        {
            super(sysPrivs, grantees);
        }

        @Override
        public Type getType()
        {
            return Type.REVOKE_SYS_PRIVS;
        }
    }
}

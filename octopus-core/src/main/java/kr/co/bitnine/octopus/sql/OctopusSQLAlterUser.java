package kr.co.bitnine.octopus.sql;

/**
 * Created by Kimbyungmoon on 15. 7. 20..
 */
public class OctopusSqlAlterUser extends OctopusSqlCommand{
    private String name;
    private String password;
    private String old_password;

    OctopusSqlAlterUser(String name, String password, String old_password)
    {
        this.name = name;
        this.password = password;
        this.old_password = old_password;
    }

    String getName()
    {
        return name;
    }

    String getPassword()
    {
        return password;
    }

    String getOld_password()
    {
        return this.old_password;
    }

    @Override
    public OctopusSqlCommand.Type getType()
    {
        return OctopusSqlCommand.Type.ALTER_USER;
    }
}

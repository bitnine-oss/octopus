package kr.co.bitnine.octopus.schema.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class MColumnACL {
    @PrimaryKey
    @Persistent(valueStrategy= IdGeneratorStrategy.INCREMENT)
    long ID;

    @Persistent
    long columnID;

    @Persistent
    long grantorUserID;

    @Persistent
    long userID; /* grantee */

    @Persistent
    int grantOption;

    @Persistent
    int privilege;

    @Persistent
    long roleID;
}

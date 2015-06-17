package kr.co.bitnine.octopus.schema.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class MUser {
    @PrimaryKey
    @Persistent(valueStrategy= IdGeneratorStrategy.INCREMENT)
    long id;

    String name;
    String password;
}

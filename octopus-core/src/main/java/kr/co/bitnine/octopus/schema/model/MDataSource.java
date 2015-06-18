package kr.co.bitnine.octopus.schema.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.Collection;

@PersistenceCapable
public class MDataSource {
    @PrimaryKey
    @Persistent(valueStrategy= IdGeneratorStrategy.INCREMENT)
    long ID;

    String name;
    int type;
    String jdbc_driver;
    String jdbc_connectionString;
    String description;

    @Persistent(mappedBy = "datasource")
    Collection<MTable> tables;

    public MDataSource(String name, int type, String jdbc_driver, String jdbc_connectionString, String description)
    {
        this.name = name;
        this.type = type;
        this.jdbc_driver = jdbc_driver;
        this.jdbc_connectionString = jdbc_connectionString;
        this.description = description;
    }
}

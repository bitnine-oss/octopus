package kr.co.bitnine.octopus.schema.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class MColumn {
    @PrimaryKey
    @Persistent(valueStrategy= IdGeneratorStrategy.INCREMENT)
    long ID;
    String name;
    int type;
    String description;
    MTable table;

    public MColumn(String name, int type, String description, MTable table)
    {
        this.name = name;
        this.type = type;
        this.description = description;
        this.table = table;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }
}

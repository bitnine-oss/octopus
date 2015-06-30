package kr.co.bitnine.octopus.schema.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PersistenceCapable
public class MSchema {
    @PrimaryKey
    @Persistent(valueStrategy= IdGeneratorStrategy.INCREMENT)
    long ID;

    String name;
    MDataSource datasource;

    @Persistent(mappedBy = "schema")
    Collection<MTable> tables;

    public MSchema(String name, MDataSource datasource) {
        this.name = name;
        this.datasource = datasource;
    }

    public List<MTable> getTables() {
        return new ArrayList<MTable>(tables);
    }

    public String getName() {
        return name;
    }
}

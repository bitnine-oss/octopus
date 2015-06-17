package kr.co.bitnine.octopus.schema.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class MTableAlias {
    @PrimaryKey
    @Persistent(valueStrategy= IdGeneratorStrategy.INCREMENT)
    long ID;

    MTable table;
    String alias;

    public MTableAlias(MTable table, String alias) {
        this.table = table;
        this.alias = alias;
    }
}

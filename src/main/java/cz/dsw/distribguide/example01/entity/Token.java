package cz.dsw.distribguide.example01.entity;

import java.io.Serializable;
import java.util.Date;

public abstract class Token implements Serializable {

    private String name;
    private Date ts;

    public Token(String name, Date ts) {
        this.name = name;
        this.ts = ts;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Date getTs() {
        return ts;
    }
    public void setTs(Date ts) {
        this.ts = ts;
    }
    @Override
    public String toString() {
        return "Token{" + "name='" + name + '\'' + ", ts=" + ts + '}';
    }
}

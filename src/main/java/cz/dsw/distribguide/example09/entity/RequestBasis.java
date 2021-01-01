package cz.dsw.distribguide.example09.entity;

import java.util.Date;

public abstract class RequestBasis extends Token {

    public RequestBasis(String name, Date ts) {
        super(name, ts);
    }

    @Override
    public String toString() { return "RequestBasis{" + super.toString() + "}"; }
}

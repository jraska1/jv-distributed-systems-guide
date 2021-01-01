package cz.dsw.distribguide.example09.entity;

import java.util.Date;

public class RequestA extends RequestBasis {

    private long value;

    public RequestA(String name, Date ts, long value) {
        super(name, ts);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RequestA{value=" + value + ", " + super.toString() + "}";
    }
}

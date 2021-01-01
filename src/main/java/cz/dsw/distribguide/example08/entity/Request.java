package cz.dsw.distribguide.example08.entity;

import java.util.Date;

public class Request extends Token {

    private long value;

    public Request(String name, Date ts, long value) {
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
        return "Request{value=" + value + ", " + super.toString() + "}";
    }
}

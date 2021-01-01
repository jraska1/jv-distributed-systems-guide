package cz.dsw.distribguide.example09.entity;

import java.util.Date;

public class ResponseA extends ResponseBasis {

    private long result;

    public ResponseA(String name, Date ts, ResponseCodeType code, long result) {
        super(name, ts, code);
        this.result = result;
    }

    public long getResult() {
        return result;
    }

    public void setResult(long result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ResponseA{result=" + result + ", " + super.toString() + "}";
    }
}

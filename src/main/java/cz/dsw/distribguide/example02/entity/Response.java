package cz.dsw.distribguide.example02.entity;

import java.util.Date;

public class Response extends Token {

    private long result;

    public Response(String name, Date ts, long result) {
        super(name, ts);
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
        return "Response{result=" + result + ", " + super.toString() + "}";
    }
}

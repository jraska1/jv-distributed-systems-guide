package cz.dsw.distribguide.example09.entity;

import java.util.Date;

public class ResponseBasis extends Token {

    private ResponseCodeType code;

    public ResponseBasis(String name, Date ts, ResponseCodeType code) {
        super(name, ts);
        this.code = code;
    }

    public ResponseCodeType getCode() { return code; }

    public void setCode(ResponseCodeType code) { this.code = code; }

    @Override
    public String toString() {
        return "ResponseBasis{code=" + code + ", " + super.toString() + "}";
    }
}

package cz.dsw.distribguide.example09.entity;

import java.util.Date;
import java.util.Objects;

public class ResponseB extends ResponseBasis {

    private String text;

    public ResponseB(String name, Date ts, ResponseCodeType code, String text) {
        super(name, ts, code);
        this.text = text;
    }

    public String getText() { return Objects.toString(text); }

    public void setText(String text) { this.text = text; }

    @Override
    public String toString() {
        return "ResponseB{text=" + text + ", " + super.toString() + "}";
    }
}

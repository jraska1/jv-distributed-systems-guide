package cz.dsw.distribguide.example09.entity;

import java.util.Date;
import java.util.Objects;

public class RequestB extends RequestBasis {

    private String text;

    public RequestB(String name, Date ts, String text) {
        super(name, ts);
        this.text = text;
    }

    public String getText() { return Objects.toString(text); }

    public void setText(String text) { this.text = text; }

    @Override
    public String toString() {
        return "RequestB{text=" + text + ", " + super.toString() + "}";
    }
}

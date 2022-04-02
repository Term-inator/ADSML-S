package com.ecnu.adsmls.utils.register;

import com.ecnu.adsmls.utils.Geometry;

import java.util.Map;

public class Between implements Requirement {
    private Object l;
    private Object r;
    private String op;

    public Between(Object l, Object r, String op) {
        this.l = l;
        this.r = r;
        this.op = op;
    }

    @Override
    public boolean check(Map<String, String> context, String value) {
        Double lVal, rVal;
        if(this.l instanceof Value) {
            lVal = ((Value) l).getValue().doubleValue();
        }
        else {
            lVal = Double.parseDouble(context.get(((Reference) l).getId()));
        }

        if(this.r instanceof Value) {
            rVal = (Double) ((Value) r).getValue();
        }
        else {
            rVal = Double.parseDouble(context.get(((Reference) r).getId()));
        }
        return Geometry.between(Double.parseDouble(value), lVal, rVal, this.op);
    }
}

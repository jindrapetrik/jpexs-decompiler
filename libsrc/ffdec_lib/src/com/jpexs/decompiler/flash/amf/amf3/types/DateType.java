package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.Amf3Tools;
import java.util.Date;

public class DateType {

    private double val;

    public DateType(double val) {
        this.val = val;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return Amf3Tools.amfToString(this);
    }

    public Date toDate() {
        return new Date((long) val);
    }
}

package com.jpexs.decompiler.flash.amf.amf3;

import com.jpexs.decompiler.flash.amf.amf3.types.Amf3ValueType;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;

public class Amf3Value {

    private Object value;

    public Amf3Value() {
        setValue(null);
    }

    public Amf3Value(Object value) {
        setValue(value);
    }

    public void setValue(Object value) {
        if (!isValueValid(value)) {
            throw new IllegalArgumentException("Invalid Amf value: " + value.getClass().getSimpleName());
        }
        this.value = value;
    }

    public static boolean isValueValid(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof Long) {
            return true;
        }
        if (value instanceof Double) {
            return true;
        }
        if (value instanceof String) {
            return true;
        }
        if (value instanceof Boolean) {
            return true;
        }
        if (value instanceof Amf3ValueType) {
            return true;
        }
        return false;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "";
        }
        return Amf3Exporter.amfToString(value);
    }
}

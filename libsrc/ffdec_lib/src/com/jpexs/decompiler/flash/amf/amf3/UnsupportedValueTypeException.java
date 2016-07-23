package com.jpexs.decompiler.flash.amf.amf3;

public class UnsupportedValueTypeException extends RuntimeException {

    private Integer marker = null;
    private Class cls = null;

    public UnsupportedValueTypeException(Class cls) {
        super("Unsupported type of value - class: " + cls.getSimpleName());
        this.cls = cls;
    }

    public UnsupportedValueTypeException(int marker) {
        super("Unsupported type of value - marker: 0x" + Integer.toHexString(marker));
        this.marker = marker;
    }

    public Integer getMarker() {
        return marker;
    }

    public Class getUnsupportedClass() {
        return this.cls;
    }

}

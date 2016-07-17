package com.jpexs.decompiler.flash.amf.amf3;

public class UnsupportedValueType extends RuntimeException {

    private int marker;

    public UnsupportedValueType(int marker) {
        super("Unsupported type of value - marker: 0x" + Integer.toHexString(marker));
        this.marker = marker;
    }

    public int getMarker() {
        return marker;
    }

}

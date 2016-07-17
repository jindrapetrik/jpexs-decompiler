package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.AMF3Tools;

public class ByteArrayType {

    private byte[] data;

    public ByteArrayType(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return AMF3Tools.amfToString(this);
    }

}

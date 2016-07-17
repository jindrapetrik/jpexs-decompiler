package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.Amf3Tools_;

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
        return Amf3Tools_.amfToString(this);
    }

}

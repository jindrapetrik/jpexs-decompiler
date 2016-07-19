package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;

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
        return Amf3Exporter.amfToString(this);
    }

}

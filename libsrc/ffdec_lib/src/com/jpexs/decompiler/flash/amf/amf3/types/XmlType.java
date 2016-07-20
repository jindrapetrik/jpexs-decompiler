package com.jpexs.decompiler.flash.amf.amf3.types;

public class XmlType implements Amf3ValueType {

    private String data;

    public XmlType(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return data;
    }

}

package com.jpexs.decompiler.flash.amf.amf3.types;

public class XmlDocType {

    private String data;

    public XmlDocType(String data) {
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

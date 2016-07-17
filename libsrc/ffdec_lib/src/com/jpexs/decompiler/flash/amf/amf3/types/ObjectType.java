package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.Amf3Tools_;
import com.jpexs.decompiler.flash.amf.amf3.Pair;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;

public class ObjectType implements WithSubValues {

    private boolean dynamic;
    private List<Pair<String, Object>> sealedMembers;
    private List<Pair<String, Object>> dynamicMembers;
    private List<Pair<String, Object>> serializedMembers;
    private String className;
    //null = not serialized or unknown
    private byte[] serializedData = null;
    private boolean serialized;

    public boolean isSerialized() {
        return serialized;
    }

    public ObjectType(String className, byte[] serializedData, List<Pair<String, Object>> serializedMembers) {
        this.className = className;
        this.serializedData = serializedData;
        this.serializedMembers = serializedMembers;
        this.dynamicMembers = new ArrayList<>();
        this.sealedMembers = new ArrayList<>();
        this.serialized = true;
    }

    public ObjectType(boolean dynamic, List<Pair<String, Object>> sealedMembers, List<Pair<String, Object>> dynamicMembers, String className) {
        this.dynamic = dynamic;
        this.sealedMembers = sealedMembers;
        this.dynamicMembers = dynamicMembers;
        this.className = className;
        this.serializedMembers = new ArrayList<>();
        this.serialized = false;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public List<Pair<String, Object>> getDynamicMembers() {
        return dynamicMembers;
    }

    public List<Pair<String, Object>> getSealedMembers() {
        return sealedMembers;
    }

    public String getClassName() {
        return className;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public void setDynamicMembers(List<Pair<String, Object>> dynamicMembers) {
        this.dynamicMembers = dynamicMembers;
    }

    public void setSealedMembers(List<Pair<String, Object>> sealedMembers) {
        this.sealedMembers = sealedMembers;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public List<Object> getSubValues() {
        List<Object> ret = new ArrayList<>();
        for (Pair<String, Object> p : dynamicMembers) {
            ret.add(p.getFirst());
            ret.add(p.getSecond());
        }
        for (Pair<String, Object> p : sealedMembers) {
            ret.add(p.getFirst());
            ret.add(p.getSecond());
        }

        for (Pair<String, Object> p : serializedMembers) {
            ret.add(p.getFirst());
            ret.add(p.getSecond());
        }

        return ret;
    }

    @Override
    public String toString() {
        return Amf3Tools_.amfToString(this);
    }

    public void setSerializedData(byte[] serializedData) {
        this.serializedData = serializedData;
    }

    public byte[] getSerializedData() {
        return serializedData;
    }

    public void setSerializedMembers(List<Pair<String, Object>> serializedMembers) {
        this.serializedMembers = serializedMembers;
    }

    public List<Pair<String, Object>> getSerializedMembers() {
        return serializedMembers;
    }

}

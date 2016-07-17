package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.Amf3Tools;
import com.jpexs.decompiler.flash.amf.amf3.Pair;
import com.jpexs.decompiler.flash.amf.amf3.Traits;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;

public class ObjectType implements WithSubValues {

    private List<Pair<String, Object>> sealedMembers;
    private List<Pair<String, Object>> dynamicMembers;
    private List<Pair<String, Object>> serializedMembers;
    //null = not serialized or unknown
    private byte[] serializedData = null;
    private boolean serialized;
    private Traits traits;

    public boolean isSerialized() {
        return serialized;
    }

    public Traits getTraits() {
        return traits;
    }

    public ObjectType(Traits traits, byte[] serializedData, List<Pair<String, Object>> serializedMembers) {
        this.traits = traits;
        this.serializedData = serializedData;
        this.serializedMembers = serializedMembers;
        this.dynamicMembers = new ArrayList<>();
        this.sealedMembers = new ArrayList<>();
        this.serialized = true;
    }

    public ObjectType(Traits traits, List<Pair<String, Object>> sealedMembers, List<Pair<String, Object>> dynamicMembers) {
        this.sealedMembers = sealedMembers;
        this.dynamicMembers = dynamicMembers;
        this.serializedMembers = new ArrayList<>();
        this.serialized = false;
        this.traits = traits;
    }

    public boolean isDynamic() {
        return traits.isDynamic();
    }

    public List<Pair<String, Object>> getDynamicMembers() {
        return dynamicMembers;
    }

    public List<Pair<String, Object>> getSealedMembers() {
        return sealedMembers;
    }

    public String getClassName() {
        return traits.getClassName();
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
        return Amf3Tools.amfToString(this);
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

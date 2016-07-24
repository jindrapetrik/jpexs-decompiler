package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.ListMap;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import java.util.ArrayList;
import java.util.List;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ArrayType implements WithSubValues, Amf3ValueType {

    private List<Object> denseValues;
    private Map<String, Object> associativeValues;

    public ArrayType(Map<? extends String, ? extends Object> associativeValues) {
        this(new ArrayList<>(), associativeValues);
    }

    public ArrayType(List<Object> denseValues) {
        this(denseValues, new HashMap<>());
    }

    public ArrayType() {
        this(new ArrayList<>(), new HashMap<>());
    }

    public ArrayType(List<Object> denseValues, Map<? extends String, ? extends Object> associativeValues) {
        this.denseValues = new ArrayList<>(denseValues);
        this.associativeValues = new ListMap<>(associativeValues);
    }

    public List<Object> getDenseValues() {
        return new ArrayList<>(denseValues);
    }

    public void setDenseValues(List<Object> denseValues) {
        this.denseValues = new ArrayList<>(denseValues);
    }

    public Object setDense(int key, Object value) {
        return denseValues.set(key, value);
    }

    public Object putAssociative(String key, Object value) {
        return associativeValues.put(key, value);
    }

    public Map<String, Object> getAssociativeValues() {
        return new ListMap<>(associativeValues);
    }

    public Object getAssociative(String key) {
        return associativeValues.get(key);
    }

    public Object getDense(int index) {
        if (index >= 0 && index < denseValues.size()) {
            return denseValues.get(index);
        }
        return null;
    }

    public Set<String> associativeKeySet() {
        return associativeValues.keySet();
    }

    @Override
    public String toString() {
        return Amf3Exporter.amfToString(this);
    }

    @Override
    public List<Object> getSubValues() {
        List<Object> ret = new ArrayList<>();
        ret.addAll(associativeValues.keySet());
        ret.addAll(associativeValues.values());
        ret.addAll(denseValues);
        return ret;
    }

    public void setAssociativeValues(Map<String, Object> associativeValues) {
        this.associativeValues = new ListMap<>(associativeValues);
    }

}

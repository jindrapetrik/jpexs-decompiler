package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import com.jpexs.decompiler.flash.amf.amf3.Pair;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;

public class ArrayType implements WithSubValues {

    private List<Object> denseValues;
    private List<Pair<String, Object>> associativeValues;

    public ArrayType(List<Object> denseValues, List<Pair<String, Object>> associativeValues) {
        this.denseValues = denseValues;
        this.associativeValues = associativeValues;
    }

    public List<Object> getDenseValues() {
        return denseValues;
    }

    public List<Pair<String, Object>> getAssociativeValues() {
        return associativeValues;
    }

    @Override
    public String toString() {
        return Amf3Exporter.amfToString(this);
    }

    @Override
    public List<Object> getSubValues() {
        List<Object> ret = new ArrayList<>();
        for (Pair<String, Object> p : associativeValues) {
            ret.add(p.getFirst());
            ret.add(p.getSecond());
        }
        for (Object v : denseValues) {
            ret.add(v);
        }
        return ret;
    }

}

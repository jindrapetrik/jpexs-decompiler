package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.AMF3Tools;
import com.jpexs.decompiler.flash.amf.amf3.Pair;
import java.util.ArrayList;
import java.util.List;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;

public class DictionaryType implements WithSubValues {

    private boolean weakKeys;
    private List<Pair<Object, Object>> pairs;

    public DictionaryType(boolean weakKeys, List<Pair<Object, Object>> pairs) {
        this.weakKeys = weakKeys;
        this.pairs = pairs;
    }

    public List<Pair<Object, Object>> getPairs() {
        return pairs;
    }

    @Override
    public List<Object> getSubValues() {
        List<Object> ret = new ArrayList<>();
        for (Pair<Object, Object> p : pairs) {
            ret.add(p.getFirst());
            ret.add(p.getSecond());
        }
        return ret;
    }

    @Override
    public String toString() {
        return AMF3Tools.amfToString(this);
    }

    public boolean hasWeakKeys() {
        return weakKeys;
    }

}

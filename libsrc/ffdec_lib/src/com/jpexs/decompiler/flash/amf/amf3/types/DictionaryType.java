package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.ListMap;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import java.util.ArrayList;
import java.util.List;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import java.util.HashMap;
import java.util.Map;

public class DictionaryType extends ListMap<Object, Object> implements WithSubValues, Amf3ValueType {

    private final boolean weakKeys;

    public DictionaryType(boolean weakKeys) {
        this(weakKeys, new HashMap<>());
    }

    public DictionaryType(boolean weakKeys, Map<Object, Object> entries) {
        super(true /*IdentityMap*/, entries);
        this.weakKeys = weakKeys; //TODO? Really make the Map weak - something like WeakIdentityMap - but is it neccessary for serialization?
    }

    @Override
    public List<Object> getSubValues() {
        List<Object> ret = new ArrayList<>();
        ret.addAll(keySet());
        ret.addAll(values());
        return ret;
    }

    @Override
    public String toString() {
        return Amf3Exporter.amfToString(this);
    }

    public boolean hasWeakKeys() {
        return weakKeys;
    }

}

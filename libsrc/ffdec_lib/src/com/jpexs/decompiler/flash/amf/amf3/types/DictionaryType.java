/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. */
package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.ListMap;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

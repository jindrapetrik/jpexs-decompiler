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

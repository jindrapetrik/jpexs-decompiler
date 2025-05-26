/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.amf.amf3.types;

import com.jpexs.decompiler.flash.amf.amf3.ListMap;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AMF3 array type
 */
public class ArrayType implements WithSubValues, Amf3ValueType {

    private List<Object> denseValues;
    private Map<String, Object> associativeValues;

    /**
     * Constructor.
     * @param associativeValues Associative values
     */
    public ArrayType(Map<? extends String, ? extends Object> associativeValues) {
        this(new ArrayList<>(), associativeValues);
    }

    /**
     * Constructor.
     * @param denseValues Dense values
     */
    public ArrayType(List<Object> denseValues) {
        this(denseValues, new HashMap<>());
    }

    /**
     * Constructor.
     */
    public ArrayType() {
        this(new ArrayList<>(), new HashMap<>());
    }

    /**
     * Constructor.
     * @param denseValues Dense values
     * @param associativeValues Associative values
     */
    public ArrayType(List<Object> denseValues, Map<? extends String, ? extends Object> associativeValues) {
        this.denseValues = new ArrayList<>(denseValues);
        this.associativeValues = new ListMap<>(associativeValues);
    }

    /**
     * Gets dense values.
     * @return Dense values
     */
    public List<Object> getDenseValues() {
        return new ArrayList<>(denseValues);
    }

    /**
     * Sets dense values.
     * @param denseValues Dense values
     */
    public void setDenseValues(List<Object> denseValues) {
        this.denseValues = new ArrayList<>(denseValues);
    }

    /**
     * Sets dense value.
     * @param key Key
     * @param value Value
     * @return Previous value
     */
    public Object setDense(int key, Object value) {
        return denseValues.set(key, value);
    }

    /**
     * Puts associative value.
     * @param key Key
     * @param value Value
     * @return Previous value
     */
    public Object putAssociative(String key, Object value) {
        return associativeValues.put(key, value);
    }

    /**
     * Gets associative values.
     * @return Associative values
     */
    public Map<String, Object> getAssociativeValues() {
        return new ListMap<>(associativeValues);
    }

    /**
     * Gets associative value.
     * @param key Key
     * @return Value
     */
    public Object getAssociative(String key) {
        return associativeValues.get(key);
    }

    /**
     * Gets dense value.
     * @param index Index
     * @return Value
     */
    public Object getDense(int index) {
        if (index >= 0 && index < denseValues.size()) {
            return denseValues.get(index);
        }
        return null;
    }

    /**
     * Gets associative key set.
     * @return Associative key set
     */
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

    /**
     * Sets associative values.
     * @param associativeValues Associative values
     */
    public void setAssociativeValues(Map<String, Object> associativeValues) {
        this.associativeValues = new ListMap<>(associativeValues);
    }

}

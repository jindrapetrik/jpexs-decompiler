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
import com.jpexs.decompiler.flash.amf.amf3.ListSet;
import com.jpexs.decompiler.flash.amf.amf3.Traits;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ObjectType implements WithSubValues, Amf3ValueType, Map<String, Object> {

    private Map<String, Object> sealedMembers;
    private Map<String, Object> dynamicMembers;
    private Map<String, Object> serializedMembers;
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

    public ObjectType(Traits traits, byte[] serializedData, Map<String, Object> serializedMembers) {
        this.traits = traits;
        this.serializedData = serializedData;
        this.serializedMembers = new ListMap<>(serializedMembers);
        this.dynamicMembers = new ListMap<>();
        this.sealedMembers = new ListMap<>();
        this.serialized = true;
    }

    public ObjectType(Traits traits) {
        this(traits, new HashMap<>(), new HashMap<>());
    }

    public ObjectType(Traits traits, Map<String, Object> sealedMembers, Map<String, Object> dynamicMembers) {
        this.sealedMembers = new ListMap<>(sealedMembers);
        this.dynamicMembers = new ListMap<>(dynamicMembers);
        this.serializedMembers = new ListMap<>();
        this.serialized = false;
        this.traits = traits;
    }

    public boolean isDynamic() {
        return traits.isDynamic();
    }

    public String getClassName() {
        return traits.getClassName();
    }

    @Override
    public List<Object> getSubValues() {
        List<Object> ret = new ArrayList<>();
        ret.addAll(dynamicMembers.keySet());
        ret.addAll(dynamicMembers.values());

        ret.addAll(sealedMembers.keySet());
        ret.addAll(sealedMembers.values());

        ret.addAll(serializedMembers.keySet());
        ret.addAll(serializedMembers.values());
        return ret;
    }

    @Override
    public String toString() {
        return Amf3Exporter.amfToString(this);
    }

    public void setSerializedData(byte[] serializedData) {
        this.serializedData = serializedData;
    }

    public byte[] getSerializedData() {
        return serializedData;
    }

    public void setSerializedMembers(Map<String, Object> serializedMembers) {
        this.serializedMembers = new ListMap<>(serializedMembers);
    }

    public Map<String, Object> getSerializedMembers() {
        return new ListMap<>(serializedMembers);
    }

    public void setSealedMembers(Map<String, Object> sealedMembers) {
        this.sealedMembers = new ListMap<>(sealedMembers);
    }

    public void setDynamicMembers(Map<String, Object> sealedMembers) {
        this.dynamicMembers = new ListMap<>(sealedMembers);
    }

    @Override
    public int size() {
        return keySet().size();
    }

    public int sealedMembersSize() {
        return sealedMembers.size();
    }

    public int dynamicMembersSize() {
        return dynamicMembers.size();
    }

    public int serializedMembersSize() {
        return serializedMembers.size();
    }

    @Override
    public boolean isEmpty() {
        return dynamicMembers.isEmpty() && sealedMembers.isEmpty() && serializedMembers.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        String keyString = (String) key;
        return containsDynamicMember(keyString) || containsSealedMember(keyString) || containsSerializedMember(keyString);
    }

    public boolean containsSealedMember(String name) {
        return sealedMembers.containsKey(name);
    }

    public boolean containsDynamicMember(String name) {
        return dynamicMembers.containsKey(name);
    }

    public boolean containsSerializedMember(String name) {
        return serializedMembers.containsKey(name);
    }

    @Override
    public boolean containsValue(Object value) {
        return dynamicMembers.containsValue(value) || sealedMembers.containsValue(value) || serializedMembers.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String stringKey = (String) key;

        if (dynamicMembers.containsKey(stringKey)) {
            return dynamicMembers.get(stringKey);
        }
        if (sealedMembers.containsKey(stringKey)) {
            return sealedMembers.get(stringKey);
        }
        if (serializedMembers.containsKey(stringKey)) {
            return serializedMembers.get(stringKey);
        }
        return null;
    }

    public Object getSealedMember(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String stringKey = (String) key;

        return sealedMembers.get(stringKey);
    }

    public Object getDynamicMember(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String stringKey = (String) key;

        return dynamicMembers.get(stringKey);
    }

    public Object getSerializedMember(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String stringKey = (String) key;

        return serializedMembers.get(stringKey);
    }

    @Override
    public Object put(String key, Object value) {
        return putDynamicMember(key, value);
    }

    public Object putDynamicMember(String key, Object value) {
        remove(key);
        return dynamicMembers.put(key, value);
    }

    public Object putSealedMember(String key, Object value) {
        remove(key);
        return sealedMembers.put(key, value);
    }

    public Object putSerializedMember(String key, Object value) {
        remove(key);
        return serializedMembers.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String stringKey = (String) key;
        if (dynamicMembers.containsKey(stringKey)) {
            return dynamicMembers.remove(stringKey);
        }
        if (sealedMembers.containsKey(stringKey)) {
            return sealedMembers.remove(stringKey);
        }
        if (serializedMembers.containsKey(stringKey)) {
            return serializedMembers.remove(stringKey);
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (Map.Entry<? extends String, ? extends Object> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    public void putAllDynamicMember(Map<? extends String, ? extends Object> m) {
        for (Map.Entry<? extends String, ? extends Object> e : m.entrySet()) {
            putDynamicMember(e.getKey(), e.getValue());
        }
    }

    public void putAllSealedMember(Map<? extends String, ? extends Object> m) {
        for (Map.Entry<? extends String, ? extends Object> e : m.entrySet()) {
            putSealedMember(e.getKey(), e.getValue());
        }
    }

    public void putAllSerializedMember(Map<? extends String, ? extends Object> m) {
        for (Map.Entry<? extends String, ? extends Object> e : m.entrySet()) {
            putSerializedMember(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        clearDynamicMembers();
        clearSealedMembers();
        clearSerializedMembers();
    }

    public void clearDynamicMembers() {
        dynamicMembers.clear();
    }

    public void clearSealedMembers() {
        sealedMembers.clear();
    }

    public void clearSerializedMembers() {
        serializedMembers.clear();
    }

    @Override
    public Set<String> keySet() {
        Set<String> ret = new ListSet<>();
        ret.addAll(sealedMembers.keySet());
        ret.addAll(dynamicMembers.keySet());
        ret.addAll(serializedMembers.keySet());
        return ret;
    }

    public Set<String> sealedMembersKeySet() {
        return new ListSet<>(sealedMembers.keySet());
    }

    public Set<String> dynamicMembersKeySet() {
        return new ListSet<>(dynamicMembers.keySet());
    }

    public Set<String> serializedMembersKeySet() {
        return new ListSet<>(serializedMembers.keySet());
    }

    @Override
    public Collection<Object> values() {
        List<Object> values = new ArrayList<>();
        Set<String> keys = keySet();
        for (String key : keys) {
            if (dynamicMembers.containsKey(key)) {
                values.add(dynamicMembers.get(key));
            } else if (sealedMembers.containsKey(key)) {
                values.add(sealedMembers.get(key));
            } else {
                values.add(serializedMembers.get(key));
            }
        }
        return values;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Set<String> keys = keySet();
        Set<Entry<String, Object>> ret = new ListSet<>();
        for (String key : keys) {
            ret.add(new ListMap.MyEntry<>(key, get(key)));
        }
        return ret;
    }

}

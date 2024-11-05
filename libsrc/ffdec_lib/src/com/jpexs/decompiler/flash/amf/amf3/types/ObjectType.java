/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.amf.amf3.Traits;
import com.jpexs.decompiler.flash.amf.amf3.WithSubValues;
import com.jpexs.decompiler.flash.exporters.amf.amf3.Amf3Exporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AMF3 object type.
 */
public class ObjectType implements WithSubValues, Amf3ValueType, Map<String, Object> {

    private Map<String, Object> sealedMembers;
    private Map<String, Object> dynamicMembers;
    private Map<String, Object> serializedMembers;
    //null = not serialized or unknown
    private byte[] serializedData = null;
    private boolean serialized;
    private Traits traits;

    /**
     * Checks if the object is serialized.
     * @return True if the object is serialized.
     */
    public boolean isSerialized() {
        return serialized;
    }

    /**
     * Gets the traits of the object.
     * @return The traits of the object.
     */
    public Traits getTraits() {
        return traits;
    }

    /**
     * Constructor.
     * @param traits The traits of the object.
     * @param serializedData The serialized data.
     * @param serializedMembers The serialized members.
     */
    public ObjectType(Traits traits, byte[] serializedData, Map<String, Object> serializedMembers) {
        this.traits = traits;
        this.serializedData = serializedData;
        this.serializedMembers = new ListMap<>(serializedMembers);
        this.dynamicMembers = new ListMap<>();
        this.sealedMembers = new ListMap<>();
        this.serialized = true;
    }

    /**
     * Constructor.
     * @param traits The traits of the object.
     */
    public ObjectType(Traits traits) {
        this(traits, new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructor.
     * @param traits The traits of the object.
     * @param sealedMembers The sealed members.
     * @param dynamicMembers The dynamic members.
     */
    public ObjectType(Traits traits, Map<String, Object> sealedMembers, Map<String, Object> dynamicMembers) {
        this.sealedMembers = new ListMap<>(sealedMembers);
        this.dynamicMembers = new ListMap<>(dynamicMembers);
        this.serializedMembers = new ListMap<>();
        this.serialized = false;
        this.traits = traits;
    }

    /**
     * Checks if the object is dynamic.
     * @return True if the object is dynamic.
     */
    public boolean isDynamic() {
        return traits.isDynamic();
    }

    /**
     * Gets the class name of the object.
     * @return The class name of the object.
     */
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

    /**
     * Sets the serialized data.
     * @param serializedData The serialized data.
     */
    public void setSerializedData(byte[] serializedData) {
        this.serializedData = serializedData;
    }

    /**
     * Gets the serialized data.
     * @return The serialized data.
     */
    public byte[] getSerializedData() {
        return serializedData;
    }

    /**
     * Sets the serialized members.
     * @param serializedMembers The serialized members.
     */
    public void setSerializedMembers(Map<String, Object> serializedMembers) {
        this.serializedMembers = new ListMap<>(serializedMembers);
    }

    /**
     * Gets the serialized members.
     * @return The serialized members.
     */
    public Map<String, Object> getSerializedMembers() {
        return new ListMap<>(serializedMembers);
    }

    /**
     * Sets the sealed members.
     * @param sealedMembers The sealed members.
     */
    public void setSealedMembers(Map<String, Object> sealedMembers) {
        this.sealedMembers = new ListMap<>(sealedMembers);
    }

    /**
     * Sets the dynamic members.
     * @param dynamicMembers The dynamic members.
     */
    public void setDynamicMembers(Map<String, Object> dynamicMembers) {
        this.dynamicMembers = new ListMap<>(dynamicMembers);
    }

    @Override
    public int size() {
        return keySet().size();
    }

    /**
     * Gets the size of the sealed members.
     * @return The size of the sealed members.
     */
    public int sealedMembersSize() {
        return sealedMembers.size();
    }

    /**
     * Gets the size of the dynamic members.
     * @return The size of the dynamic members.
     */
    public int dynamicMembersSize() {
        return dynamicMembers.size();
    }

    /**
     * Gets the size of the serialized members.
     * @return The size of the serialized members.
     */
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

    /**
     * Checks if the object contains a sealed member.
     * @param name The name of the member.
     * @return True if the object contains a sealed member.
     */
    public boolean containsSealedMember(String name) {
        return sealedMembers.containsKey(name);
    }

    /**
     * Checks if the object contains a dynamic member.
     * @param name The name of the member.
     * @return True if the object contains a dynamic member.
     */
    public boolean containsDynamicMember(String name) {
        return dynamicMembers.containsKey(name);
    }

    /**
     * Checks if the object contains a serialized member.
     * @param name The name of the member.
     * @return True if the object contains a serialized member.
     */
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

    /**
     * Gets a sealed member.
     * @param key The key of the member.
     * @return The sealed member.
     */
    public Object getSealedMember(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String stringKey = (String) key;

        return sealedMembers.get(stringKey);
    }

    /**
     * Gets a dynamic member.
     * @param key The key of the member.
     * @return The dynamic member.
     */
    public Object getDynamicMember(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String stringKey = (String) key;

        return dynamicMembers.get(stringKey);
    }

    /**
     * Gets a serialized member.
     * @param key The key of the member.
     * @return The serialized member.
     */
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

    /**
     * Puts a dynamic member.
     * @param key The key of the member.
     * @param value The value of the member.
     * @return The previous value of the member.
     */
    public Object putDynamicMember(String key, Object value) {
        remove(key);
        return dynamicMembers.put(key, value);
    }

    /**
     * Puts a sealed member.
     * @param key The key of the member.
     * @param value The value of the member.
     * @return The previous value of the member.
     */
    public Object putSealedMember(String key, Object value) {
        remove(key);
        return sealedMembers.put(key, value);
    }

    /**
     * Puts a serialized member.
     * @param key The key of the member.
     * @param value The value of the member.
     * @return The previous value of the member.
     */
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

    /**
     * Puts all dynamic members.
     * @param m The map of dynamic members.
     */
    public void putAllDynamicMember(Map<? extends String, ? extends Object> m) {
        for (Map.Entry<? extends String, ? extends Object> e : m.entrySet()) {
            putDynamicMember(e.getKey(), e.getValue());
        }
    }

    /**
     * Puts all sealed members.
     * @param m The map of sealed members.
     */
    public void putAllSealedMember(Map<? extends String, ? extends Object> m) {
        for (Map.Entry<? extends String, ? extends Object> e : m.entrySet()) {
            putSealedMember(e.getKey(), e.getValue());
        }
    }

    /**
     * Puts all serialized members.
     * @param m The map of serialized members.
     */
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

    /**
     * Clears all dynamic members.
     */
    public void clearDynamicMembers() {
        dynamicMembers.clear();
    }

    /**
     * Clears all sealed members.
     */
    public void clearSealedMembers() {
        sealedMembers.clear();
    }

    /**
     * Clears all serialized members.
     */
    public void clearSerializedMembers() {
        serializedMembers.clear();
    }

    @Override
    public Set<String> keySet() {
        Set<String> ret = new LinkedHashSet<>();
        ret.addAll(sealedMembers.keySet());
        ret.addAll(dynamicMembers.keySet());
        ret.addAll(serializedMembers.keySet());
        return ret;
    }

    /**
     * Gets the key set of the sealed members.
     * @return The key set of the sealed members.
     */
    public Set<String> sealedMembersKeySet() {
        return new LinkedHashSet<>(sealedMembers.keySet());
    }

    /**
     * Gets the key set of the dynamic members.
     * @return The key set of the dynamic members.
     */
    public Set<String> dynamicMembersKeySet() {
        return new LinkedHashSet<>(dynamicMembers.keySet());
    }

    /**
     * Gets the key set of the serialized members.
     * @return The key set of the serialized members.
     */
    public Set<String> serializedMembersKeySet() {
        return new LinkedHashSet<>(serializedMembers.keySet());
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
        Set<Entry<String, Object>> ret = new LinkedHashSet<>();
        for (String key : keys) {
            ret.add(new ListMap.MyEntry<>(key, get(key)));
        }
        return ret;
    }

}

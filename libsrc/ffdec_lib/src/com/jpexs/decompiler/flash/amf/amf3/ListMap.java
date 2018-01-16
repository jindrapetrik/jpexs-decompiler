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
package com.jpexs.decompiler.flash.amf.amf3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Map which maintains order of keys
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class ListMap<K, V> implements Map<K, V> {

    private final Set<K> orderedKeys = new ListSet<>();
    private final Map<K, V> map;

    /**
     * Creates new MaintainKeyOrderMap based on HashMap
     */
    public ListMap() {
        this(false);
    }

    /**
     * Creates new MaintainKeyOrderMap
     *
     * @param useIdentityMap The HashMap will be based on IdentityHashMap
     */
    public ListMap(boolean useIdentityMap) {
        this(useIdentityMap, new HashMap<>());
    }

    /**
     * Creates new MaintainKeyOrderMap based on HashMap
     *
     * @param m Initial items
     */
    public ListMap(Map<? extends K, ? extends V> m) {
        this(false, m);
    }

    /**
     * Creates new MaintainKeyOrderMap
     *
     * @param useIdentityMap The HashMap will be based on IdentityHashMap
     * @param m Initial items
     */
    public ListMap(boolean useIdentityMap, Map<? extends K, ? extends V> m) {
        if (useIdentityMap) {
            map = new IdentityHashMap<>();
        } else {
            map = new HashMap<>();
        }
        putAll(m);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        orderedKeys.add(key);
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        orderedKeys.remove(key);
        return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            orderedKeys.add(e.getKey());
        }
        map.putAll(m);
    }

    @Override
    public void clear() {
        orderedKeys.clear();
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return new ListSet<>(orderedKeys);
    }

    @Override
    public Collection<V> values() {
        List<V> vals = new ArrayList<>();
        for (K key : orderedKeys) {
            vals.add(map.get(key));
        }
        return vals;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> ret = new ListSet<>();
        for (K key : orderedKeys) {
            V value = map.get(key);
            ret.add(new MyEntry<>(key, value));
        }
        return ret;
    }

    public static class MyEntry<K, V> implements Entry<K, V> {

        private K key;
        private V value;

        public MyEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + Objects.hashCode(this.key);
            hash = 37 * hash + Objects.hashCode(this.value);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final MyEntry<?, ?> other = (MyEntry<?, ?>) obj;
            if (!Objects.equals(this.key, other.key)) {
                return false;
            }
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }

    }

}

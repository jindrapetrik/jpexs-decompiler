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
package com.jpexs.decompiler.flash.helpers.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @param <K>
 * @param <V>
 * @author JPEXS
 */
public class MyMap<K, V> implements Map<K, V> {

    List<MyEntry<K, V>> values = new ArrayList<>();

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        for (MyEntry<K, V> kv : values) {
            if (kv.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for (MyEntry<K, V> kv : values) {
            if (kv.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        for (MyEntry<K, V> kv : values) {
            if (kv.getKey().equals(key)) {
                return kv.getValue();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        for (MyEntry<K, V> kv : values) {
            if (kv.getKey().equals(key)) {
                kv.setValue(value);
                return value;
            }
        }
        values.add(new MyEntry<>(key, value));
        return value;
    }

    @Override
    public V remove(Object key) {
        for (int i = 0; i < values.size(); i++) {
            MyEntry<K, V> kv = values.get(i);
            if (kv.getKey().equals(key)) {
                values.remove(i);
                return kv.getValue();
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (K k : m.keySet()) {
            put(k, m.get(k));
        }
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public Set<K> keySet() {
        Set<K> ret = new MySet<>();
        for (MyEntry<K, V> kv : values) {
            ret.add(kv.getKey());
        }
        return ret;
    }

    @Override
    public Collection<V> values() {
        Collection<V> ret = new ArrayList<>();
        for (MyEntry<K, V> kv : values) {
            ret.add(kv.getValue());
        }
        return ret;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> ret = new MySet<>();
        ret.addAll(values);
        return ret;
    }
}

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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.helpers.collections;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;

/**
 *
 * @param <K> Key
 * @param <V> Value
 * @author JPEXS
 */
public class MyEntry<K, V> implements Entry<K, V>, Serializable {

    private final K key;

    private V value;

    public MyEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return key.toString() + " = " + value.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(key);
        hash = 61 * hash + Objects.hashCode(value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MyEntry<?, ?> other = (MyEntry<?, ?>) obj;
        if (!Objects.equals(key, other.key)) {
            return false;
        }
        if (!Objects.equals(value, other.value)) {
            return false;
        }
        return true;
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
        this.value = value;
        return value;
    }
}

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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @param <T>
 * @author JPEXS
 */
public class MySet<T> implements Set<T> {

    List<T> items = new ArrayList<>();

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        for (T t : items) {
            if (t.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos + 1 < items.size();
            }

            @Override
            public T next() {
                return items.get(pos++);
            }

            @Override
            public void remove() {
                items.remove(pos--);
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] ret = new Object[items.size()];
        for (int i = 0; i < items.size(); i++) {
            ret[i] = items.get(i);
        }
        return ret;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean add(T e) {
        items.add(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(o)) {
                items.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) {
            add(t);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public String toString() {
        String ret = "[";
        boolean first = true;
        for (T t : items) {
            if (!first) {
                ret += ", ";
            }
            ret += t.toString();
            first = false;
        }
        ret += "]";
        return ret;
    }
}

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
package com.jpexs.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Identity hash set which maintains insertion order.
 *
 * @param <E> Type of elements
 * @author JPEXS
 */
public class LinkedIdentityHashSet<E> implements Set<E> {

    private class MyObj {

        private final Object obj;

        public MyObj(Object obj) {
            this.obj = obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(obj);
        }

        @Override
        @SuppressWarnings("unchecked")
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
            final MyObj other = (MyObj) obj;
            return this.obj == other.obj;
        }
    }

    private final Set<MyObj> set = new LinkedHashSet<>();

    public LinkedIdentityHashSet() {
    }

    public LinkedIdentityHashSet(Collection<? extends E> c) {
        addAll(c);
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(new MyObj(o));
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<MyObj> setIterator = set.iterator();
        return new Iterator<E>() {
            @Override
            public boolean hasNext() {
                return setIterator.hasNext();
            }

            @Override
            @SuppressWarnings("unchecked")
            public E next() {
                return (E) setIterator.next().obj;
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] toArray() {
        Object[] objs = set.toArray();
        for (int i = 0; i < objs.length; i++) {
            objs[i] = ((MyObj) objs[i]).obj;
        }
        return objs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        T[] ret = prepareArray(a);
        Object[] objs = set.toArray();
        for (int i = 0; i < objs.length; i++) {
            ret[i] = (T) ((MyObj) objs[i]).obj;
        }
        return ret;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] prepareArray(T[] a) {
        int size = this.set.size();
        if (a.length < size) {
            return (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public boolean add(E e) {
        return set.add(new MyObj(e));
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(new MyObj(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Iterator<?> it = c.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (!set.contains(new MyObj(o))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Iterator<? extends E> it = c.iterator();
        List<MyObj> items = new ArrayList<>();
        while (it.hasNext()) {
            items.add(new MyObj(it.next()));
        }
        return set.addAll(items);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Iterator<?> it = c.iterator();
        List<MyObj> items = new ArrayList<>();
        while (it.hasNext()) {
            items.add(new MyObj(it.next()));
        }
        return set.retainAll(items);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Iterator<?> it = c.iterator();
        List<MyObj> items = new ArrayList<>();
        while (it.hasNext()) {
            items.add(new MyObj(it.next()));
        }
        return set.removeAll(items);
    }

    @Override
    public void clear() {
        set.clear();
    }
}

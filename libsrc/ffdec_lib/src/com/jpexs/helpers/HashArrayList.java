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
package com.jpexs.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author JPEXS
 */
public class HashArrayList<E> extends ArrayList<E> {

    private HashMap<E, Integer> map = new HashMap<>();

    public HashArrayList() {
    }

    public HashArrayList(Collection<? extends E> c) {
        for (E e : c) {
            add(e);
        }
    }

    public HashArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public void ensureCapacity(int minCapacity) {
        super.ensureCapacity(minCapacity);
        HashMap<E, Integer> oldMap = map;
        map = new HashMap<>(minCapacity * 10 / 7);
        map.putAll(oldMap);
    }

    @Override
    public boolean add(E e) {
        map.put(e, size());
        return super.add(e);
    }

    @Override
    public E set(int index, E element) {
        map.remove(get(index));
        map.put(element, index);
        return super.set(index, element);
    }

    @Override
    public int indexOf(Object o) {
        Integer index = map.get(o);
        if (index == null) {
            return -1;
        }

        return index;
    }
}

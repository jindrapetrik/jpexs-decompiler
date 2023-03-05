/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GraphPartMarkedArrayList<E> extends ArrayList<E> {

    private List<List<GraphPart>> listParts = new ArrayList<>();
    private List<GraphPart> currentParts = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public GraphPartMarkedArrayList(Collection<? extends E> collection) {
        super(collection);
        if (collection instanceof GraphPartMarkedArrayList) {
            for (int i = 0; i < collection.size(); i++) {
                listParts.add((List<GraphPart>) ((GraphPartMarkedArrayList) collection).listParts.get(i));
            }
            currentParts = ((GraphPartMarkedArrayList) collection).currentParts;
        } else {
            for (int i = 0; i < collection.size(); i++) {
                listParts.add(currentParts);
            }
        }
    }

    public GraphPartMarkedArrayList() {
    }

    public void startPart(GraphPart part) {
        currentParts.add(part);
    }

    public void clearCurrentParts() {
        currentParts = new ArrayList<>();
    }

    @Override
    public boolean add(E e) {
        listParts.add(currentParts);
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        listParts.add(index, currentParts);
        super.add(index, element);
    }

    public List<GraphPart> getPartsAt(int index) {
        return listParts.get(index);
    }

    public int indexOfPart(GraphPart part) {
        for (int i = 0; i < listParts.size(); i++) {
            List<GraphPart> list = listParts.get(i);
            if (list.indexOf(part) > -1) {
                return i;
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c instanceof GraphPartMarkedArrayList) {
            for (int i = 0; i < c.size(); i++) {
                listParts.add((List<GraphPart>) ((GraphPartMarkedArrayList) c).listParts.get(i));
            }
        } else {
            for (int i = 0; i < c.size(); i++) {
                listParts.add(currentParts);
            }
        }
        return super.addAll(c);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (c instanceof GraphPartMarkedArrayList) {
            for (int i = 0; i < c.size(); i++) {
                listParts.add(index + i, (List<GraphPart>) ((GraphPartMarkedArrayList) c).listParts.get(i));
            }
        } else {
            for (int i = 0; i < c.size(); i++) {
                listParts.add(index + i, currentParts);
            }
        }
        return super.addAll(index, c);
    }

    @Override
    public boolean remove(Object o) {
        if (contains(o)) {
            listParts.remove(indexOf(o));
        }
        return super.remove(o);
    }

    @Override
    public E remove(int index) {
        listParts.remove(index);
        return super.remove(index);
    }

    @Override
    public void clear() {
        listParts.clear();
        super.clear();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        GraphPartMarkedArrayList<E> ret = new GraphPartMarkedArrayList<E>(this);
        for (int i = size(); i > toIndex; i--) {
            ret.remove(i);
        }
        for (int i = 0; i < fromIndex; i++) {
            ret.remove(i);
        }
        return ret;
    }

    @Override
    public Object clone() {
        return new GraphPartMarkedArrayList<>(this);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            if (contains(o)) {
                listParts.remove(indexOf(o));
            }
        }

        return super.removeAll(c);
    }

}

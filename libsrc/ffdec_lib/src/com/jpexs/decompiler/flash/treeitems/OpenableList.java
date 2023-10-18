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
package com.jpexs.decompiler.flash.treeitems;

import com.jpexs.decompiler.flash.Bundle;
import com.jpexs.decompiler.flash.OpenableSourceInfo;
import com.jpexs.decompiler.flash.SWFContainerItem;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author JPEXS
 */
public class OpenableList implements List<Openable>, SWFContainerItem {

    public String name;

    public Bundle bundle;

    public OpenableSourceInfo sourceInfo;

    public List<Openable> items = new ArrayList<>();

    public boolean isBundle() {
        return bundle != null;
    }

    @Override
    public Openable getOpenable() {
        return null;
    }

    @Override
    public String toString() {
        if (isBundle()) {
            return name;
        } else {
            return items.get(0).getFileTitle();
        }
    }

    @Override
    public Iterator<Openable> iterator() {
        return items.iterator();
    }

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
        return items.contains(o);
    }

    @Override
    public Object[] toArray() {
        return items.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return items.toArray(ts);
    }      

    @Override
    public boolean containsAll(Collection<?> clctn) {
        return items.containsAll(clctn);
    }    

    @Override
    public boolean removeAll(Collection<?> clctn) {
        return items.removeAll(clctn);
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        return items.retainAll(clctn);
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public boolean addAll(Collection<? extends Openable> clctn) {
        return items.addAll(clctn);
    }

    @Override
    public boolean addAll(int i, Collection<? extends Openable> clctn) {
        return items.addAll(i, clctn);
    }

    @Override
    public Openable get(int i) {
        if (i < 0 || i >= items.size()) {
            return null;
        }
        return items.get(i);
    }

    @Override
    public Openable set(int i, Openable e) {
        return items.set(i, e);
    }

    @Override
    public boolean add(Openable e) {
        return items.add(e);
    }

    @Override
    public void add(int i, Openable e) {
        items.add(i, e);
    }

    @Override
    public boolean remove(Object o) {
        return items.remove(o);
    }

    @Override
    public Openable remove(int i) {
        return items.remove(i);
    }

    @Override
    public int indexOf(Object o) {
        return items.indexOf(0);
    }

    @Override
    public int lastIndexOf(Object o) {
        return items.lastIndexOf(o);
    }

    @Override
    public ListIterator<Openable> listIterator() {
        return items.listIterator();
    }

    @Override
    public ListIterator<Openable> listIterator(int i) {
        return items.listIterator(i);
    }

    @Override
    public List<Openable> subList(int i, int i1) {
        return items.subList(i, i1);
    }

    @Override
    public boolean isModified() {
        for (Openable s : items) {
            if (s.isModified()) {
                return true;
            }
        }
        return false;
    }
}

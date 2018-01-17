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
package com.jpexs.decompiler.flash.treeitems;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFBundle;
import com.jpexs.decompiler.flash.SWFContainerItem;
import com.jpexs.decompiler.flash.SWFSourceInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author JPEXS
 */
public class SWFList implements List<SWF>, SWFContainerItem {

    public String name;

    public SWFBundle bundle;

    public SWFSourceInfo sourceInfo;

    public List<SWF> swfs = new ArrayList<>();

    public boolean isBundle() {
        return bundle != null;
    }

    @Override
    public SWF getSwf() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String toString() {
        if (isBundle()) {
            return name;
        } else {
            return swfs.get(0).getFileTitle();
        }
    }

    @Override
    public Iterator<SWF> iterator() {
        return swfs.iterator();
    }

    @Override
    public int size() {
        return swfs.size();
    }

    @Override
    public boolean isEmpty() {
        return swfs.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return swfs.contains(o);
    }

    @Override
    public Object[] toArray() {
        return swfs.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return swfs.toArray(ts);
    }

    @Override
    public boolean add(SWF e) {
        return swfs.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return swfs.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> clctn) {
        return swfs.containsAll(clctn);
    }

    @Override
    public boolean addAll(Collection<? extends SWF> clctn) {
        return swfs.addAll(clctn);
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        return swfs.removeAll(clctn);
    }

    @Override
    public boolean retainAll(Collection<?> clctn) {
        return swfs.retainAll(clctn);
    }

    @Override
    public void clear() {
        swfs.clear();
    }

    @Override
    public boolean addAll(int i, Collection<? extends SWF> clctn) {
        return swfs.addAll(i, clctn);
    }

    @Override
    public SWF get(int i) {
        if (i < 0 || i >= swfs.size()) {
            return null;
        }
        return swfs.get(i);
    }

    @Override
    public SWF set(int i, SWF e) {
        return swfs.set(i, e);
    }

    @Override
    public void add(int i, SWF e) {
        swfs.add(i, e);
    }

    @Override
    public SWF remove(int i) {
        return swfs.remove(i);
    }

    @Override
    public int indexOf(Object o) {
        return swfs.indexOf(0);
    }

    @Override
    public int lastIndexOf(Object o) {
        return swfs.lastIndexOf(o);
    }

    @Override
    public ListIterator<SWF> listIterator() {
        return swfs.listIterator();
    }

    @Override
    public ListIterator<SWF> listIterator(int i) {
        return swfs.listIterator(i);
    }

    @Override
    public List<SWF> subList(int i, int i1) {
        return swfs.subList(i, i1);
    }

    @Override
    public boolean isModified() {
        for (SWF s : swfs) {
            if (s.isModified()) {
                return true;
            }
        }
        return false;
    }
}

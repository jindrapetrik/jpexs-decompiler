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
package com.jpexs.decompiler.flash.treeitems;

import com.jpexs.decompiler.flash.Bundle;
import com.jpexs.decompiler.flash.OpenableSourceInfo;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFContainerItem;
import com.jpexs.decompiler.flash.abc.ABC;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * List of openable items.
 *
 * @author JPEXS
 */
public class OpenableList implements List<Openable>, SWFContainerItem {

    /**
     * Name.
     */
    public String name;

    /**
     * Bundle. Can be null.
     */
    public Bundle bundle;

    /**
     * Source info.
     */
    public OpenableSourceInfo sourceInfo;

    /**
     * Items of the list.
     */
    public List<Openable> items = new ArrayList<>();

    /**
     * Checks whether it is bundle.
     *
     * @return
     */
    public boolean isBundle() {
        return bundle != null;
    }

    /**
     * Gets openable.
     *
     * @return
     */
    @Override
    public Openable getOpenable() {
        return null;
    }

    /**
     * ToString.
     *
     * @return
     */
    @Override
    public String toString() {
        if (isBundle()) {
            return name;
        } else {
            return items.get(0).getFileTitle();
        }
    }

    /**
     * Iterator.
     *
     * @return
     */
    @Override
    public Iterator<Openable> iterator() {
        return items.iterator();
    }

    /**
     * Gets item count.
     *
     * @return
     */
    @Override
    public int size() {
        return items.size();
    }

    /**
     * Checks whether items are empty.
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Checks whether list contains specific openable.
     *
     * @param o
     * @return
     */
    @Override
    public boolean contains(Object o) {
        return items.contains(o);
    }

    /**
     * Converts to array.
     *
     * @return
     */
    @Override
    public Object[] toArray() {
        return items.toArray();
    }

    /**
     * Converts to array.
     *
     * @param <T>
     * @param ts
     * @return
     */
    @Override
    public <T> T[] toArray(T[] ts) {
        return items.toArray(ts);
    }

    /**
     * Contains all.
     *
     * @param clctn
     * @return
     */
    @Override
    public boolean containsAll(Collection<?> clctn) {
        return items.containsAll(clctn);
    }

    /**
     * Removes all.
     *
     * @param clctn
     * @return
     */
    @Override
    public boolean removeAll(Collection<?> clctn) {
        return items.removeAll(clctn);
    }

    /**
     * Retains all.
     *
     * @param clctn
     * @return
     */
    @Override
    public boolean retainAll(Collection<?> clctn) {
        return items.retainAll(clctn);
    }

    /**
     * Clears list.
     */
    @Override
    public void clear() {
        items.clear();
    }

    /**
     * Adds all items.
     *
     * @param clctn
     * @return
     */
    @Override
    public boolean addAll(Collection<? extends Openable> clctn) {
        return items.addAll(clctn);
    }

    /**
     * Adds all items at index.
     *
     * @param i
     * @param clctn
     * @return
     */
    @Override
    public boolean addAll(int i, Collection<? extends Openable> clctn) {
        return items.addAll(i, clctn);
    }

    /**
     * Gets item at index.
     *
     * @param i
     * @return
     */
    @Override
    public Openable get(int i) {
        if (i < 0 || i >= items.size()) {
            return null;
        }
        return items.get(i);
    }

    /**
     * Sets item at index.
     *
     * @param i
     * @param e
     * @return
     */
    @Override
    public Openable set(int i, Openable e) {
        return items.set(i, e);
    }

    /**
     * Adds item.
     *
     * @param e
     * @return
     */
    @Override
    public boolean add(Openable e) {
        return items.add(e);
    }

    /**
     * Adds item at index.
     *
     * @param i
     * @param e
     */
    @Override
    public void add(int i, Openable e) {
        items.add(i, e);
    }

    /**
     * Removes item.
     *
     * @param o
     * @return
     */
    @Override
    public boolean remove(Object o) {
        return items.remove(o);
    }

    /**
     * Removes item at index.
     *
     * @param i
     * @return
     */
    @Override
    public Openable remove(int i) {
        return items.remove(i);
    }

    /**
     * Index of item.
     *
     * @param o
     * @return
     */
    @Override
    public int indexOf(Object o) {
        return items.indexOf(0);
    }

    /**
     * Last index of item.
     *
     * @param o
     * @return
     */
    @Override
    public int lastIndexOf(Object o) {
        return items.lastIndexOf(o);
    }

    /**
     * List iterator.
     *
     * @return
     */
    @Override
    public ListIterator<Openable> listIterator() {
        return items.listIterator();
    }

    /**
     * List iterator.
     *
     * @param i
     * @return
     */
    @Override
    public ListIterator<Openable> listIterator(int i) {
        return items.listIterator(i);
    }

    /**
     * Sublist.
     *
     * @param i
     * @param i1
     * @return
     */
    @Override
    public List<Openable> subList(int i, int i1) {
        return items.subList(i, i1);
    }

    /**
     * Gets modified flag.
     *
     * @return
     */
    @Override
    public boolean isModified() {
        for (Openable s : items) {
            if (s.isModified()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets modified flag. It marks all items inside as modified.
     */
    public void setModified() {
        for (Openable openable : this) {
            if (openable instanceof SWF) {
                SWF swf = (SWF) openable;
                swf.setModified(true);
            }
            if (openable instanceof ABC) {
                ABC abc = (ABC) openable;
                abc.getSwf().setModified(true);
            }
        }
    }
}

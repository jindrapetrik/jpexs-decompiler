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
package com.jpexs.decompiler.flash.helpers.hilight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * List of Highlighting objects.
 *
 * @author JPEXS
 */
public class HighlightingList extends ArrayList<Highlighting> {

    private boolean finished = false;

    /**
     * Marks this list as finished, so no more elements can be added.
     */
    public void finish() {
        this.finished = true;
    }

    /**
     * Returns true if this list is finished.
     *
     * @return true if this list is finished
     */
    public boolean isFinished() {
        return finished;
    }

    private void checkWriteAccess() {
        if (finished) {
            throw new RuntimeException("Cannot add to readonly list");
        }
    }

    @Override
    public boolean add(Highlighting e) {
        checkWriteAccess();
        return super.add(e);
    }

    @Override
    public void add(int index, Highlighting element) {
        checkWriteAccess();
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends Highlighting> c) {
        checkWriteAccess();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Highlighting> c) {
        checkWriteAccess();
        return super.addAll(index, c);
    }

    @Override
    public boolean remove(Object o) {
        checkWriteAccess();
        return super.remove(o);
    }

    @Override
    public Highlighting remove(int index) {
        checkWriteAccess();
        return super.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        checkWriteAccess();
        return super.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super Highlighting> filter) {
        checkWriteAccess();
        return super.removeIf(filter);
    }

    @Override
    public void replaceAll(UnaryOperator<Highlighting> operator) {
        checkWriteAccess();
        super.replaceAll(operator);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        checkWriteAccess();
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        checkWriteAccess();
        return super.retainAll(c);
    }

    @Override
    public void clear() {
        checkWriteAccess();
        super.clear();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        return o == this;
    }
}

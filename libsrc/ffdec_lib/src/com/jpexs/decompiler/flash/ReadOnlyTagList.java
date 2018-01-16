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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.tags.Tag;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ReadOnlyTagList implements Iterable<Tag> {

    public static final ReadOnlyTagList EMPTY = new ReadOnlyTagList(new ArrayList<>());

    private final List<Tag> list;

    public ReadOnlyTagList(List<Tag> list) {
        this.list = list;
    }

    @Override
    public Iterator<Tag> iterator() {
        return list.iterator();
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Tag get(int index) {
        return list.get(index);
    }

    public int indexOf(Tag tag) {
        return list.indexOf(tag);
    }

    public ArrayList<Tag> toArrayList() {
        return new ArrayList<>(list);
    }
}

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
package com.jpexs.decompiler.flash;

import com.jpexs.decompiler.flash.tags.Tag;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tag list that is read-only.
 *
 * @author JPEXS
 */
public class ReadOnlyTagList implements Iterable<Tag> {

    /**
     * Empty read-only tag list.
     */
    public static final ReadOnlyTagList EMPTY = new ReadOnlyTagList(new ArrayList<>());

    /**
     * List of tags.
     */
    private final List<Tag> list;

    /**
     * Constructs read-only tag list.
     *
     * @param list List of tags
     */
    public ReadOnlyTagList(List<Tag> list) {
        this.list = list;
    }

    /**
     * Returns iterator for tags.
     *
     * @return Iterator for tags
     */
    @Override
    public Iterator<Tag> iterator() {
        return list.iterator();
    }

    /**
     * Returns number of tags.
     *
     * @return Number of tags
     */
    public int size() {
        return list.size();
    }

    /**
     * Returns true if list is empty.
     *
     * @return True if list is empty
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Returns tag at index.
     *
     * @param index Index
     * @return Tag
     */
    public Tag get(int index) {
        return list.get(index);
    }

    /**
     * Returns index of tag.
     *
     * @param tag Tag
     * @return Index of tag or -1 if not found
     */
    public int indexOf(Tag tag) {
        return list.indexOf(tag);
    }

    /**
     * Converts list to array list.
     *
     * @return Array list
     */
    public ArrayList<Tag> toArrayList() {
        return new ArrayList<>(list);
    }
}

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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 * Special item for marking a position in the graph.
 *
 * @author JPEXS
 */
public class MarkItem extends GraphTargetItem {

    /**
     * Mark string
     */
    private final String mark;

    /**
     * Constructs a new mark item.
     *
     * @param mark Mark string
     */
    public MarkItem(String mark) {
        super(null, null, null, NOPRECEDENCE);
        this.mark = mark;
    }

    /**
     * Appends this item to the writer.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     */
    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        return writer.append("// ").append(AppResources.translate("decompilerMark")).append(":").append(mark);
    }

    /**
     * Gets the mark string.
     *
     * @return Mark string
     */
    public String getMark() {
        return mark;
    }

    /**
     * Checks if this item is empty.
     *
     * @return Always true
     */
    @Override
    public boolean isEmpty() {
        return true;
    }

    /**
     * Checks if this item has a return value.
     *
     * @return Always false
     */
    @Override
    public boolean hasReturnValue() {
        return false;
    }

    /**
     * Gets the return type of this item.
     *
     * @return Unbounded
     */
    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }
}

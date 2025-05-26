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
package com.jpexs.decompiler.graph;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.Set;

/**
 * An item that cannot be statically computed.
 *
 * @author JPEXS
 */
public class NotCompileTimeItem extends GraphTargetItem {

    /**
     * Object that cannot be statically computed.
     */
    public GraphTargetItem object;

    /**
     * Constructs a new NotCompileTimeItem.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param object Object that cannot be statically computed
     */
    public NotCompileTimeItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object) {
        super(null, instruction, lineStartIns, NOPRECEDENCE);
        this.object = object;
    }

    /**
     * Whether this item can be computed statically.
     *
     * @param dependencies Dependencies
     * @return Whether this item can be computed statically
     */
    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return false;
    }

    /**
     * Gets through the object that cannot be statically computed.
     *
     * @return Through the object that cannot be statically computed
     */
    @Override
    public GraphTargetItem getThroughNotCompilable() {
        if (object == null) {
            return object;
        }
        return object.getThroughNotCompilable();
    }

    /**
     * Appends this item to the writer.
     *
     * @param writer Writer
     * @param localData Local data
     * @return Writer
     * @throws InterruptedException On interrupt
     */
    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return object.toString(writer, localData);
    }

    /**
     * Whether this item has a return value.
     *
     * @return Whether this item has a return value
     */
    @Override
    public boolean hasReturnValue() {
        return object.hasReturnValue();
    }

    /**
     * Gets the return type of this item.
     *
     * @return Return type of this item
     */
    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }
}

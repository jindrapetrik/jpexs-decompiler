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
package com.jpexs.decompiler.flash.abc.types;

import com.jpexs.decompiler.graph.GraphTargetItem;

/**
 * Assigned value - for slots, const.
 *
 * @author JPEXS
 */
public class AssignedValue {

    /**
     * Command
     */
    public GraphTargetItem command;
    
    /**
     * Value
     */
    public GraphTargetItem value;

    /**
     * Initializer type
     */
    public int initializer;

    /**
     * Method index
     */
    public int method;

    /**
     * Constructs a new assigned value.
     *
     * @param command Command
     * @param value Value
     * @param initializer Initializer type
     * @param method Method index
     */
    public AssignedValue(GraphTargetItem command, GraphTargetItem value, int initializer, int method) {
        this.command = command;
        this.value = value;
        this.initializer = initializer;
        this.method = method;
    }
}

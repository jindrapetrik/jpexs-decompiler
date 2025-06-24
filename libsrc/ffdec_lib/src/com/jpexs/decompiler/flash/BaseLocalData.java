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

import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.SecondPassData;
import java.util.HashSet;
import java.util.Set;

/**
 * Base local data
 *
 * @author JPEXS
 */
public abstract class BaseLocalData {

    /**
     * Line start instruction
     */
    public GraphSourceItem lineStartInstruction;

    /**
     * Set of all switch parts
     */
    public Set<GraphPart> allSwitchParts = new HashSet<>();

    /**
     * Second pass data
     */
    public SecondPassData secondPassData = null;
    
    /**
     * SWF version
     */
    public int swfVersion = -1;

    /**
     * Constructor.
     */
    public BaseLocalData() {
    }
}

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

import java.util.Collection;
import java.util.Stack;

/**
 * Recursive graph target visitor interface.
 *
 * @author JPEXS
 */
public interface GraphTargetRecursiveVisitorInterface {

    /**
     * Visits a graph target item.
     *
     * @param item Graph target item
     * @param parentStack Stack of parents
     */
    public void visit(GraphTargetItem item, Stack<GraphTargetItem> parentStack);

    /**
     * Visits all graph target items.
     *
     * @param items Collection of graph target items
     * @param parentStack Stack of parents
     */
    public void visitAll(Collection<GraphTargetItem> items, Stack<GraphTargetItem> parentStack);
}

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

import com.jpexs.decompiler.graph.model.ContinueItem;
import java.util.List;

/**
 * Block interface. For example, a block can be a loop, if statement, or a
 * function.
 *
 * @author JPEXS
 */
public interface Block {

    /**
     * Gets all sub continues.
     *
     * @return List of continues
     */
    public List<ContinueItem> getContinues();

    /**
     * Gets all sub blocks.
     *
     * @return List of blocks
     */
    public List<List<GraphTargetItem>> getSubs();
}

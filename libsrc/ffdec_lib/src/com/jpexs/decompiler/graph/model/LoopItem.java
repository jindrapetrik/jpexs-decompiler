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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import java.util.List;

/**
 * Base class for loops.
 *
 * @author JPEXS
 */
public abstract class LoopItem extends GraphTargetItem {

    /**
     * Loop
     */
    public Loop loop;

    /**
     * Constructor.
     *
     * @param dialect Dialect
     * @param src Source item
     * @param lineStartItem Line start item
     * @param loop Loop
     */
    public LoopItem(GraphTargetDialect dialect, GraphSourceItem src, GraphSourceItem lineStartItem, Loop loop) {
        super(dialect, src, lineStartItem, NOPRECEDENCE);
        this.loop = loop;
    }

    /**
     * Checks if loop has base body.
     * @return True if loop has base body
     */
    public abstract boolean hasBaseBody();

    /**
     * Gets base body commands.
     * @return List of base body commands
     */
    public abstract List<GraphTargetItem> getBaseBodyCommands();
}

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
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.List;

/**
 * Binary operation interface. Operation on two operands.
 *
 * @author JPEXS
 */
public interface BinaryOp {

    /**
     * Gets left side.
     * @return Left side
     */
    public GraphTargetItem getLeftSide();

    /**
     * Gets right side.
     * @return Right side
     */
    public GraphTargetItem getRightSide();

    /**
     * Sets left side.
     * @param value Left side
     */
    public void setLeftSide(GraphTargetItem value);

    /**
     * Sets right side.
     * @param value Right side
     */
    public void setRightSide(GraphTargetItem value);

    /**
     * Gets precedence.
     * @return Precedence
     */
    public int getPrecedence();

    /**
     * Gets all sub items.
     * @return All sub items
     */
    public List<GraphTargetItem> getAllSubItems();

    /**
     * Gets operator.
     * @return Operator
     */
    public String getOperator();

    /**
     * Gets operator instruction.
     * @return Operator instruction
     */
    public List<GraphSourceItem> getOperatorInstruction();
}

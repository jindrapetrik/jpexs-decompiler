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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.avm2.model.clauses.DeclarationAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;

/**
 * Set type value.
 *
 * @author JPEXS
 */
public interface SetTypeAVM2Item {

    /**
     * Gets object.
     *
     * @return Object
     */
    public GraphTargetItem getObject();

    /**
     * Gets value.
     *
     * @return Value
     */
    public GraphTargetItem getValue();

    /**
     * Gets declaration.
     *
     * @return Declaration
     */
    public DeclarationAVM2Item getDeclaration();

    /**
     * Sets declaration.
     *
     * @param dec Declaration
     */
    public void setDeclaration(DeclarationAVM2Item dec);

    /**
     * Gets compound value.
     *
     * @return Compound value
     */
    public GraphTargetItem getCompoundValue();

    /**
     * Sets compound value.
     *
     * @param value Compound value
     */
    public void setCompoundValue(GraphTargetItem value);

    /**
     * Sets compound operator.
     *
     * @param operator Compound operator
     */
    public void setCompoundOperator(String operator);

    /**
     * Gets compound operator.
     *
     * @return Compound operator
     */
    public String getCompoundOperator();
}

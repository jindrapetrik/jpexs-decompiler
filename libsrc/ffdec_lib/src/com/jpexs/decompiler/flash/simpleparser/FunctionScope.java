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
package com.jpexs.decompiler.flash.simpleparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Function scope.
 * @author JPEXS
 */
public class FunctionScope implements Scope {

    private final List<VariableOrScopeWithAccess> items = new ArrayList<>();
    private final boolean isStatic;
    private final int position;
    private final int endPosition;

    public FunctionScope(int position, int endPosition, List<VariableOrScope> functionBody, boolean isStatic) {
        for (VariableOrScope s : functionBody) {
            items.add(new VariableOrScopeWithAccess(s, false));
        }
        this.isStatic = isStatic;
        this.position = position;
        this.endPosition = endPosition;
    }

    public boolean isStatic() {
        return isStatic;
    }


    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public int getEndPosition() {
        return endPosition;
    }        

    @Override
    public List<VariableOrScopeWithAccess> getScopeItems() {
        return items;
    }
    
}

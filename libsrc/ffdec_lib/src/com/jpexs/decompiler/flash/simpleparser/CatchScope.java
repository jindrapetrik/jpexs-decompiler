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
 * Catch (of try..catch) clause scope.
 * @author JPEXS
 */
public class CatchScope implements Scope {

    private final List<VariableOrScopeWithAccess> items = new ArrayList<>();
    private final int position;
    private final int endPosition;

    public CatchScope(int position, int endPosition, Variable catchVariable, List<VariableOrScope> catchBody) {
        items.add(new VariableOrScopeWithAccess(catchVariable, false));
        for (VariableOrScope s : catchBody) {
            items.add(new VariableOrScopeWithAccess(s, true));
        }
        this.position = position;
        this.endPosition = endPosition;
    }

    @Override
    public List<VariableOrScopeWithAccess> getScopeItems() {
        return items;
    } 

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public int getEndPosition() {
        return endPosition;
    }
        
}

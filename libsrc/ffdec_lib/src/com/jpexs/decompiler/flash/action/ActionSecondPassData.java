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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SecondPassData;
import java.util.ArrayList;
import java.util.List;

/**
 * Second pass data for ActionScript 1/2 decompiler.
 *
 * @author JPEXS
 */
public class ActionSecondPassData extends SecondPassData {

    /**
     * List of parts for each switch statement
     */
    List<List<GraphPart>> switchParts = new ArrayList<>();
    /**
     * List of onFalse parts for each switch statement
     */
    List<List<GraphPart>> switchOnFalseParts = new ArrayList<>();
    /**
     * List of case expressions for each switch statement
     */
    List<List<GraphTargetItem>> switchCaseExpressions = new ArrayList<>();

    /**
     * Constructor.
     */
    public ActionSecondPassData() {

    }
}

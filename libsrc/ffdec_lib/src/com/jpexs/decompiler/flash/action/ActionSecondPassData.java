/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
 *
 * @author JPEXS
 */
public class ActionSecondPassData extends SecondPassData {

    List<List<GraphPart>> switchParts = new ArrayList<>();
    List<List<GraphPart>> switchOnFalseParts = new ArrayList<>();
    List<List<GraphTargetItem>> switchCaseExpressions = new ArrayList<>();
}

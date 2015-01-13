/*
 *  Copyright (C) 2014-2015 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GetterAVM2Item extends MethodAVM2Item {

    public GetterAVM2Item(String pkg, boolean isInterface, String customNamespace, boolean needsActivation, boolean hasRest, int line, boolean override, boolean isFinal, boolean isStatic, int namespace, String methodName, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, List<AssignableAVM2Item> subvariables, GraphTargetItem retType) {
        super(pkg, isInterface, customNamespace, needsActivation, hasRest, line, override, isFinal, isStatic, namespace, methodName, paramTypes, paramNames, paramValues, body, subvariables, retType);
    }

}

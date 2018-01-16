/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class GetterAVM2Item extends MethodAVM2Item {

    public GetterAVM2Item(List<List<NamespaceItem>> allOpenedNamespaces, boolean outsidePackage, boolean isPrivate, List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, boolean isInterface, String customNamespace, boolean needsActivation, boolean hasRest, int line, boolean override, boolean isFinal, boolean isStatic, String methodName, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, List<AssignableAVM2Item> subvariables, GraphTargetItem retType) {
        super(allOpenedNamespaces, outsidePackage, isPrivate, metadata, pkg, isInterface, customNamespace, needsActivation, hasRest, line, override, isFinal, isStatic, methodName, paramTypes, paramNames, paramValues, body, subvariables, retType);
    }
}

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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.List;
import java.util.Map;

/**
 * Setter.
 *
 * @author JPEXS
 */
public class SetterAVM2Item extends MethodAVM2Item {

    /**
     * Constructor.
     * @param allOpenedNamespaces All opened namespaces
     * @param outsidePackage Is outside package
     * @param isPrivate Is private
     * @param metadata Metadata
     * @param pkg Package
     * @param isInterface Is interface
     * @param isNative Is native
     * @param customNamespace Custom namespace
     * @param needsActivation Needs activation
     * @param hasRest Has rest
     * @param line Line
     * @param override Override
     * @param isFinal Is final
     * @param isStatic Is static
     * @param methodName Method name
     * @param paramTypes Parameter types
     * @param paramNames Parameter names
     * @param paramValues Parameter values
     * @param body Body
     * @param subvariables Subvariables
     * @param retType Return type
     */
    public SetterAVM2Item(List<List<NamespaceItem>> allOpenedNamespaces, boolean outsidePackage, boolean isPrivate, List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, boolean isInterface, boolean isNative, String customNamespace, boolean needsActivation, boolean hasRest, int line, boolean override, boolean isFinal, boolean isStatic, String methodName, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, List<AssignableAVM2Item> subvariables, GraphTargetItem retType) {
        super(allOpenedNamespaces, outsidePackage, isPrivate, metadata, pkg, isInterface, isNative, customNamespace, needsActivation, hasRest, line, override, isFinal, isStatic, methodName, paramTypes, paramNames, paramValues, body, subvariables, retType);
    }
}

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

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class MethodAVM2Item extends FunctionAVM2Item {

    private final boolean isStatic;

    private final boolean isFinal;

    private final boolean override;

    private final boolean isPrivate;

    public final boolean outsidePackage;

    public boolean isPrivate() {
        return isPrivate;
    }

    public String customNamespace;

    public List<List<NamespaceItem>> allOpenedNamespaces;

    //public boolean isInterface;
    public MethodAVM2Item(List<List<NamespaceItem>> allOpenedNamespaces, boolean outsidePackage, boolean isPrivate, List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, boolean isInterface, String customNamespace, boolean needsActivation, boolean hasRest, int line, boolean override, boolean isFinal, boolean isStatic, String methodName, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, List<AssignableAVM2Item> subvariables, GraphTargetItem retType) {
        super(metadata, pkg, isInterface, needsActivation, hasRest, line, methodName, paramTypes, paramNames, paramValues, body, subvariables, retType);
        this.outsidePackage = outsidePackage;
        this.allOpenedNamespaces = allOpenedNamespaces;
        this.isStatic = isStatic;
        this.override = override;
        this.isFinal = isFinal;
        this.isPrivate = isPrivate;
        this.customNamespace = customNamespace;
        //this.isInterface = this.isInterface;
    }

    public boolean isOverride() {
        return override;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer; //todo?
    }
}

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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Map;

/**
 * Function.
 *
 * @author JPEXS
 */
public class FunctionAVM2Item extends AVM2Item {
    /**
     * Calculated function name
     */
    public String calculatedFunctionName;

    /**
     * Function name
     */
    public String functionName;

    /**
     * Parameter names
     */
    public List<String> paramNames;

    /**
     * Body
     */
    public List<GraphTargetItem> body;

    /**
     * Subvariables
     */
    public List<AssignableAVM2Item> subvariables;

    /**
     * Parameter types
     */
    public List<GraphTargetItem> paramTypes;

    /**
     * Parameter values
     */
    public List<GraphTargetItem> paramValues;

    /**
     * Return type
     */
    public GraphTargetItem retType;

    /**
     * Line
     */
    public int line;

    /**
     * Has rest
     */
    public boolean hasRest;

    /**
     * Needs activation
     */
    public boolean needsActivation;

    /**
     * Is interface
     */
    public boolean isInterface;

    /**
     * Is native
     */
    public boolean isNative;

    /**
     * Package
     */
    public NamespaceItem pkg;

    /**
     * Metadata
     */
    public List<Map.Entry<String, Map<String, String>>> metadata;

    /**
     * Constructor.
     *
     * @param metadata Metadata
     * @param pkg Package
     * @param isInterface Is interface
     * @param isNative Is native
     * @param needsActivation Needs activation
     * @param hasRest Has rest
     * @param line Line
     * @param functionName Function name
     * @param paramTypes Parameter types
     * @param paramNames Parameter names
     * @param paramValues Parameter values
     * @param body Body
     * @param subvariables Subvariables
     * @param retType Return type
     */
    public FunctionAVM2Item(List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, boolean isInterface, boolean isNative, boolean needsActivation, boolean hasRest, int line, String functionName, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, List<AssignableAVM2Item> subvariables, GraphTargetItem retType) {

        super(null, null, NOPRECEDENCE);
        this.metadata = metadata;
        this.pkg = pkg;
        this.needsActivation = needsActivation;
        this.paramNames = paramNames;
        this.body = body;
        this.functionName = functionName;
        this.subvariables = subvariables;
        this.paramTypes = paramTypes;
        this.paramValues = paramValues;
        this.retType = retType;
        this.line = line;
        this.hasRest = hasRest;
        this.isInterface = isInterface;
        this.isNative = isNative;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer; //todo?
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(DottedChain.FUNCTION);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return ((AVM2SourceGenerator) generator).generate(localData, this);
    }
}

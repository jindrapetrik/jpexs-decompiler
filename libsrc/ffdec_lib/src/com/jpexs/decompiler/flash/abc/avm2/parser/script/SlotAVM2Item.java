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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Slot.
 *
 * @author JPEXS
 */
public class SlotAVM2Item extends AVM2Item {

    /**
     * Is static
     */
    private final boolean isStatic;

    /**
     * Variable
     */
    public String var;

    /**
     * Type
     */
    public GraphTargetItem type;

    /**
     * Custom namespace
     */
    public String customNamespace;

    /**
     * Line
     */
    public int line;

    /**
     * Metadata
     */
    public List<Map.Entry<String, Map<String, String>>> metadata;

    /**
     * Package
     */
    public NamespaceItem pkg;

    /**
     * Is static.
     *
     * @return Is static
     */
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Constructor.
     *
     * @param metadata Metadata
     * @param pkg Package
     * @param customNamespace Custom namespace
     * @param isStatic Is static
     * @param var Variable
     * @param type Type
     * @param value Value
     * @param line Line
     */
    public SlotAVM2Item(List<Map.Entry<String, Map<String, String>>> metadata, NamespaceItem pkg, String customNamespace, boolean isStatic, String var, GraphTargetItem type, GraphTargetItem value, int line) {
        super(null, null, NOPRECEDENCE, value);
        this.metadata = metadata;
        this.pkg = pkg;
        this.line = line;
        this.isStatic = isStatic;
        this.var = var;
        this.type = type;
        this.customNamespace = customNamespace;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer; //TODO
    }

    @Override
    public GraphTargetItem returnType() {
        return null;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        
        if (localData.isStatic != isStatic) {
            return new ArrayList<>();
        }
        
        AVM2SourceGenerator agen = (AVM2SourceGenerator) generator;
        int ns = agen.genNs(localData.importedClasses, pkg.name, pkg, localData.openedNamespaces, localData, line);       
        
        List<GraphSourceItem> ret = new ArrayList<>();
        if (value != null) {
            GraphTargetItem value2 = AVM2SourceGenerator.handleAndOrCoerce(value, type);
            ret.add(ins(AVM2Instructions.FindProperty, agen.traitName(ns, var)));
            localData.isStatic = true;
            ret.addAll(agen.toInsList(value2.toSource(localData, agen)));
            ret.add(ins(AVM2Instructions.SetProperty, agen.traitName(ns, var)));
        }
        return ret;
    }

}

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

import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class ClassAVM2Item extends AVM2Item implements Block {

    public List<GraphTargetItem> traits;

    public GraphTargetItem extendsOp;

    public List<GraphTargetItem> implementsOp;

    public String className;

    public GraphTargetItem iinit;

    public boolean isDynamic;

    public boolean isFinal;

    public List<NamespaceItem> openedNamespaces;

    public List<GraphTargetItem> staticInit;

    public boolean cinitActivation;

    public boolean iinitActivation;

    public List<AssignableAVM2Item> cinitVariables;

    public List<DottedChain> importedClasses;

    public NamespaceItem pkg;

    public List<AssignableAVM2Item> iinitVariables;

    public List<Map.Entry<String, Map<String, String>>> metadata;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (traits != null) {
            ret.add(traits);
        }
        return ret;
    }

    public ClassAVM2Item(List<Map.Entry<String, Map<String, String>>> metadata, List<DottedChain> importedClasses, NamespaceItem pkg, List<NamespaceItem> openedNamespaces, boolean isFinal, boolean isDynamic, String className, GraphTargetItem extendsOp, List<GraphTargetItem> implementsOp, List<GraphTargetItem> cinit, boolean staticInitActivation, List<AssignableAVM2Item> cinitVariables, GraphTargetItem iinit, List<AssignableAVM2Item> iinitVariables, List<GraphTargetItem> traits, boolean iinitActivation) {
        super(null, null, NOPRECEDENCE);
        this.metadata = metadata;
        this.importedClasses = importedClasses;
        this.pkg = pkg;
        this.className = className;
        this.traits = traits;
        this.extendsOp = extendsOp;
        this.implementsOp = implementsOp;
        this.iinitActivation = iinitActivation;
        this.iinit = iinit;
        this.isDynamic = isDynamic;
        this.isFinal = isFinal;
        this.openedNamespaces = openedNamespaces;
        this.staticInit = cinit;
        this.cinitActivation = staticInitActivation;
        this.cinitVariables = cinitVariables;
        this.iinitVariables = iinitVariables;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return new UnboundedTypeItem(); //FIXME
    }
}

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
package com.jpexs.decompiler.flash.action.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class VariableActionItem extends ActionItem {

    private ActionItem it;

    private final String variableName;

    private GraphTargetItem storeValue;

    private boolean definition;

    public void setDefinition(boolean definition) {
        this.definition = definition;
    }

    public void setStoreValue(GraphTargetItem storeValue) {
        this.storeValue = storeValue;
    }

    public String getVariableName() {
        return variableName;
    }

    public VariableActionItem(String variableName, GraphTargetItem storeValue, boolean definition) {
        this.variableName = variableName;
        this.storeValue = storeValue;
        this.definition = definition;
    }

    public boolean isDefinition() {
        return definition;
    }

    public void setBoxedValue(ActionItem it) {
        this.it = it;
        if (it != null) {
            this.precedence = it.getPrecedence();
        }
    }

    public ActionItem getBoxedValue() {
        return it;
    }

    public GraphTargetItem getStoreValue() {
        return storeValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (it == null) {
            return writer;
        }
        return it.appendTry(writer, localData);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (it == null) {
            return new ArrayList<>();
        }
        return it.toSource(localData, generator);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (it == null) {
            return new ArrayList<>();
        }
        return it.toSourceIgnoreReturnValue(localData, generator);
    }

    @Override
    public boolean hasReturnValue() {
        if (definition) {
            return false;
        }
        return true;
    }

    @Override
    public boolean needsSemicolon() {
        if (definition) {
            return true;
        }
        return false;
    }
}

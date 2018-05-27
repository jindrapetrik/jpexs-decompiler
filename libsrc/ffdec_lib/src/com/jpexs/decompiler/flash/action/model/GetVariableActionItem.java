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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class GetVariableActionItem extends ActionItem {

    public final GraphTargetItem name;

    private GraphTargetItem computedValue;

    private Object computedResult;

    private boolean computedCompiletime = false;

    private boolean computedVariableComputed = false;

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(name);
        return ret;
    }

    public GetVariableActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.name = value;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if ((name instanceof DirectValueActionItem) && (((DirectValueActionItem) name).isString()) && (IdentifiersDeobfuscation.isValidNameWithDot(false, ((DirectValueActionItem) name).toStringNoQuotes(localData), "this", "super")
                || IdentifiersDeobfuscation.isValidNameWithSlash(((DirectValueActionItem) name).toStringNoQuotes(localData), "this", "super"))) {
            HighlightData srcData = getSrcData();
            srcData.localName = name.toStringNoQuotes(localData);
            return stripQuotes(name, localData, writer);
        } else {
            writer.append("eval(");
            name.appendTry(writer, localData);
            return writer.append(")");
        }
        //IdentifiersDeobfuscation.appendObfuscatedIdentifier(((DirectValueActionItem) name).toStringNoQuotes(localData), writer);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(name.getNeededSources());
        return ret;
    }

    @Override
    public boolean isVariableComputed() {
        return true;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return false; //?
    }

    @Override
    public Object getResult() {
        return null;
    }

    public void setComputedValue(GraphTargetItem computedValue) {
        this.computedValue = computedValue;
        if (computedValue != null) {
            computedCompiletime = computedValue.isCompileTime();
            if (computedCompiletime) {
                computedResult = computedValue.getResult();
            }
            computedVariableComputed = computedValue.isVariableComputed();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GetVariableActionItem other = (GetVariableActionItem) obj;
        if (!Objects.equals(name, other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GetVariableActionItem other = (GetVariableActionItem) obj;
        if (this.name != other.name && (this.name == null || !this.name.valueEquals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, name, new ActionGetVariable());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

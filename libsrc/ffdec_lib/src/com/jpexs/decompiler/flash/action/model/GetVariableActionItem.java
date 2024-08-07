/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.helpers.CodeFormatting;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.StringBuilderTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Get variable.
 *
 * @author JPEXS
 */
public class GetVariableActionItem extends ActionItem {

    /**
     * Name
     */
    public final GraphTargetItem name;

    /**
     * Computed value
     */
    private GraphTargetItem computedValue;

    /**
     * Computed result
     */
    private Object computedResult;

    /**
     * Computed compile time
     */
    private boolean computedCompiletime = false;

    /**
     * Computed variable computed
     */
    private boolean computedVariableComputed = false;

    /**
     * Print obfuscated name
     */
    public boolean printObfuscatedName = false;

    @Override
    public String toString() {
        return name.toString();
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(name);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param value Value
     */
    public GetVariableActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.name = value;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (name instanceof DirectValueActionItem && printObfuscatedName) {
            HighlightData srcData = getSrcData();
            srcData.localName = name.toStringNoQuotes(localData);

            StringBuilder sb = new StringBuilder();
            StringBuilderTextWriter sbw = new StringBuilderTextWriter(new CodeFormatting(), sb);
            stripQuotes(name, localData, sbw);
            writer.append(IdentifiersDeobfuscation.printIdentifier(false, sb.toString()));
            return writer;
        }
        if ((name instanceof DirectValueActionItem) && (((DirectValueActionItem) name).isString()) && (printObfuscatedName || IdentifiersDeobfuscation.isValidNameWithDot(false, ((DirectValueActionItem) name).toStringNoQuotes(localData), "this", "super"))) {
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

    /**
     * Sets computed value.
     * @param computedValue Computed value
     */
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

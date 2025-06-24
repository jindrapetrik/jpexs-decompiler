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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SimpleValue;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Unresolved constant.
 *
 * @author JPEXS
 */
public class UnresolvedConstantActionItem extends ActionItem implements SimpleValue {

    /**
     * Computed register value
     */
    public GraphTargetItem computedRegValue;

    /**
     * Index
     */
    private int index;

    /**
     * Constructor.
     *
     * @param index Index
     */
    public UnresolvedConstantActionItem(int index) {
        this(null, null, 0, index);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param instructionPos Instruction position
     * @param index Index
     */
    public UnresolvedConstantActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, int instructionPos, int index) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.index = index;
        this.pos = instructionPos;
    }
    

    @Override
    public boolean isVariableComputed() {
        return (computedRegValue != null);
    }

    @Override
    public Object getResult() {
        return Undefined.INSTANCE;
    }

    @Override
    public boolean isSimpleValue() {
        return false;
    }

    @Override
    public String toStringNoQuotes(LocalData localData) {
        return "\u00A7\u00A7constant(" + index + ")";
    }

    /**
     * Gets the index.
     * @return Index
     */
    public int getIndex() {
        return index;
    }

    @Override
    public GraphTextWriter appendToNoQuotes(GraphTextWriter writer, LocalData localData) {
        return writer.append("\u00A7\u00A7constant(" + index + ")");
    }

    public String toStringNoH(ConstantPool constants) {
        return "\u00A7\u00A7constant(" + index + ")";
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        return writer.append("\u00A7\u00A7constant(" + index + ")");
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return true;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof UnresolvedConstantActionItem)) {
            return false;
        }
        final UnresolvedConstantActionItem other = (UnresolvedConstantActionItem) obj;
        if (!Objects.equals(index, other.index)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UnresolvedConstantActionItem other = (UnresolvedConstantActionItem) obj;
        if (!Objects.equals(this.index, other.index)) {
            return false;
        }
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        return toSourceMerge(localData, generator, new ActionPush(new ConstantIndex(index), charset));
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    /**
     * Checks if this is string.
     * @return True if this is string
     */
    public boolean isString() {
        return true;
    }

    /**
     * Gets as string.
     * @return String
     */
    public String getAsString() {
        if (!isString()) {
            return null;
        }
        return (String) getResult();
    }

    @Override
    public String toString() {
        return "" + getResult();
    }
}

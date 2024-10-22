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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf4.ActionMBStringExtract;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 * Multibyte string extract.
 *
 * @author JPEXS
 */
public class MBStringExtractActionItem extends ActionItem {

    /**
     * Index
     */
    public GraphTargetItem index;

    /**
     * Count
     */
    public GraphTargetItem count;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(value);
        visitor.visit(index);
        visitor.visit(count);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param value Value
     * @param index Index
     * @param count Count
     */
    public MBStringExtractActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value, GraphTargetItem index, GraphTargetItem count) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY, value);
        this.index = index;
        this.count = count;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("mbsubstring");
        writer.spaceBeforeCallParenthesis(3);
        writer.append("(");
        value.toString(writer, localData);
        writer.append(",");
        index.toString(writer, localData);
        writer.append(",");
        count.toString(writer, localData);
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        ret.addAll(index.getNeededSources());
        ret.addAll(count.getNeededSources());
        return ret;
    }

    @Override
    public Object getResult() {
        return getResult(count.getResult(), index.getResult(), value.getResult());
    }

    /**
     * Gets result.
     *
     * @param count Count
     * @param index Index
     * @param value Value
     * @return Result
     */
    public static String getResult(Object count, Object index, Object value) {
        String str = EcmaScript.toString(value);
        int idx = EcmaScript.toInt32(index);
        idx--; // index seems to be 1 based

        int cnt = EcmaScript.toInt32(count);

        /*if (idx < 0) {
         idx = str.length() + idx;
         }*/
        if (idx < 0) {
            idx = 0;
        } else if (idx > str.length()) {
            return "";
        }

        if (cnt < 0) {
            cnt = str.length();
        }

        int endIdx = Math.min(str.length(), idx + cnt);
        return str.substring(idx, endIdx);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, value, index, count, new ActionMBStringExtract());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.index);
        hash = 97 * hash + Objects.hashCode(this.count);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MBStringExtractActionItem other = (MBStringExtractActionItem) obj;
        if (!Objects.equals(this.index, other.index)) {
            return false;
        }
        if (!Objects.equals(this.count, other.count)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MBStringExtractActionItem other = (MBStringExtractActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.index, other.index)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.count, other.count)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.value, other.value)) {
            return false;
        }
        return true;
    }

}

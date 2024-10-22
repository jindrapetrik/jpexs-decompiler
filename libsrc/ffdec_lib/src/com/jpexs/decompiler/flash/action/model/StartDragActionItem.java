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
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionStartDrag;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.TrueItem;
import java.util.List;
import java.util.Objects;

/**
 * Start drag.
 *
 * @author JPEXS
 */
public class StartDragActionItem extends ActionItem {

    /**
     * Target
     */
    public GraphTargetItem target;

    /**
     * Lock center
     */
    public GraphTargetItem lockCenter;

    /**
     * Constrain
     */
    public GraphTargetItem constrain;

    /**
     * Y2
     */
    public GraphTargetItem y2;

    /**
     * X2
     */
    public GraphTargetItem x2;

    /**
     * Y1
     */
    public GraphTargetItem y1;

    /**
     * X1
     */
    public GraphTargetItem x1;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param target Target
     * @param lockCenter Lock center
     * @param constrain Constrain
     * @param x1 X1
     * @param y1 Y1
     * @param x2 X2
     * @param y2 Y2
     */
    public StartDragActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem target, GraphTargetItem lockCenter, GraphTargetItem constrain, GraphTargetItem x1, GraphTargetItem y1, GraphTargetItem x2, GraphTargetItem y2) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.target = target;
        this.lockCenter = lockCenter;
        this.constrain = constrain;
        this.y2 = y2;
        this.x2 = x2;
        this.y1 = y1;
        this.x1 = x1;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean hasConstrains = true;
        if ((constrain instanceof DirectValueActionItem) || (constrain instanceof TrueItem) || (constrain instanceof FalseItem)) {
            if (Double.compare(constrain.getResultAsNumber(), 0) == 0) {
                hasConstrains = false;
            }
        }
        writer.append("startDrag");
        writer.spaceBeforeCallParenthesis(2);
        writer.append("(");
        target.toString(writer, localData);
        writer.append(",");
        lockCenter.toString(writer, localData);
        if (hasConstrains) {
            writer.append(",");
            x1.toString(writer, localData);
            writer.append(",");
            y1.toString(writer, localData);
            writer.append(",");
            x2.toString(writer, localData);
            writer.append(",");
            y2.toString(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(target.getNeededSources());
        ret.addAll(constrain.getNeededSources());
        ret.addAll(x1.getNeededSources());
        ret.addAll(x2.getNeededSources());
        ret.addAll(y1.getNeededSources());
        ret.addAll(y2.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, false);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, true);
    }

    private List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {

        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();

        boolean hasConstrains = true;
        if (constrain instanceof DirectValueActionItem) {
            if (Double.compare(constrain.getResultAsNumber(), 0) == 0) {
                hasConstrains = false;
            }
        }
        if (hasConstrains) {
            return toSourceMerge(localData, generator, x1, y1, x2, y2, constrain, lockCenter, target, new ActionStartDrag(), needsReturn ? new ActionPush(new Object[]{Undefined.INSTANCE, Undefined.INSTANCE}, charset) : null);
        } else {
            return toSourceMerge(localData, generator, constrain, lockCenter, target, new ActionStartDrag(), needsReturn ? new ActionPush(new Object[]{Undefined.INSTANCE, Undefined.INSTANCE}, charset) : null);
        }
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.target);
        hash = 29 * hash + Objects.hashCode(this.lockCenter);
        hash = 29 * hash + Objects.hashCode(this.constrain);
        hash = 29 * hash + Objects.hashCode(this.y2);
        hash = 29 * hash + Objects.hashCode(this.x2);
        hash = 29 * hash + Objects.hashCode(this.y1);
        hash = 29 * hash + Objects.hashCode(this.x1);
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
        final StartDragActionItem other = (StartDragActionItem) obj;
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        if (!Objects.equals(this.lockCenter, other.lockCenter)) {
            return false;
        }
        if (!Objects.equals(this.constrain, other.constrain)) {
            return false;
        }
        if (!Objects.equals(this.y2, other.y2)) {
            return false;
        }
        if (!Objects.equals(this.x2, other.x2)) {
            return false;
        }
        if (!Objects.equals(this.y1, other.y1)) {
            return false;
        }
        if (!Objects.equals(this.x1, other.x1)) {
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
        final StartDragActionItem other = (StartDragActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.target, other.target)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.lockCenter, other.lockCenter)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.constrain, other.constrain)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.y2, other.y2)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.x2, other.x2)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.y1, other.y1)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.x1, other.x1)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}

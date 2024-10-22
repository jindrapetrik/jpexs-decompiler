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
import com.jpexs.decompiler.flash.action.swf3.ActionGoToLabel;
import com.jpexs.decompiler.flash.action.swf3.ActionGotoFrame;
import com.jpexs.decompiler.flash.action.swf3.ActionPlay;
import com.jpexs.decompiler.flash.action.swf4.ActionGotoFrame2;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.ecma.Undefined;
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
 * Goto frame, v2.
 *
 * @author JPEXS
 */
public class GotoFrame2ActionItem extends ActionItem {

    /**
     * Frame
     */
    public GraphTargetItem frame;

    /**
     * Scene bias flag
     */
    public boolean sceneBiasFlag;

    /**
     * Play flag
     */
    public boolean playFlag;

    /**
     * Scene bias
     */
    public int sceneBias;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(frame);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param frame Frame
     * @param sceneBiasFlag Scene bias flag
     * @param playFlag Play flag
     * @param sceneBias Scene bias
     */
    public GotoFrame2ActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem frame, boolean sceneBiasFlag, boolean playFlag, int sceneBias) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.frame = frame;
        this.sceneBiasFlag = sceneBiasFlag;
        this.playFlag = playFlag;
        this.sceneBias = sceneBias;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        String prefix = "gotoAndStop";
        if (playFlag) {
            prefix = "gotoAndPlay";
        }
        writer.append(prefix);
        writer.spaceBeforeCallParenthesis(1);
        writer.append("(");
        if (sceneBiasFlag) {
            writer.append(sceneBias + ", ");
        }
        frame.toString(writer, localData);

        return writer.append(")");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(frame.getNeededSources());
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
        if (!sceneBiasFlag && (frame instanceof DirectValueActionItem) && (((DirectValueActionItem) frame).isString())) {
            return toSourceMerge(localData, generator, new ActionGoToLabel(((DirectValueActionItem) frame).getAsString(), charset), playFlag ? new ActionPlay() : null, needsReturn ? new ActionPush(new Object[]{Undefined.INSTANCE, Undefined.INSTANCE}, charset) : null);
        } else if (!sceneBiasFlag && (frame instanceof DirectValueActionItem) && (((DirectValueActionItem) frame).value instanceof Long)) {
            return toSourceMerge(localData, generator, new ActionGotoFrame((int) ((long) (Long) ((DirectValueActionItem) frame).value) - 1, charset), playFlag ? new ActionPlay() : null, needsReturn ? new ActionPush(new Object[]{Undefined.INSTANCE, Undefined.INSTANCE}, charset) : null);
        } else {
            return toSourceMerge(localData, generator, frame, new ActionGotoFrame2(playFlag, sceneBiasFlag, sceneBias, charset), needsReturn ? new ActionPush(new Object[]{Undefined.INSTANCE, Undefined.INSTANCE}, charset) : null);
        }
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.frame);
        hash = 67 * hash + (this.sceneBiasFlag ? 1 : 0);
        hash = 67 * hash + (this.playFlag ? 1 : 0);
        hash = 67 * hash + this.sceneBias;
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
        final GotoFrame2ActionItem other = (GotoFrame2ActionItem) obj;
        if (this.sceneBiasFlag != other.sceneBiasFlag) {
            return false;
        }
        if (this.playFlag != other.playFlag) {
            return false;
        }
        if (this.sceneBias != other.sceneBias) {
            return false;
        }
        if (!Objects.equals(this.frame, other.frame)) {
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
        final GotoFrame2ActionItem other = (GotoFrame2ActionItem) obj;
        if (this.sceneBiasFlag != other.sceneBiasFlag) {
            return false;
        }
        if (this.playFlag != other.playFlag) {
            return false;
        }
        if (this.sceneBias != other.sceneBias) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.frame, other.frame)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}

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
import com.jpexs.decompiler.flash.action.swf3.ActionGotoFrame;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 * Goto frame.
 *
 * @author JPEXS
 */
public class GotoFrameActionItem extends ActionItem {

    /**
     * Frame number
     */
    public int frame;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param frame Frame number
     */
    public GotoFrameActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, int frame) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.frame = frame;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        writer.append("gotoAndStop");
        writer.spaceBeforeCallParenthesis(1);
        writer.append("(");
        writer.append(frame + 1);
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        return toSourceMerge(localData, generator, new ActionGotoFrame(frame, charset));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + this.frame;
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
        final GotoFrameActionItem other = (GotoFrameActionItem) obj;
        if (this.frame != other.frame) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}

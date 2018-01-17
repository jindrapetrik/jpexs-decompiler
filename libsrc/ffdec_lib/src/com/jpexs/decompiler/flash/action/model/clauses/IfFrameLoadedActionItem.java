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
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionWaitForFrame2;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IfFrameLoadedActionItem extends ActionItem implements Block {

    private final List<GraphTargetItem> actions;

    private final GraphTargetItem frame;

    public IfFrameLoadedActionItem(GraphTargetItem frame, List<GraphTargetItem> actions, GraphSourceItem instruction, GraphSourceItem lineStartIns) {
        super(instruction, lineStartIns, NOPRECEDENCE);
        this.actions = actions;
        this.frame = frame;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("ifFrameLoaded");
        writer.spaceBeforeCallParenthesies(1);
        writer.append("(");
        frame.toString(writer, localData);
        writer.append(")").startBlock();
        Graph.graphToString(actions, writer, localData);
        return writer.endBlock();
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<ContinueItem> getContinues() {
        return new ArrayList<>();
    }

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (actions != null) {
            ret.add(actions);
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphSourceItem> body = generator.generate(localData, actions);
        return toSourceMerge(localData, generator, frame, new ActionWaitForFrame2(body.size()), body);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf4.ActionCloneSprite;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CloneSpriteActionItem extends ActionItem {

    public GraphTargetItem source;

    public GraphTargetItem target;

    public GraphTargetItem depth;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(target);
        ret.add(source);
        ret.add(depth);
        return ret;
    }

    public CloneSpriteActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem source, GraphTargetItem target, GraphTargetItem depth) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.source = source;
        this.target = target;
        this.depth = depth;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("duplicateMovieClip");
        writer.spaceBeforeCallParenthesies(3);
        writer.append("(");
        target.toString(writer, localData);
        writer.append(",");
        source.toString(writer, localData);
        writer.append(",");
        depth.toString(writer, localData);
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(source.getNeededSources());
        ret.addAll(target.getNeededSources());
        ret.addAll(depth.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, source, target, depth, new ActionCloneSprite());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

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
import com.jpexs.decompiler.flash.action.swf4.ActionCloneSprite;
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
 * Clone sprite.
 *
 * @author JPEXS
 */
public class CloneSpriteActionItem extends ActionItem {

    /**
     * Source
     */
    public GraphTargetItem source;

    /**
     * Target
     */
    public GraphTargetItem target;

    /**
     * Depth
     */
    public GraphTargetItem depth;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(source);
        visitor.visit(target);
        visitor.visit(depth);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param source Source
     * @param target Target
     * @param depth Depth
     */
    public CloneSpriteActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem source, GraphTargetItem target, GraphTargetItem depth) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.source = source;
        this.target = target;
        this.depth = depth;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("duplicateMovieClip");
        writer.spaceBeforeCallParenthesis(3);
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
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();

        return toSourceMerge(localData, generator, source, target, depth, new ActionCloneSprite(), new ActionPush(new Object[]{Undefined.INSTANCE, Undefined.INSTANCE}, charset));
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, source, target, depth, new ActionCloneSprite());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.source);
        hash = 17 * hash + Objects.hashCode(this.target);
        hash = 17 * hash + Objects.hashCode(this.depth);
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
        final CloneSpriteActionItem other = (CloneSpriteActionItem) obj;
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        if (!Objects.equals(this.depth, other.depth)) {
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
        final CloneSpriteActionItem other = (CloneSpriteActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.source, other.source)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.target, other.target)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.depth, other.depth)) {
            return false;
        }
        return true;
    }

}

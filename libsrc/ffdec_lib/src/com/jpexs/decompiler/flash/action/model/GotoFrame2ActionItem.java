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
import com.jpexs.decompiler.flash.action.swf3.ActionGotoFrame;
import com.jpexs.decompiler.flash.action.swf3.ActionPlay;
import com.jpexs.decompiler.flash.action.swf4.ActionGotoFrame2;
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
public class GotoFrame2ActionItem extends ActionItem {

    public GraphTargetItem frame;

    public boolean sceneBiasFlag;

    public boolean playFlag;

    public int sceneBias;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(frame);
        return ret;
    }

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
        writer.spaceBeforeCallParenthesies(1);
        writer.append("(");
        frame.toString(writer, localData);
        writer.append((sceneBiasFlag ? "," + sceneBias : ""));
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(frame.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (!sceneBiasFlag && (frame instanceof DirectValueActionItem) && (((DirectValueActionItem) frame).value instanceof Long)) {
            return toSourceMerge(localData, generator, new ActionGotoFrame((int) ((long) (Long) ((DirectValueActionItem) frame).value) - 1), playFlag ? new ActionPlay() : null);
        } else {
            return toSourceMerge(localData, generator, frame, new ActionGotoFrame2(playFlag, sceneBiasFlag, sceneBias));
        }
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

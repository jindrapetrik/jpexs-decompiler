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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.LoopWithType;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ForItem extends LoopItem implements Block {

    public List<GraphTargetItem> firstCommands;

    public GraphTargetItem expression;

    public List<GraphTargetItem> finalCommands;

    public List<GraphTargetItem> commands;

    private boolean labelUsed;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (firstCommands != null) {
            ret.add(firstCommands);
        }
        if (commands != null) {
            ret.add(commands);
        }
        if (finalCommands != null) {
            ret.add(finalCommands);
        }
        return ret;
    }

    public ForItem(GraphSourceItem src, GraphSourceItem lineStartIns, Loop loop, List<GraphTargetItem> firstCommands, GraphTargetItem expression, List<GraphTargetItem> finalCommands, List<GraphTargetItem> commands) {
        super(src, lineStartIns, loop);
        this.firstCommands = firstCommands;
        this.expression = expression;
        this.finalCommands = finalCommands;
        this.commands = commands;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (writer instanceof NulWriter) {
            ((NulWriter) writer).startLoop(loop.id, LoopWithType.LOOP_TYPE_LOOP);
        }
        if (labelUsed) {
            writer.append("loop").append(loop.id).append(":").newLine();
        }
        writer.append("for");
        if (writer.getFormatting().spaceBeforeParenthesesForParentheses) {
            writer.append(" ");
        }
        writer.append("(");
        int p = 0;
        for (int i = 0; i < firstCommands.size(); i++) {
            if (firstCommands.get(i).isEmpty()) {
                continue;
            }

            if (p > 0) {
                writer.append(",");
            }
            firstCommands.get(i).toString(writer, localData);
            p++;
        }
        writer.append("; ");
        expression.toStringBoolean(writer, localData);
        writer.append("; ");
        p = 0;
        for (int i = 0; i < finalCommands.size(); i++) {
            if (finalCommands.get(i).isEmpty()) {
                continue;
            }
            if (p > 0) {
                writer.append(",");
            }
            finalCommands.get(i).toString(writer, localData);
            p++;
        }
        writer.append(")");
        appendBlock(expression, writer, localData, commands);
        if (writer instanceof NulWriter) {
            LoopWithType loopOjb = ((NulWriter) writer).endLoop(loop.id);
            labelUsed = loopOjb.used;
        }
        return writer;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        for (GraphTargetItem ti : commands) {
            if (ti instanceof ContinueItem) {
                ret.add((ContinueItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }
}

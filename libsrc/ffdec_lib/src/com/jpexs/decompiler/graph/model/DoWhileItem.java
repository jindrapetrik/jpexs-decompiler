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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.LoopWithType;
import com.jpexs.decompiler.flash.helpers.NulWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Do-while loop.
 *
 * @author JPEXS
 */
public class DoWhileItem extends LoopItem implements Block {

    /**
     * Commands
     */
    public List<GraphTargetItem> commands;

    /**
     * Expression
     */
    public List<GraphTargetItem> expression;

    /**
     * Label used
     */
    private boolean labelUsed;

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (expression != null) {
            ret.add(expression);
        }
        if (commands != null) {
            ret.add(commands);
        }
        return ret;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        if (commands != null) {
            visitor.visitAll(commands);
        }
        if (expression != null) {
            visitor.visitAll(expression);
        }
    }

    @Override
    public void visitNoBlock(GraphTargetVisitorInterface visitor) {
        if (expression != null) {
            visitor.visitAll(expression);
        }
    }

    /**
     * Constructor.
     *
     * @param src Source
     * @param lineStartIns Line start instruction
     * @param loop Loop
     * @param commands Commands
     * @param expression Expression
     */
    public DoWhileItem(GraphTargetDialect dialect, GraphSourceItem src, GraphSourceItem lineStartIns, Loop loop, List<GraphTargetItem> commands, List<GraphTargetItem> expression) {
        super(dialect, src, lineStartIns, loop);
        this.expression = expression;
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
        writer.append("do");
        appendBlock(null, writer, localData, commands);
        writer.newLine();
        writer.append("while");
        if (writer.getFormatting().spaceBeforeParenthesesWhileParentheses) {
            writer.append(" ");
        }
        writer.append("(");

        boolean first = true;
        for (int i = 0; i < expression.size(); i++) {
            if (expression.get(i).isEmpty()) {
                continue;
            }
            if (!first) {
                writer.append(", ");
            }
            first = false;
            if (i == expression.size() - 1) {
                expression.get(i).toStringBoolean(writer, localData);
            } else {
                expression.get(i).toString(writer, localData);
            }

        }

        writer.append(");").newLine();
        if (writer instanceof NulWriter) {
            LoopWithType loopOjb = ((NulWriter) writer).endLoop(loop.id);
            labelUsed = loopOjb.used;
        }
        return writer;
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

    @Override
    public boolean hasBaseBody() {
        return true;
    }

    @Override
    public List<GraphTargetItem> getBaseBodyCommands() {
        return commands;
    }
}

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
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class IfItem extends GraphTargetItem implements Block {

    public GraphTargetItem expression;

    public List<GraphTargetItem> onTrue;

    public List<GraphTargetItem> onFalse;

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(expression)) {
            return false;
        }
        dependencies.add(expression);
        return expression.isCompileTime(dependencies);
    }

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (onTrue != null) {
            ret.add(onTrue);
        }
        if (onFalse != null) {
            ret.add(onFalse);
        }
        return ret;
    }

    public IfItem(GraphSourceItem src, GraphSourceItem lineStartIns, GraphTargetItem expression, List<GraphTargetItem> onTrue, List<GraphTargetItem> onFalse) {
        super(src, lineStartIns, NOPRECEDENCE);
        this.expression = expression;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        GraphTargetItem expr = expression;
        List<GraphTargetItem> ifBranch = onTrue;
        List<GraphTargetItem> elseBranch = onFalse;
        if (onTrue.isEmpty()) {
            if (onFalse.isEmpty()) {
                if (expr instanceof NotItem) {
                    expr = ((NotItem) expr).getOriginal();
                }
            } else {
                expr = expr.invert(null);
                ifBranch = onFalse;
                elseBranch = onTrue;
            }
        }
        writer.append("if");
        if (writer.getFormatting().spaceBeforeParenthesesIfParentheses) {
            writer.append(" ");
        }
        writer.append("(");
        expr.toStringBoolean(writer, localData);
        writer.append(")");
        appendBlock(expr, writer, localData, ifBranch);
        if (elseBranch.size() > 0) {
            boolean elseIf = elseBranch.size() == 1 && (elseBranch.get(0) instanceof IfItem);
            if (writer.getFormatting().beginBlockOnNewLine) {
                writer.newLine();
            } else {
                writer.append(" ");
            }
            writer.append("else");
            if (!elseIf) {
                appendBlock(expr, writer, localData, elseBranch);
            } else {
                writer.append(" ");
                elseBranch.get(0).toStringSemicoloned(writer, localData);
            }

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
        for (GraphTargetItem ti : onTrue) {
            if (ti instanceof ContinueItem) {
                ret.add((ContinueItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        for (GraphTargetItem ti : onFalse) {
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

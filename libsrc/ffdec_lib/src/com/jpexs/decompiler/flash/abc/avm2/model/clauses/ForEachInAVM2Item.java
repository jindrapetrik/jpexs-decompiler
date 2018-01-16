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
package com.jpexs.decompiler.flash.abc.avm2.model.clauses;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.InAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
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
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.LoopItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ForEachInAVM2Item extends LoopItem implements Block {

    public InAVM2Item expression;

    public List<GraphTargetItem> commands;

    private boolean labelUsed;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (commands != null) {
            ret.add(commands);
        }
        return ret;
    }

    public ForEachInAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, Loop loop, InAVM2Item expression, List<GraphTargetItem> commands) {
        super(instruction, lineStartIns, loop);

        /*
         Following was commented out:

         The code should fix following:
         for each (var a in col)
         {
         var b = a;      //a is temp reg
         trace(b);
         }

         but fails for following:
         for each (var a in col)
         {
         c[a] = a;
         }


         */
 /*
         if (!commands.isEmpty()) {
         GraphTargetItem firstAssign = commands.get(0);
         if (firstAssign instanceof SetTypeAVM2Item) {
         if (expression.object instanceof LocalRegAVM2Item) {
         if (((SetTypeAVM2Item) firstAssign).getValue().getNotCoerced() instanceof LocalRegAVM2Item) {
         if (((LocalRegAVM2Item) ((SetTypeAVM2Item) firstAssign).getValue().getNotCoerced()).regIndex == ((LocalRegAVM2Item) expression.object).regIndex) {
         commands.remove(0);
         expression.object = ((SetTypeAVM2Item) firstAssign).getObject();
         }
         }

         }
         //locAssign.
         }
         }*/
        this.expression = expression;
        this.commands = commands;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (writer instanceof NulWriter) {
            ((NulWriter) writer).startLoop(loop.id, LoopWithType.LOOP_TYPE_LOOP);
        }
        if (labelUsed) {
            writer.append("loop").append(loop.id).append(":").newLine();
        }
        writer.append("for each");
        if (writer.getFormatting().spaceBeforeParenthesesForEachParentheses) {
            writer.append(" ");
        }
        writer.append("(");
        expression.toString(writer, localData);
        writer.append(")");
        appendBlock(expression, writer, localData, commands);
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
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return ((AVM2SourceGenerator) generator).generate(localData, this);
    }
}

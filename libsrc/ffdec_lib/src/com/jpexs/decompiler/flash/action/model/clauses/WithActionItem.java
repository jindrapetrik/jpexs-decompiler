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
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf5.ActionWith;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 * With statement.
 *
 * @author JPEXS
 */
public class WithActionItem extends ActionItem {

    /**
     * Scope
     */
    public GraphTargetItem scope;

    /**
     * Items
     */
    public List<GraphTargetItem> items;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param scope Scope
     * @param items Items
     */
    public WithActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem scope, List<GraphTargetItem> items) {
        super(instruction, lineStartIns, NOPRECEDENCE);
        this.scope = scope;
        this.items = items;
    }

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param scope Scope
     */
    public WithActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, ActionItem scope) {
        super(instruction, lineStartIns, NOPRECEDENCE);
        this.scope = scope;
        this.items = new ArrayList<>();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("with");
        if (writer.getFormatting().spaceBeforeParenthesesWithParentheses) {
            writer.append(" ");
        }
        writer.append("(");
        scope.toString(writer, localData);
        writer.append(")");
        appendBlock(null, writer, localData, items);

        return writer;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        List<GraphSourceItem> data = generator.generate(localData, items);
        List<Action> dataA = new ArrayList<>();
        for (GraphSourceItem s : data) {
            if (s instanceof Action) {
                dataA.add((Action) s);
            }
        }
        int codeLen = Action.actionsToBytes(dataA, false, SWF.DEFAULT_VERSION).length;
        return toSourceMerge(localData, generator, scope, new ActionWith(codeLen, charset), data);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

}

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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AssignableAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class WithAVM2Item extends AVM2Item {

    public GraphTargetItem scope;

    public List<GraphTargetItem> items;

    public List<AssignableAVM2Item> subvariables = new ArrayList<>();

    public WithAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem scope, List<GraphTargetItem> items) {
        super(instruction, lineStartIns, NOPRECEDENCE);
        this.scope = scope;
        this.items = items;
    }

    public WithAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem scope) {
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
        writer.append(")").startBlock();
        //NOTE: endBlock is added with WithEndAVM2Item
        return writer;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return ((AVM2SourceGenerator) generator).generate(localData, this);
    }
}

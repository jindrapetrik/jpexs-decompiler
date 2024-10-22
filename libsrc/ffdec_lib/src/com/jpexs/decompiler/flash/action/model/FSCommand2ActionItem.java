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
import com.jpexs.decompiler.flash.action.flashlite.ActionFSCommand2;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * FSCommand2.
 *
 * @author JPEXS
 */
public class FSCommand2ActionItem extends ActionItem {

    /**
     * Arguments
     */
    public List<GraphTargetItem> arguments;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visitAll(arguments);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param command Command
     * @param arguments Arguments
     */
    public FSCommand2ActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, List<GraphTargetItem> arguments) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.arguments = arguments;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("fscommand2");
        writer.spaceBeforeCallParenthesis(arguments.size());
        writer.append("(");
        for (int t = 0; t < arguments.size(); t++) {
            if (t > 0) {
                writer.append(",");
            }
            arguments.get(t).toString(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        for (GraphTargetItem ti : arguments) {
            ret.addAll(ti.getNeededSources());
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {

        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();

        List<GraphSourceItem> ret = new ArrayList<>();
        for (GraphTargetItem a : arguments) {
            ret.addAll(a.toSource(localData, generator));
        }
        ret.add(new ActionPush((Long) (long) arguments.size(), charset));
        ret.add(new ActionFSCommand2(charset));
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return true; //FIXME ?
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.arguments);
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
        final FSCommand2ActionItem other = (FSCommand2ActionItem) obj;
        if (!Objects.equals(this.arguments, other.arguments)) {
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
        final FSCommand2ActionItem other = (FSCommand2ActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.arguments, other.arguments)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}

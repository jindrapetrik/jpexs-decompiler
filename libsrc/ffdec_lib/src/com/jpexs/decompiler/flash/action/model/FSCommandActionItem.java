/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.model.operations.StringAddActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf1.ActionGetURL;
import com.jpexs.decompiler.flash.action.swf4.ActionGetURL2;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 * FSCommand.
 *
 * @author JPEXS
 */
public class FSCommandActionItem extends ActionItem {

    /**
     * Command
     */
    private final GraphTargetItem command;

    /**
     * Parameter
     */
    private final GraphTargetItem parameter;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param command Command
     * @param parameter Parameter
     */
    public FSCommandActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem command, GraphTargetItem parameter) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.command = command;
        this.parameter = parameter;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("fscommand");
        writer.spaceBeforeCallParenthesis(1);
        writer.append("(");
        command.appendTry(writer, localData);
        if (parameter != null) {
            writer.append(",");
            parameter.appendTry(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, false);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, true);
    }

    private List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        String charset = asGenerator.getCharset();
        if ((command instanceof DirectValueActionItem)
                && ((DirectValueActionItem) command).isString()
                && (
                parameter == null
                || ((parameter instanceof DirectValueActionItem) && ((DirectValueActionItem) parameter).isString())
                )) {
            return toSourceMerge(localData, generator, new ActionGetURL("FSCommand:" + ((DirectValueActionItem) command).getAsString(), parameter == null ? "" : ((DirectValueActionItem) parameter).getAsString(), charset));
        }
        return toSourceMerge(localData, generator, new StringAddActionItem(null, null, asGenerator.pushConstTargetItem("FSCommand:"), command), parameter == null ? asGenerator.pushConstTargetItem("") : parameter, new ActionGetURL2(1/*GET*/, false, false, charset), needsReturn ? new ActionPush(new Object[]{Undefined.INSTANCE, Undefined.INSTANCE}, charset) : null);
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.command);
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
        final FSCommandActionItem other = (FSCommandActionItem) obj;
        if (!Objects.equals(this.command, other.command)) {
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
        final FSCommandActionItem other = (FSCommandActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(this.command, other.command)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}

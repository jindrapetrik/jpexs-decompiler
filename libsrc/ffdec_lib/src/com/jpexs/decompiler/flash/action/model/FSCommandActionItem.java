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
import com.jpexs.decompiler.flash.action.model.operations.AddActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf3.ActionGetURL;
import com.jpexs.decompiler.flash.action.swf4.ActionGetURL2;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class FSCommandActionItem extends ActionItem {

    private final GraphTargetItem command;

    public FSCommandActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem command) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.command = command;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("fscommand");
        writer.spaceBeforeCallParenthesies(1);
        writer.append("(");
        command.appendTry(writer, localData);
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        ActionSourceGenerator asg = (ActionSourceGenerator) generator;
        if ((command instanceof DirectValueActionItem) && ((DirectValueActionItem) command).isString()) {
            return toSourceMerge(localData, generator, new ActionGetURL("FSCommand:" + ((DirectValueActionItem) command).getAsString(), ""));
        }
        return toSourceMerge(localData, generator, new AddActionItem(null, null, asg.pushConstTargetItem("FSCommand:"), command, true), asg.pushConstTargetItem(""), new ActionGetURL2(1/*GET*/, false, false));
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

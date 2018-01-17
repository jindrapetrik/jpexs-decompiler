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
import com.jpexs.decompiler.flash.action.swf5.ActionNewMethod;
import com.jpexs.decompiler.flash.ecma.Undefined;
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
public class NewMethodActionItem extends ActionItem {

    public GraphTargetItem methodName;

    public GraphTargetItem scriptObject;

    public List<GraphTargetItem> arguments;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(scriptObject);
        ret.addAll(arguments);
        return ret;
    }

    public NewMethodActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem scriptObject, GraphTargetItem methodName, List<GraphTargetItem> arguments) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.methodName = methodName;
        this.arguments = arguments;
        this.scriptObject = scriptObject;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean blankMethod = false;
        if (methodName instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) methodName).value == Undefined.INSTANCE) {
                blankMethod = true;
            } else if (((DirectValueActionItem) methodName).value instanceof String) {
                if (((DirectValueActionItem) methodName).value.equals("")) {
                    blankMethod = true;
                }
            }
        }
        if (!blankMethod) {
            writer.append("new ");
        }
        scriptObject.toString(writer, localData);
        if (!blankMethod) {
            writer.append(".");
            if (methodName instanceof DirectValueActionItem) {
                if (((DirectValueActionItem) methodName).value == Undefined.INSTANCE) {
                } else if (((DirectValueActionItem) methodName).value instanceof String) {
                    ((DirectValueActionItem) methodName).toStringNoQuotes(writer, localData);
                } else {
                    methodName.toString(writer, localData);
                }
            } else {
                methodName.toString(writer, localData);
            }
        }
        writer.spaceBeforeCallParenthesies(arguments.size());
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
        ret.addAll(methodName.getNeededSources());
        ret.addAll(scriptObject.getNeededSources());
        for (GraphTargetItem ti : arguments) {
            ret.addAll(ti.getNeededSources());
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, toSourceCall(localData, generator, arguments), scriptObject, methodName, new ActionNewMethod());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

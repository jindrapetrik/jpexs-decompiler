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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf5.ActionCallMethod;
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
public class CallMethodActionItem extends ActionItem {

    public GraphTargetItem methodName;

    public GraphTargetItem scriptObject;

    public List<GraphTargetItem> arguments;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.addAll(arguments);
        ret.add(scriptObject);
        return ret;
    }

    public CallMethodActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem scriptObject, GraphTargetItem methodName, List<GraphTargetItem> arguments) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.methodName = methodName;
        this.arguments = arguments;
        this.scriptObject = scriptObject;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (methodName instanceof DirectValueActionItem) {
            boolean blankMethod = false;

            if (((DirectValueActionItem) methodName).value == Undefined.INSTANCE) {
                blankMethod = true;
            }
            if (((DirectValueActionItem) methodName).value instanceof String) {
                if (((DirectValueActionItem) methodName).value.equals("")) {
                    blankMethod = true;
                }
            }

            if (!blankMethod) {
                if (scriptObject.getPrecedence() > this.precedence) {
                    writer.append("(");
                    scriptObject.toString(writer, localData);
                    writer.append(")");
                } else {
                    scriptObject.toString(writer, localData);
                }
                if (IdentifiersDeobfuscation.isValidName(false, methodName.toStringNoQuotes(localData))) {
                    writer.append(".");
                    methodName.toStringNoQuotes(writer, localData);
                } else {
                    writer.append("[");
                    methodName.toString(writer, localData);
                    writer.append("]");
                }
                //writer.append(IdentifiersDeobfuscation.printIdentifier(false, methodName.toStringNoQuotes(localData)));
            } else {
                scriptObject.toString(writer, localData);
            }
        } else {
            if (scriptObject.getPrecedence() > this.precedence) {
                writer.append("(");
                scriptObject.toString(writer, localData);
                writer.append(")");
            } else {
                scriptObject.toString(writer, localData);
            }

            writer.append("[");
            methodName.appendTry(writer, localData);
            writer.append("]");
        }

        writer.spaceBeforeCallParenthesies(arguments.size());
        writer.append("(");
        for (int t = 0; t < arguments.size(); t++) {
            if (t > 0) {
                writer.append(",");
            }
            arguments.get(t).toStringNL(writer, localData);
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
        return toSourceMerge(localData, generator, toSourceCall(localData, generator, arguments), scriptObject, methodName, new ActionCallMethod());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

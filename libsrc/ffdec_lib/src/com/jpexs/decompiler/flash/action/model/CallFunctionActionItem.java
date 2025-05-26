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

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionCallMethod;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Call function.
 *
 * @author JPEXS
 */
public class CallFunctionActionItem extends ActionItem {

    /**
     * Function name.
     */
    public final GraphTargetItem functionName;

    /**
     * Arguments.
     */
    public final List<GraphTargetItem> arguments;

    /**
     * Calculated function.
     */
    public GraphTargetItem calculatedFunction;

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visitAll(arguments);
    }

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param functionName Function name
     * @param arguments Arguments
     */
    public CallFunctionActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem functionName, List<GraphTargetItem> arguments) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {

        HighlightData srcData = getSrcData();
        srcData.localName = functionName.toStringNoQuotes(localData);

        if (functionName instanceof DirectValueActionItem) {
            if (!IdentifiersDeobfuscation.isValidName(false, (functionName).toStringNoQuotes(localData))) {
                functionName.toString(writer, localData);
            } else {
                functionName.toStringNoQuotes(writer, localData);
            }
        } else {
            if (functionName.getPrecedence() > getPrecedence()) {
                writer.append("(");
            }
            functionName.appendTry(writer, localData);
            if (functionName.getPrecedence() > getPrecedence()) {
                writer.append(")");
            }
        }
        writer.spaceBeforeCallParenthesis(arguments.size());
        writer.append("(");
        for (int t = 0; t < arguments.size(); t++) {
            if (t > 0) {
                writer.allowWrapHere().append(",");
            }
            arguments.get(t).toStringNL(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(functionName.getNeededSources());
        for (GraphTargetItem ti : arguments) {
            ret.addAll(ti.getNeededSources());
        }
        return ret;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (calculatedFunction == null) {
            return false;
        }
        if (dependencies.contains(calculatedFunction)) {
            return false;
        }
        dependencies.add(calculatedFunction);
        return calculatedFunction.isCompileTime(dependencies);
    }

    @Override
    public Object getResult() {
        if (calculatedFunction == null) {
            return null;
        }
        return calculatedFunction.getResult();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(functionName);
        hash = 37 * hash + Objects.hashCode(arguments);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CallFunctionActionItem other = (CallFunctionActionItem) obj;
        if (!Objects.equals(functionName, other.functionName)) {
            return false;
        }
        if (!Objects.equals(this.arguments, other.arguments)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean valueEquals(GraphTargetItem obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CallFunctionActionItem other = (CallFunctionActionItem) obj;
        if (!GraphTargetItem.objectsValueEquals(functionName, other.functionName)) {
            return false;
        }
        if (!GraphTargetItem.objectsValueEquals(this.arguments, other.arguments)) {
            return false;
        }
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {

        if (functionName instanceof VariableActionItem) {
            VariableActionItem varItem = (VariableActionItem) functionName;
            if (varItem.getBoxedValue() instanceof DirectValueActionItem) {
                if (((DirectValueActionItem) varItem.getBoxedValue()).value instanceof RegisterNumber) {
                    return toSourceMerge(localData, generator, toSourceCall(localData, generator, arguments), varItem.getBoxedValue(), new DirectValueActionItem(Undefined.INSTANCE), new ActionCallMethod());
                }
            }
            String varName = varItem.getVariableName();
            ActionSourceGenerator asg = (ActionSourceGenerator) generator;
            return toSourceMerge(localData, generator, toSourceCall(localData, generator, arguments), asg.pushConst(varName), new ActionCallFunction());
        }

        return toSourceMerge(localData, generator, toSourceCall(localData, generator, arguments), functionName, new ActionCallFunction());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

}

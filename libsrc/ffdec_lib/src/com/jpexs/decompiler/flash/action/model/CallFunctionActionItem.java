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
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class CallFunctionActionItem extends ActionItem {

    public final GraphTargetItem functionName;

    public final List<GraphTargetItem> arguments;

    public GraphTargetItem calculatedFunction;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        return arguments;
    }

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
                writer.append("eval(");
                functionName.toString(writer, localData);
                writer.append(")");
            } else {
                functionName.toStringNoQuotes(writer, localData);
            }
            //writer.append(IdentifiersDeobfuscation.printIdentifier(false, (functionName).toStringNoQuotes(localData)));
        } else {
            functionName.appendTry(writer, localData);
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
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, toSourceCall(localData, generator, arguments), functionName, new ActionCallFunction());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

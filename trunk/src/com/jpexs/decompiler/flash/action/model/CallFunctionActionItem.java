/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Set;

public class CallFunctionActionItem extends ActionItem {

    public GraphTargetItem functionName;
    public List<GraphTargetItem> arguments;
    public GraphTargetItem calculatedFunction;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        return arguments;
    }

    public CallFunctionActionItem(GraphSourceItem instruction, GraphTargetItem functionName, List<GraphTargetItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    protected GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        String paramStr = "";
        stripQuotes(functionName, localData, writer);
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
        hash = 37 * hash + (this.functionName != null ? this.functionName.hashCode() : 0);
        hash = 37 * hash + (this.arguments != null ? this.arguments.hashCode() : 0);
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
        if (this.functionName != other.functionName && (this.functionName == null || !this.functionName.equals(other.functionName))) {
            return false;
        }
        if (this.arguments != other.arguments && (this.arguments == null || !this.arguments.equals(other.arguments))) {
            return false;
        }
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, toSourceCall(localData, generator, arguments), functionName, new ActionCallFunction());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

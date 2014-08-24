/*
 *  Copyright (C) 2010-2014 JPEXS
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
import com.jpexs.decompiler.flash.action.Deobfuscation;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
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
import java.util.Set;

public class GetVariableActionItem extends ActionItem {

    public GraphTargetItem name;
    private GraphTargetItem computedValue;
    private Object computedResult;
    private boolean computedCompiletime = false;
    private boolean computedVariableComputed = false;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(name);
        return ret;
    }

    public GetVariableActionItem(GraphSourceItem instruction, GraphTargetItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.name = value;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        
        if (((name instanceof DirectValueActionItem)) && (((DirectValueActionItem) name).isString()) && (!Deobfuscation.isValidName(((DirectValueActionItem) name).toStringNoQuotes(localData),"this","super"))){
            return writer.append(Deobfuscation.makeObfuscatedIdentifier(((DirectValueActionItem) name).toStringNoQuotes(localData)));
        }else if ((!(name instanceof DirectValueActionItem)) || (!((DirectValueActionItem) name).isString())) {
            writer.append("eval(");
            name.appendTo(writer, localData);
            return writer.append(")");
        }
        return stripQuotes(name, localData, writer);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(name.getNeededSources());
        return ret;
    }

    @Override
    public boolean isVariableComputed() {
        return true;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (computedValue == null) {
            return false;
        }
        return computedCompiletime;
    }

    @Override
    public Object getResult() {
        if (computedValue == null) {
            return new Undefined();
        }
        return computedResult;
    }

    public void setComputedValue(GraphTargetItem computedValue) {
        this.computedValue = computedValue;
        if (computedValue != null) {
            computedCompiletime = computedValue.isCompileTime();
            if (computedCompiletime) {
                computedResult = computedValue.getResult();
            }
            computedVariableComputed = computedValue.isVariableComputed();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash + (this.name != null ? this.name.hashCode() : 0);
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
        final GetVariableActionItem other = (GetVariableActionItem) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
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
        final GetVariableActionItem other = (GetVariableActionItem) obj;
        if (this.name != other.name && (this.name == null || !this.name.valueEquals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, name, new ActionGetVariable());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

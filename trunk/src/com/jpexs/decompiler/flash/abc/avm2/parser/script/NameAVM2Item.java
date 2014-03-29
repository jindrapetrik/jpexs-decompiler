/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
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
public class NameAVM2Item extends AssignableAVM2Item {

    private String variableName;
    
    private boolean definition;
    private GraphTargetItem index;
    private int nsKind = -1;
    public List<String> openedNamespaces;
    public List<Integer> openedNamespacesKind;
    public int line;
    public GraphTargetItem type;
    private String ns = "";
    private int regNumber = -1;

    public void setNs(String ns) {
        this.ns = ns;
    }

    public void setRegNumber(int regNumber) {
        this.regNumber = regNumber;
    }

    public int getRegNumber() {
        return regNumber;
    }

    
    
    public String getNs() {
        return ns;
    }

    public void appendName(String name) {
        this.variableName += "." + name;
    }

    public void setDefinition(boolean definition) {
        this.definition = definition;
    }

    public void setIndex(GraphTargetItem index) {
        this.index = index;
    }

    public GraphTargetItem getIndex() {
        return index;
    }

    public void setNsKind(int nsKind) {
        this.nsKind = nsKind;
    }

    public int getNsKind() {
        return nsKind;
    }

    public void setAssignedValue(GraphTargetItem storeValue) {
        this.assignedValue = storeValue;
    }

    public String getVariableName() {
        return variableName;
    }

    public NameAVM2Item(GraphTargetItem type, int line, String variableName, GraphTargetItem storeValue, boolean definition, List<String> openedNamespaces, List<Integer> openedNamespacesKind) {
        super(storeValue);
        this.variableName = variableName;
        this.assignedValue = storeValue;
        this.definition = definition;
        this.line = line;
        this.type = type;
    }

    public boolean isDefinition() {
        return definition;
    }

    public GraphTargetItem getStoreValue() {
        return assignedValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if (regNumber == -1) {
            throw new RuntimeException("No register set for " + variableName);
        }
        if (definition && assignedValue == null) {
            return new ArrayList<>();
        }
        AVM2SourceGenerator g=(AVM2SourceGenerator)generator;
        
        if(index!=null){
            //g.abc.constants.getmu
        }
        
        if (assignedValue == null) {
            return toSourceMerge(localData, generator, new LocalRegAVM2Item(null, regNumber, null));
        } else {
            return toSourceMerge(localData, generator, new SetLocalAVM2Item(null, regNumber, new CoerceAVM2Item(null, assignedValue, type.toString())));
        }

    }
      
    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if (regNumber == -1) {
            throw new RuntimeException("No register set for " + variableName);
        }
        if (definition && assignedValue == null) {
            return new ArrayList<>();
        }
        if (assignedValue == null) {
            return toSourceMerge(localData, generator, new LocalRegAVM2Item(null, regNumber, null),
                    new AVM2Instruction(0, new PopIns(), new int[]{}, new byte[0]));
        } else {
            return toSourceMerge(localData, generator, new SetLocalAVM2Item(null, regNumber, new CoerceAVM2Item(null, assignedValue, type.toString())).toSourceIgnoreReturnValue(localData, generator));
        }
    }

    @Override
    public boolean hasReturnValue() {
        if (definition) {
            return false;
        }
        return true;
    }

    @Override
    public boolean needsSemicolon() {
        if (definition) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return variableName;
    }

    @Override
    public GraphTargetItem returnType() {
        if (index != null) {
            return TypeItem.UNBOUNDED;
        }
        return type;
    }

    @Override
    public List<GraphSourceItem> toSourcePreChange(SourceGeneratorLocalData localData, SourceGenerator generator, List<GraphSourceItem> change) {
        return null;//TODO
    }

    @Override
    public List<GraphSourceItem> toSourcePostChange(SourceGeneratorLocalData localData, SourceGenerator generator, List<GraphSourceItem> change) {
        return null;//TODO
    }

}

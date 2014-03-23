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
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocal3Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal3Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
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
public class NameAVM2Item extends AVM2Item {


    private String variableName;
    private GraphTargetItem storeValue;
    private boolean definition;
    private GraphTargetItem index;
    private int nsKind = -1;
    public List<String> openedNamespaces;
    public List<Integer> openedNamespacesKind;
    public int line;
    public GraphTargetItem type;
    private String ns = "";
    private int regNumber=-1;

    public void setNs(String ns) {
        this.ns = ns;
    }

    public void setRegNumber(int regNumber) {
        this.regNumber = regNumber;
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

    public void setStoreValue(GraphTargetItem storeValue) {
        this.storeValue = storeValue;
    }

    public String getVariableName() {
        return variableName;
    }

    public NameAVM2Item(GraphTargetItem type, int line,String variableName, GraphTargetItem storeValue, boolean definition, List<String> openedNamespaces, List<Integer> openedNamespacesKind) {
        super(null, PRECEDENCE_PRIMARY);
        this.variableName = variableName;
        this.storeValue = storeValue;
        this.definition = definition;
        this.line = line;
        this.type = type;
    }

    public boolean isDefinition() {
        return definition;
    }

    public GraphTargetItem getStoreValue() {
        return storeValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if(regNumber==-1){
            throw new RuntimeException("No register set");
        }
        if(definition && storeValue==null){
            return new ArrayList<>();
        }
        AVM2Instruction ins;
        if(storeValue!=null){
            switch(regNumber){
                case 0:
                    ins = new AVM2Instruction(0, new GetLocal0Ins(), new int[]{}, new byte[0]);
                    break;
                case 1:
                    ins = new AVM2Instruction(0, new GetLocal1Ins(), new int[]{}, new byte[0]);
                    break;
                case 2:
                    ins = new AVM2Instruction(0, new GetLocal2Ins(), new int[]{}, new byte[0]);
                    break;
                case 3:
                    ins = new AVM2Instruction(0, new GetLocal3Ins(), new int[]{}, new byte[0]);
                    break;
                default:
                    ins = new AVM2Instruction(0, new GetLocalIns(), new int[]{regNumber}, new byte[0]);
                    break;
            }
            return toSourceMerge(localData, generator, ins);
        }else{
             switch(regNumber){
                case 0:
                    ins = new AVM2Instruction(0, new SetLocal0Ins(), new int[]{}, new byte[0]);
                    break;
                case 1:
                    ins = new AVM2Instruction(0, new SetLocal1Ins(), new int[]{}, new byte[0]);
                    break;
                case 2:
                    ins = new AVM2Instruction(0, new SetLocal2Ins(), new int[]{}, new byte[0]);
                    break;
                case 3:
                    ins = new AVM2Instruction(0, new SetLocal3Ins(), new int[]{}, new byte[0]);
                    break;
                default:
                    ins = new AVM2Instruction(0, new SetLocalIns(), new int[]{regNumber}, new byte[0]);
                    break;
            }
            return toSourceMerge(localData, generator,storeValue, ins);
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
        if(index!=null){
            return TypeItem.UNBOUNDED;
        }
        return type;
    }
        
}

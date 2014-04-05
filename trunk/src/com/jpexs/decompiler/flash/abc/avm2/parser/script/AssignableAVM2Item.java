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
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.KillIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal0Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal1Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocal3Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class AssignableAVM2Item extends AVM2Item {

    protected GraphTargetItem assignedValue;

    public AssignableAVM2Item() {
        this(null);
    }

    public abstract AssignableAVM2Item copy();
    
    public AssignableAVM2Item(GraphTargetItem storeValue) {
        super(null, PRECEDENCE_PRIMARY);
        this.assignedValue = storeValue;
    }

    public abstract List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn);

    public GraphTargetItem getAssignedValue() {
        return assignedValue;
    }

    public void setAssignedValue(GraphTargetItem storeValue) {
        this.assignedValue = storeValue;
    }

    public static List<GraphSourceItem> dupSetTemp(SourceGeneratorLocalData localData, SourceGenerator generator, Reference<Integer> register) {
        register.setVal(getFreeRegister(localData, generator));
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(ins(new DupIns()));
        ret.add(generateSetLoc(register.getVal()));
        return ret;
    }

    public static List<GraphSourceItem> setTemp(SourceGeneratorLocalData localData, SourceGenerator generator, Reference<Integer> register) {
        register.setVal(getFreeRegister(localData, generator));
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(generateSetLoc(register.getVal()));
        return ret;
    }

    public static List<GraphSourceItem> getTemp(SourceGeneratorLocalData localData, SourceGenerator generator, Reference<Integer> register) {
        if (register.getVal() < 0) {
            return new ArrayList<>();
        }
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(generateGetLoc(register.getVal()));
        return ret;
    }

    /*protected List<GraphSourceItem> getAndKillTemp(SourceGeneratorLocalData localData, SourceGenerator generator, Reference<Integer> register) {
     killRegister(localData, generator, register.getVal());
     List<GraphSourceItem> ret = new ArrayList<>();
     ret.add(generateGetLoc(register.getVal()));
     ret.add(ins(new KillIns(), register.getVal()));
     return ret;
     }*/
    @SuppressWarnings("unchecked")
    public static List<GraphSourceItem> killTemp(SourceGeneratorLocalData localData, SourceGenerator generator, List<Reference<Integer>> registers) {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (Reference<Integer> register : registers) {
            if (register.getVal() < 0) {
                continue;
            }
            killRegister(localData, generator, register.getVal());

            ret.add(ins(new KillIns(), register.getVal()));
        }
        return ret;
    }

    public static AVM2Instruction generateSetLoc(int regNumber) {
        switch (regNumber) {
            case 0:
                return ins(new SetLocal0Ins());
            case 1:
                return ins(new SetLocal1Ins());
            case 2:
                return ins(new SetLocal2Ins());
            case 3:
                return ins(new SetLocal3Ins());
            default:
                return ins(new SetLocalIns(), regNumber);
        }
    }

    public static AVM2Instruction generateGetLoc(int regNumber) {
        switch (regNumber) {
            case 0:
                return ins(new GetLocal0Ins());
            case 1:
                return ins(new GetLocal1Ins());
            case 2:
                return ins(new GetLocal2Ins());
            case 3:
                return ins(new GetLocal3Ins());
            default:
                return ins(new GetLocalIns(), regNumber);
        }
    }

}

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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.AssignmentAVM2Item;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;

public class SetLocalAVM2Item extends AVM2Item implements SetTypeAVM2Item, AssignmentAVM2Item {

    public int regIndex;
    //public GraphTargetItem value;

    public SetLocalAVM2Item(AVM2Instruction instruction, int regIndex, GraphTargetItem value) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.regIndex = regIndex;
        this.value = value;
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        hilight(localRegName(localData.localRegNames, regIndex) + " = ", writer);
        return value.toString(writer, localData);
    }

    @Override
    public GraphTargetItem getObject() {
        return new LocalRegAVM2Item(instruction, regIndex, null);
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}

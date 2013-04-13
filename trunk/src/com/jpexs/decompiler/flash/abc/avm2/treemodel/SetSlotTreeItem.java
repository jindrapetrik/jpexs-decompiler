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
package com.jpexs.decompiler.flash.abc.avm2.treemodel;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.AssignmentTreeItem;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;

public class SetSlotTreeItem extends TreeItem implements SetTypeTreeItem, AssignmentTreeItem {

    public Multiname slotName;
    public GraphTargetItem value;
    public GraphTargetItem scope;

    public SetSlotTreeItem(AVM2Instruction instruction, GraphTargetItem scope, Multiname slotName, GraphTargetItem value) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.slotName = slotName;
        this.value = value;
        this.scope = scope;
    }

    @Override
    public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {

        return getName(constants, localRegNames, fullyQualifiedNames) + hilight("=") + value.toString(constants, localRegNames, fullyQualifiedNames);
    }

    public String getName(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        String ret = "";

        /*ret = scope.toString(constants, localRegNames) + ".";
         if (!(scope instanceof NewActivationTreeItem)) {
         ret = scope.toString(constants, localRegNames) + ".";
         }
         if (scope instanceof LocalRegTreeItem) {
         if (((LocalRegTreeItem) scope).computedValue != null) {
         if (((LocalRegTreeItem) scope).computedValue instanceof NewActivationTreeItem) {
         ret = "";
         }
         }
         }*/
        if (slotName == null) {
            return ret + "/*UnknownSlot*/";
        }
        return ret + hilight(slotName.getName(constants, fullyQualifiedNames));
    }

    @Override
    public GraphTargetItem getObject() {
        return new GetSlotTreeItem(instruction, scope, slotName);
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

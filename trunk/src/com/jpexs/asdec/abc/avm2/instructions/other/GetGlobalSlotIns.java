/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.avm2.instructions.other;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.GetSlotTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.clauses.ExceptionTreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;

import java.util.List;
import java.util.Stack;


public class GetGlobalSlotIns extends InstructionDefinition {

    public GetGlobalSlotIns() {
        super(0x6e, "getglobalslot", new int[]{AVM2Code.DAT_SLOT_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        int slotIndex = ins.operands[0];
        TreeItem obj = (TreeItem) scopeStack.get(0); //scope
        Multiname slotname = null;
        if (obj instanceof ExceptionTreeItem) {
            slotname = constants.constant_multiname[((ExceptionTreeItem) obj).exception.name_index];
        } else {

            for (int t = 0; t < body.traits.traits.length; t++) {
                if (body.traits.traits[t] instanceof TraitSlotConst) {
                    if (((TraitSlotConst) body.traits.traits[t]).slot_id == slotIndex) {
                        slotname = body.traits.traits[t].getMultiName(constants);
                    }
                }

            }
        }
        stack.push(new GetSlotTreeItem(ins, obj, slotname));
    }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return 1;
   }


}

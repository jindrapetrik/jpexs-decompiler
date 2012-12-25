/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.abc.avm2.instructions.other;

 import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.instructions.SetTypeIns;
import com.jpexs.asdec.abc.avm2.treemodel.NewActivationTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.SetSlotTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.clauses.ExceptionTreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;


public class SetSlotIns extends InstructionDefinition implements SetTypeIns {

    public SetSlotIns() {
        super(0x6d, "setslot", new int[]{AVM2Code.DAT_SLOT_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc, HashMap<Integer,String> localRegNames) {
        int slotIndex = ins.operands[0];
        TreeItem value = (TreeItem) stack.pop();
        TreeItem obj = (TreeItem) stack.pop(); //scopeId

        if (obj instanceof ExceptionTreeItem) {
            return;
        }
        //if(value.startsWith("catched ")) return;
        Multiname slotname = null;
        for (int t = 0; t < body.traits.traits.length; t++) {
            if (body.traits.traits[t] instanceof TraitSlotConst) {
                if (((TraitSlotConst) body.traits.traits[t]).slot_id == slotIndex) {
                    slotname = body.traits.traits[t].getMultiName(constants);
                }
            }

        }
        
        //if new activation sets params of the function
        if(obj instanceof NewActivationTreeItem){
           if(localRegNames.containsValue(slotname.getName(constants))){
              return;
           }
        }
        
        output.add(new SetSlotTreeItem(ins, obj, slotname, value));
    }

    public String getObject(Stack<TreeItem> stack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body,HashMap<Integer,String> localRegNames) {
        int slotIndex = ins.operands[0];
        ////String obj = stack.get(1);
        String slotname = "";
        for (int t = 0; t < body.traits.traits.length; t++) {
            if (body.traits.traits[t] instanceof TraitSlotConst) {
                if (((TraitSlotConst) body.traits.traits[t]).slot_id == slotIndex) {
                    slotname = body.traits.traits[t].getMultiName(constants).getName(constants);
                }
            }

        }
        return slotname;
    }

   @Override
   public int getStackDelta(AVM2Instruction ins, ABC abc) {
      return -2;
   }


}

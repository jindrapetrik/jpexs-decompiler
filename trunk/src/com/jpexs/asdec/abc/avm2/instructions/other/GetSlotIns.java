/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.other;

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


public class GetSlotIns extends InstructionDefinition {

    public GetSlotIns() {
        super(0x6c, "getslot", new int[]{AVM2Code.DAT_SLOT_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        int slotIndex = ins.operands[0];
        TreeItem obj = (TreeItem) stack.pop(); //scope
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
}

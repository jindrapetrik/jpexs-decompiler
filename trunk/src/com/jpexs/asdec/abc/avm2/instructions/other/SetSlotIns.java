/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.other;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.instructions.SetTypeIns;
import com.jpexs.asdec.abc.avm2.treemodel.SetSlotTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.clauses.ExceptionTreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.abc.types.Multiname;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;

import java.util.List;
import java.util.Stack;


public class SetSlotIns extends InstructionDefinition implements SetTypeIns {

    public SetSlotIns() {
        super(0x6d, "setslot", new int[]{AVM2Code.DAT_SLOT_INDEX});
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
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
        output.add(new SetSlotTreeItem(ins, obj, slotname, value));
    }

    public String getObject(Stack<TreeItem> stack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body) {
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
}

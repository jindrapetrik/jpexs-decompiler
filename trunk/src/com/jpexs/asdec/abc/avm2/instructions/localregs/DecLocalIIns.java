/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.localregs;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.DecLocalTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class DecLocalIIns extends InstructionDefinition {

    public DecLocalIIns() {
        super(0xc3, "declocal_i", new int[]{AVM2Code.DAT_LOCAL_REG_INDEX});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        int locRegIndex = (int) ((Long) arguments.get(0)).longValue();
        Object obj = lda.localRegisters.get(locRegIndex);
        if (obj instanceof Long) {
            Long obj2 = ((Long) obj).longValue() - 1;
            lda.localRegisters.set(locRegIndex, obj2);
        } else if (obj instanceof Double) {
            Double obj2 = ((Double) obj).doubleValue() - 1;
            lda.localRegisters.set(locRegIndex, obj2);
        }
        if (obj instanceof String) {
            Double obj2 = Double.parseDouble((String) obj) - 1;
            lda.localRegisters.set(locRegIndex, obj2);
        } else {
            throw new RuntimeException("Cannot decrement local register");
        }
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        int regIndex = ins.operands[0];
        output.add(new DecLocalTreeItem(ins, regIndex));
    }
}

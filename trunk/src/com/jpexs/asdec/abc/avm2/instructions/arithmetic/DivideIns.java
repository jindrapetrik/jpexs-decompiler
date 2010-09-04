/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.arithmetic;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.operations.DivideTreeItem;
import com.jpexs.asdec.abc.types.MethodInfo;

import java.util.List;
import java.util.Stack;


public class DivideIns extends InstructionDefinition {

    public DivideIns() {
        super(0xa3, "divide", new int[]{});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        Object o1 = lda.operandStack.pop();
        Object o2 = lda.operandStack.pop();
        if ((o1 instanceof Long) && ((o2 instanceof Long))) {
            Long ret = new Long(((Long) o1).longValue() / ((Long) o2).longValue());
            lda.operandStack.push(ret);
        } else if ((o1 instanceof Double) && ((o2 instanceof Double))) {
            Double ret = new Double(((Double) o1).doubleValue() / ((Double) o2).doubleValue());
            lda.operandStack.push(ret);
        } else if ((o1 instanceof Long) && ((o2 instanceof Double))) {
            Double ret = new Double(((Long) o1).longValue() / ((Double) o2).doubleValue());
            lda.operandStack.push(ret);
        } else if ((o1 instanceof Double) && ((o2 instanceof Long))) {
            Double ret = new Double(((Double) o1).doubleValue() / ((Long) o2).longValue());
            lda.operandStack.push(ret);
        } else {
            throw new RuntimeException("Cannot divide");
        }
    }

    @Override
    public void translate(boolean isStatic, int classIndex, java.util.HashMap<Integer, TreeItem> localRegs, Stack<TreeItem> stack, java.util.Stack<TreeItem> scopeStack, ConstantPool constants, AVM2Instruction ins, MethodInfo[] method_info, List<TreeItem> output, com.jpexs.asdec.abc.types.MethodBody body, com.jpexs.asdec.abc.ABC abc) {
        TreeItem v2 = (TreeItem) stack.pop();
        TreeItem v1 = (TreeItem) stack.pop();
        stack.push(new DivideTreeItem(ins, v1, v2));
    }
}

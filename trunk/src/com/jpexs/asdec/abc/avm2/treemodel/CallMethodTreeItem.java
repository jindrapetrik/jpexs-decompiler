/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.List;


public class CallMethodTreeItem extends TreeItem {
    public TreeItem receiver;
    public String methodName;
    public List<TreeItem> arguments;

    public CallMethodTreeItem(AVM2Instruction instruction, TreeItem receiver, String methodName, List<TreeItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    @Override
    public String toString(ConstantPool constants) {
        String args = "";
        for (int a = 0; a < arguments.size(); a++) {
            if (a > 0) {
                args = args + ",";
            }
            args = args + arguments.get(a).toString(constants);
        }
        return receiver.toString(constants) + "." + methodName + hilight("(") + args + hilight(")");
    }


}

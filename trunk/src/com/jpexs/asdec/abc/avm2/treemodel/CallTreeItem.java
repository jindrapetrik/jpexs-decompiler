/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.List;


public class CallTreeItem extends TreeItem {
    public TreeItem receiver;
    public TreeItem function;
    public List<TreeItem> arguments;

    public CallTreeItem(AVM2Instruction instruction, TreeItem receiver, TreeItem function, List<TreeItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.function = function;
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
        return receiver.toString(constants) + hilight(".") + function.toString(constants) + hilight("(") + args + hilight(")");
    }


}

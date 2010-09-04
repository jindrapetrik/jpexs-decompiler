/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.List;


public class CallPropertyTreeItem extends TreeItem {
    public TreeItem receiver;
    public FullMultinameTreeItem propertyName;
    public List<TreeItem> arguments;
    public boolean isVoid;

    public CallPropertyTreeItem(AVM2Instruction instruction, boolean isVoid, TreeItem receiver, FullMultinameTreeItem propertyName, List<TreeItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.propertyName = propertyName;
        this.arguments = arguments;
        this.isVoid = isVoid;
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
        return formatProperty(constants, receiver, propertyName) + "(" + args + ")" + (isVoid ? ";" : "");
    }


}

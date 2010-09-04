/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.helpers.Highlighting;

import java.util.List;


public class CallSuperTreeItem extends TreeItem {
    public TreeItem receiver;
    public FullMultinameTreeItem multiname;
    public List<TreeItem> arguments;
    public boolean isVoid;

    public CallSuperTreeItem(AVM2Instruction instruction, boolean isVoid, TreeItem receiver, FullMultinameTreeItem multiname, List<TreeItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.multiname = multiname;
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
        String calee = receiver.toString(constants) + ".";
        if (Highlighting.stripHilights(calee).equals("this.")) calee = "";
        return calee + hilight("super.") + multiname.toString(constants) + hilight("(") + args + hilight(")") + (isVoid ? ";" : "");
    }


}

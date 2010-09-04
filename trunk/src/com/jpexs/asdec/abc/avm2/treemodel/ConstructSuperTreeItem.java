/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.helpers.Highlighting;

import java.util.List;


public class ConstructSuperTreeItem extends TreeItem {
    public TreeItem object;
    public List<TreeItem> args;

    public ConstructSuperTreeItem(AVM2Instruction instruction, TreeItem object, List<TreeItem> args) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.args = args;
    }

    @Override
    public String toString(ConstantPool constants) {
        String argStr = "";
        for (int a = 0; a < args.size(); a++) {
            if (a > 0) {
                argStr = argStr + ",";
            }
            argStr = argStr + args.get(a).toString(constants);
        }
        String calee = object.toString(constants) + ".";
        if (Highlighting.stripHilights(calee).equals("this.")) calee = "";
        return calee + hilight("super(") + argStr + hilight(")");

    }


}

/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.List;


public class ConstructTreeItem extends TreeItem {
    public TreeItem object;
    public List<TreeItem> args;

    public ConstructTreeItem(AVM2Instruction instruction, TreeItem object, List<TreeItem> args) {
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
        if (object instanceof NewFunctionTreeItem) {
            return object.toString(constants);
        }
        return hilight("new ") + object.toString(constants) + hilight("(") + argStr + hilight(")");

    }


}

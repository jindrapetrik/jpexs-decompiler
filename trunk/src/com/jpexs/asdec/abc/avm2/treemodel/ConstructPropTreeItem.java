/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.List;


public class ConstructPropTreeItem extends TreeItem {
    public TreeItem object;
    public FullMultinameTreeItem propertyName;
    public List<TreeItem> args;

    public ConstructPropTreeItem(AVM2Instruction instruction, TreeItem object, FullMultinameTreeItem propertyName, List<TreeItem> args) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
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
        String objstr = object.toString(constants);
        if (!objstr.equals("")) objstr += ".";
        return hilight("new ") + objstr + propertyName.toString(constants) + hilight("(") + argStr + hilight(")");

    }


}

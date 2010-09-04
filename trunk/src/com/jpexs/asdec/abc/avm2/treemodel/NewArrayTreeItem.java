/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.List;


public class NewArrayTreeItem extends TreeItem {
    public List<TreeItem> values;

    public NewArrayTreeItem(AVM2Instruction instruction, List<TreeItem> values) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.values = values;
    }

    @Override
    public String toString(ConstantPool constants) {
        String args = "";
        for (int a = 0; a < values.size(); a++) {
            if (a > 0) {
                args = args + ",";
            }
            args = args + values.get(a).toString(constants);
        }
        return hilight("[") + args + hilight("]");
    }


}

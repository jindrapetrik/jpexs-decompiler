/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.helpers.Highlighting;


public class GetSuperTreeItem extends TreeItem {
    public TreeItem object;
    public FullMultinameTreeItem propertyName;

    public GetSuperTreeItem(AVM2Instruction instruction, TreeItem object, FullMultinameTreeItem propertyName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public String toString(ConstantPool constants) {
        String calee = object.toString(constants) + ".";
        if (Highlighting.stripHilights(calee).equals("this.")) calee = "";
        return calee + hilight("super.") + propertyName.toString(constants);
    }


}

/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;


public class SetLocalTreeItem extends TreeItem {
    public int regIndex;
    public TreeItem value;

    public SetLocalTreeItem(AVM2Instruction instruction, int regIndex, TreeItem value) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.regIndex = regIndex;
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight(InstructionDefinition.localRegName(regIndex) + "=") + value.toString(constants) + ";";
    }


}

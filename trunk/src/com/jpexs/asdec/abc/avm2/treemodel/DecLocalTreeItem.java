/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;


public class DecLocalTreeItem extends TreeItem {
    public int regIndex;

    public DecLocalTreeItem(AVM2Instruction instruction, int regIndex) {
        super(instruction, PRECEDENCE_POSTFIX);
        this.regIndex = regIndex;
    }

    @Override
    public String toString(ConstantPool constants) {
        return InstructionDefinition.localRegName(regIndex) + hilight("--") + ";";
    }


}

/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;


public class LocalRegTreeItem extends TreeItem {
    public int regIndex;
    public TreeItem computedValue;

    public LocalRegTreeItem(AVM2Instruction instruction, int regIndex, TreeItem computedValue) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.regIndex = regIndex;
        if (computedValue == null) {
            computedValue = new UndefinedTreeItem(instruction);
        }
        this.computedValue = computedValue;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight(InstructionDefinition.localRegName(regIndex));
    }

    @Override
    public boolean isFalse() {
        return computedValue.isFalse();
    }

    @Override
    public boolean isTrue() {
        return computedValue.isTrue();
    }


}

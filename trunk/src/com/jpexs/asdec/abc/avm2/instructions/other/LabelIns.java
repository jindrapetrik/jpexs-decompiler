/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.other;

import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;


public class LabelIns extends InstructionDefinition {
//this can be target of branch

    public LabelIns() {
        super(0x09, "label", new int[]{});
    }

}

/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.other;

import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;


public class NopIns extends InstructionDefinition {

    public NopIns() {
        super(0x02, "nop", new int[]{});
    }

}

/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.debug;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;


public class DebugFileIns extends InstructionDefinition {

    public DebugFileIns() {
        super(0xf1, "debugfile", new int[]{AVM2Code.DAT_STRING_INDEX});
    }

}

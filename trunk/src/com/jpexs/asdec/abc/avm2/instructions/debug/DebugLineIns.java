/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.debug;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;


public class DebugLineIns extends InstructionDefinition {

    public DebugLineIns() {
        super(0xf0, "debugline", new int[]{AVM2Code.DAT_LINENUM});
    }

}

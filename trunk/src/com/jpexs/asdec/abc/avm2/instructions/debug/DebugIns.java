/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.debug;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;


public class DebugIns extends InstructionDefinition {

    public DebugIns() {
        super(0xef, "debug", new int[]{AVM2Code.DAT_DEBUG_TYPE, AVM2Code.DAT_STRING_INDEX, AVM2Code.DAT_REGISTER_INDEX, AVM2Code.OPT_U30});
    }

}

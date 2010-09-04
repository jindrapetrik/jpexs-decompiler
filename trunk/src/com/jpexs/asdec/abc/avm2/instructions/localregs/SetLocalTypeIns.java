/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.localregs;

import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public interface SetLocalTypeIns {
    public abstract int getRegisterId(AVM2Instruction ins);
}

/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.avm2.instructions.xml;

import com.jpexs.asdec.abc.avm2.AVM2Code;
import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.LocalDataArea;
import com.jpexs.asdec.abc.avm2.instructions.InstructionDefinition;

import java.util.List;


public class DXNSIns extends InstructionDefinition {

    public DXNSIns() {
        super(0x06, "dxns", new int[]{AVM2Code.DAT_STRING_INDEX});
    }

    @Override
    public void execute(LocalDataArea lda, ConstantPool constants, List arguments) {
        int strIndex = (int) ((Long) arguments.get(0)).longValue();
        String s = constants.constant_string[strIndex];
        System.out.println("Set default XML space " + s);

    }
}

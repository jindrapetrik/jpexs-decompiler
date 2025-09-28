/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2.deobfuscation;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfFalseIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.NopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushFalseIns;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import java.util.List;
import java.util.Map;

/**
 * This will fix some if(false) in Flex code in switch default statement.
 * It is not a real deobufscation, just fix for specific case.
 * @author JPEXS
 */
public class AVM2DeobfuscatorPushFalseIfFalse extends SWFDecompilerAdapter {

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        Map<Integer, List<Integer>> refs = body.getCode().visitCode(body);
        AVM2Code code = body.getCode();
        for (int ip = 1; ip < code.code.size(); ip++) {
            AVM2Instruction ins = code.code.get(ip);
            AVM2Instruction insPrev = code.code.get(ip - 1);
            if (!(ins.definition instanceof IfFalseIns)) {
                continue;
            }
            if (!(insPrev.definition instanceof PushFalseIns)) {
                continue;
            }
            if (refs.containsKey(ip) && refs.get(ip).size() > 1) {
                continue;
            }
            insPrev.definition = new NopIns();
            ins.definition = new JumpIns();            
        }
    }
    
}

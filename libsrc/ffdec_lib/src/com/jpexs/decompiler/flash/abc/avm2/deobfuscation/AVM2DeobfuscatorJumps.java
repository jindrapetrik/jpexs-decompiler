/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.deobfuscation;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import java.util.List;
import java.util.Map;

/**
 *
 * AVM2 Deobfuscator removing single assigned local registers.
 *
 * Example: var a = true; var b = false; ... if(a){ ...ok }else{ not executed }
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorJumps extends SWFDecompilerAdapter {

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {

        AVM2Code code = body.getCode();

        boolean found;
        do {
            found = false;
            Map<Integer, List<Integer>> refs = body.getCode().visitCode(body);
            loopi:
            for (int i = 0; i < code.code.size(); i++) {
                AVM2Instruction ins = code.code.get(i);
                if (ins.definition instanceof JumpIns) {
                    long targetAddr = ins.getTargetAddress();
                    {
                        for (int r : refs.get(i)) {
                            if (r >= 0) { //Not Exception start/end
                                AVM2Instruction srcIns = code.code.get(r);

                                if ((srcIns.definition instanceof JumpIns) || ((srcIns.definition instanceof IfTypeIns) && (r != i - 1))) {
                                    int oldop = srcIns.operands[0];
                                    srcIns.operands[0] = (int) (targetAddr - (srcIns.getAddress() + srcIns.getBytesLength()));
                                    if (srcIns.operands[0] != oldop) {
                                        found = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            code.removeDeadCode(body);
        } while (found);
    }
}

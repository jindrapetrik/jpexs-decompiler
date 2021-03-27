/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * AVM2 Deobfuscator replacing jumps/ifs targeting other jumps.
 *
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorJumps extends SWFDecompilerAdapter {

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {

        AVM2Code code = body.getCode();

        List<Integer> exceptionStarts = new ArrayList<>();
        for (ABCException ex : body.exceptions) {
            exceptionStarts.add(code.adr2pos(ex.start, true));
        }

        boolean found;
        do {
            found = false;
            Map<Integer, List<Integer>> refs = body.getCode().visitCode(body);
            loopi:
            for (int i = 0; i < code.code.size(); i++) {
                AVM2Instruction ins = code.code.get(i);
                if (ins.definition instanceof JumpIns) {
                    long srcAddr = ins.getAddress();
                    long targetAddr = ins.getTargetAddress();

                    //source and target must be in the same try..catch block
                    boolean exceptionMismatch = false;
                    for (int e = 0; e < body.exceptions.length; e++) {
                        boolean sourceMatch = srcAddr >= body.exceptions[e].start && srcAddr < body.exceptions[e].end;
                        boolean targetMatch = targetAddr >= body.exceptions[e].start && targetAddr < body.exceptions[e].end;
                        if (sourceMatch != targetMatch) {
                            exceptionMismatch = true;
                            break;
                        }
                    }
                    if (!exceptionMismatch) {
                        continue;
                    }

                    //We do not want exception start to be redirected somewhere else
                    if (exceptionStarts.contains(i)) {
                        continue;
                    }
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

            code.removeDeadCode(body);
        } while (found);
    }
}

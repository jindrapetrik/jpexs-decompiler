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
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stub for deobfuscator merging jump parts.
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorGroupParts extends SWFDecompilerAdapter {

    /**
     * Constructor.
     */
    public AVM2DeobfuscatorGroupParts() {

    }


    /*
      blk_1
      jump A
    B:
      blk_2
      jump C
A:    blk_3
      jump B
C:    blk_4

=>    
      jump A    
A:    jump B
B:    jump C
      blk_1
      blk_3
      blk_2
      blk_4

----------------------

    blk_1
    jump A
    B:
    blk_2
    jump C
A:  blk_3
    if B
    blk_5
C:  blk_4

=>
    jump A
B:
    blk_2
    jump C
A:  blk_1
    blk_3
    if B
    blk_5
C:
    blk_4

----------------------
    blk_1
    jump A
    B:
    blk_2
    if C
A:  blk_3
    jump B
C:  blk_4

=>

    blk_1
    jump A
    B:
    blk_3
    blk_2
    if C
A:  jump B
C:  blk_4
    
     */
    /**
     * Removes dead code and merges jump parts.
     *
     * @param path Path
     * @param classIndex Class index
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param abc ABC
     * @param trait Trait
     * @param methodInfo Method info
     * @param body Method body
     * @throws InterruptedException On interrupt
     */
    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        AVM2Code code = body.getCode();
        code.removeDeadCode(body);

        Map<Integer, List<Integer>> refs = body.getCode().visitCode(body);
        for (int i = 0; i < code.code.size(); i++) {
            AVM2Instruction ins = code.code.get(i);
            if (ins.definition instanceof JumpIns) {
                long targetAddr = ins.getTargetAddress();
                int targetIp = code.adr2pos(targetAddr);

                if (realRefs(refs, targetIp) == 1) {
                    int startIp = 0;
                    //find preceeding ip which is start of the "part"
                    for (int j = i; j >= 0; j--) {
                        AVM2Instruction startIns = code.code.get(j);
                        if (j < i && ((startIns.definition instanceof IfTypeIns) || (startIns.definition instanceof LookupSwitchIns))) {
                            startIp = j + 1;
                            break;
                        } else {
                            long srcAddr = code.pos2adr(j);
                            boolean exceptionMismatch = false;
                            for (int e = 0; e < body.exceptions.length; e++) {
                                boolean sourceMatch = srcAddr >= body.exceptions[e].start && srcAddr < body.exceptions[e].end;
                                boolean targetMatch = targetAddr >= body.exceptions[e].start && targetAddr < body.exceptions[e].end;
                                if (sourceMatch != targetMatch) {
                                    exceptionMismatch = true;
                                    break;
                                }
                            }
                            if (exceptionMismatch) {
                                startIp = j + 1;
                                break;
                            } else if (realRefs(refs, j) > 1) {
                                startIp = j;
                                break;
                            }
                        }
                    }
                    if (startIp < i) { //only when there is something to move
                        List<AVM2Instruction> movedInstructions = new ArrayList<>();
                        for (int k = startIp; k < i; k++) {
                            movedInstructions.add(code.code.get(startIp));
                            code.removeInstruction(startIp, body);
                        }

                        int newTargetIp = targetIp;
                        if (targetIp > i) { // forward jump
                            newTargetIp -= movedInstructions.size();
                        }
                        for (int m = 0; m < movedInstructions.size(); m++) {
                            code.insertInstruction(newTargetIp + m, movedInstructions.get(m), body);
                        }
                        i = -1;
                        refs = body.getCode().visitCode(body);
                    }
                }
            }
        }

        new AVM2DeobfuscatorJumps().avm2CodeRemoveTraps(path, classIndex, isStatic, scriptIndex, abc, trait, methodInfo, body);
    }

    /**
     * Gets real refs.
     *
     * @param refs Refs
     * @param ip IP
     * @return Real refs
     */
    private int realRefs(Map<Integer, List<Integer>> refs, int ip) {
        int refCount = 0;
        for (int r : refs.get(ip)) {
            if (r >= 0) {
                refCount++;
            }
        }
        return refCount;
    }

}

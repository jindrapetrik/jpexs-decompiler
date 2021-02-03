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

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.KillIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ThrowIns;
import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * AVM2 Deobfuscator removing single assigned local registers.
 *
 * Example: var a = true; var b = false; ... if(a){ ...ok }else{ not executed }
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorRegisters extends AVM2DeobfuscatorSimple {

    private Set<Integer> getRegisters(AVM2Code code) {
        Set<Integer> regs = new HashSet<>();
        for (AVM2Instruction ins : code.code) {
            InstructionDefinition def = ins.definition;
            if (def instanceof SetLocalTypeIns) {
                int regId = ((SetLocalTypeIns) def).getRegisterId(ins);
                regs.add(regId);
            } else if (def instanceof GetLocalTypeIns) {
                int regId = ((GetLocalTypeIns) def).getRegisterId(ins);
                regs.add(regId);
            } else {
                for (int p = 0; p < ins.definition.operands.length; p++) {
                    int op = ins.definition.operands[p];
                    if (op == AVM2Code.DAT_LOCAL_REG_INDEX) {
                        int regId = ins.operands[p];
                        regs.add(regId);
                    }
                }
            }
        }

        return regs;
    }

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
        //System.err.println("regdeo:" + path);

        MethodBody originalBody = body;
        body.getCode().removeDeadCode(body);
        Set<Integer> ignoredRegs = new HashSet<>();

        int localReservedCount = body.getLocalReservedCount();
        for (int i = 0; i < localReservedCount; i++) {
            ignoredRegs.add(i);
        }

        int setReg = 0;
        List<Integer> listedRegs = new ArrayList<>();
        List<MethodBody> listedLastBodies = new ArrayList<>();
        Set<Integer> ignoredRegGets = new HashSet<>();
        Reference<AVM2Instruction> assignmentRef = new Reference<>(null);

        while (setReg > -1) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            MethodBody bodybefore = body;
            body = bodybefore.clone();
            setReg = getFirstRegisterSetter(assignmentRef, body, abc, ignoredRegs, ignoredRegGets);
            //System.err.println("setreg " + setReg + " ass:" + assignmentRef.getVal());
            if (setReg < 0) {
                break;
            }

            // if there is second assignment
            if (listedRegs.contains(setReg)) {
                //System.err.println("second assignment of loc" + setReg + ", ignoring");
                int lindex = listedRegs.indexOf(setReg);
                body = listedLastBodies.get(lindex); // switch to body before
                ignoredRegs.add(setReg); // this is not obfuscated
                for (int i = listedRegs.size() - 1; i >= lindex; i--) {
                    int r = listedRegs.get(i);
                    listedRegs.remove(i);
                    listedLastBodies.remove(i);
                    ignoredRegGets.remove(r);
                }
                continue;
            }

            AVM2Instruction assignment = assignmentRef.getVal();
            InstructionDefinition def = assignment.definition;
            if ((def instanceof SetLocalTypeIns) || (def instanceof GetLocalTypeIns /*First usage -> value undefined*/)) {
                super.removeObfuscationIfs(classIndex, isStatic, scriptIndex, abc, body, assignment);
            }

            if (def instanceof GetLocalTypeIns) {
                ignoredRegGets.add(setReg);
            }

            listedRegs.add(setReg);
            listedLastBodies.add(bodybefore);
        }

        body.getCode().removeDeadCode(body);
        originalBody.exceptions = body.exceptions;
        originalBody.setCode(body.getCode());
        //System.err.println("/deo");
    }

    private void replaceSingleUseRegisters(Map<Integer, GraphTargetItem> singleRegisters, List<AVM2Instruction> setInss, int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body) throws InterruptedException {
        AVM2Code code = body.getCode();

        for (int i = 0; i < code.code.size(); i++) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            AVM2Instruction ins = code.code.get(i);
            if (((setInss == null) || setInss.contains(ins)) && (ins.definition instanceof SetLocalTypeIns)) {
                SetLocalTypeIns slt = (SetLocalTypeIns) ins.definition;
                int regId = slt.getRegisterId(ins);
                if (singleRegisters.containsKey(regId)) {
                    code.replaceInstruction(i, new AVM2Instruction(ins.getAddress(), DeobfuscatePopIns.getInstance(), null), body);
                }
            }

            if (ins.definition instanceof GetLocalTypeIns) {
                GetLocalTypeIns glt = (GetLocalTypeIns) ins.definition;
                int regId = glt.getRegisterId(ins);
                if (singleRegisters.containsKey(regId)) {
                    code.replaceInstruction(i, cpool.makePush(singleRegisters.get(regId).getResult()), body);
                }
            }
        }
    }

    private int getFirstRegisterSetter(Reference<AVM2Instruction> assignment, MethodBody body, ABC abc, Set<Integer> ignoredRegisters, Set<Integer> ignoredGets) throws InterruptedException {
        AVM2Code code = body.getCode();

        if (code.code.isEmpty()) {
            return -1;
        }

        return visitCode(assignment, new HashSet<>(), new Stack<>(), body, abc, code, 0, code.code.size() - 1, ignoredRegisters, ignoredGets);
    }

    private int visitCode(Reference<AVM2Instruction> assignment, Set<Integer> visited, Stack<Object> stack, MethodBody body, ABC abc, AVM2Code code, int idx, int endIdx, Set<Integer> ignored, Set<Integer> ignoredGets) throws InterruptedException {
        LocalDataArea localData = new LocalDataArea();
        initLocalRegs(localData, body.getLocalReservedCount(), body.max_regs, false);
        localData.localRegisters.put(0, Null.INSTANCE); // this

        List<Integer> toVisit = new ArrayList<>();
        toVisit.add(idx);
        List<Stack<Object>> toVisitStacks = new ArrayList<>();
        toVisitStacks.add(stack);
        outer:
        while (!toVisit.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            idx = toVisit.remove(0);
            stack = toVisitStacks.remove(0);

            while (true) {
                if (idx > endIdx) {
                    break;
                }
                if (visited.contains(idx)) {
                    break;
                }
                visited.add(idx);

                AVM2Instruction ins = code.code.get(idx);
                InstructionDefinition def = ins.definition;
                //System.err.println("" + idx + ": " + ins + " stack:" + stack.size());

                // do not throw EmptyStackException, much faster
                int requiredStackSize = def.getStackPopCount(ins, abc);
                if (stack.size() < requiredStackSize) {
                    continue outer;
                }

                localData.operandStack = stack;
                try {
                    ins.definition.execute(localData, abc.constants, ins);
                } catch (AVM2ExecutionException ex) {
                    Logger.getLogger(AVM2DeobfuscatorRegisters.class.getName()).log(Level.SEVERE, null, ex);
                }

                if (def instanceof SetLocalTypeIns) {
                    int regId = ((SetLocalTypeIns) def).getRegisterId(ins);
                    if (!ignored.contains(regId)) {
                        assignment.setVal(ins);
                        return regId;
                    }
                } else if (def instanceof GetLocalTypeIns) {
                    int regId = ((GetLocalTypeIns) def).getRegisterId(ins);
                    if (!ignored.contains(regId) && !ignoredGets.contains(regId)) {
                        assignment.setVal(ins);
                        return regId;
                    }
                } else if (!(def instanceof KillIns) && !(def instanceof DebugIns)) {
                    for (int p = 0; p < ins.definition.operands.length; p++) {
                        int op = ins.definition.operands[p];
                        if (op == AVM2Code.DAT_LOCAL_REG_INDEX) {
                            int regId = ins.operands[p];
                            if (!ignored.contains(regId)) {
                                assignment.setVal(ins);
                                return regId;
                            }
                        }
                    }
                }

                idx++;

                if (ins.definition instanceof JumpIns) {

                    long address = ins.getTargetAddress();
                    idx = code.adr2pos(address);//code.indexOf(code.getByAddress(address));
                    if (idx == -1) {
                        throw new TranslateException("Jump target not found: " + address);
                    }
                }

                if (ins.isBranch()) {
                    List<Integer> branches = ins.getBranches(new GraphSource() {

                        @Override
                        public int size() {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public GraphSourceItem get(int pos) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public boolean isEmpty() {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public List<GraphTargetItem> translatePart(GraphPart part, BaseLocalData localData, TranslateStack stack, int start, int end, int staticOperation, String path) throws InterruptedException {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public int adr2pos(long adr) {
                            return code.adr2pos(adr);
                        }

                        @Override
                        public long pos2adr(int pos) {
                            return code.pos2adr(pos);
                        }

                        @Override
                        public int adr2pos(long adr, boolean nearest) {
                            return code.adr2pos(adr, nearest);
                        }

                        @Override
                        public Set<Long> getImportantAddresses() {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }

                        @Override
                        public String insToString(int pos) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    });
                    idx = branches.get(0);
                    for (int n = 1; n < branches.size(); n++) {
                        //visitCode(visited, (TranslateStack) stack.clone(), classIndex, isStatic, body, scriptIndex, abc, code, branches.get(n), endIdx, result);
                        int nidx = branches.get(n);
                        if (visited.contains(nidx)) {
                            continue;
                        }
                        toVisit.add(nidx);
                        @SuppressWarnings("unchecked")
                        Stack<Object> cloneStack = (Stack<Object>) stack.clone();
                        toVisitStacks.add(cloneStack);
                    }
                }
                /*if (ins.definition instanceof IfTypeIns) {
                 long address = ins.offset + ins.getBytes().length + ins.operands[0];
                 int newIdx = code.adr2pos(address);
                 if (newIdx == -1) {
                 throw new TranslateException("If target not found: " + address);
                 }
                 visitCode(visited, (TranslateStack) stack.clone(), classIndex, isStatic, body, scriptIndex, abc, code, newIdx, endIdx, result);
                 }*/

                if (ins.definition instanceof ReturnValueIns) {
                    break;
                }

                if (ins.definition instanceof ThrowIns) {
                    break;
                }

                if (ins.definition instanceof ReturnVoidIns) {
                    break;
                }
            }
        }
        return -1;
    }
}

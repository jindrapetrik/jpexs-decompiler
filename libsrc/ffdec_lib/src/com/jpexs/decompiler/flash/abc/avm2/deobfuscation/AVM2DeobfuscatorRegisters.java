/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.DeobfuscatePopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ThrowIns;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.Reference;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * AVM2 Deobfuscator removing single assigned local registers.
 *
 * Example: var a = true; var b = false; ... if(a){ ...ok }else{ not executed }
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorRegisters extends AVM2DeobfuscatorSimple {

    //private final int executionLimit = 30000;
    @Override
    public void actionListParsed(ActionList actions, SWF swf) {

    }

    @Override
    public void deobfuscate(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody abody) throws InterruptedException {
        //System.err.println("regdeo:" + path);

        removeUnreachableInstructions(abody.getCode(), cpool, trait, minfo, abody);
        Set<Integer> ignoredRegs = new HashSet<>();

        MethodBody body = abody.clone();
        Reference<AVM2Instruction> assignment = new Reference<>(null);
        ignoredRegs.clear();
        for (int i = 0; i < body.getLocalReservedCount(); i++) {
            ignoredRegs.add(i);
        }

        int setReg = 0;
        List<Integer> listedRegs = new ArrayList<>();
        List<MethodBody> listedLastBodies = new ArrayList<>();
        Set<Integer> ignoredRegGets = new HashSet<>();

        while (setReg > -1) {
            MethodBody bodybefore = body;
            body = bodybefore.clone();
            setReg = getFirstRegisterSetter(assignment, classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body, ignoredRegs, ignoredRegGets);
            //System.err.println("setreg " + setReg + " ass:" + assignment.getVal());
            if (setReg < 0) {
                break;
            }

            //if there is second assignment
            if (listedRegs.contains(setReg)) {
                //System.err.println("second assignment of loc" + setReg + ", ignoring");
                int lindex = listedRegs.indexOf(setReg);
                body = listedLastBodies.get(lindex); //switch to body before
                ignoredRegs.add(setReg); //this is not obfuscated
                for (int i = listedRegs.size() - 1; i >= lindex; i--) {
                    int r = listedRegs.get(i);
                    listedRegs.remove(i);
                    listedLastBodies.remove(i);
                    ignoredRegGets.remove(r);
                }
                continue;
            }

            if ((assignment.getVal().definition instanceof SetLocalTypeIns) || (assignment.getVal().definition instanceof GetLocalTypeIns /*First usage -> value undefined*/)) {
                super.removeObfuscationIfs(classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body, Arrays.asList(assignment.getVal()));
            }

            if (assignment.getVal().definition instanceof GetLocalTypeIns) {
                ignoredRegGets.add(setReg);
            }

            listedRegs.add(setReg);
            listedLastBodies.add(bodybefore);
        }

        abody.exceptions = body.exceptions;
        abody.setCode(body.getCode());
        removeUnreachableInstructions(body.getCode(), cpool, trait, minfo, body);
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
                    code.replaceInstruction(i, new AVM2Instruction(ins.offset, new DeobfuscatePopIns(), null), body);
                }
            }

            if (ins.definition instanceof GetLocalTypeIns) {
                GetLocalTypeIns glt = (GetLocalTypeIns) ins.definition;
                int regId = glt.getRegisterId(ins);
                if (singleRegisters.containsKey(regId)) {
                    code.replaceInstruction(i, makePush(singleRegisters.get(regId).getResult(), cpool), body);
                }
            }
        }
    }

    private int getFirstRegisterSetter(Reference<AVM2Instruction> assignment, int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body, Set<Integer> ignoredRegisters, Set<Integer> ignoredGets) throws InterruptedException {
        AVM2Code code = body.getCode();

        if (code.code.isEmpty()) {
            return -1;
        }

        ExecutionResult res = new ExecutionResult();
        return visitCode(assignment, new HashSet<>(), new TranslateStack("deo"), classIndex, isStatic, body, scriptIndex, abc, code, 0, code.code.size() - 1, res, ignoredRegisters, ignoredGets);
    }

    private int visitCode(Reference<AVM2Instruction> assignment, Set<Integer> visited, TranslateStack stack, int classIndex, boolean isStatic, MethodBody body, int scriptIndex, ABC abc, AVM2Code code, int idx, int endIdx, ExecutionResult result, Set<Integer> ignored, Set<Integer> ignoredGets) throws InterruptedException {
        List<GraphTargetItem> output = new ArrayList<>();
        AVM2LocalData localData = newLocalData(scriptIndex, abc, abc.constants, body, isStatic, classIndex);
        localData.localRegs.put(0, new NullAVM2Item(null));//this

        List<Integer> toVisit = new ArrayList<>();
        toVisit.add(idx);
        List<TranslateStack> toVisitStacks = new ArrayList<>();
        toVisitStacks.add(stack);
        outer:
        while (!toVisit.isEmpty()) {
            idx = toVisit.remove(0);
            stack = toVisitStacks.remove(0);
            try {
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
                    int requiredStackSize = ins.getStackPopCount(localData);
                    if (stack.size() < requiredStackSize) {
                        continue outer;
                    }

                    ins.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");

                    //if (!(def instanceof KillIns))
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
                    } else {
                        for (int p = 0; p < ins.definition.operands.length; p++) {
                            int op = ins.definition.operands[p];
                            if (op == AVM2Code.DAT_REGISTER_INDEX) {
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

                        long address = ins.offset + ins.getBytesLength() + ins.operands[0];
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
                        });
                        idx = branches.get(0);
                        for (int n = 1; n < branches.size(); n++) {
                            //visitCode(visited, (TranslateStack) stack.clone(), classIndex, isStatic, body, scriptIndex, abc, code, branches.get(n), endIdx, result);
                            int nidx = branches.get(n);
                            if (visited.contains(nidx)) {
                                continue;
                            }
                            toVisit.add(nidx);
                            toVisitStacks.add((TranslateStack) stack.clone());
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
            } catch (ThreadDeath | InterruptedException ex) {
                throw ex;
            } catch (Throwable ex) {
                //ignore
            }
        }
        return -1;
    }
}

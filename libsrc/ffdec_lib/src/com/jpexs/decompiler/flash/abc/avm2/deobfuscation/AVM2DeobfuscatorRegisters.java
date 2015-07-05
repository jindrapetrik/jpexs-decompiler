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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
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

        MethodBody body = abody.clone();
        removeUnreachableActions(body.getCode(), cpool, trait, minfo, body);

        Map<Integer, GraphTargetItem> outFirstAssigned = new HashMap<>();
        Map<Integer, AVM2Instruction> outFirstAssignments = new HashMap<>();
        Map<Integer, Integer> outAssignCount1 = new HashMap<>();
        Map<Integer, Integer> outAssignCount2 = new HashMap<>();

        Map<Integer, GraphTargetItem> singleRegisters = new HashMap<>();

        List<AVM2Instruction> ignored = new ArrayList<>();
        Map<Integer, GraphTargetItem> registers = new HashMap<>();
        getFirstRegistersUsage(outAssignCount1, outFirstAssigned, outFirstAssignments, classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body, ignored, registers);
        ignored.addAll(outFirstAssignments.values());
        registers.putAll(outFirstAssigned);

        replaceSingleUseRegisters(registers, ignored, classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body);

        super.deobfuscate(path, classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body);
        removeUnreachableActions(body.getCode(), cpool, trait, minfo, body);

        //second pass - ignore all first assignments
        registers.clear();
        ignored.clear();
        outFirstAssignments.clear();
        getFirstRegistersUsage(outAssignCount2, new HashMap<>(), outFirstAssignments, classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body, ignored, registers);

        for (int regId : outAssignCount1.keySet()) {
            int ac = outAssignCount2.containsKey(regId) ? outAssignCount2.get(regId) : 0;
            if (ac == 0) {
                singleRegisters.put(regId, outFirstAssigned.get(regId));
            }
        }

        body = abody;
        replaceSingleUseRegisters(singleRegisters, null, classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body);
        super.deobfuscate(path, classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body);
        removeUnreachableActions(body.getCode(), cpool, trait, minfo, body);
    }

    private void replaceSingleUseRegisters(Map<Integer, GraphTargetItem> singleRegisters, List<AVM2Instruction> setInss, int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body) {
        AVM2Code code = body.getCode();

        for (int i = 0; i < code.code.size(); i++) {
            AVM2Instruction ins = code.code.get(i);
            if (((setInss == null) || setInss.contains(ins)) && (ins.definition instanceof SetLocalTypeIns)) {
                SetLocalTypeIns slt = (SetLocalTypeIns) ins.definition;
                int regId = slt.getRegisterId(ins);
                if (singleRegisters.containsKey(regId)) {
                    code.replaceInstruction(i, new AVM2Instruction(ins.offset, new DeobfuscatePopIns(), new int[]{}), body);
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

    private void getFirstRegistersUsage(Map<Integer, Integer> outAssignCount, Map<Integer, GraphTargetItem> outFirstAssigned, Map<Integer, AVM2Instruction> outFirstAssignments, int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body, List<AVM2Instruction> ignoredAss, Map<Integer, GraphTargetItem> registers) {
        AVM2Code code = body.getCode();
        Map<Integer, GraphTargetItem> ret = new HashMap<>();

        if (code.code.isEmpty()) {
            return;
        }

        ExecutionResult res = new ExecutionResult();
        visitCode(new HashSet<>(), new TranslateStack("deo"), classIndex, isStatic, body, scriptIndex, abc, code, 0, code.code.size() - 1, res, registers, ignoredAss);
        for (int reg : res.assignCount.keySet()) {
            if (res.firstAssigned.get(reg) != null && res.firstAssigned.get(reg).isCompileTime()) {
                outAssignCount.put(reg, res.assignCount.get(reg));
                outFirstAssigned.put(reg, res.firstAssigned.get(reg));
                outFirstAssignments.put(reg, res.firstAssignInstruction.get(reg));
            }
        }
    }
    /*
     private Map<Integer, GraphTargetItem> getSingleUseRegisters(int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body) {
     AVM2Code code = body.getCode();
     Map<Integer, GraphTargetItem> ret = new HashMap<>();

     if (code.code.isEmpty()) {
     return ret;
     }

     ExecutionResult res = new ExecutionResult();
     visitCode(new HashSet<>(), new TranslateStack("deo"), classIndex, isStatic, body, scriptIndex, abc, code, 0, code.code.size() - 1, res);
     for (int reg : res.assignCount.keySet()) {
     if (res.assignCount.get(reg) == 1) {
     ret.put(reg, res.lastAssigned.get(reg));
     }
     }

     return ret;
     }*/

    private void visitCode(Set<Integer> visited, TranslateStack stack, int classIndex, boolean isStatic, MethodBody body, int scriptIndex, ABC abc, AVM2Code code, int idx, int endIdx, ExecutionResult result, Map<Integer, GraphTargetItem> registers, List<AVM2Instruction> ignoredAss) {
        List<GraphTargetItem> output = new ArrayList<>();
        AVM2LocalData localData = newLocalData(scriptIndex, abc, abc.constants, body, isStatic, classIndex);
        localData.localRegs.put(0, new NullAVM2Item(null));//this

        List<Integer> toVisit = new ArrayList<>();
        toVisit.add(idx);
        List<TranslateStack> toVisitStacks = new ArrayList<>();
        toVisitStacks.add(stack);
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

                    AVM2Instruction action = code.code.get(idx);
                    InstructionDefinition def = action.definition;
                    //System.err.println("" + idx + ": " + action + " stack:" + stack.size());
                    if (action.definition instanceof GetLocalTypeIns) {
                        stack.push(registers.get(((GetLocalTypeIns) action.definition).getRegisterId(action)));
                    } else if (action.definition instanceof SetLocalTypeIns) {
                        SetLocalTypeIns slt = (SetLocalTypeIns) def;
                        GraphTargetItem regVal = stack.pop();//localData.localRegs.get(regId);
                        int regId = slt.getRegisterId(action);
                        if (!result.assignCount.containsKey(regId)) {
                            result.assignCount.put(regId, 0);
                        }
                        boolean isIgnored = false;
                        for (AVM2Instruction ins : ignoredAss) {
                            if (action == ins) {
                                isIgnored = true;
                                break;
                            }
                        }
                        if (isIgnored) {
                        } else {
                            result.assignCount.put(regId, result.assignCount.get(regId) + 1);

                            //GraphTargetItem regVal = localData.localRegs.get(regId);
                            if (regVal == null || !regVal.getNotCoerced().isCompileTime()) {
                                result.assignCount.put(regId, Integer.MAX_VALUE);
                            } else {
                                if (result.assignCount.get(regId) == 1) {
                                    result.firstAssigned.put(regId, regVal.getNotCoerced());
                                    result.firstAssignInstruction.put(regId, action);
                                }
                                result.lastAssigned.put(regId, regVal.getNotCoerced());
                            }
                        }
                        //assignCount
                    } else {
                        action.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");
                    }

                    idx++;

                    if (action.definition instanceof JumpIns) {

                        long address = action.offset + action.getBytesLength() + action.operands[0];
                        idx = code.adr2pos(address);//code.indexOf(code.getByAddress(address));
                        if (idx == -1) {
                            throw new TranslateException("Jump target not found: " + address);
                        }
                    }

                    if (action.isBranch()) {
                        List<Integer> branches = action.getBranches(new GraphSource() {

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
                    /*if (action.definition instanceof IfTypeIns) {
                     long address = action.offset + action.getBytes().length + action.operands[0];
                     int newIdx = code.adr2pos(address);
                     if (newIdx == -1) {
                     throw new TranslateException("If target not found: " + address);
                     }
                     visitCode(visited, (TranslateStack) stack.clone(), classIndex, isStatic, body, scriptIndex, abc, code, newIdx, endIdx, result);
                     }*/

                    if (action.definition instanceof ReturnValueIns) {
                        break;
                    }

                    if (action.definition instanceof ThrowIns) {
                        break;
                    }

                    if (action.definition instanceof ReturnVoidIns) {
                        break;
                    }
                }
            } catch (Exception ex) {
                //ignore
            }
        }
    }

    class ExecutionResult {

        public Map<Integer, Integer> assignCount = new HashMap<>();

        public Map<Integer, GraphTargetItem> lastAssigned = new HashMap<>();
        public Map<Integer, GraphTargetItem> firstAssigned = new HashMap<>();
        public Map<Integer, AVM2Instruction> firstAssignInstruction = new HashMap<>();
    }
}

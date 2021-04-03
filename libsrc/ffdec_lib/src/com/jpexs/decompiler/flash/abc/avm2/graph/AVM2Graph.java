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
package com.jpexs.decompiler.flash.abc.avm2.graph;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.FinalProcessLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.CodeStats;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewCatchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.debug.DebugLineIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfStrictEqIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.DecLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.IncLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.KillIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.HasNext2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.LabelIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.NopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DecLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.IncLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceAIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertIIns;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FilteredCheckAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.HasNextAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewActivationAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewFunctionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NextNameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NextValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetSlotAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThrowAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithEndAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ExceptionAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.FilterAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForEachInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.TryAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.StrictEqAVM2Item;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphException;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.StopPartKind;
import com.jpexs.decompiler.graph.ThrowState;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.AnyItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LoopItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class AVM2Graph extends Graph {

    private final AVM2Code avm2code;

    private final ABC abc;

    private final MethodBody body;

    private final Logger logger = Logger.getLogger(AVM2Graph.class.getName());

    final int FINALLY_KIND_STACK_BASED = 0;
    final int FINALLY_KIND_REGISTER_BASED = 1;
    final int FINALLY_KIND_INLINED = 2;
    final int FINALLY_KIND_UNKNOWN = -1;

    public AVM2Code getCode() {
        return avm2code;
    }

    private static List<GraphException> getExceptionEntries(MethodBody body) {
        List<GraphException> ret = new ArrayList<>();
        AVM2Code code = body.getCode();
        for (ABCException e : body.exceptions) {
            ret.add(new GraphException(code.adr2pos(e.start, true), code.adr2pos(e.end, true), code.adr2pos(e.target)));
        }
        return ret;
    }

    public AVM2Graph(AVM2Code code, ABC abc, MethodBody body, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, ScopeStack scopeStack, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, HashMap<Integer, Integer> localRegAssigmentIps, HashMap<Integer, List<Integer>> refs) {
        super(new AVM2GraphSource(code, isStatic, scriptIndex, classIndex, localRegs, scopeStack, abc, body, localRegNames, fullyQualifiedNames, localRegAssigmentIps, refs), getExceptionEntries(body));
        this.avm2code = code;
        this.abc = abc;
        this.body = body;
        /*heads = makeGraph(code, new ArrayList<GraphPart>(), body);
         this.code = code;
         this.abc = abc;
         this.body = body;
         for (GraphPart head : heads) {
         fixGraph(head);
         makeMulti(head, new ArrayList<GraphPart>());
         }*/

    }

    @Override
    protected boolean canBeBreakCandidate(BaseLocalData localData, GraphPart part, List<ThrowState> throwStates) {
        /*AVM2LocalData aLocalData = (AVM2LocalData) localData;
        if (aLocalData.finallyTargetParts.containsValue(part)) {
            return false;
        }*/
        for (ThrowState ts : throwStates) {
            if (ts.targetPart == part) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void beforeGetLoops(BaseLocalData localData, String path, Set<GraphPart> allParts, List<ThrowState> throwStates) throws InterruptedException {
        AVM2LocalData avm2LocalData = ((AVM2LocalData) localData);
        for (int e = 0; e < body.exceptions.length; e++) {
            ABCException ex = body.exceptions[e];
            if (ex.isFinally()) {
                avm2LocalData.finallyTargetParts.put(e, searchPart(code.adr2pos(ex.target), allParts));
            }
        }

        getIgnoredSwitches((AVM2LocalData) localData, allParts);
        Set<Integer> integerSwitchesIps = new HashSet<>();
        for (GraphPart p : avm2LocalData.ignoredSwitches.values()) {
            integerSwitchesIps.add(p.end);
        }

        for (GraphPart finallySwitchTarget : avm2LocalData.finallyJumps.values()) {
            if (!avm2LocalData.defaultParts.values().contains(finallySwitchTarget)) {
                for (ThrowState ts : throwStates) {
                    ts.throwingParts.remove(finallySwitchTarget);
                }
                //finallySwitchTarget.throwParts.clear(); // having throwparts in these causes problems
            }
        }
        Map<Integer, Set<Integer>> setLocalPosToGetLocalPos = calculateLocalRegsUsage(throwStates, avm2LocalData, integerSwitchesIps, path, allParts);
        avm2LocalData.setLocalPosToGetLocalPos = setLocalPosToGetLocalPos;
        avm2LocalData.inGetLoops = true;
    }

    @Override
    protected void afterGetLoops(BaseLocalData localData, String path, Set<GraphPart> allParts) throws InterruptedException {
        ((AVM2LocalData) localData).inGetLoops = false;
    }

    private void getIgnoredSwitches(AVM2LocalData localData, Set<GraphPart> allParts) throws InterruptedException {

        for (int e = 0; e < body.exceptions.length; e++) {
            ABCException ex = body.exceptions[e];
            if (!ex.isFinally()) {
                continue;
            }

            GraphPart finallyTryTargetPart = null;
            int targetIp = code.adr2pos(ex.target);
            for (GraphPart p : allParts) {
                if (targetIp >= p.start && targetIp <= p.end) {
                    finallyTryTargetPart = p;
                    break;
                }
            }

            GraphPart finallyPart = finallyTryTargetPart.nextParts.size() > 0 ? finallyTryTargetPart.nextParts.get(0) : null;

            TranslateStack finallyTryTargetStack = (TranslateStack) new TranslateStack("try_target");

            AVM2LocalData localData2 = new AVM2LocalData(localData);
            localData2.scopeStack = new ScopeStack();

            List<GraphTargetItem> targetOutput = translatePart(localData2, finallyTryTargetPart, finallyTryTargetStack, 0 /*??*/, "try_target");

            int switchedReg = -1;
            int finallyKind = FINALLY_KIND_UNKNOWN;
            Integer finallyThrowPushByte = null;
            if (finallyTryTargetStack.size() == 1 && finallyTryTargetStack.peek() instanceof IntegerValueAVM2Item) {
                finallyKind = FINALLY_KIND_STACK_BASED;
                finallyThrowPushByte = ((IntegerValueAVM2Item) finallyTryTargetStack.peek()).intValue();
            } else if (targetOutput.size() >= 2
                    && (targetOutput.get(targetOutput.size() - 1) instanceof SetLocalAVM2Item)
                    && (targetOutput.get(targetOutput.size() - 2) instanceof SetLocalAVM2Item)
                    && (((SetLocalAVM2Item) targetOutput.get(targetOutput.size() - 1)).value instanceof IntegerValueAVM2Item)) {
                SetLocalAVM2Item setLocal = ((SetLocalAVM2Item) targetOutput.get(targetOutput.size() - 1));
                switchedReg = setLocal.regIndex;
                finallyThrowPushByte = ((IntegerValueAVM2Item) setLocal.value).intValue();
                finallyKind = FINALLY_KIND_REGISTER_BASED;
            } else if (!targetOutput.isEmpty() && (targetOutput.get(targetOutput.size() - 1) instanceof ThrowAVM2Item)) {
                //inlined to single part                    
                //TODO: maybe replace all instances of exit nodes of try block
                finallyKind = FINALLY_KIND_INLINED;
            } else {
                //probably inlined code in more parts, cannot do :-(                    
            }
            localData.finallyKinds.put(e, finallyKind);
            Integer defaultPushByte = null;
            GraphPart switchPart = null;
            if (finallyKind == FINALLY_KIND_STACK_BASED) {
                /*
                    Search for a lookupswitch which first pops from the stack
                 */
                List<Integer> foundIps = new ArrayList<>();
                List<GraphPart> foundParts = new ArrayList<>();
                int stackAfter = localData.codeStats.instructionStats[finallyTryTargetPart.end].stackpos_after;

                //System.err.println("searching pops from stack size " + stackAfter);
                findAllPops(localData, stackAfter, finallyPart, foundIps, foundParts, new HashSet<>());
                loopFound:
                for (int i = 0; i < foundIps.size(); i++) {
                    int ip = foundIps.get(i);
                    if (avm2code.code.get(ip).definition instanceof LookupSwitchIns) {
                        switchPart = foundParts.get(i);
                    } else if (avm2code.code.get(ip).definition instanceof PopIns) {
                        //In swftools try..finally, there is dup before lookupswitch and pop in its branches
                        GraphPart popPart = searchPart(ip, allParts);
                        boolean isEmpty = true;
                        ip--;
                        loopEmpty:
                        while (isEmpty) {
                            for (int j = ip; j >= popPart.start; j--) {
                                if (avm2code.code.get(j).definition instanceof LookupSwitchIns) {
                                    switchPart = searchPart(j, allParts);
                                    localData.finallyIndicesWithDoublePush.add(e);
                                    break loopFound;
                                } else if (avm2code.code.get(j).definition instanceof LabelIns) {
                                    //okay
                                } else {
                                    isEmpty = false;
                                    break loopEmpty;
                                }
                            }
                            if (popPart.refs.size() == 1) {
                                popPart = popPart.refs.get(0);
                                ip = popPart.end;
                            } else {
                                break;
                            }
                        }

                    }
                }

                int finEndIp = avm2code.adr2pos(ex.end, true) - 1;
                GraphPart prevFinallyEndPart = searchPart(finEndIp, allParts);

                for (int j = prevFinallyEndPart.start; j <= prevFinallyEndPart.end; j++) {
                    AVM2Instruction ins = avm2code.code.get(j);
                    if (ins.definition instanceof NopIns) {

                    } else if (ins.definition instanceof PushByteIns) {
                        defaultPushByte = ins.operands[0];
                        localData.pushDefaultPart.put(e, prevFinallyEndPart);
                    } else if (ins.definition instanceof JumpIns) {
                    } else {
                        if (localData.pushDefaultPart.containsKey(e)) {
                            localData.pushDefaultPart.remove(e);
                        }
                        defaultPushByte = null;
                        break;
                    }
                }

                if (defaultPushByte == null) {
                    if (avm2code.code.get(prevFinallyEndPart.end).definition instanceof JumpIns) {
                        prevFinallyEndPart = prevFinallyEndPart.nextParts.get(0);
                        if (prevFinallyEndPart.nextParts.size() == 1 && prevFinallyEndPart.nextParts.get(0).refs.size() > 1) {
                            for (int j = prevFinallyEndPart.start; j <= prevFinallyEndPart.end; j++) {
                                AVM2Instruction ins = avm2code.code.get(j);
                                if (ins.definition instanceof NopIns) {

                                } else if (ins.definition instanceof PushByteIns) {
                                    defaultPushByte = ins.operands[0];
                                    localData.pushDefaultPart.put(e, prevFinallyEndPart);
                                    break;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            } else if (finallyKind == FINALLY_KIND_REGISTER_BASED) {
                switchPart = findLookupSwitchWithGetLocal(switchedReg, finallyPart);
                int startIp = code.adr2pos(ex.start, true);
                GraphPart tryPart = searchPart(startIp, allParts);
                if (tryPart != null) {
                    List<GraphPart> tryPartRefs = getRealRefs(tryPart);
                    if (tryPartRefs.size() == 1) {
                        GraphPart beforeTryPart = tryPartRefs.get(0);
                        if (beforeTryPart.getHeight() >= 2) {
                            int pos = beforeTryPart.end;
                            while (beforeTryPart.start <= pos) {
                                if (avm2code.code.get(pos).definition instanceof SetLocalTypeIns) {
                                    int setLocalRegister = ((SetLocalTypeIns) avm2code.code.get(pos).definition).getRegisterId(avm2code.code.get(beforeTryPart.end));
                                    if (setLocalRegister == switchedReg) {
                                        if (avm2code.code.get(pos - 1).definition instanceof PushByteIns) {
                                            if (switchPart != null) {
                                                defaultPushByte = avm2code.code.get(pos - 1).operands[0];
                                            }
                                        }
                                    }
                                    break;
                                } else if (avm2code.code.get(pos).definition instanceof DebugLineIns) {
                                    pos--;
                                } else {
                                    break;
                                }
                            }

                        }
                    }
                }
            }
            localData.switchedRegs.put(e, switchedReg);
            if (switchPart != null) {

                localData.defaultWays.put(switchPart, defaultPushByte);

                if (defaultPushByte != null) {
                    GraphPart defaultPart;
                    if (defaultPushByte == null || defaultPushByte < 0 || defaultPushByte > switchPart.nextParts.size() - 2) {
                        defaultPart = switchPart.nextParts.get(0);
                    } else {
                        defaultPart = switchPart.nextParts.get(1 + defaultPushByte);
                    }
                    localData.defaultParts.put(e, defaultPart);
                }

                if (finallyThrowPushByte != null) {
                    GraphPart finnalyThrowPart;
                    if (finallyThrowPushByte == null || finallyThrowPushByte < 0 || finallyThrowPushByte > switchPart.nextParts.size() - 2) {
                        finnalyThrowPart = switchPart.nextParts.get(0);
                    } else {
                        finnalyThrowPart = switchPart.nextParts.get(1 + finallyThrowPushByte);
                    }
                    localData.finallyThrowParts.put(e, finnalyThrowPart);
                }

                for (GraphPart r : finallyPart.refs) {
                    if (r.start < 0) {
                        continue;
                    }
                    GraphPart rr = r;
                    boolean needsPrev = true;
                    while (true) {
                        for (int ip = rr.end; ip >= rr.start; ip--) {
                            AVM2Instruction ins = avm2code.code.get(ip);
                            if (ins.definition instanceof JumpIns) {
                                continue;
                            } else if (ins.definition instanceof PushByteIns) {
                                int val = ins.operands[0];
                                if (val < 0 || val > switchPart.nextParts.size() - 2) {
                                    localData.finallyJumps.put(rr, switchPart.nextParts.get(0)); //default branch                                    
                                } else {
                                    localData.finallyJumps.put(rr, switchPart.nextParts.get(1 + val));
                                }
                                localData.finallyJumpsToFinallyIndex.put(rr, e);
                                needsPrev = false;
                                break;
                            } else if ((ins.definition instanceof SetLocalTypeIns) && (((SetLocalTypeIns) ins.definition).getRegisterId(ins) == switchedReg)) {
                                //ignore
                            } else if (ins.definition instanceof CoerceAIns) {
                                //ignore                            
                            } else {
                                needsPrev = false;
                                break;
                            }
                        }
                        if (needsPrev) {
                            List<GraphPart> prevs = new ArrayList<>();
                            for (GraphPart prevR : rr.refs) {
                                if (prevR.start >= 0) {
                                    prevs.add(prevR);
                                }
                            }

                            if (prevs.size() == 1) {
                                rr = prevs.get(0);
                            } else {
                                break;
                            }

                        } else {
                            break;
                        }
                    }
                }

                //return in finally block is joined after switch decision
                for (GraphPart p : switchPart.nextParts) {
                    for (GraphPart r : p.refs) {
                        if (r != switchPart) {
                            localData.finallyJumps.put(r, p);
                            localData.finallyJumpsToFinallyIndex.put(r, e);
                        }
                    }
                }

                localData.ignoredSwitches.put(e, switchPart);
            } else {
                //there is probably return in all branches and no other way outside finally
            }
        }
    }

    private void walkLocalRegsUsage(List<ThrowState> throwStates, AVM2LocalData localData, Set<Integer> getLocalPos, GraphPart startPart, GraphPart part, Set<GraphPart> visited, int ip, int searchRegId) {
        if (visited.contains(part) && part != startPart) {
            return;
        }

        if (localData.finallyThrowParts.containsValue(part)) {
            visited.add(part);
            return;
        }

        for (int i = ip; i <= part.end; i++) {
            AVM2Instruction ins = avm2code.code.get(i);
            if (ins.definition instanceof SetLocalTypeIns) {
                int regId = ((SetLocalTypeIns) ins.definition).getRegisterId(ins);
                if (searchRegId == regId) {
                    return;
                }
            }
            if (ins.definition instanceof GetLocalTypeIns) {
                int regId = ((GetLocalTypeIns) ins.definition).getRegisterId(ins);
                if (regId == searchRegId) {
                    getLocalPos.add(i);
                }
            }
            if ((ins.definition instanceof IncLocalIns)
                    || (ins.definition instanceof IncLocalIIns)
                    || (ins.definition instanceof IncLocalPIns)
                    || (ins.definition instanceof DecLocalIns)
                    || (ins.definition instanceof DecLocalIIns)
                    || (ins.definition instanceof DecLocalPIns)) {
                int regId = ins.operands[0];
                if (regId == searchRegId) {
                    getLocalPos.add(i);
                }
            }
            if ((ins.definition instanceof IncLocalPIns)
                    || (ins.definition instanceof DecLocalPIns)) {
                int regId = ins.operands[1];
                if (regId == searchRegId) {
                    getLocalPos.add(i);
                }
            }
            if (ins.definition instanceof HasNext2Ins) {
                int regId1 = ins.operands[0];
                if (regId1 == searchRegId) {
                    getLocalPos.add(i);
                }
                int regId2 = ins.operands[1];
                if (regId2 == searchRegId) {
                    getLocalPos.add(i);
                }
            }
        }

        if (visited.contains(part)) {
            return;
        }
        visited.add(part);

        try {
            //stop on switch
            if (localData.ignoredSwitches.values().contains(part)) {
                return;
            }
            if (localData.finallyJumps.containsKey(part)) {
                GraphPart targetPart = localData.finallyJumps.get(part);
                if (localData.defaultParts.containsValue(targetPart)) {
                    //okay, proceed to finally block
                } else if (targetPart.nextParts.size() == 1) {
                    //continue or break, definitely not a return, there won't be a register usage
                    walkLocalRegsUsage(throwStates, localData, getLocalPos, startPart, targetPart.nextParts.get(0), visited, ip, searchRegId);
                    return;
                } else {
                    return;
                }
            }
            for (GraphPart p : part.nextParts) {
                walkLocalRegsUsage(throwStates, localData, getLocalPos, startPart, p, visited, p.start, searchRegId);
            }
        } finally {
            for (ThrowState ts : throwStates) {
                if (ts.throwingParts.contains(part)) {
                    GraphPart p = ts.targetPart;
                    walkLocalRegsUsage(throwStates, localData, getLocalPos, startPart, p, visited, p.start, searchRegId);
                }

            }
        }
    }

    //TODO: optimize this to make it faster!!!
    public Map<Integer, Set<Integer>> calculateLocalRegsUsage(List<ThrowState> throwStates, AVM2LocalData localData, Set<Integer> ignoredSwitches, String path, Set<GraphPart> allParts) {
        logger.log(Level.FINE, "--- {0} ---", path);
        Map<Integer, Set<Integer>> setLocalPosToGetLocalPos = new TreeMap<>();
        Map<GraphPart, GraphPart> reverseFinallyJumps = new HashMap<>();
        for (GraphPart p : localData.finallyJumps.keySet()) {
            reverseFinallyJumps.put(localData.finallyJumps.get(p), p);
        }

        Map<Integer, Integer> setLocalPosToRegisterId = new HashMap<>();

        for (GraphPart p : allParts) {
            if (p.start < 0) {
                continue;
            }
            for (int ip = p.start; ip <= p.end; ip++) {
                AVM2Instruction ins = avm2code.code.get(ip);
                if (ins.definition instanceof SetLocalTypeIns) {
                    int regId = ((SetLocalTypeIns) ins.definition).getRegisterId(ins);
                    setLocalPosToGetLocalPos.put(ip, new TreeSet<>());
                    setLocalPosToRegisterId.put(ip, regId);
                }
            }
        }

        for (int ip : setLocalPosToGetLocalPos.keySet()) {
            GraphPart part = searchPart(ip + 1, allParts);

            if (part == null) { //might be last part of script (?)
                continue;
            }
            walkLocalRegsUsage(throwStates, localData, setLocalPosToGetLocalPos.get(ip), part, part, new HashSet<>(), ip + 1, setLocalPosToRegisterId.get(ip));
        }

        /*for (int ip : setLocalPosToGetLocalPos.keySet()) {
            System.err.println("definition at ip " + (ip + 1) + ", regid=" + setLocalPosToRegisterId.get(ip));
            for (int usageIp : setLocalPosToGetLocalPos.get(ip)) {
                System.err.println("- used at " + (usageIp + 1));
            }
        }*/
        return setLocalPosToGetLocalPos;
    }

    public static List<GraphTargetItem> translateViaGraph(String path, AVM2Code code, ABC abc, MethodBody body, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, ScopeStack scopeStack, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, int staticOperation, HashMap<Integer, Integer> localRegAssigmentIps, HashMap<Integer, List<Integer>> refs, boolean thisHasDefaultToPrimitive) throws InterruptedException {
        AVM2Graph g = new AVM2Graph(code, abc, body, isStatic, scriptIndex, classIndex, localRegs, scopeStack, localRegNames, fullyQualifiedNames, localRegAssigmentIps, refs);

        AVM2LocalData localData = new AVM2LocalData();
        localData.thisHasDefaultToPrimitive = thisHasDefaultToPrimitive;
        localData.isStatic = isStatic;
        localData.classIndex = classIndex;
        localData.localRegs = localRegs;
        localData.scopeStack = scopeStack;
        localData.methodBody = body;
        localData.abc = abc;
        localData.localRegNames = localRegNames;
        localData.fullyQualifiedNames = fullyQualifiedNames;
        localData.scriptIndex = scriptIndex;
        localData.ip = 0;
        localData.refs = refs;
        localData.code = code;
        g.init(localData);
        Set<GraphPart> allParts = new HashSet<>();
        for (GraphPart head : g.heads) {
            populateParts(head, allParts);
        }
        return g.translate(localData, staticOperation, path);
    }

    @Override
    protected void checkGraph(List<GraphPart> allBlocks) {
        for (ABCException ex : body.exceptions) {
            GraphPart targetPart = searchPart(avm2code.adr2pos(ex.target), allBlocks);
            for (GraphPart p : allBlocks) {
                if (avm2code.pos2adr(p.start) >= ex.start && avm2code.pos2adr(p.end) <= ex.end && targetPart != null) {
                    //Logger.getLogger(Graph.class.getName()).fine("ADDING throwpart " + target + " to " + p);
                    //p.throwParts.add(targetPart);
                    //target.refs.add(p);
                }
            }
            /*GraphPart startPart = searchPart(avm2code.adr2pos(ex.start), allBlocks);
            
            startPart.throwParts.add(targetPart);*/
        }
    }

    private void findNearestPartOutsideTry(AVM2LocalData localData, GraphPart part, int tryEndIp, Set<GraphPart> visited, Set<GraphPart> result) {
        if (visited.contains(part)) {
            return;
        }

        if (part.start >= tryEndIp || part.end >= tryEndIp) {
            result.add(part);
            return;
        }

        if (localData.finallyJumps.containsKey(part)) {
            GraphPart afterSwitchPart = localData.finallyJumps.get(part);
            GraphPart switchPart = null;
            for (GraphPart r : afterSwitchPart.refs) {
                if (localData.ignoredSwitches.containsValue(r)) {
                    switchPart = r;
                }
            }

            if (switchPart != null) {
                return;
            }
        }

        for (GraphPart n : part.nextParts) {
            findNearestPartOutsideTry(localData, n, tryEndIp, visited, result);
        }
    }

    private Set<GraphPart> findNearestPartOutsideTry(AVM2LocalData localData, GraphPart start, int tryEndIp) {
        Set<GraphPart> result = new HashSet<>();
        findNearestPartOutsideTry(localData, start, tryEndIp, new HashSet<>(), result);
        return result;
    }

    private GraphPart findLookupSwitchWithGetLocal(int registerId, GraphPart part, Set<GraphPart> visited) {
        if (visited.contains(part)) {
            return null;
        }
        visited.add(part);
        if (part.getHeight() >= 3) {
            if (avm2code.code.get(part.end).definition instanceof LookupSwitchIns) {
                if (avm2code.code.get(part.end - 1).definition instanceof ConvertIIns) {
                    if (avm2code.code.get(part.end - 2).definition instanceof GetLocalTypeIns) {
                        int getLocalRegId = ((GetLocalTypeIns) avm2code.code.get(part.end - 2).definition).getRegisterId(avm2code.code.get(part.end - 2));
                        if (getLocalRegId == registerId) {
                            return part;
                        }
                    }
                }
            }
        }
        for (GraphPart n : part.nextParts) {
            GraphPart found = findLookupSwitchWithGetLocal(registerId, n, visited);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private GraphPart findLookupSwitchWithGetLocal(int registerId, GraphPart part) {
        return findLookupSwitchWithGetLocal(registerId, part, new HashSet<>());
    }

    private void findAllPops(AVM2LocalData localData, int stackLevel, GraphPart part, List<Integer> foundIps, List<GraphPart> foundParts, Set<GraphPart> visited) {
        if (visited.contains(part)) {
            return;
        }
        visited.add(part);
        //System.err.println("walk part " + part);
        for (int ip = part.start; ip <= part.end; ip++) {
            //System.err.println("ip " + ip + ": " + avm2code.code.get(ip) + ": stackpos_after:" + localData.codeStats.instructionStats[ip].stackpos_after);
            if (localData.codeStats.instructionStats[ip].stackpos_after == stackLevel - 1) {
                foundIps.add(ip);
                foundParts.add(part);
                return;
            }
        }
        for (GraphPart n : part.nextParts) {
            findAllPops(localData, stackLevel, n, foundIps, foundParts, visited);
        }
    }

    private List<GraphPart> getRealRefs(GraphPart part) {
        List<GraphPart> ret = new ArrayList<>();
        for (GraphPart r : part.refs) {
            if (r.start >= 0) {
                ret.add(r);
            }
        }
        return ret;
    }

    private GraphPart firstPartOutsideWalk(GraphPart part, int startIp, int endIp, Set<GraphPart> visited) {
        if (visited.contains(part)) {
            return null;
        }
        visited.add(part);
        if (part.start < startIp || part.start >= endIp) {
            return part;
        }
        for (GraphPart n : part.nextParts) {
            GraphPart r = firstPartOutsideWalk(n, startIp, endIp, visited);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    private GraphPart searchFirstPartOutSideTryCatchSimple(ABCException ex, Collection<? extends GraphPart> allParts) {
        int startIp = code.adr2pos(ex.start, true);
        int endIp = code.adr2pos(ex.end, true);

        GraphPart startPart = searchPart(startIp, allParts);
        return firstPartOutsideWalk(startPart, startIp, endIp, new HashSet<>());
    }

    private GraphPart searchFirstPartOutSideTryCatch(AVM2LocalData localData, ABCException ex, List<Loop> loops, Collection<? extends GraphPart> allParts) {
        LinkedHashSet<GraphPart> reachable = new LinkedHashSet<>();
        int startIp = localData.code.adr2pos(ex.start, true);
        int endIp = localData.code.adr2pos(ex.end, true);

        GraphPart startPart = searchPart(startIp, allParts);
        AVM2LocalData subLocalData = new AVM2LocalData(localData);

        //make reachableparts ignore finallyjumps
        subLocalData.defaultParts = new HashMap<>();
        subLocalData.defaultWays = new HashMap<>();
        subLocalData.finallyIndexToDefaultGraphPart = new HashMap<>();
        subLocalData.finallyIndicesWithDoublePush = new HashSet<>();
        subLocalData.finallyJumps = new HashMap<>();
        subLocalData.finallyJumpsToFinallyIndex = new HashMap<>();
        subLocalData.finallyThrowParts = new HashMap<>();
        subLocalData.ignoredSwitches = new HashMap<>();
        getReachableParts(subLocalData, startPart, reachable, loops, new ArrayList<>() /*??*/);

        for (GraphPart r : reachable) {
            if (r.start < startIp || r.start >= endIp) {
                return r;
            }
        }
        return null;
    }

    private GraphPart nearestNonEmptyPart(GraphPart part) {
        while (isPartEmpty(part)) {
            part = part.nextParts.get(0);
        }
        return part;
    }

    private void getCatchedExceptionIds(GraphPart part, List<Integer> previouslyCatchedExceptionIds, List<Integer> catchedExceptionIds, Reference<Integer> finallyIndex, Collection<? extends GraphPart> allParts,
            List<Loop> loops, AVM2LocalData localData) {

        long addr = avm2code.pos2adr(part.start);
        long maxEndAddr = -1;
        finallyIndex.setVal(-1);

        List<Integer> finnalysIndicesToBe = new ArrayList<>();
        maxEndAddr = -1;
        for (int e = 0; e < body.exceptions.length; e++) {
            long fixedExStart = avm2code.pos2adr(avm2code.adr2pos(body.exceptions[e].start, true));
            long fixedExEnd = avm2code.pos2adr(avm2code.adr2pos(body.exceptions[e].end, true));
            if (!previouslyCatchedExceptionIds.contains(e)) {
                if (addr == fixedExStart) { //avm2code.getAddrThroughJumpAndDebugLine(fixedExStart)) {
                    ABCException ex = body.exceptions[e];
                    if (ex.isFinally()) {
                        if (fixedExEnd >= maxEndAddr) {
                            finnalysIndicesToBe.add(e);
                        }
                    } else {
                        long endAddr = fixedExEnd;
                        if (endAddr > maxEndAddr) {
                            catchedExceptionIds.clear();
                            maxEndAddr = fixedExEnd;

                            catchedExceptionIds.add(e);

                            //filter finallys that have lower endAddr - they do not belong to these catches
                            for (int k = 0; k < finnalysIndicesToBe.size(); k++) {
                                if (body.exceptions[finnalysIndicesToBe.get(k)].end < endAddr) {
                                    finnalysIndicesToBe.remove(k);
                                    k--;
                                }
                            }
                        } else if (endAddr == maxEndAddr) {
                            catchedExceptionIds.add(e);
                        }
                    }
                }
            }
        }

        Collections.sort(finnalysIndicesToBe, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return body.exceptions[o2].end - body.exceptions[o1].end;
            }
        });

        GraphPart outSideExceptionPart = null;
        if (!catchedExceptionIds.isEmpty()) {
            outSideExceptionPart = searchFirstPartOutSideTryCatch(localData, body.exceptions[catchedExceptionIds.get(0)], loops, allParts);
        }

        if (!finnalysIndicesToBe.isEmpty()) {
            long maxEnd = 0;
            int maxF = -1;
            for (int f : finnalysIndicesToBe) {
                long fixedExEnd = avm2code.pos2adr(avm2code.adr2pos(body.exceptions[f].end, true));
                if (fixedExEnd > maxEnd) {
                    maxEnd = fixedExEnd;
                    maxF = f;
                }
            }
            finnalysIndicesToBe.clear();
            finnalysIndicesToBe.add(maxF);
        }

        for (int e : finnalysIndicesToBe) {
            ABCException finallyExceptionToBe = body.exceptions[e];
            if (catchedExceptionIds.isEmpty() || outSideExceptionPart == null) {
                //there's no exception, finally only                
                break;
            }
            GraphPart outSideExceptionNonEmptyPart = nearestNonEmptyPart(outSideExceptionPart);

            GraphPart outSideFinallyPart = searchFirstPartOutSideTryCatch(localData, finallyExceptionToBe, loops, allParts);
            if (outSideExceptionNonEmptyPart == outSideFinallyPart) {
                finallyIndex.setVal(e);
                break;
            }
            if (outSideExceptionNonEmptyPart.nextParts.size() == 1 && outSideExceptionNonEmptyPart.nextParts.get(0) == outSideFinallyPart) {
                boolean hashPushByteOnly = true;
                for (int ip = outSideExceptionNonEmptyPart.start; ip <= outSideExceptionNonEmptyPart.end; ip++) {
                    AVM2Instruction ins = avm2code.code.get(outSideExceptionNonEmptyPart.start);
                    if (ins.definition instanceof PushByteIns) {

                    } else if (ins.definition instanceof JumpIns) {

                    } else if (ins.definition instanceof NopIns) {

                    } else {
                        hashPushByteOnly = false;
                    }
                }
                if (hashPushByteOnly) {
                    finallyIndex.setVal(e);
                    break;
                }
            }
        }

        if (finallyIndex.getVal() == -1 && !finnalysIndicesToBe.isEmpty()) {
            catchedExceptionIds.clear();
            finallyIndex.setVal(finnalysIndicesToBe.get(0));
        }
    }

    private boolean checkTry(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, AVM2LocalData localData, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, Set<GraphPart> allParts, TranslateStack stack, int staticOperation, String path, int recursionLevel) throws InterruptedException {
        if (localData.parsedExceptions == null) {
            localData.parsedExceptions = new ArrayList<>();
        }
        if (localData.finallyJumps == null) {
            localData.finallyJumps = new HashMap<>();
        }
        if (localData.ignoredSwitches == null) {
            localData.ignoredSwitches = new HashMap<>();
        }

        List<ABCException> parsedExceptions = localData.parsedExceptions;
        List<Integer> parsedExceptionIds = localData.parsedExceptionIds;

        List<Integer> catchedExceptionIds = new ArrayList<>();
        Reference<Integer> finallyIndexRef = new Reference<>(-1);

        getCatchedExceptionIds(part, parsedExceptionIds, catchedExceptionIds, finallyIndexRef, allParts, loops, localData);
        List<ABCException> catchedExceptions = new ArrayList<>();
        for (int e : catchedExceptionIds) {
            catchedExceptions.add(body.exceptions[e]);
        }
        ABCException finallyException = null;
        int finallyIndex = finallyIndexRef.getVal();
        if (finallyIndex > -1) {
            finallyException = body.exceptions[finallyIndex];
        }

        if (finallyException != null) {
            catchedExceptions.add(finallyException);
            catchedExceptionIds.add(finallyIndex);
        }

        int switchedReg = -1;
        if (finallyIndex != -1) {
            switchedReg = localData.switchedRegs.containsKey(finallyIndex) ? localData.switchedRegs.get(finallyIndex) : -1;
        }

        if (catchedExceptions.size() > 0) {

            for (ThrowState ts : throwStates) {
                if (catchedExceptionIds.contains(ts.exceptionId)) {
                    ts.state = 1;
                }
                if (ts.exceptionId == finallyIndex) {
                    ts.state = 1;
                }
            }

            parsedExceptions.addAll(catchedExceptions);
            parsedExceptionIds.addAll(catchedExceptionIds);
            if (finallyException != null) {
                catchedExceptions.remove(finallyException);
                catchedExceptionIds.remove((Integer) finallyIndex);
            }
            if (finallyIndex > -1) {
                parsedExceptionIds.add(finallyIndex);
            }
            List<GraphTargetItem> tryCommands = new ArrayList<>();
            List<List<GraphTargetItem>> catchCommands = new ArrayList<>();
            List<GraphTargetItem> finallyCommands = new ArrayList<>();

            GraphPart afterPart = null;

            List<GraphPart> partsToCalCommon = new ArrayList<>();
            partsToCalCommon.add(part);
            for (ABCException ex : catchedExceptions) {
                partsToCalCommon.add(searchPart(localData.code.adr2pos(ex.target), allParts));
            }

            if (partsToCalCommon.size() > 1) {
                /*System.err.println("getting common part of");
                for (GraphPart p : partsToCalCommon) {
                    System.err.println("- " + p);
                }*/

                afterPart = getMostCommonPart(localData, partsToCalCommon, loops, throwStates);
                //System.err.println("result: " + afterPart);
            }

            if (catchedExceptions.size() > 0 && afterPart == null) {
                //in all catches is probably continue/return/break or something
                //we need to search a part which is first outside the try..block
                afterPart = searchFirstPartOutSideTryCatch(localData, catchedExceptions.get(0), loops, allParts);
                //System.err.println("oursidetrycatch: " + afterPart);
            }

            GraphPart exAfterPart = afterPart;

            if (finallyException == null) {
                List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                stopPart2.add(afterPart);
                List<StopPartKind> stopPartKind2 = new ArrayList<>(stopPartKind);
                stopPartKind2.add(StopPartKind.OTHER);
                tryCommands = printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, null, part, stopPart2, stopPartKind2, loops, throwStates, staticOperation, path);
            }

            boolean inlinedFinally = false;
            boolean finallyAsUnnamedException = false;

            List<GraphTargetItem> finallyTargetItems = new ArrayList<>();

            GraphPart defaultPart = null;
            GraphPart finallyPart = null;
            if (finallyException != null) {

                GraphPart switchPart = localData.ignoredSwitches.get(finallyIndex);
                if (switchPart != null) {
                    defaultPart = localData.defaultParts.containsKey(finallyIndex) ? localData.defaultParts.get(finallyIndex) : null;
                }

                localData.finallyIndexToDefaultGraphPart.put(finallyIndex, defaultPart);

                GraphPart finallyTryTargetPart = null;
                int targetPos = avm2code.adr2pos(finallyException.target);
                finallyTryTargetPart = searchPart(targetPos, allParts);

                //Is it wrong to assume try target is only single part before finally?
                finallyPart = finallyTryTargetPart.nextParts.isEmpty() ? null : finallyTryTargetPart.nextParts.get(0);

                //List<GraphPart> finallyTargetStopPart = new ArrayList<>(stopPart);
                if (finallyPart != null) {
                    //finallyTargetStopPart
                }
                if (afterPart != null) {
                    //finallyTargetStopPart.add(afterPart);
                }

                TranslateStack st2 = (TranslateStack) stack.clone();
                st2.clear();
                st2.add(new ExceptionAVM2Item(finallyException));
                AVM2LocalData localData2 = new AVM2LocalData(localData);
                localData2.scopeStack = new ScopeStack();

                //We are assuming Finally target has only 1 part
                finallyTargetItems = translatePart(localData2, finallyTryTargetPart, st2, staticOperation, path);//printGraph(foundGotos, partCodes, partCodePos, visited, localData2, st2, allParts, null, finallyTryTargetPart, finallyTargetStopPart, loops, throwStates, 0, path);
                //boolean targetHasThrow = false;
                if (!finallyTargetItems.isEmpty() && (finallyTargetItems.get(finallyTargetItems.size() - 1) instanceof ThrowAVM2Item)) {

                    //ignore some usual commands at the beginning - these are ignored in ffdec later, but we need to check it's empty
                    boolean isEmpty = true;
                    for (int i = 0; i < finallyTargetItems.size(); i++) {
                        GraphTargetItem it = finallyTargetItems.get(i);
                        if (it instanceof SetLocalAVM2Item) {
                            if (it.value.getNotCoerced() instanceof ExceptionAVM2Item) {
                                //okay
                            } else {
                                isEmpty = false;
                                break;
                            }
                        } else if (it instanceof ThrowAVM2Item) {

                        } else if (it instanceof IntegerValueAVM2Item) {
                        } else {
                            isEmpty = false;
                            break;
                        }
                    }

                    //inlined finally     
                    if (!isEmpty) { //there must be at least single command before Throw
                        inlinedFinally = true;
                    }
                    //targetHasThrow = true;
                }

                List<GraphPart> tryStopPart = new ArrayList<>(stopPart);
                List<StopPartKind> tryStopPartKind = new ArrayList<>(stopPartKind);
                if (finallyPart != null) {
                    tryStopPart.add(finallyPart);
                    tryStopPartKind.add(StopPartKind.OTHER);
                }

                if (defaultPart != null) {
                    tryStopPart.add(defaultPart);
                    tryStopPartKind.add(StopPartKind.OTHER);
                }
                //switchPart == null && inlinedFinally &&
                if (afterPart == null) {
                    afterPart = searchFirstPartOutSideTryCatch(localData, finallyException, loops, allParts);
                }

                if (afterPart != null) {
                    tryStopPart.add(afterPart);
                    tryStopPartKind.add(StopPartKind.OTHER);
                }

                if (switchPart != null) {
                    afterPart = defaultPart;
                } else {
                    if (!inlinedFinally) {
                        afterPart = null;
                    }
                    finallyAsUnnamedException = localData.finallyKinds.get(finallyIndex) == FINALLY_KIND_UNKNOWN;
                }

                if (localData.pushDefaultPart.containsKey(finallyIndex)) {
                    exAfterPart = localData.pushDefaultPart.get(finallyIndex);
                    tryStopPart.add(exAfterPart);
                    tryStopPartKind.add(StopPartKind.OTHER);
                }

                tryCommands = printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, null, part, tryStopPart, tryStopPartKind, loops, throwStates, staticOperation, path);
                makeAllCommands(tryCommands, stack);
                processIfs(tryCommands);

                //there should be §§push(-1) left
                if (!tryCommands.isEmpty()
                        && (tryCommands.get(tryCommands.size() - 1) instanceof PushItem)
                        && (tryCommands.get(tryCommands.size() - 1).value instanceof IntegerValueAVM2Item)) {
                    tryCommands.remove(tryCommands.size() - 1);
                }

                List<GraphPart> finallyStopPart = new ArrayList<>(stopPart);
                List<StopPartKind> finallyStopPartKind = new ArrayList<>(stopPartKind);
                if (switchPart != null) {
                    finallyStopPart.add(switchPart);
                    finallyStopPartKind.add(StopPartKind.OTHER);
                }
                if (finallyPart != null) {
                    finallyCommands = printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, null, finallyPart, finallyStopPart, finallyStopPartKind, loops, throwStates, staticOperation, path);
                }
                if (switchPart != null) {
                    finallyCommands.addAll(translatePart(localData, switchPart, stack, staticOperation, path));
                    stack.pop(); //value switched by lookupswitch                   
                }
            }

            for (ABCException ex : catchedExceptions) {

                TranslateStack st2 = (TranslateStack) stack.clone();
                st2.clear();
                st2.add(new ExceptionAVM2Item(ex));

                GraphPart catchPart = null;
                for (GraphPart p : allParts) {
                    if (p.start == avm2code.adr2pos(ex.target)) {
                        catchPart = p;
                        break;
                    }
                }
                AVM2LocalData localData2 = new AVM2LocalData(localData);
                localData2.scopeStack = new ScopeStack();

                List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                List<StopPartKind> stopPartKind2 = new ArrayList<>(stopPartKind);
                stopPart2.add(exAfterPart);
                if (defaultPart != null) {
                    stopPart2.add(defaultPart);
                    stopPartKind2.add(StopPartKind.OTHER);
                }

                List<GraphTargetItem> currentCatchCommands = printGraph(foundGotos, partCodes, partCodePos, visited, localData2, st2, allParts, null, catchPart, stopPart2, stopPartKind2, loops, throwStates, staticOperation, path);
                /*if (!currentCatchCommands.isEmpty() && (currentCatchCommands.get(0) instanceof SetLocalAVM2Item)) {
                    if (currentCatchCommands.get(0).value.getNotCoerced() instanceof ExceptionAVM2Item) {
                        currentCatchCommands.remove(0);
                    }
                }*/
                loopwith:
                while (!currentCatchCommands.isEmpty() && (currentCatchCommands.get(0) instanceof WithAVM2Item)) {
                    WithAVM2Item w = (WithAVM2Item) currentCatchCommands.get(0);
                    if (w.scope instanceof LocalRegAVM2Item) {
                        int regId = ((LocalRegAVM2Item) w.scope).regIndex;
                        for (GraphTargetItem item : localData.scopeStack) {
                            if (item instanceof WithObjectAVM2Item) {
                                WithObjectAVM2Item wo = (WithObjectAVM2Item) item;

                                if (wo.scope instanceof SetLocalAVM2Item) {
                                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) wo.scope;
                                    if (setLocal.regIndex == regId) {
                                        currentCatchCommands.remove(0);
                                        int setLocalIp = localData.code.adr2pos(setLocal.getSrc().getAddress());
                                        int getLocalIp = localData.code.adr2pos(w.scope.getSrc().getAddress());
                                        localData.setLocalPosToGetLocalPos.get(setLocalIp).remove(getLocalIp);
                                        continue loopwith;
                                    }
                                }
                            }
                        }
                    }
                    break; //its a brand new with inside catch clause
                }
                if (!currentCatchCommands.isEmpty() && (currentCatchCommands.get(currentCatchCommands.size() - 1) instanceof SetLocalAVM2Item)) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) currentCatchCommands.get(currentCatchCommands.size() - 1);
                    if (setLocal.regIndex == switchedReg) {
                        currentCatchCommands.remove(currentCatchCommands.size() - 1);
                    }
                }
                catchCommands.add(currentCatchCommands);
            }

            for (ThrowState ts : throwStates) {
                if (catchedExceptionIds.contains(ts.exceptionId)) {
                    ts.state = 2;
                }
                if (ts.exceptionId == finallyIndex) {
                    ts.state = 2;
                }
            }

            if (!inlinedFinally && catchCommands.isEmpty() && finallyCommands.isEmpty() && tryCommands.isEmpty()) {
                return false;
            }

            //remove default assignment to switched register
            if (switchedReg != -1 && !currentRet.isEmpty()
                    && (currentRet.get(currentRet.size() - 1) instanceof SetLocalAVM2Item)
                    && (((SetLocalAVM2Item) currentRet.get(currentRet.size() - 1)).regIndex == switchedReg)) {
                currentRet.remove(currentRet.size() - 1);
            }

            if (!finallyAsUnnamedException && !inlinedFinally && catchedExceptions.isEmpty() && finallyCommands.isEmpty()) {
                currentRet.addAll(tryCommands);
                return true;
            }

            if (finallyAsUnnamedException) {
                catchedExceptions.add(finallyException);
                catchCommands.add(finallyCommands);
                finallyCommands = new ArrayList<>();
            }

            TryAVM2Item tryItem = new TryAVM2Item(tryCommands, catchedExceptions, catchCommands, finallyCommands, "");
            if (inlinedFinally) {
                List<List<GraphTargetItem>> parentCatchCommands = new ArrayList<>();
                parentCatchCommands.add(finallyTargetItems);
                List<ABCException> parentCatchedExceptions = new ArrayList<>();
                parentCatchedExceptions.add(finallyException);
                List<GraphTargetItem> parentTryCommands = new ArrayList<>();
                if (catchedExceptions.isEmpty() && finallyCommands.isEmpty()) {
                    parentTryCommands.addAll(tryCommands);
                } else {
                    parentTryCommands.add(tryItem);
                }
                tryItem = new TryAVM2Item(parentTryCommands, parentCatchedExceptions, parentCatchCommands, new ArrayList<>(), "");
            }

            if (tryItem.catchCommands.isEmpty()
                    && !tryItem.finallyCommands.isEmpty()
                    && tryItem.tryCommands.size() == 1
                    && (tryItem.tryCommands.get(0) instanceof TryAVM2Item)
                    && (((TryAVM2Item) tryItem.tryCommands.get(0)).finallyCommands.isEmpty())) {
                TryAVM2Item subTry = ((TryAVM2Item) tryItem.tryCommands.get(0));
                tryItem = new TryAVM2Item(subTry.tryCommands, subTry.catchExceptions, subTry.catchCommands, tryItem.finallyCommands, "");
            }

            currentRet.add(tryItem);

            //----- Merge continues from catches/try BEGIN
            processIfs(tryItem.tryCommands);
            processIfs(tryItem.finallyCommands);
            for (List<GraphTargetItem> cc : tryItem.catchCommands) {
                processIfs(cc);
            }

            List<List<GraphTargetItem>> blocksToCheck = new ArrayList<>();
            blocksToCheck.add(tryItem.tryCommands);
            blocksToCheck.addAll(tryItem.catchCommands);

            boolean allCntBreExit = true;
            for (List<GraphTargetItem> block : blocksToCheck) {
                if (block.isEmpty()) {
                    allCntBreExit = false;
                    break;
                }
                GraphTargetItem last = block.get(block.size() - 1);
                if (!(last instanceof ExitItem)
                        && !(last instanceof ContinueItem)
                        && !(last instanceof BreakItem)
                        && !(last instanceof GotoItem)) {
                    allCntBreExit = false;
                    break;
                }
            }

            if (allCntBreExit) {
                Map<Long, Integer> loopIdToCntCount = new HashMap<>();

                ContinueItem maxCountCnt = null;
                int maxCount = 0;

                for (List<GraphTargetItem> block : blocksToCheck) {
                    GraphTargetItem last = block.get(block.size() - 1);
                    if (last instanceof ContinueItem) {
                        ContinueItem cnt = (ContinueItem) last;
                        if (!loopIdToCntCount.containsKey(cnt.loopId)) {
                            loopIdToCntCount.put(cnt.loopId, 0);
                        }
                        int newCount = loopIdToCntCount.get(cnt.loopId) + 1;
                        if (newCount > maxCount) {
                            maxCount = newCount;
                            maxCountCnt = cnt;
                        }
                        loopIdToCntCount.put(cnt.loopId, newCount);
                    }
                }

                if (maxCountCnt != null) {
                    for (List<GraphTargetItem> block : blocksToCheck) {
                        GraphTargetItem last = block.get(block.size() - 1);
                        if (last instanceof ContinueItem) {
                            ContinueItem cnt = (ContinueItem) last;
                            if (cnt.loopId == maxCountCnt.loopId) {
                                block.remove(block.size() - 1);
                            }
                        }
                    }
                    currentRet.add(maxCountCnt);
                }
            }
            //----- Merge continues from catches/try END

            if (afterPart != null) {

                if (finallyIndex > -1 && localData.finallyIndicesWithDoublePush.contains(finallyIndex)) {
                    stack.push(new AnyItem());
                }
                printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, null, afterPart, stopPart, stopPartKind, loops, throwStates, currentRet, staticOperation, path, recursionLevel);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean canHandleVisited(BaseLocalData localData, GraphPart part) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        for (ABCException ex : body.exceptions) {
            if (aLocalData.parsedExceptions.contains(ex)) {
                continue;
            }
            int fixStart = avm2code.adr2pos(ex.start, true);
            if (part.start == fixStart) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected boolean canHandleLoop(BaseLocalData localData, GraphPart part, List<Loop> loops, List<ThrowState> throwStates) {
        Loop toBeLoop = null;
        for (Loop el : loops) {
            if ((el.loopContinue == part) && (el.phase == 0)) {
                toBeLoop = el;
                break;
            }
        }
        if (toBeLoop == null) {
            return true;
        }
        AVM2LocalData aLocalData = (AVM2LocalData) localData;

        boolean inTry = false;
        for (ABCException ex : body.exceptions) {
            if (aLocalData.parsedExceptions.contains(ex)) {
                continue;
            }
            int fixStart = avm2code.adr2pos(ex.start, true);
            int fixEnd = avm2code.adr2pos(ex.end, true);
            if (part.start == fixStart) {
                inTry = true;
                for (GraphPart be : toBeLoop.backEdges) {
                    if (be.start < fixStart || be.start >= fixEnd) {
                        //exists a backedge that is outside of try..catch
                        return true;
                    }
                }
            }
        }
        if (inTry) {
            //it is in try and there's no backedge that's outside try
            return false;
        }
        return true;
    }

    @Override
    protected boolean checkPartOutput(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, Loop currentLoop, int staticOperation, String path, int recursionLevel) throws InterruptedException {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        return checkTry(currentRet, foundGotos, partCodes, partCodePos, visited, aLocalData, part, stopPart, stopPartKind, loops, throwStates, allParts, stack, staticOperation, path, recursionLevel);
    }

    @Override
    protected List<GraphTargetItem> check(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) throws InterruptedException {
        List<GraphTargetItem> ret = null;

        /*if (ret != null) {
            return ret;
        }*/
        //Detect switch
        if ((part.nextParts.size() == 2) && (!stack.isEmpty()) && (stack.peek() instanceof StrictEqAVM2Item)) {
            GraphSourceItem switchStartItem = code.get(part.start);

            GraphTargetItem switchedObject = null;
            if (!output.isEmpty()) {
                if (output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                    switchedObject = ((SetLocalAVM2Item) output.get(output.size() - 1)).value;
                }
            }
            List<GraphTargetItem> caseValuesMapLeft = new ArrayList<>();
            List<GraphTargetItem> caseValuesMapRight = new ArrayList<>();

            StrictEqAVM2Item set = (StrictEqAVM2Item) stack.pop();
            caseValuesMapLeft.add(set.leftSide);
            caseValuesMapRight.add(set.rightSide);

            GraphPart origPart = part;
            List<GraphPart> caseBodyParts = new ArrayList<>();
            caseBodyParts.add(part.nextParts.get(0));
            GraphTargetItem top = null;
            int cnt = 1;
            while (part.nextParts.size() > 1
                    && part.nextParts.get(1).getHeight() > 1
                    && ((AVM2Instruction) code.get(part.nextParts.get(1).end >= code.size() ? code.size() - 1 : part.nextParts.get(1).end)).definition instanceof IfStrictEqIns
                    && ((top = translatePartGetStack(localData, part.nextParts.get(1), stack, staticOperation)) instanceof StrictEqAVM2Item)) {
                cnt++;
                part = part.nextParts.get(1);
                caseBodyParts.add(part.nextParts.get(0));

                set = (StrictEqAVM2Item) top;
                caseValuesMapLeft.add(set.leftSide);
                caseValuesMapRight.add(set.rightSide);
            }
            List<GraphTargetItem> caseValuesMap = caseValuesMapLeft;

            //determine whether local register are on left or on right side of === operator
            // -1 = there's no register, 
            // -2 = there are mixed registers, 
            // N = there is always register number N
            int leftReg = -1;
            int rightReg = -1;
            for (int cv = 0; cv < caseValuesMapLeft.size(); cv++) {
                if (caseValuesMapLeft.get(cv) instanceof LocalRegAVM2Item) {
                    int reg = ((LocalRegAVM2Item) caseValuesMapLeft.get(cv)).regIndex;
                    if (leftReg == -1) {
                        leftReg = reg;
                    } else {
                        if (leftReg != reg) {
                            leftReg = -2;
                        }
                    }
                }
                if (caseValuesMapRight.get(cv) instanceof LocalRegAVM2Item) {
                    int reg = ((LocalRegAVM2Item) caseValuesMapRight.get(cv)).regIndex;
                    if (rightReg == -1) {
                        rightReg = reg;
                    } else {
                        if (rightReg != reg) {
                            rightReg = -2;
                        }
                    }
                }
            }

            List<GraphTargetItem> otherSide = new ArrayList<>();
            if (leftReg > 0) {
                switchedObject = new LocalRegAVM2Item(null, null, leftReg, null);
                caseValuesMap = caseValuesMapRight;
                otherSide = caseValuesMapLeft;
            } else if (rightReg > 0) {
                switchedObject = new LocalRegAVM2Item(null, null, rightReg, null);
                otherSide = caseValuesMapRight;
            }

            if ((leftReg < 0 && rightReg < 0) || (cnt == 1)) {
                stack.push(set);
            } else {
                part = part.nextParts.get(1);
                GraphPart defaultPart = part;
                if (code.size() > defaultPart.start && ((AVM2Instruction) code.get(defaultPart.start)).definition instanceof JumpIns
                        && defaultPart.refs.size() == 1
                        && !partIsLoopContBrePre(defaultPart, loops, throwStates)) {
                    defaultPart = defaultPart.nextParts.get(0);
                }

                Reference<GraphPart> nextRef = new Reference<>(null);
                Reference<GraphTargetItem> tiRef = new Reference<>(null);
                SwitchItem sw = handleSwitch(switchedObject, switchStartItem, foundGotos, partCodes, partCodePos, visited, allParts, stack, stopPart, stopPartKind, loops, throwStates, localData, staticOperation, path, caseValuesMap, defaultPart, caseBodyParts, nextRef, tiRef);
                ret = new ArrayList<>();
                ret.addAll(output);
                checkSwitch(localData, sw, otherSide, ret.isEmpty() ? currentRet : ret /*hack :-(*/);
                ret.add(sw);
                if (nextRef.getVal() != null) {
                    if (tiRef.getVal() != null) {
                        ret.add(tiRef.getVal());
                    } else {
                        ret.addAll(printGraph(foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, null, nextRef.getVal(), stopPart, stopPartKind, loops, throwStates, staticOperation, path));
                    }
                }
            }
        }

        return ret;
    }

    @Override
    protected List<GraphPart> getNextParts(BaseLocalData localData, GraphPart part) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        /*if (aLocalData.finallyJumps.containsKey(part)) {
            List<GraphPart> ret = new ArrayList<>();
            ret.add(aLocalData.finallyJumps.get(part));
            return ret;
        }*/
        return super.getNextParts(localData, part);
    }

    @Override
    protected GraphPart checkPartWithOutput(List<GraphTargetItem> output, TranslateStack stack, BaseLocalData localData, GraphPart prev, GraphPart part, Set<GraphPart> allParts) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        if (aLocalData.finallyJumps == null) {
            aLocalData.finallyJumps = new HashMap<>();
        }
        if (aLocalData.ignoredSwitches == null) {
            aLocalData.ignoredSwitches = new HashMap<>();
        }

        if (aLocalData.finallyThrowParts.containsValue(part)) {
            return null;
        }

        if (prev != null) {
            if (!aLocalData.inGetLoops && aLocalData.ignoredSwitches.containsValue(prev)) {
                return null;
            }

            if (aLocalData.finallyJumps.containsKey(prev)) {
                GraphPart switchPart = null;
                int switchedReg = -1;

                for (GraphPart gp : aLocalData.finallyJumps.get(prev).refs) {
                    if (aLocalData.ignoredSwitches.containsValue(gp)) {

                        int finallyIndex = -1;
                        for (int fi : aLocalData.ignoredSwitches.keySet()) {
                            if (aLocalData.ignoredSwitches.get(fi) == gp) {
                                finallyIndex = fi;
                                break;
                            }
                        }

                        switchPart = gp;

                        switchedReg = aLocalData.switchedRegs.containsKey(finallyIndex) ? aLocalData.switchedRegs.get(finallyIndex) : -1;

                        break;
                    }
                }

                if (output != null && switchedReg > -1) {
                    if (!output.isEmpty()) {
                        if (output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                            SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(output.size() - 1);
                            if (setLocal.regIndex == switchedReg) {
                                output.remove(output.size() - 1);
                            }
                        }
                    }
                }
                int finallyIndex = aLocalData.finallyJumpsToFinallyIndex.get(prev);
                if (output != null && aLocalData.finallyIndicesWithDoublePush.contains(finallyIndex)) {
                    GraphPart defaultPart = aLocalData.finallyIndexToDefaultGraphPart.containsKey(finallyIndex) ? aLocalData.finallyIndexToDefaultGraphPart.get(finallyIndex) : null;
                    if (defaultPart == null) {
                        stack.push(new AnyItem());
                    } else if (defaultPart != aLocalData.finallyJumps.get(prev)) {
                        stack.push(new AnyItem());
                    }
                }
                if (output != null && switchedReg == -1 && !stack.isEmpty() && (stack.peek() instanceof IntegerValueAVM2Item)) {
                    stack.pop();
                } else if (output != null && switchedReg == -1 && !output.isEmpty() && (output.get(output.size() - 1) instanceof IntegerValueAVM2Item)) {
                    output.remove(output.size() - 1);
                }
                return aLocalData.finallyJumps.get(prev);
            }
        }
        return part;
    }

    @Override
    protected GraphPart checkPart(TranslateStack stack, BaseLocalData localData, GraphPart prev, GraphPart part, Set<GraphPart> allParts) {
        return checkPartWithOutput(null, stack, localData, prev, part, allParts);
    }

    @Override
    protected boolean isPartEmpty(GraphPart part) {
        if (part.nextParts.size() > 1) {
            return false;
        }
        if (part.start < 0) {
            return false;
        }
        for (int ip = part.start; ip <= part.end; ip++) {
            if (!(avm2code.code.get(ip).definition instanceof DebugLineIns)
                    && !(avm2code.code.get(ip).definition instanceof JumpIns)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected GraphTargetItem checkLoop(List<GraphTargetItem> output, LoopItem loopItem, BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates, TranslateStack stack) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        if (loopItem instanceof WhileItem) {
            WhileItem w = (WhileItem) loopItem;

            if ((!w.expression.isEmpty()) && (w.expression.get(w.expression.size() - 1) instanceof HasNextAVM2Item)) {
                HasNextAVM2Item hn = (HasNextAVM2Item) w.expression.get(w.expression.size() - 1);
                if (hn.obj != null) {
                    if (hn.obj.getNotCoerced().getThroughRegister().getNotCoerced() instanceof FilteredCheckAVM2Item) {

                        //All items are moved from stack to output before entering while,
                        // this code block moves them back to stack
                        int pushnum = 0;
                        for (int i = output.size() - 2 /*last is loop*/; i >= 0; i--) {
                            if (output.get(i) instanceof PushItem) {
                                pushnum++;
                            } else {
                                break;
                            }
                        }
                        int rem = output.size() - 1 - pushnum;
                        for (int i = output.size() - 1 - pushnum; i <= output.size() - 2; i++) {
                            stack.push(((PushItem) output.remove(rem)).value);
                        }
                        //---------- end moving back to stack
                        if (w.commands.size() >= 3) {
                            int pos = 0;
                            Set<Integer> localRegsToKill = new HashSet<>();

                            while (w.commands.get(pos) instanceof SetLocalAVM2Item) {
                                if (w.commands.get(pos).value instanceof NextValueAVM2Item) {
                                    NextValueAVM2Item nextValueItem = (NextValueAVM2Item) w.commands.get(pos).value;
                                    if (nextValueItem.index instanceof LocalRegAVM2Item) {
                                        localRegsToKill.add(((LocalRegAVM2Item) nextValueItem.index).regIndex);
                                    }
                                    if (nextValueItem.obj instanceof LocalRegAVM2Item) {
                                        localRegsToKill.add(((LocalRegAVM2Item) nextValueItem.obj).regIndex);
                                    }
                                }
                                pos++;
                            }
                            GraphTargetItem ft = w.commands.get(pos);
                            if (ft instanceof WithAVM2Item) {
                                pos++;
                                List<GraphTargetItem> withCommands = new ArrayList<>();
                                while (pos < w.commands.size() && !(w.commands.get(pos) instanceof WithEndAVM2Item)) {
                                    withCommands.add(w.commands.get(pos));
                                    pos++;
                                }

                                GraphTargetItem expr = null;
                                int getLocalObjectIp = -1;
                                int regIndex = -1;
                                HashMap<Integer, GraphTargetItem> localRegs = aLocalData.localRegs;

                                if (!withCommands.isEmpty()) {
                                    if (withCommands.get(withCommands.size() - 1) instanceof IfItem) {
                                        IfItem ift = (IfItem) withCommands.get(withCommands.size() - 1);
                                        if (ift.onTrue.size() > 0) {
                                            ft = ift.onTrue.get(0);
                                            if (ft instanceof SetPropertyAVM2Item) {
                                                SetPropertyAVM2Item spt = (SetPropertyAVM2Item) ft;
                                                if (spt.object instanceof LocalRegAVM2Item) {
                                                    getLocalObjectIp = avm2code.adr2pos(spt.object.getSrc().getAddress());
                                                    regIndex = ((LocalRegAVM2Item) spt.object).regIndex;
                                                }
                                            }
                                        }
                                        expr = ift.expression.getNotCoerced();
                                        if (withCommands.size() > 1) {
                                            withCommands.remove(withCommands.size() - 1);
                                            withCommands.add(expr);
                                            expr = new CommaExpressionItem(null, localData.lineStartInstruction, withCommands);
                                        }
                                    } else {
                                        //There is no if - this means there was something that
                                        // can be evaluated on compiletime and compiler removed the whole if
                                        // ASC2 does this
                                        withCommands.add(new FalseItem(null, localData.lineStartInstruction));
                                        expr = new CommaExpressionItem(null, localData.lineStartInstruction, withCommands);
                                    }
                                } else {
                                    expr = new FalseItem(null, localData.lineStartInstruction);
                                }
                                FilteredCheckAVM2Item filteredCheck = (FilteredCheckAVM2Item) hn.obj.getThroughRegister().getNotCoerced();
                                FilterAVM2Item filter = new FilterAVM2Item(null, null, filteredCheck.object, expr);

                                if (regIndex == -1) {
                                    for (int i = output.size() - 2 /*last is loop*/; i >= 0; i--) {
                                        if (output.get(i) instanceof SetLocalAVM2Item) {
                                            SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(i);
                                            if (setLocal.value instanceof ConstructAVM2Item) {
                                                ConstructAVM2Item construct = (ConstructAVM2Item) setLocal.value;

                                                boolean isXMLList = false;

                                                if (construct.object instanceof GetPropertyAVM2Item) {
                                                    GetPropertyAVM2Item gpt = (GetPropertyAVM2Item) construct.object;
                                                    if (gpt.object instanceof FindPropertyAVM2Item) {
                                                        FindPropertyAVM2Item fpt = (FindPropertyAVM2Item) gpt.object;
                                                        FullMultinameAVM2Item fptXmlMult = (FullMultinameAVM2Item) fpt.propertyName;
                                                        FullMultinameAVM2Item gptXmlMult = (FullMultinameAVM2Item) gpt.propertyName;

                                                        try {
                                                            isXMLList = fptXmlMult.isTopLevel("XMLList", aLocalData.abc, aLocalData.localRegNames, aLocalData.fullyQualifiedNames, aLocalData.seenMethods)
                                                                    && gptXmlMult.isTopLevel("XMLList", aLocalData.abc, aLocalData.localRegNames, aLocalData.fullyQualifiedNames, aLocalData.seenMethods);
                                                        } catch (InterruptedException ex) {
                                                            //ignore
                                                        }
                                                    }
                                                }
                                                if (construct.object instanceof GetLexAVM2Item) {
                                                    GetLexAVM2Item glt = (GetLexAVM2Item) construct.object;
                                                    isXMLList = glt.propertyName.getName(aLocalData.getConstants(), aLocalData.fullyQualifiedNames, true, true).equals("XMLList");
                                                }

                                                if (isXMLList) {
                                                    regIndex = setLocal.regIndex;
                                                }
                                            }
                                        } else {
                                            break;
                                        }
                                    }
                                }

                                localRegsToKill.add(regIndex);

                                if (hn.obj instanceof LocalRegAVM2Item) {
                                    localRegsToKill.add(((LocalRegAVM2Item) hn.obj).regIndex);
                                }

                                int setLocalIp = -1;
                                for (int i = output.size() - 2 /*last is loop*/; i >= 0; i--) {
                                    if (output.get(i) instanceof SetLocalAVM2Item) {
                                        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(i);
                                        if (setLocal.regIndex == regIndex) {
                                            setLocalIp = avm2code.adr2pos(setLocal.getSrc().getAddress());
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                                Set<Integer> usages = new HashSet<>();
                                if (setLocalIp > -1) {
                                    usages = new HashSet<>(aLocalData.getSetLocalUsages(setLocalIp));
                                    usages.remove(getLocalObjectIp);

                                }

                                for (int i = output.size() - 2 /*last is loop*/; i >= 0; i--) {
                                    if (localRegsToKill.isEmpty()) {
                                        break;
                                    }
                                    if (output.get(i) instanceof SetLocalAVM2Item) {
                                        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(i);
                                        if (localRegsToKill.contains(setLocal.regIndex)) {
                                            output.remove(i);
                                        }
                                        if (setLocal.regIndex == regIndex) {
                                            setLocal.value = filter;
                                        }
                                    } else {
                                        break;
                                    }
                                }

                                if (usages.isEmpty()) {
                                    output.add(filter);
                                } else {
                                    localRegs.put(regIndex, filter);
                                }
                                return null;

                            }
                        }
                    } else if (!w.commands.isEmpty()) {
                        if (w.commands.get(0) instanceof SetTypeAVM2Item) {
                            SetTypeAVM2Item sti = (SetTypeAVM2Item) w.commands.remove(0);
                            GraphTargetItem gti = sti.getValue().getNotCoerced();
                            GraphTargetItem varName = sti.getObject();
                            GraphTargetItem collection = hn.obj;

                            if (hn.obj instanceof LocalRegAVM2Item) {
                                int objRegIndex = ((LocalRegAVM2Item) hn.obj).regIndex;
                                for (int i = output.size() - 2 /*last is loop*/; i >= 0; i--) {
                                    if (output.get(i) instanceof SetLocalAVM2Item) {
                                        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(i);
                                        if (setLocal.regIndex == objRegIndex) {
                                            int setLocalIp = aLocalData.code.adr2pos(setLocal.getSrc().getAddress());
                                            Set<Integer> objUsages = new HashSet<>(aLocalData.getSetLocalUsages(setLocalIp));
                                            int hnUsageIp = aLocalData.code.adr2pos(hn.getSrc().getAddress());
                                            objUsages.remove(hnUsageIp);

                                            if (gti instanceof NextValueAVM2Item) {
                                                NextValueAVM2Item nextVal = (NextValueAVM2Item) gti;
                                                if (nextVal.obj instanceof LocalRegAVM2Item) {
                                                    LocalRegAVM2Item nextValObjReg = (LocalRegAVM2Item) nextVal.obj;
                                                    if (nextValObjReg.regIndex == objRegIndex) {
                                                        int nextValUsage = aLocalData.code.adr2pos(nextValObjReg.getSrc().getAddress());
                                                        objUsages.remove(nextValUsage);
                                                    }
                                                }
                                            }
                                            if (gti instanceof NextNameAVM2Item) {
                                                NextNameAVM2Item nextName = (NextNameAVM2Item) gti;
                                                if (nextName.obj instanceof LocalRegAVM2Item) {
                                                    LocalRegAVM2Item nextValObjReg = (LocalRegAVM2Item) nextName.obj;
                                                    if (nextValObjReg.regIndex == objRegIndex) {
                                                        int nextNameUsage = aLocalData.code.adr2pos(nextValObjReg.getSrc().getAddress());
                                                        objUsages.remove(nextNameUsage);
                                                    }
                                                }
                                            }
                                            if (objUsages.isEmpty()) {
                                                output.remove(i);
                                                collection = setLocal.value;
                                            }
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }

                            if (hn.index instanceof LocalRegAVM2Item) {
                                int indexRegIndex = ((LocalRegAVM2Item) hn.index).regIndex;
                                for (int i = output.size() - 2 /*last is loop*/; i >= 0; i--) {
                                    if (output.get(i) instanceof SetLocalAVM2Item) {
                                        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(i);
                                        if (setLocal.regIndex == indexRegIndex) {
                                            int setLocalIp = aLocalData.code.adr2pos(setLocal.getSrc().getAddress());
                                            Set<Integer> objUsages = new HashSet<>(aLocalData.getSetLocalUsages(setLocalIp));
                                            int hnUsageIp = aLocalData.code.adr2pos(hn.getSrc().getAddress());
                                            objUsages.remove(hnUsageIp);

                                            if (gti instanceof NextValueAVM2Item) {
                                                NextValueAVM2Item nextVal = (NextValueAVM2Item) gti;
                                                if (nextVal.index instanceof LocalRegAVM2Item) {
                                                    LocalRegAVM2Item nextValIndexReg = (LocalRegAVM2Item) nextVal.index;
                                                    if (nextValIndexReg.regIndex == indexRegIndex) {
                                                        int nextValUsage = aLocalData.code.adr2pos(nextValIndexReg.getSrc().getAddress());
                                                        objUsages.remove(nextValUsage);
                                                    }
                                                }
                                            }
                                            if (gti instanceof NextNameAVM2Item) {
                                                NextNameAVM2Item nextName = (NextNameAVM2Item) gti;
                                                if (nextName.index instanceof LocalRegAVM2Item) {
                                                    LocalRegAVM2Item nextValIndexReg = (LocalRegAVM2Item) nextName.index;
                                                    if (nextValIndexReg.regIndex == indexRegIndex) {
                                                        int nextNameUsage = aLocalData.code.adr2pos(nextValIndexReg.getSrc().getAddress());
                                                        objUsages.remove(nextNameUsage);
                                                    }
                                                }
                                            }
                                            if (objUsages.isEmpty()) {
                                                output.remove(i);
                                            }
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }

                            if (gti instanceof NextValueAVM2Item) {
                                return new ForEachInAVM2Item(w.getSrc(), w.getLineStartItem(), w.loop, new InAVM2Item(hn.getInstruction(), hn.getLineStartIns(), varName, collection), w.commands);
                            } else if (gti instanceof NextNameAVM2Item) {
                                return new ForInAVM2Item(w.getSrc(), w.getLineStartItem(), w.loop, new InAVM2Item(hn.getInstruction(), hn.getLineStartIns(), varName, collection), w.commands);
                            }
                        }
                    }
                }
            }
        }
        return loopItem;
    }

    @Override
    protected void finalProcessAfter(List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) {
        super.finalProcessAfter(list, level, localData, path);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof SetTypeAVM2Item) {
                if (((SetTypeAVM2Item) list.get(i)).getValue().getThroughDuplicate() instanceof ExceptionAVM2Item) {

                    boolean doRemove = false;
                    if (list.get(i) instanceof SetSlotAVM2Item) {
                        doRemove = true;
                    }
                    if (list.get(i) instanceof SetLocalAVM2Item) {
                        int setLocalIp = avm2code.adr2pos(list.get(i).getSrc().getAddress());
                        Set<Integer> usages = localData.getRegisterUsage(setLocalIp);
                        //Note: There's a hack in PushScopeIns to bypass usage of register to puscope for restoring scopestack
                        //for example in catch in catch
                        if (usages.isEmpty()) {
                            doRemove = true;
                        }
                    }
                    if (doRemove) {
                        list.remove(i);
                        i--;
                        continue;
                    }
                }
            }
        }
    }

    private boolean isIntegerOrPopInteger(GraphTargetItem item) {
        if (item instanceof IntegerValueAVM2Item) {
            return true;
        }
        if ((item instanceof PushItem) && (item.value instanceof IntegerValueAVM2Item)) {
            return true;
        }
        return false;
    }

    @Override
    protected void finalProcess(List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) throws InterruptedException {

        if (level == 0) {
            if (!list.isEmpty()) {
                if (list.get(list.size() - 1) instanceof ReturnVoidAVM2Item) {
                    list.remove(list.size() - 1);
                }
            }
        }

        if (level == 0) {
            Map<Integer, String> localRegNames = body.getLocalRegNames(abc);
            loopi:
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof SetSlotAVM2Item) {
                    SetSlotAVM2Item sslot = (SetSlotAVM2Item) list.get(i);
                    if (sslot.slotObject instanceof NewActivationAVM2Item) {
                        String slotName = sslot.slotName.getName(abc.constants, new ArrayList<>(), true, true);
                        if (sslot.value.getNotCoercedNoDup() instanceof LocalRegAVM2Item) {
                            LocalRegAVM2Item locReg = (LocalRegAVM2Item) sslot.value.getNotCoercedNoDup();
                            if (localRegNames.containsValue(slotName)) {
                                for (int regIndex : localRegNames.keySet()) {
                                    if (slotName.equals(localRegNames.get(regIndex))) {
                                        if (locReg.regIndex == regIndex) {
                                            list.remove(i);
                                            i--;
                                            continue loopi;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        List<GraphTargetItem> ret = list;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof IfItem) {
                IfItem ifi = (IfItem) list.get(i);
                if (((ifi.expression instanceof HasNextAVM2Item)
                        || ((ifi.expression instanceof NotItem)
                        && (((NotItem) ifi.expression).getOriginal() instanceof HasNextAVM2Item)))) {
                    HasNextAVM2Item hnt;
                    List<GraphTargetItem> body = new ArrayList<>();
                    List<GraphTargetItem> nextbody;//= new ArrayList<>();
                    if (ifi.expression instanceof NotItem) {
                        hnt = (HasNextAVM2Item) ((NotItem) ifi.expression).getOriginal();
                        body.addAll(ifi.onFalse);
                        for (int j = i + 1; j < list.size();) {
                            body.add(list.remove(i + 1));
                        }
                        nextbody = ifi.onTrue;
                    } else {
                        hnt = (HasNextAVM2Item) ifi.expression;
                        body = ifi.onTrue;
                        nextbody = ifi.onFalse;
                    }
                    if (!body.isEmpty()) {
                        if (body.get(0) instanceof SetTypeAVM2Item) {
                            SetTypeAVM2Item sti = (SetTypeAVM2Item) body.remove(0);
                            GraphTargetItem gti = sti.getValue().getNotCoerced();
                            GraphTargetItem repl = null;

                            if (gti instanceof NextValueAVM2Item) {
                                repl = new ForEachInAVM2Item(ifi.getSrc(), ifi.getLineStartItem(), new Loop(0, null, null), new InAVM2Item(null, null, sti.getObject(), hnt.obj), body);
                            } else if (gti instanceof NextNameAVM2Item) {
                                repl = new ForInAVM2Item(ifi.getSrc(), ifi.getLineStartItem(), new Loop(0, null, null), new InAVM2Item(null, null, sti.getObject(), hnt.obj), body);
                            }
                            if (repl != null) {
                                list.remove(i);
                                list.add(i, repl);
                                list.addAll(i + 1, nextbody);
                            }
                        }
                    }
                }
            }
        }

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i) instanceof WithAVM2Item) {
                WithAVM2Item wa = (WithAVM2Item) list.get(i);
                if (wa.scope instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) wa.scope;
                    int setLocalIp = avm2code.adr2pos(setLocal.getSrc().getAddress());
                    if (localData.getRegisterUsage(setLocalIp).isEmpty()) {
                        for (int j = i + 1; j < list.size(); j++) {
                            if (list.get(j) instanceof WithEndAVM2Item) {
                                WithEndAVM2Item we = (WithEndAVM2Item) list.get(j);
                                if (we.scope == wa.scope) {
                                    wa.scope = we.scope = setLocal.value;
                                }
                            }
                        }

                    }
                }
            }

            if (list.get(i) instanceof SetLocalAVM2Item) {
                SetLocalAVM2Item ri = (SetLocalAVM2Item) list.get(i);
                int setLocalIp = avm2code.adr2pos(ri.getSrc().getAddress());
                Set<Integer> usages = localData.getRegisterUsage(setLocalIp);
                if (ri.value.getNotCoerced() instanceof ExceptionAVM2Item) {
                    ExceptionAVM2Item ea = (ExceptionAVM2Item) ri.value.getNotCoerced();
                    if (ea.exception.isFinally()) {
                        list.remove(i);
                        localData.temporaryRegisters.add(ri.regIndex);
                        i--;
                        continue;
                    }
                }
                //if (usages.size() <= 1) 
                {
                    if (i + 1 < list.size()) {
                        if ((list.get(i + 1) instanceof ReturnValueAVM2Item)
                                && (list.get(i + 1).value instanceof LocalRegAVM2Item)
                                && (((LocalRegAVM2Item) list.get(i + 1).value).regIndex == ri.regIndex)) {
                            ReturnValueAVM2Item r = (ReturnValueAVM2Item) list.get(i + 1);
                            r.value = ri.value;
                            list.remove(i);
                            i--;
                            continue;
                        }
                        if ((list.get(i + 1) instanceof ThrowAVM2Item)
                                && (list.get(i + 1).value instanceof LocalRegAVM2Item)
                                && (((LocalRegAVM2Item) list.get(i + 1).value).regIndex == ri.regIndex)) {
                            ThrowAVM2Item t = (ThrowAVM2Item) list.get(i + 1);
                            t.value = ri.value;
                            list.remove(i);
                            i--;
                            continue;
                        }
                    }

                    //§§push(int) in every return/throw in try..finally block
                    //there may be multiple pushes as finnaly clauses may be nested
                    int numPushes = 0;
                    while (i + 1 + numPushes < list.size() && isIntegerOrPopInteger(list.get(i + 1 + numPushes))) {
                        numPushes++;
                    }
                    if (numPushes > 0) {
                        if (i + 1 + numPushes < list.size()) {
                            if (numPushes > 0 && (list.get(i + 1 + numPushes) instanceof ReturnValueAVM2Item)
                                    && (list.get(i + 1 + numPushes).value instanceof LocalRegAVM2Item)
                                    && (((LocalRegAVM2Item) list.get(i + 1 + numPushes).value).regIndex == ri.regIndex)) {
                                ReturnValueAVM2Item r = (ReturnValueAVM2Item) list.get(i + 1 + numPushes);
                                r.value = ri.value;
                                for (int n = 0; n < numPushes; n++) {
                                    list.remove(i + 1);
                                }
                                list.remove(i);
                                i--;
                                continue;
                            }
                            if (numPushes > 0 && (list.get(i + 1 + numPushes) instanceof ThrowAVM2Item)
                                    && (list.get(i + 1 + numPushes).value instanceof LocalRegAVM2Item)
                                    && (((LocalRegAVM2Item) list.get(i + 1 + numPushes).value).regIndex == ri.regIndex)) {
                                ThrowAVM2Item t = (ThrowAVM2Item) list.get(i + 1 + numPushes);
                                t.value = ri.value;
                                for (int n = 0; n < numPushes; n++) {
                                    list.remove(i + 1);
                                }
                                list.remove(i);
                                i--;
                                continue;
                            }
                        } else if (i + numPushes < list.size() && usages.isEmpty()) {
                            for (int n = 0; n < numPushes; n++) {
                                list.remove(i + 1);
                            }
                            list.remove(i);
                            i--;
                            continue;
                        }
                    }
                }
            }

            //§§push(int) before every continue/returnvoid in try..finally block
            //there may be multiple pushes as finnaly clauses may be nested
            //TODO: handle this better - actually remove only really needed
            if ((list.get(i) instanceof ContinueItem) || (list.get(i) instanceof BreakItem) || (list.get(i) instanceof ReturnVoidAVM2Item)) {
                for (int j = i - 1; j >= 0; j--) {
                    if (isIntegerOrPopInteger(list.get(j))) {
                        list.remove(j);
                        i--;
                    } else {
                        break;
                    }
                }
            }
        }

        //Handle for loops at the end:
        super.finalProcess(list, level, localData, path);
    }

    @Override
    protected FinalProcessLocalData getFinalData(BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates) {
        FinalProcessLocalData finalProcess = super.getFinalData(localData, loops, throwStates);
        finalProcess.registerUsage = ((AVM2LocalData) localData).setLocalPosToGetLocalPos;
        return finalProcess;
    }

    @Override
    public AVM2LocalData prepareBranchLocalData(BaseLocalData localData) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        AVM2LocalData ret = new AVM2LocalData(aLocalData);
        ScopeStack copyScopeStack = new ScopeStack();
        copyScopeStack.addAll(ret.scopeStack);
        ret.scopeStack = copyScopeStack;
        return ret;
    }

    @Override
    protected void checkSwitch(BaseLocalData localData, SwitchItem switchItem, Collection<? extends GraphTargetItem> otherSides, List<GraphTargetItem> output) {
        if (output.isEmpty()) {
            return;
        }
        if (!(output.get(output.size() - 1) instanceof SetLocalAVM2Item)) {
            return;
        }
        AVM2LocalData avm2LocalData = (AVM2LocalData) localData;
        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(output.size() - 1);
        int setLocalIp = InstructionDefinition.getItemIp(avm2LocalData, setLocal);;
        Set<Integer> allUsages = new HashSet<>(avm2LocalData.getSetLocalUsages(setLocalIp));
        boolean isOtherSideReg = false;
        for (GraphTargetItem otherSide : otherSides) {
            if (otherSide instanceof LocalRegAVM2Item) {
                LocalRegAVM2Item otherLog = (LocalRegAVM2Item) otherSide;
                if (otherLog.regIndex != setLocal.regIndex) {
                    break;
                }
                int getLocalIp = InstructionDefinition.getItemIp(avm2LocalData, otherLog);
                allUsages.remove((Integer) getLocalIp);
                isOtherSideReg = true;
            }
        }
        if (!isOtherSideReg) {
            return;
        }
        if (allUsages.isEmpty()) {
            output.remove(output.size() - 1);
            switchItem.switchedObject = setLocal.value;
        }
    }

    @Override
    protected boolean partIsSwitch(GraphPart part) {
        if (part.end < 0) {
            return false;
        }
        return avm2code.code.get(part.end).definition instanceof LookupSwitchIns;
    }

    @Override
    protected List<ThrowState> getThrowStates(BaseLocalData localData, Set<GraphPart> allParts) {

        AVM2LocalData avm2LocalData = (AVM2LocalData) localData;
        avm2LocalData.codeStats = avm2LocalData.code.getStats(avm2LocalData.abc, avm2LocalData.methodBody, avm2LocalData.methodBody.init_scope_depth, false);

        List<ThrowState> ret = new ArrayList<>();
        for (int e = 0; e < body.exceptions.length; e++) {
            ThrowState ts = new ThrowState();
            ts.exceptionId = e;
            ts.state = 0;
            ts.targetPart = searchPart(code.adr2pos(body.exceptions[e].target), allParts);
            int startIp = code.adr2pos(body.exceptions[e].start, true);
            int endIp = code.adr2pos(body.exceptions[e].end, true);
            ts.startPart = searchPart(startIp, allParts);
            for (GraphPart p : allParts) {
                if (p.start >= startIp && p.start < endIp) {
                    ts.throwingParts.add(p);
                }
            }
            GraphPart part = ts.targetPart;
            boolean wasNewCatch = false;
            int scopePos = -1;
            int ip = part.start;
            for (; ip <= part.end; ip++) {

                if (avm2code.code.get(ip).definition instanceof NewCatchIns) {
                    wasNewCatch = true;
                }
                if (avm2code.code.get(ip).definition instanceof PushScopeIns) {
                    if (wasNewCatch) {
                        scopePos = avm2LocalData.codeStats.instructionStats[ip].scopepos_after;
                        break;
                    }
                }
                if (ip == part.end && part.nextParts.size() == 1 && part.nextParts.get(0).refs.size() == 1) {
                    part = part.nextParts.get(0);
                    ip = part.start - 1;
                    continue;
                }
            }

            //Search all parts which have same or greater scope level, these all belong to catch
            Set<GraphPart> catchParts = new HashSet<>();
            if (scopePos > -1) {
                walkCatchParts(avm2LocalData.codeStats, part, ip, catchParts, scopePos);
            } else {
                logger.fine("No newcatch..pushscope found in catch, probably swftools");
                part = ts.targetPart;
                ip = part.start;
                int localRegId = -1;
                for (; ip <= part.end; ip++) {
                    AVM2Instruction ins = avm2code.code.get(ip);
                    if ((ins.definition instanceof NopIns)
                            || (ins.definition instanceof DebugLineIns)
                            || (ins.definition instanceof JumpIns)) {
                        //ignore
                    } else if (ins.definition instanceof SetLocalTypeIns) {
                        localRegId = (((SetLocalTypeIns) ins.definition).getRegisterId(ins));
                        break;
                    } else {
                        break;
                    }
                    if (ip == part.end && part.nextParts.size() == 1 && part.nextParts.get(0).refs.size() == 1) {
                        part = part.nextParts.get(0);
                        ip = part.start - 1;
                        continue;
                    }
                }
                if (localRegId == -1) {
                    logger.fine("Not even local reg assignment found in catch, weird :-(");
                } else {
                    walkCatchPartsReg(localRegId, part, ip + 1, catchParts, new ArrayList<>(), new HashSet<>());
                }
            }
            ts.catchParts = catchParts;
            ret.add(ts);
        }
        return ret;
    }

    private void walkCatchPartsReg(int registerId, GraphPart part, int startIp, Set<GraphPart> catchParts, List<GraphPart> path, Set<GraphPart> visited) {
        if (visited.contains(part)) {
            return;
        }
        visited.add(part);
        List<GraphPart> newPath = new ArrayList<>(path);
        newPath.add(part);
        for (int ip = startIp; ip <= part.end; ip++) {
            AVM2Instruction ins = avm2code.code.get(ip);
            if (ins.definition instanceof SetLocalTypeIns) {
                int setLocalId = ((SetLocalTypeIns) ins.definition).getRegisterId(ins);
                if (setLocalId == registerId) {
                    return;
                }
            }
            if (ins.definition instanceof KillIns) {
                int killId = ins.operands[0];
                if (killId == registerId) {
                    return;
                }
            }
            if (ins.definition instanceof GetLocalTypeIns) {
                int getLocalId = ((GetLocalTypeIns) ins.definition).getRegisterId(ins);
                if (getLocalId == registerId) {
                    catchParts.addAll(newPath);
                }
            }
        }
        for (GraphPart n : part.nextParts) {
            walkCatchPartsReg(registerId, n, n.start, catchParts, newPath, visited);
        }
    }

    private void walkCatchParts(CodeStats stats, GraphPart part, int startIp, Set<GraphPart> catchParts, int scopePos) {
        if (catchParts.contains(part)) {
            return;
        }
        catchParts.add(part);
        for (int ip = startIp; ip <= part.end; ip++) {
            if (stats.instructionStats[ip].scopepos_after < scopePos) {
                return;
            }
        }
        for (GraphPart n : part.nextParts) {
            walkCatchParts(stats, n, n.start, catchParts, scopePos);
        }
    }

    @Override
    protected void makeAllCommands(List<GraphTargetItem> commands, TranslateStack stack) {
        for (int i = 0; i < stack.size(); i++) {
            //These are often obfuscated, so ignore them                
            if (stack.get(i) instanceof NewFunctionAVM2Item) {
                stack.remove(i);
                i--;
            }
        }
        super.makeAllCommands(commands, stack);
    }


}

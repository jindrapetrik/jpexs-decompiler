/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.avm2.AVM2FinalProcessLocalData;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.DecLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.decimalsupport.IncLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.CoerceAIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertIIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
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
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.graph.AbstractGraphTargetRecursiveVisitor;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphException;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphPartChangeException;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.StopPartKind;
import com.jpexs.decompiler.graph.ThrowState;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.AnyItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LoopItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TrueItem;
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
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AVM2 graph.
 *
 * @author JPEXS
 */
public class AVM2Graph extends Graph {

    /**
     * AVM2 code
     */
    private final AVM2Code avm2code;

    /**
     * ABC
     */
    private final ABC abc;

    /**
     * Method body
     */
    private final MethodBody body;

    /**
     * ABC indexing
     */
    private final AbcIndexing abcIndex;

    /**
     * Logger
     */
    private final Logger logger = Logger.getLogger(AVM2Graph.class.getName());

    //Kinds of finally blocks
    final int FINALLY_KIND_STACK_BASED = 0;
    final int FINALLY_KIND_REGISTER_BASED = 1;
    final int FINALLY_KIND_INLINED = 2;
    final int FINALLY_KIND_UNKNOWN = -1;

    /**
     * Get AVM2 code
     *
     * @return AVM2 code
     */
    public AVM2Code getCode() {
        return avm2code;
    }

    /**
     * Get exceptions
     *
     * @param body Method body
     * @return Exceptions
     */
    private static List<GraphException> getExceptionEntries(MethodBody body) {
        List<GraphException> ret = new ArrayList<>();
        AVM2Code code = body.getCode();
        for (ABCException e : body.exceptions) {
            ret.add(new GraphException(code.adr2pos(e.start, true), code.adr2pos(e.end, true), code.adr2pos(e.target)));
        }
        return ret;
    }

    /**
     * Constructs AVM2 graph
     *
     * @param abcIndex ABC indexing
     * @param code AVM2 code
     * @param abc ABC
     * @param body Method body
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param localRegs Local registers
     * @param scopeStack Scope stack
     * @param localScopeStack Local scope stack
     * @param localRegNames Local register names
     * @param fullyQualifiedNames Fully qualified names
     * @param localRegAssignmentIps Local register assignment IPs
     */
    public AVM2Graph(AbcIndexing abcIndex, AVM2Code code, ABC abc, MethodBody body, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, ScopeStack scopeStack, ScopeStack localScopeStack, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, HashMap<Integer, Integer> localRegAssignmentIps) {
        super(new AVM2GraphSource(code, isStatic, scriptIndex, classIndex, localRegs, abc, body, localRegNames, fullyQualifiedNames, localRegAssignmentIps), getExceptionEntries(body));
        this.avm2code = code;
        this.abc = abc;
        this.body = body;
        this.abcIndex = abcIndex;
    }

    /**
     * Checks whether part can be break candidate
     *
     * @param localData Local data
     * @param part Part
     * @param throwStates List of throw states
     * @return True if part can be break candidate, false otherwise
     */
    @Override
    protected boolean canBeBreakCandidate(BaseLocalData localData, GraphPart part, List<ThrowState> throwStates) {
        for (ThrowState ts : throwStates) {
            if (ts.targetPart == part) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check before get loops
     *
     * @param localData Local data
     * @param path Path
     * @param allParts All parts
     * @param throwStates Throw states
     * @throws InterruptedException On interrupt
     */
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

    /**
     * Check after get loops
     *
     * @param localData Local data
     * @param path Path
     * @param allParts All parts
     * @throws InterruptedException On interrupt
     */
    @Override
    protected void afterGetLoops(BaseLocalData localData, String path, Set<GraphPart> allParts) throws InterruptedException {
        ((AVM2LocalData) localData).inGetLoops = false;
    }

    /**
     * Gets ignored switches
     *
     * @param localData Local data
     * @param allParts All parts
     * @throws InterruptedException On interrupt
     */
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
            //localData2.scopeStack = new ScopeStack();
            localData2.localScopeStack = new ScopeStack();

            List<GraphTargetItem> targetOutput;
            try {
                targetOutput = translatePart(localData2, finallyTryTargetPart, finallyTryTargetStack, 0 /*??*/, "try_target");
            } catch (GraphPartChangeException ex1) { //should not happen
                targetOutput = new ArrayList<>();
            }

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

                if (prevFinallyEndPart != null) {
                    for (int j = prevFinallyEndPart.start; j <= prevFinallyEndPart.end; j++) {
                        AVM2Instruction ins = avm2code.code.get(j);
                        if (ins.definition instanceof NopIns) {
                            //empty
                        } else if (ins.definition instanceof PushByteIns) {
                            defaultPushByte = ins.operands[0];
                            localData.pushDefaultPart.put(e, prevFinallyEndPart);
                        } else if (ins.definition instanceof JumpIns) {
                            //empty
                        } else {
                            if (localData.pushDefaultPart.containsKey(e)) {
                                localData.pushDefaultPart.remove(e);
                            }
                            defaultPushByte = null;
                            break;
                        }
                    }
                }

                if (defaultPushByte == null) {
                    if (prevFinallyEndPart != null && (avm2code.code.get(prevFinallyEndPart.end).definition instanceof JumpIns)) {
                        prevFinallyEndPart = prevFinallyEndPart.nextParts.get(0);
                        if (prevFinallyEndPart.nextParts.size() == 1 && prevFinallyEndPart.nextParts.get(0).refs.size() > 1) {
                            for (int j = prevFinallyEndPart.start; j <= prevFinallyEndPart.end; j++) {
                                AVM2Instruction ins = avm2code.code.get(j);
                                if (ins.definition instanceof NopIns) {
                                    //empty
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
                    GraphPart finallyThrowPart;
                    if (finallyThrowPushByte == null || finallyThrowPushByte < 0 || finallyThrowPushByte > switchPart.nextParts.size() - 2) {
                        finallyThrowPart = switchPart.nextParts.get(0);
                    } else {
                        finallyThrowPart = switchPart.nextParts.get(1 + finallyThrowPushByte);
                    }
                    localData.finallyThrowParts.put(e, finallyThrowPart);
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

                //This caused problems, so it's commented out
                //the tests still pass, so I can only wonder why it's there. :-(
                //return in finally block is joined after switch decision                
                /*for (GraphPart p : switchPart.nextParts) {
                    for (GraphPart r : p.refs) {
                        if (r != switchPart) {
                            localData.finallyJumps.put(r, p);
                            localData.finallyJumpsToFinallyIndex.put(r, e);
                        }
                    }
                }*/
                localData.ignoredSwitches.put(e, switchPart);
            } else {
                //there is probably return in all branches and no other way outside finally
            }
        }
    }

    /**
     * Walk local registers usage
     *
     * @param throwStates List of throw states
     * @param localData Local data
     * @param getLocalPos Get local position
     * @param startPart Start part
     * @param part Part
     * @param visited Visited
     * @param ip IP
     * @param searchRegId Search register ID
     */
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

    /**
     * Calculates local registers usage.
     * <p>
     * TODO: optimize this to make it faster!!!
     *
     * @param throwStates List of throw states
     * @param localData Local data
     * @param ignoredSwitches Ignored switches
     * @param path Path
     * @param allParts All parts
     * @return Map of getLocal to setLocal set
     */
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

    /**
     * Translates via Graph - decompiles.
     *
     * @param secondPassData Second pass data
     * @param callStack Call stack
     * @param abcIndex ABC indexing
     * @param path Path
     * @param code AVM2 code
     * @param abc ABC
     * @param body Method body
     * @param isStatic Is static
     * @param scriptIndex Script index
     * @param classIndex Class index
     * @param localRegs Local registers
     * @param scopeStack Scope stack
     * @param localRegNames Local register names
     * @param localRegTypes Local register types
     * @param fullyQualifiedNames Fully qualified names
     * @param staticOperation Unused
     * @param localRegAssignmentIps Local register assignment IPs
     * @param thisHasDefaultToPrimitive This has default to primitive
     * @return List of graph target items
     * @throws InterruptedException On interrupt
     */
    public static List<GraphTargetItem> translateViaGraph(SecondPassData secondPassData, List<MethodBody> callStack, AbcIndexing abcIndex, String path, AVM2Code code, ABC abc, MethodBody body, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, ScopeStack scopeStack, HashMap<Integer, String> localRegNames, HashMap<Integer, GraphTargetItem> localRegTypes, List<DottedChain> fullyQualifiedNames, int staticOperation, HashMap<Integer, Integer> localRegAssignmentIps, boolean thisHasDefaultToPrimitive) throws InterruptedException {
        ScopeStack localScopeStack = new ScopeStack();
        AVM2Graph g = new AVM2Graph(abcIndex, code, abc, body, isStatic, scriptIndex, classIndex, localRegs, scopeStack, localScopeStack, localRegNames, fullyQualifiedNames, localRegAssignmentIps);

        AVM2LocalData localData = new AVM2LocalData();
        localData.secondPassData = secondPassData;
        localData.thisHasDefaultToPrimitive = thisHasDefaultToPrimitive;
        localData.isStatic = isStatic;
        localData.classIndex = classIndex;
        localData.localRegs = localRegs;
        localData.scopeStack = scopeStack;
        localData.localScopeStack = localScopeStack;
        localData.methodBody = body;
        localData.callStack = callStack;
        localData.abc = abc;
        localData.abcIndex = abcIndex;
        localData.localRegNames = localRegNames;
        localData.localRegTypes = localRegTypes;
        localData.fullyQualifiedNames = fullyQualifiedNames;
        localData.scriptIndex = scriptIndex;
        localData.ip = 0;
        localData.code = code;
        g.init(localData);
        Set<GraphPart> allParts = new HashSet<>();
        for (GraphPart head : g.heads) {
            populateParts(head, allParts);
        }
        return g.translate(localData, staticOperation, path);
    }

    /**
     * Check graph.
     *
     * @param allBlocks All blocks
     */
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

    /**
     * Finds lookup switch with get local.
     *
     * @param registerId Register ID
     * @param part Part
     * @param visited Visited
     * @return Graph part
     */
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

    /**
     * Finds lookup switch with get local.
     *
     * @param registerId Register ID
     * @param part Part
     * @return Graph part
     */
    private GraphPart findLookupSwitchWithGetLocal(int registerId, GraphPart part) {
        return findLookupSwitchWithGetLocal(registerId, part, new HashSet<>());
    }

    /**
     * Finds all pops.
     *
     * @param localData Local data
     * @param stackLevel Stack level
     * @param part Part
     * @param foundIps Found IPs
     * @param foundParts Found parts
     * @param visited Visited
     */
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

    /**
     * Gets real references. Real = start >= 0
     *
     * @param part Part
     * @return List of graph parts
     */
    private List<GraphPart> getRealRefs(GraphPart part) {
        List<GraphPart> ret = new ArrayList<>();
        for (GraphPart r : part.refs) {
            if (r.start >= 0) {
                ret.add(r);
            }
        }
        return ret;
    }

    /**
     * Search first part outside try-catch.
     *
     * @param localData Local data
     * @param ex Exception
     * @param loops Loops
     * @param allParts All parts
     * @return Graph part
     */
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

    /**
     * Gets nearest non-empty part.
     *
     * @param part Part
     * @return Graph part
     */
    private GraphPart nearestNonEmptyPart(GraphPart part) {
        while (isPartEmpty(part)) {
            part = part.nextParts.get(0);
        }
        return part;
    }

    /**
     * Gets catched exception IDs.
     *
     * @param part Part
     * @param previouslyCatchedExceptionIds Previously catched exception IDs
     * @param catchedExceptionIds Catched exception IDs
     * @param finallyIndex Finally index
     * @param allParts All parts
     * @param loops Loops
     * @param localData Local data
     */
    private void getCatchedExceptionIds(GraphPart part, List<Integer> previouslyCatchedExceptionIds, List<Integer> catchedExceptionIds, Reference<Integer> finallyIndex, Collection<? extends GraphPart> allParts,
            List<Loop> loops, AVM2LocalData localData) {

        long addr = avm2code.pos2adr(part.start);
        long maxEndAddr = -1;
        finallyIndex.setVal(-1);

        List<Integer> finallysIndicesToBe = new ArrayList<>();
        maxEndAddr = -1;
        for (int e = 0; e < body.exceptions.length; e++) {
            long fixedExStart = avm2code.pos2adr(avm2code.adr2pos(body.exceptions[e].start, true));
            long fixedExEnd = avm2code.pos2adr(avm2code.adr2pos(body.exceptions[e].end, true));
            if (!previouslyCatchedExceptionIds.contains(e)) {
                if (addr == fixedExStart) { //avm2code.getAddrThroughJumpAndDebugLine(fixedExStart)) {
                    ABCException ex = body.exceptions[e];
                    if (ex.isFinally()) {
                        if (fixedExEnd >= maxEndAddr) {
                            finallysIndicesToBe.add(e);
                        }
                    } else {
                        long endAddr = fixedExEnd;
                        if (endAddr > maxEndAddr) {
                            catchedExceptionIds.clear();
                            maxEndAddr = fixedExEnd;

                            catchedExceptionIds.add(e);

                            //filter finallys that have lower endAddr - they do not belong to these catches
                            for (int k = 0; k < finallysIndicesToBe.size(); k++) {
                                if (body.exceptions[finallysIndicesToBe.get(k)].end < endAddr) {
                                    finallysIndicesToBe.remove(k);
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

        Collections.sort(finallysIndicesToBe, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return body.exceptions[o2].end - body.exceptions[o1].end;
            }
        });

        GraphPart outSideExceptionPart = null;
        if (!catchedExceptionIds.isEmpty()) {
            outSideExceptionPart = searchFirstPartOutSideTryCatch(localData, body.exceptions[catchedExceptionIds.get(0)], loops, allParts);
        }

        if (!finallysIndicesToBe.isEmpty()) {
            long maxEnd = 0;
            int maxF = -1;
            for (int f : finallysIndicesToBe) {
                long fixedExEnd = avm2code.pos2adr(avm2code.adr2pos(body.exceptions[f].end, true));
                if (fixedExEnd > maxEnd) {
                    maxEnd = fixedExEnd;
                    maxF = f;
                }
            }
            finallysIndicesToBe.clear();
            finallysIndicesToBe.add(maxF);
        }

        for (int e : finallysIndicesToBe) {
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
                        //empty
                    } else if (ins.definition instanceof JumpIns) {
                        //empty
                    } else if (ins.definition instanceof NopIns) {
                        //empty
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

        if (finallyIndex.getVal() == -1 && !finallysIndicesToBe.isEmpty()) {
            catchedExceptionIds.clear();
            finallyIndex.setVal(finallysIndicesToBe.get(0));
        }
    }

    /**
     * Finds nearest part outside catch.
     *
     * @param tryTarget Try target
     * @param catchParts Catch parts
     * @return Graph part
     */
    private GraphPart findNearestPartOutsideCatch(GraphPart tryTarget, Set<GraphPart> catchParts) {
        for (GraphPart p : catchParts) {
            for (GraphPart n : p.nextParts) {
                if (!catchParts.contains(n)) {
                    return n;
                }
            }
        }
        return null;
    }

    /**
     * Checks try.
     *
     * @param currentRet Current return
     * @param foundGotos Found gotos
     * @param partCodes Part codes
     * @param partCodePos Part code position
     * @param visited Visited
     * @param localData Local data
     * @param part Part
     * @param stopPart Stop part
     * @param stopPartKind Stop part kind
     * @param loops Loops
     * @param throwStates Throw states
     * @param allParts All parts
     * @param stack Stack
     * @param staticOperation Unused
     * @param path Path
     * @param recursionLevel Recursion level
     * @return True if try is found
     * @throws InterruptedException On interrupt
     */
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

                afterPart = getMostCommonPart(localData, partsToCalCommon, loops, throwStates, new ArrayList<>() /*?*/);
                //System.err.println("result: " + afterPart);
            }

            if (catchedExceptions.size() > 0 && afterPart == null) {
                //in all catches is probably continue/return/break or something
                //we need to search a part which is first outside the try..block
                afterPart = searchFirstPartOutSideTryCatch(localData, catchedExceptions.get(0), loops, allParts);
                //System.err.println("oursidetrycatch: " + afterPart);
            }

            //List<GraphPart> catchedExceptionsAfter = new ArrayList<>();
            List<GraphPart> catchAfterParts = new ArrayList<>();

            if (afterPart == null) {

                loope:
                for (int e = 0; e < catchedExceptions.size(); e++) {
                    ABCException ex = catchedExceptions.get(e);
                    int eId = catchedExceptionIds.get(e);
                    for (ThrowState ts : throwStates) {
                        if (ts.exceptionId == eId) {
                            GraphPart possibleAfter = findNearestPartOutsideCatch(ts.targetPart, ts.catchParts);
                            if (possibleAfter != null) {
                                if (!stopPart.contains(possibleAfter)) {
                                    catchAfterParts.add(possibleAfter);
                                }
                            }
                        }
                    }
                }
                if (catchAfterParts.size() == 1) {
                    afterPart = catchAfterParts.iterator().next();
                } else if (catchAfterParts.size() > 1) {
                    afterPart = getMostCommonPart(localData, catchAfterParts, loops, throwStates, stopPart);
                }
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
                //localData2.scopeStack = new ScopeStack();
                localData2.localScopeStack = new ScopeStack();

                try {
                    //We are assuming Finally target has only 1 part
                    finallyTargetItems = translatePart(localData2, finallyTryTargetPart, st2, staticOperation, path);
                } catch (GraphPartChangeException ex) { //should not happen
                    finallyTargetItems = new ArrayList<>();
                }
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
                            //empty
                        } else if (it instanceof IntegerValueAVM2Item) {
                            //empty
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
                    try {
                        finallyCommands.addAll(translatePart(localData, switchPart, stack, staticOperation, path));
                    } catch (GraphPartChangeException ex) {
                        //should not happen
                    }
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
                //localData2.scopeStack = new ScopeStack();
                localData2.localScopeStack = new ScopeStack();

                List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                List<StopPartKind> stopPartKind2 = new ArrayList<>(stopPartKind);
                stopPart2.add(exAfterPart);
                if (defaultPart != null) {
                    stopPart2.add(defaultPart);
                    stopPartKind2.add(StopPartKind.OTHER);
                }

                for (GraphPart p : catchAfterParts) {
                    stopPart2.add(p);
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
                        for (GraphTargetItem item : localData.localScopeStack) {
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

    /**
     * Checks whether the part can handle visited.
     *
     * @param localData Local data
     * @param part Graph part
     * @return True if the part can handle visited
     */
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

    /**
     * Checks whether the part can handle loop.
     *
     * @param localData Local data
     * @param part Graph part
     * @param loops List of loops
     * @param throwStates List of throw states
     * @return True if the part can handle loop
     */
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

    /**
     * Check part output.
     *
     * @param currentRet Current return
     * @param foundGotos Found gotos
     * @param partCodes Part codes
     * @param partCodePos Part code position
     * @param visited Visited
     * @param code Code
     * @param localData Local data
     * @param allParts All parts
     * @param stack Stack
     * @param parent Parent part
     * @param part Part
     * @param stopPart Stop part
     * @param stopPartKind Stop part kind
     * @param loops Loops
     * @param throwStates Throw states
     * @param currentLoop Current loop
     * @param staticOperation Unused
     * @param path Path
     * @param recursionLevel Recursion level
     * @return True to stop processing. False to continue.
     * @throws InterruptedException On interrupt
     */
    @Override
    protected boolean checkPartOutput(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos,
            Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos,
            Set<GraphPart> visited, GraphSource code,
            BaseLocalData localData, Set<GraphPart> allParts,
            TranslateStack stack, GraphPart parent,
            GraphPart part, List<GraphPart> stopPart,
            List<StopPartKind> stopPartKind, List<Loop> loops,
            List<ThrowState> throwStates, Loop currentLoop,
            int staticOperation, String path,
            int recursionLevel) throws InterruptedException {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        return checkTry(currentRet, foundGotos, partCodes, partCodePos, visited, aLocalData, part, stopPart, stopPartKind, loops, throwStates, allParts, stack, staticOperation, path, recursionLevel);
    }

    /**
     * Check before decompiling next section. Override this method to provide
     * custom behavior.
     *
     * @param currentRet Current return
     * @param foundGotos Found gotos
     * @param partCodes Part codes
     * @param partCodePos Part code position
     * @param visited Visited
     * @param code Code
     * @param localData Local data
     * @param allParts All parts
     * @param stack Stack
     * @param parent Parent part
     * @param part Part
     * @param stopPart Stop part
     * @param stopPartKind Stop part kind
     * @param loops Loops
     * @param throwStates Throw states
     * @param output Output
     * @param currentLoop Current loop
     * @param staticOperation Unused
     * @param path Path
     * @return List of GraphTargetItems to replace current output and stop
     * further processing. Null to continue.
     * @throws InterruptedException On interrupt
     */
    @Override
    protected List<GraphTargetItem> check(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos,
            Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos,
            Set<GraphPart> visited, GraphSource code,
            BaseLocalData localData, Set<GraphPart> allParts,
            TranslateStack stack, GraphPart parent,
            GraphPart part, List<GraphPart> stopPart,
            List<StopPartKind> stopPartKind, List<Loop> loops,
            List<ThrowState> throwStates, List<GraphTargetItem> output,
            Loop currentLoop, int staticOperation, String path) throws InterruptedException {
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
            StrictEqAVM2Item firstSet = set;
            caseValuesMapLeft.add(set.leftSide);
            caseValuesMapRight.add(set.rightSide);

            GraphPart origPart = part;
            List<GraphPart> caseBodyParts = new ArrayList<>();
            caseBodyParts.add(part.nextParts.get(0));
            GraphTargetItem top = null;
            int cnt = 1;
            try {
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
            } catch (GraphPartChangeException gpc) {
                //ignore
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
                switchedObject = new LocalRegAVM2Item(null, null, leftReg, null, TypeItem.UNBOUNDED /*?*/);
                caseValuesMap = caseValuesMapRight;
                otherSide = caseValuesMapLeft;
            } else if (rightReg > 0) {
                switchedObject = new LocalRegAVM2Item(null, null, rightReg, null, TypeItem.UNBOUNDED /*?*/);
                otherSide = caseValuesMapRight;
            }

            if ((leftReg < 0 && rightReg < 0) || (cnt == 1)) {
                stack.push(firstSet);
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

    /**
     * Get next parts of a part. Can be overridden to provide custom behavior.
     *
     * @param localData Local data
     * @param part Part
     * @return List of GraphParts
     */
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

    /**
     * Check of Part passing output.
     *
     * @param output List of GraphTargetItems
     * @param stack Translate stack
     * @param localData Local data
     * @param prev Previous part
     * @param part Part
     * @param allParts All parts
     * @return Return same part to continue processing or return another part to
     * continue to other part. Or return null to stop.
     */
    @Override
    protected GraphPart checkPartWithOutput(List<GraphTargetItem> output, TranslateStack stack,
            BaseLocalData localData, GraphPart prev,
            GraphPart part, Set<GraphPart> allParts
    ) {
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

    /**
     * Check of part.
     *
     * @param stack Translate stack
     * @param localData Local data
     * @param prev Previous part
     * @param part Part
     * @param allParts All parts
     * @return Return same part to continue processing or return another part to
     * continue to other part. Or return null to stop.
     */
    @Override
    protected GraphPart checkPart(TranslateStack stack, BaseLocalData localData, GraphPart prev, GraphPart part, Set<GraphPart> allParts) {
        return checkPartWithOutput(null, stack, localData, prev, part, allParts);
    }

    /**
     * Checks whether part is empty.
     *
     * @param part Part
     * @return True if part is empty
     */
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

    /**
     * Check loop.
     *
     * @param output List of GraphTargetItems
     * @param loopItem Loop item
     * @param localData Local data
     * @param loops Loops
     * @param throwStates Throw states
     * @param stack Translate stack
     * @return Return loopItem to replace current loop. Return null to continue.
     */
    @Override
    protected GraphTargetItem checkLoop(List<GraphTargetItem> output, LoopItem loopItem, BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates, TranslateStack stack) {
        if (debugDoNotProcess) {
            return loopItem;
        }
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
                                        } else if (output.get(i) instanceof PushItem) {
                                            //ignored
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
                                    } else if (output.get(i) instanceof PushItem) {
                                        //allowed
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
                                    } else if (output.get(i) instanceof PushItem) {
                                        //ignored
                                    } else {
                                        break;
                                    }
                                }

                                for (int i = output.size() - 2 /*last is loop*/; i >= 0; i--) {
                                    if (output.get(i) instanceof PushItem) {
                                        PushItem pu = (PushItem) output.remove(i);
                                        stack.push(pu.value);
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
                        } else {
                            GraphTargetItem first = w.commands.get(0);
                            Reference<Integer> kindRef = new Reference<>(0);
                            first.visitRecursively(new AbstractGraphTargetVisitor() {
                                private boolean handled = false;

                                @Override
                                public boolean visit(GraphTargetItem item) {
                                    if (handled) {
                                        return false;
                                    }
                                    if ((item instanceof NextNameAVM2Item) || (item instanceof NextValueAVM2Item)) {
                                        handled = true;
                                        if (item instanceof NextValueAVM2Item) {
                                            NextValueAVM2Item nv = (NextValueAVM2Item) item;
                                            nv.localReg = hn.index;
                                            kindRef.setVal(1);
                                        }
                                        if (item instanceof NextNameAVM2Item) {
                                            NextNameAVM2Item nn = (NextNameAVM2Item) item;
                                            nn.localReg = hn.index;
                                            kindRef.setVal(2);
                                        }
                                        return false;
                                    }
                                    return true;
                                }
                            });

                            if (kindRef.getVal() == 1) {
                                return new ForEachInAVM2Item(w.getSrc(), w.getLineStartItem(), w.loop, new InAVM2Item(hn.getInstruction(), hn.getLineStartIns(), hn.index, hn.obj), w.commands);
                            }
                            if (kindRef.getVal() == 2) {
                                return new ForInAVM2Item(w.getSrc(), w.getLineStartItem(), w.loop, new InAVM2Item(hn.getInstruction(), hn.getLineStartIns(), hn.index, hn.obj), w.commands);
                            }
                        }

                    }
                }
            }
        }
        return loopItem;
    }

    /**
     * Process various items.
     *
     * @param list List of GraphTargetItems
     * @param lastLoopId Last loop id
     */
    @Override
    protected void processOther(List<GraphTargetItem> list, long lastLoopId) {
        if (list.isEmpty()) {
            return;
        }

        int pos = list.size() - 1;

        if (list.get(pos) instanceof ContinueItem) {
            if (((ContinueItem) list.get(pos)).loopId != lastLoopId) {
                return;
            }
            pos--;
            if (list.size() < 2) {
                return;
            }
        }

        //Remove continues from all branches of try...catch block if its continue to parent loop
        if (list.get(pos) instanceof TryAVM2Item) {
            TryAVM2Item ta = (TryAVM2Item) list.get(pos);
            for (List<GraphTargetItem> cc : ta.catchCommands) {
                if (!cc.isEmpty()) {
                    if (cc.get(cc.size() - 1) instanceof ContinueItem) {
                        ContinueItem ci = (ContinueItem) cc.get(cc.size() - 1);
                        if (ci.loopId == lastLoopId) {
                            cc.remove(cc.size() - 1);
                        }
                    }
                }
            }
            if (!ta.tryCommands.isEmpty()) {
                if (ta.tryCommands.get(ta.tryCommands.size() - 1) instanceof ContinueItem) {
                    ContinueItem ci = (ContinueItem) ta.tryCommands.get(ta.tryCommands.size() - 1);
                    if (ci.loopId == lastLoopId) {
                        ta.tryCommands.remove(ta.tryCommands.size() - 1);
                    }
                }
            }
            if (!ta.finallyCommands.isEmpty()) {
                if (ta.finallyCommands.get(ta.finallyCommands.size() - 1) instanceof ContinueItem) {
                    ContinueItem ci = (ContinueItem) ta.finallyCommands.get(ta.finallyCommands.size() - 1);
                    if (ci.loopId == lastLoopId) {
                        ta.finallyCommands.remove(ta.finallyCommands.size() - 1);
                    }
                }
            }
        }
    }

    /**
     * Final process after.
     *
     * @param list List of GraphTargetItems
     * @param level Level
     * @param localData Local data
     * @param path Path
     */
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

    /**
     * Check if item is integer or pop integer.
     *
     * @param item GraphTargetItem
     * @return True if item is integer or pop integer
     */
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
    protected void finalProcess(GraphTargetItem parent, List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) throws InterruptedException {
        if (debugDoNotProcess) {
            return;
        }
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
                if (list.get(i) instanceof SetPropertyAVM2Item) {
                    SetPropertyAVM2Item sp = (SetPropertyAVM2Item) list.get(i);
                    if (sp.object instanceof FindPropertyAVM2Item) {
                        if (sp.propertyName instanceof FullMultinameAVM2Item) {
                            FullMultinameAVM2Item propName = (FullMultinameAVM2Item) sp.propertyName;
                            if (sp.value instanceof LocalRegAVM2Item) {
                                LocalRegAVM2Item lr = (LocalRegAVM2Item) sp.value;
                                AVM2FinalProcessLocalData aLocalData = (AVM2FinalProcessLocalData) localData;
                                if (Objects.equals(propName.resolvedMultinameName, AVM2Item.localRegName(aLocalData.localRegNames, lr.regIndex))) {
                                    list.remove(i);
                                    i--;
                                    continue loopi;
                                }
                            }

                        }
                    }
                }
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
                    List<GraphTargetItem> nextbody;
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
                if (true) {
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
                    //there may be multiple pushes as finally clauses may be nested
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
            //there may be multiple pushes as finally clauses may be nested
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

        
        AVM2FinalProcessLocalData adata = (AVM2FinalProcessLocalData) localData;
        //if(false)
        if (!adata.bottomSetLocals.isEmpty()) {
            boolean modified = false;
            for (int i = 0; i < list.size(); i++) {
                
                if (list.get(i) instanceof LoopItem) {
                    continue;
                }
                if (list.get(i) instanceof WithEndAVM2Item) {
                    continue;
                }
                
                List<SetLocalAVM2Item> foundSetLoc = new ArrayList<>();
                List<SetLocalAVM2Item> ignoredItems = new ArrayList<>();
                
                //We need to ignore everything on the right side of && and ||,                
                list.get(i).visitRecursivelyNoBlock(new AbstractGraphTargetRecursiveVisitor() {
                    @Override
                    public void visit(GraphTargetItem item, Stack<GraphTargetItem> parentStack) {
                        if ((item instanceof AndItem) || (item instanceof OrItem)) {
                            BinaryOpItem bo = (BinaryOpItem) item;
                            bo.getRightSide().visitRecursivelyNoBlock(new AbstractGraphTargetRecursiveVisitor() {
                                @Override
                                public void visit(GraphTargetItem item, Stack<GraphTargetItem> parentStack) {
                                    if (item instanceof SetLocalAVM2Item) {
                                        if (adata.bottomSetLocals.contains((SetLocalAVM2Item) item)) {
                                            ignoredItems.add((SetLocalAVM2Item) item);
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
                
                list.get(i).visitRecursivelyNoBlock(new AbstractGraphTargetRecursiveVisitor() {
                    @Override
                    public void visit(GraphTargetItem item, Stack<GraphTargetItem> parentStack) {
                        
                        if (item instanceof SetLocalAVM2Item) {
                            if (
                                    adata.bottomSetLocals.contains((SetLocalAVM2Item) item)
                                    && !ignoredItems.contains((SetLocalAVM2Item) item)
                                ) {
                                int s = parentStack.size() - 1;
                                if (parentStack.get(s) instanceof CoerceAVM2Item) {
                                    s--;
                                } else if (parentStack.get(s) instanceof ConvertAVM2Item) {
                                    s--;
                                }
                                if (s >= 0) {
                                    GraphTargetItem parent = parentStack.get(s);
                                    boolean move = true;
                                    if (parent instanceof SetTypeAVM2Item) {
                                        SetTypeAVM2Item setType = (SetTypeAVM2Item) parent;                                                                                
                                        
                                        if (setType.getValue().getNotCoerced() == item) { //chained assignment                                            
                                            //if (!((parent instanceof SetLocalAVM2Item)
                                            //        && ((SetLocalAVM2Item)parent).regIndex == ((SetLocalAVM2Item) item).regIndex)) { //no chain for the same localreg
                                            move = false;                                                                                       
                                        }                                        
                                    }
                                    if (move) { 
                                        foundSetLoc.add(0, (SetLocalAVM2Item) item);
                                    }
                                }                                
                            }
                        }
                    }                
                });
                for (SetLocalAVM2Item setLoc : foundSetLoc) {
                    list.add(i, setLoc.clone());
                    setLoc.hideValue = true;
                    i++;
                    modified = true;
                    adata.bottomSetLocals.remove(setLoc);
                }
            }
            if (modified && (parent instanceof WhileItem)) {
                WhileItem wi = (WhileItem) parent;
                if (wi.expression == list && !list.isEmpty()) {
                    GraphTargetItem lastExpr = list.remove(list.size() - 1);
                    List<GraphTargetItem> onTrue = new ArrayList<>();
                    BreakItem bi = new BreakItem(null, null, wi.loop.id);
                    onTrue.add(bi);                    
                    IfItem ifi = new IfItem(null, null, lastExpr.invert(null), onTrue, new ArrayList<>());
                    list.add(ifi);
                    wi.commands.addAll(0, list);
                    list.clear();
                    list.add(new TrueItem(null, null));
                }
            }
            
        }
        
        /*
        convert this situation:
        
        loc1.x = (loc1 = create()).x + 1;
        
        where loc1.x references newly created loc1.               
        
        to:
        
        loc1 = create();
        loc1.x = loc1.x + 1;
        
        It's TestSwapAssignment assembled test case.
        
         */
        /*for (int i = 0; i < list.size(); i++) {

            GraphTargetItem item = list.get(i);
            Map<Integer, List<SetLocalAVM2Item>> setRegisters = new HashMap<>();
            item.visitRecursivelyNoBlock(new AbstractGraphTargetRecursiveVisitor() {
                @Override
                public void visit(GraphTargetItem item, GraphTargetItem parent) {
                    if (item instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item setLoc = (SetLocalAVM2Item) item;
                        if (setLoc.causedByDup) {
                            if (!setRegisters.containsKey(setLoc.regIndex)) {
                                setRegisters.put(setLoc.regIndex, new ArrayList<>());
                            }
                            setRegisters.get(setLoc.regIndex).add(setLoc);
                        }
                    }
                }
            });

            Set<Integer> nextReferencedRegisters = new HashSet<>();

            for (int regIndex : setRegisters.keySet()) {
                Set<GraphTargetItem> visitedItems = new HashSet<>();
                item.visitNoBlock(new AbstractGraphTargetVisitor() {

                    boolean foundGetLoc = false;
                    boolean foundSetLoc = false;

                    @Override
                    public void visit(GraphTargetItem item) {
                        if (foundGetLoc || foundSetLoc) {
                            return;
                        }
                        if (item != null && !visitedItems.contains(item)) {
                            visitedItems.add(item);

                            if (item instanceof SetLocalAVM2Item) {
                                SetLocalAVM2Item setLoc = (SetLocalAVM2Item) item;
                                if (setLoc.regIndex == regIndex) {
                                    foundSetLoc = true;
                                    return;
                                }
                            }

                            if (item instanceof LocalRegAVM2Item) {
                                LocalRegAVM2Item getLoc = (LocalRegAVM2Item) item;
                                if (getLoc.regIndex == regIndex) {
                                    //This depends on visit order assuming visitorder is execution order.
                                    if (!foundSetLoc) {
                                        AVM2FinalProcessLocalData aLocalData = (AVM2FinalProcessLocalData) localData;
                                        boolean isSetLocUsage = true;
                                        if (getLoc.getSrc() != null) {
                                            int getIp = code.adr2pos(getLoc.getSrc().getAddress());
                                            isSetLocUsage = false;
                                            for (SetLocalAVM2Item setLoc : setRegisters.get(regIndex)) {
                                                if (setLoc.getSrc() != null) {
                                                    Set<Integer> usages = aLocalData.getSetLocalUsages(code.adr2pos(setLoc.getSrc().getAddress()));
                                                    if (usages.contains(getIp)) {
                                                        isSetLocUsage = true;
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        if (isSetLocUsage) {
                                            nextReferencedRegisters.add(regIndex);
                                            foundGetLoc = true;
                                            return;
                                        }
                                    }
                                }
                            }

                            item.visitNoBlock(this);
                        }
                    }
                }
                );
            }

            Set<GraphTargetItem> visitedItems = new HashSet<>();
            Reference<Integer> newI = new Reference<>(i);
            item.visitNoBlock(new AbstractGraphTargetVisitor() {

                boolean found = false;

                @Override
                public void visit(GraphTargetItem item) {
                    if (found) {
                        return;
                    }
                    if (item != null && !visitedItems.contains(item)) {
                        visitedItems.add(item);

                        if (item instanceof SetLocalAVM2Item) {
                            SetLocalAVM2Item setLoc = (SetLocalAVM2Item) item;
                            if (nextReferencedRegisters.contains(setLoc.regIndex)) {

                                SetLocalAVM2Item setlocClone = (SetLocalAVM2Item) setLoc.clone();
                                //TODO: handle the replacing better. It should convert SetLocal to LocalReg
                                setLoc.hideValue = true;
                                list.add(newI.getVal(), setlocClone);
                                newI.setVal(newI.getVal() + 1);
                                return;
                            }
                        }

                        item.visitNoBlock(this);
                    }
                }
            });
            i = newI.getVal();
        }
        */
        //Handle for loops at the end:
        super.finalProcess(parent, list, level, localData, path);
    }

    @Override
    protected FinalProcessLocalData getFinalData(BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates) {
        FinalProcessLocalData finalProcess = new AVM2FinalProcessLocalData(loops, ((AVM2LocalData) localData).localRegNames, ((AVM2LocalData) localData).setLocalPosToGetLocalPos, ((AVM2LocalData) localData).bottomSetLocals);
        finalProcess.registerUsage = ((AVM2LocalData) localData).setLocalPosToGetLocalPos;        
        return finalProcess;
    }

    @Override
    public AVM2LocalData prepareBranchLocalData(BaseLocalData localData) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        AVM2LocalData ret = new AVM2LocalData(aLocalData);
        ScopeStack copyScopeStack = new ScopeStack();
        copyScopeStack.addAll(ret.localScopeStack);
        ret.localScopeStack = copyScopeStack;
        return ret;
    }

    /**
     * Checks switch statement.
     *
     * @param localData Local data
     * @param switchItem Switch item
     * @param otherSides Other sides
     * @param output Output
     */
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
        int setLocalIp = InstructionDefinition.getItemIp(avm2LocalData, setLocal);
        ;
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

    /**
     * Checks if part is a switch.
     *
     * @param part Part
     * @return True if part is a switch
     */
    @Override
    protected boolean partIsSwitch(GraphPart part) {
        if (part.end < 0) {
            return false;
        }
        return avm2code.code.get(part.end).definition instanceof LookupSwitchIns;
    }

    /**
     * Gets throw states.
     *
     * @param localData Local data
     * @param allParts All parts
     * @return List of ThrowStates
     */
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
                walkCatchParts(avm2LocalData.codeStats, part, ip, catchParts, scopePos, allParts, body.exceptions[e].isFinally());
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

    /**
     * Walk catch parts register.
     *
     * @param registerId Register id
     * @param part Part
     * @param startIp Start ip
     * @param catchParts Catch parts
     * @param path Path
     * @param visited Visited
     */
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

    /**
     * Walk catch parts.
     *
     * @param stats Code stats
     * @param part Part
     * @param startIp Start ip
     * @param catchParts Catch parts
     * @param scopePos Scope position
     * @param allParts All parts
     * @param isFinally True if the catch is finally
     */
    private void walkCatchParts(CodeStats stats, GraphPart part, int startIp, Set<GraphPart> catchParts, int scopePos, Set<GraphPart> allParts, boolean isFinally) {
        if (catchParts.contains(part)) {
            return;
        }
        for (int ip = startIp; ip <= part.end; ip++) {
            if (stats.instructionStats[ip].scopepos_after < scopePos) {

                //popscope can be followed by jump (break/continue),
                //in such case, treat as single block
                if (ip >= 0 && ip < code.size()) {
                    boolean onlyKillJump = true;
                    for (int j = ip + 1; j <= part.end; j++) {
                        AVM2Instruction ins = (AVM2Instruction) code.get(j);
                        if (ins.definition instanceof KillIns) {
                            continue;
                        }
                        if (ins.definition instanceof JumpIns) {
                            continue;
                        }
                        if (ins.definition instanceof DebugLineIns) {
                            continue;
                        }
                        onlyKillJump = false;
                        break;
                    }
                    if (onlyKillJump) {
                        catchParts.add(part);
                        return;
                    }
                }
                if (ip < part.end && !isFinally) {
                    //split part into half
                    GraphPart secondPart = new GraphPart(ip + 1, part.end);
                    part.end = ip;
                    for (GraphPart n : part.nextParts) {
                        n.refs.remove(part);
                        n.refs.add(secondPart);
                    }
                    secondPart.nextParts.addAll(part.nextParts);
                    part.nextParts.clear();
                    part.nextParts.add(secondPart);
                    secondPart.refs.add(part);

                    secondPart.discoveredTime = part.discoveredTime;
                    secondPart.closedTime = part.closedTime;
                    secondPart.finishedTime = part.finishedTime;
                    secondPart.level = part.level;
                    secondPart.numBlocks = part.numBlocks;
                    secondPart.order = part.order;
                    secondPart.path = part.path;
                    allParts.add(secondPart);
                }
                catchParts.add(part);
                return;
            }
        }
        catchParts.add(part);
        for (GraphPart n : part.nextParts) {
            walkCatchParts(stats, n, n.start, catchParts, scopePos, allParts, isFinally);
        }
    }

    /**
     * Moves all stack items to commands. (If it's not a branch stack resistant
     * or other special case)
     *
     * @param commands Commands
     * @param stack Stack
     */
    public void makeAllCommands(List<GraphTargetItem> commands, TranslateStack stack) {
        for (int i = 0; i < stack.size(); i++) {
            //These are often obfuscated, so ignore them                
            if (stack.get(i) instanceof NewFunctionAVM2Item) {
                stack.remove(i);
                i--;
            }
        }
        super.makeAllCommands(commands, stack);
    }

    /**
     * Prepares second pass data. Can return null when no second pass will
     * happen.
     *
     * @param list List of GraphTargetItems
     * @return Second pass data or null
     */
    @Override
    protected SecondPassData prepareSecondPass(List<GraphTargetItem> list) {
        return new SecondPassData();
    }
    
    @Override
    protected GraphTargetItem getIfExpression(BaseLocalData localData, TranslateStack stack, List<GraphTargetItem> output) {
        GraphTargetItem result = stack.pop();
        
        /*
        Fixes this case:
        
        var i:int;
        if ((i = 5) > 2 && i < 10) {
            ...
        }
        
        when instead
        setlocal x
        getlocal x
        
        there is:
        dup
        setlocal x
        
        */
        
        if (stack.getMark("firstSetLocal") != null) {
            SetLocalAVM2Item setLocal = (SetLocalAVM2Item) stack.getMark("firstSetLocal");
            if (setLocal.directlyCausedByDup) {
                output.add(setLocal.clone());
                setLocal.hideValue = true;
            }
        }
        
        return result;
    }
}

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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2.graph;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.FinalProcessLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DecLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.IncLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FilteredCheckAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.HasNextAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NextNameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NextValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ScriptAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThrowAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithAVM2Item;
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
import com.jpexs.decompiler.graph.GraphPartEdge;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DefaultItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LoopItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
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
    protected void beforePrintGraph(BaseLocalData localData, String path, Set<GraphPart> allParts, List<Loop> loops) {
        Map<Integer, Integer> ignoredSwitches = getIgnoredSwitches(allParts);
        Map<Integer, Set<Integer>> setLocalPosToGetLocalPos = calculateLocalRegsUsage(new HashSet<Integer>(ignoredSwitches.values()), path, allParts);
        AVM2LocalData avm2LocalData = ((AVM2LocalData) localData);
        avm2LocalData.setLocalPosToGetLocalPos = setLocalPosToGetLocalPos;

    }

    private Map<Integer, Integer> getIgnoredSwitches(Set<GraphPart> allParts) {
        Map<Integer, Integer> ignoredSwitches = new HashMap<>();
        int finStart;
        for (int e = 0; e < body.exceptions.length; e++) {
            if (body.exceptions[e].isFinally()) {
                {
                    {
                        AVM2Instruction jmpIns = avm2code.code.get(code.adr2pos(avm2code.fixAddrAfterDebugLine(body.exceptions[e].end)));

                        if (jmpIns.definition instanceof JumpIns) {
                            finStart = code.adr2pos(avm2code.fixAddrAfterDebugLine(body.exceptions[e].end) + jmpIns.getBytesLength() + jmpIns.operands[0]);

                            GraphPart fpart = null;
                            for (GraphPart p : allParts) {
                                if (p.start == finStart) {
                                    fpart = p;
                                    break;
                                }
                            }
                            int swPos = -1;
                            for (int f = finStart; f < avm2code.code.size(); f++) {
                                if (avm2code.code.get(f).definition instanceof LookupSwitchIns) {
                                    AVM2Instruction swins = avm2code.code.get(f);
                                    if (swins.operands.length >= 3) {
                                        if (swins.operands[0] == swins.getBytesLength()) {
                                            if (code.adr2pos(code.pos2adr(f) + swins.operands[2]) < finStart) {
                                                swPos = f;

                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            ignoredSwitches.put(e, swPos);
                            break;
                        }
                    }
                }
            }
        }
        return ignoredSwitches;
    }

    public Map<Integer, Set<Integer>> calculateLocalRegsUsage(Set<Integer> ignoredSwitches, String path, Set<GraphPart> allParts) {
        logger.fine("--- " + path + " ---");
        Map<Integer, Set<Integer>> setLocalPosToGetLocalPos = new TreeMap<>();
        Map<GraphPart, Map<Integer, List<Integer>>> partUnresolvedRegisterToGetLocalPos = new HashMap<>();
        Map<GraphPart, Map<Integer, Integer>> partRegisterToLastSetLocalPos = new HashMap<>();

        for (GraphPart p : allParts) {
            if (p.start < 0) {
                continue;
            }
            Map<Integer, Integer> registerToLastSetLocalPos = new HashMap<>();
            for (int ip = p.start; ip <= p.end; ip++) {
                AVM2Instruction ins = avm2code.code.get(ip);
                if (ins.definition instanceof SetLocalTypeIns) {
                    int regId = ((SetLocalTypeIns) ins.definition).getRegisterId(ins);
                    registerToLastSetLocalPos.put(regId, ip);
                    setLocalPosToGetLocalPos.put(ip, new TreeSet<>());
                }
                List<Integer> usedRegs = new ArrayList<>();
                if (ins.definition instanceof GetLocalTypeIns) {
                    int regId = ((GetLocalTypeIns) ins.definition).getRegisterId(ins);
                    usedRegs.add(regId);
                }
                if ((ins.definition instanceof IncLocalIns)
                        || (ins.definition instanceof IncLocalIIns)
                        || (ins.definition instanceof IncLocalPIns)
                        || (ins.definition instanceof DecLocalIns)
                        || (ins.definition instanceof DecLocalIIns)
                        || (ins.definition instanceof DecLocalPIns)) {
                    usedRegs.add(ins.operands[0]);
                }
                if ((ins.definition instanceof IncLocalPIns)
                        || (ins.definition instanceof DecLocalPIns)) {
                    usedRegs.add(ins.operands[1]);
                }
                if (ins.definition instanceof HasNext2Ins) {
                    usedRegs.add(ins.operands[0]);
                    usedRegs.add(ins.operands[1]);
                }
                for (int regId : usedRegs) {
                    if (registerToLastSetLocalPos.containsKey(regId)) {
                        int setLocalPos = registerToLastSetLocalPos.get(regId);
                        setLocalPosToGetLocalPos.get(setLocalPos).add(ip);
                    } else {
                        if (!partUnresolvedRegisterToGetLocalPos.containsKey(p)) {
                            partUnresolvedRegisterToGetLocalPos.put(p, new HashMap<>());
                        }
                        if (!partUnresolvedRegisterToGetLocalPos.get(p).containsKey(regId)) {
                            partUnresolvedRegisterToGetLocalPos.get(p).put(regId, new ArrayList<>());
                        }
                        partUnresolvedRegisterToGetLocalPos.get(p).get(regId).add(ip);
                    }
                }
            }
            partRegisterToLastSetLocalPos.put(p, registerToLastSetLocalPos);
        }

        Set<GraphPart> pSet = new HashSet<>(partUnresolvedRegisterToGetLocalPos.keySet());
        for (GraphPart p : pSet) {
            Map<Integer, List<Integer>> unresolvedRegisterToGetLocalPos = partUnresolvedRegisterToGetLocalPos.get(p);
            Set<GraphPart> visited = new HashSet<>();
            visited.add(p);
            for (GraphPart q : p.refs) {
                calculateLocalRegsUsageWalk(ignoredSwitches, q, unresolvedRegisterToGetLocalPos, visited, partRegisterToLastSetLocalPos, setLocalPosToGetLocalPos, p);
            }
        }

        for (int setLocalPos : setLocalPosToGetLocalPos.keySet()) {
            AVM2Instruction ins = avm2code.code.get(setLocalPos);
            int regId = ((SetLocalTypeIns) ins.definition).getRegisterId(ins);
            logger.fine("set local reg " + regId + " at pos " + (setLocalPos + 1));

            for (int getLocalPos : setLocalPosToGetLocalPos.get(setLocalPos)) {
                logger.fine("- usage at pos " + (getLocalPos + 1));
            }
        }
        return setLocalPosToGetLocalPos;
    }

    public void calculateLocalRegsUsageWalk(Set<Integer> ignoredSwitches, GraphPart q,
            Map<Integer, List<Integer>> unresolvedRegisterToGetLocalPos,
            Set<GraphPart> visited,
            Map<GraphPart, Map<Integer, Integer>> partRegisterToLastSetLocalPos,
            Map<Integer, Set<Integer>> setLocalPosToGetLocalPos, GraphPart next) {
        if (visited.contains(q)) {
            return;
        }
        if (ignoredSwitches.contains(q.end)) {
            if (!next.equals(q.nextParts.get(0))) { //first is after finally
                return;
            }
        }
        Set<Integer> regIds = new HashSet<>(unresolvedRegisterToGetLocalPos.keySet());
        for (int regId : regIds) {
            if (partRegisterToLastSetLocalPos.containsKey(q)) {
                if (partRegisterToLastSetLocalPos.get(q).containsKey(regId)) {
                    int lastSetLocalPos = partRegisterToLastSetLocalPos.get(q).get(regId);
                    setLocalPosToGetLocalPos.get(lastSetLocalPos).addAll(unresolvedRegisterToGetLocalPos.get(regId));
                    unresolvedRegisterToGetLocalPos = new HashMap<>(unresolvedRegisterToGetLocalPos);
                    unresolvedRegisterToGetLocalPos.remove(regId);
                }
            }
        }
        if (unresolvedRegisterToGetLocalPos.isEmpty()) {
            return;
        }

        visited.add(q);

        for (GraphPart r : q.refs) {
            calculateLocalRegsUsageWalk(ignoredSwitches, r, unresolvedRegisterToGetLocalPos, visited, partRegisterToLastSetLocalPos, setLocalPosToGetLocalPos, q);
        }
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
            /*int startAddr = avm2code.adr2pos(ex.start);
             int endAddr = avm2code.adr2pos(ex.end);
             int targetIp = avm2code.adr2pos(ex.target);*/
            GraphPart target = null;
            for (GraphPart p : allBlocks) {
                if (avm2code.pos2adr(p.start) == ex.target) {
                    target = p;
                    break;
                }
            }
            for (GraphPart p : allBlocks) {
                if (avm2code.pos2adr(p.start) >= ex.start && avm2code.pos2adr(p.end) <= ex.end && target != null) {
                    //Logger.getLogger(Graph.class.getName()).fine("ADDING throwpart " + target + " to " + p);
                    p.throwParts.add(target);
                    target.refs.add(p);
                }
            }
        }
    }

    @Override
    protected List<GraphTargetItem> check(List<GotoItem> foundGotos, List<GraphPartEdge> gotoTargets, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) throws InterruptedException {
        List<GraphTargetItem> ret = null;

        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        if (aLocalData.parsedExceptions == null) {
            aLocalData.parsedExceptions = new ArrayList<>();
        }
        List<ABCException> parsedExceptions = aLocalData.parsedExceptions;
        if (aLocalData.finallyJumps == null) {
            aLocalData.finallyJumps = new HashMap<>();
        }
        Map<Integer, List<Integer>> finallyJumps = aLocalData.finallyJumps;
        if (aLocalData.ignoredSwitches == null) {
            aLocalData.ignoredSwitches = new HashMap<>();
        }
        Map<Integer, Integer> ignoredSwitches = aLocalData.ignoredSwitches;
        if (aLocalData.ignoredSwitches2 == null) {
            aLocalData.ignoredSwitches2 = new ArrayList<>();
        }
        List<Integer> ignoredSwitches2 = aLocalData.ignoredSwitches2;
        int ip = part.start;
        long addr = avm2code.fixAddrAfterDebugLine(avm2code.pos2adr(part.start));
        long maxend = -1;
        List<Integer> catchedFinallys = new ArrayList<>();
        List<ABCException> catchedExceptions = new ArrayList<>();
        for (int e = 0; e < body.exceptions.length; e++) {
            if (addr == avm2code.fixAddrAfterDebugLine(body.exceptions[e].start)) {
                //Add finally only when the list is empty
                if (!body.exceptions[e].isFinally() || catchedExceptions.isEmpty()) {
                    if (!parsedExceptions.contains(body.exceptions[e])) {
                        if (((body.exceptions[e].end) > maxend)) {
                            catchedExceptions.clear();
                            catchedFinallys.clear();
                            maxend = avm2code.fixAddrAfterDebugLine(body.exceptions[e].end);
                            catchedExceptions.add(body.exceptions[e]);
                        } else if (avm2code.fixAddrAfterDebugLine(body.exceptions[e].end) == maxend) {
                            catchedExceptions.add(body.exceptions[e]);
                        }
                        catchedFinallys.add(e);

                    }
                } else if (body.exceptions[e].isFinally()) {
                    parsedExceptions.add(body.exceptions[e]);
                }
            }
        }
        if (catchedExceptions.size() > 0) {
            parsedExceptions.addAll(catchedExceptions);
            int endpos = code.adr2pos(avm2code.fixAddrAfterDebugLine(catchedExceptions.get(0).end));
            int endposStartBlock = code.adr2pos(catchedExceptions.get(0).end);

            String finCatchName = "";
            List<List<GraphTargetItem>> catchedCommands = new ArrayList<>();
            if (avm2code.code.get(endpos).definition instanceof JumpIns) {
                long afterCatchAddr = avm2code.pos2adr(endpos + 1) + avm2code.code.get(endpos).operands[0];
                int afterCatchPos = avm2code.adr2pos(afterCatchAddr);
                final AVM2Graph t = this;
                Collections.sort(catchedExceptions, new Comparator<ABCException>() {
                    @Override
                    public int compare(ABCException o1, ABCException o2) {
                        return (int) (t.avm2code.fixAddrAfterDebugLine(o1.target) - t.avm2code.fixAddrAfterDebugLine(o2.target));
                    }
                });

                List<GraphTargetItem> finallyCommands = new ArrayList<>();
                boolean hasFinally = false;
                int returnPos = afterCatchPos;
                int finStart;
                for (int e = 0; e < body.exceptions.length; e++) {
                    if (body.exceptions[e].isFinally()) {
                        if (addr == avm2code.fixAddrAfterDebugLine(body.exceptions[e].start)) {
                            if (afterCatchPos + 1 == code.adr2pos(avm2code.fixAddrAfterDebugLine(body.exceptions[e].end))) {
                                catchedFinallys.add(e);
                                AVM2Instruction jmpIns = avm2code.code.get(code.adr2pos(avm2code.fixAddrAfterDebugLine(body.exceptions[e].end)));

                                if (jmpIns.definition instanceof JumpIns) {
                                    finStart = code.adr2pos(avm2code.fixAddrAfterDebugLine(body.exceptions[e].end) + jmpIns.getBytesLength() + jmpIns.operands[0]);

                                    GraphPart fpart = null;
                                    for (GraphPart p : allParts) {
                                        if (p.start == finStart) {
                                            fpart = p;
                                            break;
                                        }
                                    }
                                    TranslateStack st = (TranslateStack) stack.clone();
                                    st.clear();
                                    int swPos = -1;
                                    for (int f = finStart; f < avm2code.code.size(); f++) {
                                        if (avm2code.code.get(f).definition instanceof LookupSwitchIns) {
                                            AVM2Instruction swins = avm2code.code.get(f);
                                            if (swins.operands.length >= 3) {
                                                if (swins.operands[0] == swins.getBytesLength()) {
                                                    if (code.adr2pos(code.pos2adr(f) + swins.operands[2]) < finStart) {
                                                        //st.push(new ExceptionAVM2Item(body.exceptions[e]));
                                                        GraphPart fepart = null;
                                                        for (GraphPart p : allParts) {
                                                            if (p.start == f + 1) {
                                                                fepart = p;
                                                                break;
                                                            }
                                                        }
                                                        //this.code.code.get(f).ignored = true;
                                                        //ignoredSwitches.add(f);
                                                        swPos = f;

                                                        List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                                                        stopPart2.add(fepart);
                                                        //finallyCommands = printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, parent, fpart, stopPart2, loops, staticOperation, path);
                                                        returnPos = f + 1;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    //ignoredSwitches.add(-1);
                                    //int igs_size=ignoredSwitches.size();
                                    Map<Integer, List<Integer>> oldFinallyJumps = new HashMap<>(finallyJumps);
                                    finallyJumps.clear();
                                    ignoredSwitches.put(e, swPos);
                                    st.push(new PopItem(null, aLocalData.lineStartInstruction));
                                    finallyCommands = printGraph(foundGotos, gotoTargets, partCodes, partCodePos, localData, st, allParts, parent, fpart, null, loops, staticOperation, path);
                                    //ignoredSwitches.remove(igs_size-1);
                                    finallyJumps.putAll(oldFinallyJumps);
                                    if (!finallyJumps.containsKey(e)) {
                                        finallyJumps.put(e, new ArrayList<>());
                                    }
                                    finallyJumps.get(e).add(finStart);
                                    hasFinally = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                GraphPart retPart = null;
                for (GraphPart p : allParts) {
                    if (p.start == returnPos) {
                        retPart = p;
                        break;
                    }
                }
                List<GraphPart> catchParts = new ArrayList<>();
                for (int e = 0; e < catchedExceptions.size(); e++) {
                    int eendpos;
                    if (e < catchedExceptions.size() - 1) {
                        eendpos = code.adr2pos(avm2code.fixAddrAfterDebugLine(catchedExceptions.get(e + 1).target)) - 2;
                    } else {
                        eendpos = afterCatchPos - 1;
                    }

                    GraphPart npart = null;
                    int findpos = code.adr2pos(catchedExceptions.get(e).target);
                    for (GraphPart p : allParts) {
                        if (p.start == findpos) {
                            npart = p;
                            catchParts.add(p);
                            break;
                        }
                    }

                    GraphPart nepart = null;
                    for (GraphPart p : allParts) {
                        if (p.start == eendpos + 1) {
                            nepart = p;
                            break;
                        }
                    }
                    TranslateStack st2 = (TranslateStack) stack.clone();
                    st2.clear();
                    st2.add(new ExceptionAVM2Item(catchedExceptions.get(e)));
                    AVM2LocalData localData2 = new AVM2LocalData(aLocalData);
                    localData2.scopeStack = new ScopeStack(localData2.scriptIndex);
                    List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                    stopPart2.add(nepart);
                    if (retPart != null) {
                        stopPart2.add(retPart);
                    }

                    List<GraphTargetItem> ncatchedCommands = printGraph(foundGotos, gotoTargets, partCodes, partCodePos, localData2, st2, allParts, parent, npart, stopPart2, loops, staticOperation, path);
                    //hack for findGotos - FIXME
                    if (hasFinally && !ncatchedCommands.isEmpty()) {
                        for (int k = 0; k < ncatchedCommands.size(); k++) {
                            if (ncatchedCommands.get(k) instanceof GotoItem) {
                                GotoItem gi = (GotoItem) ncatchedCommands.get(k);
                                for (GotoItem g : foundGotos) {
                                    if (gi.labelName.equals(g.labelName) && g.targetCommands != null) {
                                        if (!g.targetCommands.isEmpty()) {
                                            if (g.targetCommands.get(0) instanceof PushItem) {
                                                if (g.targetCommands.get(0).value instanceof IntegerValueAVM2Item) {
                                                    if (((IntegerValueAVM2Item) g.targetCommands.get(0).value).value == -1) {
                                                        ncatchedCommands.remove(gi);
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (catchedExceptions.get(e).isFinally() && (catchedExceptions.size() > 1 || hasFinally)) {
                        catchedExceptions.remove(e);
                        e--;
                    } else {
                        catchedCommands.add(ncatchedCommands);
                        if (retPart != null && avm2code.code.get(retPart.start).isExit() && !(!ncatchedCommands.isEmpty() && (ncatchedCommands.get(ncatchedCommands.size() - 1) instanceof ExitItem))) {
                            avm2code.code.get(retPart.start).translate(localData, st2, ncatchedCommands, staticOperation, path);
                        }
                        if (catchedExceptions.get(e).isFinally()) {
                            //endposStartBlock = -1;
                            if (!ncatchedCommands.isEmpty() && (ncatchedCommands.get(0) instanceof SetLocalAVM2Item)) {
                                SetLocalAVM2Item sl = (SetLocalAVM2Item) ncatchedCommands.get(0);
                                if (sl.value.getNotCoerced() instanceof ExceptionAVM2Item) {
                                    finCatchName = AVM2Item.localRegName(new HashMap<>(), sl.regIndex);
                                }
                            }
                        } else {
                            //No kill ins
                            if (!ncatchedCommands.isEmpty() && (ncatchedCommands.get(0) instanceof SetLocalAVM2Item)) {
                                SetLocalAVM2Item sl = (SetLocalAVM2Item) ncatchedCommands.get(0);
                                if (sl.value.getThroughDuplicate().getNotCoerced() instanceof ExceptionAVM2Item) {
                                    ncatchedCommands.remove(0);
                                }
                            }
                        }
                    }
                }

                GraphPart nepart = null;

                for (GraphPart p : allParts) {
                    if (p.start == endposStartBlock) {
                        nepart = p;
                        break;
                    }
                }
                List<GraphPart> stopPart2 = new ArrayList<>();//stopPart);
                if (nepart != null) {
                    stopPart2.add(nepart);
                }
                stopPart2.addAll(catchParts);

                if (retPart != null) {
                    stopPart2.add(retPart);
                }
                TranslateStack st = (TranslateStack) stack.clone();
                st.clear();
                List<GraphTargetItem> tryCommands = printGraph(foundGotos, gotoTargets, partCodes, partCodePos, localData, st, allParts, parent, part, stopPart2, loops, staticOperation, path);
                if (retPart != null && avm2code.code.get(retPart.start).isExit() && !(!tryCommands.isEmpty() && (tryCommands.get(tryCommands.size() - 1) instanceof ExitItem))) {
                    avm2code.code.get(retPart.start).translate(localData, st, tryCommands, staticOperation, path);
                }
                output.clear();
                stack.clear();
                output.add(new TryAVM2Item(tryCommands, catchedExceptions, catchedCommands, finallyCommands, finCatchName));
                for (int fin_e : catchedFinallys) {
                    if (finallyJumps.containsKey(fin_e)) {
                        finallyJumps.get(fin_e).clear();
                    }
                    //.remove((Integer) finStart);
                }
                ip = returnPos;
            }

        }

        if (ip != part.start) {
            part = null;
            for (GraphPart p : allParts) {
                List<GraphPart> ps = p.getSubParts();
                for (GraphPart p2 : ps) {
                    if (p2.start == ip) {
                        part = p2;
                        break;
                    }
                }
            }
            ret = new ArrayList<>();
            ret.addAll(output);
            GraphTargetItem lop = checkLoop(new ArrayList<GraphTargetItem>(), part, stopPart, loops);
            if (lop == null) {
                TranslateStack st = (TranslateStack) stack.clone();
                st.clear();

                ret.addAll(printGraph(foundGotos, gotoTargets, partCodes, partCodePos, localData, st, allParts, null, part, stopPart, loops, staticOperation, path));
            } else {
                ret.add(lop);
            }
            return ret;
        }

        if ((avm2code.code.get(part.end).definition instanceof LookupSwitchIns) && (ignoredSwitches.containsValue(part.end) || ignoredSwitches2.contains(part.end))) {
            ret = new ArrayList<>();
            ret.addAll(output);
            return ret;
        }

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

            if (leftReg > 0) {
                switchedObject = new LocalRegAVM2Item(null, null, leftReg, null);
                caseValuesMap = caseValuesMapRight;
            } else if (rightReg > 0) {
                switchedObject = new LocalRegAVM2Item(null, null, rightReg, null);
            }

            if ((leftReg < 0 && rightReg < 0) || (cnt == 1)) {
                stack.push(set);
            } else {
                part = part.nextParts.get(1);
                GraphPart defaultPart = part;
                if (code.size() > defaultPart.start && ((AVM2Instruction) code.get(defaultPart.start)).definition instanceof JumpIns) {
                    defaultPart = defaultPart.nextParts.get(0);
                }

                boolean hasDefault = false;
                /*
                case 4:
                case 5:
                default: 
                    trace("5 & def");
                    ...
                case 6:
                
                 */
                //must go backwards to hit case 5, not case 4
                for (int i = caseBodyParts.size() - 1; i >= 0; i--) {
                    if (caseBodyParts.get(i) == defaultPart) {
                        DefaultItem di = new DefaultItem();
                        caseValuesMap.add(i + 1, di);
                        caseBodyParts.add(i + 1, defaultPart);
                        hasDefault = true;
                        break;
                    }
                }

                if (!hasDefault) {
                    /*
                    case 1:
                        trace("1");
                    case 2:
                        trace("2"); //no break
                    default:
                        trace("def");
                        ...
                    case 3:  
                     */
                    //must go backwards to hit case 2, not case 1
                    for (int i = caseBodyParts.size() - 1; i >= 0; i--) {
                        if (caseBodyParts.get(i).leadsTo(localData, this, code, defaultPart, loops, new ArrayList<>())) {
                            DefaultItem di = new DefaultItem();
                            caseValuesMap.add(i + 1, di);
                            caseBodyParts.add(i + 1, defaultPart);
                            hasDefault = true;
                            break;
                        }
                    }
                }

                if (!hasDefault) {
                    /*
                    case 1:
                        trace("1");
                        break;
                    default:
                        trace("def"); //no break
                    case 2:
                        trace("2");                    
                     */
                    for (int i = 0; i < caseBodyParts.size(); i++) {
                        if (defaultPart.leadsTo(localData, this, code, caseBodyParts.get(i), loops, new ArrayList<>())) {
                            DefaultItem di = new DefaultItem();
                            caseValuesMap.add(i, di);
                            caseBodyParts.add(i, defaultPart);
                            hasDefault = true;
                            break;
                        }
                    }
                }

                if (!hasDefault) {
                    /*
                        case 1:
                        ...
                        case 2:
                        ...
                        default:
                            trace("def");                        
                     */
                    caseValuesMap.add(new DefaultItem());
                    caseBodyParts.add(defaultPart);
                }

                GraphPart breakPart = getMostCommonPart(localData, caseBodyParts, loops, new ArrayList<>());
                //removeEdgeToFromList(gotoTargets, breakPart);

                List<List<GraphTargetItem>> caseCommands = new ArrayList<>();
                GraphPart next = breakPart;

                GraphTargetItem ti = checkLoop(new ArrayList<GraphTargetItem>() /*??*/, next, stopPart, loops);

                //create switch as new loop break command detection to work
                currentLoop = new Loop(loops.size(), null, next);
                currentLoop.phase = 1;
                loops.add(currentLoop);
                List<Integer> valuesMapping = new ArrayList<>();
                List<GraphPart> caseBodies = new ArrayList<>();
                for (int i = 0; i < caseValuesMap.size(); i++) {
                    GraphPart cur = caseBodyParts.get(i);
                    if (!caseBodies.contains(cur)) {
                        caseBodies.add(cur);
                    }
                    valuesMapping.add(caseBodies.indexOf(cur));
                }

                for (int i = 0; i < caseBodies.size(); i++) {
                    List<GraphTargetItem> currentCaseCommands = new ArrayList<>();
                    GraphPart nextCase = next;
                    if (next != null) {
                        if (i < caseBodies.size() - 1) {
                            if (!caseBodies.get(i).leadsTo(localData, this, code, caseBodies.get(i + 1), loops, new ArrayList<>())) {
                                currentCaseCommands.add(new BreakItem(null, localData.lineStartInstruction, currentLoop.id));
                            } else {
                                nextCase = caseBodies.get(i + 1);
                            }
                        }
                    }
                    List<GraphPart> stopPart2x = new ArrayList<>(stopPart);
                    for (GraphPart b : caseBodies) {
                        if (b != caseBodies.get(i)) {
                            stopPart2x.add(b);
                        }
                    }
                    if (breakPart != null) {
                        stopPart2x.add(breakPart);
                    }
                    currentCaseCommands.addAll(0, printGraph(foundGotos, gotoTargets, partCodes, partCodePos, localData, stack, allParts, null, caseBodies.get(i), stopPart2x, loops, staticOperation, path));
                    if (currentCaseCommands.size() >= 2) {
                        if (currentCaseCommands.get(currentCaseCommands.size() - 1) instanceof BreakItem) {
                            if ((currentCaseCommands.get(currentCaseCommands.size() - 2) instanceof ContinueItem) || (currentCaseCommands.get(currentCaseCommands.size() - 2) instanceof BreakItem)) {
                                currentCaseCommands.remove(currentCaseCommands.size() - 1);
                            }
                        }
                    }
                    caseCommands.add(currentCaseCommands);
                }

                //If the lastone is default empty and alone, remove it
                if (!caseCommands.isEmpty()) {
                    List<GraphTargetItem> lastc = caseCommands.get(caseCommands.size() - 1);
                    if (!lastc.isEmpty() && (lastc.get(lastc.size() - 1) instanceof BreakItem)) {
                        BreakItem bi = (BreakItem) lastc.get(lastc.size() - 1);
                        lastc.remove(lastc.size() - 1);
                    }
                    if (lastc.isEmpty()) {
                        int cnt2 = 0;
                        if (caseValuesMap.get(caseValuesMap.size() - 1) instanceof DefaultItem) {
                            for (int i = valuesMapping.size() - 1; i >= 0; i--) {
                                if (valuesMapping.get(i) == caseCommands.size() - 1) {
                                    cnt2++;
                                }
                            }

                            caseValuesMap.remove(caseValuesMap.size() - 1);
                            valuesMapping.remove(valuesMapping.size() - 1);
                            if (cnt2 == 1) {
                                caseCommands.remove(lastc);
                            }
                        }
                    }
                }
                //remove last break from last section                
                if (!caseCommands.isEmpty()) {
                    List<GraphTargetItem> lastc = caseCommands.get(caseCommands.size() - 1);
                    if (!lastc.isEmpty() && (lastc.get(lastc.size() - 1) instanceof BreakItem)) {
                        BreakItem bi = (BreakItem) lastc.get(lastc.size() - 1);
                        lastc.remove(lastc.size() - 1);
                    }
                }

                ret = new ArrayList<>();
                ret.addAll(output);
                SwitchItem sti = new SwitchItem(null, switchStartItem, currentLoop, switchedObject, caseValuesMap, caseCommands, valuesMapping);
                ret.add(sti);
                currentLoop.phase = 2;
                if (next != null) {
                    if (ti != null) {
                        ret.add(ti);
                    } else {
                        ret.addAll(printGraph(foundGotos, gotoTargets, partCodes, partCodePos, localData, stack, allParts, null, next, stopPart, loops, staticOperation, path));
                    }
                }
            }
        }

        return ret;
    }

    @Override
    protected GraphPart checkPart(TranslateStack stack, BaseLocalData localData, GraphPart prev, GraphPart next, Set<GraphPart> allParts) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        if (aLocalData.finallyJumps == null) {
            aLocalData.finallyJumps = new HashMap<>();
        }
        Map<Integer, List<Integer>> finallyJumps = aLocalData.finallyJumps;
        if (aLocalData.ignoredSwitches == null) {
            aLocalData.ignoredSwitches = new HashMap<>();
        }
        Map<Integer, Integer> ignoredSwitches = aLocalData.ignoredSwitches;
        GraphPart ret = next;
        for (int f : finallyJumps.keySet()) {//int f = 0; f < finallyJumps.size(); f++) {
            int swip = ignoredSwitches.get(f);
            for (int fip : finallyJumps.get(f)) {
                if (next.start == fip) {
                    if (stack != null && swip != -1) {
                        AVM2Instruction swIns = avm2code.code.get(swip);
                        GraphTargetItem t = stack.pop();
                        Double dval = t.getResultAsNumber();
                        int val = (int) (double) dval;
                        if (swIns.definition instanceof LookupSwitchIns) {
                            List<Integer> branches = swIns.getBranches(code);
                            int nip = branches.get(0);
                            if (val >= 0 && val < branches.size() - 1) {
                                nip = branches.get(1 + val);
                            }
                            for (GraphPart p : allParts) {
                                if (avm2code.fixIPAfterDebugLine(p.start) == avm2code.fixIPAfterDebugLine(nip)) {
                                    return p;
                                }
                            }
                        }
                    }
                    ret = null;
                }
            }
        }
        if (ret != next) {
            return ret;
        }

        int pos = next.start;
        long addr = avm2code.fixAddrAfterDebugLine(avm2code.pos2adr(pos));
        for (int e = 0; e < body.exceptions.length; e++) {
            if (body.exceptions[e].isFinally()) {
                if (addr == avm2code.fixAddrAfterDebugLine(body.exceptions[e].start)) {
                    if (true) { //afterCatchPos + 1 == code.adr2pos(this.code.fixAddrAfterDebugLine(body.exceptions[e].end))) {
                        AVM2Instruction jmpIns = avm2code.code.get(avm2code.adr2pos(avm2code.fixAddrAfterDebugLine(body.exceptions[e].end)));
                        if (jmpIns.definition instanceof JumpIns) {
                            int finStart = avm2code.adr2pos(avm2code.fixAddrAfterDebugLine(body.exceptions[e].end) + jmpIns.getBytesLength() + jmpIns.operands[0]);
                            if (!finallyJumps.containsKey(e)) {
                                finallyJumps.put(e, new ArrayList<>());
                            }
                            finallyJumps.get(e).add(finStart);
                            if (!ignoredSwitches.containsKey(e)) {
                                ignoredSwitches.put(e, -1);
                            }
                            //ignoredSwitches.put(e, -1);
                            break;
                        }
                    }
                }
            }
        }

        if (prev != null) {
            for (int swip : ignoredSwitches.values()) {
                if (swip > -1) {
                    if (prev.end == swip) {
                        return null;
                    }
                }
            }
        }

        return next;
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
    protected GraphTargetItem checkLoop(List<GraphTargetItem> output, LoopItem loopItem, BaseLocalData localData, List<Loop> loops) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        if (loopItem instanceof WhileItem) {
            WhileItem w = (WhileItem) loopItem;

            if ((!w.expression.isEmpty()) && (w.expression.get(w.expression.size() - 1) instanceof HasNextAVM2Item)) {
                HasNextAVM2Item hn = (HasNextAVM2Item) w.expression.get(w.expression.size() - 1);
                if (hn.obj != null) {
                    if (hn.obj.getNotCoerced().getThroughRegister().getNotCoerced() instanceof FilteredCheckAVM2Item) {
                        if (w.commands.size() >= 3) {
                            int pos = 0;
                            while (w.commands.get(pos) instanceof SetLocalAVM2Item) {
                                pos++;
                            }
                            GraphTargetItem ft = w.commands.get(pos);
                            if (ft instanceof WithAVM2Item) {
                                pos++;
                                while (w.commands.get(pos) instanceof SetTypeAVM2Item) {
                                    pos++;
                                }
                                ft = w.commands.get(pos);
                                if (ft instanceof IfItem) {
                                    IfItem ift = (IfItem) ft;
                                    if (ift.onTrue.size() > 0) {
                                        ft = ift.onTrue.get(0);
                                        if (ft instanceof SetPropertyAVM2Item) {
                                            SetPropertyAVM2Item spt = (SetPropertyAVM2Item) ft;
                                            if (spt.object instanceof LocalRegAVM2Item) {
                                                int regIndex = ((LocalRegAVM2Item) spt.object).regIndex;
                                                HashMap<Integer, GraphTargetItem> localRegs = aLocalData.localRegs;
                                                localRegs.put(regIndex, new FilterAVM2Item(null, null, hn.obj.getThroughRegister(), ift.expression));

                                                Set<Integer> localRegsToKill = new HashSet<>();
                                                localRegsToKill.add(regIndex);

                                                if (hn.obj instanceof LocalRegAVM2Item) {
                                                    localRegsToKill.add(((LocalRegAVM2Item) hn.obj).regIndex);
                                                }
                                                if (spt.value instanceof LocalRegAVM2Item) {
                                                    localRegsToKill.add(((LocalRegAVM2Item) spt.value).regIndex);
                                                }
                                                if (spt.propertyName instanceof FullMultinameAVM2Item) {
                                                    if (((FullMultinameAVM2Item) spt.propertyName).name instanceof LocalRegAVM2Item) {
                                                        localRegsToKill.add(((LocalRegAVM2Item) ((FullMultinameAVM2Item) spt.propertyName).name).regIndex);
                                                    }
                                                }

                                                //TODO: maybe check its single usage
                                                for (int i = output.size() - 2 /*last is loop*/; i >= 0; i--) {
                                                    if (localRegsToKill.isEmpty()) {
                                                        break;
                                                    }
                                                    if (output.get(i) instanceof SetLocalAVM2Item) {
                                                        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(i);
                                                        if (localRegsToKill.contains(setLocal.regIndex)) {
                                                            output.remove(i);
                                                        }
                                                    } else {
                                                        break;
                                                    }
                                                }

                                                return null;
                                            }
                                        }
                                    }
                                }
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
            if (list.get(i) instanceof SetLocalAVM2Item) {
                SetLocalAVM2Item ri = (SetLocalAVM2Item) list.get(i);
                if (localData.temporaryRegisters.contains(ri.regIndex)) {
                    list.remove(i);
                    i--;
                }
            }
        }
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

        /*for (int i = 0; i < list.size(); i++) {

         if (list.get(i) instanceof WhileItem) {
         WhileItem w = (WhileItem) list.get(i);

         }
         }*/
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof SetLocalAVM2Item) {
                SetLocalAVM2Item ri = (SetLocalAVM2Item) list.get(i);
                int setLocalIp = avm2code.adr2pos(ri.getSrc().getAddress());
                Set<Integer> usages = localData.registerUsage.get(setLocalIp);
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
                            ThrowAVM2Item t = (ThrowAVM2Item) list.get(i + 2);
                            t.value = ri.value;
                            list.remove(i);
                            i--;
                            continue;
                        }
                    }

                    if (i + 2 < list.size()) {
                        if ((list.get(i + 1) instanceof IntegerValueAVM2Item) && (list.get(i + 2) instanceof ReturnValueAVM2Item)
                                && (list.get(i + 2).value instanceof LocalRegAVM2Item)
                                && (((LocalRegAVM2Item) list.get(i + 2).value).regIndex == ri.regIndex)) {
                            ReturnValueAVM2Item r = (ReturnValueAVM2Item) list.get(i + 2);
                            r.value = ri.value;
                            list.remove(i + 1);
                            list.remove(i);
                            i--;
                            continue;
                        }
                        if ((list.get(i + 1) instanceof IntegerValueAVM2Item) && (list.get(i + 2) instanceof ThrowAVM2Item)
                                && (list.get(i + 2).value instanceof LocalRegAVM2Item)
                                && (((LocalRegAVM2Item) list.get(i + 2).value).regIndex == ri.regIndex)) {
                            ThrowAVM2Item t = (ThrowAVM2Item) list.get(i + 2);
                            t.value = ri.value;
                            list.remove(i + 1);
                            list.remove(i);
                            i--;
                            continue;
                        }
                    } else if (i + 1 < list.size() && usages.isEmpty()) {
                        if (list.get(i + 1) instanceof IntegerValueAVM2Item) {
                            list.remove(i + 1);
                        }
                    }
                }
            }
        }

        List<GraphTargetItem> ret = list;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof SetTypeAVM2Item) {
                if (((SetTypeAVM2Item) list.get(i)).getValue() instanceof ExceptionAVM2Item) {
                    list.remove(i);
                    i--;
                    continue;
                }
            }
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
        //Handle for loops at the end:
        super.finalProcess(list, level, localData, path);
    }

    @Override
    protected FinalProcessLocalData getFinalData(BaseLocalData localData, List<Loop> loops) {
        FinalProcessLocalData finalProcess = super.getFinalData(localData, loops);
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
    protected void checkSwitch(BaseLocalData localData, SwitchItem switchItem, Map<Integer, GraphTargetItem> otherSides, List<GraphTargetItem> output) {
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
        for (GraphTargetItem otherSide : otherSides.values()) {
            if (otherSide instanceof LocalRegAVM2Item) {
                LocalRegAVM2Item otherLog = (LocalRegAVM2Item) otherSide;
                int getLocalIp = InstructionDefinition.getItemIp(avm2LocalData, otherLog);
                allUsages.remove((Integer) getLocalIp);
            }
        }
        if (allUsages.isEmpty()) {
            output.remove(output.size() - 1);
            switchItem.switchedObject = setLocal.value;
        }
    }
}

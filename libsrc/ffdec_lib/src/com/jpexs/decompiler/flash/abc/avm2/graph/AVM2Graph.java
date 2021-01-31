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
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.HasNext2Ins;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.DecLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other2.IncLocalPIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FilteredCheckAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.HasNextAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NextNameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NextValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
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
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LoopItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.PopItem;
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
    protected void beforePrintGraph(BaseLocalData localData, String path, Set<GraphPart> allParts, List<Loop> loops) throws InterruptedException {
        AVM2LocalData avm2LocalData = ((AVM2LocalData) localData);
        avm2LocalData.codeStats = avm2LocalData.code.getStats(avm2LocalData.abc, avm2LocalData.methodBody, avm2LocalData.methodBody.init_scope_depth, false);
        getIgnoredSwitches((AVM2LocalData) localData, allParts);
        Set<Integer> integerSwitchesIps = new HashSet<>();
        for (GraphPart p : ((AVM2LocalData) localData).ignoredSwitches.values()) {
            integerSwitchesIps.add(p.end);
        }
        Map<Integer, Set<Integer>> setLocalPosToGetLocalPos = calculateLocalRegsUsage(integerSwitchesIps, path, allParts);
        avm2LocalData.setLocalPosToGetLocalPos = setLocalPosToGetLocalPos;
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

            final int FINALLY_KIND_STACK_BASED = 0;
            final int FINALLY_KIND_REGISTER_BASED = 1;
            final int FINALLY_KIND_INLINED = 2;
            final int FINALLY_KIND_UNKNOWN = -1;

            int switchedReg = -1;
            int finallyKind = FINALLY_KIND_UNKNOWN;
            if (finallyTryTargetStack.size() == 1) {
                finallyKind = FINALLY_KIND_STACK_BASED;
            } else if (targetOutput.size() >= 2
                    && (targetOutput.get(targetOutput.size() - 1) instanceof SetLocalAVM2Item)
                    && (targetOutput.get(targetOutput.size() - 2) instanceof SetLocalAVM2Item)) {
                switchedReg = ((SetLocalAVM2Item) targetOutput.get(targetOutput.size() - 1)).regIndex;
                finallyKind = FINALLY_KIND_REGISTER_BASED;
            } else if (!targetOutput.isEmpty() && (targetOutput.get(targetOutput.size() - 1) instanceof ThrowAVM2Item)) {
                //inlined to single part                    
                //TODO: maybe replace all instances of exit nodes of try block
                finallyKind = FINALLY_KIND_INLINED;
            } else {
                //probably inlined code in more parts, cannot do :-(                    
            }

            if (finallyKind == FINALLY_KIND_STACK_BASED) {

                /*
                    Search for a lookupswitch which first pops from the stack
                 */
                List<Integer> foundIps = new ArrayList<>();
                List<GraphPart> foundParts = new ArrayList<>();
                int stackAfter = localData.codeStats.instructionStats[finallyTryTargetPart.end].stackpos_after;

                //int stackAfter = localData.codeStats.instructionStats[finallyPart.start].stackpos;
                findAllPops(localData, stackAfter, finallyPart, foundIps, foundParts, new HashSet<>());
                int switchIp = -1;
                GraphPart switchPart = null;
                for (int i = 0; i < foundIps.size(); i++) {
                    int ip = foundIps.get(i);
                    if (avm2code.code.get(ip).definition instanceof LookupSwitchIns) {
                        switchIp = ip;
                        switchPart = foundParts.get(i);
                    }
                }
                if (switchIp > -1 && switchPart != null) {
                    for (GraphPart r : finallyPart.refs) {
                        for (int ip = r.end; ip >= r.start; ip--) {
                            AVM2Instruction ins = avm2code.code.get(ip);
                            if (ins.definition instanceof JumpIns) {
                                continue;
                            } else if (ins.definition instanceof PushByteIns) {
                                int val = ins.operands[0];
                                if (val < 0 || val > switchPart.nextParts.size() - 2) {
                                    localData.finallyJumps.put(r, switchPart.nextParts.get(0)); //default branch
                                } else {
                                    localData.finallyJumps.put(r, switchPart.nextParts.get(1 + val));
                                }
                            }
                        }
                    }

                    //return in finally block is joined after switch decision
                    for (GraphPart p : switchPart.nextParts) {
                        for (GraphPart r : p.refs) {
                            if (r != switchPart) {
                                localData.finallyJumps.put(r, p);
                            }
                        }
                    }

                    localData.ignoredSwitches.put(e, switchPart);
                } else {
                    //there is probably return in all branches and no other way outside finally
                }

            }
        }
    }

    public Map<Integer, Set<Integer>> calculateLocalRegsUsage(Set<Integer> ignoredSwitches, String path, Set<GraphPart> allParts) {
        logger.log(Level.FINE, "--- {0} ---", path);
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
            logger.log(Level.FINE, "set local reg {0} at pos {1}{2}", new Object[]{regId, setLocalPos, 1});

            for (int getLocalPos : setLocalPosToGetLocalPos.get(setLocalPos)) {
                logger.log(Level.FINE, "- usage at pos {0}{1}", new Object[]{getLocalPos, 1});
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
            if (q.nextParts.isEmpty() || !next.equals(q.nextParts.get(0))) { //first is after finally
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
                    //p.throwParts.add(target);
                    //target.refs.add(p);
                }
            }
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

    private void findAllPops(AVM2LocalData localData, int stackLevel, GraphPart part, List<Integer> foundIps, List<GraphPart> foundParts, Set<GraphPart> visited) {
        if (visited.contains(part)) {
            return;
        }
        visited.add(part);
        for (int ip = part.start; ip <= part.end; ip++) {
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

    private List<GraphTargetItem> checkTry(List<GraphTargetItem> output, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, AVM2LocalData localData, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, Set<GraphPart> allParts, TranslateStack stack, int staticOperation, String path) throws InterruptedException {
        if (localData.parsedExceptions == null) {
            localData.parsedExceptions = new ArrayList<>();
        }
        List<ABCException> parsedExceptions = localData.parsedExceptions;
        if (localData.finallyJumps == null) {
            localData.finallyJumps = new HashMap<>();
        }
        if (localData.ignoredSwitches == null) {
            localData.ignoredSwitches = new HashMap<>();
        }
        long addr = avm2code.getAddrThroughJumpAndDebugLine(avm2code.pos2adr(part.start));
        long maxEndAddr = -1;
        List<ABCException> catchedExceptions = new ArrayList<>();
        ABCException finallyException = null;
        int endIp = -1;
        int finallyIndex = -1;
        for (int e = 0; e < body.exceptions.length; e++) {
            if (addr == avm2code.getAddrThroughJumpAndDebugLine(body.exceptions[e].start)) {
                if (!parsedExceptions.contains(body.exceptions[e])) {
                    long endAddr = avm2code.getAddrThroughJumpAndDebugLine(body.exceptions[e].end);
                    if (endAddr > maxEndAddr) {
                        catchedExceptions.clear();
                        finallyException = null;
                        finallyIndex = -1;
                        maxEndAddr = avm2code.getAddrThroughJumpAndDebugLine(body.exceptions[e].end);
                        endIp = avm2code.adr2pos(maxEndAddr);
                        catchedExceptions.add(body.exceptions[e]);
                    } else if (endAddr == maxEndAddr) {
                        catchedExceptions.add(body.exceptions[e]);
                    }
                    if (body.exceptions[e].isFinally()) {
                        finallyException = body.exceptions[e];
                        finallyIndex = e;
                    }
                }
            }
        }
        if (catchedExceptions.size() > 0) {
            parsedExceptions.addAll(catchedExceptions);
            if (finallyException != null) {
                catchedExceptions.remove(finallyException);
            }
            List<GraphTargetItem> tryCommands = new ArrayList<>();
            List<List<GraphTargetItem>> catchCommands = new ArrayList<>();
            List<GraphTargetItem> finallyCommands = new ArrayList<>();

            GraphPart afterPart = null;
            for (GraphPart p : allParts) {
                if (endIp >= p.start && endIp <= p.end) {
                    afterPart = p;
                    break;
                }
            }

            stack.clear(); //If the original code (before check()) had "if" in it, there would be something on stack

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
                stopPart2.add(afterPart);

                List<GraphTargetItem> currentCatchCommands = printGraph(foundGotos, partCodes, partCodePos, localData2, st2, allParts, null, catchPart, stopPart2, loops, staticOperation, path);
                if (!currentCatchCommands.isEmpty() && (currentCatchCommands.get(0) instanceof SetLocalAVM2Item)) {
                    if (currentCatchCommands.get(0).value.getNotCoerced() instanceof ExceptionAVM2Item) {
                        currentCatchCommands.remove(0);
                    }
                }
                catchCommands.add(currentCatchCommands);
            }

            if (finallyException == null) {
                List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                stopPart2.add(afterPart);
                tryCommands = printGraph(foundGotos, partCodes, partCodePos, localData, stack, allParts, null, part, stopPart2, loops, staticOperation, path);
            }

            if (finallyException != null) {
                afterPart = null;
                GraphPart finallyTryTargetPart = null;
                int targetPos = avm2code.adr2pos(finallyException.target);
                for (GraphPart p : allParts) {
                    if (p.start == targetPos) {
                        finallyTryTargetPart = p;
                        break;
                    }
                }

                GraphPart finallyPart = finallyTryTargetPart.nextParts.isEmpty() ? null : finallyTryTargetPart.nextParts.get(0);

                List<GraphPart> tryStopPart = new ArrayList<>(stopPart);
                if (finallyPart != null) {
                    tryStopPart.add(finallyPart);
                }
                tryCommands = printGraph(foundGotos, partCodes, partCodePos, localData, stack, allParts, null, part, tryStopPart, loops, staticOperation, path);
                makeAllCommands(tryCommands, stack);
                processIfs(tryCommands);

                //there should be §§push(-1) left
                if (!tryCommands.isEmpty()
                        && (tryCommands.get(tryCommands.size() - 1) instanceof PushItem)
                        && (tryCommands.get(tryCommands.size() - 1).value instanceof IntegerValueAVM2Item)) {
                    tryCommands.remove(tryCommands.size() - 1);
                }

                List<GraphPart> finallyStopPart = new ArrayList<>(stopPart);
                GraphPart switchPart = localData.ignoredSwitches.containsKey(finallyIndex) ? localData.ignoredSwitches.get(finallyIndex) : null;
                if (switchPart != null) {
                    finallyStopPart.add(switchPart);
                }
                if (finallyPart != null) {
                    finallyCommands = printGraph(foundGotos, partCodes, partCodePos, localData, stack, allParts, null, finallyPart, finallyStopPart, loops, staticOperation, path);
                }
                if (switchPart != null) {
                    finallyCommands.addAll(translatePart(localData, switchPart, stack, staticOperation, path));
                    afterPart = switchPart.nextParts.get(0); //take the default branch
                }
                stack.pop();

                if (tryCommands.size() == 1
                        && (tryCommands.get(0) instanceof TryAVM2Item)
                        && catchCommands.isEmpty()
                        && ((TryAVM2Item) tryCommands.get(0)).finallyCommands.isEmpty()) {
                    catchCommands = ((TryAVM2Item) tryCommands.get(0)).catchCommands;
                    catchedExceptions = ((TryAVM2Item) tryCommands.get(0)).catchExceptions;
                    tryCommands = ((TryAVM2Item) tryCommands.get(0)).tryCommands;
                }
            }
            if (catchCommands.isEmpty() && finallyCommands.isEmpty() && tryCommands.isEmpty()) {
                return null;
            }
            List<GraphTargetItem> ret = new ArrayList<>();
            ret.add(new TryAVM2Item(tryCommands, catchedExceptions, catchCommands, finallyCommands, "TODO"));

            if (afterPart != null) {
                ret.addAll(printGraph(foundGotos, partCodes, partCodePos, localData, stack, allParts, null, afterPart, stopPart, loops, staticOperation, path));
            }
            return ret;
        }
        return null;
    }

    @Override
    protected List<GraphTargetItem> check(List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) throws InterruptedException {
        List<GraphTargetItem> ret = null;

        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        ret = checkTry(output, foundGotos, partCodes, partCodePos, aLocalData, part, stopPart, loops, allParts, stack, staticOperation, path);
        if (ret != null) {
            return ret;
        }
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
                if (code.size() > defaultPart.start && ((AVM2Instruction) code.get(defaultPart.start)).definition instanceof JumpIns) {
                    defaultPart = defaultPart.nextParts.get(0);
                }

                ret = new ArrayList<>();
                ret.addAll(output);
                Reference<GraphPart> nextRef = new Reference<>(null);
                Reference<GraphTargetItem> tiRef = new Reference<>(null);
                SwitchItem sw = handleSwitch(switchedObject, switchStartItem, foundGotos, partCodes, partCodePos, allParts, stack, stopPart, loops, localData, staticOperation, path, caseValuesMap, defaultPart, caseBodyParts, nextRef, tiRef);
                checkSwitch(localData, sw, otherSide, ret);
                ret.add(sw);
                if (nextRef.getVal() != null) {
                    if (tiRef.getVal() != null) {
                        ret.add(tiRef.getVal());
                    } else {
                        ret.addAll(printGraph(foundGotos, partCodes, partCodePos, localData, stack, allParts, null, nextRef.getVal(), stopPart, loops, staticOperation, path));
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
    protected GraphPart checkPart(TranslateStack stack, BaseLocalData localData, GraphPart prev, GraphPart next, Set<GraphPart> allParts) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        if (aLocalData.finallyJumps == null) {
            aLocalData.finallyJumps = new HashMap<>();
        }
        if (aLocalData.ignoredSwitches == null) {
            aLocalData.ignoredSwitches = new HashMap<>();
        }

        if (prev != null) {
            if (aLocalData.ignoredSwitches.containsValue(prev)) {
                return null;
            }
            if (aLocalData.finallyJumps.containsKey(prev)) {
                return aLocalData.finallyJumps.get(prev);
            }
        }

        /*Map<Integer, List<Integer>> finallyJumps = aLocalData.finallyJumps;
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
                        GraphTargetItem t = stack.peek();
                        Double dval = t.getResultAsNumber();
                        int val = (int) (double) dval;
                        if (swIns.definition instanceof LookupSwitchIns) {
                            List<Integer> branches = swIns.getBranches(code);
                            int nip = branches.get(0);
                            if (val >= 0 && val < branches.size() - 1) {
                                nip = branches.get(1 + val);
                            }
                            for (GraphPart p : allParts) {
                                if (avm2code.getIpThroughJumpAndDebugLine(p.start) == avm2code.getIpThroughJumpAndDebugLine(nip)) {
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
        long addr = avm2code.getAddrThroughJumpAndDebugLine(avm2code.pos2adr(pos));
        for (int e = 0; e < body.exceptions.length; e++) {
            if (body.exceptions[e].isFinally()) {
                if (addr == avm2code.getAddrThroughJumpAndDebugLine(body.exceptions[e].start)) {
                    if (true) { //afterCatchPos + 1 == code.adr2pos(this.code.fixAddrAfterDebugLine(body.exceptions[e].end))) {
                        AVM2Instruction jmpIns = avm2code.code.get(avm2code.adr2pos(avm2code.getAddrThroughJumpAndDebugLine(body.exceptions[e].end)));
                        if (jmpIns.definition instanceof JumpIns) {
                            int finStart = avm2code.adr2pos(avm2code.getAddrThroughJumpAndDebugLine(body.exceptions[e].end) + jmpIns.getBytesLength() + jmpIns.operands[0]);
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
        }*/
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
        /*for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof SetLocalAVM2Item) {
                SetLocalAVM2Item ri = (SetLocalAVM2Item) list.get(i);
                if (localData.temporaryRegisters.contains(ri.regIndex)) {
                    list.remove(i);
                    i--;
                }
            }
        }*/
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

        for (int i = 0; i < list.size(); i++) {
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
                            ThrowAVM2Item t = (ThrowAVM2Item) list.get(i + 2);
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
}

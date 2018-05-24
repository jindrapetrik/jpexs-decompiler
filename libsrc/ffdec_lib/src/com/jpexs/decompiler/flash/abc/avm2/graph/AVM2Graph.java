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
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.KillIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FilteredCheckAVM2Item;
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
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LoopItem;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.PopItem;
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

/**
 *
 * @author JPEXS
 */
public class AVM2Graph extends Graph {

    private final AVM2Code avm2code;

    private final ABC abc;

    private final MethodBody body;

    public AVM2Code getCode() {
        return avm2code;
    }

    public AVM2Graph(AVM2Code code, ABC abc, MethodBody body, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, ScopeStack scopeStack, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, HashMap<Integer, Integer> localRegAssigmentIps, HashMap<Integer, List<Integer>> refs) {
        super(new AVM2GraphSource(code, isStatic, scriptIndex, classIndex, localRegs, scopeStack, abc, body, localRegNames, fullyQualifiedNames, localRegAssigmentIps, refs), body.getExceptionEntries());
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
                    p.throwParts.add(target);
                    target.refs.add(p);
                }
            }
        }
    }

    @Override
    protected List<GraphTargetItem> check(Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) throws InterruptedException {
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
                                    finallyCommands = printGraph(partCodes, partCodePos, localData, st, allParts, parent, fpart, null, loops, staticOperation, path);
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
                    localData2.scopeStack = new ScopeStack();
                    List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                    stopPart2.add(nepart);
                    if (retPart != null) {
                        stopPart2.add(retPart);
                    }

                    List<GraphTargetItem> ncatchedCommands = printGraph(partCodes, partCodePos, localData2, st2, allParts, parent, npart, stopPart2, loops, staticOperation, path);
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
                List<GraphTargetItem> tryCommands = printGraph(partCodes, partCodePos, localData, st, allParts, parent, part, stopPart2, loops, staticOperation, path);
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
            GraphTargetItem lop = checkLoop(part, stopPart, loops);
            if (lop == null) {
                TranslateStack st = (TranslateStack) stack.clone();
                st.clear();

                ret.addAll(printGraph(partCodes, partCodePos, localData, st, allParts, null, part, stopPart, loops, staticOperation, path));
            } else {
                ret.add(lop);
            }
            return ret;
        }

        if (part.nextParts.isEmpty()) {
            if (avm2code.code.get(part.end).definition instanceof ReturnValueIns) {  //returns in finally clause
                if (part.getHeight() >= 3) {
                    if (avm2code.code.get(part.getPosAt(part.getHeight() - 2)).definition instanceof KillIns) {
                        if (avm2code.code.get(part.getPosAt(part.getHeight() - 3)).definition instanceof GetLocalTypeIns) {
                            if (output.size() >= 2) {
                                if (output.get(output.size() - 2) instanceof SetLocalAVM2Item) {
                                    ret = new ArrayList<>();
                                    ret.addAll(output);
                                    ret.remove(ret.size() - 1);
                                    GraphTargetItem v = ((SetLocalAVM2Item) output.get(output.size() - 2)).value;
                                    ret.add(new ReturnValueAVM2Item(avm2code.code.get(part.end), (AVM2Instruction) v.getLineStartItem(), v));
                                    return ret;
                                }
                            }
                        }
                    }
                }
            }
        }
        if ((avm2code.code.get(part.end).definition instanceof LookupSwitchIns) && (ignoredSwitches.containsValue(part.end) || ignoredSwitches2.contains(part.end))) {
            ret = new ArrayList<>();
            ret.addAll(output);
            return ret;
        }
        return ret;
    }

    @Override
    protected GraphPart checkPart(TranslateStack stack, BaseLocalData localData, GraphPart next, Set<GraphPart> allParts) {
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

        return next;
    }

    @Override
    protected GraphTargetItem checkLoop(LoopItem loopItem, BaseLocalData localData, List<Loop> loops) {
        AVM2LocalData aLocalData = (AVM2LocalData) localData;
        if (loopItem instanceof WhileItem) {
            WhileItem w = (WhileItem) loopItem;

            if ((!w.expression.isEmpty()) && (w.expression.get(w.expression.size() - 1) instanceof HasNextAVM2Item)) {
                HasNextAVM2Item hn = (HasNextAVM2Item) w.expression.get(w.expression.size() - 1);
                if (((HasNextAVM2Item) w.expression.get(w.expression.size() - 1)).obj != null) {
                    if (((HasNextAVM2Item) w.expression.get(w.expression.size() - 1)).obj.getNotCoerced().getThroughRegister() instanceof FilteredCheckAVM2Item) {
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
                                                HasNextAVM2Item iti = (HasNextAVM2Item) w.expression.get(w.expression.size() - 1);
                                                HashMap<Integer, GraphTargetItem> localRegs = aLocalData.localRegs;
                                                localRegs.put(regIndex, new FilterAVM2Item(null, null, iti.obj.getThroughRegister(), ift.expression));
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
                            if (gti instanceof NextValueAVM2Item) {
                                return new ForEachInAVM2Item(w.getSrc(), w.getLineStartItem(), w.loop, new InAVM2Item(hn.getInstruction(), hn.getLineStartIns(), sti.getObject(), ((HasNextAVM2Item) w.expression.get(w.expression.size() - 1)).obj), w.commands);
                            } else if (gti instanceof NextNameAVM2Item) {
                                return new ForInAVM2Item(w.getSrc(), w.getLineStartItem(), w.loop, new InAVM2Item(hn.getInstruction(), hn.getLineStartIns(), sti.getObject(), ((HasNextAVM2Item) w.expression.get(w.expression.size() - 1)).obj), w.commands);
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
                if (ri.value.getNotCoerced() instanceof ExceptionAVM2Item) {
                    ExceptionAVM2Item ea = (ExceptionAVM2Item) ri.value.getNotCoerced();
                    if (ea.exception.isFinally()) {
                        list.remove(i);
                        localData.temporaryRegisters.add(ri.regIndex);
                        i--;
                        continue;
                    }
                }
                if (avm2code.isKilled(ri.regIndex, 0, Integer.MAX_VALUE)) {
                    if (i + 1 < list.size()) {
                        if (list.get(i + 1) instanceof SwitchItem) {
                            SwitchItem si = (SwitchItem) list.get(i + 1);
                            if (si.switchedObject instanceof LocalRegAVM2Item) {
                                if (((LocalRegAVM2Item) si.switchedObject).regIndex == ri.regIndex) {
                                    si.switchedObject = ri.value;
                                }
                            }
                        }
                    }
                    if (i + 2 < list.size()) {
                        if ((list.get(i + 1) instanceof IntegerValueAVM2Item) && (list.get(i + 2) instanceof ReturnValueAVM2Item)) {
                            ReturnValueAVM2Item r = (ReturnValueAVM2Item) list.get(i + 2);
                            r.value = ri.value;
                            list.remove(i + 1);
                            continue;
                        }
                        if ((list.get(i + 1) instanceof IntegerValueAVM2Item) && (list.get(i + 2) instanceof ThrowAVM2Item)) {
                            ThrowAVM2Item t = (ThrowAVM2Item) list.get(i + 2);
                            t.value = ri.value;
                            list.remove(i + 1);
                            //continue;
                        }
                    } else if (i + 1 < list.size()) {
                        if (list.get(i + 1) instanceof IntegerValueAVM2Item) {
                            list.remove(i + 1);
                        }
                    }
                }
            }
        }

        List<GraphTargetItem> ret = avm2code.clearTemporaryRegisters(list);
        if (ret != list) {
            list.clear();
            list.addAll(ret);
        }
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
    protected boolean isEmpty(List<GraphTargetItem> output) {
        if (super.isEmpty(output)) {
            return true;
        }
        for (GraphTargetItem i : output) {
            if (i instanceof SetLocalAVM2Item) {
                if (avm2code.isKilled(((SetLocalAVM2Item) i).regIndex, 0, avm2code.code.size() - 1)) {
                    continue;
                }
            }
            return false;
        }
        return true;
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
    protected List<GraphTargetItem> filter(List<GraphTargetItem> list) {
        return avm2code.clearTemporaryRegisters(list);
    }
}

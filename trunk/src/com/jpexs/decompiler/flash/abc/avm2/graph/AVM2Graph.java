/*
 *  Copyright (C) 2010-2013 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.graph;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfStrictEqIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.IfStrictNeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.LookupSwitchIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.KillIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushIntegerTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.FilteredCheckTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.HasNextTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.InTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.LocalRegTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NextNameTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NextValueTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NullTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ReturnValueTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ReturnVoidTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.SetLocalTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.SetPropertyTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.SetTypeTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.WithTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.ExceptionTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.FilterTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.ForEachInTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.ForInTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.TryTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.operations.StrictEqTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.operations.StrictNeqTreeItem;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.graph.BreakItem;
import com.jpexs.decompiler.flash.graph.Graph;
import com.jpexs.decompiler.flash.graph.GraphPart;
import com.jpexs.decompiler.flash.graph.GraphPartMulti;
import com.jpexs.decompiler.flash.graph.GraphSource;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.IfItem;
import com.jpexs.decompiler.flash.graph.Loop;
import com.jpexs.decompiler.flash.graph.LoopItem;
import com.jpexs.decompiler.flash.graph.NotItem;
import com.jpexs.decompiler.flash.graph.SwitchItem;
import com.jpexs.decompiler.flash.graph.WhileItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 *
 * @author JPEXS
 */
public class AVM2Graph extends Graph {

    private AVM2Code code;
    private ABC abc;
    private MethodBody body;

    public AVM2Code getCode() {
        return code;
    }

    public AVM2Graph(AVM2Code code, ABC abc, MethodBody body, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> scopeStack, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        super(new AVM2GraphSource(code, isStatic, scriptIndex, classIndex, localRegs, scopeStack, abc, body, localRegNames, fullyQualifiedNames), body.getExceptionEntries());
        this.code = code;
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
    public static final int DATA_ISSTATIC = 0;
    public static final int DATA_CLASSINDEX = 1;
    public static final int DATA_LOCALREGS = 2;
    public static final int DATA_SCOPESTACK = 3;
    public static final int DATA_CONSTANTS = 4;
    public static final int DATA_METHOD_INFO = 5;
    public static final int DATA_BODY = 6;
    public static final int DATA_ABC = 7;
    public static final int DATA_LOCALREGNAMES = 8;
    public static final int DATA_FQN = 9;
    public static final int DATA_PARSEDEXCEPTIONS = 10;
    public static final int DATA_FINALLYJUMPS = 11;
    public static final int DATA_IGNOREDSWITCHES = 12;

    public static List<GraphTargetItem> translateViaGraph(String path, AVM2Code code, ABC abc, MethodBody body, boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, Stack<GraphTargetItem> scopeStack, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        AVM2Graph g = new AVM2Graph(code, abc, body, isStatic, scriptIndex, classIndex, localRegs, scopeStack, localRegNames, fullyQualifiedNames);
        g.init();
        List<GraphPart> allParts = new ArrayList<>();
        for (GraphPart head : g.heads) {
            populateParts(head, allParts);
        }
        List<Object> localData = new ArrayList<>();
        localData.add((Boolean) isStatic);
        localData.add((Integer) classIndex);
        localData.add(localRegs);
        localData.add(scopeStack);
        localData.add(abc.constants);
        localData.add(abc.method_info);
        localData.add(body);
        localData.add(abc);
        localData.add(localRegNames);
        localData.add(fullyQualifiedNames);
        localData.add(new ArrayList<ABCException>());
        localData.add(new ArrayList<Integer>());
        localData.add(new ArrayList<Integer>());
        localData.add((Integer) scriptIndex);
        return g.translate(localData);
    }

    @Override
    protected void checkGraph(List<GraphPart> allBlocks) {
        for (ABCException ex : body.exceptions) {
            int startIp = code.adr2pos(ex.start);
            int endIp = code.adr2pos(ex.end);
            int targetIp = code.adr2pos(ex.target);
            GraphPart target = null;
            for (GraphPart p : allBlocks) {
                if (p.start == targetIp) {
                    target = p;
                    break;
                }
            }
            for (GraphPart p : allBlocks) {
                if (p.start >= startIp && p.end <= endIp) {
                    p.throwParts.add(target);
                    target.refs.add(p);
                }
            }
        }

        /*for(ABCException ex:body.exceptions){
         for(GraphPart p:allBlocks){
         boolean next_is_ex_start=false;
         for(GraphPart n:p.nextParts){
         if(n.start==code.adr2pos(ex.start)){
         next_is_ex_start = true;
         break;
         }
         }
         if(next_is_ex_start){
         for(GraphPart q:allBlocks){ //find target part
         if(q.start==code.adr2pos(ex.target)){
         p.nextParts.add(q);
         break;
         }
         }
         }
         }
         }*/
    }

    @Override
    protected List<GraphTargetItem> check(GraphSource srcCode, List<Object> localData, List<GraphPart> allParts, Stack<GraphTargetItem> stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<GraphTargetItem> output, Loop currentLoop) {
        List<GraphTargetItem> ret = null;



        @SuppressWarnings("unchecked")
        List<ABCException> parsedExceptions = (List<ABCException>) localData.get(DATA_PARSEDEXCEPTIONS);
        @SuppressWarnings("unchecked")
        List<Integer> finallyJumps = (List<Integer>) localData.get(DATA_FINALLYJUMPS);
        @SuppressWarnings("unchecked")
        List<Integer> ignoredSwitches = (List<Integer>) localData.get(DATA_IGNOREDSWITCHES);
        int ip = part.start;
        int addr = code.fixAddrAfterDebugLine(code.pos2adr(part.start));
        int maxend = -1;
        List<ABCException> catchedExceptions = new ArrayList<>();
        for (int e = 0; e < body.exceptions.length; e++) {
            if (addr == code.fixAddrAfterDebugLine(body.exceptions[e].start)) {
                if (!body.exceptions[e].isFinally()) {
                    if (((body.exceptions[e].end) > maxend) && (!parsedExceptions.contains(body.exceptions[e]))) {
                        catchedExceptions.clear();
                        maxend = code.fixAddrAfterDebugLine(body.exceptions[e].end);
                        catchedExceptions.add(body.exceptions[e]);
                    } else if (code.fixAddrAfterDebugLine(body.exceptions[e].end) == maxend) {
                        catchedExceptions.add(body.exceptions[e]);
                    }
                }
            }
        }
        if (catchedExceptions.size() > 0) {
            /*if (currentLoop != null) {
             //currentLoop.phase=0;
             }*/
            parsedExceptions.addAll(catchedExceptions);
            int endpos = code.adr2pos(code.fixAddrAfterDebugLine(catchedExceptions.get(0).end));
            int endposStartBlock = code.adr2pos(catchedExceptions.get(0).end);


            List<List<GraphTargetItem>> catchedCommands = new ArrayList<>();
            if (code.code.get(endpos).definition instanceof JumpIns) {
                int afterCatchAddr = code.pos2adr(endpos + 1) + code.code.get(endpos).operands[0];
                int afterCatchPos = code.adr2pos(afterCatchAddr);
                Collections.sort(catchedExceptions, new Comparator<ABCException>() {
                    @Override
                    public int compare(ABCException o1, ABCException o2) {
                        return code.fixAddrAfterDebugLine(o1.target) - code.fixAddrAfterDebugLine(o2.target);
                    }
                });


                List<GraphTargetItem> finallyCommands = new ArrayList<>();
                int returnPos = afterCatchPos;
                for (int e = 0; e < body.exceptions.length; e++) {
                    if (body.exceptions[e].isFinally()) {
                        if (addr == code.fixAddrAfterDebugLine(body.exceptions[e].start)) {
                            if (afterCatchPos + 1 == code.adr2pos(code.fixAddrAfterDebugLine(body.exceptions[e].end))) {
                                AVM2Instruction jmpIns = code.code.get(code.adr2pos(code.fixAddrAfterDebugLine(body.exceptions[e].end)));
                                if (jmpIns.definition instanceof JumpIns) {
                                    int finStart = code.adr2pos(code.fixAddrAfterDebugLine(body.exceptions[e].end) + jmpIns.getBytes().length + jmpIns.operands[0]);

                                    boolean switchFound = false;
                                    GraphPart fpart = null;
                                    for (GraphPart p : allParts) {
                                        if (p.start == finStart) {
                                            fpart = p;
                                            break;
                                        }
                                    }
                                    for (int f = finStart; f < code.code.size(); f++) {
                                        if (code.code.get(f).definition instanceof LookupSwitchIns) {
                                            AVM2Instruction swins = code.code.get(f);
                                            if (swins.operands.length >= 3) {
                                                if (swins.operands[0] == swins.getBytes().length) {
                                                    if (code.adr2pos(code.pos2adr(f) + swins.operands[2]) < finStart) {
                                                        stack.push(new ExceptionTreeItem(body.exceptions[e]));
                                                        GraphPart fepart = null;
                                                        for (GraphPart p : allParts) {
                                                            if (p.start == f + 1) {
                                                                fepart = p;
                                                                break;
                                                            }
                                                        }
                                                        //code.code.get(f).ignored = true;
                                                        ignoredSwitches.add(f);
                                                        List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                                                        stopPart2.add(fepart);
                                                        finallyCommands = printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, parent, fpart, stopPart2, loops);
                                                        returnPos = f + 1;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!switchFound) {
                                        finallyCommands = printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, parent, fpart, null, loops);
                                    }
                                    finallyJumps.add(finStart);
                                    break;
                                }
                            }
                        }
                    }
                }
                List<GraphPart> catchParts = new ArrayList<>();
                for (int e = 0; e < catchedExceptions.size(); e++) {
                    int eendpos;
                    if (e < catchedExceptions.size() - 1) {
                        eendpos = code.adr2pos(code.fixAddrAfterDebugLine(catchedExceptions.get(e + 1).target)) - 2;
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
                    stack.add(new ExceptionTreeItem(catchedExceptions.get(e)));
                    List<Object> localData2 = new ArrayList<>();
                    localData2.addAll(localData);
                    localData2.set(DATA_SCOPESTACK, new Stack<GraphTargetItem>());
                    List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                    stopPart2.add(nepart);
                    catchedCommands.add(printGraph(new ArrayList<GraphPart>(), localData2, stack, allParts, parent, npart, stopPart2, loops));
                }

                GraphPart nepart = null;

                for (GraphPart p : allParts) {
                    if (p.start == endposStartBlock) {
                        nepart = p;
                        break;
                    }
                }
                List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                stopPart2.add(nepart);
                stopPart2.addAll(catchParts);
                List<GraphTargetItem> tryCommands = printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, parent, part, stopPart2, loops);

                output.clear();
                output.add(new TryTreeItem(tryCommands, catchedExceptions, catchedCommands, finallyCommands));
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
                ret.addAll(printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, null, part, stopPart, loops));
            } else {
                ret.add(lop);
            }
            return ret;
        }

        if (part.nextParts.isEmpty()) {
            if (code.code.get(part.end).definition instanceof ReturnValueIns) {  //returns in finally clause
                if (part.getHeight() >= 3) {
                    if (code.code.get(part.getPosAt(part.getHeight() - 2)).definition instanceof KillIns) {
                        if (code.code.get(part.getPosAt(part.getHeight() - 3)).definition instanceof GetLocalTypeIns) {
                            if (output.size() >= 2) {
                                if (output.get(output.size() - 2) instanceof SetLocalTreeItem) {
                                    ret = new ArrayList<>();
                                    ret.addAll(output);
                                    ret.remove(ret.size() - 1);
                                    ret.add(new ReturnValueTreeItem(code.code.get(part.end), ((SetLocalTreeItem) output.get(output.size() - 2)).value));
                                    return ret;
                                }
                            }
                        }
                    }
                }
            }
        }
        if ((code.code.get(part.end).definition instanceof LookupSwitchIns) && ignoredSwitches.contains(part.end)) {
            ret = new ArrayList<>();
            ret.addAll(output);
            return ret;
        }
        if (((part.nextParts.size() == 2)
                && (!stack.isEmpty())
                && (stack.peek() instanceof StrictEqTreeItem)
                && (part.nextParts.get(0).getHeight() >= 2)
                && (code.code.get(code.fixIPAfterDebugLine(part.nextParts.get(0).start)).definition instanceof PushIntegerTypeIns)
                && (code.code.get(part.nextParts.get(0).nextParts.get(0).end).definition instanceof LookupSwitchIns))
                || ((part.nextParts.size() == 2)
                && (!stack.isEmpty())
                && (stack.peek() instanceof StrictNeqTreeItem)
                && (part.nextParts.get(1).getHeight() >= 2)
                && (code.code.get(code.fixIPAfterDebugLine(part.nextParts.get(1).start)).definition instanceof PushIntegerTypeIns)
                && (code.code.get(part.nextParts.get(1).nextParts.get(0).end).definition instanceof LookupSwitchIns))) {

            if (stack.peek() instanceof StrictEqTreeItem) {
                ignoredSwitches.add(part.nextParts.get(0).nextParts.get(0).end);
            } else {
                ignoredSwitches.add(part.nextParts.get(1).nextParts.get(0).end);
            }
            ret = new ArrayList<>();
            ret.addAll(output);
            boolean reversed = false;
            if (stack.peek() instanceof StrictEqTreeItem) {
                reversed = true;
            }
            GraphTargetItem switchedObject = null;
            if (!output.isEmpty()) {
                if (output.get(output.size() - 1) instanceof SetLocalTreeItem) {
                    switchedObject = ((SetLocalTreeItem) output.get(output.size() - 1)).value;
                }
            }
            if (switchedObject == null) {
                switchedObject = new NullTreeItem(null);
            }
            HashMap<Integer, GraphTargetItem> caseValuesMap = new HashMap<>();

            GraphTargetItem tar = stack.pop();
            if (tar instanceof StrictEqTreeItem) {
                tar = ((StrictEqTreeItem) tar).leftSide;
            }
            if (tar instanceof StrictNeqTreeItem) {
                tar = ((StrictNeqTreeItem) tar).leftSide;
            }
            caseValuesMap.put(code.code.get(part.nextParts.get(reversed ? 0 : 1).start).operands[0], tar);

            GraphPart switchLoc = part.nextParts.get(reversed ? 0 : 1).nextParts.get(0);


            while ((code.code.get(part.nextParts.get(reversed ? 1 : 0).end).definition instanceof IfStrictNeIns)
                    || (code.code.get(part.nextParts.get(reversed ? 1 : 0).end).definition instanceof IfStrictEqIns)) {
                part = part.nextParts.get(reversed ? 1 : 0);
                translatePart(localData, part, stack);
                tar = stack.pop();
                if (tar instanceof StrictEqTreeItem) {
                    tar = ((StrictEqTreeItem) tar).leftSide;
                }
                if (tar instanceof StrictNeqTreeItem) {
                    tar = ((StrictNeqTreeItem) tar).leftSide;
                }
                if (code.code.get(part.end).definition instanceof IfStrictNeIns) {
                    reversed = false;
                } else {
                    reversed = true;
                }
                caseValuesMap.put(code.code.get(code.fixIPAfterDebugLine(part.nextParts.get(reversed ? 0 : 1).start)).operands[0], tar);
                while (code.code.get(part.nextParts.get(reversed ? 1 : 0).start).definition instanceof JumpIns) {
                    reversed = false;
                    part = part.nextParts.get(reversed ? 1 : 0);
                    if (part instanceof GraphPartMulti) {
                        part = ((GraphPartMulti) part).parts.get(0);
                    }
                }
            }
            boolean hasDefault = false;
            GraphPart dp = part.nextParts.get(reversed ? 1 : 0);
            while (code.code.get(dp.start).definition instanceof JumpIns) {
                if (dp instanceof GraphPartMulti) {
                    dp = ((GraphPartMulti) dp).parts.get(0);
                }
                dp = dp.nextParts.get(0);
            }
            if (code.code.get(dp.start).definition instanceof PushIntegerTypeIns) {
                hasDefault = true;
            }
            List<GraphTargetItem> caseValues = new ArrayList<>();
            for (int i = 0; i < switchLoc.nextParts.size() - 1; i++) {
                if (caseValuesMap.containsKey(i)) {
                    caseValues.add(caseValuesMap.get(i));
                } else {
                    continue;
                }
            }

            List<List<GraphTargetItem>> caseCommands = new ArrayList<>();
            GraphPart next = null;

            List<GraphPart> loopContinues = getLoopsContinues(loops);

            next = switchLoc.getNextPartPath(loopContinues);
            if (next == null) {
                next = switchLoc.getNextSuperPartPath(loopContinues);
            }

            GraphTargetItem ti = checkLoop(next, stopPart, loops);
            currentLoop = new Loop(loops.size(), null, next);
            currentLoop.phase = 1;
            loops.add(currentLoop);
            //switchLoc.getNextPartPath(new ArrayList<GraphPart>());
            List<Integer> valuesMapping = new ArrayList<>();
            List<GraphPart> caseBodies = new ArrayList<>();
            for (int i = 0; i < caseValues.size(); i++) {
                GraphPart cur = switchLoc.nextParts.get(1 + i);
                if (!caseBodies.contains(cur)) {
                    caseBodies.add(cur);
                }
                valuesMapping.add(caseBodies.indexOf(cur));
            }

            List<GraphTargetItem> defaultCommands = new ArrayList<>();
            GraphPart defaultPart = null;
            if (hasDefault) {
                defaultPart = switchLoc.nextParts.get(switchLoc.nextParts.size() - 1);
                List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                stopPart2.add(next);
                defaultCommands = printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, switchLoc, defaultPart, stopPart2, loops);
                if (!defaultCommands.isEmpty()) {
                    if (defaultCommands.get(defaultCommands.size() - 1) instanceof BreakItem) {
                        if (((BreakItem) defaultCommands.get(defaultCommands.size() - 1)).loopId == currentLoop.id) {
                            defaultCommands.remove(defaultCommands.size() - 1);
                        }
                    }
                }
            }

            List<GraphPart> ignored = new ArrayList<>();
            for (Loop l : loops) {
                ignored.add(l.loopContinue);
            }

            for (int i = 0; i < caseBodies.size(); i++) {
                List<GraphTargetItem> cc = new ArrayList<>();
                GraphPart nextCase = null;
                nextCase = next;
                if (next != null) {
                    if (i < caseBodies.size() - 1) {
                        if (!caseBodies.get(i).leadsTo(srcCode, caseBodies.get(i + 1), loops)) {
                            //cc.add(new BreakItem(null, currentLoop.id));
                        } else {
                            nextCase = caseBodies.get(i + 1);
                        }
                    } else if (hasDefault) {
                        if (!caseBodies.get(i).leadsTo(srcCode, defaultPart, loops)) {
                            //cc.add(new BreakItem(null, currentLoop.id));
                        } else {
                            nextCase = defaultPart;
                        }
                    }
                }
                List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                stopPart2.add(nextCase);
                cc.addAll(0, printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, switchLoc, caseBodies.get(i), stopPart2, loops));
                caseCommands.add(cc);
            }

            SwitchItem sti = new SwitchItem(null, currentLoop, switchedObject, caseValues, caseCommands, defaultCommands, valuesMapping);
            ret.add(sti);
            //loops.remove(currentLoop);
            if (next != null) {
                /*if (ti != null) {
                 ret.add(ti);
                 } else {*/
                currentLoop.phase = 2;
                ret.addAll(printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, null, next, stopPart, loops));
                //}
            }
        }
        return ret;
    }

    @Override
    protected GraphPart checkPart(List<Object> localData, GraphPart next) {
        @SuppressWarnings("unchecked")
        List<Integer> finallyJumps = (List<Integer>) localData.get(DATA_FINALLYJUMPS);
        for (int f : finallyJumps) {
            if (next.start == f) {
                return null;
            }
        }
        return next;
    }

    @Override
    protected GraphTargetItem checkLoop(LoopItem loopItem, List<Object> localData, List<Loop> loops) {
        if (loopItem instanceof WhileItem) {
            WhileItem w = (WhileItem) loopItem;

            if ((!w.expression.isEmpty()) && (w.expression.get(w.expression.size() - 1) instanceof HasNextTreeItem)) {
                if (((HasNextTreeItem) w.expression.get(w.expression.size() - 1)).collection != null) {
                    if (((HasNextTreeItem) w.expression.get(w.expression.size() - 1)).collection.getNotCoerced().getThroughRegister() instanceof FilteredCheckTreeItem) {
                        //GraphTargetItem gti = ((HasNextTreeItem) ((HasNextTreeItem) w.expression.get(w.expression.size() - 1))).collection.getNotCoerced().getThroughRegister();
                        if (w.commands.size() >= 3) { //((w.commands.size() == 3) || (w.commands.size() == 4)) {
                            int pos = 0;
                            while (w.commands.get(pos) instanceof SetLocalTreeItem) {
                                pos++;
                            }
                            GraphTargetItem ft = w.commands.get(pos);
                            if (ft instanceof WithTreeItem) {
                                ft = w.commands.get(pos + 1);
                                if (ft instanceof IfItem) {
                                    IfItem ift = (IfItem) ft;
                                    if (ift.onTrue.size() > 0) {
                                        ft = ift.onTrue.get(0);
                                        if (ft instanceof SetPropertyTreeItem) {
                                            SetPropertyTreeItem spt = (SetPropertyTreeItem) ft;
                                            if (spt.object instanceof LocalRegTreeItem) {
                                                int regIndex = ((LocalRegTreeItem) spt.object).regIndex;
                                                HasNextTreeItem iti = (HasNextTreeItem) w.expression.get(w.expression.size() - 1);
                                                @SuppressWarnings("unchecked")
                                                HashMap<Integer, GraphTargetItem> localRegs = (HashMap<Integer, GraphTargetItem>) localData.get(DATA_LOCALREGS);
                                                localRegs.put(regIndex, new FilterTreeItem(null, iti.collection.getThroughRegister(), ift.expression));
                                                return null;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (!w.commands.isEmpty()) {
                        if (w.commands.get(0) instanceof SetTypeTreeItem) {
                            SetTypeTreeItem sti = (SetTypeTreeItem) w.commands.remove(0);
                            GraphTargetItem gti = sti.getValue().getNotCoerced();
                            if (gti instanceof NextValueTreeItem) {
                                return new ForEachInTreeItem(w.src, w.loop, new InTreeItem(null, sti.getObject(), ((HasNextTreeItem) w.expression.get(w.expression.size() - 1)).collection), w.commands);
                            } else if (gti instanceof NextNameTreeItem) {
                                return new ForInTreeItem(w.src, w.loop, new InTreeItem(null, sti.getObject(), ((HasNextTreeItem) w.expression.get(w.expression.size() - 1)).collection), w.commands);
                            }
                        }
                    }
                }
            }
        }
        return loopItem;
    }

    @Override
    protected void finalProcess(List<GraphTargetItem> list, int level) {
        if (level == 0) {
            if (!list.isEmpty()) {
                if (list.get(list.size() - 1) instanceof ReturnVoidTreeItem) {
                    list.remove(list.size() - 1);
                }
            }
        }

        /*for (int i = 0; i < list.size(); i++) {

         if (list.get(i) instanceof WhileItem) {
         WhileItem w = (WhileItem) list.get(i);

         }
         }*/


        List<GraphTargetItem> ret = code.clearTemporaryRegisters(list);
        if (ret != list) {
            list.clear();
            list.addAll(ret);
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof SetTypeTreeItem) {
                if (((SetTypeTreeItem) list.get(i)).getValue() instanceof ExceptionTreeItem) {
                    list.remove(i);
                    i--;
                    continue;
                }
            }
            if (list.get(i) instanceof IfItem) {
                IfItem ifi = (IfItem) list.get(i);
                if (((ifi.expression instanceof HasNextTreeItem)
                        || ((ifi.expression instanceof NotItem)
                        && (((NotItem) ifi.expression).getOriginal() instanceof HasNextTreeItem)))) {
                    HasNextTreeItem hnt = null;
                    List<GraphTargetItem> body = new ArrayList<>();
                    List<GraphTargetItem> nextbody = new ArrayList<>();
                    if (ifi.expression instanceof NotItem) {
                        hnt = (HasNextTreeItem) ((NotItem) ifi.expression).getOriginal();
                        body.addAll(ifi.onFalse);
                        for (int j = i + 1; j < list.size();) {
                            body.add(list.remove(i + 1));
                        }
                        nextbody = ifi.onTrue;
                    } else {
                        hnt = (HasNextTreeItem) ifi.expression;
                        body = ifi.onTrue;
                        nextbody = ifi.onFalse;
                    }
                    if (!body.isEmpty()) {
                        if (body.get(0) instanceof SetTypeTreeItem) {
                            SetTypeTreeItem sti = (SetTypeTreeItem) body.remove(0);
                            GraphTargetItem gti = sti.getValue().getNotCoerced();
                            GraphTargetItem repl = null;

                            if (gti instanceof NextValueTreeItem) {
                                repl = new ForEachInTreeItem(ifi.src, new Loop(0, null, null), new InTreeItem(null, sti.getObject(), hnt.collection), body);
                            } else if (gti instanceof NextNameTreeItem) {
                                repl = new ForInTreeItem(ifi.src, new Loop(0, null, null), new InTreeItem(null, sti.getObject(), hnt.collection), body);
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
    }

    @Override
    protected boolean isEmpty(List<GraphTargetItem> output) {
        if (super.isEmpty(output)) {
            return true;
        }
        for (GraphTargetItem i : output) {
            if (i instanceof SetLocalTreeItem) {
                if (code.isKilled(((SetLocalTreeItem) i).regIndex, 0, code.code.size() - 1)) {
                    continue;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public List<Object> prepareBranchLocalData(List<Object> localData) {
        List<Object> ret = new ArrayList<>();
        ret.addAll(localData);
        @SuppressWarnings("unchecked")
        Stack<GraphTargetItem> scopeStack = (Stack<GraphTargetItem>) ret.get(DATA_SCOPESTACK);
        Stack<GraphTargetItem> copyScopeStack = new Stack<>();
        copyScopeStack.addAll(scopeStack);
        ret.set(DATA_SCOPESTACK, copyScopeStack);
        return ret;
    }
}

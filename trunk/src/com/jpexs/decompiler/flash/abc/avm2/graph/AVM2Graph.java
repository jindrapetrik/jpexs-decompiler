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
import com.jpexs.decompiler.flash.abc.avm2.treemodel.HasNextTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.InTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NextNameTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NextValueTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.NullTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ReturnValueTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.ReturnVoidTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.SetLocalTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.SetTypeTreeItem;
import com.jpexs.decompiler.flash.abc.avm2.treemodel.clauses.ExceptionTreeItem;
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
import com.jpexs.decompiler.flash.graph.Loop;
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
        List<GraphPart> allParts = new ArrayList<GraphPart>();
        for (GraphPart head : g.heads) {
            populateParts(head, allParts);
        }
        List localData = new ArrayList();
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
    /*
     public GraphPart getNextNoJump(GraphPart part) {
     while (code.code.get(part.start).definition instanceof JumpIns) {
     part = part.getSubParts().get(0).nextParts.get(0);
     }
     return part;
     }

     public static List<TreeItem> translateViaGraph(String path, AVM2Code code, ABC abc, MethodBody body) {
     AVM2Graph g = new AVM2Graph(code, abc, body);
     List<GraphPart> allParts = new ArrayList<GraphPart>();
     for (GraphPart head : g.heads) {
     populateParts(head, allParts);
     }
     return g.printGraph(path, new Stack<TreeItem>(), new Stack<TreeItem>(), allParts, new ArrayList<ABCException>(), new ArrayList<Integer>(), 0, null, g.heads.get(0), null, new ArrayList<Loop>(), new HashMap<Integer, TreeItem>(), body, new ArrayList<Integer>());
     }

     private List<GraphPart> getLoopsContinues(List<Loop> loops) {
     List<GraphPart> ret = new ArrayList<GraphPart>();
     for (Loop l : loops) {
     if (l.loopContinue != null) {
     ret.add(l.loopContinue);
     }
     }
     return ret;
     }

     private TreeItem checkLoop(GraphPart part, GraphPart stopPart, List<Loop> loops) {
     if (part == stopPart) {
     return null;
     }
     for (Loop l : loops) {
     if (l.loopContinue == part) {
     return (new ContinueTreeItem(null, l.id));
     }
     if (l.loopBreak == part) {
     return (new BreakTreeItem(null, l.id));
     }
     }
     return null;
     }
     private boolean doDecompile = true;

     private void checkContinueAtTheEnd(List<TreeItem> commands, Loop loop) {
     if (!commands.isEmpty()) {
     if (commands.get(commands.size() - 1) instanceof ContinueTreeItem) {
     if (((ContinueTreeItem) commands.get(commands.size() - 1)).loopPos == loop.id) {
     commands.remove(commands.size() - 1);
     }
     }
     }
     }

     private List<TreeItem> printGraph(String methodPath, Stack<TreeItem> stack, Stack<TreeItem> scopeStack, List<GraphPart> allParts, List<ABCException> parsedExceptions, List<Integer> finallyJumps, int level, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, HashMap<Integer, TreeItem> localRegs, MethodBody body, List<Integer> ignoredSwitches) {
     List<TreeItem> ret = new ArrayList<TreeItem>();
     boolean debugMode = false;

     try {
     if (!doDecompile) {
     ret.add(new CommentTreeItem(null, "not decompiled"));
     return ret;
     }

     if (debugMode) {
     System.err.println("PART " + part);
     }

     if (part == stopPart) {
     return ret;
     }
     if (part.ignored) {
     return ret;
     }
     List<String> fqn = new ArrayList<String>();
     HashMap<Integer, String> lrn = new HashMap<Integer, String>();
     List<TreeItem> output = new ArrayList<TreeItem>();
     boolean isSwitch = false;
     try {
     code.initToSource();
     List<GraphPart> parts = new ArrayList<GraphPart>();
     if (part instanceof GraphPartMulti) {
     parts = ((GraphPartMulti) part).parts;
     } else {
     parts.add(part);
     }
     boolean isIf = false;
     int end = part.end;
     for (GraphPart p : parts) {
     end = p.end;
     int start = p.start;
     isIf = false;
     if (code.code.get(end).definition instanceof JumpIns) {
     end--;
     } else if (code.code.get(end).definition instanceof IfTypeIns) {
     end--;
     isIf = true;
     } else if (code.code.get(end).definition instanceof LookupSwitchIns) {
     isSwitch = true;
     end--;
     }
     ConvertOutput co = code.toSourceOutput(false, false, 0, localRegs, stack, scopeStack, abc, abc.constants, abc.method_info, body, start, end, lrn, fqn, new boolean[code.code.size()]);
     output.addAll(co.output);

     }
     if (isIf) {
     AVM2Instruction ins = code.code.get(end + 1);
     if ((stack.size() >= 2) && (ins.definition instanceof IfFalseIns) && (stack.get(stack.size() - 1) == stack.get(stack.size() - 2))) {
     ret.addAll(output);

     GraphPart sp0 = getNextNoJump(part.nextParts.get(0));
     GraphPart sp1 = getNextNoJump(part.nextParts.get(1));
     boolean reversed = false;
     List<GraphPart> loopContinues = getLoopsContinues(loops);
     loopContinues.add(part);
     if (sp1.leadsTo(sp0, loopContinues)) {
     } else if (sp0.leadsTo(sp1, loopContinues)) {
     reversed = true;
     }
     GraphPart next = reversed ? sp0 : sp1;
     TreeItem ti;
     if ((ti = checkLoop(next, stopPart, loops)) != null) {
     ret.add(ti);
     } else {
     printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, next, reversed ? sp1 : sp0, loops, localRegs, body, ignoredSwitches);
     TreeItem second = stack.pop();
     TreeItem first = stack.pop();
     if (!reversed) {
     AndTreeItem a = new AndTreeItem(ins, first, second);
     stack.push(a);
     a.firstPart = part;
     if (second instanceof AndTreeItem) {
     a.firstPart = ((AndTreeItem) second).firstPart;
     }
     if (second instanceof OrTreeItem) {
     a.firstPart = ((AndTreeItem) second).firstPart;
     }
     } else {
     OrTreeItem o = new OrTreeItem(ins, first, second);
     stack.push(o);
     o.firstPart = part;
     if (second instanceof OrTreeItem) {
     o.firstPart = ((OrTreeItem) second).firstPart;
     }
     if (second instanceof OrTreeItem) {
     o.firstPart = ((OrTreeItem) second).firstPart;
     }
     }
     next = reversed ? sp1 : sp0;
     if ((ti = checkLoop(next, stopPart, loops)) != null) {
     ret.add(ti);
     } else {
     ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, next, stopPart, loops, localRegs, body, ignoredSwitches));
     }
     }
     return ret;
     } else if ((stack.size() >= 2) && (ins.definition instanceof IfTrueIns) && (stack.get(stack.size() - 1) == stack.get(stack.size() - 2))) {
     ret.addAll(output);
     GraphPart sp0 = getNextNoJump(part.nextParts.get(0));
     GraphPart sp1 = getNextNoJump(part.nextParts.get(1));
     boolean reversed = false;
     List<GraphPart> loopContinues = getLoopsContinues(loops);
     loopContinues.add(part);
     if (sp1.leadsTo(sp0, loopContinues)) {
     } else if (sp0.leadsTo(sp1, loopContinues)) {
     reversed = true;
     }
     GraphPart next = reversed ? sp0 : sp1;
     TreeItem ti;
     if ((ti = checkLoop(next, stopPart, loops)) != null) {
     ret.add(ti);
     } else {
     printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, next, reversed ? sp1 : sp0, loops, localRegs, body, ignoredSwitches);
     TreeItem second = stack.pop();
     TreeItem first = stack.pop();
     if (reversed) {
     AndTreeItem a = new AndTreeItem(ins, first, second);
     stack.push(a);
     a.firstPart = part;
     if (second instanceof AndTreeItem) {
     a.firstPart = ((AndTreeItem) second).firstPart;
     }
     if (second instanceof OrTreeItem) {
     a.firstPart = ((AndTreeItem) second).firstPart;
     }
     } else {
     OrTreeItem o = new OrTreeItem(ins, first, second);
     stack.push(o);
     o.firstPart = part;
     if (second instanceof OrTreeItem) {
     o.firstPart = ((OrTreeItem) second).firstPart;
     }
     if (second instanceof OrTreeItem) {
     o.firstPart = ((OrTreeItem) second).firstPart;
     }
     }

     next = reversed ? sp1 : sp0;
     if ((ti = checkLoop(next, stopPart, loops)) != null) {
     ret.add(ti);
     } else {
     ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, next, stopPart, loops, localRegs, body, ignoredSwitches));
     }
     }

     return ret;
     } else if ((((ins.definition instanceof IfStrictNeIns)) && ((part.nextParts.get(1).getHeight() == 2) && (code.code.get(part.nextParts.get(1).start).definition instanceof PushByteIns) && (code.code.get(part.nextParts.get(1).nextParts.get(0).end).definition instanceof LookupSwitchIns)))
     || (((ins.definition instanceof IfStrictEqIns)) && ((part.nextParts.get(0).getHeight() == 2) && (code.code.get(part.nextParts.get(0).start).definition instanceof PushByteIns) && (code.code.get(part.nextParts.get(0).nextParts.get(0).end).definition instanceof LookupSwitchIns)))) {
     ret.addAll(output);
     boolean reversed = false;
     if (ins.definition instanceof IfStrictEqIns) {
     reversed = true;
     }
     TreeItem switchedObject = null;
     if (!output.isEmpty()) {
     if (output.get(output.size() - 1) instanceof SetLocalTreeItem) {
     switchedObject = ((SetLocalTreeItem) output.get(output.size() - 1)).value;
     }
     }
     if (switchedObject == null) {
     switchedObject = new NullTreeItem(null);
     }
     HashMap<Integer, TreeItem> caseValuesMap = new HashMap<Integer, TreeItem>();

     stack.pop();
     caseValuesMap.put(code.code.get(part.nextParts.get(reversed ? 0 : 1).start).operands[0], stack.pop());

     GraphPart switchLoc = part.nextParts.get(reversed ? 0 : 1).nextParts.get(0);


     while ((code.code.get(part.nextParts.get(reversed ? 1 : 0).end).definition instanceof IfStrictNeIns)
     || (code.code.get(part.nextParts.get(reversed ? 1 : 0).end).definition instanceof IfStrictEqIns)) {
     part = part.nextParts.get(reversed ? 1 : 0);
     List<GraphPart> ps = part.getSubParts();
     for (GraphPart p : ps) {
     code.toSourceOutput(false, false, 0, localRegs, stack, scopeStack, abc, abc.constants, abc.method_info, body, p.start, p.end - 1, lrn, fqn, new boolean[code.code.size()]);
     }
     stack.pop();
     if (code.code.get(part.end).definition instanceof IfStrictNeIns) {
     reversed = false;
     } else {
     reversed = true;
     }
     caseValuesMap.put(code.code.get(part.nextParts.get(reversed ? 0 : 1).start).operands[0], stack.pop());

     }
     boolean hasDefault = false;
     GraphPart dp = part.nextParts.get(reversed ? 1 : 0);
     while (code.code.get(dp.start).definition instanceof JumpIns) {
     if (dp instanceof GraphPartMulti) {
     dp = ((GraphPartMulti) dp).parts.get(0);
     }
     dp = dp.nextParts.get(0);
     }
     if (code.code.get(dp.start).definition instanceof PushByteIns) {
     hasDefault = true;
     }
     List<TreeItem> caseValues = new ArrayList<TreeItem>();
     for (int i = 0; i < switchLoc.nextParts.size() - 1; i++) {
     if (caseValuesMap.containsKey(i)) {
     caseValues.add(caseValuesMap.get(i));
     } else {
     continue;
     }
     }

     List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();
     GraphPart next = null;

     List<GraphPart> loopContinues = getLoopsContinues(loops);

     next = switchLoc.getNextPartPath(loopContinues);
     if (next == null) {
     next = switchLoc.getNextSuperPartPath(loopContinues);
     }

     TreeItem ti = checkLoop(next, stopPart, loops);
     Loop currentLoop = new Loop(null, next);
     loops.add(currentLoop);
     //switchLoc.getNextPartPath(new ArrayList<GraphPart>());
     List<Integer> valuesMapping = new ArrayList<Integer>();
     List<GraphPart> caseBodies = new ArrayList<GraphPart>();
     for (int i = 0; i < caseValues.size(); i++) {
     GraphPart cur = switchLoc.nextParts.get(1 + i);
     if (!caseBodies.contains(cur)) {
     caseBodies.add(cur);
     }
     valuesMapping.add(caseBodies.indexOf(cur));
     }

     List<TreeItem> defaultCommands = new ArrayList<TreeItem>();
     GraphPart defaultPart = null;
     if (hasDefault) {
     defaultPart = switchLoc.nextParts.get(switchLoc.nextParts.size() - 1);
     defaultCommands = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, switchLoc, defaultPart, next, loops, localRegs, body, ignoredSwitches);
     }

     List<GraphPart> ignored = new ArrayList<GraphPart>();
     for (Loop l : loops) {
     ignored.add(l.loopContinue);
     }

     for (int i = 0; i < caseBodies.size(); i++) {
     List<TreeItem> cc = new ArrayList<TreeItem>();
     GraphPart nextCase = null;
     nextCase = next;
     if (next != null) {
     if (i < caseBodies.size() - 1) {
     if (!caseBodies.get(i).leadsTo(caseBodies.get(i + 1), ignored)) {
     cc.add(new BreakTreeItem(null, next.start));
     } else {
     nextCase = caseBodies.get(i + 1);
     }
     } else if (hasDefault) {
     if (!caseBodies.get(i).leadsTo(defaultPart, ignored)) {
     cc.add(new BreakTreeItem(null, next.start));
     } else {
     nextCase = defaultPart;
     }
     }
     }
     cc.addAll(0, printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, switchLoc, caseBodies.get(i), nextCase, loops, localRegs, body, ignoredSwitches));
     caseCommands.add(cc);
     }

     SwitchTreeItem sti = new SwitchTreeItem(null, next == null ? -1 : next.start, switchedObject, caseValues, caseCommands, defaultCommands, valuesMapping);
     ret.add(sti);
     loops.remove(currentLoop);
     if (next != null) {
     if (ti != null) {
     ret.add(ti);
     } else {
     ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, null, next, stopPart, loops, localRegs, body, ignoredSwitches));
     }
     }
     return ret;
     } else {
     try {
     ins.definition.translate(false, 0, new HashMap<Integer, TreeItem>(), stack, new Stack<TreeItem>(), abc.constants, ins, abc.method_info, output, body, abc, lrn, fqn);
     } catch (Exception ex) {
     System.err.println("ip:" + (end + 1));
     ex.printStackTrace();
     }
     }
     //((IfTypeIns)ins.definition).translateInverted(new HashMap<Integer,TreeItem>(), co.stack, ins);
     }
     } catch (ConvertException ex) {
     Logger.getLogger(AVM2Graph.class.getName()).log(Level.SEVERE, null, ex);
     }

     int ip = part.start;
     int addr = code.fixAddrAfterDebugLine(code.pos2adr(part.start));
     int maxend = -1;
     List<ABCException> catchedExceptions = new ArrayList<ABCException>();
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
     parsedExceptions.addAll(catchedExceptions);
     int endpos = code.adr2pos(code.fixAddrAfterDebugLine(catchedExceptions.get(0).end));
     int endposStartBlock = code.adr2pos(catchedExceptions.get(0).end);


     List<List<TreeItem>> catchedCommands = new ArrayList<List<TreeItem>>();
     if (code.code.get(endpos).definition instanceof JumpIns) {
     int afterCatchAddr = code.pos2adr(endpos + 1) + code.code.get(endpos).operands[0];
     int afterCatchPos = code.adr2pos(afterCatchAddr);
     Collections.sort(catchedExceptions, new Comparator<ABCException>() {
     public int compare(ABCException o1, ABCException o2) {
     try {
     return code.fixAddrAfterDebugLine(o1.target) - code.fixAddrAfterDebugLine(o2.target);
     } catch (ConvertException ex) {
     return 0;
     }
     }
     });


     List<TreeItem> finallyCommands = new ArrayList<TreeItem>();
     int returnPos = afterCatchPos;
     for (int e = 0; e < body.exceptions.length; e++) {
     if (body.exceptions[e].isFinally()) {
     if (addr == code.fixAddrAfterDebugLine(body.exceptions[e].start)) {
     if (afterCatchPos + 1 == code.adr2pos(code.fixAddrAfterDebugLine(body.exceptions[e].end))) {
     AVM2Instruction jmpIns = code.code.get(code.adr2pos(code.fixAddrAfterDebugLine(body.exceptions[e].end)));
     if (jmpIns.definition instanceof JumpIns) {
     int finStart = code.adr2pos(code.fixAddrAfterDebugLine(body.exceptions[e].end) + jmpIns.getBytes().length + jmpIns.operands[0]);
     finallyJumps.add(finStart);
     for (int f = finStart; f < code.code.size(); f++) {
     if (code.code.get(f).definition instanceof LookupSwitchIns) {
     AVM2Instruction swins = code.code.get(f);
     if (swins.operands.length >= 3) {
     if (swins.operands[0] == swins.getBytes().length) {
     if (code.adr2pos(code.pos2adr(f) + swins.operands[2]) < finStart) {
     GraphPart fpart = null;
     for (GraphPart p : allParts) {
     if (p.start == finStart) {
     fpart = p;
     break;
     }
     }
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
     finallyCommands = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, fpart, fepart, loops, localRegs, body, ignoredSwitches);
     returnPos = f + 1;
     break;
     }
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

     for (int e = 0; e < catchedExceptions.size(); e++) {
     int eendpos;
     if (e < catchedExceptions.size() - 1) {
     eendpos = code.adr2pos(code.fixAddrAfterDebugLine(catchedExceptions.get(e + 1).target)) - 2;
     } else {
     eendpos = afterCatchPos - 1;
     }
     Stack<TreeItem> substack = new Stack<TreeItem>();
     substack.add(new ExceptionTreeItem(catchedExceptions.get(e)));

     GraphPart npart = null;
     int findpos = code.adr2pos(code.fixAddrAfterDebugLine(catchedExceptions.get(e).target));
     for (GraphPart p : allParts) {
     if (p.start == findpos) {
     npart = p;
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
     catchedCommands.add(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, npart, nepart, loops, localRegs, body, ignoredSwitches));
     }

     GraphPart nepart = null;

     for (GraphPart p : allParts) {
     if (p.start == endposStartBlock) {
     nepart = p;
     break;
     }
     }
     List<TreeItem> tryCommands = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, parent, part, nepart, loops, localRegs, body, ignoredSwitches);

     output.clear();
     output.add(new TryTreeItem(tryCommands, catchedExceptions, catchedCommands, finallyCommands));
     ip = returnPos;
     addr = code.pos2adr(ip);
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
     ret.addAll(output);
     TreeItem lop = checkLoop(part, stopPart, loops);
     if (lop == null) {
     ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, null, part, stopPart, loops, localRegs, body, ignoredSwitches));
     } else {
     ret.add(lop);
     }
     return ret;
     }

     List<GraphPart> loopContinues = new ArrayList<GraphPart>();
     for (Loop l : loops) {
     if (l.loopContinue != null) {
     loopContinues.add(l.loopContinue);
     }
     }
     boolean loop = false;
     boolean reversed = false;
     boolean whileTrue = false;
     Loop whileTrueLoop = null;
     if ((!part.nextParts.isEmpty()) && part.nextParts.get(0).leadsTo(part, loopContinues)) {
     if ((part.nextParts.size() > 1) && part.nextParts.get(1).leadsTo(part, loopContinues)) {
     if (output.isEmpty()) {
     whileTrueLoop = new Loop(part, null);
     loops.add(whileTrueLoop);
     whileTrue = true;
     } else {
     loop = true;//doWhile
     }

     } else {
     loop = true;
     }
     } else if ((part.nextParts.size() > 1) && part.nextParts.get(1).leadsTo(part, loopContinues)) {
     loop = true;
     reversed = true;
     }
     if (((part.nextParts.size() == 2) || ((part.nextParts.size() == 1) && loop)) && (!isSwitch)) {

     boolean doWhile = loop;
     if (loop && output.isEmpty()) {
     doWhile = false;
     }
     Loop currentLoop = new Loop(part, null);
     if (loop) {
     loops.add(currentLoop);
     }

     loopContinues = new ArrayList<GraphPart>();
     for (Loop l : loops) {
     if (l.loopContinue != null) {
     loopContinues.add(l.loopContinue);
     }
     }

     if ((part.nextParts.size() > 1) && (!doWhile)) {
     currentLoop.loopBreak = part.nextParts.get(reversed ? 0 : 1);
     }

     TreeItem expr = null;
     if ((code.code.get(part.end).definition instanceof JumpIns) || (!(code.code.get(part.end).definition instanceof IfTypeIns))) {
     expr = new BooleanTreeItem(null, true);
     } else {
     if (!stack.isEmpty()) {
     expr = stack.pop();
     }
     }
     if (loop) {
     if (expr instanceof AndTreeItem) {
     currentLoop.loopContinue = ((AndTreeItem) expr).firstPart;
     }
     if (expr instanceof OrTreeItem) {
     currentLoop.loopContinue = ((OrTreeItem) expr).firstPart;
     }
     }

     if (doWhile) {
     //ret.add(new DoWhileTreeItem(null, currentLoop.id, part.start, output, expr));
     } else {
     ret.addAll(output);
     }
     GraphPart loopBodyStart = null;
     GraphPart next = part.getNextPartPath(loopContinues);
     if (reversed && (expr instanceof LogicalOp)) {
     expr = ((LogicalOp) expr).invert();
     }
     List<TreeItem> retx = ret;
     if ((!loop) || (doWhile && (part.nextParts.size() > 1))) {
     if (doWhile) {
     retx = output;

     }
     int stackSizeBefore = stack.size();
     Stack<TreeItem> trueStack = (Stack<TreeItem>) stack.clone();
     Stack<TreeItem> falseStack = (Stack<TreeItem>) stack.clone();
     TreeItem lopTrue = checkLoop(part.nextParts.get(1), stopPart, loops);
     TreeItem lopFalse = null;
     if (next != part.nextParts.get(0)) {
     lopFalse = checkLoop(part.nextParts.get(0), stopPart, loops);
     }
     List<TreeItem> onTrue = new ArrayList<TreeItem>();
     if (lopTrue != null) {
     onTrue.add(lopTrue);
     } else {
     if (debugMode) {
     System.err.println("ONTRUE: (inside " + part + ")");
     }
     onTrue = printGraph(methodPath, trueStack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, part.nextParts.get(1), next == null ? stopPart : next, loops, localRegs, body, ignoredSwitches);
     if (debugMode) {
     System.err.println("/ONTRUE (inside " + part + ")");
     }
     }
     List<TreeItem> onFalse = new ArrayList<TreeItem>();
     if (lopFalse != null) {
     onFalse.add(lopFalse);
     } else {
     if (debugMode) {
     System.err.println("ONFALSE: (inside " + part + ")");
     }
     onFalse = (((next == part.nextParts.get(0)) || (part.nextParts.get(0).path.equals(part.path) || part.nextParts.get(0).path.length() < part.path.length())) ? new ArrayList<TreeItem>() : printGraph(methodPath, falseStack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, part.nextParts.get(0), next == null ? stopPart : next, loops, localRegs, body, ignoredSwitches));
     if (debugMode) {
     System.err.println("/ONFALSE (inside " + part + ")");
     }
     }

     if (onTrue.isEmpty() && onFalse.isEmpty() && (trueStack.size() > stackSizeBefore) && (falseStack.size() > stackSizeBefore)) {
     stack.push(new TernarOpTreeItem(null, expr, trueStack.pop(), falseStack.pop()));
     } else {
     List<TreeItem> retw = retx;
     if (whileTrue) {
     retw = new ArrayList<TreeItem>();
     retw.add(new IfTreeItem(null, expr, onTrue, onFalse));
     retx.add(new WhileTreeItem(null, whileTrueLoop.id, whileTrueLoop.loopContinue.start, new BooleanTreeItem(null, true), retw));
     } else {
     retx.add(new IfTreeItem(null, expr, onTrue, onFalse));
     }

     //Same continues in onTrue and onFalse gets continue on parent level
     if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
     if (onTrue.get(onTrue.size() - 1) instanceof ContinueTreeItem) {
     if (onFalse.get(onFalse.size() - 1) instanceof ContinueTreeItem) {
     if (((ContinueTreeItem) onTrue.get(onTrue.size() - 1)).loopPos == ((ContinueTreeItem) onFalse.get(onFalse.size() - 1)).loopPos) {
     onTrue.remove(onTrue.size() - 1);
     retw.add(onFalse.remove(onFalse.size() - 1));
     }
     }
     }
     }

     if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
     if (onTrue.get(onTrue.size() - 1) instanceof ReturnValueTreeItem || onTrue.get(onTrue.size() - 1) instanceof ReturnVoidTreeItem) {
     if (onFalse.get(onFalse.size() - 1) instanceof ContinueTreeItem) {
     retw.add(onFalse.remove(onFalse.size() - 1));
     }
     }
     }

     if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
     if (onFalse.get(onFalse.size() - 1) instanceof ReturnValueTreeItem || onFalse.get(onFalse.size() - 1) instanceof ReturnVoidTreeItem) {
     if (onTrue.get(onTrue.size() - 1) instanceof ContinueTreeItem) {
     retw.add(onTrue.remove(onTrue.size() - 1));
     }
     }
     }
     if (whileTrue) {
     checkContinueAtTheEnd(retw, whileTrueLoop);
     }
     }
     if (doWhile) {
     loopBodyStart = next;
     }
     }
     if (loop) { // && (!doWhile)) {
     List<TreeItem> loopBody = null;
     List<TreeItem> finalCommands = null;
     GraphPart finalPart = null;
     boolean isFor = false;
     try {
     loopBody = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, loopBodyStart != null ? loopBodyStart : part.nextParts.get(reversed ? 1 : 0), stopPart, loops, localRegs, body, ignoredSwitches);
     checkContinueAtTheEnd(loopBody, currentLoop);
     } catch (ForException fex) {
     loopBody = fex.output;
     finalCommands = fex.finalOutput;
     if (!finalCommands.isEmpty()) {
     finalCommands.remove(finalCommands.size() - 1); //remove continue
     }
     finalPart = fex.continuePart;
     isFor = true;
     for (Object o : finalPart.forContinues) {
     if (o instanceof ContinueTreeItem) {
     ((ContinueTreeItem) o).loopPos = currentLoop.id;
     }
     }
     }
     if (isFor) {
     ret.add(new ForTreeItem(null, currentLoop.id, finalPart.start, new ArrayList<TreeItem>(), expr, finalCommands, loopBody));
     } else if ((expr instanceof HasNextTreeItem) && ((HasNextTreeItem) expr).collection.getNotCoerced().getThroughRegister() instanceof FilteredCheckTreeItem) {
     TreeItem gti = ((HasNextTreeItem) expr).collection.getNotCoerced().getThroughRegister();
     boolean found = false;
     if ((loopBody.size() == 3) || (loopBody.size() == 4)) {
     TreeItem ft = loopBody.get(0);
     if (ft instanceof WithTreeItem) {
     ft = loopBody.get(1);
     if (ft instanceof IfTreeItem) {
     IfTreeItem ift = (IfTreeItem) ft;
     if (ift.onTrue.size() > 0) {
     ft = ift.onTrue.get(0);
     if (ft instanceof SetPropertyTreeItem) {
     SetPropertyTreeItem spt = (SetPropertyTreeItem) ft;
     if (spt.object instanceof LocalRegTreeItem) {
     int regIndex = ((LocalRegTreeItem) spt.object).regIndex;
     HasNextTreeItem iti = (HasNextTreeItem) expr;
     localRegs.put(regIndex, new FilterTreeItem(null, iti.collection.getThroughRegister(), ift.expression));
     }
     }
     }
     }
     }
     }
     } else if ((expr instanceof HasNextTreeItem) && (!loopBody.isEmpty()) && (loopBody.get(0) instanceof SetTypeTreeItem) && (((SetTypeTreeItem) loopBody.get(0)).getValue().getNotCoerced() instanceof NextValueTreeItem)) {
     TreeItem obj = ((SetTypeTreeItem) loopBody.get(0)).getObject();
     loopBody.remove(0);
     ret.add(new ForEachInTreeItem(null, currentLoop.id, part.start, new InTreeItem(expr.instruction, obj, ((HasNextTreeItem) expr).collection), loopBody));
     } else if ((expr instanceof HasNextTreeItem) && (!loopBody.isEmpty()) && (loopBody.get(0) instanceof SetTypeTreeItem) && (((SetTypeTreeItem) loopBody.get(0)).getValue().getNotCoerced() instanceof NextNameTreeItem)) {
     TreeItem obj = ((SetTypeTreeItem) loopBody.get(0)).getObject();
     loopBody.remove(0);
     ret.add(new ForInTreeItem(null, currentLoop.id, part.start, new InTreeItem(expr.instruction, obj, ((HasNextTreeItem) expr).collection), loopBody));
     } else {
     if (doWhile) {
     if (stack.isEmpty()) {
     expr = new BooleanTreeItem(null, true);
     } else {
     expr = stack.pop();
     }
     loopBody.addAll(0, output);
     checkContinueAtTheEnd(loopBody, currentLoop);

     List<TreeItem> addIf = new ArrayList<TreeItem>();
     if ((!loopBody.isEmpty()) && (loopBody.get(loopBody.size() - 1) instanceof IfTreeItem)) {
     IfTreeItem ift = (IfTreeItem) loopBody.get(loopBody.size() - 1);
     if (ift.onFalse.isEmpty()) {
     expr = ift.expression;
     addIf = ift.onTrue;
     loopBody.remove(loopBody.size() - 1);
     }
     }
     ret.add(new DoWhileTreeItem(null, currentLoop.id, part.start, loopBody, expr));
     ret.addAll(addIf);

     } else {
     ret.add(new WhileTreeItem(null, currentLoop.id, part.start, expr, loopBody));
     }
     }
     }
     if ((!doWhile) && (!whileTrue) && loop && (part.nextParts.size() > 1)) {
     loops.remove(currentLoop); //remove loop so no break shows up
     //ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level, part, part.nextParts.get(reversed ? 0 : 1), stopPart, loops, localRegs, body, ignoredSwitches));
     next = part.nextParts.get(reversed ? 0 : 1);
     }
     if (doWhile) {
     next = null;
     }
     if (next != null) {
     boolean finallyJump = false;
     for (int f : finallyJumps) {
     if (next.start == f) {
     finallyJump = true;
     break;
     }
     }
     if (!finallyJump) {
     TreeItem ti = checkLoop(next, stopPart, loops);
     if (ti != null) {
     ret.add(ti);
     } else {
     if (debugMode) {
     System.err.println("NEXT: (inside " + part + ")");
     }
     ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level, part, next, stopPart, loops, localRegs, body, ignoredSwitches));
     if (debugMode) {
     System.err.println("/NEXT: (inside " + part + ")");
     }
     }
     }
     }
     } else {
     ret.addAll(output);
     }
     onepart:
     if (part.nextParts.size() == 1 && (!loop)) {
     if (part.end - part.start > 4) {
     if (code.code.get(part.end).definition instanceof PopIns) {
     if (code.code.get(part.end - 1).definition instanceof LabelIns) {
     if (code.code.get(part.end - 2).definition instanceof PushByteIns) {

     //if (code.code.get(part.end - 3).definition instanceof SetLocalTypeIns) {
     if (part.nextParts.size() == 1) {
     GraphPart sec = part.nextParts.get(0);

     if (code.code.get(sec.end).definition instanceof ReturnValueIns) {
     if (sec.end - sec.start >= 3) {
     if (code.code.get(sec.end - 1).definition instanceof KillIns) {
     if (code.code.get(sec.end - 2).definition instanceof GetLocalTypeIns) {
     if (!output.isEmpty()) {
     if (output.get(output.size() - 1) instanceof SetLocalTreeItem) {
     sec.ignored = true;
     ret.add(new ReturnValueTreeItem(code.code.get(sec.end), ((SetLocalTreeItem) output.get(output.size() - 1)).value));
     break onepart;
     }
     }
     }
     }
     }

     } else if (code.code.get(sec.end).definition instanceof ReturnVoidIns) {
     ret.add(new ReturnVoidTreeItem(code.code.get(sec.end)));
     break onepart;
     }
     //}
     }
     }
     }
     }
     }

     for (int f : finallyJumps) {
     if (part.nextParts.get(0).start == f) {
     if ((!output.isEmpty()) && (output.get(output.size() - 1) instanceof SetLocalTreeItem)) {
     ret.add(new ReturnValueTreeItem(null, ((SetLocalTreeItem) output.get(output.size() - 1)).value));
     } else {
     ret.add(new ReturnVoidTreeItem(null));
     }

     break onepart;
     }
     }

     GraphPart p = part.nextParts.get(0);
     TreeItem lop = checkLoop(p, stopPart, loops);
     if (lop == null) {
     if (p.path.length() == part.path.length()) {
     ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, p, stopPart, loops, localRegs, body, ignoredSwitches));
     } else {
     if ((p != stopPart) && (p.refs.size() > 1)) {
     ContinueTreeItem cti = new ContinueTreeItem(null, -1);
     //p.forContinues.add(cti);
     ret.add(new CommentTreeItem(null, "Unknown jump to part " + p));
     ret.add(cti);
     }
     }
     } else {
     ret.add(lop);
     }
     //}
     //ret += (strOfChars(level, TAB) + "continue;\r\n");
     //}
     }
     if (isSwitch && (!ignoredSwitches.contains(part.end))) {
     //ret.add(new CommentTreeItem(code.code.get(part.end), "Switch not supported"));
     TreeItem switchedObject = stack.pop();
     List<TreeItem> caseValues = new ArrayList<TreeItem>();
     List<Integer> valueMappings = new ArrayList<Integer>();
     List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();

     GraphPart next = part.getNextPartPath(loopContinues);
     int breakPos = -1;
     if (next != null) {
     breakPos = next.start;
     }
     List<TreeItem> defaultCommands = printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, part.nextParts.get(0), stopPart, loops, localRegs, body, ignoredSwitches);

     for (int i = 0; i < part.nextParts.size() - 1; i++) {
     caseValues.add(new IntegerValueTreeItem(null, (Long) (long) i));
     valueMappings.add(i);
     GraphPart nextCase = next;
     List<TreeItem> caseBody = new ArrayList<TreeItem>();
     if (i < part.nextParts.size() - 1 - 1) {
     if (!part.nextParts.get(1 + i).leadsTo(part.nextParts.get(1 + i + 1), new ArrayList<GraphPart>())) {
     caseBody.add(new BreakTreeItem(null, breakPos));
     } else {
     nextCase = part.nextParts.get(1 + i + 1);
     }
     } else if (!part.nextParts.get(1 + i).leadsTo(part.nextParts.get(0), new ArrayList<GraphPart>())) {
     caseBody.add(new BreakTreeItem(null, breakPos));
     } else {
     nextCase = part.nextParts.get(0);
     }
     caseBody.addAll(0, printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, part.nextParts.get(1 + i), nextCase, loops, localRegs, body, ignoredSwitches));
     caseCommands.add(caseBody);
     }

     SwitchTreeItem swt = new SwitchTreeItem(null, breakPos, switchedObject, caseValues, caseCommands, defaultCommands, valueMappings);
     ret.add(swt);

     if (next != null) {
     TreeItem lopNext = checkLoop(next, stopPart, loops);
     if (lopNext != null) {
     ret.add(lopNext);
     } else {
     ret.addAll(printGraph(methodPath, stack, scopeStack, allParts, parsedExceptions, finallyJumps, level + 1, part, next, stopPart, loops, localRegs, body, ignoredSwitches));
     }
     }

     }

     } catch (ForException fex) {
     ret.addAll(fex.output);
     fex.output = ret;
     throw fex;
     }
     code.clearTemporaryRegisters(ret);
     if (!part.forContinues.isEmpty()) {
     throw new ForException(new ArrayList<TreeItem>(), ret, part);
     }
     return ret;
     }

     private List<GraphPart> makeGraph(AVM2Code code, List<GraphPart> allBlocks, MethodBody body) {
     HashMap<Integer, List<Integer>> refs = code.visitCode(body);
     List<GraphPart> ret = new ArrayList<GraphPart>();
     boolean visited[] = new boolean[code.code.size()];
     ret.add(makeGraph(null, "0", code, 0, 0, allBlocks, refs, visited));
     for (ABCException ex : body.exceptions) {
     GraphPart e1 = new GraphPart(-1, -1);
     e1.path = "e";
     GraphPart e2 = new GraphPart(-1, -1);
     e2.path = "e";
     GraphPart e3 = new GraphPart(-1, -1);
     e3.path = "e";
     makeGraph(e1, "e", code, code.adr2pos(ex.start), code.adr2pos(ex.start), allBlocks, refs, visited);
     makeGraph(e2, "e", code, code.adr2pos(ex.end), code.adr2pos(ex.end), allBlocks, refs, visited);
     ret.add(makeGraph(e3, "e", code, code.adr2pos(ex.target), code.adr2pos(ex.target), allBlocks, refs, visited));
     }
     return ret;
     }

     private GraphPart makeGraph(GraphPart parent, String path, AVM2Code code, int startip, int lastIp, List<GraphPart> allBlocks, HashMap<Integer, List<Integer>> refs, boolean visited2[]) {

     int ip = startip;
     for (GraphPart p : allBlocks) {
     if (p.start == ip) {
     p.refs.add(parent);
     return p;
     }
     }
     GraphPart g;
     GraphPart ret = new GraphPart(ip, -1);
     ret.path = path;
     GraphPart part = ret;
     while (ip < code.code.size()) {
     if (visited2[ip] || ((ip != startip) && (refs.get(ip).size() > 1))) {
     part.end = lastIp;
     GraphPart found = null;
     for (GraphPart p : allBlocks) {
     if (p.start == ip) {
     found = p;
     break;
     }
     }

     allBlocks.add(part);

     if (found != null) {
     part.nextParts.add(found);
     found.refs.add(part);
     break;
     } else {
     GraphPart gp = new GraphPart(ip, -1);
     gp.path = path;
     part.nextParts.add(gp);
     gp.refs.add(part);
     part = gp;
     }
     }
     lastIp = ip;
     AVM2Instruction ins = code.code.get(ip);
     if ((ins.definition instanceof ThrowIns) || (ins.definition instanceof ReturnValueIns) || (ins.definition instanceof ReturnVoidIns)) {
     part.end = ip;
     allBlocks.add(part);
     break;
     }
     if (ins.definition instanceof LookupSwitchIns) {
     part.end = ip;
     allBlocks.add(part);
     try {
     part.nextParts.add(g = makeGraph(part, path + "0", code, code.adr2pos(code.pos2adr(ip) + ins.operands[0]), ip, allBlocks, refs, visited2));
     g.refs.add(part);
     for (int i = 2; i < ins.operands.length; i++) {
     part.nextParts.add(g = makeGraph(part, path + (i - 1), code, code.adr2pos(code.pos2adr(ip) + ins.operands[i]), ip, allBlocks, refs, visited2));
     g.refs.add(part);
     }
     break;
     } catch (ConvertException ex) {
     }
     }
     if (ins.definition instanceof JumpIns) {
     try {
     part.end = ip;
     allBlocks.add(part);
     ip = code.adr2pos(code.pos2adr(ip) + ins.getBytes().length + ins.operands[0]);
     part.nextParts.add(g = makeGraph(part, path, code, ip, lastIp, allBlocks, refs, visited2));
     g.refs.add(part);
     break;
     } catch (ConvertException ex) {
     Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
     }
     } else if (ins.definition instanceof IfTypeIns) {
     part.end = ip;
     allBlocks.add(part);
     try {
     part.nextParts.add(g = makeGraph(part, path + "0", code, code.adr2pos(code.pos2adr(ip) + ins.getBytes().length + ins.operands[0]), ip, allBlocks, refs, visited2));
     g.refs.add(part);
     part.nextParts.add(g = makeGraph(part, path + "1", code, ip + 1, ip, allBlocks, refs, visited2));
     g.refs.add(part);

     } catch (ConvertException ex) {
     Logger.getLogger(AVM2Code.class.getName()).log(Level.FINE, null, ex);
     }
     break;
     }
     ip++;
     };
     return ret;
     }*/

    @Override
    protected List<GraphTargetItem> check(GraphSource srcCode, List localData, List<GraphPart> allParts, Stack<GraphTargetItem> stack, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, List<GraphTargetItem> output, HashMap<Loop, List<GraphTargetItem>> forFinalCommands) {
        List<GraphTargetItem> ret = null;



        List<ABCException> parsedExceptions = (List<ABCException>) localData.get(DATA_PARSEDEXCEPTIONS);
        List<Integer> finallyJumps = (List<Integer>) localData.get(DATA_FINALLYJUMPS);
        List<Integer> ignoredSwitches = (List<Integer>) localData.get(DATA_IGNOREDSWITCHES);
        int ip = part.start;
        int addr = code.fixAddrAfterDebugLine(code.pos2adr(part.start));
        int maxend = -1;
        List<ABCException> catchedExceptions = new ArrayList<ABCException>();
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
            parsedExceptions.addAll(catchedExceptions);
            int endpos = code.adr2pos(code.fixAddrAfterDebugLine(catchedExceptions.get(0).end));
            int endposStartBlock = code.adr2pos(catchedExceptions.get(0).end);


            List<List<GraphTargetItem>> catchedCommands = new ArrayList<List<GraphTargetItem>>();
            if (code.code.get(endpos).definition instanceof JumpIns) {
                int afterCatchAddr = code.pos2adr(endpos + 1) + code.code.get(endpos).operands[0];
                int afterCatchPos = code.adr2pos(afterCatchAddr);
                Collections.sort(catchedExceptions, new Comparator<ABCException>() {
                    @Override
                    public int compare(ABCException o1, ABCException o2) {
                        return code.fixAddrAfterDebugLine(o1.target) - code.fixAddrAfterDebugLine(o2.target);
                    }
                });


                List<GraphTargetItem> finallyCommands = new ArrayList<GraphTargetItem>();
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
                                                        finallyCommands = printGraph(localData, stack, allParts, parent, fpart, fepart, loops, forFinalCommands);
                                                        returnPos = f + 1;
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (!switchFound) {
                                        finallyCommands = printGraph(localData, stack, allParts, parent, fpart, null, loops, forFinalCommands);
                                    }
                                    finallyJumps.add(finStart);
                                    break;
                                }
                            }
                        }
                    }
                }

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
                    List localData2 = new ArrayList();
                    localData2.addAll(localData);
                    localData2.set(DATA_SCOPESTACK, new Stack<GraphTargetItem>());
                    catchedCommands.add(printGraph(localData2, stack, allParts, parent, npart, nepart, loops, forFinalCommands));
                }

                GraphPart nepart = null;

                for (GraphPart p : allParts) {
                    if (p.start == endposStartBlock) {
                        nepart = p;
                        break;
                    }
                }
                List<GraphTargetItem> tryCommands = printGraph(localData, stack, allParts, parent, part, nepart, loops, forFinalCommands);

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
            ret = new ArrayList<GraphTargetItem>();
            ret.addAll(output);
            GraphTargetItem lop = checkLoop(part, stopPart, loops);
            if (lop == null) {
                ret.addAll(printGraph(localData, stack, allParts, null, part, stopPart, loops, forFinalCommands));
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
                                    ret = new ArrayList<GraphTargetItem>();
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
        if (code.code.get(part.end).definition instanceof LookupSwitchIns) {
            ret = new ArrayList<GraphTargetItem>();
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
            ret = new ArrayList<GraphTargetItem>();
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
            HashMap<Integer, GraphTargetItem> caseValuesMap = new HashMap<Integer, GraphTargetItem>();

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
            List<GraphTargetItem> caseValues = new ArrayList<GraphTargetItem>();
            for (int i = 0; i < switchLoc.nextParts.size() - 1; i++) {
                if (caseValuesMap.containsKey(i)) {
                    caseValues.add(caseValuesMap.get(i));
                } else {
                    continue;
                }
            }

            List<List<GraphTargetItem>> caseCommands = new ArrayList<List<GraphTargetItem>>();
            GraphPart next = null;

            List<GraphPart> loopContinues = getLoopsContinues(loops);

            next = switchLoc.getNextPartPath(loopContinues);
            if (next == null) {
                next = switchLoc.getNextSuperPartPath(loopContinues);
            }

            GraphTargetItem ti = checkLoop(next, stopPart, loops);
            Loop currentLoop = new Loop(loops.size(), null, next);
            loops.add(currentLoop);
            //switchLoc.getNextPartPath(new ArrayList<GraphPart>());
            List<Integer> valuesMapping = new ArrayList<Integer>();
            List<GraphPart> caseBodies = new ArrayList<GraphPart>();
            for (int i = 0; i < caseValues.size(); i++) {
                GraphPart cur = switchLoc.nextParts.get(1 + i);
                if (!caseBodies.contains(cur)) {
                    caseBodies.add(cur);
                }
                valuesMapping.add(caseBodies.indexOf(cur));
            }

            List<GraphTargetItem> defaultCommands = new ArrayList<GraphTargetItem>();
            GraphPart defaultPart = null;
            if (hasDefault) {
                defaultPart = switchLoc.nextParts.get(switchLoc.nextParts.size() - 1);
                defaultCommands = printGraph(localData, stack, allParts, switchLoc, defaultPart, next, loops, forFinalCommands);
            }

            List<GraphPart> ignored = new ArrayList<GraphPart>();
            for (Loop l : loops) {
                ignored.add(l.loopContinue);
            }

            for (int i = 0; i < caseBodies.size(); i++) {
                List<GraphTargetItem> cc = new ArrayList<GraphTargetItem>();
                GraphPart nextCase = null;
                nextCase = next;
                if (next != null) {
                    if (i < caseBodies.size() - 1) {
                        if (!caseBodies.get(i).leadsTo(srcCode, caseBodies.get(i + 1), ignored)) {
                            cc.add(new BreakItem(null, currentLoop.id));
                        } else {
                            nextCase = caseBodies.get(i + 1);
                        }
                    } else if (hasDefault) {
                        if (!caseBodies.get(i).leadsTo(srcCode, defaultPart, ignored)) {
                            cc.add(new BreakItem(null, currentLoop.id));
                        } else {
                            nextCase = defaultPart;
                        }
                    }
                }
                cc.addAll(0, printGraph(localData, stack, allParts, switchLoc, caseBodies.get(i), nextCase, loops, forFinalCommands));
                caseCommands.add(cc);
            }

            SwitchItem sti = new SwitchItem(null, currentLoop, switchedObject, caseValues, caseCommands, defaultCommands, valuesMapping);
            ret.add(sti);
            loops.remove(currentLoop);
            if (next != null) {
                if (ti != null) {
                    ret.add(ti);
                } else {
                    ret.addAll(printGraph(localData, stack, allParts, null, next, stopPart, loops, forFinalCommands));
                }
            }
        }
        return ret;
    }

    @Override
    protected GraphPart checkPart(List localData, GraphPart next) {
        List<Integer> finallyJumps = (List<Integer>) localData.get(DATA_FINALLYJUMPS);
        for (int f : finallyJumps) {
            if (next.start == f) {
                return null;
            }
        }
        return next;
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
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof WhileItem) {
                WhileItem w = (WhileItem) list.get(i);
                if ((!w.expression.isEmpty()) && (w.expression.get(w.expression.size() - 1) instanceof HasNextTreeItem)) {
                    if (!w.commands.isEmpty()) {
                        if (w.commands.get(0) instanceof SetTypeTreeItem) {
                            SetTypeTreeItem sti = (SetTypeTreeItem) w.commands.remove(0);
                            GraphTargetItem gti = sti.getValue().getNotCoerced();
                            if (gti instanceof NextValueTreeItem) {
                                list.set(i, new ForEachInTreeItem(w.src, w.loop, new InTreeItem(null, sti.getObject(), ((HasNextTreeItem) w.expression.get(w.expression.size() - 1)).collection), w.commands));
                            } else if (gti instanceof NextNameTreeItem) {
                                list.set(i, new ForInTreeItem(w.src, w.loop, new InTreeItem(null, sti.getObject(), ((HasNextTreeItem) w.expression.get(w.expression.size() - 1)).collection), w.commands));
                            }
                        }
                    }
                }
            }
        }


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
    public List prepareBranchLocalData(List localData) {
        List ret = new ArrayList();
        ret.addAll(localData);
        Stack<GraphTargetItem> scopeStack = (Stack<GraphTargetItem>) ret.get(DATA_SCOPESTACK);
        Stack<GraphTargetItem> copyScopeStack = new Stack<GraphTargetItem>();
        copyScopeStack.addAll(scopeStack);
        ret.set(DATA_SCOPESTACK, copyScopeStack);
        return ret;
    }
}

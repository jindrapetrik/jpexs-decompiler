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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.Null;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.EnumerateTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.SetTypeTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.StoreRegisterTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.clauses.ForInTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.NeqTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.operations.StrictEqTreeItem;
import com.jpexs.decompiler.flash.graph.BreakItem;
import com.jpexs.decompiler.flash.graph.ContinueItem;
import com.jpexs.decompiler.flash.graph.Graph;
import com.jpexs.decompiler.flash.graph.GraphPart;
import com.jpexs.decompiler.flash.graph.GraphSource;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
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
public class ActionGraph extends Graph {

   public ActionGraph(List<Action> code, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int version) {
      super(new ActionGraphSource(code, version, registerNames, variables, functions), new ArrayList<Integer>());
      //this.version = version;
      /*heads = makeGraph(code, new ArrayList<GraphPart>());
       for (GraphPart head : heads) {
       fixGraph(head);
       makeMulti(head, new ArrayList<GraphPart>());
       }*/
   }

   public static List<GraphTargetItem> translateViaGraph(HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, List<Action> code, int version) {
      ActionGraph g = new ActionGraph(code, registerNames, variables, functions, version);
      List localData = new ArrayList();
      localData.add(registerNames);
      return g.translate(localData);
   }

   @Override
   protected void finalProcess(List<GraphTargetItem> list, int level) {
      List<GraphTargetItem> ret = Action.checkClass(list);
      if (ret != list) {
         list.clear();
         list.addAll(ret);
      }
      for (int t = 0; t < list.size(); t++) {
         GraphTargetItem it = list.get(t);
         if (it instanceof WhileItem) {
            WhileItem wi = (WhileItem) it;
            if ((!wi.commands.isEmpty()) && (wi.commands.get(0) instanceof SetTypeTreeItem)) {
               SetTypeTreeItem sti = (SetTypeTreeItem) wi.commands.get(0);
               if (wi.expression instanceof NeqTreeItem) {
                  NeqTreeItem ne = (NeqTreeItem) wi.expression;
                  if (ne.rightSide instanceof DirectValueTreeItem) {
                     DirectValueTreeItem dv = (DirectValueTreeItem) ne.rightSide;
                     if (dv.value instanceof Null) {
                        if (ne.leftSide instanceof EnumerateTreeItem) {
                           EnumerateTreeItem eti = (EnumerateTreeItem) ne.leftSide;
                           list.remove(t);
                           wi.commands.remove(0);
                           list.add(t, new ForInTreeItem(null, wi.loop, sti.getObject(), eti.object, wi.commands));
                        }
                     }
                  }
               }
            }

         }
      }
   }

   @Override
   protected List<GraphTargetItem> check(GraphSource code, List localData, List<GraphPart> allParts, Stack<GraphTargetItem> stack, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, List<GraphTargetItem> output, HashMap<Loop, List<GraphTargetItem>> forFinalCommands) {
      if (!output.isEmpty()) {
         if (output.get(output.size() - 1) instanceof StoreRegisterTreeItem) {
            StoreRegisterTreeItem str = (StoreRegisterTreeItem) output.get(output.size() - 1);
            if (str.value instanceof EnumerateTreeItem) {
               output.remove(output.size() - 1);
            }
         }
      }
      List<GraphTargetItem> ret = null;
      if ((part.nextParts.size() == 2) && (!stack.isEmpty()) && (stack.peek() instanceof StrictEqTreeItem)) {

         GraphTargetItem switchedObject = null;
         if (!output.isEmpty()) {
            if (output.get(output.size() - 1) instanceof StoreRegisterTreeItem) {
               switchedObject = ((StoreRegisterTreeItem) output.get(output.size() - 1)).value;
            }
         }
         if (switchedObject == null) {
            switchedObject = new DirectValueTreeItem(null, -1, new Null(), null);
         }
         HashMap<Integer, GraphTargetItem> caseValuesMap = new HashMap<Integer, GraphTargetItem>();

         int pos = 0;
         StrictEqTreeItem set = (StrictEqTreeItem) stack.pop();
         caseValuesMap.put(pos, set.rightSide);

         //GraphPart switchLoc = part.nextParts.get(1).nextParts.get(0);
         List<GraphPart> caseBodyParts = new ArrayList<GraphPart>();
         caseBodyParts.add(part.nextParts.get(0));
         GraphTargetItem top = null;
         int cnt = 1;
         while (part.nextParts.size() > 1
                 && part.nextParts.get(1).getHeight() > 1
                 && code.get(part.nextParts.get(1).end) instanceof ActionIf
                 && ((top = translatePartGetStack(localData, part.nextParts.get(1), stack)) instanceof StrictEqTreeItem)) {
            cnt++;
            part = part.nextParts.get(1);
            pos++;
            caseBodyParts.add(part.nextParts.get(0));

            set = (StrictEqTreeItem) top;
            caseValuesMap.put(pos, set.rightSide);
         }
         if (cnt == 1) {
            stack.push(set);
         } else {
            part = part.nextParts.get(1);

            GraphPart defaultPart = part;
            //caseBodyParts.add(defaultPart);



            List<GraphPart> defaultAndLastPart = new ArrayList<GraphPart>();
            defaultAndLastPart.add(defaultPart);
            defaultAndLastPart.add(caseBodyParts.get(caseBodyParts.size() - 1));

            GraphPart defaultPart2 = getCommonPart(defaultAndLastPart);

            List<GraphTargetItem> defaultCommands = new ArrayList<GraphTargetItem>();

            defaultCommands = printGraph(localData, stack, allParts, null, defaultPart, defaultPart2, loops, forFinalCommands);


            List<GraphPart> loopContinues = new ArrayList<GraphPart>();
            for (Loop l : loops) {
               if (l.loopContinue != null) {
                  loopContinues.add(l.loopContinue);
               }
            }

            List<GraphPart> breakParts = new ArrayList<GraphPart>();
            for (int g = 0; g < caseBodyParts.size(); g++) {
               if (g < caseBodyParts.size() - 1) {
                  if (caseBodyParts.get(g).leadsTo(code, caseBodyParts.get(g + 1), loopContinues)) {
                     continue;
                  }
               }
               GraphPart nsp = caseBodyParts.get(g).getNextSuperPartPath(loopContinues);
               if (nsp != null) {
                  breakParts.add(nsp);
               }
            }
            Collections.sort(breakParts, new Comparator<GraphPart>() {
               @Override
               public int compare(GraphPart o1, GraphPart o2) {
                  return o2.path.length() - o1.path.length();
               }
            });

            GraphPart breakPart = breakParts.isEmpty() ? null : breakParts.get(0);
            if ((defaultPart2 != breakPart) && (defaultCommands.isEmpty())) {
               defaultPart = defaultPart2;
            }


            List<GraphTargetItem> caseValues = new ArrayList<GraphTargetItem>();
            for (int i = 0; i < caseBodyParts.size(); i++) {
               if (caseValuesMap.containsKey(i)) {
                  caseValues.add(caseValuesMap.get(i));
               } else {
                  continue;
               }
            }

            List<List<GraphTargetItem>> caseCommands = new ArrayList<List<GraphTargetItem>>();
            GraphPart next = null;



            next = breakPart;

            GraphTargetItem ti = checkLoop(next, stopPart, loops);
            Loop currentLoop = new Loop(loops.size(), null, next);
            loops.add(currentLoop);
            //switchLoc.getNextPartPath(new ArrayList<GraphPart>());
            List<Integer> valuesMapping = new ArrayList<Integer>();
            List<GraphPart> caseBodies = new ArrayList<GraphPart>();
            for (int i = 0; i < caseValues.size(); i++) {
               GraphPart cur = caseBodyParts.get(i);
               if (!caseBodies.contains(cur)) {
                  caseBodies.add(cur);
               }
               valuesMapping.add(caseBodies.indexOf(cur));
            }




            if (defaultPart == breakPart) {
               defaultPart = null;
            }
            if ((defaultPart != null) && (defaultCommands.isEmpty())) {
               defaultCommands = printGraph(localData, stack, allParts, null, defaultPart, next, loops, forFinalCommands);
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
                     if (!caseBodies.get(i).leadsTo(code, caseBodies.get(i + 1), ignored)) {
                        cc.add(new BreakItem(null, currentLoop.id));
                     } else {
                        nextCase = caseBodies.get(i + 1);
                     }
                  } else if (!defaultCommands.isEmpty()) {
                     if (!caseBodies.get(i).leadsTo(code, defaultPart, ignored)) {
                        cc.add(new BreakItem(null, currentLoop.id));
                     } else {
                        nextCase = defaultPart;
                     }
                  }
               }
               cc.addAll(0, printGraph(localData, stack, allParts, null, caseBodies.get(i), nextCase, loops, forFinalCommands));
               if (cc.size() >= 2) {
                  if (cc.get(cc.size() - 1) instanceof BreakItem) {
                     if ((cc.get(cc.size() - 2) instanceof ContinueItem) || (cc.get(cc.size() - 2) instanceof BreakItem)) {
                        cc.remove(cc.size() - 1);
                     }
                  }
               }
               caseCommands.add(cc);
            }
            ret = new ArrayList<GraphTargetItem>();
            SwitchItem sti = new SwitchItem(null, currentLoop, switchedObject, caseValues, caseCommands, defaultCommands, valuesMapping);
            ret.add(sti);

            if (next != null) {
               if (ti != null) {
                  ret.add(ti);
               } else {
                  ret.addAll(printGraph(localData, stack, allParts, null, next, stopPart, loops, forFinalCommands));
               }
            }
         }
      }
      return ret;
   }
   /*
    private TreeItem translatePartGetStack(GraphPart part, Stack<TreeItem> stack, HashMap<Integer, String> registerNames) {
    stack = (Stack<TreeItem>) stack.clone();
    translatePart(part, stack, registerNames);
    return stack.pop();
    }

    private List<TreeItem> translatePart(GraphPart part, Stack<TreeItem> stack, HashMap<Integer, String> registerNames) {
    List<GraphPart> sub = part.getSubParts();
    List<TreeItem> ret = new ArrayList<TreeItem>();
    int end = 0;
    for (GraphPart p : sub) {
    if (p.end == -1) {
    p.end = code.size() - 1;
    }
    if (p.start == code.size()) {
    continue;
    } else if (p.end == code.size()) {
    p.end--;
    }
    end = p.end;
    int start = p.start;
    if (code.get(end) instanceof ActionJump) {
    end--;
    } else if (code.get(end) instanceof ActionIf) {
    end--;
    }
    ret.addAll(Action.actionsPartToTree(registerNames, stack, code, start, end, version));
    }
    return ret;
    }

    private TreeItem checkLoop(GraphPart part, GraphPart stopPart, List<Loop> loops) {
    if (part == stopPart) {
    return null;
    }
    for (Loop l : loops) {
    if (l.loopContinue == part) {
    return (new ContinueTreeItem(null, l.loopBreak == null ? -1 : l.loopBreak.start));
    }
    if (l.loopBreak == part) {
    return (new BreakTreeItem(null, part.start));
    }
    }
    return null;
    }
    private boolean doDecompile = true;

    private List<TreeItem> printGraph(HashMap<Integer, String> registerNames, Stack<TreeItem> stack, List<GraphPart> allParts, GraphPart parent, GraphPart part, GraphPart stopPart, List<Loop> loops, HashMap<Loop, List<TreeItem>> forFinalCommands) {
    List<TreeItem> ret = new ArrayList<TreeItem>();
    boolean debugMode = false;
    if (part.start >= code.size()) {
    return ret;
    }

    if (!doDecompile) {
    // ret.add(new CommentTreeItem(null, "not decompiled"));
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
    List<GraphPart> parts = new ArrayList<GraphPart>();
    if (part instanceof GraphPartMulti) {
    parts = ((GraphPartMulti) part).parts;
    } else {
    parts.add(part);
    }
    boolean isIf = false;
    int end = part.end;
    if (end == -1) {
    end = code.size() - 1;
    }
    if (end == code.size()) {
    end--;
    }
    output.addAll(translatePart(part, stack, registerNames));
    if (end > -1) {
    if (code.get(end) instanceof ActionJump) {
    end--;
    } else if (code.get(end) instanceof ActionIf) {
    end--;
    isIf = true;
    }
    }
    if (isIf) {
    Action ins = code.get(end + 1);
    if ((stack.size() >= 2) && (stack.get(stack.size() - 1) instanceof NotTreeItem) && (((NotTreeItem) stack.get(stack.size() - 1)).value == stack.get(stack.size() - 2))) {
    ret.addAll(output);
    printGraph(registerNames, stack, allParts, parent, part.nextParts.get(1), part.nextParts.get(0), loops, forFinalCommands);
    TreeItem second = stack.pop();
    TreeItem first = stack.pop();
    stack.push(new AndTreeItem(ins, first, second));
    ret.addAll(printGraph(registerNames, stack, allParts, parent, part.nextParts.get(0), stopPart, loops, forFinalCommands));
    return ret;
    } else if ((stack.size() >= 2) && (stack.get(stack.size() - 1) == stack.get(stack.size() - 2))) {
    ret.addAll(output);
    printGraph(registerNames, stack, allParts, parent, part.nextParts.get(1), part.nextParts.get(0), loops, forFinalCommands);
    TreeItem second = stack.pop();
    TreeItem first = stack.pop();
    stack.push(new OrTreeItem(ins, first, second));
    ret.addAll(printGraph(registerNames, stack, allParts, parent, part.nextParts.get(0), stopPart, loops, forFinalCommands));
    return ret;
    } else if (stack.peek() instanceof StrictEqTreeItem) {

    TreeItem switchedObject = null;
    if (!output.isEmpty()) {
    if (output.get(output.size() - 1) instanceof StoreRegisterTreeItem) {
    switchedObject = ((StoreRegisterTreeItem) output.get(output.size() - 1)).value;
    }
    }
    if (switchedObject == null) {
    switchedObject = new DirectValueTreeItem(null, -1, new Null(), null);
    }
    HashMap<Integer, TreeItem> caseValuesMap = new HashMap<Integer, TreeItem>();

    int pos = 0;
    StrictEqTreeItem set = (StrictEqTreeItem) stack.pop();
    caseValuesMap.put(pos, set.rightSide);

    //GraphPart switchLoc = part.nextParts.get(1).nextParts.get(0);
    List<GraphPart> caseBodyParts = new ArrayList<GraphPart>();
    caseBodyParts.add(part.nextParts.get(0));
    TreeItem top = null;
    int cnt = 1;
    while (part.nextParts.size() > 1
    && part.nextParts.get(1).getHeight() > 1
    && code.get(part.nextParts.get(1).end) instanceof ActionIf
    && ((top = translatePartGetStack(part.nextParts.get(1), stack, registerNames)) instanceof StrictEqTreeItem)) {
    cnt++;
    part = part.nextParts.get(1);
    pos++;
    caseBodyParts.add(part.nextParts.get(0));
               
    set = (StrictEqTreeItem) top;
    caseValuesMap.put(pos, set.rightSide);
    }
    if (cnt == 1) {
    stack.push(set);
    } else {
    part = part.nextParts.get(1);

    GraphPart defaultPart = part;
    //caseBodyParts.add(defaultPart);



    List<GraphPart> defaultAndLastPart = new ArrayList<GraphPart>();
    defaultAndLastPart.add(defaultPart);
    defaultAndLastPart.add(caseBodyParts.get(caseBodyParts.size() - 1));

    GraphPart defaultPart2 = getCommonPart(defaultAndLastPart);

    List<TreeItem> defaultCommands = new ArrayList<TreeItem>();

    defaultCommands = printGraph(registerNames, stack, allParts, null, defaultPart, defaultPart2, loops, forFinalCommands);


    List<GraphPart> loopContinues = new ArrayList<GraphPart>();
    for (Loop l : loops) {
    if (l.loopContinue != null) {
    loopContinues.add(l.loopContinue);
    }
    }

    List<GraphPart> breakParts = new ArrayList<GraphPart>();
    for (int g = 0; g < caseBodyParts.size(); g++) {
    if (g < caseBodyParts.size() - 1) {
    if (caseBodyParts.get(g).leadsTo(caseBodyParts.get(g + 1), loopContinues)) {
    continue;
    }
    }
    GraphPart nsp = caseBodyParts.get(g).getNextSuperPartPath(loopContinues);
    if (nsp != null) {
    breakParts.add(nsp);
    }
    }
    Collections.sort(breakParts, new Comparator<GraphPart>() {
    @Override
    public int compare(GraphPart o1, GraphPart o2) {
    return o2.path.length() - o1.path.length();
    }
    });

    GraphPart breakPart = breakParts.isEmpty() ? null : breakParts.get(0);
    if ((defaultPart2 != breakPart) && (defaultCommands.isEmpty())) {
    defaultPart = defaultPart2;
    }


    List<TreeItem> caseValues = new ArrayList<TreeItem>();
    for (int i = 0; i < caseBodyParts.size(); i++) {
    if (caseValuesMap.containsKey(i)) {
    caseValues.add(caseValuesMap.get(i));
    } else {
    continue;
    }
    }

    List<List<TreeItem>> caseCommands = new ArrayList<List<TreeItem>>();
    GraphPart next = null;



    next = breakPart;

    TreeItem ti = checkLoop(next, stopPart, loops);
    loops.add(new Loop(null, next));
    //switchLoc.getNextPartPath(new ArrayList<GraphPart>());
    List<Integer> valuesMapping = new ArrayList<Integer>();
    List<GraphPart> caseBodies = new ArrayList<GraphPart>();
    for (int i = 0; i < caseValues.size(); i++) {
    GraphPart cur = caseBodyParts.get(i);
    if (!caseBodies.contains(cur)) {
    caseBodies.add(cur);
    }
    valuesMapping.add(caseBodies.indexOf(cur));
    }




    if (defaultPart == breakPart) {
    defaultPart = null;
    }
    if ((defaultPart != null) && (defaultCommands.isEmpty())) {
    defaultCommands = printGraph(registerNames, stack, allParts, null, defaultPart, next, loops, forFinalCommands);
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
    } else if (!defaultCommands.isEmpty()) {
    if (!caseBodies.get(i).leadsTo(defaultPart, ignored)) {
    cc.add(new BreakTreeItem(null, next.start));
    } else {
    nextCase = defaultPart;
    }
    }
    }
    cc.addAll(0, printGraph(registerNames, stack, allParts, null, caseBodies.get(i), nextCase, loops, forFinalCommands));
    if (cc.size() >= 2) {
    if (cc.get(cc.size() - 1) instanceof BreakTreeItem) {
    if ((cc.get(cc.size() - 2) instanceof ContinueTreeItem) || (cc.get(cc.size() - 2) instanceof BreakTreeItem)) {
    cc.remove(cc.size() - 1);
    }
    }
    }
    caseCommands.add(cc);
    }

    SwitchTreeItem sti = new SwitchTreeItem(null, next == null ? -1 : next.start, switchedObject, caseValues, caseCommands, defaultCommands, valuesMapping);
    ret.add(sti);

    if (next != null) {
    if (ti != null) {
    ret.add(ti);
    } else {
    ret.addAll(printGraph(registerNames, stack, allParts, null, next, stopPart, loops, forFinalCommands));
    }
    }
    return ret;
    }
    } else {
    //ins.translate
    }
    //((IfTypeIns)ins.definition).translateInverted(new HashMap<Integer,TreeItem>(), co.stack, ins);
    }

    int ip = part.start;
    List<GraphPart> loopContinues = new ArrayList<GraphPart>();
    for (Loop l : loops) {
    if (l.loopContinue != null) {
    loopContinues.add(l.loopContinue);
    }
    }
    boolean loop = false;
    boolean reversed = false;
    if ((!part.nextParts.isEmpty()) && part.nextParts.get(0).leadsTo(part, loopContinues)) {
    loop = true;
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

    if (part.nextParts.size() > 1) {
    currentLoop.loopBreak = part.nextParts.get(reversed ? 0 : 1);
    }

    forFinalCommands.put(currentLoop, new ArrayList<TreeItem>());

    int breakIp = -1;
    if (currentLoop.loopBreak != null) {
    breakIp = currentLoop.loopBreak.start;
    }
    TreeItem expr = null;
    if ((code.get(part.end) instanceof ActionJump) || (!(code.get(part.end) instanceof ActionIf))) {
    expr = new DirectValueTreeItem(null, -1, (Boolean) true, new ArrayList<String>());
    } else {
    if (stack.isEmpty()) {
    }
    expr = stack.pop();
    }
    if (doWhile) {
    ret.add(new DoWhileTreeItem(null, breakIp, part.start, output, expr));
    } else {
    ret.addAll(output);
    }

    GraphPart next = part.getNextPartPath(loopContinues);

    if (loop && (!doWhile)) {

    if (reversed && (expr instanceof LogicalOp)) {
    expr = ((LogicalOp) expr).invert();
    }
    List<TreeItem> loopBody = null;
    List<TreeItem> finalCommands = null;
    GraphPart finalPart = null;
    loopBody = printGraph(registerNames, stack, allParts, part, part.nextParts.get(reversed ? 1 : 0), stopPart, loops, forFinalCommands);

    finalCommands = forFinalCommands.get(currentLoop);
    if (!finalCommands.isEmpty()) {
    ret.add(new ForTreeItem(null, breakIp, currentLoop.loopContinue.start, new ArrayList<TreeItem>(), expr, finalCommands, loopBody));
    } else {
    ret.add(new WhileTreeItem(null, breakIp, part.start, expr, loopBody));
    }
    } else if (!loop) {
    if (expr instanceof LogicalOp) {
    expr = ((LogicalOp) expr).invert();
    } else {
    expr = new NotTreeItem(null, expr);
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
    onTrue = printGraph(registerNames, trueStack, allParts, part, part.nextParts.get(1), next == null ? stopPart : next, loops, forFinalCommands);
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
    onFalse = (((next == part.nextParts.get(0)) || (part.nextParts.get(0).path.equals(part.path) || part.nextParts.get(0).path.length() < part.path.length())) ? new ArrayList<TreeItem>() : printGraph(registerNames, falseStack, allParts, part, part.nextParts.get(0), next == null ? stopPart : next, loops, forFinalCommands));
    if (debugMode) {
    System.err.println("/ONFALSE (inside " + part + ")");
    }
    }

    if (onTrue.isEmpty() && onFalse.isEmpty() && (trueStack.size() > stackSizeBefore) && (falseStack.size() > stackSizeBefore)) {
    stack.push(new TernarOpTreeItem(null, expr, trueStack.pop(), falseStack.pop()));
    } else {
    ret.add(new IfTreeItem(null, expr, onTrue, onFalse));

    //Same continues in onTrue and onFalse gets continue on parent level
    if ((!onTrue.isEmpty()) && (!onFalse.isEmpty())) {
    if (onTrue.get(onTrue.size() - 1) instanceof ContinueTreeItem) {
    if (onFalse.get(onFalse.size() - 1) instanceof ContinueTreeItem) {
    if (((ContinueTreeItem) onTrue.get(onTrue.size() - 1)).loopPos == ((ContinueTreeItem) onFalse.get(onFalse.size() - 1)).loopPos) {
    onTrue.remove(onTrue.size() - 1);
    ret.add(onFalse.remove(onFalse.size() - 1));
    }
    }
    }
    }
    }
    }
    if (loop && (part.nextParts.size() > 1)) {
    loops.remove(currentLoop); //remove loop so no break shows up
    ret.addAll(printGraph(registerNames, stack, allParts, part, part.nextParts.get(reversed ? 0 : 1), stopPart, loops, forFinalCommands));
    }

    if (next != null) {
    TreeItem ti = checkLoop(next, stopPart, loops);
    if (ti != null) {
    ret.add(ti);
    } else {
    if (debugMode) {
    System.err.println("NEXT: (inside " + part + ")");
    }
    ret.addAll(printGraph(registerNames, stack, allParts, part, next, stopPart, loops, forFinalCommands));
    if (debugMode) {
    System.err.println("/NEXT: (inside " + part + ")");
    }
    }

    }
    } else {
    ret.addAll(output);
    }
    onepart:
    if (part.nextParts.size() == 1 && (!loop)) {


    GraphPart p = part.nextParts.get(0);
    TreeItem lop = checkLoop(p, stopPart, loops);
    if (lop == null) {
    if (p.path.length() == part.path.length()) {
    ret.addAll(printGraph(registerNames, stack, allParts, part, p, stopPart, loops, forFinalCommands));
    } else {
    if ((p != stopPart) && (p.refs.size() > 1)) {
    List<GraphPart> nextList = new ArrayList<GraphPart>();
    populateParts(p, nextList);
    Loop nearestLoop = null;
    loopn:
    for (GraphPart n : nextList) {
    for (Loop l : loops) {
    if (l.loopContinue == n) {
    nearestLoop = l;
    break loopn;
    }
    }
    }
    if ((nearestLoop != null) && (nearestLoop.loopBreak != null)) {
    List<TreeItem> finalCommands = printGraph(registerNames, stack, allParts, part, p, nearestLoop.loopContinue, loops, forFinalCommands);
    nearestLoop.loopContinue = p;
    forFinalCommands.put(nearestLoop, finalCommands);
    ContinueTreeItem cti = new ContinueTreeItem(null, nearestLoop.loopBreak.start);
    ret.add(cti);
    }
    }
    }
    } else {
    ret.add(lop);
    }
    }
    return ret;
    }
    private List<Integer> posCache;

    private void buildCache() {
    posCache = new ArrayList<Integer>();
    int pos = 0;
    for (int i = 0; i < code.size(); i++) {
    posCache.add(pos);
    pos += code.get(i).getBytes(version).length;
    }
    posCache.add(pos);
    }

    private int pos2adr(int pos) {
    if (posCache == null) {
    buildCache();
    }
    return posCache.get(pos);
    }

    private int adr2pos(int adr) {
    if (posCache == null) {
    buildCache();
    }
    return posCache.indexOf(adr);
    }

    private void visitCode(int ip, int lastIp, HashMap<Integer, List<Integer>> refs) {
    while (ip < code.size()) {
    refs.get(ip).add(lastIp);
    lastIp = ip;
    if (refs.get(ip).size() > 1) {
    break;
    }
    Action ins = code.get(ip);
    if (ins instanceof ActionThrow) {
    break;
    }
    if (ins instanceof ActionReturn) {
    break;
    }

    if (ins instanceof ActionJump) {
    ip = adr2pos(pos2adr(ip) + ins.getBytes(version).length + ((ActionJump) ins).offset);
    continue;
    } else if (ins instanceof ActionIf) {
    visitCode(adr2pos(pos2adr(ip) + ins.getBytes(version).length + ((ActionIf) ins).offset), ip, refs);
    }
    ip++;
    };
    }

    public HashMap<Integer, List<Integer>> visitCode() {
    HashMap<Integer, List<Integer>> refs = new HashMap<Integer, List<Integer>>();
    for (int i = 0; i < code.size(); i++) {
    refs.put(i, new ArrayList<Integer>());
    }
    visitCode(0, 0, refs);
    return refs;
    }

    private List<GraphPart> makeGraph(List<Action> actions, List<GraphPart> allBlocks) {
    HashMap<Integer, List<Integer>> refs = visitCode();
    List<GraphPart> ret = new ArrayList<GraphPart>();
    boolean visited[] = new boolean[code.size()];
    ret.add(makeGraph(null, "0", code, 0, 0, allBlocks, refs, visited));
    return ret;
    }

    private GraphPart makeGraph(GraphPart parent, String path, List<Action> code, int startip, int lastIp, List<GraphPart> allBlocks, HashMap<Integer, List<Integer>> refs, boolean visited2[]) {

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
    while (ip < code.size()) {
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
    Action ins = code.get(ip);
    if ((ins instanceof ActionThrow) || (ins instanceof ActionReturn)) {
    part.end = ip;
    allBlocks.add(part);
    break;
    }
    if (ins instanceof ActionJump) {
    part.end = ip;
    allBlocks.add(part);
    int newip = adr2pos(pos2adr(ip) + ins.getBytes(version).length + ((ActionJump) ins).offset);
    part.nextParts.add(g = makeGraph(part, path, code, newip, lastIp, allBlocks, refs, visited2));
    g.refs.add(part);
    break;
    } else if (ins instanceof ActionIf) {
    part.end = ip;
    allBlocks.add(part);
    part.nextParts.add(g = makeGraph(part, path + "0", code, adr2pos(pos2adr(ip) + ins.getBytes(version).length + ((ActionIf) ins).offset), ip, allBlocks, refs, visited2));
    g.refs.add(part);
    part.nextParts.add(g = makeGraph(part, path + "1", code, ip + 1, ip, allBlocks, refs, visited2));
    g.refs.add(part);

    break;
    }
    ip++;
    };
    if (ip == code.size()) {
    allBlocks.add(part);
    if (part.start == ip) {
    part.end = ip;
    } else {
    part.end = code.size() - 1;
    part.nextParts.add(makeGraph(part, path, code, ip, ip, allBlocks, refs, visited2));
    }

    }
    return ret;
    }
    */

   @Override
   protected int checkIp(int ip) {

      //return in for..in
      GraphSourceItem action = code.get(ip);
      if ((action instanceof ActionPush) && (((ActionPush) action).values.size() == 1) && (((ActionPush) action).values.get(0) instanceof Null)) {
         if (ip + 3 < code.size()) {
            if ((code.get(ip + 1) instanceof ActionEquals) || (code.get(ip + 1) instanceof ActionEquals2)) {
               if (code.get(ip + 2) instanceof ActionNot) {
                  if (code.get(ip + 3) instanceof ActionIf) {
                     ActionIf aif = (ActionIf) code.get(ip + 3);
                     if (code.adr2pos(code.pos2adr(ip + 4) + aif.offset) == ip) {
                        ip += 4;
                     }
                  }
               }
            }
         }
      }
      return ip;
   }
}

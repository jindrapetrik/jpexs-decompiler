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
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.EnumerateTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.FunctionTreeItem;
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
        List<Object> localData = new ArrayList<>();
        localData.add(registerNames);
        g.init();
        return g.translate(localData);
    }

    @Override
    public void finalProcessStack(Stack<GraphTargetItem> stack, List<GraphTargetItem> output) {
        if (stack.size() > 0) {
            for (int i = stack.size() - 1; i >= 0; i--) {
                //System.err.println(stack.get(i));
                if (stack.get(i) instanceof FunctionTreeItem) {
                    FunctionTreeItem f = (FunctionTreeItem) stack.remove(i);
                    if (!output.contains(f)) {
                        output.add(0, f);
                    }
                }
            }
        }
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
                    if (wi.expression.get(wi.expression.size() - 1) instanceof NeqTreeItem) {
                        NeqTreeItem ne = (NeqTreeItem) wi.expression.get(wi.expression.size() - 1);
                        if (ne.rightSide instanceof DirectValueTreeItem) {
                            DirectValueTreeItem dv = (DirectValueTreeItem) ne.rightSide;
                            if (dv.value instanceof Null) {
                                GraphTargetItem en = ne.leftSide;
                                if (en instanceof StoreRegisterTreeItem) {
                                    en = ((StoreRegisterTreeItem) en).value;
                                }
                                if (en instanceof EnumerateTreeItem) {
                                    EnumerateTreeItem eti = (EnumerateTreeItem) en;
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
    protected List<GraphPart> checkPrecoNextParts(GraphPart part) {
        List<GraphSourceItem> items = getPartItems(part);
        part = makeMultiPart(part);
        if (!items.isEmpty()) {
            if (items.get(items.size() - 1) instanceof ActionIf) {
                if (items.get(items.size() - 2) instanceof ActionStrictEquals) {
                    List<Integer> storeRegisters = new ArrayList<>();
                    for (GraphSourceItem s : items) {
                        if (s instanceof ActionStoreRegister) {
                            ActionStoreRegister sr = (ActionStoreRegister) s;
                            storeRegisters.add(sr.registerNumber);
                        }
                    }
                    if (!storeRegisters.isEmpty()) {
                        List<GraphPart> caseBodies = new ArrayList<>();
                        boolean proceed = false;
                        do {
                            proceed = false;
                            caseBodies.add(part.nextParts.get(0)); //jump
                            part = part.nextParts.get(1); //nojump
                            items = getPartItems(part);
                            part = makeMultiPart(part);
                            if (!items.isEmpty()) {
                                if (items.get(0) instanceof ActionPush) {
                                    ActionPush pu = (ActionPush) items.get(0);
                                    if (!pu.values.isEmpty()) {
                                        if (pu.values.get(0) instanceof RegisterNumber) {
                                            RegisterNumber rn = (RegisterNumber) pu.values.get(0);
                                            if (storeRegisters.contains(rn.number)) {
                                                storeRegisters.clear();
                                                storeRegisters.add(rn.number);
                                                if (items.get(items.size() - 1) instanceof ActionIf) {
                                                    if (items.size() > 1) {
                                                        if (items.get(items.size() - 2) instanceof ActionStrictEquals) {
                                                            proceed = true;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } while (proceed);

                        if (caseBodies.size() > 1) {
                            caseBodies.add(part); //TODO: properly detect default clause (?)
                            return caseBodies;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected List<GraphTargetItem> check(GraphSource code, List<Object> localData, List<GraphPart> allParts, Stack<GraphTargetItem> stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<GraphTargetItem> output, Loop currentLoop) {
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
            HashMap<Integer, GraphTargetItem> caseValuesMap = new HashMap<>();

            int pos = 0;
            StrictEqTreeItem set = (StrictEqTreeItem) stack.pop();
            caseValuesMap.put(pos, set.rightSide);
            if (set.leftSide instanceof StoreRegisterTreeItem) {
                switchedObject = ((StoreRegisterTreeItem) set.leftSide).value;
            }
            //GraphPart switchLoc = part.nextParts.get(1).nextParts.get(0);
            List<GraphPart> caseBodyParts = new ArrayList<>();
            caseBodyParts.add(part.nextParts.get(0));
            GraphTargetItem top = null;
            int cnt = 1;
            while (part.nextParts.size() > 1
                    && part.nextParts.get(1).getHeight() > 1
                    && code.get(part.nextParts.get(1).end >= code.size() ? code.size() - 1 : part.nextParts.get(1).end) instanceof ActionIf
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

                GraphPart defaultPart = part; //21-21
                //caseBodyParts.add(defaultPart);



                List<GraphPart> defaultAndLastPart = new ArrayList<>();
                defaultAndLastPart.add(defaultPart);
                defaultAndLastPart.add(caseBodyParts.get(caseBodyParts.size() - 1));

                GraphPart defaultPart2 = getCommonPart(defaultAndLastPart, loops);//34-37

                List<GraphTargetItem> defaultCommands = new ArrayList<>();
                List<GraphPart> stopPart2 = new ArrayList<>(stopPart);
                stopPart2.add(defaultPart2);
                defaultCommands = printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, null, defaultPart, stopPart2, loops);


                List<GraphPart> loopContinues = new ArrayList<>();
                for (Loop l : loops) {
                    if (l.loopContinue != null) {
                        loopContinues.add(l.loopContinue);
                    }
                }

                List<GraphPart> breakParts = new ArrayList<>();
                /*for (int g = 0; g < caseBodyParts.size(); g++) {
                 if (g < caseBodyParts.size() - 1) {
                 if (caseBodyParts.get(g).leadsTo(code, caseBodyParts.get(g + 1), loops)) {
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
                 });*/

                //GraphPart breakPart = breakParts.isEmpty() ? null : breakParts.get(0);
                GraphPart breakPart = getMostCommonPart(caseBodyParts, loops);
                if ((defaultPart2 != breakPart) && (defaultCommands.isEmpty())) {
                    defaultPart = defaultPart2;
                }


                List<GraphTargetItem> caseValues = new ArrayList<>();
                for (int i = 0; i < caseBodyParts.size(); i++) {
                    if (caseValuesMap.containsKey(i)) {
                        caseValues.add(caseValuesMap.get(i));
                    } else {
                        continue;
                    }
                }

                List<List<GraphTargetItem>> caseCommands = new ArrayList<>();
                GraphPart next = null;



                next = breakPart;

                GraphTargetItem ti = checkLoop(next, stopPart, loops);
                currentLoop = new Loop(loops.size(), null, next);
                loops.add(currentLoop);
                //switchLoc.getNextPartPath(new ArrayList<GraphPart>());
                List<Integer> valuesMapping = new ArrayList<>();
                List<GraphPart> caseBodies = new ArrayList<>();
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
                    List<GraphPart> stopPart2x = new ArrayList<>(stopPart);
                    stopPart2x.add(next);
                    defaultCommands = printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, null, defaultPart, stopPart2x, loops);
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
                            if (!caseBodies.get(i).leadsTo(code, caseBodies.get(i + 1), loops)) {
                                cc.add(new BreakItem(null, currentLoop.id));
                            } else {
                                nextCase = caseBodies.get(i + 1);
                            }
                        } else if (!defaultCommands.isEmpty()) {
                            if (!caseBodies.get(i).leadsTo(code, defaultPart, loops)) {
                                cc.add(new BreakItem(null, currentLoop.id));
                            } else {
                                nextCase = defaultPart;
                            }
                        }
                    }
                    List<GraphPart> stopPart2x = new ArrayList<>(stopPart);
                    //stopPart2.add(nextCase);
                    for (GraphPart b : caseBodies) {
                        if (b != caseBodies.get(i)) {
                            stopPart2x.add(b);
                        }
                    }
                    if (defaultPart != null) {
                        stopPart2x.add(defaultPart);
                    }
                    if (breakPart != null) {
                        stopPart2x.add(breakPart);
                    }
                    cc.addAll(0, printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, null, caseBodies.get(i), stopPart2x, loops));
                    if (cc.size() >= 2) {
                        if (cc.get(cc.size() - 1) instanceof BreakItem) {
                            if ((cc.get(cc.size() - 2) instanceof ContinueItem) || (cc.get(cc.size() - 2) instanceof BreakItem)) {
                                cc.remove(cc.size() - 1);
                            }
                        }
                    }
                    caseCommands.add(cc);
                }
                ret = new ArrayList<>();
                if (!output.isEmpty()) {
                    if (output.get(output.size() - 1) instanceof StoreRegisterTreeItem) {
                        output.remove(output.size() - 1);
                    }
                }
                ret.addAll(output);
                SwitchItem sti = new SwitchItem(null, currentLoop, switchedObject, caseValues, caseCommands, defaultCommands, valuesMapping);
                ret.add(sti);

                if (next != null) {
                    if (ti != null) {
                        ret.add(ti);
                    } else {
                        ret.addAll(printGraph(new ArrayList<GraphPart>(), localData, stack, allParts, null, next, stopPart, loops));
                    }
                }
            }
        }
        return ret;
    }

    @Override
    protected int checkIp(int ip) {
        int oldIp = ip;
        //return in for..in
        GraphSourceItem action = code.get(ip);
        if ((action instanceof ActionPush) && (((ActionPush) action).values.size() == 1) && (((ActionPush) action).values.get(0) instanceof Null)) {
            if (ip + 3 < code.size()) {
                if ((code.get(ip + 1) instanceof ActionEquals) || (code.get(ip + 1) instanceof ActionEquals2)) {
                    if (code.get(ip + 2) instanceof ActionNot) {
                        if (code.get(ip + 3) instanceof ActionIf) {
                            ActionIf aif = (ActionIf) code.get(ip + 3);
                            if (code.adr2pos(code.pos2adr(ip + 4) + aif.getJumpOffset()) == ip) {
                                ip += 4;
                            }
                        }
                    }
                }
            }
        }
        if (oldIp != ip) {
            return checkIp(ip);
        }
        return ip;
    }
}

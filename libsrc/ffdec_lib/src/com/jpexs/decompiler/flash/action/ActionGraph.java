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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.FinalProcessLocalData;
import com.jpexs.decompiler.flash.SWF;
import static com.jpexs.decompiler.flash.action.Action.adr2ip;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.EnumerateActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.SetTarget2ActionItem;
import com.jpexs.decompiler.flash.action.model.SetTargetActionItem;
import com.jpexs.decompiler.flash.action.model.SetTypeActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ForInActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.TellTargetActionItem;
import com.jpexs.decompiler.flash.action.model.operations.NeqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StrictEqActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DefaultItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.UniversalLoopItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ActionGraph extends Graph {

    private boolean insideDoInitAction;

    public ActionGraph(String path, boolean insideDoInitAction, List<Action> code, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int version) {
        super(new ActionGraphSource(path, insideDoInitAction, code, version, registerNames, variables, functions), new ArrayList<>());
        this.insideDoInitAction = insideDoInitAction;
    }

    @Override
    public LinkedHashMap<String, Graph> getSubGraphs() {
        LinkedHashMap<String, Graph> subgraphs = new LinkedHashMap<>();
        List<Action> alist = ((ActionGraphSource) code).getActions();
        int ip = 0;
        for (Action action : alist) {
            if ((action instanceof GraphSourceItemContainer) && ((action instanceof ActionDefineFunction) || (action instanceof ActionDefineFunction2))) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) action;
                String functionName = (action instanceof ActionDefineFunction) ? ((ActionDefineFunction) action).functionName : ((ActionDefineFunction2) action).functionName;
                long endAddr = action.getAddress() + cnt.getHeaderSize();
                List<ActionList> outs = new ArrayList<>();
                for (long size : cnt.getContainerSizes()) {
                    if (size == 0) {
                        outs.add(new ActionList());
                        continue;
                    }
                    outs.add(new ActionList(alist.subList(adr2ip(alist, endAddr), adr2ip(alist, endAddr + size))));
                    endAddr += size;
                }

                for (ActionList al : outs) {
                    subgraphs.put("loc" + Helper.formatAddress(code.pos2adr(ip)) + ": function " + functionName,
                            new ActionGraph("", false, al, new HashMap<>(), new HashMap<>(), new HashMap<>(), SWF.DEFAULT_VERSION)
                    );
                }
            }
            ip++;
        }
        return subgraphs;
    }

    public boolean isInsideDoInitAction() {
        return insideDoInitAction;
    }

    @Override
    protected void afterPopupateAllParts(Set<GraphPart> allParts) {

    }

    public static List<GraphTargetItem> translateViaGraph(boolean insideDoInitAction, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, List<Action> code, int version, int staticOperation, String path) throws InterruptedException {
        ActionGraph g = new ActionGraph(path, insideDoInitAction, code, registerNames, variables, functions, version);
        ActionLocalData localData = new ActionLocalData(insideDoInitAction, registerNames);
        g.init(localData);
        return g.translate(localData, staticOperation, path);
    }

    @Override
    public void finalProcessStack(TranslateStack stack, List<GraphTargetItem> output, String path) {
        if (stack.size() > 0) {
            for (int i = stack.size() - 1; i >= 0; i--) {
                //System.err.println(stack.get(i));
                if (stack.get(i) instanceof FunctionActionItem) {
                    FunctionActionItem f = (FunctionActionItem) stack.remove(i);
                    if (!output.contains(f)) {
                        output.add(0, f);
                    }
                }
            }
        }
    }

    @Override
    protected boolean canBeBreakCandidate(GraphPart part) {
        if (part.refs.size() <= 1) {
            return true;
        }
        boolean isSwitch = true;
        for (GraphPart r : part.refs) {
            if (code.get(r.end) instanceof ActionIf) {
                if (!(r.start < r.end - 1 && (code.get(r.end - 1) instanceof ActionStrictEquals))) {
                    isSwitch = false;
                }
            } else {
                isSwitch = false;
            }
        }
        return !isSwitch;
    }

    @Override
    protected void finalProcess(List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) throws InterruptedException {

        if (insideDoInitAction) {
            ActionScript2ClassDetector detector = new ActionScript2ClassDetector();
            detector.checkClass(list, path);
        }
        int targetStart;
        int targetEnd;

        boolean again;
        do {
            again = false;
            targetStart = -1;
            targetEnd = -1;
            GraphTargetItem targetStartItem = null;
            GraphTargetItem target = null;
            for (int t = 0; t < list.size(); t++) {
                GraphTargetItem it = list.get(t);
                if (it instanceof SetTargetActionItem) {
                    SetTargetActionItem st = (SetTargetActionItem) it;
                    if (st.target.isEmpty()) {
                        if (targetStart > -1) {
                            targetEnd = t;
                            break;
                        }
                    } else {
                        target = new DirectValueActionItem(null, null, 0, st.target, new ArrayList<>());
                        targetStart = t;
                        targetStartItem = it;
                    }
                }
                if (it instanceof SetTarget2ActionItem) {
                    SetTarget2ActionItem st = (SetTarget2ActionItem) it;
                    if ((st.target instanceof DirectValueActionItem) && st.target.getResult().equals("")) {
                        if (targetStart > -1) {
                            targetEnd = t;
                            break;
                        }
                    } else {
                        targetStart = t;
                        target = st.target;
                        targetStartItem = it;
                    }
                }
            }
            if ((targetStart > -1) && (targetEnd > -1) && targetStartItem != null) {
                List<GraphTargetItem> newlist = new ArrayList<>();
                for (int i = 0; i < targetStart; i++) {
                    newlist.add(list.get(i));
                }
                List<GraphTargetItem> tellist = new ArrayList<>();
                for (int i = targetStart + 1; i < targetEnd; i++) {
                    tellist.add(list.get(i));
                }
                newlist.add(new TellTargetActionItem(targetStartItem.getSrc(), targetStartItem.getLineStartItem(), target, tellist));
                for (int i = targetEnd + 1; i < list.size(); i++) {
                    newlist.add(list.get(i));
                }
                list.clear();
                list.addAll(newlist);
                again = true;
            }
        } while (again);
        for (int t = 1/*not first*/; t < list.size(); t++) {
            GraphTargetItem it = list.get(t);
            List<GraphTargetItem> checkedBody = null;
            GraphTargetItem checkedCondition = null;
            Loop checkedLoop = null;
            if (it instanceof WhileItem) {
                WhileItem wi = (WhileItem) it;
                checkedBody = wi.commands;
                checkedCondition = wi.expression.get(wi.expression.size() - 1);
                checkedLoop = wi.loop;
            } else if (it instanceof IfItem) {
                IfItem ifi = (IfItem) it;
                if (ifi.onFalse.isEmpty()) {
                    checkedBody = ifi.onTrue;
                    checkedCondition = ifi.expression;
                    checkedLoop = null;
                }
            }
            if (checkedBody != null && (!checkedBody.isEmpty()) && (checkedBody.get(0) instanceof SetTypeActionItem)) {
                SetTypeActionItem sti = (SetTypeActionItem) checkedBody.get(0);
                if (checkedCondition instanceof NeqActionItem) {
                    NeqActionItem ne = (NeqActionItem) checkedCondition;
                    if (ne.rightSide instanceof DirectValueActionItem) {
                        DirectValueActionItem dv = (DirectValueActionItem) ne.rightSide;
                        if (dv.value == Null.INSTANCE) {
                            GraphTargetItem en = list.get(t - 1);
                            if (en instanceof EnumerateActionItem) {
                                EnumerateActionItem eti = (EnumerateActionItem) en;
                                list.remove(t);
                                checkedBody.remove(0);
                                if (checkedLoop == null) {
                                    checkedLoop = new Loop(localData.loops.size(), null, null);
                                    checkedBody.add(new BreakItem(null, null, checkedLoop.id));
                                }
                                list.add(t, new ForInActionItem(null, null, checkedLoop, sti.getObject(), eti.object, checkedBody));
                                list.remove(t - 1);
                                t--;
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
    protected List<GraphPart> checkPrecoNextParts(GraphPart part) {
        List<GraphSourceItem> items = getPartItems(part);
        part = makeMultiPart(part);
        if (items.size() > 1) {
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
                        boolean proceed;
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
    protected List<GraphTargetItem> check(Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<Loop> loops, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) throws InterruptedException {
        if (!output.isEmpty()) {
            if (output.get(output.size() - 1) instanceof StoreRegisterActionItem) {
                StoreRegisterActionItem str = (StoreRegisterActionItem) output.get(output.size() - 1);
                if (str.value instanceof EnumerateActionItem) {
                    output.remove(output.size() - 1);
                }
            }
        }
        List<GraphTargetItem> ret = null;
        if ((part.nextParts.size() == 2) && (!stack.isEmpty()) && (stack.peek() instanceof StrictEqActionItem)) {
            GraphSourceItem switchStartItem = code.get(part.start);

            GraphTargetItem switchedObject = null;
            if (!output.isEmpty()) {
                if (output.get(output.size() - 1) instanceof StoreRegisterActionItem) {
                    switchedObject = ((StoreRegisterActionItem) output.get(output.size() - 1)).value;
                }
            }
            if (switchedObject == null) {
                switchedObject = new DirectValueActionItem(null, null, -1, Null.INSTANCE, null);
            }
            List<GraphTargetItem> caseValuesMap = new ArrayList<>();

            //int pos = 0;
            StrictEqActionItem set = (StrictEqActionItem) stack.pop();
            caseValuesMap.add(set.rightSide);
            if (set.leftSide instanceof StoreRegisterActionItem) {
                switchedObject = ((StoreRegisterActionItem) set.leftSide).value;
            }
            //GraphPart switchLoc = part.nextParts.get(1).nextParts.get(0);
            List<GraphPart> caseBodyParts = new ArrayList<>();
            caseBodyParts.add(part.nextParts.get(0));
            GraphTargetItem top = null;
            int cnt = 1;
            while (part.nextParts.size() > 1
                    && part.nextParts.get(1).getHeight() > 1
                    && code.get(part.nextParts.get(1).end >= code.size() ? code.size() - 1 : part.nextParts.get(1).end) instanceof ActionIf
                    && ((top = translatePartGetStack(localData, part.nextParts.get(1), stack, staticOperation)) instanceof StrictEqActionItem)) {
                cnt++;
                part = part.nextParts.get(1);
                caseBodyParts.add(part.nextParts.get(0));

                set = (StrictEqActionItem) top;
                caseValuesMap.add(set.rightSide);
            }
            if (cnt == 1) {
                stack.push(set);
            } else {
                part = part.nextParts.get(1);
                GraphPart defaultPart = part;
                if (code.size() > defaultPart.start && code.get(defaultPart.start) instanceof ActionJump) {
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
                        if (caseBodyParts.get(i).leadsTo(localData, this, code, defaultPart, loops)) {
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
                        if (defaultPart.leadsTo(localData, this, code, caseBodyParts.get(i), loops)) {
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

                GraphPart breakPart = getMostCommonPart(localData, caseBodyParts, loops);
                List<List<GraphTargetItem>> caseCommands = new ArrayList<>();
                GraphPart next = breakPart;

                GraphTargetItem ti = checkLoop(next, stopPart, loops);

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
                            if (!caseBodies.get(i).leadsTo(localData, this, code, caseBodies.get(i + 1), loops)) {
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
                    currentCaseCommands.addAll(0, printGraph(partCodes, partCodePos, localData, stack, allParts, null, caseBodies.get(i), stopPart2x, loops, staticOperation, path));
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
                        ret.addAll(printGraph(partCodes, partCodePos, localData, stack, allParts, null, next, stopPart, loops, staticOperation, path));
                    }
                }
            }
        }
        return ret;
    }

    @Override
    protected int checkIp(int ip) {
        int oldIp = ip;
        //return/break in for..in
        GraphSourceItem action = code.get(ip);
        if ((action instanceof ActionPush) && (((ActionPush) action).values.size() == 1) && (((ActionPush) action).values.get(0) == Null.INSTANCE)) {
            if (ip + 3 <= code.size()) {
                if ((code.get(ip + 1) instanceof ActionEquals) || (code.get(ip + 1) instanceof ActionEquals2)) {
                    if (code.get(ip + 2) instanceof ActionNot) {
                        if (code.get(ip + 3) instanceof ActionIf) {
                            ActionIf aif = (ActionIf) code.get(ip + 3);
                            if (code.adr2pos(code.pos2adr(ip + 3) + 5 /*IF numbytes*/ + aif.getJumpOffset()) == ip) {
                                ip += 4;
                            }
                        }
                    }
                }
            }
        }
        if (oldIp != ip) {
            if (ip == code.size()) { //no next checkIp call since its after code size
                return ip;
            }
            return checkIp(ip);
        }
        return ip;
    }
}

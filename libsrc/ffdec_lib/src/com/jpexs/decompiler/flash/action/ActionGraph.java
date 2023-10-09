/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.action.as2.ActionScript2ClassDetector;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.EnumerateActionItem;
import com.jpexs.decompiler.flash.action.model.EnumeratedValueActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.SetTarget2ActionItem;
import com.jpexs.decompiler.flash.action.model.SetTargetActionItem;
import com.jpexs.decompiler.flash.action.model.SetTypeActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.TemporaryRegisterMark;
import com.jpexs.decompiler.flash.action.model.clauses.ForInActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.TellTargetActionItem;
import com.jpexs.decompiler.flash.action.model.operations.NeqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StrictEqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StrictNeqActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphPartChangeException;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.StopPartKind;
import com.jpexs.decompiler.graph.ThrowState;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    private boolean insideFunction;
    private Map<String, Map<String, Trait>> uninitializedClassTraits;

    public ActionGraph(Map<String, Map<String, Trait>> uninitializedClassTraits, String path, boolean insideDoInitAction, boolean insideFunction, List<Action> code, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int version, String charset) {
        super(new ActionGraphSource(path, insideDoInitAction, code, version, registerNames, variables, functions, charset), new ArrayList<>());
        this.uninitializedClassTraits = uninitializedClassTraits;
        this.insideDoInitAction = insideDoInitAction;
        this.insideFunction = insideFunction;
    }

    public Map<String, Map<String, Trait>> getUninitializedClassTraits() {
        return uninitializedClassTraits;
    }

    @Override
    public ActionGraphSource getGraphCode() {
        return (ActionGraphSource) code;
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
                        outs.add(new ActionList(((ActionGraphSource) code).getCharset()));
                        continue;
                    }
                    outs.add(new ActionList(alist.subList(Action.adr2ip(alist, endAddr), Action.adr2ip(alist, endAddr + size)), getGraphCode().getCharset()));
                    endAddr += size;
                }

                for (ActionList al : outs) {
                    subgraphs.put("loc" + Helper.formatAddress(code.pos2adr(ip)) + ": function " + functionName,
                            new ActionGraph(uninitializedClassTraits, "", false, false, al, new HashMap<>(), new HashMap<>(), new HashMap<>(), SWF.DEFAULT_VERSION, ((ActionGraphSource) getGraphCode()).getCharset())
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

    public static List<GraphTargetItem> translateViaGraph(Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, boolean insideFunction, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, List<Action> code, int version, int staticOperation, String path, String charset) throws InterruptedException {
        ActionGraph g = new ActionGraph(uninitializedClassTraits, path, insideDoInitAction, insideFunction, code, registerNames, variables, functions, version, charset);
        ActionLocalData localData = new ActionLocalData(secondPassData, insideDoInitAction, registerNames, uninitializedClassTraits);
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
    protected boolean canBeBreakCandidate(BaseLocalData localData, GraphPart part, List<ThrowState> throwStates) {
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

        if (level == 0) {
            List<GraphTargetItem> removed = new ArrayList<>();
            for (int i = list.size() - 1; i >= 0; i--) {
                if (list.get(i) instanceof ScriptEndItem) {
                    continue;
                }
                if (list.get(i) instanceof FunctionActionItem) {
                    removed.add(0, list.remove(i));
                } else {
                    break;
                }
            }
            list.addAll(0, removed);
        }

        int targetStart;
        int targetEnd;
        GraphTargetItem targetStartItem = null;

        boolean again;
        do {
            again = false;
            targetStart = -1;
            targetEnd = -1;
            targetStartItem = null;
            GraphTargetItem target = null;
            for (int t = 0; t < list.size(); t++) {
                GraphTargetItem it = list.get(t);
                if (it instanceof PushItem) {
                    PushItem pi = (PushItem) it;
                    if (pi.value instanceof GetPropertyActionItem) {
                        GetPropertyActionItem gp = (GetPropertyActionItem) pi.value;
                        if (gp.propertyIndex == 11 /*_target*/) {
                            list.remove(t);
                            t--;
                            continue;
                        }
                    }
                }
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
                    if (st.target instanceof PopItem) {
                        list.remove(t);
                        t--;
                        continue;
                    }
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

            if (targetStart > -1 && targetEnd == -1) {
                targetEnd = list.size();
                if (list.get(list.size() - 1) instanceof ScriptEndItem) {
                    targetEnd--;
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
                //TODO: maybe set nested flag
                for (int i = targetEnd + 1; i < list.size(); i++) {
                    newlist.add(list.get(i));
                }
                list.clear();
                list.addAll(newlist);
                again = true;
            }
        } while (again);

        targetStart = -1;
        targetEnd = -1;
        GraphTargetItem target = null;

        //process empty telltargets
        for (int t = 0; t < list.size(); t++) {
            GraphTargetItem it = list.get(t);

            if (it instanceof SetTargetActionItem) {
                SetTargetActionItem st = (SetTargetActionItem) it;
                if (st.target.isEmpty()) {
                    if (targetStart > -1) {
                        targetEnd = t;
                    } else {
                        targetStart = t;
                        targetStartItem = st;
                        target = new DirectValueActionItem(null, null, 0, st.target, new ArrayList<>());
                    }
                }
            }
            if (it instanceof SetTarget2ActionItem) {
                SetTarget2ActionItem st = (SetTarget2ActionItem) it;
                if ((st.target instanceof DirectValueActionItem) && st.target.getResult().equals("")) {
                    if (targetStart > -1) {
                        targetEnd = t;
                    } else {
                        targetStart = t;
                        targetStartItem = st;
                        target = st.target;
                    }
                }
            }

            if (targetStart > -1 && targetEnd > -1) {
                List<GraphTargetItem> newlist = new ArrayList<>();
                for (int i = 0; i < targetStart; i++) {
                    newlist.add(list.get(i));
                }
                List<GraphTargetItem> tellist = new ArrayList<>();
                for (int i = targetStart + 1; i < targetEnd; i++) {
                    tellist.add(list.get(i));
                }
                newlist.add(new TellTargetActionItem(targetStartItem.getSrc(), targetStartItem.getLineStartItem(), target, tellist));
                //TODO: maybe set nested flag
                for (int i = targetEnd + 1; i < list.size(); i++) {
                    newlist.add(list.get(i));
                }
                list.clear();
                list.addAll(newlist);
                targetStart = -1;
                targetEnd = -1;
                target = null;
                t = 0;
            }
        }
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

            if (checkedCondition instanceof NeqActionItem) {
                NeqActionItem ne = (NeqActionItem) checkedCondition;
                if (ne.rightSide instanceof DirectValueActionItem) {
                    DirectValueActionItem dv = (DirectValueActionItem) ne.rightSide;
                    if (dv.value == Null.INSTANCE) {
                        GraphTargetItem en = list.get(t - 1);
                        if (en instanceof EnumerateActionItem) {
                            EnumerateActionItem eti = (EnumerateActionItem) en;
                            if (checkedCondition instanceof NeqActionItem) {
                                NeqActionItem neq = (NeqActionItem) checkedCondition;
                                if (neq.leftSide instanceof StoreRegisterActionItem) {
                                    if (checkedBody != null && (!checkedBody.isEmpty()) && (checkedBody.get(0) instanceof SetTypeActionItem)) {
                                        SetTypeActionItem sti = (SetTypeActionItem) checkedBody.get(0);

                                        if ((sti.getValue() instanceof DirectValueActionItem) && (((DirectValueActionItem) sti.getValue()).value instanceof RegisterNumber)) {
                                            if ((neq.rightSide instanceof DirectValueActionItem) && (((DirectValueActionItem) neq.rightSide).value instanceof Null)) {
                                                if (neq.leftSide.value instanceof EnumeratedValueActionItem) {
                                                    if (((StoreRegisterActionItem) neq.leftSide).register.number == ((RegisterNumber) (((DirectValueActionItem) sti.getValue()).value)).number) {
                                                        list.remove(t);
                                                        checkedBody.remove(0);
                                                        if (checkedLoop == null) {
                                                            checkedLoop = new Loop(localData.loops.size(), null, null);
                                                            checkedBody.add(new BreakItem(null, null, checkedLoop.id));
                                                        }
                                                        sti.setValue(new DirectValueActionItem(Null.INSTANCE));
                                                        list.add(t, new ForInActionItem(null, null, checkedLoop, (GraphTargetItem) sti, eti.object, checkedBody));
                                                        //sti.getObject()
                                                        list.remove(t - 1);
                                                        t--;
                                                        continue;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (checkedBody != null) {
                                        list.remove(t);
                                        if (checkedLoop == null) {
                                            checkedLoop = new Loop(localData.loops.size(), null, null);
                                            checkedBody.add(new BreakItem(null, null, checkedLoop.id));
                                        }
                                        list.remove(t - 1);
                                        t--;
                                        if (eti.object instanceof SetTypeActionItem) {
                                            list.add(t++, eti.object);
                                            eti.object = ((SetTypeActionItem) eti.object).getObject();
                                        }
                                        list.add(t, new ForInActionItem(null, null, checkedLoop, (GraphTargetItem) neq.leftSide, eti.object, checkedBody));
                                        if (t + 1 < list.size()) {
                                            if (list.get(t + 1) instanceof EnumeratedValueActionItem) {
                                                list.remove(t + 1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
        //Handle for loops at the end:
        super.finalProcess(list, level, localData, path);

    }

    public void makeAllCommands(List<GraphTargetItem> commands, TranslateStack stack) {
        GraphTargetItem enumerate = null;
        if (!commands.isEmpty() && (commands.get(commands.size() - 1) instanceof EnumerateActionItem)) {
            enumerate = commands.remove(commands.size() - 1);
        }
        super.makeAllCommands(commands, stack);
        //ags.getVariables()
        ActionGraphSource ags = (ActionGraphSource) code;
        for (String varName : ags.getVariables().keySet()) {
            if (varName.matches("^__register.*")) {
                GraphTargetItem varValue = ags.getVariables().get(varName);
                if (varValue instanceof TemporaryRegister) {
                    TemporaryRegister tempReg = (TemporaryRegister) varValue;

                    if (!tempReg.used) {
                        if (varValue.value instanceof SetTypeActionItem) {
                            SetTypeActionItem st = (SetTypeActionItem) varValue.value;
                            for (int i = 0; i < commands.size(); i++) {
                                if (commands.get(i) instanceof TemporaryRegisterMark) {
                                    TemporaryRegisterMark trm = (TemporaryRegisterMark) commands.get(i);
                                    if (st.getValue() instanceof StoreRegisterActionItem) {
                                        StoreRegisterActionItem sr = (StoreRegisterActionItem) st.getValue();
                                        if (sr.register.number == tempReg.getRegId()) {
                                            sr.temporary = false;
                                        }
                                    }
                                    if (trm.tempReg == tempReg) {
                                        commands.set(i, (GraphTargetItem) st);
                                        ags.getVariables().put(varName, null);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    //System.err.println(varValue.value.getClass().getSimpleName());
                }
            }
        }

        if (enumerate != null) {
            commands.add(enumerate);
        }
    }

    @Override
    public List<GraphTargetItem> translate(BaseLocalData localData, int staticOperation, String path) throws InterruptedException {
        List<GraphTargetItem> ret = super.translate(localData, staticOperation, path);
        if (insideDoInitAction && !insideFunction) {
            ActionScript2ClassDetector detector = new ActionScript2ClassDetector();
            detector.checkClass(uninitializedClassTraits, ret, ((ActionGraphSource) code).getVariables(), path);
        }
        makeDefineRegistersUp(ret);
        return ret;
    }


    /*
      This makes declarations of registers on one level up when inside some
      structure. 
       Example : 
        if((var loc4 = random()) > 5) {
            trace("x");
        } 
        => 
        var loc4 = null;
        if ((loc4 = random()) > 5)
        {
            trace("x");
        }
     
     
     */
    private void makeDefineRegistersUp(List<GraphTargetItem> list) {
        for (int i = 0; i < list.size(); i++) {
            final int fi = i;
            GraphTargetItem ti = list.get(i);

            if (ti instanceof TemporaryRegister) {
                continue;
            }

            Set<GraphTargetItem> visitedItems = new HashSet<>();
            ti.visitNoBlock(new AbstractGraphTargetVisitor() {
                @Override
                public void visit(GraphTargetItem item) {
                    if (item != null && !visitedItems.contains(item)) {
                        visitedItems.add(item);

                        if (item instanceof TemporaryRegister) {
                            return;
                        }
                        //can has definition in for in...
                        if ((ti instanceof ForInActionItem) && (item == ((ForInActionItem) ti).variableName)) {
                            return;
                        }
                        if (item instanceof StoreRegisterActionItem) {
                            StoreRegisterActionItem sr = (StoreRegisterActionItem) item;
                            if (sr.define) {
                                list.add(fi, new StoreRegisterActionItem(null, null, sr.register, new DirectValueActionItem(Null.INSTANCE), true));
                                sr.define = false;
                            }
                        }

                        item.visitNoBlock(this);
                    }
                }
            });
            if (ti instanceof Block) {
                Block b = (Block) ti;
                for (List<GraphTargetItem> items : b.getSubs()) {
                    makeDefineRegistersUp(items);
                }
            }
        }
    }

    private GraphPart findPart(int ip, Set<GraphPart> allParts) {
        for (GraphPart p : allParts) {
            if (p.containsIP(ip)) {
                return p;
            }
        }
        return null;
    }

    @Override
    protected List<GraphTargetItem> check(List<GraphTargetItem> currentRet, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) throws InterruptedException {
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
                //switchedObject = new DirectValueActionItem(null, null, -1, Null.INSTANCE, null);
            }
            List<GraphTargetItem> caseValuesMap = new ArrayList<>();

            StrictEqActionItem set = (StrictEqActionItem) stack.pop();
            caseValuesMap.add(set.rightSide);
            if (set.leftSide instanceof StoreRegisterActionItem) {
                switchedObject = ((StoreRegisterActionItem) set.leftSide).value;
            } else {
                switchedObject = set.leftSide;
            }

            if (switchedObject == null) {
                stack.push(set);
                return ret;
            }
            List<GraphPart> caseBodyParts = new ArrayList<>();
            caseBodyParts.add(part.nextParts.get(0));
            GraphTargetItem top = null;

            ActionSecondPassData secondPassData = (ActionSecondPassData) localData.secondPassData;
            boolean secondSwitchFound = false;
            if (secondPassData != null) {
                for (int si = 0; si < secondPassData.switchParts.size(); si++) {
                    if (secondPassData.switchParts.get(si).get(0).equals(part)) {
                        //we need to use findpart as parts have changed between first and second pass
                        part = findPart(secondPassData.switchParts.get(si).get(secondPassData.switchParts.get(si).size() - 1).start, allParts);
                        caseBodyParts.clear();
                        for (GraphPart p : secondPassData.switchOnFalseParts.get(si)) {
                            caseBodyParts.add(findPart(p.start, allParts));
                        }
                        caseValuesMap.clear();
                        caseValuesMap.addAll(secondPassData.switchCaseExpressions.get(si));
                        secondSwitchFound = true;
                    }
                }
            }
            int cnt = 1;
            if (false) { //always do secondPass
                try {
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
                } catch (GraphPartChangeException gce) {
                    //ignore
                }
            }
            if (!secondSwitchFound && cnt == 1) {
                stack.push(set);
            } else {
                part = part.nextParts.get(1);
                GraphPart defaultPart = part;
                if (code.size() > defaultPart.start && code.get(defaultPart.start) instanceof ActionJump
                        && defaultPart.refs.size() == 1
                        && !partIsLoopContBrePre(defaultPart, loops, throwStates)) {
                    defaultPart = defaultPart.nextParts.get(0);
                }

                Reference<GraphPart> nextRef = new Reference<>(null);
                Reference<GraphTargetItem> tiRef = new Reference<>(null);
                SwitchItem sw = handleSwitch(switchedObject, switchStartItem, foundGotos, partCodes, partCodePos, visited, allParts, stack, stopPart, stopPartKind, loops, throwStates, localData, staticOperation, path, caseValuesMap, defaultPart, caseBodyParts, nextRef, tiRef);
                ret = new ArrayList<>();
                ret.addAll(output);
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

    @Override
    public SecondPassData prepareSecondPass(List<GraphTargetItem> list) {
        ActionSecondPassData spd = new ActionSecondPassData();
        Set<GraphPart> processedIfs = new HashSet<>();
        checkSecondPassSwitches(processedIfs, list, spd.switchParts, spd.switchOnFalseParts, spd.switchCaseExpressions);

        if (spd.switchParts.isEmpty()) {
            return null; //no need to second pass
        }
        return spd;
    }

    private void checkSecondPassSwitches(Set<GraphPart> processedIfs, List<GraphTargetItem> list, List<List<GraphPart>> allSwitchParts, List<List<GraphPart>> allSwitchOnFalseParts, List<List<GraphTargetItem>> allSwitchExpressions) {
        for (GraphTargetItem item : list) {
            if (item instanceof IfItem) {
                IfItem ii = (IfItem) item;
                boolean isNeq = true;
                if (!processedIfs.contains(ii.decisionPart)) {
                    if ((ii.expression instanceof StrictNeqActionItem) || (ii.expression instanceof StrictEqActionItem)) {
                        isNeq = (ii.expression instanceof StrictNeqActionItem);

                        List<GraphPart> switchParts = new ArrayList<>();
                        List<GraphTargetItem> switchExpressions = new ArrayList<>();
                        List<GraphPart> switchOnFalseParts = new ArrayList<>();
                        BinaryOpItem sneq = (BinaryOpItem) ii.expression;
                        if (true) {
                            /*(sneq.leftSide instanceof StoreRegisterActionItem)
                            || (sneq.leftSide instanceof GetVariableActionItem)
                            || (sneq.leftSide instanceof GetMemberActionItem)
                            ) {*/

                            int regId = -1;
                            GraphTargetItem svar = null;
                            if (sneq.leftSide instanceof StoreRegisterActionItem) {
                                StoreRegisterActionItem sr = (StoreRegisterActionItem) sneq.leftSide;
                                regId = sr.register.number;
                            } else {
                                svar = sneq.leftSide;
                            }

                            switchParts.add(ii.decisionPart);
                            switchExpressions.add(sneq.rightSide);
                            switchOnFalseParts.add(ii.onTruePart);

                            IfItem ii2 = ii;
                            IfItem lastOkayIi = ii;
                            while (true) {
                                if ((isNeq && (!ii2.onTrue.isEmpty() && (ii2.onTrue.get(0) instanceof IfItem)))
                                        || (!isNeq && (!ii2.onFalse.isEmpty() && (ii2.onFalse.get(0) instanceof IfItem)))) {
                                    ii2 = (IfItem) (isNeq ? ii2.onTrue.get(0) : ii2.onFalse.get(0));
                                    if ((ii2.expression instanceof StrictNeqActionItem) || (ii2.expression instanceof StrictEqActionItem)) {
                                        isNeq = (ii2.expression instanceof StrictNeqActionItem);
                                        sneq = ((BinaryOpItem) ii2.expression);
                                        if (sneq.leftSide instanceof DirectValueActionItem) {
                                            DirectValueActionItem dv = (DirectValueActionItem) sneq.leftSide;
                                            if (dv.value instanceof RegisterNumber) {
                                                RegisterNumber rn = (RegisterNumber) dv.value;
                                                if (rn.number == regId) {
                                                    processedIfs.add(ii.decisionPart);
                                                    processedIfs.add(ii2.decisionPart);
                                                    switchParts.add(ii2.decisionPart);
                                                    switchOnFalseParts.add(ii2.onTruePart);
                                                    switchExpressions.add(sneq.rightSide);
                                                    lastOkayIi = ii2;
                                                } else {
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        } else if (svar != null) {
                                            if (sneq.leftSide.valueEquals(svar)) {
                                                processedIfs.add(ii.decisionPart);
                                                processedIfs.add(ii2.decisionPart);
                                                switchParts.add(ii2.decisionPart);
                                                switchOnFalseParts.add(ii2.onTruePart);
                                                switchExpressions.add(sneq.rightSide);
                                                lastOkayIi = ii2;
                                            } else {
                                                break;
                                            }
                                        } else {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }

                            if (switchParts.size() > 1) {
                                allSwitchParts.add(switchParts);
                                allSwitchOnFalseParts.add(switchOnFalseParts);
                                allSwitchExpressions.add(switchExpressions);
                            }
                        }
                    }
                }
            }
            if ((item instanceof Block)) {
                for (List<GraphTargetItem> sub : ((Block) item).getSubs()) {
                    checkSecondPassSwitches(processedIfs, sub, allSwitchParts, allSwitchOnFalseParts, allSwitchExpressions);
                }
            }
        }
    }
}

/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.AppResources;
import com.jpexs.decompiler.flash.BaseLocalData;
import com.jpexs.decompiler.flash.FinalProcessLocalData;
import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.action.as2.ActionScript2ClassDetector;
import com.jpexs.decompiler.flash.action.as2.Trait;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.EnumerateActionItem;
import com.jpexs.decompiler.flash.action.model.EnumeratedValueActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetPropertyActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetTarget2ActionItem;
import com.jpexs.decompiler.flash.action.model.SetTargetActionItem;
import com.jpexs.decompiler.flash.action.model.SetTypeActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StoreRegisterActionItem;
import com.jpexs.decompiler.flash.action.model.TemporaryRegister;
import com.jpexs.decompiler.flash.action.model.TemporaryRegisterMark;
import com.jpexs.decompiler.flash.action.model.clauses.ForInActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.IfFrameLoadedActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.TellTargetActionItem;
import com.jpexs.decompiler.flash.action.model.operations.EqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.NeqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StrictEqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StrictNeqActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionEnumerate;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf6.ActionStrictEquals;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.AbstractGraphTargetVisitor;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphPartChangeException;
import com.jpexs.decompiler.graph.GraphSource;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SecondPassData;
import com.jpexs.decompiler.graph.SecondPassException;
import com.jpexs.decompiler.graph.StopPartKind;
import com.jpexs.decompiler.graph.ThrowState;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.CommentItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.ExitItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.GotoItem;
import com.jpexs.decompiler.graph.model.HasTempIndex;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LabelItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.PopItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.ScriptEndItem;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TemporaryItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.UniversalLoopItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.LinkedIdentityHashSet;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ActionScript 1/2 graph
 *
 * @author JPEXS
 */
public class ActionGraph extends Graph {

    /**
     * Labels whose unreachable goto was removed.
     */
    private final Set<String> labelsOfRemovedGotos = new HashSet<>();

    private ActionRegisterUsage registerUsage;

    /**
     * Inside DoInitAction
     */
    private boolean insideDoInitAction;

    /**
     * Inside function
     */
    private boolean insideFunction;

    /**
     * Needs uninitialized class fields detection
     */
    private final boolean needsUninitializedClassFieldsDetection;

    /**
     * Uninitialized class traits - maps class name to map of trait name to
     * trait
     */
    private Map<String, Map<String, Trait>> uninitializedClassTraits;

    /**
     * Constructs ActionGraph
     *
     * @param needsUninitializedClassFieldsDetection Needs uninitialized class
     * fields detection
     * @param uninitializedClassTraits Uninitialized class traits
     * @param path Path
     * @param insideDoInitAction Inside DoInitAction
     * @param insideFunction Inside function
     * @param code Code
     * @param registerNames Register names
     * @param variables Variables
     * @param functions Functions
     * @param version Version
     * @param charset Charset
     * @param startIp Start IP
     */
    public ActionGraph(boolean needsUninitializedClassFieldsDetection, Map<String, Map<String, Trait>> uninitializedClassTraits, String path, boolean insideDoInitAction, boolean insideFunction, List<Action> code, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, int version, String charset, int startIp) {
        super(ActionGraphTargetDialect.INSTANCE, new ActionGraphSource(path, insideDoInitAction, code, version, registerNames, variables, functions, charset, startIp), new ArrayList<>(), startIp);
        this.needsUninitializedClassFieldsDetection = needsUninitializedClassFieldsDetection;
        this.uninitializedClassTraits = uninitializedClassTraits;
        this.insideDoInitAction = insideDoInitAction;
        this.insideFunction = insideFunction;
    }

    public boolean doesNeedUninitializedClassFieldsDetection() {
        return needsUninitializedClassFieldsDetection;
    }

    /**
     * Get uninitialized class traits
     *
     * @return Uninitialized class traits - maps class name to map of trait name
     * to trait
     */
    public Map<String, Map<String, Trait>> getUninitializedClassTraits() {
        return uninitializedClassTraits;
    }

    /**
     * Gets the graphSource
     *
     * @return GraphSource
     */
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
                List<Integer> startIps = new ArrayList<>();
                for (long size : cnt.getContainerSizes()) {
                    if (size == 0) {
                        outs.add(new ActionList(((ActionGraphSource) code).getCharset(), ((ActionGraphSource) code).version));
                        startIps.add(0);
                        continue;
                    }
                    int startIp = Action.adr2ip(alist, endAddr);
                    startIps.add(startIp);
                    outs.add(new ActionList(alist.subList(0, Action.adr2ip(alist, endAddr + size)), getGraphCode().getCharset()));
                    endAddr += size;
                }

                for (int i = 0; i < outs.size(); i++) {
                    ActionList al = outs.get(i);
                    int startIp = startIps.get(i);
                    subgraphs.put("loc" + Helper.formatAddress(code.pos2adr(ip)) + ": function " + functionName,
                            new ActionGraph(needsUninitializedClassFieldsDetection, uninitializedClassTraits, "", false, false, al, new HashMap<>(), new HashMap<>(), new HashMap<>(), ((ActionGraphSource) code).version, ((ActionGraphSource) getGraphCode()).getCharset(), startIp)
                    );
                }
            }
            ip++;
        }
        return subgraphs;
    }

    /**
     * Checks whether is inside DoInitAction.
     *
     * @return True if is inside DoInitAction, false otherwise
     */
    public boolean isInsideDoInitAction() {
        return insideDoInitAction;
    }

    /**
     * Method called after populating all parts.
     *
     * @param allParts All parts
     */
    @Override
    protected void afterPopulateAllParts(Set<GraphPart> allParts) {

    }

    @Override
    protected void beforeGetLoops(BaseLocalData localData, String path, Set<GraphPart> allParts, List<ThrowState> throwStates) throws InterruptedException {
        if (registerUsage == null) {
            registerUsage = new ActionRegisterUsage(getGraphCode(), startIp);
        }
    }

    /**
     * Gets register usage shared with with/try subgraphs of this scope.
     *
     * @return Register usage
     */
    public ActionRegisterUsage getRegisterUsage() {
        return registerUsage;
    }

    /**
     * Translates via Graph - decompiles.
     *
     * @param usedDeobfuscations Used deobfuscations
     * @param needsUninitializedClassFieldsDetection Needs uninitialized class
     * fields detection
     * @param uninitializedClassTraits Uninitialized class traits
     * @param secondPassData Second pass data
     * @param insideDoInitAction Inside DoInitAction
     * @param insideFunction Inside function
     * @param registerNames Register names
     * @param variables Variables
     * @param functions Functions
     * @param code Code
     * @param version Version
     * @param staticOperation Unused
     * @param path Path
     * @param charset Charset
     * @return List of graph target items
     * @throws InterruptedException On interrupt
     */
    public static List<GraphTargetItem> translateViaGraph(Set<String> usedDeobfuscations, boolean needsUninitializedClassFieldsDetection, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, boolean insideFunction, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, List<Action> code, int version, int staticOperation, String path, String charset, int startIp) throws InterruptedException {
        return translateViaGraph(usedDeobfuscations, needsUninitializedClassFieldsDetection, uninitializedClassTraits, secondPassData,
                insideDoInitAction, insideFunction, registerNames, variables, functions, code, version, staticOperation, path, charset, startIp, null);
    }

    /**
     * Translates a graph, optionally sharing register usage of its enclosing
     * scope when translating a with/try section.
     *
     * @param usedDeobfuscations Used deobfuscations
     * @param needsUninitializedClassFieldsDetection Needs uninitialized class fields detection
     * @param uninitializedClassTraits Uninitialized class traits
     * @param secondPassData Second pass data
     * @param insideDoInitAction Is inside DoInitAction
     * @param insideFunction Is inside a function or container
     * @param registerNames Register names
     * @param variables Variables
     * @param functions Functions
     * @param code Code
     * @param version SWF version
     * @param staticOperation Static operation
     * @param path Script path
     * @param charset Charset
     * @param startIp Start IP
     * @param registerUsage Register usage of the complete enclosing scope
     * @return Translated items
     * @throws InterruptedException On interruption
     */
    public static List<GraphTargetItem> translateViaGraph(Set<String> usedDeobfuscations, boolean needsUninitializedClassFieldsDetection, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, boolean insideFunction, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, List<Action> code, int version, int staticOperation, String path, String charset, int startIp, ActionRegisterUsage registerUsage) throws InterruptedException {
        ActionSecondPassContext context = ActionSecondPassContext.current();
        if (context != null && secondPassData == null) {
            ActionSecondPassContext.GraphKey key = context.getKey(code, startIp, path);
            if (context.isCollecting()) {
                try {
                    return translateViaGraphOnce(usedDeobfuscations, needsUninitializedClassFieldsDetection, uninitializedClassTraits, null, insideDoInitAction, insideFunction, registerNames, variables, functions, code, version, staticOperation, path, charset, startIp, registerUsage);
                } catch (SecondPassException spe) {
                    context.put(key, spe.getData());
                    return spe.getFirstPassResult();
                }
            }

            SecondPassData collectedData = context.get(key);
            if (collectedData == null) {
                HashMap<String, GraphTargetItem> variablesBackup = new LinkedHashMap<>(variables);
                HashMap<String, GraphTargetItem> functionsBackup = new LinkedHashMap<>(functions);
                context.startCollecting();
                try {
                    translateViaGraph(usedDeobfuscations, needsUninitializedClassFieldsDetection, uninitializedClassTraits, null, insideDoInitAction, insideFunction, registerNames, variables, functions, code, version, staticOperation, path, charset, startIp, registerUsage);
                } finally {
                    context.startRendering();
                }
                variables.clear();
                variables.putAll(variablesBackup);
                functions.clear();
                functions.putAll(functionsBackup);
                collectedData = context.get(key);
            }
            secondPassData = collectedData;
        }

        return translateViaGraphOnce(usedDeobfuscations, needsUninitializedClassFieldsDetection, uninitializedClassTraits, secondPassData, insideDoInitAction, insideFunction, registerNames, variables, functions, code, version, staticOperation, path, charset, startIp, registerUsage);
    }

    private static List<GraphTargetItem> translateViaGraphOnce(Set<String> usedDeobfuscations, boolean needsUninitializedClassFieldsDetection, Map<String, Map<String, Trait>> uninitializedClassTraits, SecondPassData secondPassData, boolean insideDoInitAction, boolean insideFunction, HashMap<Integer, String> registerNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions, List<Action> code, int version, int staticOperation, String path, String charset, int startIp, ActionRegisterUsage registerUsage) throws InterruptedException {
        ActionGraph g = new ActionGraph(needsUninitializedClassFieldsDetection, uninitializedClassTraits, path, insideDoInitAction, insideFunction, code, registerNames, variables, functions, version, charset, startIp);
        g.registerUsage = registerUsage;
        ActionLocalData localData = new ActionLocalData(secondPassData, insideDoInitAction, registerNames, uninitializedClassTraits, usedDeobfuscations, new ArrayList<>(), new ArrayList<>());
        g.init(localData);
        return g.translate(localData, staticOperation, path);
    }

    /**
     * Final process stack. Override this method to provide custom behavior.
     *
     * @param stack Translate stack
     * @param output Output
     * @param path Path
     */
    @Override
    public void finalProcessStack(TranslateStack stack, List<GraphTargetItem> output, String path) {
    }

    /**
     * Checks whether a part can be a break candidate.
     *
     * @param localData Local data
     * @param part Part
     * @param throwStates List of throw states
     * @return True if part can be a break candidate
     */
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
    protected GraphTargetItem getShortCircuitRightSide(List<GraphTargetItem> commands) {
        if (commands.size() == 3 && commands.get(0) instanceof PopItem
                && (commands.get(1) instanceof SetVariableActionItem || commands.get(1) instanceof SetMemberActionItem)
                && commands.get(2) instanceof PushItem) {
            SetTypeActionItem assignment = (SetTypeActionItem) commands.get(1);
            GraphTargetItem result = commands.get(2).value;
            if (assignment.getCompoundOperator() == null && assignment.getValue() instanceof StoreRegisterActionItem
                    && result instanceof DirectValueActionItem && ((DirectValueActionItem) result).value instanceof RegisterNumber) {
                StoreRegisterActionItem store = (StoreRegisterActionItem) assignment.getValue();
                if (!store.define && store.register.number == ((RegisterNumber) ((DirectValueActionItem) result).value).number) {
                    // A reused register is still an assignment result. Keep its
                    // write in the expression in case it is read again later.
                    if (commands.get(1) instanceof SetVariableActionItem) {
                        GraphTargetItem name = ((SetVariableActionItem) commands.get(1)).name;
                        if (!(name instanceof DirectValueActionItem) || !((DirectValueActionItem) name).isString()
                                || !IdentifiersDeobfuscation.isValidName(false, name.toStringNoQuotes(LocalData.empty), "this", "super")) {
                            // set(name, value) does not return the assigned value.
                            List<GraphTargetItem> expressions = new ArrayList<>();
                            expressions.add(commands.get(1));
                            expressions.add(result);
                            return new CommaExpressionItem(dialect, null, result.getLineStartItem(), expressions);
                        }
                    }
                    List<GraphSourceItemPos> reads = registerUsage == null ? null : registerUsage.getReads(store.getSrc());
                    if (store.register.name == null && reads != null && reads.size() == 1
                            && reads.get(0).item == result.getSrc() && reads.get(0).pos == result.getPos()) {
                        // Only this consumed read uses the write. Return a copy:
                        // the caller may still decide this is not a logical operator.
                        GraphTargetItem original = commands.get(1);
                        if (original instanceof SetVariableActionItem) {
                            SetVariableActionItem variable = (SetVariableActionItem) original;
                            SetVariableActionItem replacement = new SetVariableActionItem(original.getSrc(), original.getLineStartItem(), variable.name, store.value);
                            replacement.forceUseSet = variable.forceUseSet;
                            return replacement;
                        }
                        SetMemberActionItem member = (SetMemberActionItem) original;
                        SetMemberActionItem replacement = new SetMemberActionItem(original.getSrc(), original.getLineStartItem(), member.object, member.objectName, store.value);
                        replacement.isSetter = member.isSetter;
                        return replacement;
                    }
                    return commands.get(1);
                }
            }
        }
        return super.getShortCircuitRightSide(commands);
    }

    @Override
    protected void finalProcess(GraphTargetItem parent, List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) throws InterruptedException {

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

                    if (pi.value instanceof FunctionActionItem) {
                        list.set(t, pi.value);
                    }
                }
                if (it instanceof SetTemporaryItem) {
                    SetTemporaryItem st = (SetTemporaryItem) it;
                    if (st.value instanceof GetPropertyActionItem) {
                        GetPropertyActionItem gp = (GetPropertyActionItem) st.value;
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
                    if (st.target instanceof TemporaryItem) {
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
                } else if (targetStart > -1) {
                    targetEnd = t;
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
                } else if (targetStart > -1) {
                    targetEnd = t;
                }
            }

            if (it instanceof TellTargetActionItem && targetStart > -1) {
                targetEnd = t;
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
                for (int i = targetEnd + (it instanceof TellTargetActionItem ? 0 : 1); i < list.size(); i++) {
                    newlist.add(list.get(i));
                }
                list.clear();
                list.addAll(newlist);
                targetStart = -1;
                targetEnd = -1;
                target = null;
                t = -1;
            }
        }
        resolveFrameLoadedBooleanBranches(list);
        resolveDuplicatedComparisonChains(list);
        structureForInLoops(list, localData);

        for (int t = 0; t < list.size(); t++) {
            GraphTargetItem it = list.get(t);
            if (it instanceof PushItem) {
                it = it.value;
            }
            if (it instanceof EnumeratedValueActionItem) {
                list.remove(t);
                t--;
            }
        }

        //Handle for loops at the end:
        super.finalProcess(parent, list, level, localData, path);
        removeUnreachableGotosAfterTerminalControl(list);
    }

    private boolean isBooleanLiteral(GraphTargetItem item, boolean value) {
        return (value ? item instanceof TrueItem : item instanceof FalseItem)
                || (item instanceof DirectValueActionItem
                && Boolean.valueOf(value).equals(((DirectValueActionItem) item).value));
    }

    private void resolveFrameLoadedBooleanBranches(List<GraphTargetItem> list) {
        for (int i = 0; i + 2 < list.size(); i++) {
            if (!(list.get(i) instanceof PushItem) || !isBooleanLiteral(list.get(i).value, false)
                    || !(list.get(i + 1) instanceof IfFrameLoadedActionItem)
                    || !(list.get(i + 2) instanceof IfItem)) {
                continue;
            }
            IfFrameLoadedActionItem loaded = (IfFrameLoadedActionItem) list.get(i + 1);
            IfItem branch = (IfItem) list.get(i + 2);
            GraphTargetItem condition = branch.expression;
            while (condition instanceof NotItem && condition.value instanceof NotItem) {
                condition = condition.value.value;
            }
            List<GraphTargetItem> body = loaded.getActions();
            // The skipped pair must replace the saved false with true. A graph
            // translated separately for this pair can print its final push as
            // a bare value, even though the surrounding graph consumes it.
            if (!(loaded.getSrc() instanceof ActionStore) || ((ActionStore) loaded.getSrc()).getStoreSize() != 2
                    || body.size() != 2 || !(body.get(0) instanceof PopItem)
                    || ((PopItem) body.get(0)).resolvedValue != null
                    || !isBooleanLiteral(body.get(1) instanceof PushItem ? body.get(1).value : body.get(1), true)
                    || !(condition instanceof PopItem) || ((PopItem) condition).resolvedValue != null
                    || (branch.onFalse != null && !branch.onFalse.isEmpty())) {
                continue;
            }
            list.set(i, new IfFrameLoadedActionItem(loaded.getFrame(), branch.onTrue, loaded.getSrc(), loaded.getLineStartItem()));
            list.remove(i + 1);
            list.remove(i + 1);
        }
    }

    private boolean isCachedTemporary(GraphTargetItem item, int index) {
        return item instanceof HasTempIndex && !(item instanceof SetTemporaryItem)
                && ((HasTempIndex) item).getTempIndex() == index;
    }

    private boolean leavesPreservedStackAlone(List<GraphTargetItem> commands, int preservedTemp) {
        Reference<Boolean> valid = new Reference<>(true);
        Set<GraphTargetItem> seen = new LinkedIdentityHashSet<>();
        GraphTargetVisitorInterface visitor = new AbstractGraphTargetVisitor() {
            @Override
            public boolean visit(GraphTargetItem item) {
                if (item == null || !valid.getVal() || !seen.add(item) || item instanceof FunctionActionItem) {
                    return false;
                }
                if (item instanceof PushItem || (item instanceof PopItem && ((PopItem) item).resolvedValue == null)
                        || item instanceof EnumerateActionItem || item instanceof EnumeratedValueActionItem
                        || item instanceof GotoItem || item instanceof LabelItem || item instanceof BreakItem || item instanceof ContinueItem
                        || (item instanceof SetTemporaryItem && ((SetTemporaryItem) item).tempIndex == preservedTemp)) {
                    valid.setVal(false);
                    return false;
                }
                // Temporary references print the cached value, not their backing expression.
                if (!(item instanceof HasTempIndex) || item instanceof SetTemporaryItem) {
                    item.visit(this);
                }
                return true;
            }
        };
        for (GraphTargetItem command : commands) {
            visitor.visit(command);
        }
        return valid.getVal();
    }

    private boolean isCachedComparison(IfItem item, int temp, int preservedTemp) {
        if (!(item.expression instanceof EqActionItem || item.expression instanceof NeqActionItem)) {
            return false;
        }
        BinaryOpItem comparison = (BinaryOpItem) item.expression;
        GraphTargetItem other;
        if (isCachedTemporary(comparison.leftSide, temp)) {
            other = comparison.rightSide;
        } else if (isCachedTemporary(comparison.rightSide, temp)) {
            other = comparison.leftSide;
        } else {
            return false;
        }
        List<GraphTargetItem> expression = new ArrayList<>();
        expression.add(other);
        return leavesPreservedStackAlone(expression, preservedTemp);
    }

    private boolean isRestoredComparison(List<GraphTargetItem> commands) {
        return commands.size() == 3 && commands.get(0) instanceof SetTemporaryItem
                && commands.get(1) instanceof PushItem && commands.get(2) instanceof IfItem;
    }

    private void resolveDuplicatedComparisonChains(List<GraphTargetItem> list) {
        for (int i = 1; i + 2 < list.size(); i++) {
            if (!(list.get(i - 1) instanceof SetTemporaryItem) || !(list.get(i) instanceof PushItem)
                    || !(list.get(i + 1) instanceof IfItem) || !(list.get(i + 2) instanceof PopItem)
                    || ((PopItem) list.get(i + 2)).resolvedValue != null) {
                continue;
            }
            SetTemporaryItem snapshot = (SetTemporaryItem) list.get(i - 1);
            if (snapshot.tempIndex <= 0 || !isCachedTemporary(list.get(i).value, snapshot.tempIndex)) {
                continue;
            }
            IfItem current = (IfItem) list.get(i + 1);
            int currentTemp = snapshot.tempIndex;
            Set<Integer> aliases = new HashSet<>();
            aliases.add(currentTemp);
            List<List<GraphTargetItem>> restores = new ArrayList<>();
            List<PopItem> pops = new ArrayList<>();
            boolean valid = true;
            while (true) {
                if (!isCachedComparison(current, currentTemp, snapshot.tempIndex)) {
                    valid = false;
                    break;
                }
                boolean restoreTrue = isRestoredComparison(current.onTrue);
                boolean restoreFalse = isRestoredComparison(current.onFalse);
                if (restoreTrue == restoreFalse) {
                    valid = !restoreTrue && leavesPreservedStackAlone(current.onTrue, snapshot.tempIndex)
                            && leavesPreservedStackAlone(current.onFalse, snapshot.tempIndex);
                    break;
                }
                List<GraphTargetItem> unmatched = restoreTrue ? current.onTrue : current.onFalse;
                List<GraphTargetItem> matched = restoreTrue ? current.onFalse : current.onTrue;
                if (!leavesPreservedStackAlone(matched, snapshot.tempIndex)) {
                    valid = false;
                    break;
                }
                SetTemporaryItem restore = (SetTemporaryItem) unmatched.get(0);
                if (!(restore.value instanceof PopItem) || ((PopItem) restore.value).resolvedValue != null
                        || restore.tempIndex <= 0 || !aliases.add(restore.tempIndex)
                        || !isCachedTemporary(unmatched.get(1).value, restore.tempIndex)) {
                    valid = false;
                    break;
                }
                restores.add(unmatched);
                pops.add((PopItem) restore.value);
                currentTemp = restore.tempIndex;
                current = (IfItem) unmatched.get(2);
            }
            if (!valid || restores.isEmpty()) {
                continue;
            }
            // Every branch preserves this one stack slot. The duplicates in
            // subsequent comparisons therefore read the original cached value.
            for (int r = 0; r < restores.size(); r++) {
                List<GraphTargetItem> restored = restores.get(r);
                SetTemporaryItem alias = (SetTemporaryItem) restored.get(0);
                IfItem next = (IfItem) restored.get(2);
                BinaryOpItem comparison = (BinaryOpItem) next.expression;
                GraphTargetItem cached = new TemporaryItem(dialect, snapshot.getSrc(), snapshot.getLineStartItem(), snapshot.value, snapshot.tempIndex);
                pops.get(r).resolvedValue = cached;
                if (isCachedTemporary(comparison.leftSide, alias.tempIndex)) {
                    comparison.leftSide = cached;
                } else {
                    comparison.rightSide = cached;
                }
                restored.remove(1);
            }
            list.remove(i + 2);
            list.remove(i);
        }
    }

    private void structureForInLoops(List<GraphTargetItem> list, FinalProcessLocalData localData) {
        for (int t = 1/*not first*/; t < list.size(); t++) {
            if (structureCopiedEnumerationKey(list, t)) {
                t--;
                continue;
            }
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

            EnumerateActionItem enumerateItem = null;
            BinaryOpItem comparisonOp = null;
            //for..in inside switch before break
            if ((checkedCondition instanceof TrueItem) && checkedBody != null) {
                if (!checkedBody.isEmpty() && (checkedBody.get(0) instanceof IfItem)) {
                    IfItem ifi = (IfItem) checkedBody.get(0);
                    if (ifi.onFalse.isEmpty()) {
                        if (!ifi.onTrue.isEmpty() && (ifi.onTrue.get(ifi.onTrue.size() - 1) instanceof BreakItem)) {
                            checkedCondition = ifi.expression;

                            if (checkedCondition instanceof EqActionItem) {
                                EqActionItem eq = (EqActionItem) checkedCondition;
                                if (eq.rightSide instanceof DirectValueActionItem) {
                                    DirectValueActionItem dv = (DirectValueActionItem) eq.rightSide;
                                    if (dv.value == Null.INSTANCE) {
                                        GraphTargetItem en = list.get(t - 1);
                                        if (en instanceof EnumerateActionItem) {
                                            enumerateItem = (EnumerateActionItem) en;
                                            if (eq.leftSide instanceof StoreRegisterActionItem) {
                                                comparisonOp = eq;
                                                checkedBody.remove(0);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (checkedCondition instanceof NeqActionItem) {
                NeqActionItem ne = (NeqActionItem) checkedCondition;
                if (ne.rightSide instanceof DirectValueActionItem) {
                    DirectValueActionItem dv = (DirectValueActionItem) ne.rightSide;
                    if (dv.value == Null.INSTANCE) {
                        GraphTargetItem en = list.get(t - 1);
                        if (en instanceof EnumerateActionItem) {
                            enumerateItem = (EnumerateActionItem) en;
                            if (ne.leftSide instanceof StoreRegisterActionItem) {
                                comparisonOp = ne;
                            }
                        }
                    }
                }
            }
            if (comparisonOp != null) {

                if (checkedBody != null && (!checkedBody.isEmpty()) && (checkedBody.get(0) instanceof SetTypeActionItem)) {
                    SetTypeActionItem sti = (SetTypeActionItem) checkedBody.get(0);

                    if ((sti.getValue() instanceof DirectValueActionItem) && (((DirectValueActionItem) sti.getValue()).value instanceof RegisterNumber)) {
                        if ((comparisonOp.rightSide instanceof DirectValueActionItem) && (((DirectValueActionItem) comparisonOp.rightSide).value instanceof Null)) {
                            if (comparisonOp.leftSide.value.getThroughDuplicate() instanceof EnumeratedValueActionItem) {
                                if (((StoreRegisterActionItem) comparisonOp.leftSide).register.number == ((RegisterNumber) (((DirectValueActionItem) sti.getValue()).value)).number) {
                                    list.remove(t);
                                    checkedBody.remove(0);
                                    if (checkedLoop == null) {
                                        checkedLoop = new Loop(localData.loops.size(), null, null);
                                        checkedBody.add(new BreakItem(dialect, null, null, checkedLoop.id));
                                    }
                                    sti.setValue(new DirectValueActionItem(Null.INSTANCE));
                                    list.add(t, new ForInActionItem(null, null, checkedLoop, (GraphTargetItem) sti, enumerateItem.object, checkedBody));
                                    //sti.getObject()
                                    list.remove(t - 1);
                                    t--;

                                    if (comparisonOp.leftSide.value instanceof TemporaryItem) {
                                        TemporaryItem ti = (TemporaryItem) comparisonOp.leftSide.value;
                                        if (t > 0) {
                                            if (list.get(t - 1) instanceof SetTemporaryItem) {
                                                SetTemporaryItem sti2 = (SetTemporaryItem) list.get(t - 1);
                                                if (sti2.tempIndex == ti.tempIndex) {
                                                    list.remove(t - 1);
                                                    t--;
                                                }
                                            }
                                        }
                                    }

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
                        checkedBody.add(new BreakItem(dialect, null, null, checkedLoop.id));
                    }
                    list.remove(t - 1);
                    t--;
                    if (enumerateItem.object instanceof SetTypeActionItem) {
                        list.add(t++, enumerateItem.object);
                        enumerateItem.object = ((SetTypeActionItem) enumerateItem.object).getObject();
                    }
                    list.add(t, new ForInActionItem(null, null, checkedLoop, (GraphTargetItem) comparisonOp.leftSide, enumerateItem.object, checkedBody));
                    if (t + 1 < list.size()) {
                        if (list.get(t + 1) instanceof EnumeratedValueActionItem) {
                            list.remove(t + 1);
                        }
                    }
                }
            }
        }
    }

    private boolean isNullValue(GraphTargetItem item) {
        return item instanceof DirectValueActionItem && ((DirectValueActionItem) item).value == Null.INSTANCE;
    }

    private boolean isRegisterValue(GraphTargetItem item, int register) {
        return item instanceof DirectValueActionItem && ((DirectValueActionItem) item).value instanceof RegisterNumber
                && ((RegisterNumber) ((DirectValueActionItem) item).value).number == register;
    }

    private boolean structureCopiedEnumerationKey(List<GraphTargetItem> list, int index) {
        if (!(list.get(index - 1) instanceof EnumerateActionItem) || !(list.get(index) instanceof WhileItem)
                || index + 1 >= list.size() || !(list.get(index + 1) instanceof WhileItem)) {
            return false;
        }
        WhileItem loop = (WhileItem) list.get(index);
        WhileItem drain = (WhileItem) list.get(index + 1);
        List<GraphTargetItem> body = loop.commands;
        if (loop.expression.size() != 1 || !(loop.expression.get(0) instanceof TrueItem) || body.size() < 4
                || !(body.get(0) instanceof PushItem) || !(body.get(0).value instanceof StoreRegisterActionItem)
                || !(body.get(1) instanceof IfItem) || !(body.get(2) instanceof PopItem)
                || ((PopItem) body.get(2)).resolvedValue != null
                || !(body.get(3) instanceof SetVariableActionItem || body.get(3) instanceof SetMemberActionItem)
                || drain.expression.size() != 1 || !drain.commands.isEmpty()
                || !(drain.expression.get(0) instanceof NeqActionItem)) {
            return false;
        }
        StoreRegisterActionItem store = (StoreRegisterActionItem) body.get(0).value;
        IfItem endCheck = (IfItem) body.get(1);
        if (!(store.value.getThroughDuplicate() instanceof EnumeratedValueActionItem)
                || !(endCheck.expression instanceof EqActionItem) || !endCheck.onFalse.isEmpty()
                || endCheck.onTrue.size() != 1 || !(endCheck.onTrue.get(0) instanceof BreakItem)
                || ((BreakItem) endCheck.onTrue.get(0)).loopId != loop.loop.id) {
            return false;
        }
        EqActionItem comparison = (EqActionItem) endCheck.expression;
        GraphTargetItem key = comparison.leftSide;
        if (isNullValue(key)) {
            key = comparison.rightSide;
        } else if (!isNullValue(comparison.rightSide)) {
            return false;
        }
        SetTypeActionItem assignment = (SetTypeActionItem) body.get(3);
        NeqActionItem drainCheck = (NeqActionItem) drain.expression.get(0);
        boolean drainsEnumeration = (drainCheck.leftSide instanceof EnumeratedValueActionItem && isNullValue(drainCheck.rightSide))
                || (drainCheck.rightSide instanceof EnumeratedValueActionItem && isNullValue(drainCheck.leftSide));
        if (store.register.name != null || assignment.getCompoundOperator() != null
                || !isRegisterValue(key, store.register.number) || !isRegisterValue(assignment.getValue(), store.register.number)
                || !drainsEnumeration || registerUsage == null) {
            return false;
        }
        List<GraphSourceItemPos> reads = registerUsage.getReads(store.getSrc());
        if (reads == null || reads.size() != 2) {
            return false;
        }
        for (GraphSourceItemPos read : reads) {
            if (!(read.item == key.getSrc() && read.pos == key.getPos())
                    && !(read.item == assignment.getValue().getSrc() && read.pos == assignment.getValue().getPos())) {
                return false;
            }
        }
        EnumerateActionItem enumerate = (EnumerateActionItem) list.get(index - 1);
        GraphTargetItem object = enumerate.object;
        if (enumerate.getSrc() instanceof ActionEnumerate) {
            object = new GetVariableActionItem(enumerate.getSrc(), enumerate.getLineStartItem(), object);
        }
        List<GraphTargetItem> commands = new ArrayList<>(body.subList(4, body.size()));
        assignment.setValue(new DirectValueActionItem(Null.INSTANCE));
        // The extra stack copy and the empty drain belong to the enumeration
        // protocol. The source-level for-in also handles cleanup on break.
        for (int i = index + 1; i >= index - 1; i--) {
            list.remove(i);
        }
        list.add(index - 1, new ForInActionItem(null, null, loop.loop, (GraphTargetItem) assignment, object, commands));
        return true;
    }

    private void removeUnreachableGotosAfterTerminalControl(List<GraphTargetItem> list) {
        for (int i = 1; i < list.size(); i++) {
            GraphTargetItem previous = list.get(i - 1);
            if (list.get(i) instanceof GotoItem
                    && (previous instanceof BreakItem
                    || previous instanceof ContinueItem
                    || previous instanceof ExitItem)) {
                String labelName = ((GotoItem) list.get(i)).labelName;
                if (labelName != null) {
                    labelsOfRemovedGotos.add(labelName);
                }
                list.remove(i);
                i--;
            }
        }
    }

    @Override
    protected void finalProcessAfter(List<GraphTargetItem> list, int level, FinalProcessLocalData localData, String path) {
        super.finalProcessAfter(list, level, localData, path);
        // An inner enumeration may become a conditional while only after
        // the graph pass has moved its continue of the outer loop.
        structureForInLoops(list, localData);
        Set<String> usedLabels = new HashSet<>();
        collectUsedLabels(list, usedLabels);
        for (int i = list.size() - 1; i >= 0; i--) {
            GraphTargetItem item = list.get(i);
            if (item instanceof LabelItem
                    && labelsOfRemovedGotos.contains(((LabelItem) item).labelName)
                    && !usedLabels.contains(((LabelItem) item).labelName)) {
                list.remove(i);
            }
        }
    }

    private void collectUsedLabels(List<GraphTargetItem> list, Set<String> usedLabels) {
        for (GraphTargetItem item : list) {
            if (item instanceof GotoItem) {
                String labelName = ((GotoItem) item).labelName;
                if (labelName != null) {
                    usedLabels.add(labelName);
                }
            }
            if (item instanceof Block) {
                for (List<GraphTargetItem> sub : ((Block) item).getSubs()) {
                    collectUsedLabels(sub, usedLabels);
                }
            }
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

    /**
     * Translates the graph - decompiles.
     *
     * @param localData Local data
     * @param staticOperation Unused
     * @param path Path
     * @return List of GraphTargetItems
     * @throws InterruptedException On interrupt
     */
    @Override
    public List<GraphTargetItem> translate(BaseLocalData localData, int staticOperation, String path) throws InterruptedException {
        List<GraphTargetItem> ret = super.translate(localData, staticOperation, path);
        if (insideDoInitAction && !insideFunction) {
            ActionScript2ClassDetector detector = new ActionScript2ClassDetector();
            detector.checkClass(uninitializedClassTraits, ret, ((ActionGraphSource) code).getVariables(), path);
            // Class construction can contain pops that disappear only during
            // detection, after the common graph pass checked unused pushes.
            promoteUnpoppedPushItemsToCommands(ret);
            if (needsUninitializedClassFieldsDetection && Configuration.skipDetectionOfUninitializedClassFields.get()) {
                ret.add(0, new CommentItem(AppResources.translate("decompilationWarning.as2.noUninitializedClassFieldsDetection")));
            }
        }
        ActionLocalData ald = (ActionLocalData) localData;

        makeDefineRegistersUp(ret, new HashSet<>(ald.regNames.keySet()));
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
     
        It also makes sure that var keyword is on the first occurrence of that register.
     
     */
    /**
     * Makes define registers up.
     *
     * @param list List of GraphTargetItems
     * @param definedRegisters Defined registers
     */
    private void makeDefineRegistersUp(List<GraphTargetItem> list, Set<Integer> definedRegisters) {
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem ti = list.get(i);
            Reference<Integer> ri = new Reference<>(i);

            if (ti instanceof TemporaryRegister) {
                continue;
            }

            Set<GraphTargetItem> visitedItems = new LinkedIdentityHashSet<>();
            GraphTargetVisitorInterface visitor = new AbstractGraphTargetVisitor() {
                @Override
                public boolean visit(GraphTargetItem item) {
                    if (item != null && !visitedItems.contains(item)) {
                        visitedItems.add(item);

                        if (item instanceof TemporaryRegister) {
                            return true;
                        }
                        // A for-in header can declare its iteration register directly.
                        if ((ti instanceof ForInActionItem) && (item == ((ForInActionItem) ti).variableName)) {
                            if (item instanceof StoreRegisterActionItem) {
                                StoreRegisterActionItem sr = (StoreRegisterActionItem) item;
                                sr.define = definedRegisters.add(sr.register.number);
                            }
                            return true;
                        }
                        if (item instanceof StoreRegisterActionItem) {
                            StoreRegisterActionItem sr = (StoreRegisterActionItem) item;
                            sr.define = !definedRegisters.contains(sr.register.number);
                            definedRegisters.add(sr.register.number);
                            if (sr.define && sr != ti) {
                                list.add(ri.getVal(), new StoreRegisterActionItem(null, null, sr.register, new DirectValueActionItem(Undefined.INSTANCE), true));
                                sr.define = false;
                                ri.setVal(ri.getVal() + 1);
                            }
                        }

                        if (item instanceof FunctionActionItem) {
                            return false;
                        }

                        item.visitNoBlock(this);
                    }
                    return true;
                }
            };

            if (ti instanceof StoreRegisterActionItem) {
                StoreRegisterActionItem sr = (StoreRegisterActionItem) ti;
                sr.define = !definedRegisters.contains(sr.register.number);
                definedRegisters.add(sr.register.number);
            }

            ti.visitNoBlock(visitor);
            //visitor.visit(ti);
            //ti.visitRecursively(visitor);
            i = ri.getVal();
            if (ti instanceof Block) {
                Block b = (Block) ti;
                for (List<GraphTargetItem> items : b.getSubs()) {
                    makeDefineRegistersUp(items, definedRegisters);
                }

                for (List<GraphTargetItem> items : b.getSubs()) {
                    for (int j = 0; j < items.size(); j++) {
                        GraphTargetItem item = items.get(j);
                        if (item instanceof StoreRegisterActionItem) {
                            StoreRegisterActionItem sr = (StoreRegisterActionItem) item;
                            if (sr.define) {
                                list.add(ri.getVal(), new StoreRegisterActionItem(null, null, sr.register, new DirectValueActionItem(Undefined.INSTANCE), true));
                                sr.define = false;
                                if ((sr.value instanceof DirectValueActionItem) && (((DirectValueActionItem) sr.value).value == Undefined.INSTANCE)) {
                                    items.remove(j);
                                    j--;
                                }
                                ri.setVal(ri.getVal() + 1);
                            }
                        }
                    }
                }
            }
            i = ri.getVal();
        }
    }

    /**
     * Finds part.
     *
     * @param ip IP
     * @param allParts All parts
     * @return GraphPart
     */
    private GraphPart findPart(int ip, Set<GraphPart> allParts) {
        for (GraphPart p : allParts) {
            if (p.containsIP(ip)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Check before decompiling next section.
     *
     * @param hasEmptyStackPops Has empty stack pops
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
    protected List<GraphTargetItem> check(Reference<Boolean> hasEmptyStackPops, List<GraphTargetItem> currentRet, List<GotoItem> foundGotos, Map<GraphPart, List<GraphTargetItem>> partCodes, Map<GraphPart, Integer> partCodePos, Set<GraphPart> visited, GraphSource code, BaseLocalData localData, Set<GraphPart> allParts, TranslateStack stack, GraphPart parent, GraphPart part, List<GraphPart> stopPart, List<StopPartKind> stopPartKind, List<Loop> loops, List<ThrowState> throwStates, List<GraphTargetItem> output, Loop currentLoop, int staticOperation, String path) throws InterruptedException {
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
                makeAllCommands(output, stack);
                SwitchItem sw = handleSwitch(switchedObject, switchStartItem, foundGotos, partCodes, partCodePos, visited, allParts, stack, stopPart, stopPartKind, loops, throwStates, localData, staticOperation, path, caseValuesMap, defaultPart, caseBodyParts, nextRef, tiRef, hasEmptyStackPops);
                fixSwitchEnd(sw);
                ret = new ArrayList<>();
                ret.addAll(output);
                ret.add(sw);
                if (nextRef.getVal() != null) {
                    if (tiRef.getVal() != null) {
                        ret.add(tiRef.getVal());
                    } else {
                        ret.addAll(printGraph(hasEmptyStackPops, foundGotos, partCodes, partCodePos, visited, localData, stack, allParts, null, nextRef.getVal(), stopPart, stopPartKind, loops, throwStates, staticOperation, path));
                    }
                }
            }
        }
        return ret;
    }

    @Override
    protected boolean supportsLabeledBreaksAndContinues() {
        return false;
    }

    private int ipAfterJumps(int nip) {
        while (nip < code.size() && code.get(nip) instanceof ActionJump) {
            ActionJump j = (ActionJump) code.get(nip);
            int nip2 = code.adr2pos(j.getTargetAddress());
            if (nip2 == nip) {
                break;
            }
            nip = nip2;
        }
        return nip;
    }

    /**
     * Checks IP and allows to modify it.
     *
     * @param ip Current IP
     * @return New IP
     */
    @Override
    protected int checkIp(int ip) {

        if (ip >= code.size()) {
            return ip;
        }

        int oldIp = ip;

        //return/break in for..in
        /*
        We need to skip following:
        
        locA:Push null
        Equals/Equals2
        Not
        If locA
        ...
        
        Beware: There can be obfuscation jumps anywhere on the path!
         */
        GraphSourceItem action = code.get(ip);
        if ((action instanceof ActionPush) && (((ActionPush) action).values.size() == 1) && (((ActionPush) action).values.get(0) == Null.INSTANCE)) {
            int nip = ip;
            if (nip + 1 < code.size()) {
                nip++;
                nip = ipAfterJumps(nip);
                if (nip < code.size() && ((code.get(nip) instanceof ActionEquals) || (code.get(nip) instanceof ActionEquals2))) {
                    nip++;
                    nip = ipAfterJumps(nip);
                    if (nip < code.size() && code.get(nip) instanceof ActionNot) {
                        nip++;
                        nip = ipAfterJumps(nip);
                        if (nip < code.size() && code.get(nip) instanceof ActionIf) {
                            ActionIf aif = (ActionIf) code.get(nip);

                            int jip = code.adr2pos(aif.getTargetAddress());
                            jip = ipAfterJumps(jip);
                            if (jip == ip) {
                                nip++;
                                ip = nip;
                            }
                        }
                    }
                }
            }

            //The simple approach is not working, there may be jumps inside
            /*
            if (ip + 3 <= code.size()) {
                if ((code.get(ip + 1) instanceof ActionEquals) || (code.get(ip + 1) instanceof ActionEquals2)) {
                    if (code.get(ip + 2) instanceof ActionNot) {
                        if (code.get(ip + 3) instanceof ActionIf) {
                            ActionIf aif = (ActionIf) code.get(ip + 3);
                            if (code.adr2pos(code.pos2adr(ip + 3) + 5 //IF numbytes
                            + aif.getJumpOffset()) == ip) {
                                ip += 4;
                            }
                        }
                    }
                }
            }
             */
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
    public SecondPassData prepareSecondPass(BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates, List<GraphTargetItem> list) {
        ActionSecondPassData spd = new ActionSecondPassData();
        Set<GraphPart> processedIfs = new HashSet<>();
        checkSecondPassSwitches(localData, loops, throwStates, spd.switchCases, spd.switchBreaks, processedIfs, list, spd.switchParts, spd.switchOnFalseParts, spd.switchCaseExpressions);

        return spd;
    }

    private GraphTargetItem getFirstListItem(List<GraphTargetItem> list) {
        int i = 0;
        while (i < list.size()) {
            GraphTargetItem item = list.get(i);
            if (item instanceof LabelItem) {
                i++;
                continue;
            }
            return item;
        }
        return null;
    }

    /**
     * Checks second pass switches.
     *
     * @param allSwitchCases SwitchCases
     * @param processedIfs Processed ifs
     * @param list List of GraphTargetItems
     * @param allSwitchParts All switch parts
     * @param allSwitchOnFalseParts All switch on false parts
     * @param allSwitchExpressions All switch expressions
     */
    private void checkSecondPassSwitches(BaseLocalData localData, List<Loop> loops, List<ThrowState> throwStates, List<List<GraphPart>> allSwitchCases, List<GraphPart> allSwitchBreaks, Set<GraphPart> processedIfs, List<GraphTargetItem> list, List<List<GraphPart>> allSwitchParts, List<List<GraphPart>> allSwitchOnFalseParts, List<List<GraphTargetItem>> allSwitchExpressions) {
        for (int i = 0; i < list.size(); i++) {
            GraphTargetItem item = list.get(i);
            if (item instanceof DoWhileItem) {
                DoWhileItem dw = (DoWhileItem) item;
                UniversalLoopItem uni = new UniversalLoopItem(dialect, null, null, dw.loop, dw.commands);

                GraphTargetItem expr = dw.expression.remove(dw.expression.size() - 1);
                dw.commands.addAll(dw.expression);

                IfItem ifi = new IfItem(dialect, null, null, expr.invert(expr.getSrc()), new ArrayList<>(), new ArrayList<>());
                ifi.onTrue.add(new BreakItem(dialect, null, null, uni.loop.id));
                ifi.decisionPart = new GraphPart(Integer.MAX_VALUE, processedIfs.size()); //hack
                uni.commands.add(ifi);
                list.set(i, uni);
            }
        }
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
                        List<GraphPart> switchCases = new ArrayList<>();
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
                            switchCases.add(isNeq ? ii.onTruePart : ii.onFalsePart); //the expression is inverted when creating ifItem

                            IfItem ii2 = ii;
                            IfItem lastOkayIi = ii;
                            while (true) {
                                if ((isNeq && (getFirstListItem(ii2.onTrue) instanceof IfItem))
                                        || (!isNeq && (getFirstListItem(ii2.onFalse) instanceof IfItem))) {
                                    ii2 = (IfItem) (isNeq ? getFirstListItem(ii2.onTrue) : getFirstListItem(ii2.onFalse));
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
                                                    switchCases.add(isNeq ? ii2.onTruePart : ii2.onFalsePart); //the expression is inverted when creating ifItem
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
                                                switchCases.add(isNeq ? ii2.onTruePart : ii2.onFalsePart); //the expression is inverted when creating ifItem
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
                                allSwitchCases.add(switchCases);
                                try {
                                    allSwitchBreaks.add(getMostCommonPart(localData, switchCases, loops, throwStates, new ArrayList<>()));
                                } catch (InterruptedException ex) {
                                    allSwitchBreaks.add(null);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            if ((item instanceof Block)) {
                for (List<GraphTargetItem> sub : ((Block) item).getSubs()) {
                    checkSecondPassSwitches(localData, loops, throwStates, allSwitchCases, allSwitchBreaks, processedIfs, sub, allSwitchParts, allSwitchOnFalseParts, allSwitchExpressions);
                }
            }
        }
    }
}

/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.action.deobfuscation;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.ReturnActionItem;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionMultiply;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionSubtract;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionAdd2;
import com.jpexs.decompiler.flash.action.swf5.ActionBitAnd;
import com.jpexs.decompiler.flash.action.swf5.ActionBitLShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitOr;
import com.jpexs.decompiler.flash.action.swf5.ActionBitRShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitXor;
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDecrement;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionIncrement;
import com.jpexs.decompiler.flash.action.swf5.ActionModulo;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.action.swf6.ActionGreater;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author JPEXS
 */
public class ActionDeobfuscator implements SWFDecompilerListener {

    private final int executionLimit = 30000;

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {
        combinePushs(actions);
        Map<String, Object> fakeFunctions = getFakeFunctionResults(actions);
        removeUnreachableActions(actions);
        removeObfuscationIfs(actions, fakeFunctions);
        removeUnreachableActions(actions);
        removeZeroJumps(actions);
        rereadActionList(actions, swf); // this call will fix the contant pool assigments
    }

    private void combinePushs(ActionList actions) {
        for (int i = 0; i < actions.size() - 1; i++) {
            Action action = actions.get(i);
            Action action2 = actions.get(i + 1);
            if (action instanceof ActionPush && action2 instanceof ActionPush) {
                if (!actions.getReferencesFor(action2).hasNext()) {
                    ActionPush push = (ActionPush) action;
                    ActionPush push2 = (ActionPush) action2;
                    if (!(push.constantPool != null && push2.constantPool != null && push.constantPool != push.constantPool)) {
                        ActionPush newPush = new ActionPush(0);
                        newPush.constantPool = push.constantPool == null ? push2.constantPool : push.constantPool;
                        newPush.values.clear();
                        newPush.values.addAll(push.values);
                        newPush.values.addAll(push2.values);
                        actions.addAction(i + 1, newPush);
                        actions.removeAction(i + 2);
                        actions.removeAction(i);
                        i--;
                    }
                }
            }
        }
    }

    private boolean rereadActionList(ActionList actions, SWF swf) {
        byte[] actionBytes = Action.actionsToBytes(actions, true, SWF.DEFAULT_VERSION);
        try {
            SWFInputStream rri = new SWFInputStream(swf, actionBytes);
            ActionList newActions = ActionListReader.readActionList(new ArrayList<DisassemblyListener>(), rri, SWF.DEFAULT_VERSION, 0, actionBytes.length, "", -1);
            actions.setActions(newActions);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ActionDeobfuscator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private boolean removeUnreachableActions(ActionList actions) {
        Set<Action> reachableActions = new HashSet<>();
        Set<Action> processedActions = new HashSet<>();
        reachableActions.add(actions.get(0));
        boolean modified = true;
        while (modified) {
            modified = false;
            for (int i = 0; i < actions.size(); i++) {
                Action action = actions.get(i);
                if (reachableActions.contains(action) && !processedActions.contains(action)) {
                    if (!action.isExit() && !(action instanceof ActionJump) && i != actions.size() - 1) {
                        Action next = actions.get(i + 1);
                        if (!reachableActions.contains(next)) {
                            reachableActions.add(next);
                        }
                    }

                    if (action instanceof ActionJump) {
                        ActionJump aJump = (ActionJump) action;
                        long ref = aJump.getAddress() + aJump.getTotalActionLength() + aJump.getJumpOffset();
                        Action target = actions.getByAddress(ref);
                        if (target != null && !reachableActions.contains(target)) {
                            reachableActions.add(target);
                        }
                    } else if (action instanceof ActionIf) {
                        ActionIf aIf = (ActionIf) action;
                        long ref = aIf.getAddress() + aIf.getTotalActionLength() + aIf.getJumpOffset();
                        Action target = actions.getByAddress(ref);
                        if (target != null && !reachableActions.contains(target)) {
                            reachableActions.add(target);
                        }
                    } else if (action instanceof ActionStore) {
                        ActionStore aStore = (ActionStore) action;
                        int storeSize = aStore.getStoreSize();
                        if (actions.size() > i + storeSize) {
                            Action target = actions.get(i + storeSize);
                            if (!reachableActions.contains(target)) {
                                reachableActions.add(target);
                            }
                        }
                    } else if (action instanceof GraphSourceItemContainer) {
                        GraphSourceItemContainer container = (GraphSourceItemContainer) action;
                        long ref = action.getAddress() + action.getTotalActionLength();
                        for (Long size : container.getContainerSizes()) {
                            ref += size;
                            Action target = actions.getByAddress(ref);
                            if (target != null && !reachableActions.contains(target)) {
                                reachableActions.add(target);
                            }
                        }
                    }

                    processedActions.add(action);
                    modified = true;
                }
            }
        }

        boolean result = false;
        for (int i = 0; i < actions.size(); i++) {
            if (!reachableActions.contains(actions.get(i))) {
                actions.removeAction(i);
                i--;
                result = true;
            }
        }

        return result;
    }

    private boolean removeZeroJumps(ActionList actions) {
        boolean result = false;
        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            if (action instanceof ActionJump && ((ActionJump) action).getJumpOffset() == 0) {
                actions.removeAction(i);
                i--;
                result = true;
            }
        }
        return result;
    }

    private boolean removeObfuscationIfs(ActionList actions, Map<String, Object> fakeFunctions) {
        if (actions.size() == 0) {
            return false;
        }

        ActionConstantPool cPool = getConstantPool(actions);
        for (int i = 0; i < actions.size(); i++) {
            ExecutionResult result = new ExecutionResult();
            executeActions(actions, i, actions.size() - 1, cPool, result, fakeFunctions);

            if (result.idx != -1) {
                int newIstructionCount = 1; // jump
                if (result.constantPool != null) {
                    newIstructionCount++;
                }
                if (!result.stack.isEmpty()) {
                    newIstructionCount++;
                }
                newIstructionCount += 2 * result.variables.size();

                if (newIstructionCount * 2 < result.instructionsProcessed) {
                    Action target = actions.get(result.idx);
                    Action prevAction = actions.get(i);

                    if (result.constantPool != null) {
                        ActionConstantPool constantPool2 = new ActionConstantPool(new ArrayList<>(result.constantPool.constantPool));
                        actions.addAction(i++, constantPool2);
                        prevAction = constantPool2;
                    }

                    for (String variableName : result.variables.keySet()) {
                        Object value = result.variables.get(variableName);
                        ActionPush push = new ActionPush(variableName);
                        push.values.add(value);
                        push.setAddress(prevAction.getAddress());
                        actions.addAction(i++, push);
                        prevAction = push;

                        if (result.defines.contains(variableName)) {
                            ActionDefineLocal defineLocal = new ActionDefineLocal();
                            defineLocal.setAddress(prevAction.getAddress());
                            actions.addAction(i++, defineLocal);
                            prevAction = defineLocal;
                        } else {
                            ActionSetVariable setVariable = new ActionSetVariable();
                            setVariable.setAddress(prevAction.getAddress());
                            actions.addAction(i++, setVariable);
                            prevAction = setVariable;
                        }
                    }

                    if (!result.stack.isEmpty()) {
                        ActionPush push = new ActionPush(0);
                        push.values.clear();
                        for (GraphTargetItem graphTargetItem : result.stack) {
                            DirectValueActionItem dv = (DirectValueActionItem) graphTargetItem;
                            push.values.add(dv.value);
                        }
                        push.setAddress(prevAction.getAddress());
                        actions.addAction(i++, push);
                        prevAction = push;
                    }

                    ActionJump jump = new ActionJump(0);
                    jump.setAddress(prevAction.getAddress());
                    jump.setJumpOffset((int) (target.getAddress() - jump.getAddress() - jump.getTotalActionLength()));
                    actions.addAction(i++, jump);

                    return true;
                }
            }
        }

        return false;
    }

    private ActionConstantPool getConstantPool(ActionList actions) {
        ActionConstantPool cPool = null;
        for (Action action : actions) {
            if (action instanceof ActionConstantPool) {
                if (cPool != null) {
                    // there are multiple constant pools
                    return null;
                }
                cPool = (ActionConstantPool) action;
            }
        }
        return cPool;
    }
    
    private void executeActions(ActionList actions, int idx, int endIdx, ActionConstantPool constantPool, ExecutionResult result, Map<String, Object> fakeFunctions) {
        List<GraphTargetItem> output = new ArrayList<>();
        ActionLocalData localData = new ActionLocalData();
        FixItemCounterTranslateStack stack = new FixItemCounterTranslateStack();
        int instructionsProcessed = 0;
        ActionConstantPool lastConstantPool = null;

        try {
            while (true) {
                if (idx > endIdx) {
                    break;
                }

                Action action = actions.get(idx);
                instructionsProcessed++;

                if (instructionsProcessed > executionLimit) {
                    break;
                }

                /*System.out.print(action.getASMSource(actions, new ArrayList<Long>(), ScriptExportMode.PCODE));
                 for (int j = 0; j < stack.size(); j++) {
                 System.out.print(" '" + stack.get(j).getResult() + "'");
                 }
                 System.out.println();*/
                if (action instanceof ActionConstantPool) {
                    lastConstantPool = (ActionConstantPool) action;
                }

                if (action instanceof ActionDefineLocal) {
                    GraphTargetItem top = stack.pop();
                    String variableName = stack.peek().getResult().toString();
                    result.defines.add(variableName);
                    stack.push(top);
                }

                if (action instanceof ActionGetVariable) {
                    String variableName = stack.peek().getResult().toString();
                    if (!localData.variables.containsKey(variableName)) {
                        break;
                    }
                }

                if (action instanceof ActionCallFunction) {
                    String functionName = stack.pop().getResult().toString();
                    long numArgs = EcmaScript.toUint32(stack.pop().getResult());
                    if (numArgs == 0) {
                        if (fakeFunctions != null && fakeFunctions.containsKey(functionName)) {
                            stack.push(new DirectValueActionItem(fakeFunctions.get(functionName)));
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    action.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");
                }

                if (!(action instanceof ActionPush
                        || action instanceof ActionPushDuplicate
                        || action instanceof ActionAdd
                        || action instanceof ActionAdd2
                        || action instanceof ActionIncrement
                        || action instanceof ActionDecrement
                        || action instanceof ActionSubtract
                        || action instanceof ActionModulo
                        || action instanceof ActionMultiply
                        || action instanceof ActionBitXor
                        || action instanceof ActionBitAnd
                        || action instanceof ActionBitOr
                        || action instanceof ActionBitLShift
                        || action instanceof ActionBitRShift
                        || action instanceof ActionDefineLocal
                        || action instanceof ActionJump
                        || action instanceof ActionGetVariable
                        || action instanceof ActionSetVariable
                        || action instanceof ActionEquals
                        || action instanceof ActionEquals2
                        || action instanceof ActionGreater
                        || action instanceof ActionNot
                        || action instanceof ActionIf
                        || action instanceof ActionConstantPool
                        || action instanceof ActionCallFunction
                        || action instanceof ActionReturn
                        || action instanceof ActionEnd)) {
                    break;
                }

                if (action instanceof ActionPush) {
                    ActionPush push = (ActionPush) action;
                    boolean ok = true;
                    for (Object value : push.values) {
                        if ((constantPool == null && value instanceof ConstantIndex) || value instanceof RegisterNumber) {
                            ok = false;
                            break;
                        }
                    }
                    if (!ok) {
                        break;
                    }
                }

                /*for (String variable : localData.variables.keySet()) {
                 System.out.println(Helper.byteArrToString(variable.getBytes()));
                 }*/
                idx++;

                if (action instanceof ActionJump) {
                    ActionJump jump = (ActionJump) action;
                    long address = jump.getAddress() + jump.getTotalActionLength() + jump.getJumpOffset();
                    idx = actions.indexOf(actions.getByAddress(address));
                    if (idx == -1) {
                        throw new TranslateException("Jump target not found: " + address);
                    }
                }

                if (action instanceof ActionIf) {
                    ActionIf aif = (ActionIf) action;
                    if (EcmaScript.toBoolean(stack.pop().getResult())) {
                        long address = aif.getAddress() + aif.getTotalActionLength() + aif.getJumpOffset();
                        idx = actions.indexOf(actions.getByAddress(address));
                        if (idx == -1) {
                            throw new TranslateException("If target not found: " + address);
                        }
                    }
                }

                if (action instanceof ActionDefineFunction) {
                    List<Action> lastActions = actions.getContainerLastActions(action);
                    int lastActionIdx = actions.indexOf(lastActions.get(0));
                    idx = lastActionIdx != -1 ? lastActionIdx + 1 : -1;
                }

                if (/*localData.variables.size() == 1 && */stack.allItemsFixed() || action instanceof ActionEnd) {
                    result.idx = idx == actions.size() ? idx - 1 : idx;
                    result.instructionsProcessed = instructionsProcessed;
                    result.constantPool = lastConstantPool;
                    result.variables.clear();
                    for (String variableName : localData.variables.keySet()) {
                        Object value = localData.variables.get(variableName).getResult();
                        result.variables.put(variableName, value);
                    }
                    result.stack.clear();
                    result.stack.addAll(stack);
                }

                if (action instanceof ActionReturn) {
                    if (output.size() > 0) {
                        ReturnActionItem ret = (ReturnActionItem) output.get(output.size() - 1);
                        result.resultValue = ret.value.getResult();
                    }
                    break;
                }
            }
        } catch (EmptyStackException | TranslateException | InterruptedException ex) {
        }
    }

    private Map<String, Object> getFakeFunctionResults(ActionList actions) {
        /*
         DefineFunction "fakeName" 0  {
         Push 1777
         Return
         }        
         */

        Map<String, Object> results = new HashMap<>();

        for (int i = 0; i < actions.size(); i++) {
            Action action = actions.get(i);
            if (action instanceof ActionDefineFunction) {
                ActionDefineFunction def = (ActionDefineFunction) action;
                if (def.paramNames.isEmpty()) {
                    ExecutionResult result = new ExecutionResult();
                    List<Action> lastActions = actions.getContainerLastActions(action);
                    int lastActionIdx = actions.indexOf(lastActions.get(0));
                    executeActions(actions, i + 1, lastActionIdx, null, result, null);
                    if (result.resultValue != null) {
                        results.put(def.functionName, result.resultValue);
                        for (int j = i; j <= lastActionIdx; j++) {
                            actions.removeAction(i);
                        }
                    }
                }
            }
        }

        return results;
    }

    @Override
    public byte[] proxyFileCatched(byte[] data) {
        return null;
    }

    @Override
    public void swfParsed(SWF swf) {
    }

    @Override
    public void abcParsed(ABC abc, SWF swf) {
    }

    @Override
    public void methodBodyParsed(MethodBody body, SWF swf) {
    }

    class ExecutionResult {

        public int idx = -1;
        public int instructionsProcessed = -1;
        public ActionConstantPool constantPool;
        public Map<String, Object> variables = new HashMap<>();
        public Set<String> defines = new HashSet<>();
        public TranslateStack stack = new TranslateStack();
        public Object resultValue;
    }
}

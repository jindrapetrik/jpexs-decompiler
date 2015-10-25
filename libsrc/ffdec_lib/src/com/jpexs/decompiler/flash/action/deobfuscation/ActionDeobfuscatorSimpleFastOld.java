/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.fastactionlist.ActionItem;
import com.jpexs.decompiler.flash.action.fastactionlist.FastActionList;
import com.jpexs.decompiler.flash.action.fastactionlist.FastActionListIterator;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.ReturnActionItem;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionAnd;
import com.jpexs.decompiler.flash.action.swf4.ActionAsciiToChar;
import com.jpexs.decompiler.flash.action.swf4.ActionCharToAscii;
import com.jpexs.decompiler.flash.action.swf4.ActionDivide;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetTime;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionLess;
import com.jpexs.decompiler.flash.action.swf4.ActionMBAsciiToChar;
import com.jpexs.decompiler.flash.action.swf4.ActionMBStringLength;
import com.jpexs.decompiler.flash.action.swf4.ActionMultiply;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionOr;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionStringAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionStringEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionStringLength;
import com.jpexs.decompiler.flash.action.swf4.ActionStringLess;
import com.jpexs.decompiler.flash.action.swf4.ActionSubtract;
import com.jpexs.decompiler.flash.action.swf4.ActionToInteger;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionAdd2;
import com.jpexs.decompiler.flash.action.swf5.ActionBitAnd;
import com.jpexs.decompiler.flash.action.swf5.ActionBitLShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitOr;
import com.jpexs.decompiler.flash.action.swf5.ActionBitRShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitURShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitXor;
import com.jpexs.decompiler.flash.action.swf5.ActionCallFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDecrement;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionIncrement;
import com.jpexs.decompiler.flash.action.swf5.ActionLess2;
import com.jpexs.decompiler.flash.action.swf5.ActionModulo;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.action.swf5.ActionToNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionToString;
import com.jpexs.decompiler.flash.action.swf5.ActionTypeOf;
import com.jpexs.decompiler.flash.action.swf6.ActionGreater;
import com.jpexs.decompiler.flash.action.swf6.ActionStringGreater;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.PushItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ActionDeobfuscatorSimpleFastOld implements SWFDecompilerListener {

    private final int executionLimit = 30000;

    @Override
    public void actionListParsed(ActionList actions, SWF swf) throws InterruptedException {
        FastActionList fastActions = new FastActionList(actions);
        fastActions.expandPushes();
        Map<String, Object> fakeFunctions = getFakeFunctionResults(fastActions);
        boolean changed = true;
        boolean useVariables = false;
        while (changed) {
            changed = removeGetTimes(fastActions);
            changed |= removeObfuscationIfs(fastActions, fakeFunctions, useVariables);
            changed |= removeObfuscatedUnusedVariables(fastActions);

            actions.setActions(fastActions.toActionList());
            changed |= ActionListReader.fixConstantPools(null, actions);
            if (!changed && !useVariables) {
                useVariables = true;
                changed = true;
            }
        }

    }

    private boolean removeGetTimes(FastActionList actions) {
        if (actions.isEmpty()) {
            return false;
        }

        boolean ret = false;
        boolean changed = true;
        int getTimeCount = 1;
        while (changed && getTimeCount > 0) {
            changed = false;
            actions.removeUnreachableActions();
            actions.removeZeroJumps();
            getTimeCount = 0;

            // GetTime, If => Jump, assume GetTime > 0
            FastActionListIterator iterator = actions.iterator();
            while (iterator.hasNext()) {
                Action a = iterator.next().action;
                ActionItem a2Item = iterator.peek(0);
                Action a2 = a2Item.action;
                boolean isGetTime = a instanceof ActionGetTime;
                if (isGetTime) {
                    getTimeCount++;
                }

                if (isGetTime && a2 instanceof ActionIf) {
                    ActionJump jump = new ActionJump(0);
                    ActionItem jumpItem = new ActionItem(jump);
                    jumpItem.setJumpTarget(a2Item.getJumpTarget());
                    iterator.remove(); // GetTime
                    iterator.next();
                    iterator.remove(); // If
                    iterator.add(jumpItem); // replace If with Jump
                    changed = true;
                    ret = true;
                    getTimeCount--;
                }
            }

            if (!changed && getTimeCount > 0) {
                // GetTime, Increment If => Jump
                iterator = actions.iterator();
                while (iterator.hasNext()) {
                    Action a = iterator.next().action;
                    Action a1 = iterator.peek(0).action;
                    ActionItem a2Item = iterator.peek(1);
                    Action a2 = a2Item.action;
                    if (a instanceof ActionGetTime && a1 instanceof ActionIncrement && a2 instanceof ActionIf) {
                        ActionJump jump = new ActionJump(0);
                        ActionItem jumpItem = new ActionItem(jump);
                        jumpItem.setJumpTarget(a2Item.getJumpTarget());
                        iterator.remove(); // GetTime
                        iterator.next();
                        iterator.remove(); // Increment
                        iterator.next();
                        iterator.remove(); // If
                        iterator.add(jumpItem); // replace If with Jump
                        changed = true;
                        ret = true;
                    }
                }
            }
        }

        return ret;
    }

    private boolean removeObfuscationIfs(FastActionList actions, Map<String, Object> fakeFunctions, boolean useVariables) throws InterruptedException {
        if (actions.isEmpty()) {
            return false;
        }

        boolean ret = false;
        actions.removeUnreachableActions();
        actions.removeZeroJumps();

        ActionConstantPool cPool = getConstantPool(actions);
        FastActionListIterator iterator = actions.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            ActionItem actionItem = iterator.next();
            ExecutionResult result = new ExecutionResult();
            executeActions(actionItem, cPool, result, fakeFunctions, useVariables, first);

            if (result.item != null && result.resultValue == null) {
                int newIstructionCount = 1 /*jump */ + result.stack.size();
                if (result.constantPool != null) {
                    newIstructionCount++;
                }

                newIstructionCount += 3 * result.variables.size(); /* 2x Push + Set or Define */

                boolean allValueValid = true;
                for (Object value : result.variables.values()) {
                    if (!ActionPush.isValidValue(value)) {
                        allValueValid = false;
                        break;
                    }
                }

                int unreachableCount = actions.getUnreachableActionCount(actionItem, result.item);

                if (allValueValid && newIstructionCount < unreachableCount) {
                    if (result.stack.isEmpty() && result.variables.isEmpty() && result.constantPool == null && actionItem.action instanceof ActionJump) {
                        actionItem.setJumpTarget(result.item);
                    } else {
                        ActionItem prevActionItem = actionItem.prev;
                        if (result.constantPool != null) {
                            ActionConstantPool constantPool2 = new ActionConstantPool(new ArrayList<>(result.constantPool.constantPool));
                            ActionItem constantPoolItem = new ActionItem(constantPool2);
                            iterator.addBefore(constantPoolItem);
                        }

                        for (String variableName : result.variables.keySet()) {
                            Object value = result.variables.get(variableName);
                            ActionPush push = new ActionPush(variableName);
                            ActionItem pushItem = new ActionItem(push);
                            iterator.addBefore(pushItem);
                            push = new ActionPush(value);
                            pushItem = new ActionItem(push);
                            iterator.addBefore(pushItem);

                            if (result.defines.contains(variableName)) {
                                ActionDefineLocal defineLocal = new ActionDefineLocal();
                                ActionItem defineLocalItem = new ActionItem(defineLocal);
                                iterator.addBefore(defineLocalItem);
                            } else {
                                ActionSetVariable setVariable = new ActionSetVariable();
                                ActionItem setVariableItem = new ActionItem(setVariable);
                                iterator.addBefore(setVariableItem);
                            }
                        }

                        if (!result.stack.isEmpty()) {
                            for (GraphTargetItem graphTargetItem : result.stack) {
                                ActionPush push = new ActionPush(graphTargetItem.getResult());
                                ActionItem pushItem = new ActionItem(push);
                                iterator.addBefore(pushItem);
                            }
                        }

                        ActionJump jump = new ActionJump(0);
                        ActionItem jumpItem = new ActionItem(jump);
                        jumpItem.setJumpTarget(result.item);
                        iterator.addBefore(jumpItem);

                        actions.replaceJumpTargets(actionItem, prevActionItem.next);
                    }

                    ActionItem prevItem = actionItem.prev;

                    actions.removeUnreachableActions();
                    actions.removeZeroJumps();

                    iterator.setCurrent(prevItem.next.next);
                    ret = true;
                }
            }

            first = false;
        }

        return ret;
    }

    private boolean removeObfuscatedUnusedVariables(FastActionList actions) throws InterruptedException {
        if (actions.isEmpty()) {
            return false;
        }

        Map<String, Integer> pushValues = getPushValues(actions);

        boolean ret = false;

        // Push, Push DefineLocal => remove when first pushed value is obfuscated and never used
        FastActionListIterator iterator = actions.iterator();
        while (iterator.hasNext()) {
            Action a = iterator.next().action;
            Action a1 = iterator.peek(0).action;
            Action a2 = iterator.peek(1).action;
            if (a instanceof ActionPush && a1 instanceof ActionPush && a2 instanceof ActionDefineLocal) {
                ActionPush pushName = (ActionPush) a;
                ActionPush pushValue = (ActionPush) a1;
                if (pushName.values.size() == 1 && pushValue.values.size() == 1) {
                    String strName = EcmaScript.toString(pushName.values.get(0), pushName.constantPool);
                    if (isFakeName(strName) && pushValues.get(strName) == 1) {
                        iterator.remove(); // Push name
                        iterator.next();
                        iterator.remove(); // Push value
                        iterator.next();
                        iterator.remove(); // DefineLocal
                        ret = true;
                    }
                }
            }
        }

        return ret;
    }

    private Map<String, Integer> getPushValues(FastActionList actions) {
        Map<String, Integer> ret = new HashMap<>();
        for (ActionItem actionItem : actions) {
            Action action = actionItem.action;
            if (action instanceof ActionPush) {
                ActionPush push = (ActionPush) action;
                for (int i = 0; i < push.values.size(); i++) {
                    String str = EcmaScript.toString(push.values.get(i), push.constantPool);
                    Integer cnt = ret.get(str);
                    cnt = cnt == null ? 1 : cnt + 1;
                    ret.put(str, cnt);
                }
            }
        }

        return ret;
    }

    private ActionConstantPool getConstantPool(FastActionList actions) {
        ActionConstantPool cPool = null;
        for (ActionItem actionItem : actions) {
            Action action = actionItem.action;
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

    private Map<String, Object> getFakeFunctionResults(FastActionList actions) throws InterruptedException {
        /*
         DefineFunction "fakeName" 0  {
         Push 1777
         Return
         }
         */

        Map<String, Object> results = new HashMap<>();

        for (ActionItem actionItem : actions) {
            Action action = actionItem.action;
            if (action instanceof ActionDefineFunction) {
                ActionDefineFunction def = (ActionDefineFunction) action;
                if (def.paramNames.isEmpty() && def.functionName.length() > 0) {
                    // remove funcion only when the function name contains only non printable characters
                    if (!isFakeName(def.functionName)) {
                        continue;
                    }

                    ExecutionResult result = new ExecutionResult();
                    ActionItem lastActionItem = actionItem.getContainerLastActions().get(0);
                    // has at least 1 inner item
                    if (lastActionItem != actionItem) {
                        actions.setExcludedFlags(true);
                        ActionItem actionItem2 = actionItem;
                        do {
                            actionItem2.excluded = false;
                            actionItem2 = actionItem2.next;
                        } while (actionItem2 != lastActionItem && actionItem2 != actions.last());
                        actionItem2.excluded = false;
                        executeActions(actionItem.next, null, result, null, true, false);
                        if (result.resultValue != null) {
                            results.put(def.functionName, result.resultValue);
                            actions.removeIncludedActions();
                        }
                    }
                }
            }
        }

        actions.setExcludedFlags(false);
        return results;
    }

    protected boolean isFakeName(String name) {
        for (char ch : name.toCharArray()) {
            if (ch > 31) {
                return false;
            }
        }

        return true;
    }

    private void executeActions(ActionItem item, ActionConstantPool constantPool, ExecutionResult result, Map<String, Object> fakeFunctions, boolean useVariables, boolean allowGetUninitializedVariables) throws InterruptedException {
        List<GraphTargetItem> output = new ArrayList<>();
        ActionLocalData localData = new ActionLocalData();
        FixItemCounterTranslateStack stack = new FixItemCounterTranslateStack("");
        int instructionsProcessed = 0;
        ActionConstantPool lastConstantPool = null;

        while (true) {
            if (item.isExcluded()) {
                break;
            }

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            if (instructionsProcessed > executionLimit) {
                break;
            }

            Action action = item.action;

            /*System.out.print(action.getASMSource(actions, new ArrayList<Long>(), ScriptExportMode.PCODE));
             for (int j = 0; j < stack.size(); j++) {
             System.out.print(" '" + stack.get(j).getResult() + "'");
             }
             System.out.println();*/
            if (action instanceof ActionConstantPool) {
                lastConstantPool = (ActionConstantPool) action;
            }

            if (action instanceof ActionDefineLocal) {
                if (stack.size() < 2) {
                    break;
                }

                String variableName = stack.peek(2).getResult().toString();
                result.defines.add(variableName);
            }

            if (action instanceof ActionGetVariable) {
                if (stack.isEmpty()) {
                    break;
                }

                GraphTargetItem variableNameObj = stack.peek();
                if (!(variableNameObj instanceof DirectValueActionItem)) {
                    // avoid dynamic variable names, for example: eval("item" add i);
                    break;
                }

                String variableName = variableNameObj.getResult().toString();
                if (!localData.variables.containsKey(variableName)
                        && (!allowGetUninitializedVariables || !isFakeName(variableName))) {
                    break;
                }
            }

            if (action instanceof ActionSetVariable) {
                if (stack.size() < 2) {
                    break;
                }

                if (!(stack.peek(2) instanceof DirectValueActionItem)) {
                    // avoid dynamic variable names, for example: set("item" add i, 1);
                    break;
                }
            }

            if (action instanceof ActionCallFunction) {
                if (stack.size() < 2) {
                    break;
                }

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
                // do not throw EmptyStackException, much faster
                int requiredStackSize = action.getStackPopCount(localData, stack);
                if (stack.size() < requiredStackSize) {
                    break;
                }

                action.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");
            }

            if (!useVariables && (action instanceof ActionDefineLocal
                    || action instanceof ActionGetVariable
                    || action instanceof ActionSetVariable
                    || action instanceof ActionConstantPool
                    || action instanceof ActionCallFunction
                    || action instanceof ActionReturn
                    || action instanceof ActionEnd)) {
                break;
            }

            if (!(action instanceof ActionPush
                    || action instanceof ActionPushDuplicate
                    //|| action instanceof ActionPop
                    || action instanceof ActionAsciiToChar
                    || action instanceof ActionCharToAscii
                    || action instanceof ActionDecrement
                    || action instanceof ActionIncrement
                    || action instanceof ActionNot
                    || action instanceof ActionToInteger
                    || action instanceof ActionToNumber
                    || action instanceof ActionToString
                    || action instanceof ActionTypeOf
                    || action instanceof ActionStringLength
                    || action instanceof ActionMBAsciiToChar
                    || action instanceof ActionMBStringLength
                    || action instanceof ActionAnd
                    || action instanceof ActionAdd
                    || action instanceof ActionAdd2
                    || action instanceof ActionBitAnd
                    || action instanceof ActionBitLShift
                    || action instanceof ActionBitOr
                    || action instanceof ActionBitRShift
                    || action instanceof ActionBitURShift
                    || action instanceof ActionBitXor
                    || action instanceof ActionDivide
                    || action instanceof ActionEquals
                    || action instanceof ActionEquals2
                    || action instanceof ActionGreater
                    || action instanceof ActionLess
                    || action instanceof ActionLess2 // todo: fix (tz.swf/frame_6/DoAction: _loc3_.icon.gotoAndStop((Number(item.cost) || 0) >= 0?1:2)
                    || action instanceof ActionModulo
                    || action instanceof ActionMultiply
                    || action instanceof ActionOr
                    || action instanceof ActionStringAdd
                    || action instanceof ActionStringEquals
                    || action instanceof ActionStringGreater
                    || action instanceof ActionStringLess
                    || action instanceof ActionSubtract
                    || action instanceof ActionIf
                    || action instanceof ActionJump
                    || action instanceof ActionDefineLocal
                    || action instanceof ActionGetVariable
                    || action instanceof ActionSetVariable
                    || action instanceof ActionConstantPool
                    || action instanceof ActionCallFunction
                    || action instanceof ActionReturn
                    || action instanceof ActionEnd)) {
                break;
            }

            if (action instanceof ActionPush) {
                ActionPush push = (ActionPush) action;
                boolean ok = true;
                instructionsProcessed += push.values.size() - 1;
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

            ActionItem prevItem = item;
            if (action instanceof ActionJump) {
                item = item.getJumpTarget();
            } else if (action instanceof ActionIf) {
                if (stack.isEmpty()) {
                    break;
                }

                if (EcmaScript.toBoolean(stack.pop().getResult())) {
                    item = item.getJumpTarget();
                } else {
                    item = item.next;
                }
            } else {
                item = item.next;
            }

            instructionsProcessed++;

            if ((stack.allItemsFixed() || action instanceof ActionEnd) && !(action instanceof ActionPush)) {
                result.item = item;
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
            } else if (action instanceof ActionEnd) {
                result.item = prevItem;
                break;
            }
        }
    }

    @Override
    public void actionTreeCreated(List<GraphTargetItem> tree, SWF swf) {
        if (tree.size() > 0) {
            GraphTargetItem firstItem = tree.get(0);
            if (firstItem instanceof PushItem && firstItem.value instanceof FalseItem) {
                tree.remove(0);
            }
        }
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

    @Override
    public void avm2CodeRemoveTraps(String path, int classIndex, boolean isStatic, int scriptIndex, ABC abc, Trait trait, int methodInfo, MethodBody body) throws InterruptedException {
    }

    class ExecutionResult {

        public ActionItem item = null;

        public int instructionsProcessed = -1;

        public TranslateStack stack = new TranslateStack("?");

        public Object resultValue;

        public ActionConstantPool constantPool;

        public Map<String, Object> variables = new HashMap<>();

        public Set<String> defines = new HashSet<>();
    }
}

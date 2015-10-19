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
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.fastactionlist.ActionItem;
import com.jpexs.decompiler.flash.action.fastactionlist.FastActionList;
import com.jpexs.decompiler.flash.action.fastactionlist.FastActionListIterator;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionAnd;
import com.jpexs.decompiler.flash.action.swf4.ActionAsciiToChar;
import com.jpexs.decompiler.flash.action.swf4.ActionCharToAscii;
import com.jpexs.decompiler.flash.action.swf4.ActionDivide;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetTime;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionLess;
import com.jpexs.decompiler.flash.action.swf4.ActionMBAsciiToChar;
import com.jpexs.decompiler.flash.action.swf4.ActionMBStringLength;
import com.jpexs.decompiler.flash.action.swf4.ActionMultiply;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionOr;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
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
import com.jpexs.decompiler.flash.action.swf5.ActionDecrement;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionIncrement;
import com.jpexs.decompiler.flash.action.swf5.ActionLess2;
import com.jpexs.decompiler.flash.action.swf5.ActionModulo;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JPEXS
 */
public class ActionDeobfuscatorSimpleFast implements SWFDecompilerListener {

    private final int executionLimit = 30000;

    @Override
    public void actionListParsed(ActionList actions, SWF swf) throws InterruptedException {
        FastActionList fastActions = new FastActionList(actions);
        fastActions.expandPushes();
        boolean changed = true;
        while (changed) {
            changed = removeGetTimes(fastActions);
            changed |= removeObfuscationIfs(fastActions);
            changed |= removeObfuscatedUnusedVariables(fastActions);
        }

        actions.setActions(fastActions.toActionList());
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

    private boolean removeObfuscationIfs(FastActionList actions) throws InterruptedException {
        if (actions.isEmpty()) {
            return false;
        }

        boolean ret = false;
        actions.removeUnreachableActions();
        actions.removeZeroJumps();

        FastActionListIterator iterator = actions.iterator();
        while (iterator.hasNext()) {
            ActionItem actionItem = iterator.next();
            ExecutionResult result = new ExecutionResult();
            executeActions(actionItem, actions.last(), result);

            if (result.item != null) {
                int newIstructionCount = 1 /*jump */ + result.stack.size();
                int unreachableCount = actions.getUnreachableActionCount(actionItem, result.item);

                if (newIstructionCount < unreachableCount) {
                    if (result.stack.isEmpty() && actionItem.action instanceof ActionJump) {
                        actionItem.setJumpTarget(result.item);
                    } else {
                        ActionItem prevActionItem = actionItem.prev;
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

    protected boolean isFakeName(String name) {
        for (char ch : name.toCharArray()) {
            if (ch > 31) {
                return false;
            }
        }

        return true;
    }

    private void executeActions(ActionItem item, ActionItem endItem, ExecutionResult result) throws InterruptedException {
        List<GraphTargetItem> output = new ArrayList<>();
        ActionLocalData localData = new ActionLocalData();
        FixItemCounterTranslateStack stack = new FixItemCounterTranslateStack("");
        int instructionsProcessed = 0;

        while (true) {
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
            // do not throw EmptyStackException, much faster
            int requiredStackSize = action.getStackPopCount(localData, stack);
            if (stack.size() < requiredStackSize) {
                return;
            }

            action.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");

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
                    || action instanceof ActionJump)) {
                break;
            }

            if (action instanceof ActionPush) {
                ActionPush push = (ActionPush) action;
                boolean ok = true;
                instructionsProcessed += push.values.size() - 1;
                for (Object value : push.values) {
                    if (value instanceof ConstantIndex || value instanceof RegisterNumber) {
                        ok = false;
                        break;
                    }
                }
                if (!ok) {
                    break;
                }
            }

            if (action instanceof ActionJump) {
                item = item.getJumpTarget();
            } else if (action instanceof ActionIf) {
                if (stack.isEmpty()) {
                    return;
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

            if (stack.allItemsFixed() && !(action instanceof ActionPush)) {
                result.item = item;
                result.instructionsProcessed = instructionsProcessed;
                result.stack.clear();
                result.stack.addAll(stack);
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

    class ExecutionResult {

        public ActionItem item = null;

        public int instructionsProcessed = -1;

        public TranslateStack stack = new TranslateStack("?");

        public Object resultValue;
    }
}

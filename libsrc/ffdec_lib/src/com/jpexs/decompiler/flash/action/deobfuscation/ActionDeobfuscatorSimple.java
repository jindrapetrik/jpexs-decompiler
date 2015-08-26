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
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionCharToAscii;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetTime;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionMultiply;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSubtract;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionAdd2;
import com.jpexs.decompiler.flash.action.swf5.ActionBitAnd;
import com.jpexs.decompiler.flash.action.swf5.ActionBitLShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitOr;
import com.jpexs.decompiler.flash.action.swf5.ActionBitRShift;
import com.jpexs.decompiler.flash.action.swf5.ActionBitXor;
import com.jpexs.decompiler.flash.action.swf5.ActionIncrement;
import com.jpexs.decompiler.flash.action.swf5.ActionModulo;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class ActionDeobfuscatorSimple implements SWFDecompilerListener {

    private final int executionLimit = 30000;

    @Override
    public void actionListParsed(ActionList actions, SWF swf) throws InterruptedException {
        removeGetTimes(actions);
        removeObfuscationIfs(actions);
    }

    private boolean removeGetTimes(ActionList actions) {
        if (actions.size() == 0) {
            return false;
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            removeUnreachableActions(actions);
            removeZeroJumps(actions);

            // GetTime, If => Jump, assume GetTime > 0
            for (int i = 0; i < actions.size() - 1; i++) {
                Action a = actions.get(i);
                Action a2 = actions.get(i + 1);
                if (a instanceof ActionGetTime && a2 instanceof ActionIf) {
                    ActionIf aIf = (ActionIf) a2;
                    ActionJump jump = new ActionJump(0);
                    jump.setAddress(aIf.getAddress());
                    jump.setJumpOffset(aIf.getJumpOffset());
                    actions.remove(i); // GetTime
                    actions.remove(i); // If
                    actions.addAction(i, jump); // replace If with Jump
                    changed = true;
                    break;
                }
            }

            if (!changed) {
                // GetTime, Increment If => Jump
                for (int i = 0; i < actions.size() - 2; i++) {
                    Action a = actions.get(i);
                    Action a1 = actions.get(i + 1);
                    Action a2 = actions.get(i + 2);
                    if (a instanceof ActionGetTime && a1 instanceof ActionIncrement && a2 instanceof ActionIf) {
                        ActionIf aIf = (ActionIf) a2;
                        ActionJump jump = new ActionJump(0);
                        jump.setAddress(aIf.getAddress());
                        jump.setJumpOffset(aIf.getJumpOffset());
                        actions.remove(i); // GetTime
                        actions.remove(i); // Increment
                        actions.remove(i); // If
                        actions.addAction(i, jump); // replace If with Jump
                        changed = true;
                        break;
                    }
                }
            }
        }

        return false;
    }

    private boolean removeObfuscationIfs(ActionList actions) throws InterruptedException {
        if (actions.size() == 0) {
            return false;
        }

        removeUnreachableActions(actions);
        removeZeroJumps(actions);

        for (int i = 0; i < actions.size(); i++) {
            ExecutionResult result = new ExecutionResult();
            executeActions(actions, i, actions.size() - 1, result);

            if (result.idx != -1) {
                int newIstructionCount = 1; // jump
                if (!result.stack.isEmpty()) {
                    newIstructionCount += result.stack.size();
                }

                if (newIstructionCount < result.instructionsProcessed) {
                    Action target = actions.get(result.idx);
                    Action prevAction = actions.get(i);

                    if (result.stack.isEmpty() && prevAction instanceof ActionJump) {
                        ActionJump jump = (ActionJump) prevAction;
                        jump.setJumpOffset((int) (target.getAddress() - jump.getAddress() - jump.getTotalActionLength()));
                    } else {
                        if (!result.stack.isEmpty()) {
                            ActionPush push = new ActionPush(0);
                            push.values.clear();
                            for (GraphTargetItem graphTargetItem : result.stack) {
                                push.values.add(graphTargetItem.getResult());
                            }
                            push.setAddress(prevAction.getAddress());
                            actions.addAction(i++, push);
                            prevAction = push;
                        }

                        ActionJump jump = new ActionJump(0);
                        jump.setAddress(prevAction.getAddress());
                        jump.setJumpOffset((int) (target.getAddress() - jump.getAddress() - jump.getTotalActionLength()));
                        actions.addAction(i++, jump);
                    }

                    Action nextAction = actions.size() > i ? actions.get(i) : null;

                    removeUnreachableActions(actions);
                    removeZeroJumps(actions);

                    if (nextAction != null) {
                        int nextIdx = actions.indexOf(nextAction);
                        if (nextIdx < i) {
                            i = nextIdx;
                        }
                    }
                }
            }
        }

        return false;
    }

    protected boolean removeUnreachableActions(ActionList actions) {
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

    protected boolean removeZeroJumps(ActionList actions) {
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

    private void executeActions(ActionList actions, int idx, int endIdx, ExecutionResult result) throws InterruptedException {
        List<GraphTargetItem> output = new ArrayList<>();
        ActionLocalData localData = new ActionLocalData();
        FixItemCounterTranslateStack stack = new FixItemCounterTranslateStack("");
        int instructionsProcessed = 0;

        try {
            while (true) {
                if (idx > endIdx) {
                    break;
                }

                if (instructionsProcessed > executionLimit) {
                    break;
                }

                Action action = actions.get(idx);

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
                        || action instanceof ActionCharToAscii
                        || action instanceof ActionAdd
                        || action instanceof ActionAdd2
                        || action instanceof ActionSubtract
                        || action instanceof ActionModulo
                        || action instanceof ActionMultiply
                        || action instanceof ActionBitXor
                        || action instanceof ActionBitAnd
                        || action instanceof ActionBitOr
                        || action instanceof ActionBitLShift
                        || action instanceof ActionBitRShift
                        || action instanceof ActionEquals
                        || action instanceof ActionNot
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
                    if (stack.isEmpty()) {
                        return;
                    }

                    if (EcmaScript.toBoolean(stack.pop().getResult())) {
                        long address = aif.getAddress() + aif.getTotalActionLength() + aif.getJumpOffset();
                        idx = actions.indexOf(actions.getByAddress(address));
                        if (idx == -1) {
                            throw new TranslateException("If target not found: " + address);
                        }
                    }
                }

                instructionsProcessed++;

                if (stack.allItemsFixed()) {
                    result.idx = idx == actions.size() ? idx - 1 : idx;
                    result.instructionsProcessed = instructionsProcessed;
                    result.stack.clear();
                    result.stack.addAll(stack);
                }
            }
        } catch (EmptyStackException | TranslateException ex) {
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

        public int idx = -1;

        public int instructionsProcessed = -1;

        public TranslateStack stack = new TranslateStack("?");

        public Object resultValue;
    }
}

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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
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
import com.jpexs.decompiler.flash.action.swf5.ActionModulo;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ActionDeobfuscatorSimple implements SWFDecompilerListener {

    private final int executionLimit = 30000;

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {
        removeObfuscationIfs(actions);
    }

    private boolean removeObfuscationIfs(ActionList actions) {
        if (actions.size() == 0) {
            return false;
        }

        for (int i = 0; i < actions.size(); i++) {
            ExecutionResult result = new ExecutionResult();
            executeActions(actions, i, actions.size() - 1, result);

            if (result.idx != -1) {
                int newIstructionCount = 1; // jump
                if (!result.stack.isEmpty()) {
                    newIstructionCount++;
                }

                if (newIstructionCount * 2 < result.instructionsProcessed) {
                    Action target = actions.get(result.idx);
                    Action prevAction = actions.get(i);

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
                }
            }
        }

        return false;
    }

    private void executeActions(ActionList actions, int idx, int endIdx, ExecutionResult result) {
        List<GraphTargetItem> output = new ArrayList<>();
        ActionLocalData localData = new ActionLocalData();
        FixItemCounterTranslateStack stack = new FixItemCounterTranslateStack();
        int instructionsProcessed = 0;

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
                action.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");

                if (!(action instanceof ActionPush
                        || action instanceof ActionPushDuplicate
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
                        || action instanceof ActionIf)) {
                    break;
                }

                if (action instanceof ActionPush) {
                    ActionPush push = (ActionPush) action;
                    boolean ok = true;
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

                if (stack.allItemsFixed()) {
                    result.idx = idx == actions.size() ? idx - 1 : idx;
                    result.instructionsProcessed = instructionsProcessed;
                    result.stack.clear();
                    result.stack.addAll(stack);
                }
            }
        } catch (EmptyStackException | TranslateException | InterruptedException ex) {
        }
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
        public TranslateStack stack = new TranslateStack();
        public Object resultValue;
    }
}

/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionNot;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionSubtract;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionAdd2;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
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
        removeFakeFunction(actions);
        removeUnreachableActions(actions);
        removeObfuscationIfs(actions);
        removeUnreachableActions(actions);
        removeZeroJumps(actions);
        rereadActionList(actions, swf); // this call will fix the contant pool assigments
    }
    
    private void combinePushs(ActionList actions) {
        for (int i = 0; i < actions.size() - 1; i++) {
            Action action = actions.get(i); 
            Action action2 = actions.get(i + 1); 
            if (action instanceof ActionPush && action2 instanceof ActionPush) {
                if (actions.getReferencesFor(action2).hasNext()) {
                    ActionPush push = (ActionPush) action; 
                    ActionPush push2 = (ActionPush) action2; 
                    push.values.addAll(push2.values);
                    actions.remove(i + 1);
                    i--;
                }
            }
        }
    }
    
    private boolean rereadActionList(ActionList actions, SWF swf) {
        byte[] actionBytes = Action.actionsToBytes(actions, true, SWF.DEFAULT_VERSION);
        try {
            SWFInputStream rri = new SWFInputStream(swf, actionBytes);
            ActionList newActions = ActionListReader.readActionList(new ArrayList<DisassemblyListener>(), rri, SWF.DEFAULT_VERSION, 0, actionBytes.length, "", false);
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
    
    private boolean removeObfuscationIfs(ActionList actions) {
        if (actions.size() == 0) {
            return false;
        }
        
        for (int i = 0; i < actions.size(); i++) {
            int idx = i;
            List<GraphTargetItem> output = new ArrayList<>();
            ActionLocalData localData = new ActionLocalData();
            Stack<GraphTargetItem> stack = new Stack<>();

            try {
                int lastOkIdx = -1;
                int lastOkInstructionsProcessed = -1;
                int instructionsProcessed = 0;
                Map<String, Object> lastOkVariables = new HashMap<>();
                Set<String> defines = new HashSet<>();
                ActionConstantPool lastOkConstantPool = null;
                ActionConstantPool constantPool = null;
                while (true) {
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
                        constantPool = (ActionConstantPool) action;
                    }
                    
                    if (action instanceof ActionDefineLocal) {
                        GraphTargetItem top = stack.pop();
                        String variableName = stack.peek().getResult().toString();
                        defines.add(variableName);
                        stack.push(top);
                    }
                    
                    action.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");

                    if (!(action instanceof ActionPush ||
                            action instanceof ActionAdd ||
                            action instanceof ActionAdd2 ||
                            action instanceof ActionSubtract ||
                            action instanceof ActionDefineLocal ||
                            action instanceof ActionJump ||
                            action instanceof ActionGetVariable ||
                            action instanceof ActionSetVariable ||
                            action instanceof ActionEquals ||
                            action instanceof ActionNot ||
                            action instanceof ActionIf ||
                            action instanceof ActionConstantPool)) {
                        break;
                    }
                    
                    if (action instanceof ActionPush) {
                        ActionPush push = (ActionPush) action;
                        boolean ok = true; 
                        for (Object value : push.values) {
                            if (value instanceof ConstantIndex) {
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
                            int a = 1;
                        }
                    }

                    if (action instanceof ActionIf) {
                        ActionIf aif = (ActionIf) action;
                        if (EcmaScript.toBoolean(stack.pop().getResult())) {
                            System.out.println("if true");
                            long address = aif.getAddress() + aif.getTotalActionLength() + aif.getJumpOffset();
                            idx = actions.indexOf(actions.getByAddress(address));
                            if (idx == -1) {
                                int a = 1;
                            }
                        }
                    }

                    if (/*localData.variables.size() == 1 && */stack.empty()) {
                        lastOkIdx = idx;
                        lastOkInstructionsProcessed = instructionsProcessed;
                        lastOkConstantPool = constantPool;
                        lastOkVariables.clear();
                        for (String variableName : localData.variables.keySet()) {
                            Object value = localData.variables.get(variableName).getResult();
                            lastOkVariables.put(variableName, value);
                        }
                    }
                }
                
                if (lastOkIdx != -1) {
                    int newIstructionCount = constantPool != null ? 2 : 1;
                    newIstructionCount += 2 * lastOkVariables.size();
                    
                    if (newIstructionCount < lastOkInstructionsProcessed) {
                        Action target = actions.get(lastOkIdx);
                        Action prevAction = actions.get(i);

                        if (constantPool != null) {
                            actions.addAction(i++, constantPool);
                            prevAction = constantPool;
                        }

                        for (String variableName : lastOkVariables.keySet()) {
                            Object value = lastOkVariables.get(variableName);
                            ActionPush push = new ActionPush(variableName);
                            push.values.add(value);
                            push.setAddress(prevAction.getAddress());
                            actions.addAction(i++, push);
                            prevAction = push;

                            if (defines.contains(variableName)) {
                                Action defineLocal = new ActionDefineLocal();
                                defineLocal.setAddress(prevAction.getAddress());
                                actions.addAction(i++, defineLocal);
                                prevAction = defineLocal;
                            } else {
                                Action setVariable = new ActionSetVariable();
                                setVariable.setAddress(prevAction.getAddress());
                                actions.addAction(i++, setVariable);
                                prevAction = setVariable;
                            }
                        }

                        ActionJump jump = new ActionJump(0);
                        jump.setAddress(prevAction.getAddress());
                        jump.setJumpOffset((int) (target.getAddress() - jump.getAddress() - jump.getTotalActionLength()));
                        actions.addAction(i++, jump);

                        return true;
                    }
                }
            } catch (EmptyStackException | TranslateException | InterruptedException ex) {
            }
        }
        
        return false;
    }

    private boolean removeFakeFunction(ActionList actions) {
        /*
            DefineFunction "fakeName" 0  {
                Push 1777
                Return
            }        
        */
        
        for (int i = 0; i < actions.size() - 2; i++) {
            Action action = actions.get(i); 
            if (action instanceof ActionDefineFunction) {
                Action action2 = actions.get(i + 1);
                Action action3 = actions.get(i + 2);
                if (action2 instanceof ActionPush && action3 instanceof ActionReturn) {
                    ActionDefineFunction def = (ActionDefineFunction) action;
                    ActionPush push = (ActionPush) action2;
                    if (def.paramNames.isEmpty() && push.values.size() == 1) {
                        Object pushValueObj = push.values.get(0);
                        if (pushValueObj instanceof Long) {
                            String functionName = def.functionName;
                            long pushValue = (Long) pushValueObj;
                            for (int j = 0; j < 3; j++) {
                                actions.removeAction(i);
                            }

                            for (int j = 0; j < actions.size() - 1; j++) {
                                action = actions.get(j); 
                                if (action instanceof ActionPush) {
                                    push = (ActionPush) action;
                                    int pushValuesCount = push.values.size(); 
                                    if (pushValuesCount >= 2
                                            && push.values.get(pushValuesCount - 1).equals(functionName)) {
                                        push.values.remove(pushValuesCount - 1);
                                        push.values.remove(pushValuesCount - 2);
                                        push.values.add(pushValue);
                                        actions.removeAction(j + 1);
                                    }
                                }
                            }

                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
}

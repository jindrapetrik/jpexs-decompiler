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
package com.jpexs.decompiler.flash.abc.avm2;

import com.jpexs.decompiler.flash.action.deobfuscation.*;
import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.AddIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.AddIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.ModuloIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.MultiplyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.NotIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.SubtractIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.SubtractIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitAndIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitOrIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.BitXorIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.LShiftIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.RShiftIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.bitwise.URShiftIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.EqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.GreaterEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.GreaterThanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.LessEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.LessThanIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.comparison.StrictEqualsIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.NewFunctionIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.GetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.localregs.SetLocalTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushDoubleIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushFalseIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushNullIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushShortIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushStringIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushTrueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushUndefinedIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.SwapIns;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.ActionListReader;
import com.jpexs.decompiler.flash.action.ActionLocalData;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.ReturnActionItem;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.swf4.ActionAdd;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionGetVariable;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionLess;
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
import com.jpexs.decompiler.flash.action.swf5.ActionLess2;
import com.jpexs.decompiler.flash.action.swf5.ActionModulo;
import com.jpexs.decompiler.flash.action.swf5.ActionPushDuplicate;
import com.jpexs.decompiler.flash.action.swf5.ActionReturn;
import com.jpexs.decompiler.flash.action.swf6.ActionGreater;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
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
 * AVM2 Deobfuscator - FIXME!!! Not ready yet!
 *
 * @author JPEXS
 */
public class AVM2Deobfuscator extends AVM2DeobfuscatorSimple {

    private final int executionLimit = 30000;

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {

    }

    @Override
    public void deobfuscate(int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body) throws InterruptedException {
        removeUnreachableActions(body.getCode(), cpool, trait, minfo, body);
        removeObfuscationIfs(classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body);
        removeUnreachableActions(body.getCode(), cpool, trait, minfo, body);
        removeZeroJumps(body.getCode(), body);
    }

    private boolean removeObfuscationIfs(int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body) {
        AVM2Code code = body.getCode();
        if (code.code.size() == 0) {
            return false;
        }
        System.err.println("=============================================");
        for (int i = 0; i < code.code.size(); i++) {
            ExecutionResult result = new ExecutionResult();
            System.err.println("Execute from " + i);
            executeActions(classIndex, isStatic, body, scriptIndex, abc, code, i, code.code.size() - 1, result);

            if (result.idx != -1) {
                int newIstructionCount = 1; // jump
                if (!result.stack.isEmpty()) {
                    newIstructionCount++;
                }
                newIstructionCount += 2 * result.variables.size();

                if (newIstructionCount * 2 < result.instructionsProcessed) {
                    AVM2Instruction target = code.code.get(result.idx);
                    AVM2Instruction prevAction = code.code.get(i);

                    for (int variableName : result.variables.keySet()) {
                        Object value = result.variables.get(variableName);
                        /*ActionPush push = new ActionPush(variableName);
                         push.values.add(value);*/
                        AVM2Instruction push = makePush(value, cpool);

                        code.insertInstruction(i++, push);
                        push.offset = prevAction.offset;

                        code.insertInstruction(i++, push);
                        prevAction = push;

                        /*if (result.defines.contains(variableName)) {
                         //ActionDefineLocal defineLocal = new ActionDefineLocal();
                         AVM2Instruction defineLocal = new AVM2Instruction(prevAction.offset, new SetLocalIns(), new int[]{});
                         defineLocal.setAddress(prevAction.getAddress());
                         code.addAction(i++, defineLocal);
                         prevAction = defineLocal;
                         } else {
                         ActionSetVariable setVariable = new ActionSetVariable();
                         setVariable.setAddress(prevAction.getAddress());
                         code.addAction(i++, setVariable);
                         prevAction = setVariable;
                         }*/
                        AVM2Instruction setVariable = new AVM2Instruction(prevAction.offset, new SetLocalIns(), new int[]{});
                        code.insertInstruction(i++, setVariable);
                        prevAction = setVariable;
                    }

                    if (!result.stack.isEmpty()) {
                        //ActionPush push = new ActionPush(0);
                        //push.values.clear();
                        long ofs = prevAction.offset;
                        for (GraphTargetItem graphTargetItem : result.stack) {
                            //DirectValueActionItem dv = (DirectValueActionItem) graphTargetItem;
                            //push.values.add(dv.value);
                            AVM2Instruction push = makePush(cpool, graphTargetItem);
                            push.offset = ofs;
                            code.insertInstruction(i++, push);
                            ofs += push.getBytes().length;
                            prevAction = push;
                        }
                    }

                    //ctionJump jump = new ActionJump(0);
                    AVM2Instruction jump = new AVM2Instruction(prevAction.offset, new JumpIns(), new int[]{0});
                    //jump.setAddress(prevAction.getAddress());
                    jump.operands[0] = (int) (target.offset - jump.offset - jump.getBytes().length);
                    code.insertInstruction(i++, jump);
                    return true;
                }
            }
        }

        return false;
    }

    private AVM2LocalData newLocalData(int scriptIndex, ABC abc, AVM2ConstantPool cpool, MethodBody body, boolean isStatic, int classIndex) {
        AVM2LocalData localData = new AVM2LocalData();
        localData.isStatic = isStatic;
        localData.classIndex = classIndex;
        localData.localRegs = new HashMap<>();
        localData.scopeStack = new ScopeStack();
        localData.constants = cpool;
        localData.methodInfo = abc.method_info;
        localData.methodBody = body;
        localData.abc = abc;
        localData.localRegNames = new HashMap<>();
        localData.fullyQualifiedNames = new ArrayList<>();
        localData.parsedExceptions = new ArrayList<>();
        localData.finallyJumps = new HashMap<>();
        localData.ignoredSwitches = new HashMap<>();
        localData.ignoredSwitches2 = new ArrayList<>();
        localData.scriptIndex = scriptIndex;
        localData.localRegAssignmentIps = new HashMap<>();
        localData.ip = 0;
        localData.refs = new HashMap<>();
        localData.code = body.getCode();
        return localData;
    }

    private void executeActions(int classIndex, boolean isStatic, MethodBody body, int scriptIndex, ABC abc, AVM2Code code, int idx, int endIdx, ExecutionResult result) {
        List<GraphTargetItem> output = new ArrayList<>();
        AVM2LocalData localData = newLocalData(scriptIndex, abc, abc.constants, body, isStatic, classIndex);
        localData.localRegs.put(0, new NullAVM2Item(null));//this
        FixItemCounterTranslateStack stack = new FixItemCounterTranslateStack("");
        int instructionsProcessed = 0;

        try {
            while (true) {
                if (idx > endIdx) {
                    break;
                }

                AVM2Instruction action = code.code.get(idx);
                instructionsProcessed++;

                if (instructionsProcessed > executionLimit) {
                    break;
                }

                /*if (action instanceof ActionDefineLocal) {
                 GraphTargetItem top = stack.pop();
                 String variableName = stack.peek().getResult().toString();
                 result.defines.add(variableName);
                 stack.push(top);
                 }*/
                if (action.definition instanceof GetLocalTypeIns) {
                    int regId = ((GetLocalTypeIns) action.definition).getRegisterId(action);//stack.peek().getResult().toString();
                    if (!localData.localRegs.containsKey(regId)) {
                        break;
                    }
                }

                /*if (action instanceof ActionCallFunction) {
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
                 }*/
                System.err.println("Translating " + action);
                action.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");
                Class allowedDefs[] = new Class[]{
                    PushByteIns.class,
                    PushShortIns.class,
                    PushIntIns.class,
                    PushDoubleIns.class,
                    PushStringIns.class,
                    PushNullIns.class,
                    PushUndefinedIns.class,
                    PushFalseIns.class,
                    PushTrueIns.class,
                    DupIns.class,
                    SwapIns.class,
                    AddIns.class,
                    AddIIns.class,
                    SubtractIns.class,
                    SubtractIIns.class,
                    ModuloIns.class,
                    MultiplyIns.class,
                    BitAndIns.class,
                    BitXorIns.class,
                    BitOrIns.class,
                    LShiftIns.class,
                    RShiftIns.class,
                    URShiftIns.class,
                    EqualsIns.class,
                    NotIns.class,
                    IfTypeIns.class,
                    JumpIns.class,
                    IncrementIns.class,
                    IncrementIIns.class,
                    DecrementIns.class,
                    DecrementIIns.class,
                    SetLocalTypeIns.class,
                    GetLocalTypeIns.class,
                    GreaterEqualsIns.class,
                    GreaterThanIns.class,
                    LessThanIns.class,
                    LessEqualsIns.class,
                    StrictEqualsIns.class,
                    IfTypeIns.class,
                    ReturnVoidIns.class,
                    ReturnValueIns.class,
                    NewFunctionIns.class,
                    PopIns.class,
                    PushScopeIns.class
                };

                InstructionDefinition def = action.definition;

                boolean ok = false;
                for (Class<?> s : allowedDefs) {
                    if (s.isAssignableFrom(def.getClass())) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    System.err.println("Broken");
                    break;
                }


                /*for (String variable : localData.variables.keySet()) {
                 System.out.println(Helper.byteArrToString(variable.getBytes()));
                 }*/
                idx++;

                if (action.definition instanceof JumpIns) {

                    long address = action.offset + action.getBytes().length + action.operands[0];
                    idx = code.adr2pos(address);//code.indexOf(code.getByAddress(address));
                    if (idx == -1) {
                        throw new TranslateException("Jump target not found: " + address);
                    }
                }

                if (action.definition instanceof IfTypeIns) {
                    if (EcmaScript.toBoolean(stack.pop().getResult())) {
                        long address = action.offset + action.getBytes().length + action.operands[0];
                        idx = code.adr2pos(address);
                        if (idx == -1) {
                            throw new TranslateException("If target not found: " + address);
                        }
                    }
                }

                if (/*localData.variables.size() == 1 && */stack.allItemsFixed()) {
                    result.idx = idx == code.code.size() ? idx - 1 : idx;
                    result.instructionsProcessed = instructionsProcessed;
                    result.variables.clear();
                    for (int variableName : localData.localRegs.keySet()) {
                        Object value = localData.localRegs.get(variableName).getResult();
                        result.variables.put(variableName, value);
                    }
                    result.stack.clear();
                    result.stack.addAll(stack);
                }

                if (action.definition instanceof ReturnValueIns) {
                    if (output.size() > 0) {
                        ReturnValueAVM2Item ret = (ReturnValueAVM2Item) output.get(output.size() - 1);
                        result.resultValue = ret.value.getResult();
                    }
                    break;
                }

                if (action.definition instanceof ReturnVoidIns) {
                    break;
                }
            }
        } catch (EmptyStackException | TranslateException | InterruptedException ex) {
            //ex.printStackTrace();
        }
    }

    /*private Map<String, Object> getFakeFunctionResults(ActionList actions) {

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
     }*/
    class ExecutionResult {

        public int idx = -1;

        public int instructionsProcessed = -1;

        //public ActionConstantPool constantPool;
        public Map<Integer, Object> variables = new HashMap<>();

        //public Set<String> defines = new HashSet<>();
        public TranslateStack stack = new TranslateStack("?");

        public ScopeStack scopeStack = new ScopeStack();

        public Object resultValue;
    }
}

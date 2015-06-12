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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.IfTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.AddIIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.AddIns;
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
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushByteIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushDoubleIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushFalseIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushIntIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushNullIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushShortIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushStringIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushTrueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PushUndefinedIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.SwapIns;
import com.jpexs.decompiler.flash.abc.avm2.model.FloatValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerListener;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AVM2DeobfuscatorSimple implements SWFDecompilerListener {

    private final int executionLimit = 30000;

    @Override
    public void actionListParsed(ActionList actions, SWF swf) {

    }

    protected AVM2Instruction makePush(Object ovalue, AVM2ConstantPool cpool) {
        if (ovalue instanceof Long) {
            long value = (Long) ovalue;
            if (value >= -128 && value <= 127) {
                return new AVM2Instruction(0, new PushByteIns(), new int[]{(int) (long) value});
            } else if (value >= -32768 && value <= 32767) {
                return new AVM2Instruction(0, new PushShortIns(), new int[]{((int) (long) value) & 0xffff});
            } else {
                return new AVM2Instruction(0, new PushIntIns(), new int[]{cpool.getIntId(value, true)});
            }
        }
        if (ovalue instanceof Double) {
            return new AVM2Instruction(0, new PushDoubleIns(), new int[]{cpool.getDoubleId((Double) ovalue, true)});
        }
        if (ovalue instanceof String) {
            return new AVM2Instruction(0, new PushStringIns(), new int[]{cpool.getStringId((String) ovalue, true)});
        }
        if (ovalue instanceof Boolean) {
            if ((Boolean) ovalue) {
                return new AVM2Instruction(0, new PushTrueIns(), new int[]{});
            }
            return new AVM2Instruction(0, new PushFalseIns(), new int[]{});
        }
        if (ovalue instanceof Null) {
            return new AVM2Instruction(0, new PushNullIns(), new int[]{});
        }
        if (ovalue instanceof Undefined) {
            return new AVM2Instruction(0, new PushUndefinedIns(), new int[]{});
        }
        return null;
    }

    protected AVM2Instruction makePush(AVM2ConstantPool cpool, GraphTargetItem graphTargetItem) {
        AVM2Instruction ins = null;
        if (graphTargetItem instanceof IntegerValueAVM2Item) {
            IntegerValueAVM2Item iv = (IntegerValueAVM2Item) graphTargetItem;
            return makePush(iv.value, cpool);
        } else if (graphTargetItem instanceof FloatValueAVM2Item) {
            FloatValueAVM2Item fv = (FloatValueAVM2Item) graphTargetItem;
            return makePush(fv.value, cpool);
        } else if (graphTargetItem instanceof StringAVM2Item) {
            StringAVM2Item fv = (StringAVM2Item) graphTargetItem;
            return makePush(fv.value, cpool);
        } else if (graphTargetItem instanceof TrueItem) {
            return makePush(Boolean.TRUE, cpool);
        } else if (graphTargetItem instanceof FalseItem) {
            return makePush(Boolean.FALSE, cpool);
        } else if (graphTargetItem instanceof NullAVM2Item) {
            return makePush(new Null(), cpool);
        } else if (graphTargetItem instanceof UndefinedAVM2Item) {
            return makePush(new Undefined(), cpool);
        } else {
            return null;
        }
    }

    private boolean removeObfuscationIfs(int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body) throws InterruptedException {
        AVM2Code code = body.getCode();
        if (code.code.size() == 0) {
            return false;
        }
        System.err.println("=====================================================");

        for (int i = 0; i < code.code.size(); i++) {
            ExecutionResult result = new ExecutionResult();
            System.err.println("Execute from " + i);
            executeActions(code, i, code.code.size() - 1, result);

            if (result.idx != -1) {
                int newIstructionCount = 1; // jump
                if (!result.stack.isEmpty()) {
                    newIstructionCount += result.stack.size();
                }

                if (newIstructionCount < result.instructionsProcessed) {
                    AVM2Instruction target = code.code.get(result.idx);
                    AVM2Instruction prevAction = code.code.get(i);

                    if (result.stack.isEmpty() && prevAction.definition instanceof JumpIns) {
                        prevAction.operands[0] = ((int) (target.getOffset() - prevAction.getOffset() - prevAction.getBytes().length));
                    } else {
                        if (!result.stack.isEmpty()) {
                            for (GraphTargetItem graphTargetItem : result.stack) {
                                AVM2Instruction ins = makePush(cpool, graphTargetItem);
                                if (ins != null) {
                                    code.insertInstruction(i++, ins);
                                }
                                prevAction = ins;
                                //DirectValueActionItem dv = (DirectValueActionItem) graphTargetItem;
                                //push.values.add(dv.value);
                            }
                            //push.setAddress(prevAction.getAddress());

                        }

                        AVM2Instruction jump = new AVM2Instruction(0, new JumpIns(), new int[]{0});
                        jump.offset = prevAction.offset;
                        jump.operands[0] = ((int) (target.offset - jump.offset - jump.getBytes().length));
                        code.insertInstruction(i++, jump);
                    }

                    AVM2Instruction nextAction = code.code.size() > i ? code.code.get(i) : null;

                    removeUnreachableActions(code, cpool, trait, minfo, body);
                    removeZeroJumps(code, body);

                    if (nextAction != null) {
                        int nextIdx = code.code.indexOf(nextAction);
                        if (nextIdx < i) {
                            i = nextIdx;
                        }
                    }
                }
            }
        }

        return false;
    }

    protected void removeUnreachableActions(AVM2Code code, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body) throws InterruptedException {
        code.removeDeadCode(cpool, trait, minfo, body);
    }

    protected boolean removeZeroJumps(AVM2Code actions, MethodBody body) {
        boolean result = false;
        for (int i = 0; i < actions.code.size(); i++) {
            AVM2Instruction action = actions.code.get(i);
            if (action.definition instanceof JumpIns && action.operands[0] == 0) {
                actions.removeInstruction(i, body);
                i--;
                result = true;
            }
        }
        return result;
    }

    private void executeActions(AVM2Code code, int idx, int endIdx, ExecutionResult result) {
        List<GraphTargetItem> output = new ArrayList<>();
        AVM2LocalData localData = new AVM2LocalData();

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

                AVM2Instruction action = code.code.get(idx);

                /*System.out.print(action.getASMSource(actions, new ArrayList<Long>(), ScriptExportMode.PCODE));
                 for (int j = 0; j < stack.size(); j++) {
                 System.out.print(" '" + stack.get(j).getResult() + "'");
                 }
                 System.out.println();*/
                InstructionDefinition def = action.definition;

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
                    JumpIns.class
                };

                boolean ok = false;
                for (Class<?> s : allowedDefs) {
                    if (s.isAssignableFrom(def.getClass())) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    break;
                }
                action.translate(localData, stack, output, Graph.SOP_USE_STATIC, "");

                idx++;

                if (def instanceof JumpIns) {
                    //ActionJump jump = (ActionJump) action;
                    long address = action.getOffset() + action.getBytes().length + action.operands[0];
                    idx = code.adr2pos(address);
                    if (idx == -1) {
                        throw new TranslateException("Jump target not found: " + address);
                    }
                }

                if (def instanceof IfTypeIns) {
                    //ActionIf aif = (ActionIf) action;
                    if (EcmaScript.toBoolean(stack.pop().getResult())) {
                        long address = action.offset + action.getBytes().length + action.operands[0];
                        idx = code.adr2pos(address);//code.indexOf(code.getByAddress(address));
                        if (idx == -1) {
                            throw new TranslateException("If target not found: " + address);
                        }
                    }
                }

                instructionsProcessed++;

                if (stack.allItemsFixed()) {
                    result.idx = idx == code.code.size() ? idx - 1 : idx;
                    result.instructionsProcessed = instructionsProcessed;
                    result.stack.clear();
                    result.stack.addAll(stack);
                }
            }
        } catch (EmptyStackException | TranslateException | InterruptedException ex) {
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

    public void deobfuscate(int classIndex, boolean isStatic, int scriptIndex, ABC abc, AVM2ConstantPool cpool, Trait trait, MethodInfo minfo, MethodBody body) throws InterruptedException {
        removeUnreachableActions(body.getCode(), cpool, trait, minfo, body);
        removeObfuscationIfs(classIndex, isStatic, scriptIndex, abc, cpool, trait, minfo, body);
        removeZeroJumps(body.getCode(), body);
    }

    class ExecutionResult {

        public int idx = -1;

        public int instructionsProcessed = -1;

        public TranslateStack stack = new TranslateStack("?");

        public Object resultValue;
    }
}

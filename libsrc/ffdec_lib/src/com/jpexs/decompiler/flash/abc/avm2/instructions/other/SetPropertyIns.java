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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.ScopeStack;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class SetPropertyIns extends InstructionDefinition implements SetTypeIns {

    public SetPropertyIns() {
        super(0x61, "setproperty", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    @Override
    public void translate(boolean isStatic, int scriptIndex, int classIndex, HashMap<Integer, GraphTargetItem> localRegs, TranslateStack stack, ScopeStack scopeStack, AVM2ConstantPool constants, AVM2Instruction ins, List<MethodInfo> method_info, List<GraphTargetItem> output, MethodBody body, ABC abc, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames, String path, HashMap<Integer, Integer> localRegsAssignmentIps, int ip, HashMap<Integer, List<Integer>> refs, AVM2Code code) {
        int multinameIndex = ins.operands[0];
        GraphTargetItem value = stack.pop();
        FullMultinameAVM2Item multiname = resolveMultiname(stack, constants, multinameIndex, ins);
        GraphTargetItem obj = stack.pop();
        if (value.getThroughDuplicate().getThroughRegister().getThroughDuplicate() instanceof IncrementAVM2Item) {
            GraphTargetItem inside = ((IncrementAVM2Item) value.getThroughDuplicate().getThroughRegister().getThroughDuplicate()).value.getThroughRegister().getNotCoerced().getThroughDuplicate();
            if (inside instanceof GetPropertyAVM2Item) {
                GetPropertyAVM2Item insideProp = ((GetPropertyAVM2Item) inside);
                if (((FullMultinameAVM2Item) insideProp.propertyName).compareSame(multiname)) {
                    GraphTargetItem insideObj = obj.getThroughDuplicate();
                    if (insideObj instanceof LocalRegAVM2Item) {
                        if (((LocalRegAVM2Item) insideObj).computedValue != null) {
                            insideObj = ((LocalRegAVM2Item) insideObj).computedValue.getThroughNotCompilable().getThroughDuplicate();
                        }
                    }
                    if (insideProp.object.getThroughDuplicate() == insideObj) {
                        if (stack.size() > 0) {
                            GraphTargetItem top = stack.peek().getNotCoerced().getThroughDuplicate();
                            if (top == insideProp) {
                                stack.pop();
                                stack.push(new PostIncrementAVM2Item(ins, insideProp));
                            } else if ((top instanceof IncrementAVM2Item) && (((IncrementAVM2Item) top).value == inside)) {
                                stack.pop();
                                stack.push(new PreIncrementAVM2Item(ins, insideProp));
                            } else {
                                output.add(new PostIncrementAVM2Item(ins, insideProp));
                            }
                        } else {
                            output.add(new PostIncrementAVM2Item(ins, insideProp));
                        }
                        return;
                    }
                }
            }
        }

        if (value.getThroughDuplicate().getThroughRegister().getThroughDuplicate() instanceof DecrementAVM2Item) {
            GraphTargetItem inside = ((DecrementAVM2Item) value.getThroughDuplicate().getThroughRegister().getThroughDuplicate()).value.getThroughRegister().getNotCoerced().getThroughDuplicate();
            if (inside instanceof GetPropertyAVM2Item) {
                GetPropertyAVM2Item insideProp = ((GetPropertyAVM2Item) inside);
                if (((FullMultinameAVM2Item) insideProp.propertyName).compareSame(multiname)) {
                    GraphTargetItem insideObj = obj.getThroughDuplicate();
                    if (insideObj instanceof LocalRegAVM2Item) {
                        if (((LocalRegAVM2Item) insideObj).computedValue != null) {
                            insideObj = ((LocalRegAVM2Item) insideObj).computedValue.getThroughNotCompilable().getThroughDuplicate();
                        }
                    }
                    if (insideProp.object.getThroughDuplicate() == insideObj) {
                        if (stack.size() > 0) {
                            GraphTargetItem top = stack.peek().getNotCoerced().getThroughDuplicate();
                            if (top == insideProp) {
                                stack.pop();
                                stack.push(new PostDecrementAVM2Item(ins, insideProp));
                            } else if ((top instanceof DecrementAVM2Item) && (((DecrementAVM2Item) top).value == inside)) {
                                stack.pop();
                                stack.push(new PreDecrementAVM2Item(ins, insideProp));
                            } else {
                                output.add(new PostDecrementAVM2Item(ins, insideProp));
                            }
                        } else {
                            output.add(new PostDecrementAVM2Item(ins, insideProp));
                        }
                        return;
                    }
                }
            }
        }

        if (obj.getThroughDuplicate() instanceof ConstructAVM2Item) {
            ConstructAVM2Item c = (ConstructAVM2Item) obj.getThroughDuplicate();
            if (c.object instanceof ApplyTypeAVM2Item) {
                ApplyTypeAVM2Item at = (ApplyTypeAVM2Item) c.object;
                c.args.clear();
                List<GraphTargetItem> vals = new ArrayList<>();
                vals.add(value);
                c.object = new InitVectorAVM2Item(c.getInstruction(), at.params.get(0), vals);
                return;
            } else if (c.object instanceof InitVectorAVM2Item) {
                InitVectorAVM2Item iv = (InitVectorAVM2Item) c.object;
                iv.arguments.add(value);
                return;
            }
        }

        output.add(new SetPropertyAVM2Item(ins, obj, multiname, value));

    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        int multinameIndex = ins.operands[0];
        return 2 + getMultinameRequiredStackSize(abc.constants, multinameIndex);
    }

    @Override
    public String getObject(Stack<AVM2Item> stack, ABC abc, AVM2Instruction ins, List<AVM2Item> output, MethodBody body, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames) {
        int multinameIndex = ins.operands[0];
        String multiname = resolveMultinameNoPop(0, stack, abc.constants, multinameIndex, ins, fullyQualifiedNames);
        GraphTargetItem obj = stack.get(1 + resolvedCount(abc.constants, multinameIndex)); //pod vrcholem
        if ((!obj.toString().isEmpty())) {
            multiname = "." + multiname;
        }
        return obj + multiname;
    }
}

/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.ClassAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.PostIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.PushItem;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import java.util.List;

/**
 * getlocal type instruction - get local register value.
 *
 * @author JPEXS
 */
public abstract class GetLocalTypeIns extends InstructionDefinition {

    /**
     * Constructor
     * @param instructionCode Instruction code
     * @param instructionName Instruction name
     * @param operands Operands
     * @param canThrow Can throw exception
     */
    public GetLocalTypeIns(int instructionCode, String instructionName, int[] operands, boolean canThrow) {
        super(instructionCode, instructionName, operands, canThrow);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        Object value = lda.localRegisters.get(getRegisterId(ins));
        lda.operandStack.push(value == null ? Undefined.INSTANCE : value);
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {

        int regId = getRegisterId(ins);

        if (regId == 0) {
            if (localData.classIndex == -1) {
                stack.push(new ThisAVM2Item(ins, localData.lineStartInstruction, DottedChain.parseNoSuffix("global"), false, false));
                return;
            }
            if ((localData.classIndex >= localData.getInstanceInfo().size())) {
                stack.push(new ThisAVM2Item(ins, localData.lineStartInstruction, DottedChain.OBJECT /*?*/, false, false));
                return;
            }
            if (localData.isStatic) {
                stack.push(new ClassAVM2Item(localData.getInstanceInfo().get(localData.classIndex).getName(localData.getConstants())));
            } else {
                List<Trait> ts = localData.getInstanceInfo().get(localData.classIndex).instance_traits.traits;
                boolean isBasicObject = localData.thisHasDefaultToPrimitive;
                Multiname m = localData.getInstanceInfo().get(localData.classIndex).getName(localData.getConstants());
                stack.push(new ThisAVM2Item(ins, localData.lineStartInstruction, m.getNameWithNamespace(localData.usedDeobfuscations, localData.abc, localData.getConstants(), true), isBasicObject, false));
            }
            return;
        }

        GraphTargetItem computedValue = localData.localRegs.get(regId);
        int assignCount = 0;
        if (localData.localRegAssignmentIps.containsKey(regId)) {
            assignCount = localData.localRegAssignmentIps.get(regId);
        }
        if (assignCount > 5) { //Do not allow change register more than 5 - for deobfuscation
            //computedValue = new NotCompileTimeItem(ins, localData.lineStartInstruction, computedValue);
        }

        GraphTargetItem type = TypeItem.UNBOUNDED;

        if (localData.localRegTypes.containsKey(regId)) {
            type = localData.localRegTypes.get(regId);
        } else if (computedValue != null) {
            type = computedValue.returnType();
        }
        LocalRegAVM2Item result = new LocalRegAVM2Item(ins, localData.lineStartInstruction, regId, computedValue, type);
        
        
        stack.finishBlock(output);
        
        if (!output.isEmpty()) {            
            if (output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                SetLocalAVM2Item setLocal = (SetLocalAVM2Item) output.get(output.size() - 1);
                if (setLocal.regIndex == regId) {
                    if (setLocal.getSrc() != null) {
                        if (localData.getSetLocalUsages(localData.code.adr2pos(setLocal.getSrc().getAddress())).size() == 1) {
                            if (output.size() >= 2 && output.get(output.size() - 2) instanceof PushItem) {
                                output.remove(output.size() - 1);
                                stack.moveToStack(output);
                                stack.push(setLocal.value);
                                return;
                            }
                        }
                    }
                }
            }
        }
        
        //TestIncDec7 AIR
        if (!output.isEmpty()) {
            GraphTargetItem lastOutput = output.get(output.size() - 1);
            if (lastOutput instanceof IncLocalAVM2Item) {
                output.remove(output.size() - 1);
                stack.moveToStack(output);
                stack.push(new PreIncrementAVM2Item(lastOutput.getSrc(), lastOutput.getLineStartItem(), result));
                return;
            }
            if (output.get(output.size() - 1) instanceof DecLocalAVM2Item) {
                output.remove(output.size() - 1);
                stack.moveToStack(output);   
                stack.push(new PreDecrementAVM2Item(lastOutput.getSrc(), lastOutput.getLineStartItem(), result));
                return;
            }
        }
        
        
        //TestIncDec6 with result AIR
        if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetPropertyAVM2Item) {
            SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) output.get(output.size() - 1);
            if (setProp.value instanceof IncrementAVM2Item
                    || setProp.value instanceof DecrementAVM2Item) {
                boolean isIncrement = setProp.value instanceof IncrementAVM2Item;
                if (setProp.value.value instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLoc = (SetLocalAVM2Item) setProp.value.value;
                    if (setLoc.regIndex == regId) {
                        if (setLoc.getSrc() instanceof AVM2Instruction) {
                            AVM2Instruction src = (AVM2Instruction) setLoc.getSrc();
                            if (localData.getSetLocalUsages(localData.code.adr2pos(src.getAddress())).size() == 1) {
                                if (setLoc.value instanceof ConvertAVM2Item) {
                                    if (setLoc.value.value instanceof GetPropertyAVM2Item) {
                                        GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLoc.value.value;
                                        if (setProp.object instanceof DuplicateSourceItem) {
                                            DuplicateSourceItem ds = (DuplicateSourceItem) setProp.object;
                                            if (getProp.object instanceof DuplicateItem) {
                                                DuplicateItem d = (DuplicateItem) getProp.object;
                                                if (output.size() >= 2 && output.get(output.size() - 2) instanceof SetTemporaryItem) {
                                                    SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 2);
                                                    if (st.tempIndex == d.tempIndex) {
                                                        getProp.object = st.value;
                                                        output.remove(output.size() - 1);
                                                        output.remove(output.size() - 1);
                                                        stack.moveToStack(output);
                                                        if (isIncrement) {
                                                            stack.push(new PostIncrementAVM2Item(setProp.value.getSrc(), setProp.value.getLineStartItem(), getProp));
                                                        } else {
                                                            stack.push(new PostDecrementAVM2Item(setProp.value.getSrc(), setProp.value.getLineStartItem(), getProp));                                                        
                                                        }
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }                            
                        }
                    }
                }
            }
        }
        
        //TestChainedAssignments1
        /*
         trace("c = b = a = 5;");
         var a:int = 0;
         var b:int = 0;
         var c:int = 0;
         var _loc4_:*;
         a = _loc4_ = 5;
         b = _loc4_ = _loc4_;
         c = _loc4_;
        */
        stack.moveToStack(output);            
        if (!output.isEmpty()) {
            //chained assignments
            if ((output.get(output.size() - 1) instanceof SetTypeAVM2Item)) {
                GraphTargetItem setItem = output.get(output.size() - 1);
                if (setItem.value.getNotCoercedNoDup() instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) setItem.value.getNotCoercedNoDup();
                    if (setLocal.regIndex == regId) {
                        int setLocalIp = localData.code.adr2pos(setLocal.getSrc().getAddress());
                        if (localData.getSetLocalUsages(setLocalIp).size() == 1) {
                                                 
                            
                            //TestIncDec5 with result AIR
                            if (setItem instanceof SetPropertyAVM2Item
                                    && (setLocal.value instanceof IncrementAVM2Item
                                    || setLocal.value instanceof DecrementAVM2Item
                                    )) {
                                SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) setItem;
                                boolean isIncrement = setLocal.value instanceof IncrementAVM2Item;
                                if (setLocal.value.value instanceof GetPropertyAVM2Item) {
                                    
                                    //TestIncDec6 with result AIR
                                    /*
                                    var _temp_4:* = trace;
                                    var _temp_3:* = global;
                                    var _temp_1:* = a;
                                    var _loc2_:Number;
                                    _temp_1.attrib = (_loc2_ = _temp_1.attrib) + 1;
                                    _temp_4(_loc2_);
                                    */
                                    GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLocal.value.value;
                                    if (getProp.object instanceof DuplicateItem) {
                                        DuplicateItem d = (DuplicateItem) getProp.object;
                                        if (setProp.object instanceof DuplicateSourceItem) {
                                            DuplicateSourceItem ds = (DuplicateSourceItem) setProp.object;
                                            if (ds.tempIndex == d.tempIndex) {
                                                if (output.size() >= 2 && output.get(output.size() - 2) instanceof SetTemporaryItem) {
                                                    SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 2);
                                                    if (st.tempIndex == d.tempIndex) {
                                                        getProp.object = st.value;
                                                        output.remove(output.size() - 1);
                                                        output.remove(output.size() - 1);
                                                        stack.moveToStack(output);
                                                        if (isIncrement) {
                                                            stack.push(new PreIncrementAVM2Item(setLocal.value.getSrc(), setLocal.value.getLineStartItem(), getProp));
                                                        } else {
                                                            stack.push(new PreDecrementAVM2Item(setLocal.value.getSrc(), setLocal.value.getLineStartItem(), getProp));                                                        
                                                        }
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            if ((setItem.value instanceof CoerceAVM2Item) || (setItem.value instanceof ConvertAVM2Item)) {
                                setItem.value.value = setLocal.value;
                            } else {
                                setItem.value = setLocal.value;
                            }
                            output.remove(output.size() - 1);
                            stack.moveToStack(output);                           

                            stack.push(setItem);
                            return;                                                              
                        }
                    }                                        
                }
            }
            stack.finishBlock(output);
        }
        
        stack.moveToStack(output);        
        stack.push(result);
        
        //chained assignments and/or ASC post/pre increment
        /*
        if (stack.peek() instanceof CommaExpressionItem) {
            CommaExpressionItem ce = (CommaExpressionItem) stack.peek();
            if (ce.commands.size() == 2 && ce.commands.get(0) instanceof SetTypeAVM2Item) {
                GraphTargetItem setItem = ce.commands.get(0);
                if ((setItem instanceof SetPropertyAVM2Item)
                        && ((setItem.value.getNotCoerced() instanceof DecrementAVM2Item)
                        || (setItem.value.getNotCoerced() instanceof IncrementAVM2Item))) {
                    boolean isIncrement = (setItem.value.getNotCoerced() instanceof IncrementAVM2Item);
                    GraphTargetItem val = setItem.value.getNotCoerced();
                    if (val.value instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) val.value;
                        if (setLocal.regIndex == regId) {                           
                            if (setLocal.value.getNotCoerced() instanceof GetPropertyAVM2Item) {
                                SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) setItem;
                                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLocal.value.getNotCoerced();
                                if (getProp.object.getThroughDuplicate() == setProp.object.getThroughDuplicate()) {
                                    if (((FullMultinameAVM2Item) setProp.propertyName).compareSame((FullMultinameAVM2Item) getProp.propertyName)) {
                                        if ((getProp.object instanceof DuplicateItem) || (getProp.object instanceof DuplicateSourceItem)) {
                                            getProp.object = getProp.object.value;
                                        }
                                        GraphTargetItem result;
                                        if (isIncrement) {
                                            result = new PostIncrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                        } else {
                                            result = new PostDecrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                        }
                                        //output.remove(output.size() - 1);
                                        //stack.moveToStack(output);
                                        stack.pop();
                                        stack.push(result);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                } else if (setItem.value.getNotCoerced() instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) setItem.value.getNotCoerced();
                    if (setLocal.regIndex == regId) {
                        int setLocalIp = localData.code.adr2pos(setLocal.getSrc().getAddress());
                        if (localData.getSetLocalUsages(setLocalIp).size() == 1) {
                            if ((setItem.value instanceof CoerceAVM2Item) || (setItem.value instanceof ConvertAVM2Item)) {
                                setItem.value.value = setLocal.value;
                            } else {
                                setItem.value = setLocal.value;
                            }

                            //output.remove(output.size() - 1);
                            //stack.moveToStack(output);
                            stack.pop();
                            
                            if (setItem instanceof SetPropertyAVM2Item) {
                                if ((setItem.value instanceof IncrementAVM2Item) || (setItem.value instanceof DecrementAVM2Item)) {
                                    boolean isIncrement = (setItem.value instanceof IncrementAVM2Item);
                                    if (setItem.value.value instanceof GetPropertyAVM2Item) {
                                        SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) setItem;
                                        GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setItem.value.value;
                                        if (getProp.object.getThroughDuplicate() == setProp.object.getThroughDuplicate()) {
                                            if (((FullMultinameAVM2Item) setProp.propertyName).compareSame((FullMultinameAVM2Item) getProp.propertyName)) {
                                                if (getProp.object instanceof DuplicateItem
                                                        || getProp.object instanceof DuplicateSourceItem)  {
                                                    getProp.object = getProp.object.value;
                                                }
                                                if (isIncrement) {
                                                    setItem = new PreIncrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                                } else {
                                                    setItem = new PreDecrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            stack.push(setItem);
                            return;
                        }
                    }
                }
            }
        } else if (!output.isEmpty()) {
            if ((output.get(output.size() - 1) instanceof SetTypeAVM2Item)) {
                GraphTargetItem setItem = output.get(output.size() - 1);
                if (setItem.value.getNotCoerced() instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) setItem.value.getNotCoerced();
                    if (setLocal.regIndex == regId) {
                        int setLocalIp = localData.code.adr2pos(setLocal.getSrc().getAddress());
                        if (localData.getSetLocalUsages(setLocalIp).size() == 1) {
                            if ((setItem.value instanceof CoerceAVM2Item) || (setItem.value instanceof ConvertAVM2Item)) {
                                setItem.value.value = setLocal.value;
                            } else {
                                setItem.value = setLocal.value;
                            }
        
                            stack.pop();
                            

                            
                            output.remove(output.size() - 1);
                            stack.moveToStack(output);

                            if (setItem instanceof SetPropertyAVM2Item) {
                                if ((setItem.value instanceof IncrementAVM2Item) || (setItem.value instanceof DecrementAVM2Item)) {
                                    boolean isIncrement = (setItem.value instanceof IncrementAVM2Item);
                                    if (setItem.value.value instanceof GetPropertyAVM2Item) {
                                        SetPropertyAVM2Item setProp = (SetPropertyAVM2Item) setItem;
                                        GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setItem.value.value;
                                        if (getProp.object.getThroughDuplicate() == setProp.object) {
                                            if (((FullMultinameAVM2Item) setProp.propertyName).compareSame((FullMultinameAVM2Item) getProp.propertyName)) {
                                                if (getProp.object instanceof DuplicateItem
                                                        || getProp.object instanceof DuplicateSourceItem) {
                                                    getProp.object = getProp.object.value;
                                                }
                                                if (isIncrement) {
                                                    setItem = new PreIncrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                                } else {
                                                    setItem = new PreDecrementAVM2Item(setProp.getSrc(), localData.lineStartInstruction, getProp);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            stack.push(setItem);
                            return;
                        }
                    }
                }
            }
        }*/
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    public abstract int getRegisterId(AVM2Instruction ins);
}

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
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
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
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import com.jpexs.decompiler.graph.model.TemporaryItem;
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
        
        
        //TestIncDec5 with result AIR
        /*
        //var _temp_4:* = trace;
        //var _temp_3:* = global;
        var _temp_1:* = a;
        var _temp_2:* = _temp_1.attrib + 1;
        var _loc2_:* = _temp_2;
        _temp_1.attrib = _temp_2;
        _temp_4(_loc2_);
        */
        
        stack.finishBlock(output);
        
        Class[] expectedClasses = new Class[]{
            SetTemporaryItem.class,
            SetTemporaryItem.class,
            SetLocalAVM2Item.class,
            SetPropertyAVM2Item.class
        };
                                                
        if (output.size() >= expectedClasses.length) {

            loopout: do {
                for (int i = 0; i < expectedClasses.length; i++) {
                    if (!expectedClasses[expectedClasses.length - 1 - i].isAssignableFrom(output.get(output.size() - 1 - i).getClass())) {
                        break loopout;
                    }
                }

                SetPropertyAVM2Item setProperty = (SetPropertyAVM2Item) output.get(output.size() - 1);
                SetLocalAVM2Item setLocalValue = (SetLocalAVM2Item) output.get(output.size() - 2);
                SetTemporaryItem setTempValue = (SetTemporaryItem) output.get(output.size() - 3);
                SetTemporaryItem setTempObj = (SetTemporaryItem) output.get(output.size() - 4);


                if (setLocalValue.regIndex != result.regIndex) {
                    break;
                }
                if (!(setLocalValue.value instanceof DuplicateItem)) {
                    break;
                }
                if (!(setLocalValue.value.value instanceof IncrementAVM2Item
                    || setLocalValue.value.value instanceof DecrementAVM2Item)) {
                    break;
                }
                if (!(setLocalValue.value.value.value instanceof GetPropertyAVM2Item)) {
                    break;
                }
                boolean isIncrement = setLocalValue.value.value instanceof IncrementAVM2Item;

                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setLocalValue.value.value.value;
                if (!(getProp.object instanceof DuplicateItem)) {
                    break;
                }
                if (!(getProp.propertyName instanceof FullMultinameAVM2Item)) {
                    break;
                }
                FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                FullMultinameAVM2Item setFm = (FullMultinameAVM2Item) setProperty.propertyName;
                
                if (!fm.compareSame(setFm)) {
                    break;
                }

                DuplicateItem dupObj = (DuplicateItem) getProp.object;
                if (dupObj.tempIndex != setTempObj.tempIndex) {
                    break;
                }

                DuplicateItem dupValue = (DuplicateItem) setLocalValue.value;
                if (dupValue.tempIndex != setTempValue.tempIndex) {
                    break;
                }               
                getProp.object = setTempObj.value;
                for (int i = 0; i < expectedClasses.length; i++) {
                    output.remove(output.size() - 1);
                }
                stack.moveToStack(output);
                if (isIncrement) {
                    stack.push(new PreIncrementAVM2Item(setLocalValue.value.value.getSrc(), setLocalValue.value.value.lineStartItem, getProp));
                } else {
                    stack.push(new PreDecrementAVM2Item(setLocalValue.value.value.getSrc(), setLocalValue.value.value.lineStartItem, getProp));                        
                }
                return;
            } while(false);
        }
        
        //TestIncDec6 with result AIR
        /*
         //var _temp_4:* = trace;
         //var _temp_3:* = global;
         var _temp_1:* = a;
         var _temp_2:* = _temp_1.attrib;
         var _loc2_:* = _temp_2;
         _temp_1.attrib = _temp_2 + 1;
         _temp_4(_loc2_);                
        */
        expectedClasses = new Class[] {
            SetTemporaryItem.class,
            SetTemporaryItem.class,
            SetLocalAVM2Item.class,
            SetPropertyAVM2Item.class
        };
                                                
        if (output.size() >= expectedClasses.length) {

            loopout: do {
                for (int i = 0; i < expectedClasses.length; i++) {
                    if (!expectedClasses[expectedClasses.length - 1 - i].isAssignableFrom(output.get(output.size() - 1 - i).getClass())) {
                        break loopout;
                    }
                }

                SetPropertyAVM2Item setProperty = (SetPropertyAVM2Item) output.get(output.size() - 1);
                SetLocalAVM2Item setLocalValue = (SetLocalAVM2Item) output.get(output.size() - 2);
                SetTemporaryItem setTempValue = (SetTemporaryItem) output.get(output.size() - 3);
                SetTemporaryItem setTempObj = (SetTemporaryItem) output.get(output.size() - 4);


                if (setLocalValue.regIndex != result.regIndex) {
                    break;
                }
                if (!(setProperty.object instanceof DuplicateSourceItem)) {
                    break;
                }
                if (!(setProperty.value instanceof IncrementAVM2Item
                    || setProperty.value instanceof DecrementAVM2Item)) {
                    break;
                }
                
                if (!(setProperty.value.value instanceof DuplicateSourceItem)) {
                    break;
                }
                                
                if (!(setProperty.value.value.value.getNotCoercedNoDup() instanceof GetPropertyAVM2Item)) {
                    break;
                }
                boolean isIncrement = setProperty.value instanceof IncrementAVM2Item;

                GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) setProperty.value.value.value.getNotCoercedNoDup();
                if (!(getProp.object instanceof DuplicateItem)) {
                    break;
                }
                if (!(getProp.propertyName instanceof FullMultinameAVM2Item)) {
                    break;
                }
                FullMultinameAVM2Item fm = (FullMultinameAVM2Item) getProp.propertyName;
                FullMultinameAVM2Item setFm = (FullMultinameAVM2Item) setProperty.propertyName;
                
                if (!fm.compareSame(setFm)) {
                    break;
                }
                
                if (!(setLocalValue.value instanceof DuplicateItem)) {
                    break;
                }
                DuplicateSourceItem  dupSourceObj = (DuplicateSourceItem) setProperty.object;
                DuplicateItem dupObj = (DuplicateItem) getProp.object;
                if (dupObj.tempIndex != setTempObj.tempIndex
                    || dupSourceObj.tempIndex != setTempObj.tempIndex) {
                    break;
                }                
                
                DuplicateSourceItem dupValue = (DuplicateSourceItem) setProperty.value.value;
                DuplicateItem dupSourceValue = (DuplicateItem) setLocalValue.value;               
                if (dupValue.tempIndex != setTempValue.tempIndex
                    || dupSourceValue.tempIndex != setTempValue.tempIndex) {
                    break;
                }               
                getProp.object = setTempObj.value;
                for (int i = 0; i < expectedClasses.length; i++) {
                    output.remove(output.size() - 1);
                }
                stack.moveToStack(output);
                if (isIncrement) {
                    stack.push(new PreIncrementAVM2Item(setLocalValue.value.value.getSrc(), setLocalValue.value.value.lineStartItem, getProp));
                } else {
                    stack.push(new PreDecrementAVM2Item(setLocalValue.value.value.getSrc(), setLocalValue.value.value.lineStartItem, getProp));                        
                }
                return;
            } while(false);
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

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
package com.jpexs.decompiler.flash.abc.avm2.instructions;

import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ConvertAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.DecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IncrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreDecrementAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.PreIncrementAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.DuplicateSourceItem;
import com.jpexs.decompiler.graph.model.SetTemporaryItem;
import java.util.List;

/**
 * SetType instruction interface.
 *
 * @author JPEXS
 */
public interface SetTypeIns {

    /**
     * Handles number to int conversion.
     *
     * @param value Value to convert
     * @param type Type to convert to
     * @return Value
     */
    public static GraphTargetItem handleNumberToInt(GraphTargetItem value, GraphTargetItem type) {
        if ((value instanceof ConvertAVM2Item) || (value instanceof CoerceAVM2Item)) {
            if (type != null && (type.equals(TypeItem.INT) || type.equals(TypeItem.UINT))) {
                if (value.value.returnType().equals(TypeItem.NUMBER)) {
                    return value.value;
                }
            }
        }
        return value;
    }

    /**
     * Handles result.
     *
     * @param value Value
     * @param stack Stack
     * @param output Output
     * @param localData Local data
     * @param result Result
     * @param regId Register ID
     * @param type Type
     */
    public static void handleResult(GraphTargetItem value, TranslateStack stack, List<GraphTargetItem> output, AVM2LocalData localData, GraphTargetItem result, int regId, GraphTargetItem type) {
        
        GraphTargetItem notCoercedValue = value;
        if ((value instanceof CoerceAVM2Item) || (value instanceof ConvertAVM2Item)) {
            notCoercedValue = value.value;
        }               
        
        //TestChainedAssignments1 both AIR and nonair
        stack.moveToStack(output);
        if (!stack.isEmpty()) {
            if (GraphTargetItem.checkDup2(notCoercedValue, stack.peek(), output, stack) >= -1) {
                GraphTargetItem newValue = notCoercedValue.getThroughDuplicate();
                if ((value instanceof CoerceAVM2Item) || (value instanceof ConvertAVM2Item)) {
                    result.value.value = newValue;
                } else {
                    result.value = newValue;
                }

                //Assembled - TestForEach
                if (AVM2Item.mustStayIntact2(newValue) && result instanceof SetLocalAVM2Item) {
                    SetLocalAVM2Item setLocal = (SetLocalAVM2Item) result;
                    stack.pop();
                    stack.moveToStack(output);
                    stack.addToOutput(result);                               
                    stack.push(new LocalRegAVM2Item(null, null, setLocal.regIndex, setLocal.value, setLocal.type));
                    return;
                }


                stack.pop();
                stack.moveToStack(output);                    
                stack.push(result);
                return;
            }
            
            if (notCoercedValue instanceof DuplicateItem) {
                GraphTargetItem insideDup = notCoercedValue.value;
                if (!AVM2Item.mustStayIntact1(insideDup.getNotCoercedNoDup())) {
                    if (stack.peek().getThroughDuplicate() == insideDup.getThroughDuplicate()) {
                        stack.pop();

                        if ((insideDup instanceof DuplicateItem) && regId > -1) {
                            int numDups = 1;
                            while ((insideDup instanceof DuplicateItem) && !stack.isEmpty() && stack.peek().getThroughDuplicate() == insideDup.getThroughDuplicate()) {
                                insideDup = insideDup.value;
                                stack.pop();
                                numDups++;
                            }
                            if ((value instanceof CoerceAVM2Item) || (value instanceof ConvertAVM2Item)) {
                                value.value = insideDup;
                            } else {
                                value = insideDup;
                            }
                            result.value = value;
                            if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetTemporaryItem) {
                                output.remove(output.size() - 1);
                            }                            
                            stack.addToOutput(result);
                            for (int i = 0; i < numDups; i++) {
                                stack.push(new LocalRegAVM2Item(null, localData.lineStartInstruction, regId, value, localData.localRegTypes.containsKey(regId) ? localData.localRegTypes.get(regId) : value.returnType()));
                            }
                            return;
                        }
                    }
                }
            }
        }
        
        
        stack.finishBlock(output);
        /*if (notCoercedValue instanceof DuplicateItem) {
            DuplicateItem d = (DuplicateItem) notCoercedValue;
            
            if (!stack.isEmpty() && stack.peek() instanceof DuplicateSourceItem) {
                DuplicateSourceItem ds = (DuplicateSourceItem) stack.peek();
                if (ds.tempIndex == d.tempIndex) {  
                    GraphTargetItem newValue = d.value;                    
                    if (!output.isEmpty() && output.get(output.size() - 1) instanceof SetTemporaryItem) {
                        SetTemporaryItem st = (SetTemporaryItem) output.get(output.size() - 1);
                        if (st.tempIndex == ds.tempIndex) {
                            output.remove(output.size() - 1);
                        }
                    }                    
                    
                    if ((value instanceof CoerceAVM2Item) || (value instanceof ConvertAVM2Item)) {
                        result.value.value = newValue;
                    } else {
                        result.value = newValue;
                    }
                    
                    //Assembled - TestForEach
                    if (AVM2Item.mustStayIntact2(newValue) && result instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item setLocal = (SetLocalAVM2Item) result;
                        stack.pop();
                        stack.moveToStack(output);
                        stack.addToOutput(result);                               
                        stack.push(new LocalRegAVM2Item(null, null, setLocal.regIndex, setLocal.value, setLocal.type));
                        return;
                    }
                    
                                        
                    stack.pop();
                    stack.moveToStack(output);                    
                    stack.push(result);
                    return;
                }
            }
            
        }*/
        
        
        
        stack.addToOutput(result);
        if (true) { //FIXME??
            return;
        }
        //
        

        if (notCoercedValue instanceof DuplicateItem) {
            GraphTargetItem insideDup = notCoercedValue.value;
            if (!AVM2Item.mustStayIntact1(insideDup.getNotCoercedNoDup())) {
                stack.moveToStack(output);
                if (!stack.isEmpty() && stack.peek().getThroughDuplicate() == insideDup.getThroughDuplicate()) {
                    stack.pop();

                    if ((insideDup instanceof DuplicateItem) && regId > -1) {
                        int numDups = 1;
                        while ((insideDup instanceof DuplicateItem) && !stack.isEmpty() && stack.peek().getThroughDuplicate() == insideDup.getThroughDuplicate()) {
                            insideDup = insideDup.value;
                            stack.pop();
                            numDups++;
                        }
                        if ((value instanceof CoerceAVM2Item) || (value instanceof ConvertAVM2Item)) {
                            value.value = insideDup;
                        } else {
                            value = insideDup;
                        }
                        result.value = value;
                        stack.addToOutput(result);
                        for (int i = 0; i < numDups; i++) {
                            stack.push(new LocalRegAVM2Item(null, localData.lineStartInstruction, regId, value, localData.localRegTypes.containsKey(regId) ? localData.localRegTypes.get(regId) : value.returnType()));
                        }
                        return;
                    } else {

                        if ((value instanceof CoerceAVM2Item) || (value instanceof ConvertAVM2Item)) {
                            value.value = insideDup;
                        } else {
                            value = insideDup;
                        }

                        result.value = value;

                        if ((result instanceof SetLocalAVM2Item) && regId > -1) {
                            ((SetLocalAVM2Item) result).causedByDup = true;
                        }

                        if (regId > -1 && AVM2Item.mustStayIntact2(insideDup.getNotCoerced())) { //hack
                            stack.addToOutput(result);
                            stack.push(new LocalRegAVM2Item(null, localData.lineStartInstruction, regId, value, localData.localRegTypes.containsKey(regId) ? localData.localRegTypes.get(regId) : TypeItem.UNBOUNDED));
                            return;
                        }

                        stack.push(result);
                        return;
                    }
                }
            }
        }
        stack.addToOutput(result);        
    }
}

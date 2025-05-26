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
package com.jpexs.decompiler.flash.abc.avm2.instructions.other;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.SetTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetLexAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetLocalAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.SetTypeAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.CompoundableBinaryOp;
import java.util.List;
import java.util.Objects;

/**
 * setproperty instruction - set property value.
 *
 * @author JPEXS
 */
public class SetPropertyIns extends InstructionDefinition implements SetTypeIns {

    /**
     * Constructor
     */
    public SetPropertyIns() {
        super(0x61, "setproperty", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        handleSetProperty(false, localData, stack, ins, output, path);
    }

    public static void handleCompound(AVM2LocalData localData, GraphTargetItem obj, FullMultinameAVM2Item multiname, GraphTargetItem value, List<GraphTargetItem> output, SetTypeAVM2Item result) {
        if (value instanceof LocalRegAVM2Item) {
            LocalRegAVM2Item locVal = (LocalRegAVM2Item) value;
            if (multiname.name instanceof LocalRegAVM2Item) {
                LocalRegAVM2Item locName = (LocalRegAVM2Item) multiname.name;
                if (output.size() > 2) {
                    if (output.get(output.size() - 1) instanceof SetLocalAVM2Item) {
                        SetLocalAVM2Item setLocVal = (SetLocalAVM2Item) output.get(output.size() - 1);
                        if (setLocVal.regIndex == locVal.regIndex) {
                            if (output.get(output.size() - 2) instanceof SetLocalAVM2Item) {
                                SetLocalAVM2Item setLocName = (SetLocalAVM2Item) output.get(output.size() - 2);
                                if (setLocName.regIndex == locName.regIndex) {
                                    if (setLocVal.value instanceof CompoundableBinaryOp) {
                                        CompoundableBinaryOp binaryOp = (CompoundableBinaryOp) setLocVal.value;
                                        if (binaryOp.getLeftSide().getNotCoerced() instanceof GetPropertyAVM2Item) {
                                            GetPropertyAVM2Item getProp = (GetPropertyAVM2Item) binaryOp.getLeftSide().getNotCoerced();
                                            if (((FullMultinameAVM2Item) getProp.propertyName).compareSame(multiname) && Objects.equals(getProp.object, obj)) {
                                                multiname.name = setLocName.value;
                                                result.setCompoundValue(binaryOp.getRightSide());
                                                result.setCompoundOperator(binaryOp.getOperator());
                                                output.remove(output.size() - 2);
                                                output.remove(output.size() - 1);
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

        if (value.getNotCoerced() instanceof CompoundableBinaryOp) {
            if (!obj.hasSideEffect() && !multiname.hasSideEffect()) {
                CompoundableBinaryOp binaryOp = (CompoundableBinaryOp) value.getNotCoerced();
                if (binaryOp.getLeftSide() instanceof GetLexAVM2Item) {
                    GetLexAVM2Item getLex = (GetLexAVM2Item) binaryOp.getLeftSide();
                    if ((obj instanceof FindPropertyAVM2Item) && localData.abc.constants.getMultiname(multiname.multinameIndex).equals(getLex.propertyName)) {
                        result.setCompoundValue(binaryOp.getRightSide());
                        result.setCompoundOperator(binaryOp.getOperator());
                    }
                } else if (binaryOp.getLeftSide() instanceof GetPropertyAVM2Item) {
                    GetPropertyAVM2Item propItem = (GetPropertyAVM2Item) binaryOp.getLeftSide();
                    if (Objects.equals(obj, propItem.object.getThroughDuplicate()) && Objects.equals(multiname, propItem.propertyName)) {
                        result.setCompoundValue(binaryOp.getRightSide());
                        result.setCompoundOperator(binaryOp.getOperator());
                    }
                }
            }
        }
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        int multinameIndex = ins.operands[0];
        return 2 + getMultinameRequiredStackSize(abc.constants, multinameIndex);
    }
}

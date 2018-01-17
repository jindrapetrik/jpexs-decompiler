/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.abc.avm2.instructions.construction;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.ConstructPropAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FindPropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FullMultinameAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.RegExpAvm2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.XMLAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ConstructPropIns extends InstructionDefinition {

    public ConstructPropIns() {
        super(0x4a, "constructprop", new int[]{AVM2Code.DAT_MULTINAME_INDEX, AVM2Code.DAT_ARG_COUNT}, true);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        /*int multinameIndex = ins.getParamAsLong(constants, 0).intValue();
         int argCount = ins.getParamAsLong(constants, 1).intValue();
         List<Object> passArguments = new ArrayList<Object>();
         for (int i = argCount - 1; i >= 0; i--) {
         passArguments.set(i, lda.operandStack.pop());
         }*/
        //if multiname[multinameIndex] is runtime
        //pop(name) pop(ns)
        //lda.executionException = "Cannot construct property";
        return false;
        //create property
        //push new instance
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) throws InterruptedException {
        int multinameIndex = ins.operands[0];
        int argCount = ins.operands[1];
        List<GraphTargetItem> args = new ArrayList<>();
        for (int a = 0; a < argCount; a++) {
            args.add(0, stack.pop());
        }
        FullMultinameAVM2Item multiname = resolveMultiname(localData, true, stack, localData.getConstants(), multinameIndex, ins);
        GraphTargetItem obj = stack.pop();
        if (obj instanceof FindPropertyAVM2Item) {
            multiname.property = false;  //can be type
        }

        if (multiname.isXML(localData.getConstants(), localData.localRegNames, localData.fullyQualifiedNames)) {
            if (args.size() == 1) {
                GraphTargetItem arg = args.get(0);
                List<GraphTargetItem> xmlLines = new ArrayList<>();
                if (ConstructIns.walkXML(arg, xmlLines)) {
                    stack.push(new XMLAVM2Item(ins, localData.lineStartInstruction, xmlLines));
                    return;
                }
            }
        }//
        boolean isRegExp = false;
        if (multiname.isTopLevel("RegExp", localData.getConstants(), localData.localRegNames, localData.fullyQualifiedNames)) {
            isRegExp = true;
        }
        if (isRegExp && (args.size() >= 1) && (args.get(0) instanceof StringAVM2Item) && (args.size() == 1 || (args.size() == 2 && args.get(1) instanceof StringAVM2Item))) {
            String pattern = ((StringAVM2Item) args.get(0)).getValue();
            String modifiers = "";
            if (args.size() == 2) {
                modifiers = ((StringAVM2Item) args.get(1)).getValue();
            }
            stack.push(new RegExpAvm2Item(pattern, modifiers, ins, localData.lineStartInstruction));
            return;
        }

        stack.push(new ConstructPropAVM2Item(ins, localData.lineStartInstruction, obj, multiname, args));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        int multinameIndex = ins.operands[0];
        return ins.operands[1] + 1 + getMultinameRequiredStackSize(abc.constants, multinameIndex);
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}

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
package com.jpexs.decompiler.flash.abc.avm2.instructions.types;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2ExecutionException;
import com.jpexs.decompiler.flash.abc.avm2.exceptions.AVM2TypeErrorException;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.CoerceAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.PropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.EcmaType;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CoerceIns extends InstructionDefinition implements CoerceOrConvertTypeIns {

    public CoerceIns() {
        super(0x80, "coerce", new int[]{AVM2Code.DAT_MULTINAME_INDEX}, true);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) throws AVM2ExecutionException {
        Multiname multiname = (Multiname) ins.getParam(constants, 0);
        if (multiname == null) {
            throw new AVM2TypeErrorException(1034, lda.isDebug());
        }

        Object value = lda.operandStack.pop();
        if (value == Undefined.INSTANCE) {
            value = Null.INSTANCE;
        }

        //push and pop coerced value to specified type
        EcmaType type = EcmaScript.type(value);
        switch (type) {
            case NUMBER:
            case BOOLEAN:
                lda.operandStack.push(EcmaScript.toString(value));
                break;
            case NULL:
            case OBJECT:
            case STRING:
                lda.operandStack.push(value);
                break;
            default:
                throw new AVM2TypeErrorException(1034, lda.isDebug());
        }

        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        int multinameIndex = ins.operands[0];
        stack.push(new CoerceAVM2Item(ins, localData.lineStartInstruction, stack.pop(), PropertyAVM2Item.multinameToType(multinameIndex, localData.getConstants())));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    @Override
    public GraphTargetItem getTargetType(AVM2ConstantPool constants, AVM2Instruction ins) {
        int multinameIndex = ins.operands[0];
        return PropertyAVM2Item.multinameToType(multinameIndex, constants);
    }
}

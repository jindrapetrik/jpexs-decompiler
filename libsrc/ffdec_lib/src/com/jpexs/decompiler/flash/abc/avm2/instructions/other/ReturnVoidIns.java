/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.LocalDataArea;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

/**
 * returnvoid instruction - return from function with no return value.
 *
 * @author JPEXS
 */
public class ReturnVoidIns extends InstructionDefinition {

    /**
     * Constructor
     */
    public ReturnVoidIns() {
        super(0x47, "returnvoid", new int[]{}, false);
    }

    @Override
    public boolean execute(LocalDataArea lda, AVM2ConstantPool constants, AVM2Instruction ins) {
        lda.returnValue = Undefined.INSTANCE;
        return true;
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {
        if (returnsValue(localData)) {
            //Haxe returns without a value from typed functions, which AS3 only allows with a value
            stack.addToOutput(new ReturnValueAVM2Item(ins, localData.lineStartInstruction, new UndefinedAVM2Item(ins, localData.lineStartInstruction)));
            return;
        }
        stack.addToOutput(new ReturnVoidAVM2Item(ins, localData.lineStartInstruction));
    }

    /**
     * Checks whether the method declares a return type other than void and *.
     *
     * @param localData Local data
     * @return True if it does
     */
    private static boolean returnsValue(AVM2LocalData localData) {
        if (localData.methodBody == null || localData.abc == null) {
            return false;
        }
        int methodInfo = localData.methodBody.method_info;
        if (methodInfo < 0 || methodInfo >= localData.abc.method_info.size()) {
            return false;
        }
        int retType = localData.abc.method_info.get(methodInfo).ret_type;
        if (retType == 0) {
            return false;
        }
        Multiname m = localData.abc.constants.getMultiname(retType);
        return m != null && m.name_index > 0 && !"void".equals(localData.abc.constants.getString(m.name_index));
    }

    @Override
    public boolean isExitInstruction() {
        return true;
    }
}

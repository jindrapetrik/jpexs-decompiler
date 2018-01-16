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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.UnparsedAVM2Item;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class NewClassIns extends InstructionDefinition {

    public NewClassIns() {
        super(0x58, "newclass", new int[]{AVM2Code.DAT_CLASS_INDEX}, true);
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) throws InterruptedException {
        int clsIndex = ins.operands[0];
        HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
        stack.pop().toString(writer, LocalData.create(localData.getConstants(), localData.localRegNames, localData.fullyQualifiedNames));
        String baseType = writer.toString();
        ABC abc = localData.abc;
        stack.push(new UnparsedAVM2Item(ins, localData.lineStartInstruction, "new " + abc.constants.getMultiname(abc.instance_info.get(clsIndex).name_index).getName(localData.getConstants(), localData.fullyQualifiedNames, false, true) + ".class extends " + baseType));
    }

    @Override
    public int getStackPopCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }
}

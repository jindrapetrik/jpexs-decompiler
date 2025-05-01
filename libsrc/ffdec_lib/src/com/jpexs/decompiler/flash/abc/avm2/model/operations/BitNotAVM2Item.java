/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphTargetDialect;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnaryOpItem;
import java.util.List;

/**
 * Bitwise NOT.
 *
 * @author JPEXS
 */
public class BitNotAVM2Item extends UnaryOpItem {

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param value Value
     */
    public BitNotAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value) {
        super(AVM2GraphTargetDialect.INSTANCE, instruction, lineStartIns, PRECEDENCE_UNARY, value, "~", "int");
    }

    @Override
    public Object getResult() {
        return ~((long) (double) value.getResultAsNumber());
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, value,
                new AVM2Instruction(0, AVM2Instructions.BitNot, null)
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.INT; //?
        //return TypeItem.UNBOUNDED;
    }

    @Override
    protected void operandToString(GraphTargetItem operand, GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (operand instanceof IntegerValueAVM2Item) {
            Integer val = ((IntegerValueAVM2Item) operand).value;
            if (val > 9) {
                String valHex = Integer.toHexString(val).toUpperCase();
                if (valHex.length() % 2 == 1) {
                    valHex = "0" + valHex;
                }
                writer.append("0x" + valHex);
                return;
            }
        }
        operand.toString(writer, localData, "");
    }
}

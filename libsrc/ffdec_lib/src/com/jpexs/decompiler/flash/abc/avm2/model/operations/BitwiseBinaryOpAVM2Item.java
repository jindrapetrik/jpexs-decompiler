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

import com.jpexs.decompiler.flash.abc.avm2.graph.AVM2GraphTargetDialect;
import com.jpexs.decompiler.flash.abc.avm2.model.DoubleValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.CompoundableBinaryOp;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 * Bitwise binary operation.
 *
 * @author JPEXS
 */
public abstract class BitwiseBinaryOpAVM2Item extends BinaryOpItem implements CompoundableBinaryOp {

    /**
     * Constructor.
     * @param instruction Instruction
     * @param lineStartItem Line start item
     * @param precedence Precedence
     * @param leftSide Left side
     * @param rightSide Right side
     * @param operator Operator
     * @param coerceLeft Coerce left
     * @param coerceRight Coerce right
     */
    public BitwiseBinaryOpAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartItem, int precedence, GraphTargetItem leftSide, GraphTargetItem rightSide, String operator, String coerceLeft, String coerceRight) {
        super(AVM2GraphTargetDialect.INSTANCE, instruction, lineStartItem, precedence, leftSide, rightSide, operator, coerceLeft, coerceRight);
    }

    @Override
    protected void operandToString(GraphTargetItem operand, GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if ((operand instanceof IntegerValueAVM2Item) || (operand instanceof DoubleValueAVM2Item)) {
            long val = operand.getAsLong();
            if (val > 9) {
                String valHex = Long.toHexString(val).toUpperCase();
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

/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.model.operations;

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.EcmaType;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import java.util.List;

public class AddAVM2Item extends BinaryOpItem {

    public AddAVM2Item(AVM2Instruction instruction, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, PRECEDENCE_ADDITIVE, leftSide, rightSide, "+");
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, List<Object> localData) {
        if (rightSide.precedence >= precedence) { //string + vs number +
            if (leftSide.precedence > precedence) {
                hilight("(", writer);
                leftSide.toString(writer, localData);
                hilight(")", writer);
            } else {
                leftSide.toString(writer, localData);
            }
            hilight(" ", writer);
            hilight(operator, writer);
            hilight(" ", writer);

            hilight("(", writer);
            rightSide.toString(writer, localData);
            return hilight(")", writer);
        } else {
            return super.toString(writer, localData);
        }
    }

    @Override
    public Object getResult() {
        if (EcmaScript.type(leftSide.getResult()) == EcmaType.STRING || EcmaScript.type(rightSide.getResult()) == EcmaType.STRING) {
            return "" + leftSide.getResult() + rightSide.getResult();
        }
        return EcmaScript.toNumber(leftSide.getResult()) + EcmaScript.toNumber(rightSide.getResult());
    }
}

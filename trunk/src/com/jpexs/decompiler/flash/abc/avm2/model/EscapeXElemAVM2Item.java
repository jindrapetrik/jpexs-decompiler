/*
 *  Copyright (C) 2012-2013 JPEXS
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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.Helper;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jindra
 */
public class EscapeXElemAVM2Item extends AVM2Item {

    public GraphTargetItem expression;

    public EscapeXElemAVM2Item(AVM2Instruction instruction, GraphTargetItem expression) {
        super(instruction, NOPRECEDENCE);
        this.expression = expression;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        return hilight("{", highlight) + expression.toString(highlight, Helper.toList(constants, localRegNames, fullyQualifiedNames)) + hilight("}", highlight);
    }
}

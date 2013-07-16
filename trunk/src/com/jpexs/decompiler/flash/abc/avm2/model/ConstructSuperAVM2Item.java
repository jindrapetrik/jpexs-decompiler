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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.util.HashMap;
import java.util.List;

public class ConstructSuperAVM2Item extends AVM2Item {

    public GraphTargetItem object;
    public List<GraphTargetItem> args;

    public ConstructSuperAVM2Item(AVM2Instruction instruction, GraphTargetItem object, List<GraphTargetItem> args) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.args = args;
    }

    @Override
    public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        String argStr = "";
        for (int a = 0; a < args.size(); a++) {
            if (a > 0) {
                argStr = argStr + ",";
            }
            argStr = argStr + args.get(a).toString(constants, localRegNames, fullyQualifiedNames);
        }
        String calee = object.toString(constants, localRegNames, fullyQualifiedNames) + ".";
        if (Highlighting.stripHilights(calee).equals("this.")) {
            calee = "";
        }
        return calee + hilight("super(") + argStr + hilight(")");

    }
}

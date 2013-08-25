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
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;

public class CallSuperAVM2Item extends AVM2Item {

    public GraphTargetItem receiver;
    public FullMultinameAVM2Item multiname;
    public List<GraphTargetItem> arguments;
    public boolean isVoid;

    public CallSuperAVM2Item(AVM2Instruction instruction, boolean isVoid, GraphTargetItem receiver, FullMultinameAVM2Item multiname, List<GraphTargetItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.multiname = multiname;
        this.arguments = arguments;
        this.isVoid = isVoid;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        String args = "";
        for (int a = 0; a < arguments.size(); a++) {
            if (a > 0) {
                args = args + hilight(",", highlight);
            }
            args = args + arguments.get(a).toString(highlight, constants, localRegNames, fullyQualifiedNames, highlight);
        }
        String calee = receiver.toString(highlight, constants, localRegNames, fullyQualifiedNames, highlight) + ".";
        if (Highlighting.stripHilights(calee).equals("this.")) {
            calee = "";
        }
        return calee + hilight("super.", highlight) + multiname.toString(highlight, constants, localRegNames, fullyQualifiedNames) + hilight("(", highlight) + args + hilight(")", highlight);
    }
}

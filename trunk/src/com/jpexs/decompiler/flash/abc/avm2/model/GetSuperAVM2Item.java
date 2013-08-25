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

public class GetSuperAVM2Item extends AVM2Item {

    public GraphTargetItem object;
    public FullMultinameAVM2Item propertyName;

    public GetSuperAVM2Item(AVM2Instruction instruction, GraphTargetItem object, FullMultinameAVM2Item propertyName) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        String calee = object.toString(highlight, constants, localRegNames, fullyQualifiedNames) + hilight(".", highlight);
        if (Highlighting.stripHilights(calee).equals("this.")) {
            calee = "";
        }
        return calee + hilight("super.", highlight) + propertyName.toString(highlight, constants, localRegNames, fullyQualifiedNames);
    }
}

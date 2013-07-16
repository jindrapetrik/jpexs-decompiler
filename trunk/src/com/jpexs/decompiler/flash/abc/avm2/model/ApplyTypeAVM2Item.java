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
import com.jpexs.decompiler.flash.helpers.Helper;
import java.util.HashMap;
import java.util.List;

public class ApplyTypeAVM2Item extends AVM2Item {

    public GraphTargetItem object;
    public List<GraphTargetItem> params;

    public ApplyTypeAVM2Item(AVM2Instruction instruction, GraphTargetItem object, List<GraphTargetItem> params) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.params = params;
        this.object = object;
    }

    @Override
    public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        StringBuilder ret = new StringBuilder();
        ret.append(object.toString(Helper.toList(constants, localRegNames, fullyQualifiedNames)));
        if (!params.isEmpty()) {
            ret.append(hilight(".<"));
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    ret.append(hilight(","));
                }
                GraphTargetItem p = params.get(i);
                if (p instanceof NullAVM2Item) {
                    ret.append("*");
                } else {
                    ret.append(p.toString(Helper.toList(constants, localRegNames, fullyQualifiedNames)));
                }
            }
            ret.append(">");
        }
        return ret.toString();
    }
}

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
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;

public class CallPropertyAVM2Item extends AVM2Item {

    public GraphTargetItem receiver;
    public FullMultinameAVM2Item propertyName;
    public List<GraphTargetItem> arguments;
    public boolean isVoid;

    public CallPropertyAVM2Item(AVM2Instruction instruction, boolean isVoid, GraphTargetItem receiver, FullMultinameAVM2Item propertyName, List<GraphTargetItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.propertyName = propertyName;
        this.arguments = arguments;
        this.isVoid = isVoid;
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        formatProperty(writer, receiver, propertyName, localData);
        hilight("(", writer);
        for (int a = 0; a < arguments.size(); a++) {
            if (a > 0) {
                hilight(",", writer);
            }
            arguments.get(a).toString(writer, localData);
        }
        return hilight(")", writer);
    }
}

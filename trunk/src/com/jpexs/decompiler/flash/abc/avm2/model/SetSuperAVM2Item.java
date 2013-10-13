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
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;

public class SetSuperAVM2Item extends AVM2Item {

    //public GraphTargetItem value;
    public GraphTargetItem object;
    public FullMultinameAVM2Item propertyName;

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
    }

    public SetSuperAVM2Item(AVM2Instruction instruction, GraphTargetItem value, GraphTargetItem object, FullMultinameAVM2Item propertyName) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.value = value;
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        if (!object.toString(false, localData).equals("this")) {
            object.toString(writer, localData);
            hilight(".", writer);
        }
        hilight("super.", writer);
        propertyName.toString(writer, localData);
        hilight(" = ", writer);
        return value.toString(writer, localData);
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}

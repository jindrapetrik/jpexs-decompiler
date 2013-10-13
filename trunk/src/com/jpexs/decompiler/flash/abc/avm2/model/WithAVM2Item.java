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
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WithAVM2Item extends AVM2Item {

    public GraphTargetItem scope;
    public List<GraphTargetItem> items;

    public WithAVM2Item(AVM2Instruction instruction, GraphTargetItem scope, List<GraphTargetItem> items) {
        super(instruction, NOPRECEDENCE);
        this.scope = scope;
        this.items = items;
    }

    public WithAVM2Item(AVM2Instruction instruction, GraphTargetItem scope) {
        super(instruction, NOPRECEDENCE);
        this.scope = scope;
        this.items = new ArrayList<>();
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        hilight("with(", writer);
        scope.toString(writer, localData);
        hilight(")", writer).appendNewLine();
        hilight("{", writer).appendNewLine();
        hilight(Graph.INDENTOPEN, writer).appendNewLine();
        /*for (GraphTargetItem ti : items) {
         ret += ti.toString(constants, localRegNames, fullyQualifiedNames) + "\r\n";
         }*/
        hilight(Graph.INDENTCLOSE, writer).appendNewLine();
        return hilight("}", writer);
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }
}

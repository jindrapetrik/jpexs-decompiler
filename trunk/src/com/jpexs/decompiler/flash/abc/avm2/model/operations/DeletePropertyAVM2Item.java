/*
 *  Copyright (C) 2010-2014 JPEXS
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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.DeletePropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.PropertyAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.UnresolvedAVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

public class DeletePropertyAVM2Item extends AVM2Item {

    public GraphTargetItem object;
    public GraphTargetItem propertyName;

    private int line;

    //Constructor for compiler
    public DeletePropertyAVM2Item(GraphTargetItem property, int line) {
        this(null, property, null);
        this.line = line;
    }

    public DeletePropertyAVM2Item(AVM2Instruction instruction, GraphTargetItem object, GraphTargetItem propertyName) {
        super(instruction, PRECEDENCE_UNARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("delete ");
        object.toString(writer, localData);
        writer.append("[");
        propertyName.toString(writer, localData);
        return writer.append("]");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        GraphTargetItem p = object;
        if (p instanceof UnresolvedAVM2Item) {
            p = ((UnresolvedAVM2Item) p).resolved;
        }
        if (!(p instanceof PropertyAVM2Item)) {
            throw new CompilationException("Not a property", line); //TODO: handle line better way
        }

        PropertyAVM2Item prop = (PropertyAVM2Item) p;

        return toSourceMerge(localData, generator, prop.object,
                ins(new DeletePropertyIns(), prop.resolveProperty(localData))
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem("Boolean");
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

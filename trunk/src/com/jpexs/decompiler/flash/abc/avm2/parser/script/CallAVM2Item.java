/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeFunctionItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CallAVM2Item extends AVM2Item {

    public GraphTargetItem name;
    public List<GraphTargetItem> arguments;
    
    public CallAVM2Item(GraphTargetItem name,List<GraphTargetItem> arguments) {
        super(null,NOPRECEDENCE);
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) {
        if(name instanceof PropertyAVM2Item){
            PropertyAVM2Item prop=(PropertyAVM2Item)name;
            return toSourceMerge(localData, generator, prop.object,prop.index,arguments,
                    new AVM2Instruction(0, new CallPropertyIns(), new int[]{prop.resolveProperty(),arguments.size()}, new byte[0])
                    );
        }
        if(name instanceof NameAVM2Item){
            //TODO
        }
        return new ArrayList<>();
    }

    @Override
    public GraphTargetItem returnType() {
        GraphTargetItem ti = name.returnType();
        if(ti instanceof TypeFunctionItem){
            TypeFunctionItem tfi=(TypeFunctionItem)ti;
            return new TypeItem(tfi.fullTypeName);
        }
        return ti;
    }
           
        
}

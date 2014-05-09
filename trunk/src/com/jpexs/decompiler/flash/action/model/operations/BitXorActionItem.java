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
package com.jpexs.decompiler.flash.action.model.operations;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.swf5.ActionBitXor;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.util.List;

public class BitXorActionItem extends BinaryOpItem {

    public BitXorActionItem(GraphSourceItem instruction, GraphTargetItem leftSide, GraphTargetItem rightSide) {
        super(instruction, PRECEDENCE_BITWISEXOR, leftSide, rightSide, "^");
    }

    @Override
    public Object getResult() {
        return ((long) (double) EcmaScript.toNumber(leftSide.getResult())) ^ ((long) (double) EcmaScript.toNumber(rightSide.getResult()));
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if((rightSide instanceof DirectValueActionItem)&&(((DirectValueActionItem)rightSide).value.equals(4.294967295E9))){
            writer.append("~");
            if(leftSide.getPrecedence()>PRECEDENCE_UNARY){
                writer.append("(");
            }
            leftSide.appendTo(writer, localData);
            if(leftSide.getPrecedence()>PRECEDENCE_UNARY){
                writer.append(")");
            }
            return writer;
        }else{
            return super.appendTo(writer, localData);
        }
    }

    
    
    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, leftSide, rightSide, new ActionBitXor());
    }

    @Override
    public GraphTargetItem returnType() {
        return new UnboundedTypeItem();
    }
}

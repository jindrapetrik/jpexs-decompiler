/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TernarOpItem extends GraphTargetItem {

    public GraphTargetItem expression;

    public GraphTargetItem onTrue;

    public GraphTargetItem onFalse;

    public TernarOpItem(GraphSourceItem src, GraphSourceItem lineStartIns, GraphTargetItem expression, GraphTargetItem onTrue, GraphTargetItem onFalse) {
        super(src, lineStartIns, PRECEDENCE_CONDITIONAL);
        this.expression = expression;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(onTrue);
        visitor.visit(onFalse);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (expression.getPrecedence() >= precedence){
            writer.append("(");
        }
        expression.toString(writer, localData);
        if (expression.getPrecedence() >= precedence){
            writer.append(")");
        }
        writer.append(" ? ");
        
        if (onTrue instanceof TernarOpItem){  //ternar in ternar better in parenthesis
            writer.append("(");
        }
        onTrue.toString(writer, localData);
        if (onTrue instanceof TernarOpItem){
            writer.append(")");
        }                
        writer.append(" : ");
        if (onFalse instanceof TernarOpItem){ 
             writer.append("(");
        }
        onFalse.toString(writer, localData);
        if (onFalse instanceof TernarOpItem){ 
             writer.append(")");
        }
        
        return writer;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return generator.generate(localData, this);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        return onTrue.returnType();
    }
}

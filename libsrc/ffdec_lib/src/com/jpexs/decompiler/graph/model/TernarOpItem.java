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
import java.util.Objects;

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
        if (expression.getPrecedence() >= precedence) {
            writer.append("(");
        }
        expression.toString(writer, localData);
        if (expression.getPrecedence() >= precedence) {
            writer.append(")");
        }
        writer.append(" ? ");

        if (onTrue.getPrecedence() >= precedence && onTrue.getPrecedence() != GraphTargetItem.NOPRECEDENCE) {  // >= ternar in ternar better in parenthesis
            writer.append("(");
        }
        onTrue.toString(writer, localData);
        if (onTrue.getPrecedence() >= precedence && onTrue.getPrecedence() != GraphTargetItem.NOPRECEDENCE) {
            writer.append(")");
        }
        writer.append(" : ");
        if (onFalse.getPrecedence() >= precedence && onFalse.getPrecedence() != GraphTargetItem.NOPRECEDENCE) {
            writer.append("(");
        }
        onFalse.toString(writer, localData);
        if (onFalse.getPrecedence() >= precedence && onFalse.getPrecedence() != GraphTargetItem.NOPRECEDENCE) {
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + Objects.hashCode(this.expression);
        hash = 61 * hash + Objects.hashCode(this.onTrue);
        hash = 61 * hash + Objects.hashCode(this.onFalse);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TernarOpItem other = (TernarOpItem) obj;
        if (!Objects.equals(this.expression, other.expression)) {
            return false;
        }
        if (!Objects.equals(this.onTrue, other.onTrue)) {
            return false;
        }
        if (!Objects.equals(this.onFalse, other.onFalse)) {
            return false;
        }
        return true;
    }

}

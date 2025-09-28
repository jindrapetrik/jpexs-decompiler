/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.List;
import java.util.Objects;

/**
 * Ternar operator.
 *
 * @author JPEXS
 */
public class TernarOpItem extends GraphTargetItem {

    /**
     * Expression
     */
    public GraphTargetItem expression;

    /**
     * On true
     */
    public GraphTargetItem onTrue;

    /**
     * On false
     */
    public GraphTargetItem onFalse;

    /**
     * Constructor.
     *
     * @param dialect Dialect
     * @param src Source
     * @param lineStartIns Line start instruction
     * @param expression Expression
     * @param onTrue On true
     * @param onFalse On false
     */
    public TernarOpItem(GraphTargetDialect dialect, GraphSourceItem src, GraphSourceItem lineStartIns, GraphTargetItem expression, GraphTargetItem onTrue, GraphTargetItem onFalse) {
        super(dialect, src, lineStartIns, PRECEDENCE_CONDITIONAL);
        this.expression = expression;
        this.onTrue = onTrue;
        this.onFalse = onFalse;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(expression);
        visitor.visit(onTrue);
        visitor.visit(onFalse);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (expression.getPrecedence() >= precedence) {
            writer.append("(");
        }
        expression.toString(writer, localData, "Boolean", true);
        if (expression.getPrecedence() >= precedence) {
            writer.append(")");
        }
        writer.append(" ? ");

        if (onTrue.getPrecedence() >= precedence && onTrue.getPrecedence() != GraphTargetItem.NOPRECEDENCE) {  // >= ternar in ternar better in parenthesis
            writer.append("(");
        }
        onTrue.toString(writer, localData, "", true);
        if (onTrue.getPrecedence() >= precedence && onTrue.getPrecedence() != GraphTargetItem.NOPRECEDENCE) {
            writer.append(")");
        }
        writer.append(" : ");
        if (onFalse.getPrecedence() >= precedence && onFalse.getPrecedence() != GraphTargetItem.NOPRECEDENCE) {
            writer.append("(");
        }
        onFalse.toString(writer, localData, "", true);
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
        GraphTargetItem onTrueType = onTrue.returnType();
        GraphTargetItem onFalseType = onFalse.returnType();
        if (onTrueType.equals(onFalseType)) {
            return onTrueType;
        }
        if ((onTrueType.equals(TypeItem.NUMBER) || onTrueType.equals(TypeItem.INT) || onTrueType.equals(TypeItem.UINT))
                && (onFalseType.equals(TypeItem.NUMBER) || onFalseType.equals(TypeItem.INT) || onFalseType.equals(TypeItem.UINT))) {
            return TypeItem.NUMBER;
        }
        return TypeItem.UNKNOWN;
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

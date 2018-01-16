/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public abstract class UnaryOpItem extends GraphTargetItem implements UnaryOp {

    public String operator;

    protected String coerce;

    public UnaryOpItem(GraphSourceItem instruction, GraphSourceItem lineStartItem, int precedence, GraphTargetItem value, String operator, String coerce) {
        super(instruction, lineStartItem, precedence, value);
        this.operator = operator;
        this.coerce = coerce;
    }

    @Override
    public GraphTargetItem simplify(String implicitCoerce) {
        GraphTargetItem r = clone();
        r.value = r.value.simplify(coerce);
        return simplifySomething(r, implicitCoerce);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append(operator);
        if (value != null) {
            if (value.getPrecedence() > precedence) {
                writer.append("(");
                value.toString(writer, localData, coerce);
                writer.append(")");
            } else {
                value.toString(writer, localData, coerce);
            }
        } else {
            writer.append("null");
        }
        return writer;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        dependencies.add(value);
        return value.isConvertedCompileTime(dependencies);
    }

    @Override
    public boolean isVariableComputed() {
        return value.isVariableComputed();
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean hasSideEffect() {
        return value.hasSideEffect();
    }

    @Override
    public int getPrecedence() {
        return precedence;
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

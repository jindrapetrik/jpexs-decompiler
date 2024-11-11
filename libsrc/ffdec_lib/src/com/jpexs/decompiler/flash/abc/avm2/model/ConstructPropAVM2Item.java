/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 * Construct property.
 *
 * @author JPEXS
 */
public class ConstructPropAVM2Item extends AVM2Item {

    /**
     * Object
     */
    public GraphTargetItem object;

    /**
     * Property name
     */
    public GraphTargetItem propertyName;

    /**
     * Arguments
     */
    public List<GraphTargetItem> args;

    /**
     * Type
     */
    public GraphTargetItem type;
    
    /**
     * Is static
     */
    public boolean isStatic;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param object Object
     * @param propertyName Property name
     * @param args Arguments
     * @param type Type
     * @param isStatic Is static
     */
    public ConstructPropAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, GraphTargetItem propertyName, List<GraphTargetItem> args, GraphTargetItem type, boolean isStatic) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
        this.args = args;
        this.type = type;
        this.isStatic = isStatic;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(object);
        visitor.visit(propertyName);
        visitor.visitAll(args);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("new ");
        formatProperty(writer, object, propertyName, localData, isStatic, false);        
        writer.spaceBeforeCallParenthesis(args.size());
        writer.append("(");
        for (int a = 0; a < args.size(); a++) {
            if (a > 0) {
                writer.allowWrapHere().append(",");
            }
            args.get(a).toString(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public GraphTargetItem returnType() {
        return type;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.object);
        hash = 67 * hash + Objects.hashCode(this.propertyName);
        hash = 67 * hash + Objects.hashCode(this.args);
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
        final ConstructPropAVM2Item other = (ConstructPropAVM2Item) obj;
        if (!Objects.equals(this.object, other.object)) {
            return false;
        }
        if (!Objects.equals(this.propertyName, other.propertyName)) {
            return false;
        }
        if (!Objects.equals(this.args, other.args)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}

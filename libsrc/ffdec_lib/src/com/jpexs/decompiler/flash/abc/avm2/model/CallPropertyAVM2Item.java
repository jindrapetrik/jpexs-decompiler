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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Objects;

/**
 * Call property.
 *
 * @author JPEXS
 */
public class CallPropertyAVM2Item extends AVM2Item {

    /**
     * Receiver
     */
    public GraphTargetItem receiver;

    /**
     * Property name
     */
    public GraphTargetItem propertyName;

    /**
     * Arguments
     */
    public List<GraphTargetItem> arguments;

    /**
     * Is void
     */
    public boolean isVoid;

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
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param isVoid Is void
     * @param receiver Receiver
     * @param propertyName Property name
     * @param arguments Arguments
     * @param type Type
     * @param isStatic Is static
     */
    public CallPropertyAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, boolean isVoid, GraphTargetItem receiver, GraphTargetItem propertyName, List<GraphTargetItem> arguments, GraphTargetItem type, boolean isStatic) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.propertyName = propertyName;
        this.arguments = arguments;
        this.isVoid = isVoid;
        this.type = type;
        this.isStatic = isStatic;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(receiver);
        visitor.visit(propertyName);
        visitor.visitAll(arguments);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        formatProperty(writer, receiver, propertyName, localData, isStatic, false);
        writer.spaceBeforeCallParenthesis(arguments.size());
        writer.append("(");
        for (int a = 0; a < arguments.size(); a++) {
            if (a > 0) {               
                writer.allowWrapHere().append(",");
            }
            arguments.get(a).toString(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, receiver, arguments,
                new AVM2Instruction(0, AVM2Instructions.CallProperty, new int[]{((AVM2SourceGenerator) generator).propertyName(propertyName), arguments.size()})
        );
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
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.receiver);
        hash = 17 * hash + Objects.hashCode(this.propertyName);
        hash = 17 * hash + Objects.hashCode(this.arguments);
        hash = 17 * hash + (this.isVoid ? 1 : 0);
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
        final CallPropertyAVM2Item other = (CallPropertyAVM2Item) obj;
        if (this.isVoid != other.isVoid) {
            return false;
        }
        if (!Objects.equals(this.receiver, other.receiver)) {
            return false;
        }
        if (!Objects.equals(this.propertyName, other.propertyName)) {
            return false;
        }
        if (!Objects.equals(this.arguments, other.arguments)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }
}

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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.ecma.ObjectType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This object.
 *
 * @author JPEXS
 */
public class ThisAVM2Item extends AVM2Item {

    /**
     * Class name
     */
    public DottedChain className;

    /**
     * Is basic object
     */
    public boolean basicObject;

    /**
     * Show class name
     */
    public boolean showClassName;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param className Class name
     * @param basicObject Is basic object
     * @param showClassName Show class name
     */
    public ThisAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, DottedChain className, boolean basicObject, boolean showClassName) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.className = className;
        this.basicObject = basicObject;
        this.showClassName = showClassName;
        getSrcData().localName = "this";
    }

    /**
     * Checks if this is a basic object.
     * @return True if this is a basic object
     */
    public boolean isBasicObject() {
        return basicObject;
    }

    @Override
    public boolean isConvertedCompileTime(Set<GraphTargetItem> dependencies) {
        return isBasicObject();
    }

    @Override
    public Object getResult() {
        if (basicObject) {
            return new ObjectType(new HashMap<>()) {
                @Override
                public String toString() {
                    return "[object " + className.getLast() + "]";
                }

            };
        }
        return null;
    }

    @Override
    public Boolean getResultAsBoolean() {
        return true;
    }

    @Override
    public String toString() {
        return "this";
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        if (showClassName) {
            if (className != null) {
                if (localData.fullyQualifiedNames != null && localData.fullyQualifiedNames.contains(className)) {
                    return writer.append(className.toPrintableString(true)).append(".this");
                }
                return writer.append(IdentifiersDeobfuscation.printIdentifier(true, className.getLast())).append(".this");
            }
        }
        return writer.append("this");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, new AVM2Instruction(0, AVM2Instructions.GetLocal0, null)
        );
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(className);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
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
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
}

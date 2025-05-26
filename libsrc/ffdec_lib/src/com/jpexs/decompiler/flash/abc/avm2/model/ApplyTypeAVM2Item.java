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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Apply type parameters to object.
 *
 * @author JPEXS
 */
public class ApplyTypeAVM2Item extends AVM2Item {

    /**
     * Object
     */
    public GraphTargetItem object;

    /**
     * Parameters
     */
    public List<GraphTargetItem> params;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param object Object
     * @param params Parameters
     */
    public ApplyTypeAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, List<GraphTargetItem> params) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.params = params;
        this.object = object;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        visitor.visit(object);
        visitor.visitAll(params);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.object);
        hash = 79 * hash + Objects.hashCode(this.params);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ApplyTypeAVM2Item other = (ApplyTypeAVM2Item) obj;
        if (!Objects.equals(this.object, other.object)) {
            return false;
        }
        if (!Objects.equals(this.params, other.params)) {
            return false;
        }
        return true;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        object.toString(writer, localData);
        if (!params.isEmpty()) {
            writer.append(".<");
            for (int i = 0; i < params.size(); i++) {
                if (i > 0) {
                    writer.append(",");
                }
                GraphTargetItem p = params.get(i);
                if (p instanceof NullAVM2Item) {
                    writer.append("*");
                } else {
                    p.toString(writer, localData);
                }
            }
            writer.append(">");
        }
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        //int qname = AVM2SourceGenerator.resolveType(localData, object, ((AVM2SourceGenerator)generator).abc, ((AVM2SourceGenerator)generator).allABCs);
        List<GraphSourceItem> nparams = new ArrayList<>();
        for (GraphTargetItem i : params) {
            nparams.addAll(i.toSource(localData, generator));
        }
        return toSourceMerge(localData, generator,
                object,
                nparams,
                ins(AVM2Instructions.ApplyType, params.size())
        );
    }

    @Override
    public String toString() {
        String ret = object.toString();
        if (!params.isEmpty()) {
            ret += ".<";
            boolean first = true;
            for (GraphTargetItem param : params) {
                if (!first) {
                    ret += ", ";
                }
                ret += param.toString();
                first = false;
            }
            ret += ">";
        }
        return ret;
    }

}

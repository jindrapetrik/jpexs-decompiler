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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.ObjectType;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class NewObjectAVM2Item extends AVM2Item {

    public List<NameValuePair> pairs;

    public NewObjectAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, List<NameValuePair> pairs) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.pairs = pairs;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean singleLine = pairs.size() < 2;
        //no new line before as it may break return clause (#593)
        writer.append("{");
        if (!singleLine) {
            writer.newLine();
            writer.indent();
        }
        for (int n = 0; n < pairs.size(); n++) {
            if (n > 0) {
                writer.append(",").newLine();
            }
            pairs.get(n).toString(writer, localData);
        }
        if (!singleLine) {
            writer.newLine();
            writer.unindent();
        }
        writer.append("}");
        return writer;
    }

    @Override
    public GraphTargetItem returnType() {
        return new TypeItem(DottedChain.OBJECT);
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphTargetItem> args = new ArrayList<>();
        for (NameValuePair p : pairs) {
            args.add(p.name);
            args.add(p.value);
        }
        return toSourceMerge(localData, generator, args,
                new AVM2Instruction(0, AVM2Instructions.NewObject, new int[]{pairs.size()})
        );
    }

    @Override
    public Object getResult() {
        Map<String, Object> props = new HashMap<>();
        for (NameValuePair v : pairs) {
            props.put(EcmaScript.toString(v.name.getResult()), v.value.getResult());
        }
        return new ObjectType(props);
    }

    @Override
    public GraphTargetItem simplify(String implicitCoerce) {
        if (implicitCoerce.isEmpty()) {
            return this;
        } else {
            return super.simplify(implicitCoerce);
        }
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        for (NameValuePair v : pairs) {
            if (!v.name.isCompileTime() || !v.value.isCompileTime()) {
                return false;
            }
        }
        return true;
    }
}

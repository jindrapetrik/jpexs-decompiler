/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ApplyTypeIns;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class InitVectorAVM2Item extends AVM2Item {

    public static final DottedChain VECTOR_PACKAGE = new DottedChain("__AS3__", "vec");

    public static final DottedChain VECTOR_FQN = new DottedChain("__AS3__", "vec", "Vector");

    public static final DottedChain VECTOR_INT = new DottedChain("__AS3__", "vec", "Vector$int");

    public static final DottedChain VECTOR_DOUBLE = new DottedChain("__AS3__", "vec", "Vector$double");

    public static final DottedChain VECTOR_UINT = new DottedChain("__AS3__", "vec", "Vector$uint");

    public static final DottedChain VECTOR_OBJECT = new DottedChain("__AS3__", "vec", "Vector$object");

    public GraphTargetItem subtype;

    public List<GraphTargetItem> arguments;

    List<Integer> openedNamespaces;

    private int allNsSet(ABC abc) {
        int[] nssa = new int[openedNamespaces.size()];
        for (int i = 0; i < openedNamespaces.size(); i++) {
            nssa[i] = openedNamespaces.get(i);
        }
        return abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);
    }

    public InitVectorAVM2Item(AVM2Instruction ins, GraphTargetItem subtype, List<GraphTargetItem> arguments) {
        super(ins, PRECEDENCE_PRIMARY);
        this.subtype = subtype;
        this.arguments = arguments;
    }

    public InitVectorAVM2Item(GraphTargetItem subtype, List<GraphTargetItem> arguments, List<Integer> openedNamespaces) {
        super(null, PRECEDENCE_PRIMARY);
        this.subtype = subtype;
        this.arguments = arguments;
        this.openedNamespaces = openedNamespaces;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("<");
        subtype.appendTo(writer, localData);
        writer.append(">");
        writer.append("[");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                writer.append(",");
            }
            arguments.get(i).appendTo(writer, localData);
        }
        writer.append("]");
        return writer;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        List<GraphTargetItem> pars = new ArrayList<>();
        pars.add(subtype);
        return new ApplyTypeAVM2Item(null, new TypeItem(VECTOR_FQN), pars);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        List<GraphSourceItem> ret = toSourceMerge(localData, generator,
                ins(new FindPropertyStrictIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAME, g.abc.constants.getStringId("Vector", true), 0, g.abc.constants.getNamespaceSetId(new NamespaceSet(new int[]{g.abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, g.abc.constants.getStringId("__AS3__.vec", true)), 0, true)}), true), 0, new ArrayList<>()), true)),
                ins(new GetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAME, g.abc.constants.getStringId("Vector", true), 0, allNsSet(g.abc), 0, new ArrayList<>()), true)),
                subtype,
                ins(new ApplyTypeIns(), 1),
                new IntegerValueAVM2Item(null, (long) arguments.size()),
                ins(new ConstructIns(), 1)
        );
        for (int i = 0; i < arguments.size(); i++) {
            ret.addAll(toSourceMerge(localData, generator,
                    ins(new DupIns()),
                    new IntegerValueAVM2Item(null, (long) i),
                    arguments.get(i),
                    ins(new SetPropertyIns(), g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, g.abc.constants.getNamespaceSetId(new NamespaceSet(new int[]{g.abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, g.abc.constants.getStringId("", true)), 0, true)}), true), precedence, openedNamespaces), true))
            ));
        }
        return ret;
    }
}

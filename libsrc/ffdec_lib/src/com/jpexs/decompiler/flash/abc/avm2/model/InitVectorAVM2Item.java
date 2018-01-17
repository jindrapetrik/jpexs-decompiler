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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AVM2SourceGenerator;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.AbcIndexing;
import com.jpexs.decompiler.flash.abc.avm2.parser.script.NamespaceItem;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
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

    public static final DottedChain VECTOR_PACKAGE = new DottedChain(new String[]{"__AS3__", "vec"}, "");

    public static final DottedChain VECTOR_FQN = new DottedChain(new String[]{"__AS3__", "vec", "Vector"}, "");

    public static final DottedChain VECTOR_INT = new DottedChain(new String[]{"__AS3__", "vec", "Vector$int"}, "");

    public static final DottedChain VECTOR_DOUBLE = new DottedChain(new String[]{"__AS3__", "vec", "Vector$double"}, "");

    public static final DottedChain VECTOR_UINT = new DottedChain(new String[]{"__AS3__", "vec", "Vector$uint"}, "");

    public static final DottedChain VECTOR_OBJECT = new DottedChain(new String[]{"__AS3__", "vec", "Vector$object"}, "");

    public GraphTargetItem subtype;

    public List<GraphTargetItem> arguments;

    List<NamespaceItem> openedNamespaces;

    private int allNsSet(AbcIndexing abc) throws CompilationException {
        return NamespaceItem.getCpoolSetIndex(abc, openedNamespaces);
    }

    public InitVectorAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem subtype, List<GraphTargetItem> arguments) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.subtype = subtype;
        this.arguments = arguments;
    }

    public InitVectorAVM2Item(GraphTargetItem subtype, List<GraphTargetItem> arguments, List<NamespaceItem> openedNamespaces) {
        super(null, null, PRECEDENCE_PRIMARY);
        this.subtype = subtype;
        this.arguments = arguments;
        this.openedNamespaces = openedNamespaces;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("<");
        subtype.appendTry(writer, localData);
        writer.append(">");
        writer.append("[");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) {
                writer.append(",");
            }
            arguments.get(i).appendTry(writer, localData);
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
        return new ApplyTypeAVM2Item(null, null, new TypeItem(VECTOR_FQN), pars);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        ABC abc = g.abcIndex.getSelectedAbc();
        AVM2ConstantPool constants = abc.constants;
        List<GraphSourceItem> ret = toSourceMerge(localData, generator,
                ins(AVM2Instructions.FindPropertyStrict, constants.getMultinameId(Multiname.createMultiname(false, constants.getStringId("Vector", true), constants.getNamespaceSetId(new int[]{constants.getNamespaceId(Namespace.KIND_PACKAGE, "__AS3__.vec", 0, true)}, true)), true)),
                ins(AVM2Instructions.GetProperty, constants.getMultinameId(Multiname.createMultiname(false, constants.getStringId("Vector", true), allNsSet(g.abcIndex)), true)),
                subtype,
                ins(AVM2Instructions.ApplyType, 1),
                new IntegerValueAVM2Item(null, null, (long) arguments.size()),
                ins(AVM2Instructions.Construct, 1)
        );
        for (int i = 0; i < arguments.size(); i++) {
            ret.addAll(toSourceMerge(localData, generator,
                    ins(AVM2Instructions.Dup),
                    new IntegerValueAVM2Item(null, null, (long) i),
                    arguments.get(i),
                    ins(AVM2Instructions.SetProperty, constants.getMultinameId(Multiname.createMultinameL(false, NamespaceItem.getCpoolSetIndex(g.abcIndex, openedNamespaces)), true))
            ));
        }
        return ret;
    }
}

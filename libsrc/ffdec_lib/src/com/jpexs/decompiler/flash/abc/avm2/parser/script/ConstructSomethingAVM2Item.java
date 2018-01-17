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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ConstructSomethingAVM2Item extends CallAVM2Item {

    public List<NamespaceItem> openedNamespaces;

    public ConstructSomethingAVM2Item(int line, List<NamespaceItem> openedNamespaces, GraphTargetItem name, List<GraphTargetItem> arguments) {
        super(openedNamespaces, line, name, arguments);
        this.openedNamespaces = openedNamespaces;
    }

    @Override
    public GraphTargetItem returnType() {
        return name.returnType();
    }

    private int allNsSetWithVec(AbcIndexing abc) throws CompilationException {
        int[] nssa = new int[openedNamespaces.size() + 1];
        for (int i = 0; i < openedNamespaces.size(); i++) {
            nssa[i] = openedNamespaces.get(i).getCpoolIndex(abc);
        }
        nssa[nssa.length - 1] = abc.getSelectedAbc().constants.getNamespaceId(Namespace.KIND_PACKAGE, "__AS3__.vec", 0, true);
        return abc.getSelectedAbc().constants.getNamespaceSetId(nssa, true);

    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {

        GraphTargetItem resname = name;
        if (resname instanceof UnresolvedAVM2Item) {
            resname = ((UnresolvedAVM2Item) resname).resolved;
        }

        if (resname instanceof TypeItem) {
            TypeItem prop = (TypeItem) resname;
            if (localData.isStatic && localData.pkg.addWithSuffix(localData.currentClass).equals(prop.fullTypeName)) {
                return toSourceMerge(localData, generator,
                        new AVM2Instruction(0, AVM2Instructions.GetLocal0, new int[]{}), arguments,
                        new AVM2Instruction(0, AVM2Instructions.Construct, new int[]{arguments.size()}));

            }
            int type_index = AVM2SourceGenerator.resolveType(localData, resname, ((AVM2SourceGenerator) generator).abcIndex);
            return toSourceMerge(localData, generator,
                    new AVM2Instruction(0, AVM2Instructions.FindPropertyStrict, new int[]{type_index, arguments.size()}), arguments,
                    new AVM2Instruction(0, AVM2Instructions.ConstructProp, new int[]{type_index, arguments.size()})
            );
        }

        if (resname instanceof PropertyAVM2Item) {
            PropertyAVM2Item prop = (PropertyAVM2Item) resname;
            return toSourceMerge(localData, generator, prop.resolveObject(localData, generator), arguments,
                    ins(AVM2Instructions.ConstructProp, prop.resolveProperty(localData), arguments.size())
            );
        }

        if (resname instanceof NameAVM2Item) {
            return toSourceMerge(localData, generator, resname, arguments, ins(AVM2Instructions.Construct, arguments.size()));
        }

        if (resname instanceof IndexAVM2Item) {
            return ((IndexAVM2Item) resname).toSource(localData, generator, true, false, arguments, false, true);
        }

        if (resname instanceof NamespacedAVM2Item) {
            return ((NamespacedAVM2Item) resname).toSource(localData, generator, true, false, arguments, false, true);
        }
        return toSourceMerge(localData, generator, resname, arguments, ins(AVM2Instructions.Construct, arguments.size()));
    }
}

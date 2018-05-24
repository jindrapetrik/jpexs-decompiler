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
 * License along with this library.
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.helpers.Reference;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeFunctionItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class CallAVM2Item extends AVM2Item {

    public GraphTargetItem name;

    public List<GraphTargetItem> arguments;

    public int line;

    public List<NamespaceItem> openedNamespaces;

    public CallAVM2Item(List<NamespaceItem> openedNamespaces, int line, GraphTargetItem name, List<GraphTargetItem> arguments) {
        super(null, null, NOPRECEDENCE);
        this.openedNamespaces = openedNamespaces;
        this.name = name;
        this.arguments = arguments;
        this.line = line;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn) throws CompilationException {

        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;

        GraphTargetItem callable = name;
        if (callable instanceof UnresolvedAVM2Item) {
            callable = ((UnresolvedAVM2Item) callable).resolved;
        }
        if (callable instanceof NameAVM2Item) {
            NameAVM2Item n = (NameAVM2Item) callable;
            /*List<ABC> allAbcs = new ArrayList<>();
             allAbcs.add(g.abc);
             allAbcs.addAll(g.allABCs);*/
            String cname = localData.currentClass;
            DottedChain pkgName = localData.pkg;
            GraphTargetItem obj = null;
            Reference<String> outName = new Reference<>("");
            Reference<DottedChain> outNs = new Reference<>(DottedChain.EMPTY);
            Reference<DottedChain> outPropNs = new Reference<>(DottedChain.EMPTY);
            Reference<Integer> outPropNsKind = new Reference<>(1);
            Reference<Integer> outPropNsIndex = new Reference<>(0);
            Reference<GraphTargetItem> outPropType = new Reference<>(null);
            Reference<ValueKind> outPropValue = new Reference<>(null);
            Reference<ABC> outPropValueABC = new Reference<>(null);
            List<Integer> otherNs = new ArrayList<>();
            for (NamespaceItem on : openedNamespaces) {
                if (on.isResolved()) {
                    otherNs.add(on.getCpoolIndex(g.abcIndex));
                }
            }
            if (cname != null && AVM2SourceGenerator.searchPrototypeChain(otherNs, localData.privateNs, localData.protectedNs, true, g.abcIndex, pkgName, cname, n.getVariableName(), outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueABC)) {
                NameAVM2Item nobj = new NameAVM2Item(new TypeItem(localData.getFullClass()), n.line, "this", null, false, n.openedNamespaces);
                nobj.setRegNumber(0);
                obj = nobj;
            }
            PropertyAVM2Item p = new PropertyAVM2Item(obj, n.getVariableName(), g.abcIndex, n.openedNamespaces, new ArrayList<>());
            p.setAssignedValue(n.getAssignedValue());
            callable = p;
        }

        int propIndex = -1;
        if (callable instanceof TypeItem) {
            TypeItem t = (TypeItem) callable;
            propIndex = AVM2SourceGenerator.resolveType(localData, t, ((AVM2SourceGenerator) generator).abcIndex);
        }
        Object obj = null;

        if (callable instanceof PropertyAVM2Item) {
            PropertyAVM2Item prop = (PropertyAVM2Item) callable;
            obj = prop.object;
            if (obj == null) {

                /*List<ABC> allAbcs = new ArrayList<>();
                 allAbcs.add(g.abc);
                 allAbcs.addAll(g.allABCs);
                 */ String cname = localData.currentClass;
                DottedChain pkgName = localData.pkg;
                Reference<String> outName = new Reference<>("");
                Reference<DottedChain> outNs = new Reference<>(DottedChain.EMPTY);
                Reference<DottedChain> outPropNs = new Reference<>(DottedChain.EMPTY);
                Reference<Integer> outPropNsKind = new Reference<>(1);
                Reference<Integer> outPropNsIndex = new Reference<>(0);
                Reference<GraphTargetItem> outPropType = new Reference<>(null);
                Reference<ValueKind> outPropValue = new Reference<>(null);
                Reference<ABC> outPropValueAbc = new Reference<>(null);

                List<Integer> otherNs = new ArrayList<>();
                for (NamespaceItem n : openedNamespaces) {
                    if (n.isResolved()) {
                        otherNs.add(n.getCpoolIndex(g.abcIndex));
                    }
                }

                if (cname != null && AVM2SourceGenerator.searchPrototypeChain(otherNs, localData.privateNs, localData.protectedNs, true, g.abcIndex, pkgName, cname, prop.propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue, outPropValueAbc) && (localData.getFullClass().equals(outNs.getVal().addWithSuffix(outName.getVal()).toRawString()))) {
                    NameAVM2Item nobj = new NameAVM2Item(new TypeItem(localData.getFullClass()), 0, "this", null, false, new ArrayList<>());
                    nobj.setRegNumber(0);
                    obj = nobj;
                }
            }
            propIndex = prop.resolveProperty(localData);
        }

        if (propIndex != -1) {
            if (obj == null) {
                obj = new AVM2Instruction(0, AVM2Instructions.FindPropertyStrict, new int[]{propIndex});
            }

            boolean isSuper = (obj instanceof NameAVM2Item) && "super".equals(((NameAVM2Item) obj).getVariableName());

            int insCode = isSuper
                    ? (needsReturn ? AVM2Instructions.CallSuper : AVM2Instructions.CallSuperVoid)
                    : (needsReturn ? AVM2Instructions.CallProperty : AVM2Instructions.CallPropVoid);
            return toSourceMerge(localData, generator, obj, arguments,
                    ins(insCode, propIndex, arguments.size())
            );
        }

        if (callable instanceof IndexAVM2Item) {
            return ((IndexAVM2Item) callable).toSource(localData, generator, needsReturn, true, arguments, false, false);
        }
        if (callable instanceof NamespacedAVM2Item) {
            return ((NamespacedAVM2Item) callable).toSource(localData, generator, needsReturn, true, arguments, false, false);
        }

        return toSourceMerge(localData, generator, callable, ins(AVM2Instructions.GetGlobalScope), arguments, ins(AVM2Instructions.Call, arguments.size()));
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, true);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, false);
    }

    @Override
    public GraphTargetItem returnType() {
        GraphTargetItem callable = name;
        if (callable instanceof UnresolvedAVM2Item) {
            callable = ((UnresolvedAVM2Item) callable).resolved;
        }

        if (callable instanceof TypeItem) {
            return TypeItem.UNBOUNDED;
        }

        GraphTargetItem ti = callable.returnType();
        if (ti instanceof TypeFunctionItem) {
            TypeFunctionItem tfi = (TypeFunctionItem) ti;
            return new TypeItem(tfi.fullTypeName);
        }
        return ti;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

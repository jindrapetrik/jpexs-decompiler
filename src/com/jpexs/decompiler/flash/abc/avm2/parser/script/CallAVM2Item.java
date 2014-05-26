/*
 * Copyright (C) 2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.FindPropertyStrictIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetGlobalScopeIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
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

    public CallAVM2Item(int line, GraphTargetItem name, List<GraphTargetItem> arguments) {
        super(null, NOPRECEDENCE);
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
            List<ABC> allAbcs = new ArrayList<>();
            allAbcs.add(g.abc);
            allAbcs.addAll(g.allABCs);
            String cname;
            String pkgName = "";
            cname = localData.currentClass;
            pkgName = localData.pkg;
            GraphTargetItem obj = null;
            Reference<String> outName = new Reference<>("");
            Reference<String> outNs = new Reference<>("");
            Reference<String> outPropNs = new Reference<>("");
            Reference<Integer> outPropNsKind = new Reference<>(1);
            Reference<Integer> outPropNsIndex = new Reference<>(0);
            Reference<GraphTargetItem> outPropType = new Reference<>(null);
            Reference<ValueKind> outPropValue = new Reference<>(null);
            if (cname != null && AVM2SourceGenerator.searchPrototypeChain(true, allAbcs, pkgName, cname, n.getVariableName(), outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue)) {
                NameAVM2Item nobj = new NameAVM2Item(new TypeItem(localData.getFullClass()), n.line, "this", null, false, n.openedNamespaces);
                nobj.setRegNumber(0);
                obj = nobj;
            }
            PropertyAVM2Item p = new PropertyAVM2Item(obj, n.getVariableName(), g.abc, g.allABCs, n.openedNamespaces, new ArrayList<MethodBody>());
            p.setAssignedValue(n.getAssignedValue());
            callable = p;
        }

        int propIndex = -1;
        if (callable instanceof TypeItem) {
            TypeItem t = (TypeItem) callable;
            propIndex = AVM2SourceGenerator.resolveType(localData, t, ((AVM2SourceGenerator) generator).abc, ((AVM2SourceGenerator) generator).allABCs);
        }
        Object obj = null;

        if (callable instanceof PropertyAVM2Item) {
            PropertyAVM2Item prop = (PropertyAVM2Item) callable;
            obj = prop.object;
            if (obj == null) {

                List<ABC> allAbcs = new ArrayList<>();
                allAbcs.add(g.abc);
                allAbcs.addAll(g.allABCs);
                String cname;
                String pkgName = "";
                cname = localData.currentClass;
                pkgName = localData.pkg;
                Reference<String> outName = new Reference<>("");
                Reference<String> outNs = new Reference<>("");
                Reference<String> outPropNs = new Reference<>("");
                Reference<Integer> outPropNsKind = new Reference<>(1);
                Reference<Integer> outPropNsIndex = new Reference<>(0);
                Reference<GraphTargetItem> outPropType = new Reference<>(null);
                Reference<ValueKind> outPropValue = new Reference<>(null);
                if (cname != null && AVM2SourceGenerator.searchPrototypeChain(true, allAbcs, pkgName, cname, prop.propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue) && (localData.currentClass.equals("".equals(outNs.getVal()) ? outName.getVal() : outNs.getVal() + "." + outName.getVal()))) {
                    NameAVM2Item nobj = new NameAVM2Item(new TypeItem(localData.getFullClass()), 0, "this", null, false, new ArrayList<Integer>());
                    nobj.setRegNumber(0);
                    obj = nobj;
                }
            }
            propIndex = prop.resolveProperty(localData);
        }

        if (propIndex != -1) {
            if (obj == null) {
                obj = new AVM2Instruction(0, new FindPropertyStrictIns(), new int[]{propIndex}, new byte[0]);
            }
            return toSourceMerge(localData, generator, obj, arguments,
                    ins(needsReturn ? new CallPropertyIns() : new CallPropVoidIns(), propIndex, arguments.size())
            );
        }

        if (callable instanceof IndexAVM2Item) {
            return ((IndexAVM2Item) callable).toSource(localData, generator, needsReturn, true, arguments, false, false);
        }
        if (callable instanceof NamespacedAVM2Item) {
            return ((NamespacedAVM2Item) callable).toSource(localData, generator, needsReturn, true, arguments, false, false);
        }

        return toSourceMerge(localData, generator, callable, ins(new GetGlobalScopeIns()), arguments, ins(new CallIns(), arguments.size()));
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

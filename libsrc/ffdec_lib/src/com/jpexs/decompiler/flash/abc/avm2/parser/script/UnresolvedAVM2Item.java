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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.model.BooleanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.InitVectorAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author JPEXS
 */
public class UnresolvedAVM2Item extends AssignableAVM2Item {

    private DottedChain name;

    private int nsKind = -1;

    public List<NamespaceItem> openedNamespaces;

    public int line;

    public GraphTargetItem type;

    //private GraphTargetItem ns = null;
    public GraphTargetItem resolved;

    public GraphTargetItem resolvedRoot;

    private final boolean mustBeType;

    public List<DottedChain> importedClasses;

    public List<GraphTargetItem> scopeStack = new ArrayList<>();

    public List<GraphTargetItem> subtypes;

    private AbcIndexing abcIndex;

    @Override
    public AssignableAVM2Item copy() {
        UnresolvedAVM2Item c = new UnresolvedAVM2Item(subtypes, importedClasses, mustBeType, type, line, name, assignedValue, openedNamespaces, abcIndex);
        //c.setNs(ns);
        c.nsKind = nsKind;
        c.resolved = resolved;
        return c;
    }

    public void setSlotScope(int slotScope) {
        if (resolved instanceof NameAVM2Item) {
            ((NameAVM2Item) resolved).setSlotScope(slotScope);
        }
    }

    public int getSlotScope() {
        if (resolved instanceof NameAVM2Item) {
            return ((NameAVM2Item) resolved).getSlotScope();
        }
        return -1;
    }

    /*public void setNs(GraphTargetItem ns) {
     this.ns = ns;
     }*/
    public void setRegNumber(int regNumber) {
        if (resolved instanceof NameAVM2Item) {
            ((NameAVM2Item) resolved).setRegNumber(regNumber);
        }
    }

    public int getSlotNumber() {
        if (resolved instanceof NameAVM2Item) {
            return ((NameAVM2Item) resolved).getSlotNumber();
        }
        return -1;
    }

    public void setSlotNumber(int slotNumber) {
        if (resolved instanceof NameAVM2Item) {
            ((NameAVM2Item) resolved).setSlotNumber(slotNumber);
        }
    }

    public int getRegNumber() {
        if (resolved instanceof NameAVM2Item) {
            return ((NameAVM2Item) resolved).getRegNumber();
        }
        return -1;
    }

    /*
     public GraphTargetItem getNs() {
     return ns;
     }
     */
    public void appendName(String name) {
        this.name = this.name.addWithSuffix(name);
    }

    public void setDefinition(boolean definition) {
        if (resolved instanceof NameAVM2Item) {
            ((NameAVM2Item) resolved).setDefinition(definition);
        }
    }

    public void setNsKind(int nsKind) {
        this.nsKind = nsKind;
    }

    public int getNsKind() {
        return nsKind;
    }

    @Override
    public void setAssignedValue(GraphTargetItem storeValue) {
        this.assignedValue = storeValue;
    }

    public DottedChain getVariableName() {
        return name;
    }

    public void setVariableName(DottedChain name) {
        this.name = name;
    }

    public UnresolvedAVM2Item(List<GraphTargetItem> subtypes, List<DottedChain> importedClasses, boolean mustBeType, GraphTargetItem type, int line, DottedChain name, GraphTargetItem storeValue, List<NamespaceItem> openedNamespaces, AbcIndexing abcIndex) {
        super(storeValue);
        this.name = name;
        this.assignedValue = storeValue;
        this.line = line;
        this.type = type;
        this.openedNamespaces = openedNamespaces;
        this.mustBeType = mustBeType;
        this.importedClasses = importedClasses;
        this.subtypes = subtypes;
        this.abcIndex = abcIndex;
    }

    public boolean isDefinition() {
        if (resolved instanceof NameAVM2Item) {
            return ((NameAVM2Item) resolved).isDefinition();
        }
        return false;
    }

    public GraphTargetItem getStoreValue() {
        return assignedValue;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer;
    }

    public static GraphTargetItem getDefaultValue(String type) {
        switch (type) {
            case "*":
                return new UndefinedAVM2Item(null, null);
            case "int":
                return new IntegerValueAVM2Item(null, null, 0);
            case "Boolean":
                return new BooleanAVM2Item(null, null, Boolean.FALSE);
            case "Number":
                return new NanAVM2Item(null, null);
            default:
                return new NullAVM2Item(null, null);
        }
    }

    public static AVM2Instruction generateCoerce(SourceGeneratorLocalData localData, SourceGenerator generator, GraphTargetItem type) throws CompilationException {
        AVM2Instruction ins;
        switch (type.toString()) {
            case "int":
                ins = ins(AVM2Instructions.ConvertI);
                break;
            case "*":
                ins = ins(AVM2Instructions.CoerceA);
                break;
            case "String":
                ins = ins(AVM2Instructions.CoerceS);
                break;
            default:
                int type_index = AVM2SourceGenerator.resolveType(localData, type, ((AVM2SourceGenerator) generator).abcIndex);
                ins = ins(AVM2Instructions.Coerce, type_index);
                break;
        }
        return ins;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (resolved == null) {
            throw new CompilationException("Undefined variable or property: " + toString(), line);
        }
        return resolved.toSource(localData, generator);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (resolved == null) {
            throw new CompilationException("Undefined variable or property: " + toString(), line);
        }
        return resolved.toSourceIgnoreReturnValue(localData, generator);
    }

    @Override
    public boolean hasReturnValue() {
        if (resolved != null) {
            return resolved.hasReturnValue();
        }
        return true;
    }

    @Override
    public boolean needsSemicolon() {
        if (resolved != null) {
            return resolved.needsSemicolon();
        }
        return false;
    }

    @Override
    public String toString() {
        if (resolved != null) {
            return resolved.toString();
        }
        return name.toRawString();
    }

    @Override
    public GraphTargetItem returnType() {
        if (resolved != null) {
            return resolved.returnType();
        }
        if (type == null) {
            return TypeItem.UNBOUNDED;
        }
        return type;
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {
        if (resolved == null) {
            throw new CompilationException("Undefined variable or property: " + toString(), line);
        }
        if (resolved instanceof AssignableAVM2Item) {
            return ((AssignableAVM2Item) resolved).toSourceChange(localData, generator, post, decrement, needsReturn);
        }
        throw new CompilationException("Cannot assign", line);
    }

    public GraphTargetItem resolve(SourceGeneratorLocalData localData /*can be null!!!*/, String currentClassFullName, GraphTargetItem thisType, List<GraphTargetItem> paramTypes, List<String> paramNames, AbcIndexing abc, List<MethodBody> callStack, List<AssignableAVM2Item> variables) throws CompilationException {            
        if (scopeStack.isEmpty()) { //Everything is multiname property in with command

            //search for variable
            for (AssignableAVM2Item a : variables) {
                if (a instanceof NameAVM2Item) {
                    NameAVM2Item n = (NameAVM2Item) a;
                    if (n.isDefinition() && name.get(0).equals(n.getVariableName())) {
                        NameAVM2Item ret = new NameAVM2Item(n.type, n.line, name.isAttribute(0), name.get(0), name.getNamespaceSuffix(0), null, false, openedNamespaces, abcIndex);
                        ret.setSlotScope(n.getSlotScope());
                        ret.setSlotNumber(n.getSlotNumber());
                        ret.setRegNumber(n.getRegNumber());
                        resolved = ret;
                        for (int i = 1; i < name.size(); i++) {
                            resolved = new PropertyAVM2Item(resolved, name.isAttribute(i), name.get(i), name.getNamespaceSuffix(i), abc, openedNamespaces, new ArrayList<>());
                            if (i == name.size() - 1) {
                                ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                            }
                        }
                        if (name.size() == 1) {
                            ret.setAssignedValue(assignedValue);
                        }
                        ret.setNs(n.getNs());
                        return resolvedRoot = ret;
                    }
                }
            }
        }

        if ((paramNames.contains(name.get(0)) || name.get(0).equals("arguments"))) {
            int ind = paramNames.indexOf(name.get(0));
            GraphTargetItem t = TypeItem.UNBOUNDED;
            if (ind == -1) {
                //empty
            } else if (ind < paramTypes.size()) {
                t = paramTypes.get(ind);
            } //else rest parameter

            GraphTargetItem ret = new NameAVM2Item(t, line, name.isAttribute(0), name.get(0), name.getNamespaceSuffix(0), null, false, openedNamespaces, abcIndex);
            resolved = ret;
            for (int i = 1; i < name.size(); i++) {
                resolved = new PropertyAVM2Item(resolved, name.isAttribute(i), name.get(i), name.getNamespaceSuffix(i), abc, openedNamespaces, new ArrayList<>());
                if (i == name.size() - 1) {
                    ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                }
            }
            if (name.size() == 1) {
                ((NameAVM2Item) ret).setAssignedValue(assignedValue);
            }
            return resolvedRoot = ret;
        }

        boolean isProperty = false;
        if (localData != null) { //resolve can be called without localData
            PropertyAVM2Item resolvedx = new PropertyAVM2Item(null, name.isAttribute(0), name.get(0), name.getNamespaceSuffix(0), abc, openedNamespaces, callStack);
            ((PropertyAVM2Item) resolvedx).scopeStack = scopeStack;
            ((PropertyAVM2Item) resolvedx).setAssignedValue(assignedValue);
            Reference<GraphTargetItem> objectType = new Reference<>(null);
            Reference<GraphTargetItem> propertyType = new Reference<>(null);
            Reference<Integer> propertyIndex = new Reference<>(null);
            Reference<ValueKind> propertyValue = new Reference<>(null);
            Reference<ABC> propertyValueABC = new Reference<>(null);
            Reference<Boolean> isType = new Reference<>(false);

            resolvedx.resolve(true, localData, isType, objectType, propertyType, propertyIndex, propertyValue, propertyValueABC);

            if (objectType.getVal() != null && !isType.getVal()) {
                isProperty = true;
            }
        }

        //search same package classes
        if (currentClassFullName != null && !isProperty) {
            DottedChain classChain = DottedChain.parseWithSuffix(currentClassFullName);
            DottedChain pkg = classChain.getWithoutLast();
            
            if (!pkg.isTopLevel()) { //toplevel in next step
                TypeItem ti = new TypeItem(pkg.addWithSuffix(name.get(0)));
                AbcIndexing.ClassIndex ci = abc.findClass(ti, null, null/*FIXME?*/);

                if (ci != null) {                    
                    resolved = ti;
                    for (int i = 1; i < name.size(); i++) {
                        resolved = new PropertyAVM2Item(resolved, name.isAttribute(i), name.get(i), name.getNamespaceSuffix(i), abc, openedNamespaces, new ArrayList<>());
                        if (i == name.size() - 1) {
                            ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                        }
                    }
                    return resolvedRoot = ti;
                }
            }
        }
        
        //Search toplevel classes
        if (currentClassFullName != null && !isProperty) {
            DottedChain pkg = DottedChain.TOPLEVEL;
            
            TypeItem ti = new TypeItem(pkg.addWithSuffix(name.get(0)));
            AbcIndexing.ClassIndex ci = abc.findClass(ti, null, null/*FIXME?*/);

            if (ci != null) {                
                for (DottedChain imp : importedClasses) {
                    String impName = imp.getLast();

                    if (impName.equals(name.get(0))) {
                        throw new CompilationException("The type \"" + name.get(0) +"\" exists on toplevel package and also as an import from different package. Please make it fully qualified so it matches the desired import.", line);
                    }
                }
                resolved = ti;
                for (int i = 1; i < name.size(); i++) {
                    resolved = new PropertyAVM2Item(resolved, name.isAttribute(i), name.get(i), name.getNamespaceSuffix(i), abc, openedNamespaces, new ArrayList<>());
                    if (i == name.size() - 1) {
                        ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                    }
                }
                return resolvedRoot = ti;
            }            
        }               
        
        //Search for types in imported classes
        if (!isProperty) {
            for (DottedChain imp : importedClasses) {
                String impName = imp.getLast();

                if (impName.equals(name.get(0))) {
                    TypeItem ret = new TypeItem(imp);
                    resolved = ret;
                    for (int i = 1; i < name.size(); i++) {
                        resolved = new PropertyAVM2Item(resolved, name.isAttribute(i), name.get(i), name.getNamespaceSuffix(i), abc, openedNamespaces, new ArrayList<>());
                        if (i == name.size() - 1) {
                            ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                        }
                    }

                    if (name.size() == 1) {
                        AbcIndexing.TraitIndex ti = abc.findScriptProperty(imp);
                        if (ti != null && (ti.trait instanceof TraitSlotConst)) {
                            resolved = new ImportedSlotConstItem(ret);
                            if (assignedValue != null) {
                                ((ImportedSlotConstItem) resolved).assignedValue = assignedValue;
                            }
                        }
                    }

                    return resolvedRoot = ret;
                }
            }
        }

        //Search all fully qualitfied types
        if (!isProperty) {
            for (int i = 0; i < name.size(); i++) {
                DottedChain fname = name.subChain(i + 1);
                AbcIndexing.ClassIndex ci = abc.findClass(new TypeItem(fname), localData != null ? abc.getSelectedAbc() : null, localData != null ? localData.scriptIndex : null);
                if (ci != null) {
                    if (!subtypes.isEmpty() && name.size() > i + 1) {
                        continue;
                    }
                    TypeItem ret = new TypeItem(fname);
                    resolved = ret;
                    for (int j = i + 1; j < name.size(); j++) {
                        resolved = new PropertyAVM2Item(resolved, name.isAttribute(j), name.get(j), name.getNamespaceSuffix(j), abc, openedNamespaces, new ArrayList<>());
                        if (j == name.size() - 1) {
                            ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                        }
                    }
                    if (name.size() == i + 1 && assignedValue != null) {
                        throw new CompilationException("Cannot assign type", line);
                    }

                    return resolvedRoot = ret;
                }
            }

            DottedChain classChain = DottedChain.parseWithSuffix(currentClassFullName);
            DottedChain pkg = classChain.getWithoutLast();

            //Search for types in opened namespaces
            for (NamespaceItem n : openedNamespaces) {
                n.resolveCustomNs(abcIndex, importedClasses, pkg, openedNamespaces, localData);
                Namespace ons = abc.getSelectedAbc().constants.getNamespace(n.getCpoolIndex(abc));
                TypeItem ti = new TypeItem(ons.getName(abc.getSelectedAbc().constants).addWithSuffix(name.get(0)));
                AbcIndexing.ClassIndex ci = abc.findClass(ti, null, null/*FIXME?*/);
                if (ci != null) {
                    if (!subtypes.isEmpty() && name.size() > 1) {
                        continue;
                    }
                    TypeItem ret = ti;
                    resolved = ret;
                    for (int i = 1; i < name.size(); i++) {
                        resolved = new PropertyAVM2Item(resolved, name.isAttribute(i), name.get(i), name.getNamespaceSuffix(i), abc, openedNamespaces, new ArrayList<>());
                        if (i == name.size() - 1) {
                            ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                        }
                    }
                    if (name.size() == 1 && assignedValue != null) {
                        throw new CompilationException("Cannot assign type", line);
                    }

                    return resolvedRoot = ret;
                }
            }
        }

        if (!isProperty && (name.get(0).equals("this") || name.get(0).equals("super"))) {
            if (thisType == null) {
                throw new CompilationException("Cannot use this in that context", line);
            }

            boolean isSuper = name.get(0).equals("super");
            GraphTargetItem ntype = thisType;
            if (isSuper) {
                AbcIndexing.ClassIndex ci = abc.findClass(thisType, null, null/*FIXME?*/);
                if (ci == null) {
                    throw new CompilationException("This class not found", line);
                }
                ci = ci.parent;
                if (ci == null) {
                    ntype = new TypeItem("Object");
                } else {
                    ntype = new TypeItem(ci.abc.instance_info.get(ci.index).getName(ci.abc.constants).getNameWithNamespace(ci.abc.constants, true));
                }
            }

            NameAVM2Item ret = new NameAVM2Item(ntype, line, name.isAttribute(0), name.get(0), name.getNamespaceSuffix(0), null, false, openedNamespaces, abcIndex);
            resolved = ret;
            for (int i = 1; i < name.size(); i++) {
                resolved = new PropertyAVM2Item(resolved, name.isAttribute(i), name.get(i), name.getNamespaceSuffix(i), abc, openedNamespaces, new ArrayList<>());
                if (i == name.size() - 1) {
                    ((PropertyAVM2Item) resolved).assignedValue = assignedValue;
                }
            }
            if (name.size() == 1) {
                ret.setAssignedValue(assignedValue);
            }
            return resolvedRoot = ret;
        }

        if (!isProperty && (name.size() == 1 && name.get(0).equals("Vector"))) {
            TypeItem ret = new TypeItem(InitVectorAVM2Item.VECTOR_FQN);
            resolved = ret;
            return resolvedRoot = ret;
        }

        if (mustBeType) {
            throw new CompilationException(name.toPrintableString(true) + " is not an existing type", line);
        }
        resolved = null;
        GraphTargetItem ret = null;
        for (int i = 0; i < name.size(); i++) {
            resolved = new PropertyAVM2Item(resolved, name.isAttribute(i), name.get(i), name.getNamespaceSuffix(i), abc, openedNamespaces, callStack);
            if (ret == null) {
                ((PropertyAVM2Item) resolved).scopeStack = scopeStack;
                ret = resolved;
            }
            if (i == name.size() - 1) {
                ((PropertyAVM2Item) resolved).setAssignedValue(assignedValue);
            }
        }
        return resolvedRoot = ret;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.name);
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
        final UnresolvedAVM2Item other = (UnresolvedAVM2Item) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }

}

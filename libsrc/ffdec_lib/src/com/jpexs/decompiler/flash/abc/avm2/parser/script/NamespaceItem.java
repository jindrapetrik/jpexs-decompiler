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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Namespace.
 *
 * @author JPEXS
 */
public class NamespaceItem {

    /**
     * Custom namespace.
     */
    public static final int KIND_NAMESPACE_CUSTOM = -2;

    /**
     * Name
     */
    public DottedChain name;

    /**
     * Kind
     */
    public int kind;

    /**
     * Namespace index
     */
    private int nsIndex = -1;

    /**
     * Force resolves namespace.
     * @param abcIndex ABC indexing
     */
    public void forceResolve(AbcIndexing abcIndex) {
        nsIndex = abcIndex.getSelectedAbc().constants.getNamespaceId(kind, name, 0, true);
    }

    /**
     * Constructor.
     * @param name Name
     * @param kind Kind
     */
    public NamespaceItem(DottedChain name, int kind) {
        this.name = name;
        this.kind = kind;
    }

    /**
     * Constructor.
     * @param name Name
     * @param kind Kind
     */
    public NamespaceItem(String name, int kind) {
        this.name = DottedChain.parseWithSuffix(name);
        this.kind = kind;
    }

    /**
     * Constructor.
     * @param nsIndex Namespace index
     */
    public NamespaceItem(int nsIndex) {
        this.nsIndex = nsIndex;
    }

    @Override
    public String toString() {
        return Namespace.kindToStr(kind) + " " + name.toRawString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final NamespaceItem other = (NamespaceItem) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return (this.kind == other.kind);
    }

    /**
     * Resolves custom namespace.
     * @param abcIndex ABC indexing
     * @param importedClasses Imported classes
     * @param pkg Package
     * @param openedNamespaces Opened namespaces
     * @param localData Local data
     * @throws CompilationException On compilation error
     */
    public void resolveCustomNs(AbcIndexing abcIndex, List<DottedChain> importedClasses, DottedChain pkg, List<NamespaceItem> openedNamespaces, SourceGeneratorLocalData localData) throws CompilationException {
        if (nsIndex > -1) { //already resolved
            return;
        }
        if (kind == Namespace.KIND_NAMESPACE) {
            nsIndex = abcIndex.getSelectedAbc().constants.getNamespaceId(Namespace.KIND_NAMESPACE, name, 0, true);
        }
        if (kind == KIND_NAMESPACE_CUSTOM) {
            String custom = name.toRawString();
            PropertyAVM2Item prop = new PropertyAVM2Item(null, false, custom, "", abcIndex, openedNamespaces, new ArrayList<>(), false);
            Reference<ValueKind> value = new Reference<>(null);
            Reference<ABC> outAbc = new Reference<>(null);
            Reference<Boolean> isType = new Reference<>(false);
            Reference<Trait> outPropTrait = new Reference<>(null);

            prop.resolve(true, localData, isType, new Reference<>(null), new Reference<>(null), new Reference<>(0), value, outAbc, outPropTrait);
            boolean resolved = true;
            if (value.getVal() == null) {
                resolved = false;
            }
            if (!resolved) {
                DottedChain fullCustom = null;
                for (DottedChain imp : importedClasses) {
                    if (imp.getLast().equals(custom)) {
                        fullCustom = imp;
                        break;
                    }
                }
                if (fullCustom != null) {
                    /*List<ABC> aas = new ArrayList<>();
                     aas.add(abc);
                     aas.addAll(allABCs);*/
                    AbcIndexing.TraitIndex ti = abcIndex.findScriptProperty(fullCustom);

                    if (ti != null) {
                        if (ti.trait instanceof TraitSlotConst) {
                            if (((TraitSlotConst) ti.trait).isNamespace()) {
                                Namespace ns = ti.abc.constants.getNamespace(((TraitSlotConst) ti.trait).value_index);
                                nsIndex = abcIndex.getSelectedAbc().constants.getNamespaceId(ns.kind, ns.getName(ti.abc.constants), 0, true);
                                return;
                            }
                        }
                    }
                }

                throw new CompilationException("Namespace \"" + name + "\" not defined", -1);
            }
            nsIndex = abcIndex.getSelectedAbc().constants.getNamespaceId(Namespace.KIND_NAMESPACE,
                    outAbc.getVal().constants.getNamespace(value.getVal().value_index).getName(outAbc.getVal().constants), 0, true);

        }
    }

    /**
     * Checks if namespace is resolved.
     * @return True if namespace is resolved
     */
    public boolean isResolved() {
        return nsIndex > -1;
    }

    /**
     * Gets constant pool index.
     * @param abcIndex ABC indexing
     * @return Constant pool index
     * @throws CompilationException On compilation error
     */
    public int getCpoolIndex(AbcIndexing abcIndex) throws CompilationException {
        if (nsIndex > -1) {
            return nsIndex;
        }
        if (kind == Namespace.KIND_NAMESPACE) { //must set manually
            throw new CompilationException("Namespace \"" + name + "\" unresolved", -1);
        }
        nsIndex = abcIndex.getSelectedAbc().constants.getNamespaceId(kind, name, 0, true);
        return nsIndex;
    }

    /**
     * Gets namespace set index.
     * @param abcIndex ABC indexing
     * @param namespaces Namespaces
     * @return Namespace set index
     * @throws CompilationException On compilation error
     */
    public static int getCpoolSetIndex(AbcIndexing abcIndex, List<NamespaceItem> namespaces) throws CompilationException {
        int[] nssa = new int[namespaces.size()];
        for (int i = 0; i < nssa.length; i++) {
            nssa[i] = namespaces.get(i).getCpoolIndex(abcIndex);
        }

        return abcIndex.getSelectedAbc().constants.getNamespaceSetId(nssa, true);
    }
}

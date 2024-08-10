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
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import static com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item.ins;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.CompoundableBinaryOp;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Index.
 *
 * @author JPEXS
 */
public class IndexAVM2Item extends AssignableAVM2Item {

    /**
     * Opened namespaces
     */
    private final List<NamespaceItem> openedNamespaces;

    /**
     * Object
     */
    public GraphTargetItem object;

    /**
     * Index
     */
    public GraphTargetItem index;

    /**
     * Attribute
     */
    public boolean attr;

    /**
     * Constructor.
     * @param attr Attribute
     * @param object Object
     * @param index Index
     * @param storeValue Store value
     * @param openedNamespaces Opened namespaces
     */
    public IndexAVM2Item(boolean attr, GraphTargetItem object, GraphTargetItem index, GraphTargetItem storeValue, List<NamespaceItem> openedNamespaces) {
        super(storeValue);
        this.object = object;
        this.index = index;
        this.openedNamespaces = openedNamespaces;
        this.attr = attr;
    }

    private int allNsSet(AbcIndexing abc) throws CompilationException {
        int[] nssa = new int[openedNamespaces.size()];
        for (int i = 0; i < nssa.length; i++) {
            nssa[i] = openedNamespaces.get(i).getCpoolIndex(abc);
        }
        return abc.getSelectedAbc().constants.getNamespaceSetId(nssa, true);
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return null;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public AssignableAVM2Item copy() {
        return new IndexAVM2Item(attr, object, index, assignedValue, openedNamespaces);
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {
        Reference<Integer> obj_temp = new Reference<>(-1);
        Reference<Integer> index_temp = new Reference<>(-1);
        Reference<Integer> val_temp = new Reference<>(-1);
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        int indexPropIndex = g.abcIndex.getSelectedAbc().constants.getMultinameId(Multiname.createMultinameL(attr, allNsSet(g.abcIndex)), true);

        AVM2Instruction changeIns;
        if (localData.numberContext != null) {
            changeIns = ins(decrement ? AVM2Instructions.DecrementP : AVM2Instructions.IncrementP, localData.numberContext);
        } else {
            changeIns = ins(decrement ? AVM2Instructions.Decrement : AVM2Instructions.Increment);
        }
        
        return toSourceMerge(localData, generator,
                object, dupSetTemp(localData, generator, obj_temp),
                index, dupSetTemp(localData, generator, index_temp),
                ins(AVM2Instructions.GetProperty, indexPropIndex),
                post ? ins(AVM2Instructions.ConvertD) : null,
                (!post) ? changeIns : null,
                needsReturn ? ins(AVM2Instructions.Dup) : null,
                post ? changeIns : null,
                setTemp(localData, generator, val_temp),
                getTemp(localData, generator, obj_temp),
                getTemp(localData, generator, index_temp),
                getTemp(localData, generator, val_temp),
                ins(AVM2Instructions.SetProperty, indexPropIndex),
                killTemp(localData, generator, Arrays.asList(val_temp, obj_temp, index_temp))
        );

    }

    /**
     * Convert to source.
     * @param localData Local data
     * @param generator Generator
     * @param needsReturn Needs return
     * @param call Call
     * @param callargs Call arguments
     * @param delete Delete
     * @param construct Construct
     * @return Source
     * @throws CompilationException On compilation error
     */
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn, boolean call, List<GraphTargetItem> callargs, boolean delete, boolean construct) throws CompilationException {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        int indexPropIndex = g.abcIndex.getSelectedAbc().constants.getMultinameId(Multiname.createMultinameL(attr, allNsSet(g.abcIndex)), true);
        Reference<Integer> ret_temp = new Reference<>(-1);

        if (assignedValue != null) {

            if (assignedValue instanceof CompoundableBinaryOp) {
                CompoundableBinaryOp comp = (CompoundableBinaryOp) assignedValue;
                if (comp.getLeftSide() instanceof IndexAVM2Item) {
                    IndexAVM2Item left = (IndexAVM2Item) comp.getLeftSide();
                    if (left.assignedValue == null && Objects.equals(left.object, object) && Objects.equals(left.index, index) && index.hasSideEffect()) {
                        Reference<Integer> val_temp = new Reference<>(-1);
                        Reference<Integer> index_temp = new Reference<>(-1);
                        return toSourceMerge(localData, generator,
                                index,
                                setTemp(localData, generator, index_temp),
                                object,
                                getTemp(localData, generator, index_temp),
                                ins(AVM2Instructions.GetProperty, indexPropIndex),
                                comp.getRightSide(),
                                comp.getOperatorInstruction(),
                                setTemp(localData, generator, val_temp),
                                object,
                                getTemp(localData, generator, index_temp),
                                getTemp(localData, generator, val_temp),
                                needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                                ins(AVM2Instructions.SetProperty, indexPropIndex),
                                needsReturn ? getTemp(localData, generator, ret_temp) : null,
                                killTemp(localData, generator, Arrays.asList(index_temp, val_temp, ret_temp)));
                    }
                }
            }

            return toSourceMerge(localData, generator,
                    object,
                    index,
                    assignedValue,
                    needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                    ins(AVM2Instructions.SetProperty, indexPropIndex),
                    needsReturn ? getTemp(localData, generator, ret_temp) : null,
                    killTemp(localData, generator, Arrays.asList(ret_temp)));
        } else {
            return toSourceMerge(localData, generator,
                    object,
                    call ? dupSetTemp(localData, generator, ret_temp) : null,
                    index,
                    construct ? getTemp(localData, generator, ret_temp) : null,
                    construct ? callargs : null,
                    ins(construct ? AVM2Instructions.ConstructProp : delete ? AVM2Instructions.DeleteProperty : AVM2Instructions.GetProperty, indexPropIndex, construct ? callargs.size() : null),
                    call ? getTemp(localData, generator, ret_temp) : null,
                    call ? callargs : null,
                    call ? ins(AVM2Instructions.Call, callargs.size()) : null,
                    needsReturn ? null : ins(AVM2Instructions.Pop),
                    (call || construct) ? killTemp(localData, generator, Arrays.asList(ret_temp)) : null);
        }

    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, true, false, new ArrayList<>(), false, false);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, false, false, new ArrayList<>(), false, false);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.object);
        hash = 89 * hash + Objects.hashCode(this.index);
        hash = 89 * hash + (this.attr ? 1 : 0);
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
        final IndexAVM2Item other = (IndexAVM2Item) obj;
        if (this.attr != other.attr) {
            return false;
        }
        if (!Objects.equals(this.object, other.object)) {
            return false;
        }
        if (!Objects.equals(this.index, other.index)) {
            return false;
        }
        return true;
    }

}

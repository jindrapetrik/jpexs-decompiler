/*
 *  Copyright (C) 2014-2015 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.DecrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.arithmetic.IncrementIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructPropIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.executing.CallIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.DeletePropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.GetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.SetPropertyIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.DupIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.stack.PopIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.types.ConvertDIns;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class IndexAVM2Item extends AssignableAVM2Item {

    private final List<Integer> openedNamespaces;
    public GraphTargetItem object;
    public GraphTargetItem index;
    public boolean attr;

    public IndexAVM2Item(boolean attr, GraphTargetItem object, GraphTargetItem index, GraphTargetItem storeValue, List<Integer> openedNamespaces) {
        super(storeValue);
        this.object = object;
        this.index = index;
        this.openedNamespaces = openedNamespaces;
        this.attr = attr;
    }

    private int allNsSet(ABC abc) {
        int nssa[] = new int[openedNamespaces.size()];
        for (int i = 0; i < openedNamespaces.size(); i++) {
            nssa[i] = openedNamespaces.get(i);
        }
        return abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);
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
        int indexPropIndex = g.abc.constants.getMultinameId(new Multiname(attr ? Multiname.MULTINAMELA : Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true);

        return toSourceMerge(localData, generator,
                object, dupSetTemp(localData, generator, obj_temp),
                index, dupSetTemp(localData, generator, index_temp),
                ins(new GetPropertyIns(), indexPropIndex),
                post ? ins(new ConvertDIns()) : null,
                (!post) ? (decrement ? ins(new DecrementIns()) : ins(new IncrementIns())) : null,
                needsReturn ? ins(new DupIns()) : null,
                post ? (decrement ? ins(new DecrementIns()) : ins(new IncrementIns())) : null,
                setTemp(localData, generator, val_temp),
                getTemp(localData, generator, obj_temp),
                getTemp(localData, generator, index_temp),
                getTemp(localData, generator, val_temp),
                ins(new SetPropertyIns(), indexPropIndex),
                killTemp(localData, generator, Arrays.asList(val_temp, obj_temp, index_temp))
        );

    }

    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn, boolean call, List<GraphTargetItem> callargs, boolean delete, boolean construct) throws CompilationException {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        int indexPropIndex = g.abc.constants.getMultinameId(new Multiname(attr ? Multiname.MULTINAMELA : Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true);
        Reference<Integer> ret_temp = new Reference<>(-1);

        if (assignedValue != null) {
            return toSourceMerge(localData, generator,
                    object,
                    index,
                    assignedValue,
                    needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                    ins(new SetPropertyIns(), indexPropIndex),
                    needsReturn ? getTemp(localData, generator, ret_temp) : null,
                    killTemp(localData, generator, Arrays.asList(ret_temp)));
        } else {
            return toSourceMerge(localData, generator,
                    object,
                    call ? ins(new DupIns()) : null,
                    index,
                    construct ? callargs : null,
                    ins(construct ? new ConstructPropIns() : delete ? new DeletePropertyIns() : new GetPropertyIns(), indexPropIndex, construct ? callargs.size() : null),
                    call ? callargs : null,
                    call ? ins(new CallIns(), callargs.size()) : null,
                    needsReturn ? null : ins(new PopIns()));
        }

    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, true, false, new ArrayList<GraphTargetItem>(), false, false);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, false, false, new ArrayList<GraphTargetItem>(), false, false);
    }

}

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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.IdentifiersDeobfuscation;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal2;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class DefineLocalActionItem extends ActionItem implements SetTypeActionItem {

    public GraphTargetItem name;

    private int tempRegister = -1;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(name);
        if (value != null) {
            ret.add(value);
        }
        return ret;
    }

    @Override
    public void setValue(GraphTargetItem value) {
        this.value = value;
    }

    @Override
    public int getTempRegister() {
        return tempRegister;
    }

    @Override
    public void setTempRegister(int tempRegister) {
        this.tempRegister = tempRegister;
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
    }

    public DefineLocalActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem name, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY, value);
        this.name = name;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {

        writer.append("var ");

        HighlightData srcData = getSrcData();
        srcData.localName = name.toStringNoQuotes(localData);
        srcData.declaration = true;
        if (((name instanceof DirectValueActionItem)) && (((DirectValueActionItem) name).isString()) && (!IdentifiersDeobfuscation.isValidName(false, ((DirectValueActionItem) name).toStringNoQuotes(localData), "this", "super"))) {
            IdentifiersDeobfuscation.appendObfuscatedIdentifier(((DirectValueActionItem) name).toStringNoQuotes(localData), writer);
        } else {
            stripQuotes(name, localData, writer);
        }
        if (value == null) {
            return writer;
        }
        writer.append(" = ");
        return value.toString(writer, localData);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        ret.addAll(name.getNeededSources());
        return ret;
    }

    @Override
    public GraphTargetItem getObject() {
        return new DefineLocalActionItem(getSrc(), getLineStartItem(), name, null);
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        if (value == null) {
            return toSourceMerge(localData, generator, name, new ActionDefineLocal2());
        } else {
            return toSourceMerge(localData, generator, name, value, new ActionDefineLocal());
        }

    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

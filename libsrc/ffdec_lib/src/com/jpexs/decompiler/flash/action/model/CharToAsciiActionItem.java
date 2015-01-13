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
 * License along with this library. */
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf4.ActionCharToAscii;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Set;

public class CharToAsciiActionItem extends ActionItem {

    public CharToAsciiActionItem(GraphSourceItem instruction, GraphTargetItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("ord");
        writer.spaceBeforeCallParenthesies(1);
        writer.append("(");
        value.toString(writer, localData);
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (value instanceof DirectValueActionItem) {
            DirectValueActionItem dv = (DirectValueActionItem) value;
            if (dv.value instanceof String) {
                String s = (String) dv.value;
                if (s.length() > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Object getResult() {
        Object res = value.getResult();
        String s = res.toString();
        if (s.length() > 0) {
            char c = s.charAt(0);
            return (int) c;
        }
        return 0;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, value, new ActionCharToAscii());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

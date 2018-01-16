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

/**
 *
 * @author JPEXS
 */
public class CharToAsciiActionItem extends ActionItem {

    public CharToAsciiActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem value) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY, value);
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
        return getResult(value.getResult());
    }

    public static int getResult(Object ch) {
        String s = ch.toString();
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

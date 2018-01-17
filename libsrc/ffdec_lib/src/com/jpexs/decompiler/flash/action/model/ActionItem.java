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
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class ActionItem extends GraphTargetItem implements Serializable {

    public ActionItem() {
        super(null, null, NOPRECEDENCE);
    }

    public ActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, int precedence) {
        this(instruction, lineStartIns, precedence, null);
    }

    public ActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, int precedence, GraphTargetItem value) {
        super(instruction, lineStartIns, precedence, value);
    }

    protected boolean isEmptyString(GraphTargetItem target) {
        if (target instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) target).value instanceof String) {

                if (((DirectValueActionItem) target).value.equals("")) {
                    return true;
                }
            }
        }
        return false;
    }

    protected GraphTextWriter stripQuotes(GraphTargetItem target, LocalData localData, GraphTextWriter writer) throws InterruptedException {
        if (target instanceof DirectValueActionItem) {
            if (((DirectValueActionItem) target).value instanceof String) {
                return writer.append((String) ((DirectValueActionItem) target).value);
            }
        }
        if (target == null) {
            return writer;
        } else {
            return target.toString(writer, localData);
        }
    }

    protected List<GraphSourceItem> toSourceCall(SourceGeneratorLocalData localData, SourceGenerator gen, List<GraphTargetItem> list) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            ret.addAll(0, list.get(i).toSource(localData, gen));
        }
        ret.add(new ActionPush((Long) (long) list.size()));
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphSourceItem> ret = toSource(localData, generator);
        if (hasReturnValue()) {
            ret.add(new ActionPop());
        }
        return ret;
    }

    @Override
    public GraphTargetItem returnType() {
        return new UnboundedTypeItem();
    }
}

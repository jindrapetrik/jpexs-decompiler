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
import com.jpexs.decompiler.flash.action.swf4.ActionStartDrag;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class StartDragActionItem extends ActionItem {

    public GraphTargetItem target;

    public GraphTargetItem lockCenter;

    public GraphTargetItem constrain;

    public GraphTargetItem y2;

    public GraphTargetItem x2;

    public GraphTargetItem y1;

    public GraphTargetItem x1;

    public StartDragActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem target, GraphTargetItem lockCenter, GraphTargetItem constrain, GraphTargetItem x1, GraphTargetItem y1, GraphTargetItem x2, GraphTargetItem y2) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.target = target;
        this.lockCenter = lockCenter;
        this.constrain = constrain;
        this.y2 = y2;
        this.x2 = x2;
        this.y1 = y1;
        this.x1 = x1;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        boolean hasConstrains = true;
        if (constrain instanceof DirectValueActionItem) {
            if (Double.compare(constrain.getResultAsNumber(), 0) == 0) {
                hasConstrains = false;
            }
        }
        writer.append("startDrag");
        writer.spaceBeforeCallParenthesies(2);
        writer.append("(");
        target.toString(writer, localData);
        writer.append(",");
        lockCenter.toString(writer, localData);
        if (hasConstrains) {
            writer.append(",");
            x1.toString(writer, localData);
            writer.append(",");
            y1.toString(writer, localData);
            writer.append(",");
            x2.toString(writer, localData);
            writer.append(",");
            y2.toString(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(target.getNeededSources());
        ret.addAll(constrain.getNeededSources());
        ret.addAll(x1.getNeededSources());
        ret.addAll(x2.getNeededSources());
        ret.addAll(y1.getNeededSources());
        ret.addAll(y2.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        boolean hasConstrains = true;
        if (constrain instanceof DirectValueActionItem) {
            if (Double.compare(constrain.getResultAsNumber(), 0) == 0) {
                hasConstrains = false;
            }
        }
        if (hasConstrains) {
            return toSourceMerge(localData, generator, x1, y1, x2, y2, constrain, lockCenter, target, new ActionStartDrag());
        } else {
            return toSourceMerge(localData, generator, constrain, lockCenter, target, new ActionStartDrag());
        }
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

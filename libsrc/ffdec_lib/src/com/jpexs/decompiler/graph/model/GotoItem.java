/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.graph.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.GraphTargetVisitorInterface;
import com.jpexs.decompiler.graph.TypeItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class GotoItem extends GraphTargetItem implements Block {

    public String labelName;

    public List<GraphTargetItem> targetCommands = null;

    public GotoItem(GraphSourceItem src, GraphSourceItem lineStartIns, String labelName) {
        super(src, lineStartIns, PRECEDENCE_PRIMARY);
        this.labelName = labelName;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (targetCommands != null) {
            if (labelName != null) {
                writer.append(labelName);
                writer.append(":");
                writer.newLine();
            }
            appendCommands(value, writer, localData, targetCommands, false);
        } else {
            writer.append("§§goto(").append(labelName).append(")");
        }
        return writer;
    }

    @Override
    public boolean needsSemicolon() {
        if (targetCommands != null) {
            return false;
        }
        return super.needsSemicolon();
    }

    @Override
    public boolean needsNewLine() {
        if (targetCommands != null) {
            return false;
        }
        return super.needsNewLine();
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public Object getResult() {
        return null;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        if (targetCommands == null) {
            return ret;
        }
        for (GraphTargetItem c : targetCommands) {
            if (c instanceof ContinueItem) {
                ret.add((ContinueItem) c);
            }
        }
        return ret;
    }

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (targetCommands != null) {
            ret.add(targetCommands);
        }
        return ret;
    }

    @Override
    public void visit(GraphTargetVisitorInterface visitor) {
        if (targetCommands != null) {
            visitor.visitAll(targetCommands);
        }
    }

    @Override
    public void visitNoBlock(GraphTargetVisitorInterface visitor) {
    }
}

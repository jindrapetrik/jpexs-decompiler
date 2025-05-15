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
package com.jpexs.decompiler.flash.action.model.operations;

import com.jpexs.decompiler.flash.action.ActionGraphTargetDialect;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.BinaryOpItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Check if object is part of another object.
 *
 * @author JPEXS
 */
public class InActionItem extends BinaryOpItem {

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param name Name
     * @param object Object
     */
    public InActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, ActionItem name, ActionItem object) {
        super(ActionGraphTargetDialect.INSTANCE, instruction, lineStartIns, PRECEDENCE_RELATIONAL, name, object, "in", "", "");
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        return false;
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.BOOLEAN;
    }

    @Override
    public List<GraphSourceItem> getOperatorInstruction() {
        List<GraphSourceItem> ret = new ArrayList<>();
        return ret;
    }
}

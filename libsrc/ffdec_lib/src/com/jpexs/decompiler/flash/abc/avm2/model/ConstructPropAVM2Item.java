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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ConstructPropAVM2Item extends AVM2Item {

    public GraphTargetItem object;

    public GraphTargetItem propertyName;

    public List<GraphTargetItem> args;

    public ConstructPropAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, GraphTargetItem propertyName, List<GraphTargetItem> args) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.object = object;
        this.propertyName = propertyName;
        this.args = args;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("new ");
        int idx = writer.getLength();
        object.toString(writer, localData);
        if (idx < writer.getLength()) {
            writer.append(".");
        }
        propertyName.toString(writer, localData);
        writer.spaceBeforeCallParenthesies(args.size());
        writer.append("(");
        for (int a = 0; a < args.size(); a++) {
            if (a > 0) {
                writer.append(",");
            }
            args.get(a).toString(writer, localData);
        }
        return writer.append(")");
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

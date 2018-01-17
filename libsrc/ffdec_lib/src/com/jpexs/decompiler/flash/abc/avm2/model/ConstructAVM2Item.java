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
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ConstructAVM2Item extends AVM2Item {

    public GraphTargetItem object;

    public List<GraphTargetItem> args;

    public ConstructAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem object, List<GraphTargetItem> args) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.object = object;
        this.args = args;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (object instanceof NewFunctionAVM2Item) {
            writer.append("new ");
            return object.toString(writer, localData);
        }
        writer.append("new ");
        if (object.getPrecedence() > getPrecedence()) {
            writer.append("(");
        }
        object.toString(writer, localData);
        if (object.getPrecedence() > getPrecedence()) {
            writer.append(")");
        }
        writer.spaceBeforeCallParenthesies(args.size());
        if (object instanceof InitVectorAVM2Item) {
            return writer;
        }
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
        return object.returnType();
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

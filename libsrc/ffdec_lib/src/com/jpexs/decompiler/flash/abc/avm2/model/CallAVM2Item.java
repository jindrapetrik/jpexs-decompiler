/*
 *  Copyright (C) 2010-2016 JPEXS, All rights reserved.
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
public class CallAVM2Item extends AVM2Item {

    public GraphTargetItem receiver;

    public GraphTargetItem function;

    public List<GraphTargetItem> arguments;

    public CallAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, GraphTargetItem receiver, GraphTargetItem function, List<GraphTargetItem> arguments) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        /*String recPart = ""; receiver.toString(constants, localRegNames) + writer.append(".");
         if (receiver instanceof NewActivationAVM2Item) {
         recPart = "";
         }
         if (receiver instanceof ThisAVM2Item) {
         recPart = "";
         }*/
        if (function.getPrecedence() > precedence) {
            writer.append("(");
            function.toString(writer, localData);
            writer.append(")");
        } else {
            function.toString(writer, localData);
        }
        writer.spaceBeforeCallParenthesies(arguments.size());
        writer.append("(");
        for (int a = 0; a < arguments.size(); a++) {
            if (a > 0) {
                writer.append(",");
            }
            arguments.get(a).toString(writer, localData);
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

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

import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 *
 * @author JPEXS
 */
public class StrictModeActionItem extends ActionItem {

    public int mode;

    public StrictModeActionItem(GraphSourceItem instruction, GraphSourceItem lineStartIns, int mode) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.mode = mode;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) {
        writer.append("StrictMode");
        writer.spaceBeforeCallParenthesies(0);
        // I still don't know how AS source of Strict Mode instruction looks like, assuming this...
        return writer.append("(").append(mode).append(");");
    }

    @Override
    public boolean hasReturnValue() {
        return false; //FIXME ?
    }
}

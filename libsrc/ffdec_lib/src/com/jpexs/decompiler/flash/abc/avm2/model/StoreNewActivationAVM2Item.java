/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.helpers.hilight.HighlightData;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 * Store new Activation object. This exists just for the purpose of passing
 * activation register to the GraphTextWriter to correctly be read by debug info
 * injector.
 *
 * @author JPEXS
 */
public class StoreNewActivationAVM2Item extends AVM2Item {

    /**
     * Register index
     */
    public int regIndex;

    /**
     * Constructor.
     *
     * @param instruction Instruction
     * @param lineStartIns Line start instruction
     * @param regIndex Register index
     */
    public StoreNewActivationAVM2Item(GraphSourceItem instruction, GraphSourceItem lineStartIns, int regIndex) {
        super(instruction, lineStartIns, PRECEDENCE_PRIMARY);
        this.regIndex = regIndex;
    }

    @Override
    public boolean needsNewLine() {
        return false;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        HighlightData hd = new HighlightData();
        hd.activationRegIndex = regIndex;
        writer.addCurrentMethodData(hd);
        return writer;
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
    public boolean isEmpty() {
        return true;
    }
}

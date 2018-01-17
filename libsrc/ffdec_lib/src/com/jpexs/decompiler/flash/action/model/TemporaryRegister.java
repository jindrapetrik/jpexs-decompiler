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
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class TemporaryRegister extends ActionItem {

    private final int regId;

    public TemporaryRegister(int regId, GraphTargetItem value) {
        super(value.getSrc(), value.getLineStartItem(), value.getPrecedence(), value);
        this.regId = regId;
    }

    public int getRegId() {
        return regId;
    }

    @Override
    public String toString() {
        return "temp reg " + regId + ":" + value.toString();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return value.toString(writer, localData);
    }

    @Override
    public boolean hasReturnValue() {
        return value.hasReturnValue();
    }

    @Override
    public Object getResult() {
        return value.getResult();
    }

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        return value.getAllSubItems();
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        return value.getNeededSources();
    }

    @Override
    public GraphTargetItem getNotCoerced() {
        return value.getNotCoerced();
    }

    @Override
    public GraphTargetItem getNotCoercedNoDup() {
        return value.getNotCoercedNoDup();
    }
}

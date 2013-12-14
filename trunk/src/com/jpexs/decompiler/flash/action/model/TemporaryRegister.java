/*
 *  Copyright (C) 2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
public class TemporaryRegister extends GraphTargetItem {

    private int regId;

    public TemporaryRegister(int regId, GraphTargetItem value) {
        super(value.src, value.precedence);
        this.value = value;
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
    protected GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
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
}

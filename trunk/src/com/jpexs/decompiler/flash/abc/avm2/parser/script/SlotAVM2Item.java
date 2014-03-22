/*
 * Copyright (C) 2010-2014 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 *
 * @author JPEXS
 */
public class SlotAVM2Item extends AVM2Item {

    private final int nsKind;
    private boolean isStatic;
    public String var;
    public GraphTargetItem type;

    public int getNsKind() {
        return nsKind;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public SlotAVM2Item(boolean isStatic, int nsKind, String var, GraphTargetItem type, GraphTargetItem value) {
        super(null, NOPRECEDENCE);
        this.nsKind = nsKind;
        this.value = value;
        this.isStatic = isStatic;
        this.var = var;
        this.type = type;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        return writer; //TODO
    }

}

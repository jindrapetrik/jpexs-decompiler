/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineLocal2;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class DefineLocalActionItem extends ActionItem implements SetTypeActionItem {

    public GraphTargetItem name;
    //public GraphTargetItem value;
    private int tempRegister = -1;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(name);
        if (value != null) {
            ret.add(value);
        }
        return ret;
    }

    @Override
    public void setValue(GraphTargetItem value) {
        this.value = value;
    }

    @Override
    public int getTempRegister() {
        return tempRegister;
    }

    @Override
    public void setTempRegister(int tempRegister) {
        this.tempRegister = tempRegister;
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
    }

    public DefineLocalActionItem(GraphSourceItem instruction, GraphTargetItem name, GraphTargetItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        if (value == null) {
            return hilight("var  ", highlight) + stripQuotes(name, constants, highlight);
        }
        return hilight("var  ", highlight) + stripQuotes(name, constants, highlight) + hilight(" = ", highlight) + value.toString(highlight, constants);
    }

    @Override
    public List<com.jpexs.decompiler.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        ret.addAll(name.getNeededSources());
        return ret;
    }

    @Override
    public GraphTargetItem getObject() {
        return new DefineLocalActionItem(src, name, null);
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        if (value == null) {
            return toSourceMerge(localData, generator, name, new ActionDefineLocal2());
        } else {
            return toSourceMerge(localData, generator, name, value, new ActionDefineLocal());
        }

    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

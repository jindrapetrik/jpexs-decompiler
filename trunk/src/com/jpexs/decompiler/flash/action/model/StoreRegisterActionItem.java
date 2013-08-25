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

import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.List;

public class StoreRegisterActionItem extends ActionItem implements SetTypeActionItem {

    public RegisterNumber register;
    //public GraphTargetItem value;
    public boolean define = false;
    public boolean temporary = false;

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
    }

    @Override
    public void setValue(GraphTargetItem value) {
        this.value = value;
    }
    private int tempRegister = -1;

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

    public StoreRegisterActionItem(GraphSourceItem instruction, RegisterNumber register, GraphTargetItem value, boolean define) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
        this.register = register;
        this.define = define;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        return temporary ? value.toString(highlight, constants) : ((define ? hilight("var ", highlight) : "") + hilight(register.translate() + " = ", highlight) + value.toString(highlight, constants));
    }

    @Override
    public GraphTargetItem getObject() {
        return new DirectValueActionItem(src, -1, register, null);
    }

    @Override
    public List<com.jpexs.decompiler.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, value, new ActionStoreRegister(register.number));
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public boolean isCompileTime() {
        return value.isCompileTime();
    }

    @Override
    public Object getResult() {
        return value.getResult();
    }
}

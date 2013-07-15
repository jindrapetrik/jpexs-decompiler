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
package com.jpexs.decompiler.flash.action.treemodel;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.parser.script.ActionScriptSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetProperty;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.graph.GraphPart;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import java.util.List;

public class SetPropertyTreeItem extends TreeItem implements SetTypeTreeItem {

    public GraphTargetItem target;
    public int propertyIndex;
    //public GraphTargetItem value;

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

    public SetPropertyTreeItem(GraphSourceItem instruction, GraphTargetItem target, int propertyIndex, GraphTargetItem value) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.target = target;
        this.propertyIndex = propertyIndex;
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        if (isEmptyString(target)) {
            return hilight(Action.propertyNames[propertyIndex] + "=") + value.toString(constants);
        }
        return target.toString(constants) + hilight("." + Action.propertyNames[propertyIndex] + "=") + value.toString(constants);
    }

    @Override
    public GraphTargetItem getObject() {
        return new GetPropertyTreeItem(src, target, propertyIndex);
    }

    @Override
    public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(target.getNeededSources());
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        ActionScriptSourceGenerator asGenerator = (ActionScriptSourceGenerator) generator;
        int tmpReg = asGenerator.getTempRegister(localData);
        return toSourceMerge(localData, generator, target, new ActionPush((Long) (long) propertyIndex), value, new ActionStoreRegister(tmpReg), new ActionSetProperty(), new ActionPush(new RegisterNumber(tmpReg)));
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, target, new ActionPush((Long) (long) propertyIndex), value, new ActionSetProperty());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

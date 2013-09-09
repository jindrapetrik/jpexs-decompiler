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

import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class SetMemberActionItem extends ActionItem implements SetTypeActionItem {

    public GraphTargetItem object;
    public GraphTargetItem objectName;
    //public GraphTargetItem value;
    private int tempRegister = -1;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(object);
        ret.add(value);
        return ret;
    }

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
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

    public SetMemberActionItem(GraphSourceItem instruction, GraphTargetItem object, GraphTargetItem objectName, GraphTargetItem value) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.object = object;
        this.objectName = objectName;
        this.value = value;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants) {
        if (!((objectName instanceof DirectValueActionItem) && (((DirectValueActionItem) objectName).value instanceof String))) {
            //if(!(functionName instanceof GetVariableActionItem))
            return object.toString(highlight, constants) + hilight("[", highlight) + stripQuotes(objectName, constants, highlight) + hilight("]", highlight) + hilight(" = ", highlight) + value.toString(highlight, constants);
        }
        return object.toString(highlight, constants) + hilight(".", highlight) + stripQuotes(objectName, constants, highlight) + hilight(" = ", highlight) + value.toString(highlight, constants);
    }

    @Override
    public GraphTargetItem getObject() {
        return new GetMemberActionItem(src, object, objectName);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(object.getNeededSources());
        ret.addAll(objectName.getNeededSources());
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        int tmpReg = asGenerator.getTempRegister(localData);
        return toSourceMerge(localData, generator, object, objectName, value, new ActionStoreRegister(tmpReg), new ActionSetMember(), new ActionPush(new RegisterNumber(tmpReg)));
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, object, objectName, value, new ActionSetMember());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

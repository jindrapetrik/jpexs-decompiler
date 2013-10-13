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

import com.jpexs.decompiler.flash.action.model.operations.SubtractActionItem;
import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetProperty;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDecrement;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

public class PostDecrementActionItem extends ActionItem implements SetTypeActionItem {

    public GraphTargetItem object;
    private int tempRegister = -1;

    @Override
    public List<GraphTargetItem> getAllSubItems() {
        List<GraphTargetItem> ret = new ArrayList<>();
        ret.add(object);
        return ret;
    }

    public PostDecrementActionItem(GraphSourceItem instruction, GraphTargetItem object) {
        super(instruction, PRECEDENCE_POSTFIX);
        this.object = object;
    }

    @Override
    protected HilightedTextWriter appendTo(HilightedTextWriter writer, LocalData localData) {
        object.toString(writer, localData);
        return writer.append("--");
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public GraphTargetItem getObject() {
        return object;
    }

    @Override
    public GraphTargetItem getValue() {
        return new SubtractActionItem(null, object, new DirectValueActionItem(null, 0, new Long(1), null));
    }

    @Override
    public void setTempRegister(int regIndex) {
        tempRegister = regIndex;
    }

    @Override
    public int getTempRegister() {
        return tempRegister;
    }

    @Override
    public void setValue(GraphTargetItem value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> ret = new ArrayList<>();

        if (object instanceof GetVariableActionItem) {
            GetVariableActionItem gv = (GetVariableActionItem) object;
            ret.addAll(gv.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetVariable
            ret.addAll(gv.toSource(localData, generator));
            ret.add(new ActionDecrement());
            ret.add(new ActionSetVariable());
        } else if (object instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) object;
            ret.addAll(mem.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetMember
            ret.addAll(mem.toSource(localData, generator));
            ret.add(new ActionDecrement());
            ret.add(new ActionSetMember());
        } else if ((object instanceof DirectValueActionItem) && ((DirectValueActionItem) object).value instanceof RegisterNumber) {
            RegisterNumber rn = (RegisterNumber) ((DirectValueActionItem) object).value;
            ret.add(new ActionPush(new RegisterNumber(rn.number)));
            ret.add(new ActionDecrement());
            ret.add(new ActionStoreRegister(rn.number));
            ret.add(new ActionPop());
        } else if (object instanceof GetPropertyActionItem) {
            GetPropertyActionItem gp = (GetPropertyActionItem) object;
            ret.addAll(gp.toSource(localData, generator));
            ret.remove(ret.size() - 1);
            ret.addAll(gp.toSource(localData, generator));
            ret.add(new ActionDecrement());
            ret.add(new ActionSetProperty());
        }
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, object.toSource(localData, generator), toSourceIgnoreReturnValue(localData, generator));
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

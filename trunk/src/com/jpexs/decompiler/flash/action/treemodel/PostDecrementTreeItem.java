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

import com.jpexs.decompiler.flash.action.swf4.ActionPop;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetProperty;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionDecrement;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.treemodel.operations.SubtractTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class PostDecrementTreeItem extends TreeItem implements SetTypeTreeItem {

    public GraphTargetItem object;
    private int tempRegister = -1;

    public PostDecrementTreeItem(GraphSourceItem instruction, GraphTargetItem object) {
        super(instruction, PRECEDENCE_POSTFIX);
        this.object = object;
    }

    @Override
    public String toString(ConstantPool constants) {
        return object.toString(constants) + hilight("--");
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
        return new SubtractTreeItem(null, object, new DirectValueTreeItem(null, 0, new Long(1), null));
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

        if (object instanceof GetVariableTreeItem) {
            GetVariableTreeItem gv = (GetVariableTreeItem) object;
            ret.addAll(gv.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetVariable
            ret.addAll(gv.toSource(localData, generator));
            ret.add(new ActionDecrement());
            ret.add(new ActionSetVariable());
        } else if (object instanceof GetMemberTreeItem) {
            GetMemberTreeItem mem = (GetMemberTreeItem) object;
            ret.addAll(mem.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetMember
            ret.addAll(mem.toSource(localData, generator));
            ret.add(new ActionDecrement());
            ret.add(new ActionSetMember());
        } else if ((object instanceof DirectValueTreeItem) && ((DirectValueTreeItem) object).value instanceof RegisterNumber) {
            RegisterNumber rn = (RegisterNumber) ((DirectValueTreeItem) object).value;
            ret.add(new ActionPush(new RegisterNumber(rn.number)));
            ret.add(new ActionDecrement());
            ret.add(new ActionStoreRegister(rn.number));
            ret.add(new ActionPop());
        } else if (object instanceof GetPropertyTreeItem) {
            GetPropertyTreeItem gp = (GetPropertyTreeItem) object;
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

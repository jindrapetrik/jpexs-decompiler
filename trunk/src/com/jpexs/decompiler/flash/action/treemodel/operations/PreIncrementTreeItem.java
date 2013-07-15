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
package com.jpexs.decompiler.flash.action.treemodel.operations;

import com.jpexs.decompiler.flash.action.parser.script.ActionScriptSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetProperty;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionIncrement;
import com.jpexs.decompiler.flash.action.swf5.ActionSetMember;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.treemodel.DirectValueTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetMemberTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetPropertyTreeItem;
import com.jpexs.decompiler.flash.action.treemodel.GetVariableTreeItem;
import com.jpexs.decompiler.flash.ecma.*;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import com.jpexs.decompiler.flash.graph.UnaryOpItem;
import java.util.ArrayList;
import java.util.List;

public class PreIncrementTreeItem extends UnaryOpItem {

    public PreIncrementTreeItem(GraphSourceItem instruction, GraphTargetItem object) {
        super(instruction, PRECEDENCE_UNARY, object, "++");
    }

    @Override
    public Object getResult() {
        return EcmaScript.toNumber(value.getResult()) + 1;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        ActionScriptSourceGenerator asGenerator = (ActionScriptSourceGenerator) generator;
        List<GraphSourceItem> ret = new ArrayList<>();

        if (value instanceof GetVariableTreeItem) {
            GetVariableTreeItem gv = (GetVariableTreeItem) value;
            ret.addAll(gv.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetVariable
            ret.addAll(gv.toSource(localData, generator));
            ret.add(new ActionIncrement());
            int tmpReg = asGenerator.getTempRegister(localData);
            ret.add(new ActionStoreRegister(tmpReg));
            ret.add(new ActionSetVariable());
            ret.add(new ActionPush(new RegisterNumber(tmpReg)));
        } else if (value instanceof GetMemberTreeItem) {
            GetMemberTreeItem mem = (GetMemberTreeItem) value;
            ret.addAll(mem.toSource(localData, generator));
            ret.remove(ret.size() - 1); //ActionGetMember
            ret.addAll(mem.toSource(localData, generator));
            ret.add(new ActionIncrement());
            int tmpReg = asGenerator.getTempRegister(localData);
            ret.add(new ActionStoreRegister(tmpReg));
            ret.add(new ActionSetMember());
            ret.add(new ActionPush(new RegisterNumber(tmpReg)));
        } else if ((value instanceof DirectValueTreeItem) && ((DirectValueTreeItem) value).value instanceof RegisterNumber) {
            RegisterNumber rn = (RegisterNumber) ((DirectValueTreeItem) value).value;
            ret.add(new ActionPush(new RegisterNumber(rn.number)));
            ret.add(new ActionIncrement());
            ret.add(new ActionStoreRegister(rn.number));
        } else if (value instanceof GetPropertyTreeItem) {
            GetPropertyTreeItem gp = (GetPropertyTreeItem) value;
            ret.addAll(gp.toSource(localData, generator)); // old value
            ret.addAll(gp.toSource(localData, generator));
            ret.remove(ret.size() - 1);
            ret.addAll(gp.toSource(localData, generator));
            ret.add(new ActionIncrement());
            ret.add(new ActionSetProperty());
        }
        return ret;
    }
}

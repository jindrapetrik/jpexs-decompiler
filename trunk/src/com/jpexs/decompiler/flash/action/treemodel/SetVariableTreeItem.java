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

import com.jpexs.decompiler.flash.action.parser.script.ActionScriptSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf4.ActionSetVariable;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.graph.GraphPart;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import java.util.List;

public class SetVariableTreeItem extends TreeItem implements SetTypeTreeItem {

    public GraphTargetItem name;
    //public GraphTargetItem value;
    private int tempRegister = -1;

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

    public SetVariableTreeItem(GraphSourceItem instruction, GraphTargetItem name, GraphTargetItem value) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString(ConstantPool constants) {
        if (name instanceof DirectValueTreeItem || name instanceof GetVariableTreeItem) {
            return stripQuotes(name, constants) + hilight("=") + value.toString(constants);
        } else {
            return hilight("set(") + name.toString(constants) + hilight(",") + value.toString(constants) + hilight(")");
        }
    }

    @Override
    public GraphTargetItem getObject() {
        return new GetVariableTreeItem(src, name);
    }

    @Override
    public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(name.getNeededSources());
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public boolean isCompileTime() {
        return value.isCompileTime();
    }

    @Override
    public boolean hasSideEffect() {
        return true;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        ActionScriptSourceGenerator asGenerator = (ActionScriptSourceGenerator) generator;
        int tmpReg = asGenerator.getTempRegister(localData);
        return toSourceMerge(localData, generator, name, value, new ActionStoreRegister(tmpReg), new ActionSetVariable(), new ActionPush(new RegisterNumber(tmpReg)));
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, name, value, new ActionSetVariable());
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

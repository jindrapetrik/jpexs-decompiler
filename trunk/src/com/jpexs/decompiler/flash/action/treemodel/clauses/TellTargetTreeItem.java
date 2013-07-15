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
package com.jpexs.decompiler.flash.action.treemodel.clauses;

import com.jpexs.decompiler.flash.action.swf3.ActionSetTarget;
import com.jpexs.decompiler.flash.action.swf4.ActionSetTarget2;
import com.jpexs.decompiler.flash.action.treemodel.*;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import java.util.ArrayList;
import java.util.List;

public class TellTargetTreeItem extends TreeItem {

    public List<GraphTargetItem> commands;
    public GraphTargetItem target;

    public TellTargetTreeItem(GraphSourceItem instruction, GraphTargetItem target, List<GraphTargetItem> commands) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.target = target;
        this.commands = commands;
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = hilight("tellTarget(") + target.toString(constants) + hilight(")\r\n{\r\n");
        for (GraphTargetItem ti : commands) {
            ret += ti.toString(constants) + "\r\n";
        }
        ret += hilight("}");
        return ret;
    }

    @Override
    public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(target.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(target.toSource(localData, generator));
        ret.add(new ActionSetTarget2());
        ret.addAll(generator.generate(localData, commands));
        ret.add(new ActionSetTarget(""));
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}

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
import com.jpexs.decompiler.flash.action.swf4.ActionGetProperty;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.graph.SourceGenerator;
import java.util.List;

public class GetPropertyTreeItem extends TreeItem {

    public GraphTargetItem target;
    public int propertyIndex;

    public GetPropertyTreeItem(GraphSourceItem instruction, GraphTargetItem target, int propertyIndex) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.target = target;
        this.propertyIndex = propertyIndex;
    }

    @Override
    public String toString(ConstantPool constants) {
        if (isEmptyString(target)) {
            return hilight(Action.propertyNames[propertyIndex]);
        }
        return target.toString(constants) + hilight("." + Action.propertyNames[propertyIndex]);
    }

    @Override
    public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
        List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(target.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(List<Object> localData, SourceGenerator generator) {
        return toSourceMerge(localData, generator, target, new ActionPush((Long) (long) propertyIndex), new ActionGetProperty());
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}

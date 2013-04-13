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
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import static com.jpexs.decompiler.flash.graph.GraphTargetItem.PRECEDENCE_ASSIGMENT;
import java.util.List;

public class SetPropertyTreeItem extends TreeItem implements SetTypeTreeItem {

    public GraphTargetItem target;
    public int propertyIndex;
    public GraphTargetItem value;

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
}

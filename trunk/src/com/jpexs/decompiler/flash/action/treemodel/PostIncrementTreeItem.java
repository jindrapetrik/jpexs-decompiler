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

import com.jpexs.decompiler.flash.action.treemodel.operations.AddTreeItem;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;

public class PostIncrementTreeItem extends TreeItem implements SetTypeTreeItem {

    public GraphTargetItem object;
    private int tempRegister = -1;

    public PostIncrementTreeItem(GraphSourceItem instruction, GraphTargetItem object) {
        super(instruction, PRECEDENCE_POSTFIX);
        this.object = object;
    }

    @Override
    public String toString(ConstantPool constants) {
        return object.toString(constants) + hilight("++");
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
        return new AddTreeItem(null, object, new DirectValueTreeItem(null, 0, new Long(1), null));
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

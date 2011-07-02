/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.action.treemodel.operations;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.treemodel.ConstantPool;
import com.jpexs.asdec.action.treemodel.TreeItem;


public abstract class UnaryOpTreeItem extends TreeItem {
    public TreeItem value;
    public String operator;

    public UnaryOpTreeItem(Action instruction, int precedence, TreeItem value, String operator) {
        super(instruction, precedence);
        this.value = value;
        this.operator = operator;
    }

    @Override
    public String toString(ConstantPool constants) {
        String s = value.toString(constants);
        if (value.precedence > precedence) s = "(" + s + ")";
        return hilight(operator) + s;
    }
}

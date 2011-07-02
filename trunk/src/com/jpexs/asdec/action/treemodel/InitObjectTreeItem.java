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

package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

import java.util.List;

public class InitObjectTreeItem extends TreeItem {
    public List<TreeItem> names;
    public List<TreeItem> values;

    public InitObjectTreeItem(Action instruction, List<TreeItem> names, List<TreeItem> values) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.values = values;
        this.names = names;
    }

    @Override
    public String toString(ConstantPool constants) {
        String objStr = "";
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) objStr += ",";
            objStr += names.get(i).toString(constants) + ":" + values.get(i).toString(constants);
        }
        return "{" + objStr + "}";
    }
}

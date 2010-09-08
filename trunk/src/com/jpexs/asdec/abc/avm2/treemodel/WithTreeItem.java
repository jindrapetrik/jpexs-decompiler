/*
 *  Copyright (C) 2010 JPEXS
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

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.ArrayList;
import java.util.List;


public class WithTreeItem extends TreeItem {
    public TreeItem scope;
    public List<TreeItem> items;

    public WithTreeItem(AVM2Instruction instruction, TreeItem scope, List<TreeItem> items) {
        super(instruction, NOPRECEDENCE);
        this.scope = scope;
        this.items = items;
    }

    public WithTreeItem(AVM2Instruction instruction, TreeItem scope) {
        super(instruction, NOPRECEDENCE);
        this.scope = scope;
        this.items = new ArrayList<TreeItem>();
    }


    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        ret = hilight("with(") + scope.toString(constants) + hilight(")\r\n{\r\n");
        for (TreeItem ti : items) {
            ret += ti.toString(constants) + "\r\n";
        }
        ret += hilight("}");
        return ret;
    }

}

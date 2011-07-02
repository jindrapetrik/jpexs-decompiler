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

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;


public class EachTreeItem extends TreeItem {
    public TreeItem object;
    public TreeItem collection;

    public EachTreeItem(AVM2Instruction instruction, TreeItem object, TreeItem collection) {
        super(instruction, NOPRECEDENCE);
        this.object = object;
        this.collection = collection;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("each (") + object.toString(constants) + hilight(" in ") + collection.toString(constants) + ")";
    }


}

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
import com.jpexs.asdec.helpers.Highlighting;


public class SetSuperTreeItem extends TreeItem {
    public TreeItem value;
    public TreeItem object;
    public FullMultinameTreeItem propertyName;

    public SetSuperTreeItem(AVM2Instruction instruction, TreeItem value, TreeItem object, FullMultinameTreeItem propertyName) {
        super(instruction, PRECEDENCE_ASSIGMENT);
        this.value = value;
        this.object = object;
        this.propertyName = propertyName;
    }


    @Override
    public String toString(ConstantPool constants) {
        String calee = object.toString(constants) + ".";
        if (Highlighting.stripHilights(calee).equals("this.")) calee = "";
        return calee + hilight("super.") + propertyName.toString(constants) + hilight("=") + value.toString(constants) + ";";
    }


}

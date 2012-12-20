/*
 *  Copyright (C) 2010-2011 JPEXS
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

package com.jpexs.asdec.abc.avm2.treemodel.operations;

import com.jpexs.asdec.abc.avm2.ConstantPool; import java.util.HashMap;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.abc.avm2.treemodel.FullMultinameTreeItem;
import com.jpexs.asdec.abc.avm2.treemodel.TreeItem;


public class DeletePropertyTreeItem extends TreeItem {
    public TreeItem object;
    public FullMultinameTreeItem propertyName;

    public DeletePropertyTreeItem(AVM2Instruction instruction, TreeItem object, FullMultinameTreeItem propertyName) {
        super(instruction, PRECEDENCE_UNARY);
        this.object = object;
        this.propertyName = propertyName;
    }

    @Override
    public String toString(ConstantPool constants, HashMap<Integer,String> localRegNames) {
        return hilight("delete ") + object.toString(constants,localRegNames) + "[" + propertyName.toString(constants,localRegNames) + "]";
    }


}

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
import com.jpexs.asdec.action.swf4.RegisterNumber;

public class StoreRegisterTreeItem extends TreeItem {
    public RegisterNumber register;
    public TreeItem value;

    public StoreRegisterTreeItem(Action instruction, RegisterNumber register, TreeItem value) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.value = value;
        this.register = register;
    }

    @Override
    public String toString(ConstantPool constants) {
        return register.toString() + "=" + value.toString(constants) + ";";
    }
}

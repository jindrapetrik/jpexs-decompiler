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
import com.jpexs.asdec.abc.types.Namespace;


public class FullMultinameTreeItem extends TreeItem {
    public int multinameIndex;
    public TreeItem name;
    public TreeItem namespace;

    public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex, TreeItem name) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = null;
    }

    public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = null;
        this.namespace = null;
    }

    public FullMultinameTreeItem(AVM2Instruction instruction, int multinameIndex, TreeItem name, TreeItem namespace) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = namespace;
    }

    public boolean isRuntime() {
        return (name != null) || (namespace != null);
    }

    @Override
    public String toString(ConstantPool constants) {
        String ret = "";
        if (name != null) {
            ret = name.toString(constants);
        } else {
            ret = constants.constant_multiname[multinameIndex].getName(constants);
        }
        if (namespace != null) {
            ret = ret + "[" + namespace.toString(constants) + "]";
        } else {
            Namespace ns = constants.constant_multiname[multinameIndex].getNamespace(constants);
            if (ns != null) {
                ret = ret + "[" + ns.getName(constants) + "]";
            }
        }

        if ((name == null) && (namespace == null)) {
            ret = hilight(constants.constant_multiname[multinameIndex].getName(constants));
        }
        return ret;
    }


}

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

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;

import java.util.List;


public class CallTreeItem extends TreeItem {
    public TreeItem receiver;
    public TreeItem function;
    public List<TreeItem> arguments;

    public CallTreeItem(AVM2Instruction instruction, TreeItem receiver, TreeItem function, List<TreeItem> arguments) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.receiver = receiver;
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public String toString(ConstantPool constants) {
        String args = "";
        for (int a = 0; a < arguments.size(); a++) {
            if (a > 0) {
                args = args + ",";
            }
            args = args + arguments.get(a).toString(constants);
        }
        return receiver.toString(constants) + hilight(".") + function.toString(constants) + hilight("(") + args + hilight(")");
    }


}

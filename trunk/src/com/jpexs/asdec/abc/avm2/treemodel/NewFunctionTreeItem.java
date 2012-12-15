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


public class NewFunctionTreeItem extends TreeItem {
    public String paramStr;
    public String returnStr;
    public String functionBody;

    public NewFunctionTreeItem(AVM2Instruction instruction, String paramStr, String returnStr, String functionBody) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.paramStr = paramStr;
        this.returnStr = returnStr;
        this.functionBody = functionBody;
    }

    @Override
    public String toString(ConstantPool constants) {
        return hilight("new function(" + paramStr + "):" + returnStr + "\r\n{\r\n") + functionBody + hilight("}\r\n");
    }


}

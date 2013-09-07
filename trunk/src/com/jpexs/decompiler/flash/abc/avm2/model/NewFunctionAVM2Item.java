/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import com.jpexs.decompiler.graph.Graph;
import java.util.HashMap;
import java.util.List;

public class NewFunctionAVM2Item extends AVM2Item {

    public String paramStr;
    public String returnStr;
    public String functionBody;
    public String functionName;

    public NewFunctionAVM2Item(AVM2Instruction instruction, String functionName, String paramStr, String returnStr, String functionBody) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.paramStr = paramStr;
        this.returnStr = returnStr;
        this.functionBody = functionBody;
        this.functionName = functionName;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        String ret = hilight("function" + (!functionName.equals("") ? " " + functionName : "") + "(" + paramStr + "):" + returnStr, highlight);
        ret += "\r\n" + hilight("{", highlight) + "\r\n";
        ret += Graph.INDENTOPEN + "\r\n";
        ret += (highlight ? functionBody : Highlighting.stripHilights(functionBody)) + "\r\n";
        ret += Graph.INDENTCLOSE + "\r\n";
        ret += hilight("}", highlight);
        return ret;
    }
}

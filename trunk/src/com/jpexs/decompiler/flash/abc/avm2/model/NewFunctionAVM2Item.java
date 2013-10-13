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

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.flash.helpers.hilight.Highlighting;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.model.LocalData;

public class NewFunctionAVM2Item extends AVM2Item {

    public String paramStr;
    public String returnStr;
    public String functionBody;
    public String functionName;
    public int methodIndex;

    public NewFunctionAVM2Item(AVM2Instruction instruction, String functionName, String paramStr, String returnStr, String functionBody, int methodIndex) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.paramStr = paramStr;
        this.returnStr = returnStr;
        this.functionBody = functionBody;
        this.functionName = functionName;
        this.methodIndex = methodIndex;
    }

    @Override
    public HilightedTextWriter toString(HilightedTextWriter writer, LocalData localData) {
        hilight("function" + (!functionName.equals("") ? " " + functionName : ""), writer);
        boolean highlight = writer.getIsHighlighted();
        String mhead = "(" + (highlight ? paramStr : Highlighting.stripHilights(paramStr)) + "):" + (highlight ? returnStr : Highlighting.stripHilights(returnStr));
        writer.appendNoHilight(writer.getIsHighlighted() ? Highlighting.hilighMethod(mhead, methodIndex) : mhead);
        writer.appendNewLine();
        hilight("{", writer).appendNewLine();
        hilight(Graph.INDENTOPEN, writer).appendNewLine();
        writer.appendNoHilight((writer.getIsHighlighted() ? functionBody : Highlighting.stripHilights(functionBody)));
        writer.appendNewLine();
        hilight(Graph.INDENTCLOSE, writer).appendNewLine();
        hilight("}", writer);
        return writer;
    }
}

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
import com.jpexs.decompiler.graph.Graph;
import java.util.HashMap;
import java.util.List;

public class NewObjectAVM2Item extends AVM2Item {

    public List<NameValuePair> pairs;

    public NewObjectAVM2Item(AVM2Instruction instruction, List<NameValuePair> pairs) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.pairs = pairs;
    }

    @Override
    public String toString(boolean highlight, ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        String params = "";
        for (int n = 0; n < pairs.size(); n++) {
            if (n > 0) {
                params += hilight(",", highlight) + "\r\n";
            }
            params += pairs.get(n).toString(highlight, constants, localRegNames, fullyQualifiedNames);
        }
        if (pairs.size() < 2) {
            return hilight("{", highlight) + params + hilight("}", highlight);
        }
        return "\r\n" + Graph.INDENTOPEN + "\r\n" + hilight("{", highlight) + "\r\n" + params + "\r\n" + hilight("}", highlight) + "\r\n" + Graph.INDENTCLOSE + "\r\n";
    }
}

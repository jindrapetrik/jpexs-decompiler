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
package com.jpexs.decompiler.flash.abc.avm2.treemodel;

import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.HashMap;
import java.util.List;

public class ConvertTreeItem extends TreeItem {

    //public GraphTargetItem value;
    public String type;

    public ConvertTreeItem(AVM2Instruction instruction, GraphTargetItem value, String type) {
        super(instruction, NOPRECEDENCE);
        this.value = value;
        this.type = type;
    }

    @Override
    public String toString(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        return value.toString(constants, localRegNames, fullyQualifiedNames);
    }

    @Override
    public GraphTargetItem getNotCoerced() {
        return value.getNotCoerced();
    }

    @Override
    public Object getResult() {
        switch (type) {
            case "Boolean":
                return EcmaScript.toBoolean(value.getResult());
            case "Number":
                return EcmaScript.toNumber(value.getResult());
            case "int":
                return (int) (double) EcmaScript.toNumber(value.getResult());
            case "uint":
                return (int) (double) EcmaScript.toUint32(value.getResult());
            case "String":
                return value.getResult().toString();
            case "Object":
                return value.getResult(); //if not object throw TypeError
            default:
                return new Object();
        }
    }

    @Override
    public boolean isCompileTime() {
        return value.isCompileTime();
    }
}

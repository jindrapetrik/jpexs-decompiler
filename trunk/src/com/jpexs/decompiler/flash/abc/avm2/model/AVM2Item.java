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
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.helpers.Helper;
import java.util.HashMap;
import java.util.List;

public abstract class AVM2Item extends GraphTargetItem {

    public AVM2Instruction instruction;
    public boolean hidden = false;

    public AVM2Item(GraphSourceItem instruction, int precedence) {
        super(instruction, precedence);
    }

    @Override
    @SuppressWarnings("unchecked")
    public String toString(boolean highlight, List<Object> localData) {
        return toString(highlight, (ConstantPool) localData.get(0), (HashMap<Integer, String>) localData.get(1), (List<String>) localData.get(2));
    }

    public abstract String toString(boolean highlight, ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames);

    public String toStringNoH(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        return toString(false, constants, localRegNames, fullyQualifiedNames);
    }

    public String toStringSemicoloned(boolean highlight, ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        return toString(highlight, constants, localRegNames, fullyQualifiedNames) + (needsSemicolon() ? ";" : "");
    }

    @Override
    public boolean needsSemicolon() {
        return true;
    }

    protected String formatProperty(boolean highlight, ConstantPool constants, GraphTargetItem object, GraphTargetItem propertyName, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        String obStr = object.toString(highlight, Helper.toList(constants, localRegNames, fullyQualifiedNames));
        if (object.precedence > PRECEDENCE_PRIMARY) {
            obStr = "(" + obStr + ")";
        }
        if (object instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) object).computedValue != null) {
                if (((LocalRegAVM2Item) object).computedValue.getThroughNotCompilable() instanceof FindPropertyAVM2Item) {
                    obStr = "";
                }
            }
        }
        if (obStr.equals("")) {
            return propertyName.toString(highlight, Helper.toList(constants, localRegNames, fullyQualifiedNames));
        }
        if (propertyName instanceof FullMultinameAVM2Item) {
            if (((FullMultinameAVM2Item) propertyName).name != null) {
                return obStr + propertyName.toString(highlight, Helper.toList(constants, localRegNames, fullyQualifiedNames));
            } else {
                return obStr + "." + propertyName.toString(highlight, Helper.toList(constants, localRegNames, fullyQualifiedNames));
            }
        } else {
            return obStr + "[" + propertyName.toString(highlight, Helper.toList(constants, localRegNames, fullyQualifiedNames)) + "]";
        }
    }

    public static String localRegName(HashMap<Integer, String> localRegNames, int reg) {
        if (localRegNames.containsKey(reg)) {
            return localRegNames.get(reg);
        } else {
            if (reg == 0) {
                return "this";
            }
            return "_loc" + reg + "_";
        }
    }

    @Override
    public boolean hasReturnValue() {
        throw new UnsupportedOperationException(); //Not supported in AVM2 yet
    }
}

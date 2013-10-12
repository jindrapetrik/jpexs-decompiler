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
import com.jpexs.decompiler.flash.helpers.HilightedTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import static com.jpexs.decompiler.graph.GraphTargetItem.PRECEDENCE_PRIMARY;
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
    public HilightedTextWriter toString(HilightedTextWriter writer, List<Object> localData) {
        return toString(writer, (ConstantPool) localData.get(0), (HashMap<Integer, String>) localData.get(1), (List<String>) localData.get(2));
    }

    public abstract HilightedTextWriter toString(HilightedTextWriter writer, ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames);

    public String toStringNoH(ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        HilightedTextWriter writer = new HilightedTextWriter(false);
        toString(writer, constants, localRegNames, fullyQualifiedNames);
        return writer.toString();
    }

    public String toStringSemicoloned(HilightedTextWriter writer, ConstantPool constants, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        return toString(writer, constants, localRegNames, fullyQualifiedNames) + (needsSemicolon() ? ";" : "");
    }

    @Override
    public boolean needsSemicolon() {
        return true;
    }

    protected HilightedTextWriter formatProperty(HilightedTextWriter writer, ConstantPool constants, GraphTargetItem object, GraphTargetItem propertyName, HashMap<Integer, String> localRegNames, List<String> fullyQualifiedNames) {
        boolean empty = false;
        if (object instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) object).computedValue != null) {
                if (((LocalRegAVM2Item) object).computedValue.getThroughNotCompilable() instanceof FindPropertyAVM2Item) {
                    empty = true;
                }
            }
        }

        if (!empty) {
            if (object.precedence > PRECEDENCE_PRIMARY) {
                hilight("(", writer);
                object.toString(writer, Helper.toList(constants, localRegNames, fullyQualifiedNames));
                hilight(")", writer);
                empty = false;
            } else {
                int writerLength = writer.getLength();
                object.toString(writer, Helper.toList(constants, localRegNames, fullyQualifiedNames));
                if (writerLength == writer.getLength()) {
                    empty = true;
                }
            }
        }

        if (empty) {
            return propertyName.toString(writer, Helper.toList(constants, localRegNames, fullyQualifiedNames));
        }
        if (propertyName instanceof FullMultinameAVM2Item) {
            if (((FullMultinameAVM2Item) propertyName).name != null) {
                return propertyName.toString(writer, Helper.toList(constants, localRegNames, fullyQualifiedNames));
            } else {
                hilight(".", writer);
                return propertyName.toString(writer, Helper.toList(constants, localRegNames, fullyQualifiedNames));
            }
        } else {
            hilight("[", writer);
            propertyName.toString(writer, Helper.toList(constants, localRegNames, fullyQualifiedNames));
            return hilight("]", writer);
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

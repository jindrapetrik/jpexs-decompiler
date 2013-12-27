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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;

public abstract class AVM2Item extends GraphTargetItem {

    public AVM2Instruction instruction;
    public boolean hidden = false;

    public AVM2Item(GraphSourceItem instruction, int precedence) {
        super(instruction, precedence);
    }

    @Override
    public boolean needsSemicolon() {
        return true;
    }

    protected GraphTextWriter formatProperty(GraphTextWriter writer, GraphTargetItem object, GraphTargetItem propertyName, LocalData localData) throws InterruptedException {
        boolean empty = false;
        if (object instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) object).computedValue != null) {
                if (((LocalRegAVM2Item) object).computedValue.getThroughNotCompilable() instanceof FindPropertyAVM2Item) {
                    empty = true;
                }
            }
        }

        if (!empty) {
            if (object.getPrecedence() > PRECEDENCE_PRIMARY) {
                writer.append("(");
                object.toString(writer, localData);
                writer.append(")");
                empty = false;
            } else {
                int writerLength = writer.getLength();
                object.toString(writer, localData);
                if (writerLength == writer.getLength()) {
                    empty = true;
                }
            }
        }

        if (empty) {
            return propertyName.toString(writer, localData);
        }
        if (propertyName instanceof FullMultinameAVM2Item) {
            if (((FullMultinameAVM2Item) propertyName).name != null) {
                return propertyName.toString(writer, localData);
            } else {
                writer.append(".");
                return propertyName.toString(writer, localData);
            }
        } else {
            writer.append("[");
            propertyName.toString(writer, localData);
            return writer.append("]");
        }
    }

    public static String localRegName(HashMap<Integer, String> localRegNames, int reg) {
        if (localRegNames.containsKey(reg)) {
            return localRegNames.get(reg);
        } else {
            if (reg == 0) {
                return "this";
            }
            return String.format(Configuration.registerNameFormat.get(), reg);
        }
    }

    @Override
    public boolean hasReturnValue() {
        throw new UnsupportedOperationException(); //Not supported in AVM2 yet
    }
}

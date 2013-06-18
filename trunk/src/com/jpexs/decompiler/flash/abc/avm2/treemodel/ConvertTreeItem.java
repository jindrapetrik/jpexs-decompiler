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
    public double toNumber() {
        return toBoolean() ? 1 : 0;
    }

    @Override
    public boolean toBoolean() {
        if (type.contains("Boolean")) {
            if (value instanceof UndefinedTreeItem) {
                return false;
            }
            if (value instanceof NullTreeItem) {
                return false;
            }
            if (value instanceof BooleanTreeItem) {
                return ((BooleanTreeItem) value).value;
            }
            if (value instanceof IntegerValueTreeItem) {
                IntegerValueTreeItem iv = (IntegerValueTreeItem) value;
                return iv.value != 0;
            }
            if (value instanceof FloatValueTreeItem) {
                FloatValueTreeItem fv = (FloatValueTreeItem) value;
                return !(fv.value == 0 || fv.value.isNaN());
            }
            if (value instanceof StringTreeItem) {
                StringTreeItem sv = (StringTreeItem) value;
                return !sv.value.equals("");
            }

            if (value instanceof ThisTreeItem) {
                return true;
            }
            if (value instanceof ClassTreeItem) {
                return true;
            }
            //object
            return false;
        }
        return false;
    }

    @Override
    public boolean isCompileTime() {
        if (type.contains("Boolean")) {
            if (value instanceof UndefinedTreeItem) {
                return true;
            }
            if (value instanceof NullTreeItem) {
                return true;
            }
            if (value instanceof BooleanTreeItem) {
                return true;
            }
            if (value instanceof IntegerValueTreeItem) {
                return true;
            }
            if (value instanceof FloatValueTreeItem) {
                return true;
            }
            if (value instanceof StringTreeItem) {
                return true;
            }
            if (value instanceof ThisTreeItem) {
                return true;
            }
            if (value instanceof ClassTreeItem) {
                return true;
            }
            //object
            return false;
        }
        return false;
    }
}

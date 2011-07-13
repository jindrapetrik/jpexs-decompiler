/*
 *  Copyright (C) 2010-2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.avm2.treemodel;

import com.jpexs.asdec.abc.avm2.ConstantPool;
import com.jpexs.asdec.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.asdec.helpers.Highlighting;


public abstract class TreeItem {

    public static final int PRECEDENCE_PRIMARY = 0;
    public static final int PRECEDENCE_POSTFIX = 1;
    public static final int PRECEDENCE_UNARY = 2;
    public static final int PRECEDENCE_MULTIPLICATIVE = 3;
    public static final int PRECEDENCE_ADDITIVE = 4;
    public static final int PRECEDENCE_BITWISESHIFT = 5;
    public static final int PRECEDENCE_RELATIONAL = 6;
    public static final int PRECEDENCE_EQUALITY = 7;
    public static final int PRECEDENCE_BITWISEAND = 8;
    public static final int PRECEDENCE_BITWISEXOR = 9;
    public static final int PRECEDENCE_BITWISEOR = 10;
    public static final int PRECEDENCE_LOGICALAND = 11;
    public static final int PRECEDENCE_LOGICALOR = 12;
    public static final int PRECEDENCE_CONDITIONAL = 13;
    public static final int PRECEDENCE_ASSIGMENT = 14;
    public static final int PRECEDENCE_COMMA = 15;
    public static final int NOPRECEDENCE = 16;


    public int precedence = NOPRECEDENCE;
    public AVM2Instruction instruction;

    public TreeItem(AVM2Instruction instruction, int precedence) {
        this.instruction = instruction;
        this.precedence = precedence;
    }


    public abstract String toString(ConstantPool constants);


    protected String hilight(String str) {
        if (instruction == null)
            return str;
        return Highlighting.hilighOffset(str, instruction.offset);
    }

    public boolean isFalse() {
        return false;
    }

    public boolean isTrue() {
        return false;
    }

    protected String formatProperty(ConstantPool constants, TreeItem object, TreeItem propertyName) {
        String obStr = object.toString(constants);
        if (object.precedence > PRECEDENCE_PRIMARY) {
            obStr = "(" + obStr + ")";
        }
        if (object instanceof LocalRegTreeItem) {
            if (((LocalRegTreeItem) object).computedValue instanceof FindPropertyTreeItem)
                obStr = "";
        }
        if (propertyName instanceof FullMultinameTreeItem) {

            if (((FullMultinameTreeItem) propertyName).isRuntime()) {
                return obStr + "[" + propertyName.toString(constants) + "]";
            } else {
                if (!obStr.equals("")) obStr += ".";
                return obStr + ((FullMultinameTreeItem) propertyName).toString(constants);
            }
        } else {
            return obStr + "[" + propertyName.toString(constants) + "]";
        }
    }
    public TreeItem getNotCoerced(){
       return this;
    }
}

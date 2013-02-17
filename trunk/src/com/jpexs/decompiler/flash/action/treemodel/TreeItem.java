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
package com.jpexs.decompiler.flash.action.treemodel;

import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.IgnoredPair;
import com.jpexs.decompiler.flash.helpers.Highlighting;
import java.util.ArrayList;
import java.util.List;

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
   public Action instruction;
   public int instructionPos = 0;
   public List<IgnoredPair> moreInstructions = new ArrayList<IgnoredPair>();

   public TreeItem(Action instruction, int precedence) {
      this.instruction = instruction;
      this.precedence = precedence;
   }

   public abstract String toString(ConstantPool constants);

   public String toStringNoQuotes(ConstantPool constants) {
      return toString(constants);
   }

   @Override
   public String toString() {
      return toString(null);
   }

   protected String hilight(String str) {
      if (instruction == null) {
         return str;
      }
      return Highlighting.hilighOffset(str, instruction.getAddress());
   }

   public boolean isFalse() {
      return false;
   }

   public boolean isTrue() {
      return false;
   }

   protected boolean isEmptyString(TreeItem target) {
      if (target instanceof DirectValueTreeItem) {
         if (((DirectValueTreeItem) target).value instanceof String) {

            if (((DirectValueTreeItem) target).value.equals("")) {
               return true;
            }
         }
      }
      return false;
   }

   protected String stripQuotes(TreeItem target) {
      if (target instanceof DirectValueTreeItem) {
         if (((DirectValueTreeItem) target).value instanceof String) {
            return (String) ((DirectValueTreeItem) target).hilight((String) ((DirectValueTreeItem) target).value);
         }
      }
      if (target == null) {
         return "";
      } else {
         return target.toString();
      }
   }

   public boolean isCompileTime() {
      return false;
   }

   public double toNumber() {
      return 0;
   }

   public boolean toBoolean() {
      return Double.compare(toNumber(), 0.0) != 0;
   }

   public List<com.jpexs.decompiler.flash.action.IgnoredPair> getNeededActions() {
      List<com.jpexs.decompiler.flash.action.IgnoredPair> ret = new ArrayList<com.jpexs.decompiler.flash.action.IgnoredPair>();
      if (instruction != null) {
         ret.add(new IgnoredPair(instruction, instructionPos));
      }
      ret.addAll(moreInstructions);
      return ret;
   }
}

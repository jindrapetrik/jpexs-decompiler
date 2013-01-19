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


package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;
import com.jpexs.asdec.action.swf4.ConstantIndex;
import com.jpexs.asdec.helpers.Helper;
import java.util.List;

public class DirectValueTreeItem extends TreeItem {

   public Object value;
   public List<String> constants;

   public DirectValueTreeItem(Action instruction, Object value, ConstantPool constants) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.constants = constants.constants;
      this.value = value;
   }

   public String toStringNoQuotes(ConstantPool constants) {
      if (value instanceof Double) {
         if (Double.compare((double) (Double) value, 0) == 0) {
            return "0";
         }
      }
      if (value instanceof Float) {
         if (Float.compare((float) (Float) value, 0) == 0) {
            return "0";
         }
      }
      if (value instanceof String) {
         return Helper.escapeString((String) value);
      }
      if (value instanceof ConstantIndex) {
         return (this.constants.get(((ConstantIndex) value).index));
      }
      return value.toString();
   }

   @Override
   public String toString(ConstantPool constants) {
      if (value instanceof Double) {
         if (Double.compare((double) (Double) value, 0) == 0) {
            return "0";
         }
      }
      if (value instanceof Float) {
         if (Float.compare((float) (Float) value, 0) == 0) {
            return "0";
         }
      }
      if (value instanceof String) {
         return "\"" + Helper.escapeString((String) value) + "\"";
      }
      if (value instanceof ConstantIndex) {
         return "\"" + Helper.escapeString(this.constants.get(((ConstantIndex) value).index)) + "\"";
      }
      return value.toString();
   }
}

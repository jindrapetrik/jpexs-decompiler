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
      if (constants != null) {
         this.constants = constants.constants;
      }
      this.value = value;
   }

   @Override
   public double toNumber() {
      if(value instanceof Double){
         return (Double)value;
      }
      if(value instanceof Float){
         return (Float)value;
      }
      if(value instanceof Long){
         return (Long)value;
      }
      return super.toNumber();
   }

   @Override
   public boolean toBoolean() {
      boolean ret=(value instanceof Boolean)?(Boolean)value:false;
      return ret;
   }
   
   
   

   @Override
   public String toStringNoQuotes(ConstantPool constants) {
      if (value instanceof Double) {
         if (Double.compare((double) (Double) value, 0) == 0) {
            return hilight("0");
         }
      }
      if (value instanceof Float) {
         if (Float.compare((float) (Float) value, 0) == 0) {
            return hilight("0");
         }
      }
      if (value instanceof String) {
         return hilight((String) value);
      }
      if (value instanceof ConstantIndex) {
         return hilight(this.constants.get(((ConstantIndex) value).index));
      }
      return hilight(value.toString());
   }

   @Override
   public String toString(ConstantPool constants) {
      if (value instanceof Double) {
         if (Double.compare((double) (Double) value, 0) == 0) {
            return hilight("0");
         }
      }
      if (value instanceof Float) {
         if (Float.compare((float) (Float) value, 0) == 0) {
            return hilight("0");
         }
      }
      if (value instanceof String) {
         return hilight("\"" + Helper.escapeString((String) value) + "\"");
      }
      if (value instanceof ConstantIndex) {
         return hilight("\"" + Helper.escapeString(this.constants.get(((ConstantIndex) value).index)) + "\"");
      }
      return hilight(value.toString());
   }
   
   @Override
   public boolean isCompileTime(){
      return (value instanceof Double)||(value instanceof Float)||(value instanceof Boolean)||(value instanceof Long);
   }
}

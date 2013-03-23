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

import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf4.Null;
import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import com.jpexs.decompiler.flash.helpers.Helper;
import java.util.List;

public class DirectValueTreeItem extends TreeItem {

   public Object value;
   public List<String> constants;
   public GraphTargetItem computedRegValue;

   public DirectValueTreeItem(GraphSourceItem instruction, int instructionPos, Object value, List<String> constants) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.constants = constants;
      this.value = value;
      this.pos = instructionPos;
   }

   @Override
   public boolean isVariableComputed() {
      if (computedRegValue != null) {
         return true;
      }
      return false;
   }

   @Override
   public double toNumber() {
      if (computedRegValue != null) {
         return computedRegValue.toNumber();
      }
      if (value instanceof Double) {
         return (Double) value;
      }
      if (value instanceof Float) {
         return (Float) value;
      }
      if (value instanceof Long) {
         return (Long) value;
      }
      if (value instanceof Boolean) {
         return ((Boolean) value) ? 1 : 0;
      }
      return super.toNumber();
   }

   @Override
   public boolean toBoolean() {
      if (computedRegValue != null) {
         return computedRegValue.toBoolean();
      }
      if (value instanceof Boolean) {
         return (Boolean) value;
      }
      if (value instanceof Double) {
         return Double.compare((Double) value, 0.0) != 0;
      }
      if (value instanceof Float) {
         return Float.compare((Float) value, 0.0f) != 0;
      }
      if (value instanceof Long) {
         return ((Long) value) != 0;
      }
      return false;
   }

   @Override
   public String toStringNoQuotes(List localData) {
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

   public String toStringNoH(ConstantPool constants) {
      if (value instanceof Double) {
         if (Double.compare((double) (Double) value, 0) == 0) {
            return ("0");
         }
      }
      if (value instanceof Float) {
         if (Float.compare((float) (Float) value, 0) == 0) {
            return ("0");
         }
      }
      if (value instanceof String) {
         return (String) value;
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
   public boolean isCompileTime() {
      return (value instanceof Double) || (value instanceof Float) || (value instanceof Boolean) || (value instanceof Long) || (value instanceof Null) || (computedRegValue != null && computedRegValue.isCompileTime());
   }
}

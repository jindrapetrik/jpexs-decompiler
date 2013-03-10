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
package com.jpexs.decompiler.flash.graph;

import java.util.List;

public abstract class BinaryOpItem extends GraphTargetItem {

   public GraphTargetItem leftSide;
   public GraphTargetItem rightSide;
   protected String operator = "";

   public BinaryOpItem(GraphSourceItem instruction, int precedence, GraphTargetItem leftSide, GraphTargetItem rightSide, String operator) {
      super(instruction, precedence);
      this.leftSide = leftSide;
      this.rightSide = rightSide;
      this.operator = operator;
   }

   @Override
   public String toString(List localData) {
      String ret = "";
      if (leftSide.getPrecedence() > precedence) {
         ret += "(" + leftSide.toString(localData) + ")";
      } else {
         ret += leftSide.toString(localData);
      }
      ret += hilight(operator);
      if (rightSide.getPrecedence() > precedence) {
         ret += "(" + rightSide.toString(localData) + ")";
      } else {
         ret += rightSide.toString(localData);
      }
      return ret;
   }

   @Override
   public List<GraphSourceItemPos> getNeededSources() {
      List<GraphSourceItemPos> ret = super.getNeededSources();
      ret.addAll(leftSide.getNeededSources());
      ret.addAll(rightSide.getNeededSources());
      return ret;
   }

   @Override
   public boolean isCompileTime() {
      return leftSide.isCompileTime() && rightSide.isCompileTime();
   }

   @Override
   public boolean isVariableComputed() {
      return leftSide.isVariableComputed() || rightSide.isVariableComputed();
   }
}

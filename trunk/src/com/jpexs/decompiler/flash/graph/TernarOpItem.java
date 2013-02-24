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

public class TernarOpItem extends GraphTargetItem {

   public GraphTargetItem expression;
   public GraphTargetItem onTrue;
   public GraphTargetItem onFalse;

   public TernarOpItem(GraphSourceItem src, GraphTargetItem expression, GraphTargetItem onTrue, GraphTargetItem onFalse) {
      super(src, PRECEDENCE_CONDITIONAL);
      this.expression = expression;
      this.onTrue = onTrue;
      this.onFalse = onFalse;
   }

   @Override
   public String toString(List localData) {
      return expression.toString(localData) + hilight("?") + onTrue.toString(localData) + hilight(":") + onFalse.toString(localData);
   }
}

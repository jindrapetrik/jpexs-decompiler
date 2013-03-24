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

import com.jpexs.decompiler.flash.graph.GraphSourceItem;
import com.jpexs.decompiler.flash.graph.GraphTargetItem;
import java.util.List;

public class GetURL2TreeItem extends TreeItem {

   public GraphTargetItem urlString;
   public GraphTargetItem targetString;
   public int method;
   public boolean loadTargetFlag;
   public boolean loadVariablesFlag;

   @Override
   public String toString(ConstantPool constants) {
      String methodStr = "";
      if (method == 1) {
         methodStr = ",\"GET\"";
      }
      if (method == 2) {
         methodStr = ",\"POST\"";
      }
      String prefix = "getUrl";
      if (loadVariablesFlag) {
         prefix = "loadVariables";
      }
      if (loadTargetFlag && (!loadVariablesFlag)) {
         prefix = "loadMovie";
      }

      return hilight(prefix + "(") + urlString.toString(constants) + hilight(",") + targetString.toString(constants) + hilight(methodStr + ")");
   }

   public GetURL2TreeItem(GraphSourceItem instruction, GraphTargetItem urlString, GraphTargetItem targetString, int method, boolean loadTargetFlag, boolean loadVariablesFlag) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.urlString = urlString;
      this.targetString = targetString;
      this.method = method;
      this.loadTargetFlag = loadTargetFlag;
      this.loadVariablesFlag = loadVariablesFlag;
   }

   @Override
   public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
      List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
      ret.addAll(urlString.getNeededSources());
      ret.addAll(targetString.getNeededSources());
      return ret;
   }
}

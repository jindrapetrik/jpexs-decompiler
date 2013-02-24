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

public class CloneSpriteTreeItem extends TreeItem {

   public GraphTargetItem source;
   public GraphTargetItem target;
   public GraphTargetItem depth;

   public CloneSpriteTreeItem(GraphSourceItem instruction, GraphTargetItem source, GraphTargetItem target, GraphTargetItem depth) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.source = source;
      this.target = target;
      this.depth = depth;
   }

   @Override
   public String toString(ConstantPool constants) {
      return hilight("duplicateMovieClip(") + target.toString(constants) + hilight(",") + source.toString(constants) + hilight(",") + depth.toString(constants) + hilight(")");
   }

   @Override
   public List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> getNeededSources() {
      List<com.jpexs.decompiler.flash.graph.GraphSourceItemPos> ret = super.getNeededSources();
      ret.addAll(source.getNeededSources());
      ret.addAll(target.getNeededSources());
      ret.addAll(depth.getNeededSources());
      return ret;
   }
}

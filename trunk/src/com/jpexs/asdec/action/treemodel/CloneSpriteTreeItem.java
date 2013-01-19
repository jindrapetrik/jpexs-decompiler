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

public class CloneSpriteTreeItem extends TreeItem {

   public TreeItem source;
   public TreeItem target;
   public TreeItem depth;

   public CloneSpriteTreeItem(Action instruction, TreeItem source, TreeItem target, TreeItem depth) {
      super(instruction, PRECEDENCE_PRIMARY);
      this.source = source;
      this.target = target;
      this.depth = depth;
   }

   @Override
   public String toString(ConstantPool constants) {
      return "duplicateMovieClip(" + target.toString(constants) + "," + source.toString(constants) + "," + depth.toString(constants) + ");";
   }
}

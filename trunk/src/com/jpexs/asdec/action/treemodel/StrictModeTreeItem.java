/*
 * Copyright (C) 2011 JPEXS
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.jpexs.asdec.action.treemodel;

import com.jpexs.asdec.action.Action;

public class StrictModeTreeItem extends TreeItem {
   public int mode;
   public StrictModeTreeItem(Action instruction,int mode){
      super(instruction,PRECEDENCE_PRIMARY);
      this.mode=mode;
   }
   
   @Override
   public String toString(ConstantPool constants) {
      return "StrictMode("+mode+");"; //I still don't know how AS source of Strict Mode instruction looks like, assuming this...
   }
   
}

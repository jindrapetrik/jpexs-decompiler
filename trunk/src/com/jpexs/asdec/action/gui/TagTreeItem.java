/*
 *  Copyright (C) 2010-2011 JPEXS
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
package com.jpexs.asdec.action.gui;

import java.util.ArrayList;
import java.util.List;

public class TagTreeItem {

   public List<TagTreeItem> subItems;
   public Object tag;

   public TagTreeItem(Object tag) {
      this.tag = tag;
      this.subItems = new ArrayList<TagTreeItem>();
   }

   @Override
   public String toString() {
      return tag.toString();
   }
}

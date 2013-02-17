/*
 *  Copyright (C) 2011-2013 Paolo Cancedda
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
package com.jpexs.decompiler.flash.abc.gui;

import java.util.StringTokenizer;

public class Tree {

   private final TreeElement ROOT = new TreeElement("", "", null, null);

   public void add(String name, String path, Object item) {
      StringTokenizer st = new StringTokenizer(path, ".");
      TreeElement parent = ROOT;
      while (st.hasMoreTokens()) {
         String pathElement = st.nextToken();
         parent = parent.getBranch(pathElement);
      }
      parent.addLeaf(name, item);
   }

   public TreeElement getRoot() {
      return ROOT;
   }

   public void visit(TreeVisitor visitor) {
      ROOT.visitLeafs(visitor);
      ROOT.visitBranches(visitor);
   }

   public TreeElement get(String fullPath) {
      if ("".equals(fullPath)) {
         return ROOT;
      }
      return ROOT.getByPath(fullPath);
   }
}

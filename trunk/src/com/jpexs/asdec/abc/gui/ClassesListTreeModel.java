/*
 *  Copyright (C) 2010-2011 JPEXS, Paolo Cancedda
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
package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.abc.ABC;
import java.util.HashMap;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

class ClassIndexVisitor implements TreeVisitor {

   private TreeElement found = null;
   private Object item;

   public ClassIndexVisitor(Object item) {
      this.item = item;
   }

   public void onBranch(TreeElement branch) {
      if (branch.getItem() == item) {
         found = branch;
      }
   }

   public void onLeaf(TreeElement leaf) {
      if (leaf.getItem() == item) {
         found = leaf;
      }
   }

   public TreeElement getFound() {
      return found;
   }
}

public class ClassesListTreeModel implements TreeModel {

   private ABC abc;
   private Tree classTree = new Tree();

   public ClassesListTreeModel(ABC abc) {
      this.abc = abc;
      for (int i = 0; i < abc.instance_info.length; i++) {
         String packageName = abc.instance_info[i].getName(abc.constants).getNamespace(abc.constants).getName(abc.constants);
         String className = abc.instance_info[i].getName(abc.constants).getName(abc.constants);
         //String full = packageName + "." + className;
         classTree.add(className, packageName, new TreeLeafClass(i));
      }
      HashMap<String, String> ns = abc.namespacesToString();
      for (String n : ns.keySet()) {
         String nsName = n.substring(n.lastIndexOf(".") + 1);
         String packageName = n.substring(0, n.lastIndexOf("."));
         classTree.add(nsName, packageName, new TreeLeafString(ns.get(n)));
      }
   }

   public Object getItemByPath(String fullPath) {
      TreeElement elem = classTree.get(fullPath);
      if (elem == null) {
         return -1;
      }
      return elem.getItem();
   }

   public TreeElement getElementByClassIndex(int classIndex) {
      ClassIndexVisitor civ = new ClassIndexVisitor(classIndex);
      classTree.visit(civ);
      return civ.getFound();
   }

   public Object getRoot() {
      return classTree.getRoot();
   }

   public Object getChild(Object parent, int index) {
      TreeElement pte = (TreeElement) parent;
      TreeElement te = pte.getChild(index);
      return te;
   }

   public int getChildCount(Object parent) {
      TreeElement te = (TreeElement) parent;
      return te.getChildCount();
   }

   public boolean isLeaf(Object node) {
      TreeElement te = (TreeElement) node;
      return te.isLeaf();
   }

   public void valueForPathChanged(TreePath path, Object newValue) {
   }

   public int getIndexOfChild(Object parent, Object child) {
      TreeElement te1 = (TreeElement) parent;
      TreeElement te2 = (TreeElement) child;
      return te1.getIndexOfChild(te2);
   }

   public void addTreeModelListener(TreeModelListener l) {
   }

   public void removeTreeModelListener(TreeModelListener l) {
   }
}

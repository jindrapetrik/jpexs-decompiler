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

import com.jpexs.asdec.abc.types.traits.Trait;
import com.jpexs.asdec.abc.types.traits.TraitClass;
import com.jpexs.asdec.tags.DoABCTag;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

class ClassIndexVisitor implements TreeVisitor {

   private TreeElement found = null;
   private int classIndex = 0;

   public ClassIndexVisitor(int classIndex) {
      this.classIndex = classIndex;
   }

   public void onBranch(TreeElement branch) {
      Object o = branch.getItem();
      if (o == null) {
         return;
      }
      TreeLeafScript sc = (TreeLeafScript) o;
      for (Trait t : sc.abc.script_info[sc.scriptIndex].traits.traits) {
         if (t instanceof TraitClass) {
            if (((TraitClass) t).class_info == classIndex) {
               found = branch;
               return;
            }
         }
      }
   }

   public void onLeaf(TreeElement leaf) {
      Object o = leaf.getItem();
      if (o == null) {
         return;
      }
      TreeLeafScript sc = (TreeLeafScript) o;
      for (Trait t : sc.abc.script_info[sc.scriptIndex].traits.traits) {
         if (t instanceof TraitClass) {
            if (((TraitClass) t).class_info == classIndex) {
               found = leaf;
               return;
            }
         }
      }
   }

   public TreeElement getFound() {
      return found;
   }
}

public class ClassesListTreeModel implements TreeModel {

   private Tree classTree = new Tree();
   private List<DoABCTag> list;

   public ClassesListTreeModel(List<DoABCTag> list) {
      this.list = list;
      for (DoABCTag tag : list) {
         for (int i = 0; i < tag.abc.script_info.length; i++) {
            String path = tag.abc.script_info[i].getPath(tag.abc);
            String nsName = path.substring(path.lastIndexOf(".") + 1);
            String packageName = path.substring(0, path.lastIndexOf("."));
            classTree.add(nsName, packageName, new TreeLeafScript(tag.abc, i));
         }
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

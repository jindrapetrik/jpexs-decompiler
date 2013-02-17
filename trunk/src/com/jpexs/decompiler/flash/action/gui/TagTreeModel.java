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
package com.jpexs.decompiler.flash.action.gui;

import com.jpexs.decompiler.flash.action.TagNode;
import com.jpexs.decompiler.flash.tags.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TagTreeModel implements TreeModel {

   private String root = "";
   private List<TagNode> list = new ArrayList<TagNode>();

   public TagTreeModel(List<Tag> list) {
      List<Object> list2 = new ArrayList<Object>();
      list2.addAll(list);
      this.list = TagNode.createTagList(list2);
   }

   public List<TagNode> getNodeList() {
      return list;
   }

   public Object getRoot() {
      return root;
   }

   public Object getChild(Object parent, int index) {
      if (parent == root) {
         return list.get(index);
      } else {
         return ((TagNode) parent).subItems.get(index);
      }
   }

   public int getChildCount(Object parent) {
      if (parent == root) {
         return list.size();
      } else {
         return ((TagNode) parent).subItems.size();
      }
   }

   public boolean isLeaf(Object node) {
      return (getChildCount(node) == 0);
   }

   public void valueForPathChanged(TreePath path, Object newValue) {
   }

   public int getIndexOfChild(Object parent, Object child) {
      if (parent == root) {
         for (int t = 0; t < list.size(); t++) {
            if (list.get(t) == child) {
               return t;
            }
         }
         return -1;
      } else {
         List<TagNode> subTags = ((TagNode) parent).subItems;
         for (int t = 0; t < subTags.size(); t++) {
            if (subTags.get(t) == child) {
               return t;
            }
         }
         return -1;
      }
   }

   public void addTreeModelListener(TreeModelListener l) {
   }

   public void removeTreeModelListener(TreeModelListener l) {
   }
}

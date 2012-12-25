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

import com.jpexs.asdec.tags.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class TagTreeModel implements TreeModel {

   private String root = "";
   private List<TagTreeItem> list = new ArrayList<TagTreeItem>();

   public TagTreeModel(List<Tag> list) {
      List<Object> list2 = new ArrayList<Object>();
      list2.addAll(list);
      this.list = processTagList(list2);
   }

   public List<TagTreeItem> processTagList(List<Object> list) {
      List<TagTreeItem> ret = new ArrayList<TagTreeItem>();
      int frame = 1;
      List<TagTreeItem> frames = new ArrayList<TagTreeItem>();

      List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();
      for (Object t : list) {
         if (t instanceof ExportAssetsTag) {
            exportAssetsTags.add((ExportAssetsTag) t);
         }
         if (t instanceof ShowFrameTag) {
            TagTreeItem tti = new TagTreeItem("frame" + frame);

            for (int r = ret.size() - 1; r >= 0; r--) {
               if (!(ret.get(r).tag instanceof DefineSpriteTag)) {
                  if (!(ret.get(r).tag instanceof DefineButtonTag)) {
                     if (!(ret.get(r).tag instanceof DefineButton2Tag)) {
                        if (!(ret.get(r).tag instanceof DoInitActionTag)) {
                           tti.subItems.add(ret.get(r));
                           ret.remove(r);
                        }
                     }
                  }
               }
            }
            frame++;
            frames.add(tti);
         } else if (t instanceof ASMSource) {
            TagTreeItem tti = new TagTreeItem(t);
            ret.add(tti);
         } else if (t instanceof Container) {
            if (((Container) t).getItemCount() > 0) {

               TagTreeItem tti = new TagTreeItem(t);
               List<Object> subItems = ((Container) t).getSubItems();

               tti.subItems = processTagList(subItems);
               ret.add(tti);
            }
         }

      }
      ret.addAll(frames);
      for (int i = ret.size() - 1; i >= 0; i--) {
         if (ret.get(i).tag instanceof DefineSpriteTag) {
            ((DefineSpriteTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof DefineButtonTag) {
            ((DefineButtonTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof DefineButton2Tag) {
            ((DefineButton2Tag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof DoInitActionTag) {
            ((DoInitActionTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof ASMSource) {
            ASMSource ass = (ASMSource) ret.get(i).tag;
            if (ass.containsSource()) {
               continue;
            }
         }
         if (ret.get(i).subItems.size() == 0) {
            ret.remove(i);
         }
      }
      return ret;
   }

   public Object getRoot() {
      return root;
   }

   public Object getChild(Object parent, int index) {
      if (parent == root) {
         return list.get(index);
      } else {
         return ((TagTreeItem) parent).subItems.get(index);
      }
   }

   public int getChildCount(Object parent) {
      if (parent == root) {
         return list.size();
      } else {
         return ((TagTreeItem) parent).subItems.size();
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
         List<TagTreeItem> subTags = ((TagTreeItem) parent).subItems;
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

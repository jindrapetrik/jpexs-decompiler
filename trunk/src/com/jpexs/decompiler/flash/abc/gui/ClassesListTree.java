/*
 *  Copyright (C) 2010-2013 JPEXS, Paolo Cancedda
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

import com.jpexs.decompiler.flash.Main;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class ClassesListTree extends JTree implements TreeSelectionListener {

   private List<DoABCTag> abcList;
   private HashMap<String, TreeLeafScript> treeList;
   private ABCPanel abcPanel;

   public void selectClass(int classIndex) {
      ClassesListTreeModel model = (ClassesListTreeModel) getModel();
      TreeElement selectedElement = model.getElementByClassIndex(classIndex);
      TreePath treePath = selectedElement.getTreePath();
      setSelectionPath(treePath);
      scrollPathToVisible(treePath);
   }

   public ClassesListTree(List<DoABCTag> list, ABCPanel abcPanel) {
      this.abcList = list;
      this.treeList = getTreeList(list);
      this.abcPanel = abcPanel;
      setModel(new ClassesListTreeModel(this.treeList));
      addTreeSelectionListener(this);
      DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
      ClassLoader cldr = this.getClass().getClassLoader();
      java.net.URL imageURL = cldr.getResource("com/jpexs/decompiler/flash/gui/graphics/as16.png");
      ImageIcon leafIcon = new ImageIcon(imageURL);
      treeRenderer.setLeafIcon(leafIcon);
      setCellRenderer(treeRenderer);
   }

   public List<TreeLeafScript> getSelectedScripts() {
      TreeSelectionModel tsm = getSelectionModel();
      final List<TreeLeafScript> selectedScripts = new ArrayList<TreeLeafScript>();
      TreePath tps[] = tsm.getSelectionPaths();
      if (tps == null) {
         return selectedScripts;
      }
      for (TreePath tp : tps) {
         TreeElement te = (TreeElement) tp.getLastPathComponent();
         if (te.isLeaf()) {
            Object item = te.getItem();
            if (item instanceof TreeLeafScript) {
               selectedScripts.add((TreeLeafScript) item);
            }
         } else {
            TreeVisitor tvi = new TreeVisitor() {
               @Override
               public void onBranch(TreeElement branch) {
               }

               @Override
               public void onLeaf(TreeElement leaf) {
                  Object item = leaf.getItem();
                  if (item instanceof TreeLeafScript) {
                     selectedScripts.add((TreeLeafScript) item);
                  }
               }
            };
            te.visitBranches(tvi);
            te.visitLeafs(tvi);
         }
      }
      return selectedScripts;
   }

   public HashMap<String, TreeLeafScript> getTreeList(List<DoABCTag> list) {
      HashMap<String, TreeLeafScript> ret = new HashMap<String, TreeLeafScript>();
      for (DoABCTag tag : list) {
         for (int i = 0; i < tag.abc.script_info.length; i++) {
            String path = tag.abc.script_info[i].getPath(tag.abc);
            ret.put(path, new TreeLeafScript(tag.abc, i));
         }
      }
      return ret;
   }

   public void setDoABCTags(List<DoABCTag> list) {
      this.abcList = list;
      this.treeList = getTreeList(list);
      setModel(new ClassesListTreeModel(this.treeList));
   }

   public void applyFilter(String filter) {
      setModel(new ClassesListTreeModel(this.treeList, filter));
   }

   @Override
   public void valueChanged(TreeSelectionEvent e) {
      if (Main.isWorking()) {
         return;
      }
      final TreeElement tp = (TreeElement) getLastSelectedPathComponent();
      if (tp == null) {
         return;
      }
      Object item = tp.getItem();
      if (item instanceof TreeLeafScript) {
         final TreeLeafScript scriptLeaf = (TreeLeafScript) item;

         if (!Main.isWorking()) {
            Main.startWork("Decompiling...");
            (new Thread() {
               @Override
               public void run() {
                  int classIndex = -1;
                  for (Trait t : scriptLeaf.abc.script_info[scriptLeaf.scriptIndex].traits.traits) {
                     if (t instanceof TraitClass) {
                        classIndex = ((TraitClass) t).class_info;
                        break;
                     }
                  }
                  abcPanel.navigator.setABC(abcList, scriptLeaf.abc);
                  abcPanel.navigator.setClassIndex(classIndex);
                  abcPanel.setAbc(scriptLeaf.abc);
                  abcPanel.decompiledTextArea.setScript(scriptLeaf.abc.script_info[scriptLeaf.scriptIndex], scriptLeaf.abc, abcList);
                  abcPanel.decompiledTextArea.setClassIndex(classIndex);
                  abcPanel.decompiledTextArea.setNoTrait();
                  abcPanel.detailPanel.methodTraitPanel.methodCodePanel.setCode("");
                  Main.stopWork();
               }
            }).start();
         }

      }

   }
}

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

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.types.ScriptInfo;
import com.jpexs.asdec.abc.types.traits.Trait;
import com.jpexs.asdec.abc.types.traits.TraitClass;
import com.jpexs.asdec.tags.DoABCTag;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

public class ClassesListTree extends JTree implements TreeSelectionListener {

   private List<DoABCTag> abcList;
   public void selectClass(int classIndex) {
      ClassesListTreeModel model = (ClassesListTreeModel) getModel();
      TreeElement selectedElement = model.getElementByClassIndex(classIndex);
      TreePath treePath = selectedElement.getTreePath();
      setSelectionPath(treePath);
      scrollPathToVisible(treePath);
   }

   public ClassesListTree(List<DoABCTag> list) {
      this.abcList=list;
      setModel(new ClassesListTreeModel(list));
      addTreeSelectionListener(this);
      DefaultTreeCellRenderer treeRenderer = new DefaultTreeCellRenderer();
      ClassLoader cldr = this.getClass().getClassLoader();
      java.net.URL imageURL = cldr.getResource("com/jpexs/asdec/abc/gui/graphics/class.png");
      ImageIcon leafIcon = new ImageIcon(imageURL);
      treeRenderer.setLeafIcon(leafIcon);
      setCellRenderer(treeRenderer);
   }

   public void setDoABCTags(List<DoABCTag> list) {
      this.abcList=list;
      setModel(new ClassesListTreeModel(list));
   }

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
                  Main.abcMainFrame.navigator.setClassIndex(classIndex);
                  Main.abcMainFrame.navigator.setABC(abcList,scriptLeaf.abc);
                  Main.abcMainFrame.setAbc(scriptLeaf.abc);
                  Main.abcMainFrame.decompiledTextArea.setScript(scriptLeaf.abc.script_info[scriptLeaf.scriptIndex], scriptLeaf.abc,abcList);
                  Main.abcMainFrame.detailPanel.methodTraitPanel.methodCodePanel.sourceTextArea.setText("");
                  Main.stopWork();
               }
            }).start();
         }

      }

   }
}

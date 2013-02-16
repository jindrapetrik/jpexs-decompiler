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
package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.usages.InsideClassMultinameUsage;
import com.jpexs.asdec.abc.usages.MethodMultinameUsage;
import com.jpexs.asdec.abc.usages.MultinameUsage;
import com.jpexs.asdec.abc.usages.TraitMultinameUsage;
import com.jpexs.asdec.gui.View;
import com.jpexs.asdec.tags.DoABCTag;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author JPEXS
 */
public class UsageFrame extends JFrame implements ActionListener, MouseListener {

   private JButton gotoButton = new JButton("Go to");
   private JButton cancelButton = new JButton("Cancel");
   private JList usageList;
   private UsageListModel usageListModel;
   private ABC abc;
   private ABCPanel abcPanel;

   public UsageFrame(List<DoABCTag> abcTags, ABC abc, int multinameIndex, ABCPanel abcPanel) {
      this.abcPanel = abcPanel;
      List<MultinameUsage> usages = abc.findMultinameUsage(multinameIndex);
      this.abc = abc;
      usageListModel = new UsageListModel(abcTags, abc);
      for (MultinameUsage u : usages) {
         usageListModel.addElement(u);
      }
      usageList = new JList(usageListModel);
      gotoButton.setActionCommand("GOTO");
      gotoButton.addActionListener(this);
      cancelButton.setActionCommand("CANCEL");
      cancelButton.addActionListener(this);
      JPanel buttonsPanel = new JPanel();
      buttonsPanel.setLayout(new FlowLayout());
      buttonsPanel.add(gotoButton);
      buttonsPanel.add(cancelButton);

      usageList.addMouseListener(this);
      Container cont = getContentPane();
      cont.setLayout(new BorderLayout());
      cont.add(new JScrollPane(usageList), BorderLayout.CENTER);
      cont.add(buttonsPanel, BorderLayout.SOUTH);
      setSize(400, 300);
      setTitle("Usages:" + abc.constants.constant_multiname[multinameIndex].getNameWithNamespace(abc.constants));
      View.centerScreen(this);
      View.setWindowIcon(this);
   }

   private void gotoUsage() {
      if (usageList.getSelectedIndex() != -1) {
         MultinameUsage usage = usageListModel.getUsage(usageList.getSelectedIndex());
         if (usage instanceof InsideClassMultinameUsage) {
            InsideClassMultinameUsage icu = (InsideClassMultinameUsage) usage;
            abcPanel.classTree.selectClass(icu.classIndex);
            try {
               Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
            if (usage instanceof TraitMultinameUsage) {
               TraitMultinameUsage tmu = (TraitMultinameUsage) usage;
               int traitIndex;
               if (tmu.parentTraitIndex > -1) {
                  traitIndex = tmu.parentTraitIndex;
               } else {
                  traitIndex = tmu.traitIndex;
               }
               if (!tmu.isStatic) {
                  traitIndex += abc.class_info[tmu.classIndex].static_traits.traits.length;
               }
               if (tmu instanceof MethodMultinameUsage) {
                  MethodMultinameUsage mmu = (MethodMultinameUsage) usage;
                  if (mmu.isInitializer == true) {
                     traitIndex = abc.class_info[mmu.classIndex].static_traits.traits.length + abc.instance_info[mmu.classIndex].instance_traits.traits.length + (mmu.isStatic ? 1 : 0);
                  }
               }
               abcPanel.decompiledTextArea.gotoTrait(traitIndex);
            }
         }
      }
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("GOTO")) {
         gotoUsage();
         setVisible(false);
      }
      if (e.getActionCommand().equals("CANCEL")) {
         setVisible(false);
      }
   }

   public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
         gotoUsage();
      }
   }

   public void mousePressed(MouseEvent e) {
   }

   public void mouseReleased(MouseEvent e) {
   }

   public void mouseEntered(MouseEvent e) {
   }

   public void mouseExited(MouseEvent e) {
   }
}

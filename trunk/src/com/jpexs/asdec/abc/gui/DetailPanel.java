/*
 *  Copyright (C) 2011-2013 JPEXS
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.BevelBorder;

/**
 *
 * @author JPEXS
 */
public class DetailPanel extends JPanel implements ActionListener {

   public MethodTraitDetailPanel methodTraitPanel;
   public JPanel unsupportedTraitPanel;
   public SlotConstTraitDetailPanel slotConstTraitPanel;
   public static final String METHOD_TRAIT_CARD = "Method/Getter/Setter Trait";
   public static final String UNSUPPORTED_TRAIT_CARD = "-";
   public static final String SLOT_CONST_TRAIT_CARD = "Slot/Const Trait";
   private JPanel innerPanel;
   public JButton saveButton = new JButton("Save");
   public JButton editButton = new JButton("Edit");
   public JButton cancelButton = new JButton("Cancel");
   private HashMap<String, JComponent> cardMap = new HashMap<String, JComponent>();
   private String selectedCard;
   private JLabel selectedLabel;
   private boolean editMode = false;
   private JPanel buttonsPanel;
   private ABCPanel abcPanel;

   public DetailPanel(ABCPanel abcPanel) {
      this.abcPanel = abcPanel;
      innerPanel = new JPanel();
      CardLayout layout = new CardLayout();
      innerPanel.setLayout(layout);
      methodTraitPanel = new MethodTraitDetailPanel(abcPanel);
      cardMap.put(METHOD_TRAIT_CARD, methodTraitPanel);

      unsupportedTraitPanel = new JPanel(new BorderLayout());
      JLabel unsup = new JLabel("Select class and click a trait in Actionscript source to edit it.", SwingConstants.CENTER);
      unsupportedTraitPanel.add(unsup, BorderLayout.CENTER);

      cardMap.put(UNSUPPORTED_TRAIT_CARD, unsupportedTraitPanel);

      slotConstTraitPanel = new SlotConstTraitDetailPanel();
      cardMap.put(SLOT_CONST_TRAIT_CARD, slotConstTraitPanel);

      for (String key : cardMap.keySet()) {
         innerPanel.add(cardMap.get(key), key);
      }

      setLayout(new BorderLayout());
      add(innerPanel, BorderLayout.CENTER);

      buttonsPanel = new JPanel();
      buttonsPanel.setLayout(new FlowLayout());
      saveButton.setActionCommand("SAVEDETAIL");
      saveButton.addActionListener(this);
      editButton.setActionCommand("EDITDETAIL");
      editButton.addActionListener(this);
      cancelButton.setActionCommand("CANCELDETAIL");
      cancelButton.addActionListener(this);
      buttonsPanel.setBorder(new BevelBorder(BevelBorder.RAISED));
      buttonsPanel.add(editButton);
      buttonsPanel.add(saveButton);
      buttonsPanel.add(cancelButton);
      add(buttonsPanel, BorderLayout.SOUTH);
      selectedCard = UNSUPPORTED_TRAIT_CARD;
      layout.show(innerPanel, UNSUPPORTED_TRAIT_CARD);
      buttonsPanel.setVisible(false);
      selectedLabel = new JLabel("");
      selectedLabel.setText(selectedCard);
      selectedLabel.setBorder(new BevelBorder(BevelBorder.RAISED));
      selectedLabel.setHorizontalAlignment(SwingConstants.CENTER);
      add(selectedLabel, BorderLayout.NORTH);
   }

   public void setEditMode(boolean val) {
      slotConstTraitPanel.setEditMode(val);
      methodTraitPanel.setEditMode(val);
      saveButton.setVisible(val);
      editButton.setVisible(!val);
      cancelButton.setVisible(val);
      editMode = val;
   }

   public void showCard(String name) {
      CardLayout layout = (CardLayout) innerPanel.getLayout();
      layout.show(innerPanel, name);
      boolean b = cardMap.get(name) instanceof TraitDetail;
      buttonsPanel.setVisible(b);
      selectedCard = name;
      selectedLabel.setText(selectedCard);
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("EDITDETAIL")) {
         setEditMode(true);
         methodTraitPanel.methodCodePanel.focusEditor();
      }
      if (e.getActionCommand().equals("CANCELDETAIL")) {
         setEditMode(false);
         abcPanel.decompiledTextArea.resetEditing();
      }
      if (e.getActionCommand().equals("SAVEDETAIL")) {
         if (cardMap.get(selectedCard) instanceof TraitDetail) {
            if (((TraitDetail) cardMap.get(selectedCard)).save()) {
               int lasttrait = abcPanel.decompiledTextArea.lastTraitIndex;
               abcPanel.decompiledTextArea.reloadClass();
               abcPanel.decompiledTextArea.gotoTrait(lasttrait);
               JOptionPane.showMessageDialog(this, "Trait Successfully saved");
            }
         }
      }
   }
}

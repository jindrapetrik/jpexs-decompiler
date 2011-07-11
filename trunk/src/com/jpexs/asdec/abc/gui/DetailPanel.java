/*
 *  Copyright (C) 2011 JPEXS
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.jpexs.asdec.abc.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author JPEXS
 */
public class DetailPanel extends JPanel implements ActionListener {
   public MethodTraitDetailPanel methodTraitPanel;
   public JPanel unsupportedTraitPanel;
   public static final String METHOD_TRAIT_CARD="MethodTrait";
   public static final String UNSUPPORTED_TRAIT_CARD="UnsupportedTrait";
   private JPanel innerPanel;
   public JButton saveButton;
   private HashMap<String,JComponent> cardMap=new HashMap<String,JComponent>();
   private String selectedCard;

   public DetailPanel()
   {
      innerPanel=new JPanel();
      CardLayout layout=new CardLayout();
      innerPanel.setLayout(layout);
      methodTraitPanel=new MethodTraitDetailPanel();
      cardMap.put(METHOD_TRAIT_CARD, methodTraitPanel);
      
      unsupportedTraitPanel=new JPanel(new BorderLayout());
      JLabel unsup=new JLabel("Editing of this trait type is currently unsupported",SwingConstants.CENTER);
      unsupportedTraitPanel.add(unsup,BorderLayout.CENTER);

      cardMap.put(UNSUPPORTED_TRAIT_CARD, unsupportedTraitPanel);


      for(String key:cardMap.keySet())
      {
         innerPanel.add(cardMap.get(key),key);
      }

      setLayout(new BorderLayout());
      add(innerPanel,BorderLayout.CENTER);

      JPanel buttonsPanel=new JPanel();
      buttonsPanel.setLayout(new FlowLayout());
      saveButton = new JButton("Save");
      saveButton.setActionCommand("SAVEDETAIL");
      saveButton.addActionListener(this);
      buttonsPanel.add(saveButton);
      add(buttonsPanel,BorderLayout.SOUTH);
      selectedCard=UNSUPPORTED_TRAIT_CARD;
      layout.show(innerPanel, UNSUPPORTED_TRAIT_CARD);
      saveButton.setVisible(false);
   }

   public void showCard(String name)
   {
      CardLayout layout=(CardLayout)innerPanel.getLayout();
      layout.show(innerPanel, name);
      saveButton.setVisible(cardMap.get(name) instanceof TraitDetail);
      selectedCard=name;
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getActionCommand().equals("SAVEDETAIL")) {
            if(cardMap.get(selectedCard) instanceof TraitDetail)
            {
               if(((TraitDetail)cardMap.get(selectedCard)).save())
               {
                  JOptionPane.showMessageDialog(this, "Trait Successfully saved");
               }
            }
        }
   }
}

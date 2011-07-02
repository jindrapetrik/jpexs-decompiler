/*
 *  Copyright (C) 2010-2011 JPEXS
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

import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.Usage;
import com.jpexs.asdec.abc.types.traits.Trait;
import com.jpexs.asdec.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.asdec.gui.View;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class UsageFrame extends JFrame implements ActionListener {
    private JButton gotoButton=new JButton("Go to");
    private JButton cancelButton=new JButton("Cancel");
    private JList usageList;
    private DefaultListModel usageListModel=new DefaultListModel();
    public UsageFrame(ABC abc,int multinameIndex){
      List<Usage> usages=abc.findMultinameUsage(multinameIndex);
      for(Usage u:usages){
          usageListModel.addElement(u);
      }
      usageList=new JList(usageListModel);
      gotoButton.setActionCommand("GOTO");
      gotoButton.addActionListener(this);
      cancelButton.setActionCommand("CANCEL");
      cancelButton.addActionListener(this);
      JPanel buttonsPanel=new JPanel();
      buttonsPanel.setLayout(new FlowLayout());
      //buttonsPanel.add(gotoButton);
      buttonsPanel.add(cancelButton);

      Container cont=getContentPane();
      cont.setLayout(new BorderLayout());
      cont.add(new JScrollPane(usageList),BorderLayout.CENTER);
      cont.add(buttonsPanel, BorderLayout.SOUTH);
      setSize(400,300);
      setTitle("Usages:"+abc.constants.constant_multiname[multinameIndex].getNameWithNamespace(abc.constants));
      View.centerScreen(this);
      View.setWindowIcon(this);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("GOTO")){

        }
        if(e.getActionCommand().equals("CANCEL")){
            setVisible(false);
        }
    }
}

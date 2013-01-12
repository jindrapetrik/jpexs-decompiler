/*
 *  Copyright (C) 2011 JPEXS
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
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class MethodCodePanel extends JPanel implements ActionListener {

   public ASMSourceEditorPane sourceTextArea;
   public JPanel buttonsPanel;

   public MethodCodePanel() {
      sourceTextArea = new ASMSourceEditorPane();

      setLayout(new BorderLayout());
      add(new JScrollPane(sourceTextArea), BorderLayout.CENTER);
      sourceTextArea.setContentType("text/flasm3");

      buttonsPanel = new JPanel();
      buttonsPanel.setLayout(new FlowLayout());
      JButton verifyButton = new JButton("Verify");
      verifyButton.setActionCommand("VERIFYBODY");
      verifyButton.addActionListener(this);

      JButton graphButton = new JButton("Graph");
      graphButton.setActionCommand("GRAPH");
      graphButton.addActionListener(this);

      JButton execButton = new JButton("Execute");
      execButton.setActionCommand("EXEC");
      execButton.addActionListener(this);

      //buttonsPan.add(graphButton);
      // buttonsPanel.add(saveButton);
      // buttonsPan.add(execButton);

      //add(buttonsPanel, BorderLayout.SOUTH);
   }

   public void actionPerformed(ActionEvent e) {
      if (Main.isWorking()) {
         return;
      }
      if (e.getActionCommand().equals("GRAPH")) {
         sourceTextArea.graph();
      }

      if (e.getActionCommand().equals("EXEC")) {
         sourceTextArea.exec();
      }
      if (e.getActionCommand().equals("VERIFYBODY")) {
         sourceTextArea.verify(Main.mainFrame.abcPanel.abc.constants, Main.mainFrame.abcPanel.abc);
      }
   }
}

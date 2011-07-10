/*
 *  Copyright (C) 2011 Jindra
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

import com.jpexs.asdec.abc.types.MethodBody;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.NumberFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

/**
 *
 * @author Jindra
 */
public class MethodBodyPanel extends JPanel {

   public JLabel maxStackLabel = new JLabel("Max stack:");
   public JFormattedTextField maxStackField = new JFormattedTextField(NumberFormat.getNumberInstance());
   public JLabel localCountLabel = new JLabel("Local registers count:");
   public JFormattedTextField localCountField = new JFormattedTextField(NumberFormat.getNumberInstance());
   public JLabel initScopeDepthLabel = new JLabel("Minimum scope depth:");
   public JFormattedTextField initScopeDepthField = new JFormattedTextField(NumberFormat.getNumberInstance());
   public JLabel maxScopeDepthLabel = new JLabel("Maximum scope depth:");
   public JFormattedTextField maxScopeDepthField = new JFormattedTextField(NumberFormat.getNumberInstance());

   public MethodBody body;

   public MethodBodyPanel() {
      setLayout(new GridLayout(4,1));
      
      maxStackField.setPreferredSize(new Dimension(75, 20));
      JPanel pan1=new JPanel();
      pan1.setLayout(new FlowLayout());
      pan1.add(maxStackLabel);
      pan1.add(maxStackField);
      
      add(pan1);
      localCountField.setPreferredSize(new Dimension(75, 20));
      JPanel pan2=new JPanel();
      pan2.setLayout(new FlowLayout());
      pan2.add(localCountLabel);
      pan2.add(localCountField);

      add(pan2);
      initScopeDepthField.setPreferredSize(new Dimension(75, 20));
      JPanel pan3=new JPanel();
      pan3.setLayout(new FlowLayout());
      pan3.add(initScopeDepthLabel);
      pan3.add(initScopeDepthField);

      add(pan3);
      maxScopeDepthField.setPreferredSize(new Dimension(75, 20));
      JPanel pan4=new JPanel();
      pan4.setLayout(new FlowLayout());
      pan4.add(maxScopeDepthLabel);
      pan4.add(maxScopeDepthField);
      add(pan4);
      setPreferredSize(new Dimension(150,150));
   }

   public void loadFromBody(MethodBody body)
   {
      this.body=body;
      if(body==null){
         maxStackField.setText("0");
         localCountField.setText("0");
         initScopeDepthField.setText("0");
         maxScopeDepthField.setText("0");
         return;
      }
      maxStackField.setText(""+body.max_stack);
      localCountField.setText(""+body.max_regs);
      initScopeDepthField.setText(""+body.init_scope_depth);
      maxScopeDepthField.setText(""+body.max_scope_depth);
   }

   public void save(){
      if(body!=null)
      {
         body.max_stack=Integer.parseInt(maxStackField.getText());
         body.max_regs=Integer.parseInt(localCountField.getText());
         body.init_scope_depth=Integer.parseInt(initScopeDepthField.getText());
         body.max_scope_depth=Integer.parseInt(maxScopeDepthField.getText());
      }
   }
}

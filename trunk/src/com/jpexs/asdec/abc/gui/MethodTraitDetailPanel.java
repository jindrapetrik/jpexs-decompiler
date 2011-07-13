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

import com.jpexs.asdec.Main;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

/**
 *
 * @author JPEXS
 */
public class MethodTraitDetailPanel extends JTabbedPane implements TraitDetail {

   public MethodCodePanel methodCodePanel;
   public MethodBodyParamsPanel methodBodyParamsPanel;
   public MethodInfoPanel methodInfoPanel;

   public MethodTraitDetailPanel() {
      methodCodePanel = new MethodCodePanel();
      methodBodyParamsPanel = new MethodBodyParamsPanel();
      methodInfoPanel=new MethodInfoPanel();
      addTab("MethodInfo",methodInfoPanel);
      addTab("MethodBody Code", methodCodePanel);
      addTab("MethodBody params", new JScrollPane(methodBodyParamsPanel));
      setSelectedIndex(1);
   }

   public boolean save() {
      if(!methodInfoPanel.save())
      {
         return false;
      }
      if (!methodCodePanel.sourceTextArea.save(Main.abcMainFrame.abc.constants))
      {
         return false;
      }
      if(!methodBodyParamsPanel.save())
      {
         return false;
      }

      int lasttrait = Main.abcMainFrame.decompiledTextArea.lastTraitIndex;
      Main.abcMainFrame.decompiledTextArea.reloadClass();
      Main.abcMainFrame.decompiledTextArea.gotoTrait(lasttrait);
      return true;
   }
}

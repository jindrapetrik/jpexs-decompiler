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
   public ABCPanel abcPanel;

   public MethodTraitDetailPanel(ABCPanel abcPanel) {
      this.abcPanel = abcPanel;
      methodCodePanel = new MethodCodePanel(abcPanel.decompiledTextArea);
      methodBodyParamsPanel = new MethodBodyParamsPanel(abcPanel);
      methodInfoPanel = new MethodInfoPanel();
      addTab("MethodInfo", methodInfoPanel);
      addTab("MethodBody Code", methodCodePanel);
      addTab("MethodBody params", new JScrollPane(methodBodyParamsPanel));
      setSelectedIndex(1);
   }

   public boolean save() {
      if (!methodInfoPanel.save()) {
         return false;
      }
      if (!methodCodePanel.save(abcPanel.abc.constants)) {
         return false;
      }
      if (!methodBodyParamsPanel.save()) {
         return false;
      }

      return true;
   }

   @Override
   public void setEditMode(boolean val) {
      methodCodePanel.setEditMode(val);
      methodBodyParamsPanel.setEditMode(val);
      methodInfoPanel.setEditMode(val);
   }
}

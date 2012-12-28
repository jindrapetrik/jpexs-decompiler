/*
 *  Copyright (C) 2010-2011 JPEXS
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
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TraitsList extends JList implements ListSelectionListener {

   ABC abc;
   int classIndex = -1;

   public TraitsList() {
      addListSelectionListener(this);
      setCellRenderer(new IconListRenderer());
   }

   public void setABC(ABC abc) {
      this.abc = abc;
      setModel(new DefaultListModel());
   }

   public void setClassIndex(int classIndex) {
      if (classIndex == -1) {
         setModel(new DefaultListModel());
      } else {
         if (abc != null) {
            setModel(new TraitsListModel(abc, classIndex));
         }
      }
      this.classIndex = classIndex;

   }

   public void valueChanged(ListSelectionEvent e) {

      int index = getSelectedIndex();

      Main.abcMainFrame.decompiledTextArea.gotoTrait(index);

   }
}

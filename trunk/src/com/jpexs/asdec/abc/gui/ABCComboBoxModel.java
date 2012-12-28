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

import com.jpexs.asdec.tags.DoABCTag;
import java.util.Collections;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

public class ABCComboBoxModel implements ComboBoxModel {

   public List<DoABCTag> list;
   public int itemIndex = 0;

   public ABCComboBoxModel(List<DoABCTag> list) {
      this.list = list;
      Collections.sort(this.list);
   }

   public int getSize() {
      return list.size();
   }

   public Object getElementAt(int index) {
      return list.get(index);
   }

   public void addListDataListener(ListDataListener l) {
   }

   public void removeListDataListener(ListDataListener l) {
   }

   public void setSelectedItem(Object anItem) {
      itemIndex = list.indexOf(anItem);
   }

   public Object getSelectedItem() {
      return getElementAt(itemIndex);
   }
}

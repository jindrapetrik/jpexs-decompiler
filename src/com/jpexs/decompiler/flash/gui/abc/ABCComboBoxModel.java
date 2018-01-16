/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import java.util.Collections;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

/**
 *
 * @author JPEXS
 */
public class ABCComboBoxModel implements ComboBoxModel<ABCContainerTag> {

    public List<ABCContainerTag> list;

    public int itemIndex = 0;

    public static final ABCContainerTag ROOT = new RootABCContainerTag();

    public ABCComboBoxModel(List<ABCContainerTag> list) {
        this.list = list;
        Collections.sort(this.list);
    }

    @Override
    public int getSize() {
        return 1 + list.size();
    }

    @Override
    public ABCContainerTag getElementAt(int index) {
        if (index == 0) {
            return ROOT;
        }
        return list.get(index - 1);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem == ROOT) {
            itemIndex = 0;
        } else {
            itemIndex = 1 + list.indexOf(anItem);
        }
    }

    @Override
    public ABCContainerTag getSelectedItem() {
        return getElementAt(itemIndex);
    }
}

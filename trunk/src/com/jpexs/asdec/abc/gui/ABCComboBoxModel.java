/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.tags.DoABCTag;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.List;


public class ABCComboBoxModel implements ComboBoxModel {
    public List<DoABCTag> list;
    public int itemIndex = 0;

    public ABCComboBoxModel(List<DoABCTag> list) {
        this.list = list;
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

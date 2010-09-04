/*
 * Copyright (c) 2010. JPEXS
 */

package com.jpexs.asdec.abc.gui;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;

import javax.swing.*;
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
        if (classIndex != -1)
            setModel(new TraitsListModel(abc, classIndex));
    }

    public void setClassIndex(int classIndex) {
        if (abc != null)
            setModel(new TraitsListModel(abc, classIndex));
        this.classIndex = classIndex;

    }

    public void valueChanged(ListSelectionEvent e) {

        int index = getSelectedIndex();
        if (index == -1)
            return;
        Main.abcMainFrame.decompiledTextArea.gotoTrait(index);

    }
}

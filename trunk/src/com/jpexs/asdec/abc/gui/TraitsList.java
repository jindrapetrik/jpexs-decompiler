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
       
        Main.abcMainFrame.decompiledTextArea.gotoTrait(index);

    }
}

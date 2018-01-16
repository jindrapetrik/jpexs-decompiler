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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author JPEXS
 */
public class TraitsList extends JList<Object> implements ListSelectionListener {

    private ABC abc;

    private int classIndex = -1;

    private final ABCPanel abcPanel;

    private boolean sorted = false;

    public void setSorted(boolean sorted) {
        if (getModel() instanceof TraitsListModel) {
            ((TraitsListModel) getModel()).setSorted(sorted);
        }
        this.sorted = sorted;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public TraitsList(ABCPanel abcPanel) {
        addListSelectionListener(this);
        this.abcPanel = abcPanel;
        setCellRenderer(new IconListRenderer());
        //setUI(new BasicListUI());
        setBackground(Color.white);
    }

    public void clearAbc() {
        this.abc = null;
        setModel(new DefaultListModel<>());
    }

    public void setAbc(ABC abc) {
        this.abc = abc;
        setModel(new DefaultListModel<>());
        setClassIndex(-1, -1);
    }

    public void setClassIndex(int classIndex, int scriptIndex) {
        if (classIndex >= abc.instance_info.size()) {
            return;
        }

        this.classIndex = classIndex;
        setModel(new TraitsListModel(abc, classIndex, scriptIndex, sorted));
    }

    private int lastSelected = -1;

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (getSelectedIndex() == lastSelected) {
            return;
        }
        lastSelected = getSelectedIndex();
        TraitsListItem sel = (TraitsListItem) getSelectedValue();
        abcPanel.decompiledTextArea.gotoTrait(sel == null ? GraphTextWriter.TRAIT_UNKNOWN : sel.getGlobalTraitId());
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.SrcOver);
        super.paint(g);
    }
}

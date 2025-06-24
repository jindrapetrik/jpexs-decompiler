/*
 *  Copyright (C) 2024-2025 JPEXS
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
package com.jpexs.decompiler.flash.easygui.properties;

import com.jpexs.decompiler.flash.configuration.Configuration;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

/**
 *
 * @author JPEXS
 */

public class JTriStateCheckBox extends JCheckBox {

    private static final boolean MID_AS_SELECTED = true;  //consider mid-state as selected ?

    public JTriStateCheckBox() {
        this("");
    }

    public JTriStateCheckBox(String text) {
        super(text);
        putClientProperty("SelectionState", 0);
    }

    @Override
    protected void fireActionPerformed(ActionEvent event) {
        actionPerformed();
        super.fireActionPerformed(event);
    }

    public JTriStateCheckBox(String text, int sel) {
        /* tri-state checkbox has 3 selection states:
         * 0 unselected
         * 1 mid-state selection
         * 2 fully selected
         */
        super(text, sel > 1 ? true : false);

        switch (sel) {
            case 2:
                setSelected(true);
                //fallthrough
            case 1:
            case 0:
                putClientProperty("SelectionState", sel);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isSelected() {
        if (MID_AS_SELECTED && (getSelectionState() > 0)) {
            return true;
        }
        return super.isSelected();
    }

    public int getSelectionState() {
        return (getClientProperty("SelectionState") != null ? (int) getClientProperty("SelectionState")
                : super.isSelected() ? 2
                : 0);
    }

    public void setSelectionState(int sel) {
        switch (sel) {
            case 2:
                super.setSelected(true);
                break;
            case 1:
            case 0:
                super.setSelected(false);
                break;
            default:
                throw new IllegalArgumentException();
        }
        putClientProperty("SelectionState", sel);
    }

    @Override
    public void setSelected(boolean b) {
        super.setSelected(b);
        putClientProperty("SelectionState", b ? 2 : 0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (getSelectionState() == 1) {
            int w = 12;
            if (Configuration.useRibbonInterface.get()) {
                /*
                SubstanceColorScheme baseMarkColorScheme = SubstanceColorSchemeUtilities
                .getColorScheme(this, ColorSchemeAssociationKind.MARK,
                ComponentState.getState(this));
           
                g.setColor(baseMarkColorScheme.getSelectionForegroundColor());*/
                w = 14;
            } else {
                w = 12;
            }
            g.setColor(getForeground());
            g.fillRect(5, getHeight() / 2 - 2, w, 4);
        }
    }

    private void actionPerformed() {
        int selectionState = getSelectionState();
        switch (selectionState) {
            case 0:
            case 1:
                selectionState = 2;
                break;
            case 2:
                selectionState = 0;
                break;
        }
        putClientProperty("SelectionState", selectionState);

        repaint();
    }
}

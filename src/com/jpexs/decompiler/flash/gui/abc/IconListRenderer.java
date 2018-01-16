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

import com.jpexs.decompiler.flash.abc.types.traits.TraitType;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.Component;
import java.net.URL;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;

/**
 *
 * @author JPEXS
 */
public class IconListRenderer extends DefaultListCellRenderer {

    private final Icon constIcon;

    private final Icon functionIcon;

    private final Icon variableIcon;

    private Icon loadIcon(String path) {
        ClassLoader cldr = this.getClass().getClassLoader();
        URL imageURL = cldr.getResource(path);
        return new ImageIcon(imageURL);
    }

    public IconListRenderer() {
        constIcon = View.getIcon("constant");
        functionIcon = View.getIcon("function");
        variableIcon = View.getIcon("variable");
    }

    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        // Get the renderer component from parent class
        JLabel label
                = (JLabel) super.getListCellRendererComponent(list,
                        value, index, isSelected, cellHasFocus);

        // Get icon to use for the list item value
        TraitsListItem tli = (TraitsListItem) value;

        if (tli.getType() == TraitType.CONST) {
            label.setIcon(constIcon);
        }

        if (tli.getType() == TraitType.VAR) {
            label.setIcon(variableIcon);
        }

        if (tli.getType() == TraitType.METHOD) {
            label.setIcon(functionIcon);
        }

        if (tli.getType() == TraitType.INITIALIZER) {
            label.setIcon(functionIcon);
        }

        if (tli.getType() == TraitType.SCRIPT_INITIALIZER) {
            label.setIcon(functionIcon);
        }

        return label;
    }
}

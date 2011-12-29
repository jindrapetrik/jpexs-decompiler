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

import javax.swing.*;
import java.awt.*;


public class IconListRenderer
        extends DefaultListCellRenderer {

    private Icon constIcon;
    private Icon functionIcon;
    private Icon variableIcon;

    private Icon loadIcon(String path) {
        ClassLoader cldr = this.getClass().getClassLoader();
        java.net.URL imageURL = cldr.getResource(path);
        return new ImageIcon(imageURL);
    }

    public IconListRenderer() {
        constIcon = loadIcon("com/jpexs/asdec/abc/gui/graphics/constant.png");
        functionIcon = loadIcon("com/jpexs/asdec/abc/gui/graphics/function.png");
        variableIcon = loadIcon("com/jpexs/asdec/abc/gui/graphics/variable.png");
    }

    @Override
    public Component getListCellRendererComponent(
            JList list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        // Get the renderer component from parent class

        JLabel label =
                (JLabel) super.getListCellRendererComponent(list,
                        value, index, isSelected, cellHasFocus);

        // Get icon to use for the list item value

        String modifiersRegex = "(public |static |final |override |private |protected |package )*";

        if (value.toString().matches(modifiersRegex + "const .*"))
            label.setIcon(constIcon);
        if (value.toString().matches(modifiersRegex + "var .*"))
            label.setIcon(variableIcon);
        if (value.toString().matches(modifiersRegex + "function .*"))
            label.setIcon(functionIcon);
        if (value.toString().equals(TraitsListModel.STR_CLASS_INITIALIZER))
            label.setIcon(functionIcon);
        if (value.toString().equals(TraitsListModel.STR_INSTANCE_INITIALIZER))
            label.setIcon(functionIcon);
        return label;
    }

}


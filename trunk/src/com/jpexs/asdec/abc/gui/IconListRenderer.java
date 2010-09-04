/*
 * Copyright (c) 2010. JPEXS
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


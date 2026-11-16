/*
 *  Copyright (C) 2026 JPEXS, All rights reserved.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.helpers.FontHelper;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * A font specification field with a button to open the font chooser.
 */
class ConfigurationFontSelection extends JPanel {

    private final JTextField textField;

    ConfigurationFontSelection(String value, String description, String chooseLabel) {
        super(new BorderLayout(4, 0));
        textField = new JTextField(value);
        textField.setToolTipText(description);
        add(textField, BorderLayout.CENTER);

        JButton chooseButton = new JButton(View.getIcon("font16"));
        chooseButton.setToolTipText(chooseLabel);
        chooseButton.getAccessibleContext().setAccessibleName(chooseLabel);
        chooseButton.addActionListener(e -> chooseFont());
        add(chooseButton, BorderLayout.EAST);
    }

    JTextField getTextField() {
        return textField;
    }

    private void chooseFont() {
        String value = textField.getText();
        Font initialFont = value.isEmpty() ? textField.getFont() : FontHelper.stringToFont(value);
        FontChooserDialog dialog = new FontChooserDialog(SwingUtilities.getWindowAncestor(this), initialFont);
        dialog.setVisible(true);
        Font selectedFont = dialog.getSelectedFont();
        if (selectedFont != null) {
            textField.setText(FontHelper.fontToString(selectedFont));
        }
    }
}

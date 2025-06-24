/*
 *  Copyright (C) 2022-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.configuration.ConfigurationItem;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author JPEXS
 */
public class ConfigurationDirectorySelection extends JPanel {

    private JTextField textField;

    public ConfigurationDirectorySelection(ConfigurationItem item, String value, String description) {
        setLayout(new BorderLayout());

        textField = new JTextField();
        textField.setText(value);
        textField.setToolTipText(description);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));

        setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
        add(textField, BorderLayout.CENTER);
        JButton butSelect = new JButton(View.getIcon("folderopen16"));
        butSelect.setToolTipText(ResourceBundle.getBundle(AppStrings.getResourcePath(MainFrame.class)).getString("FileChooser.openButtonText"));
        butSelect.setMargin(new Insets(2, 2, 2, 2));
        butSelect.addActionListener((ActionEvent e) -> {
            textField.setText(selectConfigDirectory(item, textField.getText()));
        });
        add(butSelect, BorderLayout.EAST);
    }

    private static String selectConfigDirectory(ConfigurationItem config, String current) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(current));
        fc.setMultiSelectionEnabled(false);
        fc.setCurrentDirectory(new File((String) config.get()));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(Main.getDefaultMessagesComponent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return Helper.fixDialogFile(fc.getSelectedFile()).getAbsolutePath();
        } else {
            return (String) config.get();
        }
    }

    public String getValue() {
        return textField.getText();
    }

    public JTextField getTextField() {
        return textField;
    }

}

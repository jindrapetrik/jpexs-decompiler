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

import com.jpexs.decompiler.flash.configuration.ConfigurationFile;
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
import javax.swing.filechooser.FileFilter;

/**
 * @author JPEXS
 */
public class ConfigurationFileSelection extends JPanel {

    private JTextField textField;

    public ConfigurationFileSelection(ConfigurationItem item, ConfigurationFile confFile, String value, String description) {
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
            textField.setText(selectConfigFile(item, textField.getText(), confFile.value()));
        });
        add(butSelect, BorderLayout.EAST);
    }

    private static String selectConfigFile(ConfigurationItem config, String current, String pattern) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(current));
        fc.setMultiSelectionEnabled(false);
        fc.setCurrentDirectory(new File((String) config.get()));
        FileFilter allSupportedFilter = new FileFilter() {
            private final String[] supportedExtensions = new String[]{".swf", ".gfx", ".swc", ".zip", ".iggy"};

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                return f.getName().matches(pattern);
            }

            @Override
            public String getDescription() {
                return "";
            }
        };
        fc.setFileFilter(allSupportedFilter);

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

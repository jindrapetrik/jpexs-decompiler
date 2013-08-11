/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.Configuration;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author JPEXS
 */
public class SelectLanguageDialog extends AppDialog implements ActionListener {

    JComboBox<Language> languageCombobox = new JComboBox<>();
    public String languageCode = null;
    private String languages[] = new String[]{"en", "cs", "de", "hu", "ru", "sv"};

    public SelectLanguageDialog() {
        setSize(350, 130);
        Container cnt1 = getContentPane();
        JPanel cnt = new JPanel();
        cnt1.setLayout(new BorderLayout());
        cnt1.add(cnt, BorderLayout.CENTER);


        String currentLanguage = (String) Configuration.getConfig("locale", Locale.getDefault().getLanguage());
        boolean found = false;
        int enIndex = 0;
        for (String code : languages) {
            String name = ResourceBundle.getBundle(AppStrings.getResourcePath(getClass()), Locale.forLanguageTag(code.equals("en") ? "" : code)).getString("language");
            if (name.length() > 1) {
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
            }
            if (code.equals("en")) {
                enIndex = languageCombobox.getItemCount();
            }
            languageCombobox.addItem(new Language(code, name));
            if (code.equals(currentLanguage)) {
                languageCombobox.setSelectedIndex(languageCombobox.getItemCount() - 1);
                found = true;
            }
        }

        if (!found) {
            languageCombobox.setSelectedIndex(enIndex);
        }
        cnt.setBorder(new EmptyBorder(10, 10, 10, 10));
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));
        JLabel langLabel = new JLabel(translate("language.label"));
        langLabel.setAlignmentX(0.5f);
        cnt.add(langLabel);
        languageCombobox.setAlignmentX(0.5f);
        cnt.add(languageCombobox);
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.setAlignmentX(0.5f);
        JButton okButton = new JButton(translate("button.ok"));
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.setActionCommand("CANCEL");
        cancelButton.addActionListener(this);
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        cnt.add(buttonsPanel);
        getRootPane().setDefaultButton(okButton);
        setModalityType(ModalityType.APPLICATION_MODAL);
        View.setWindowIcon(this);
        View.centerScreen(this);
        setTitle(translate("dialog.title"));
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "OK":
                if (languageCombobox.getSelectedIndex() == -1) {
                } else {
                    languageCode = ((Language) languageCombobox.getSelectedItem()).code;
                    String newLanguage = languageCode;
                    if (newLanguage.equals("en")) {
                        newLanguage = "";
                    }
                    Configuration.setConfig("locale", newLanguage);
                    Locale.setDefault(Locale.forLanguageTag(newLanguage));
                    updateLanguage();
                    setVisible(false);
                    AppStrings.updateLanguage();
                    Main.reloadApp();
                }
                break;
            case "CANCEL":
                setVisible(false);
                break;
        }
    }

    public String display() {
        setVisible(true);
        return languageCode;
    }
}

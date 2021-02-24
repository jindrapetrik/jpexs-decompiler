/*
 *  Copyright (C) 2021 JPEXS
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
package com.jpexs.decompiler.flash.gui.hexview;

import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author JPEXS
 */
public class GotoAddressDialog extends AppDialog {

    private final JTextField lineTextField;
    private Long value = null;
    private final JButton okButton;
    private final JCheckBox hexCheckBox;
    private boolean okPressed = false;

    public GotoAddressDialog() {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));
        lineTextField = new JTextField(10);
        lineTextField.setFont(new Font("Monospaced", Font.PLAIN, lineTextField.getFont().getSize()));
        lineTextField.addActionListener(this::okButtonActionPerformed);
        okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);

        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        Container cnt = getContentPane();

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        JPanel centerPanel = new JPanel(new FlowLayout());
        centerPanel.add(lineTextField);

        hexCheckBox = new JCheckBox(translate("hex"));
        hexCheckBox.setSelected(true);
        centerPanel.add(hexCheckBox);

        cnt.setLayout(new BorderLayout());
        cnt.add(buttonsPanel, BorderLayout.SOUTH);
        cnt.add(centerPanel, BorderLayout.CENTER);

        hexCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkEnabled();
            }
        });

        lineTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkEnabled();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkEnabled();
            }
        });

        pack();
        View.centerScreen(this);
        View.setWindowIcon(this);
        setModal(true);
        checkEnabled();
    }

    private void checkEnabled() {
        try {
            if (lineTextField.getText().matches(".*[a-fA-F].*") && lineTextField.getText().matches("[a-fA-F0-9]")) {
                hexCheckBox.setSelected(true);
            }
            value = Long.parseLong(lineTextField.getText(), hexCheckBox.isSelected() ? 16 : 10);
            okButton.setEnabled(true);
        } catch (NumberFormatException nfe) {
            value = null;
            okButton.setEnabled(false);
        }
    }

    public void okButtonActionPerformed(ActionEvent e) {
        okPressed = true;
        setVisible(false);
    }

    public void cancelButtonActionPerformed(ActionEvent e) {
        setVisible(false);
    }


    public Long showDialog() {
        setVisible(true);
        if (okPressed) {
            return value;
        } else {
            return null;
        }
    }
}

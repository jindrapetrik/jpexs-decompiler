/*
 * Copyright (C) 2021 JPEXS
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
package com.jpexs.decompiler.flash.gui.action;

import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author JPEXS
 */
public class AddScriptDialog extends AppDialog {

    private final JButton okButton = new JButton(translate("button.ok"));

    private final JButton cancelButton = new JButton(translate("button.cancel"));
    private JTextField frameTextField;

    private int frame = -1;

    private int result = ERROR_OPTION;

    public AddScriptDialog() {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));

        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());

        JPanel panButtons = new JPanel(new FlowLayout());
        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        panButtons.add(okButton);
        panButtons.add(cancelButton);

        JPanel centerPanel = new JPanel(new FlowLayout());
        JLabel frameLabel = new JLabel(translate("framenum"));
        frameTextField = new JTextField(4);
        frameTextField.addActionListener(this::okButtonActionPerformed);
        frameLabel.setLabelFor(frameTextField);
        centerPanel.add(frameLabel);
        centerPanel.add(frameTextField);
        cnt.add(centerPanel, BorderLayout.CENTER);

        cnt.add(panButtons, BorderLayout.SOUTH);

        setModal(true);
        setResizable(true);
        pack();
        View.centerScreen(this);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        boolean invalid = false;
        try {
            frame = Integer.parseInt(frameTextField.getText());
            if (frame <= 0) {
                invalid = true;
            }

        } catch (NumberFormatException nfe) {
            invalid = true;
        }

        if (invalid) {
            View.showMessageDialog(this, translate("message.framenum.invalid"), Main.getMainFrame().translate("error"), JOptionPane.ERROR_MESSAGE);
            frame = -1;
        } else {
            result = OK_OPTION;
            setVisible(false);
        }
    }

    public int getFrame() {
        return frame;
    }


    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = CANCEL_OPTION;
        setVisible(false);
    }

    public int showDialog() {
        setVisible(true);
        return result;
    }
}

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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author JPEXS
 */
public class AddClassDialog extends AppDialog {

    private final JButton okButton = new JButton(translate("button.ok"));
    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private final JTextField classNameTextField = new JTextField(30);
    private String result = null;

    public AddClassDialog(Window owner) {
        super(owner);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));

        Container cnt = getContentPane();
        cnt.setLayout(new BoxLayout(cnt, BoxLayout.Y_AXIS));

        JPanel panButtons = new JPanel(new FlowLayout());
        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        panButtons.add(okButton);
        panButtons.add(cancelButton);

        JLabel classNameLabel = new JLabel(translate("classname"));
        classNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cnt.add(classNameLabel);
        cnt.add(classNameTextField);

        cnt.add(panButtons);

        classNameTextField.getDocument().addDocumentListener(new DocumentListener() {
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

        classNameTextField.addActionListener(this::okButtonActionPerformed);

        pack();
        setModal(true);
        setResizable(false);
        View.setWindowIcon(this);
        View.centerScreen(this);
        checkEnabled();
    }

    private void checkEnabled() {

        boolean ok = true;

        if (classNameTextField.getText().isEmpty()) {
            ok = false;
        }

        if (classNameTextField.getText().endsWith(".")) {
            ok = false;
        }

        if (ok) {
            SWF swf = Main.getMainFrame().getPanel().getCurrentSwf();
            List<String> classNames = new ArrayList<>();
            classNames.add(classNameTextField.getText());
            try {
                List<ScriptPack> scriptPacks = swf.getScriptPacksByClassNames(classNames);
                if (!scriptPacks.isEmpty()) {
                    ok = false;
                }
            } catch (Exception ex) {
                ok = false;
            }
        }

        okButton.setEnabled(ok);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        if (!okButton.isEnabled()) {
            return;
        }
        result = classNameTextField.getText();
        setVisible(false);
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        result = null;
        setVisible(false);
    }

    public String showDialog() {
        return showDialog("");
    }
    public String showDialog(String pkg) {
        classNameTextField.setText(pkg);
        result = null;
        setVisible(true);
        return result;
    }
}

/*
 *  Copyright (C) 2010-2025 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.configuration.CustomConfigurationKeys;
import com.jpexs.decompiler.flash.configuration.SwfSpecificCustomConfiguration;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author JPEXS
 */
public class PathResolvingDialog extends AppDialog {

    private final JEditorPane editor;
    private final JButton okButton = new JButton(translate("button.ok"));
    private final JButton cancelButton = new JButton(translate("button.cancel"));

    private int result = ERROR_OPTION;
    private final SWF swf;

    public PathResolvingDialog(SWF swf, Window owner) {
        super(owner);
        setTitle(translate("dialog.title"));
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        cnt.add(new JLabel("<html>" + translate("info") + "</html>"), BorderLayout.NORTH);
        editor = new JEditorPane();
        cnt.add(new JScrollPane(editor), BorderLayout.CENTER);
        editor.setContentType("text/plain");

        okButton.addActionListener(this::okButtonActionPerformed);
        cancelButton.addActionListener(this::cancelButtonActionPerformed);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        cnt.add(buttonsPanel, BorderLayout.SOUTH);

        SwfSpecificCustomConfiguration cc = Configuration.getSwfSpecificCustomConfiguration(swf.getShortPathTitle());
        String pathResolving = "";
        if (cc != null) {
            pathResolving = cc.getCustomData(CustomConfigurationKeys.KEY_PATH_RESOLVING, "");
        }
        editor.setText(pathResolving);

        setSize(800, 600);
        View.centerScreen(this);
        View.setWindowIcon(this);
        getRootPane().setDefaultButton(okButton);
        setModal(true);
        this.swf = swf;
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = OK_OPTION;
        SwfSpecificCustomConfiguration cc = Configuration.getOrCreateSwfSpecificCustomConfiguration(swf.getShortPathTitle());
        String txt = editor.getText();
        txt = txt.replace("\r\n", "\n");
        txt = txt.replace("\n", "\r\n");
        cc.setCustomData(CustomConfigurationKeys.KEY_PATH_RESOLVING, txt);
        setVisible(false);
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

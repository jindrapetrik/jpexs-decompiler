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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.DeobfuscationLevel;
import com.jpexs.decompiler.flash.abc.avm2.deobfuscation.DeobfuscationScope;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * @author JPEXS
 */
public class DeobfuscationDialog extends AppDialog {

    private final JRadioButton removeDeadCodeRadioButton = new JRadioButton(translate("deobfuscation.removedeadcode"));
    private final JRadioButton removeTrapsRadioButton = new JRadioButton(translate("deobfuscation.removetraps"));

    private final JRadioButton methodScopeRadioButton = new JRadioButton(translate("deobfuscation.scope.method"));
    private final JRadioButton scriptScopeRadioButton = new JRadioButton(translate("deobfuscation.scope.script"));
    private final JRadioButton swfScopeRadioButton = new JRadioButton(translate("deobfuscation.scope.swf"));

    private int result = ERROR_OPTION;

    @SuppressWarnings("unchecked")
    public DeobfuscationDialog(Window owner) {
        super(owner);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setTitle(translate("dialog.title"));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        JLabel processingLevelLabel = new JLabel(translate("deobfuscation.level"));
        processingLevelLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        contentPane.add(processingLevelLabel);

        JPanel processingLevelPanel = new JPanel(new FlowLayout());
        ButtonGroup levelGroup = new ButtonGroup();
        levelGroup.add(removeDeadCodeRadioButton);
        levelGroup.add(removeTrapsRadioButton);
        removeTrapsRadioButton.setSelected(true);

        processingLevelPanel.add(removeDeadCodeRadioButton);
        processingLevelPanel.add(removeTrapsRadioButton);
        processingLevelPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        removeTrapsRadioButton.setSelected(true);
        contentPane.add(processingLevelPanel);

        JLabel scopeLabel = new JLabel(translate("deobfuscation.scope"));
        scopeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        contentPane.add(scopeLabel);

        ButtonGroup scopeGroup = new ButtonGroup();
        scopeGroup.add(methodScopeRadioButton);
        scopeGroup.add(scriptScopeRadioButton);
        scopeGroup.add(swfScopeRadioButton);
        JPanel scopePanel = new JPanel(new FlowLayout());
        scopePanel.add(methodScopeRadioButton);
        scopePanel.add(scriptScopeRadioButton);
        scopePanel.add(swfScopeRadioButton);
        swfScopeRadioButton.setSelected(true);
        scopePanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        contentPane.add(scopePanel);

        JLabel warningLabel = new JLabel("<html><center>" + translate("warning.modify").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\r\n", "<br/>") + "</center></html>");
        warningLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        warningLabel.setFont(warningLabel.getFont().deriveFont(Font.BOLD));
        contentPane.add(warningLabel);

        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        JButton okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setAlignmentX(0.5f);
        contentPane.add(buttonsPanel);

        setModal(true);
        pack();
        View.centerScreen(this);
        setIconImage(View.loadImage("deobfuscate16"));
    }

    public DeobfuscationLevel getDeobfuscationLevel() {
        if (removeTrapsRadioButton.isSelected()) {
            return DeobfuscationLevel.LEVEL_REMOVE_TRAPS;
        }
        return DeobfuscationLevel.LEVEL_REMOVE_DEAD_CODE;
    }

    public DeobfuscationScope getDeobfuscationScope() {
        if (methodScopeRadioButton.isSelected()) {
            return DeobfuscationScope.METHOD;
        }
        if (scriptScopeRadioButton.isSelected()) {
            return DeobfuscationScope.CLASS;
        }
        return DeobfuscationScope.SWF;
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            result = ERROR_OPTION;
        }

        super.setVisible(b);
    }

    private void okButtonActionPerformed(ActionEvent evt) {
        result = OK_OPTION;
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

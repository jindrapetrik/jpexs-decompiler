/*
 *  Copyright (C) 2010-2018 JPEXS
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
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/**
 *
 * @author JPEXS
 */
public class DeobfuscationDialog extends AppDialog {

    public JCheckBox processAllCheckbox = new JCheckBox(translate("processallclasses"));

    public JSlider codeProcessingLevel;

    private int result = ERROR_OPTION;

    @SuppressWarnings("unchecked")
    public DeobfuscationDialog() {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(new Dimension(330, 270));
        setTitle(translate("dialog.title"));
        Container cp = getContentPane();
        cp.setLayout(new BoxLayout(cp, BoxLayout.Y_AXIS));
        codeProcessingLevel = new JSlider(JSlider.VERTICAL, 1, 3, 3);
        codeProcessingLevel.setMajorTickSpacing(1);
        codeProcessingLevel.setPaintTicks(true);
        codeProcessingLevel.setMinorTickSpacing(1);
        codeProcessingLevel.setSnapToTicks(true);
        JLabel lab1 = new JLabel(translate("deobfuscation.level"));
        //lab1.setBounds(30, 0, getWidth() - 60, 25);
        lab1.setAlignmentX(0.5f);
        cp.add(lab1);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        //labelTable.put(LEVEL_NONE, new JLabel("None"));

        labelTable.put(DeobfuscationLevel.LEVEL_REMOVE_DEAD_CODE.getLevel(), new JLabel(translate("deobfuscation.removedeadcode")));
        labelTable.put(DeobfuscationLevel.LEVEL_REMOVE_TRAPS.getLevel(), new JLabel(translate("deobfuscation.removetraps")));
        labelTable.put(DeobfuscationLevel.LEVEL_RESTORE_CONTROL_FLOW.getLevel(), new JLabel(translate("deobfuscation.restorecontrolflow")));
        codeProcessingLevel.setLabelTable(labelTable);

        codeProcessingLevel.setPaintLabels(true);
        codeProcessingLevel.setAlignmentX(Component.CENTER_ALIGNMENT);
        //codeProcessingLevel.setSize(300, 200);

        //codeProcessingLevel.setBounds(30, 25, getWidth() - 60, 125);
        codeProcessingLevel.setAlignmentX(0.5f);
        add(codeProcessingLevel);
        //processAllCheckbox.setBounds(50, 150, getWidth() - 100, 25);
        processAllCheckbox.setAlignmentX(0.5f);
        add(processAllCheckbox);

        processAllCheckbox.setSelected(true);

        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(this::cancelButtonActionPerformed);
        JButton okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(this::okButtonActionPerformed);

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setAlignmentX(0.5f);
        cp.add(buttonsPanel);

        setModal(true);
        View.centerScreen(this);
        setIconImage(View.loadImage("deobfuscate16"));
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

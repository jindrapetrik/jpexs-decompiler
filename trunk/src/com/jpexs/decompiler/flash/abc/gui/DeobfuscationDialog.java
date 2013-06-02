/*
 *  Copyright (C) 2010-2013 JPEXS
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
package com.jpexs.decompiler.flash.abc.gui;

import com.jpexs.decompiler.flash.gui.View;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;

/**
 *
 * @author JPEXS
 */
public class DeobfuscationDialog extends JDialog implements ActionListener {

    public JCheckBox processAllCheckbox = new JCheckBox("Process all classes");
    public JSlider codeProcessingLevel;
    public boolean ok = false;
    public static final int LEVEL_REMOVE_DEAD_CODE = 1;
    public static final int LEVEL_REMOVE_TRAPS = 2;
    public static final int LEVEL_RESTORE_CONTROL_FLOW = 3;

    @SuppressWarnings("unchecked")
    public DeobfuscationDialog() {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(new Dimension(300, 270));
        setTitle("PCode deobfuscation");
        Container cp = getContentPane();
        cp.setLayout(null);
        codeProcessingLevel = new JSlider(JSlider.VERTICAL, 1, 3, 3);
        codeProcessingLevel.setMajorTickSpacing(1);
        codeProcessingLevel.setPaintTicks(true);
        codeProcessingLevel.setMinorTickSpacing(1);
        codeProcessingLevel.setSnapToTicks(true);
        JLabel lab1 = new JLabel("Code deobfuscation level:");
        lab1.setBounds(30, 0, getWidth() - 60, 25);
        cp.add(lab1);
        Hashtable labelTable = new Hashtable();
        //labelTable.put(new Integer(LEVEL_NONE), new JLabel("None"));
        
        labelTable.put(new Integer(LEVEL_REMOVE_DEAD_CODE), new JLabel("Remove dead code"));
        labelTable.put(new Integer(LEVEL_REMOVE_TRAPS), new JLabel("Remove traps"));
        labelTable.put(new Integer(LEVEL_RESTORE_CONTROL_FLOW), new JLabel("Restore control flow"));
        codeProcessingLevel.setLabelTable(labelTable);

        codeProcessingLevel.setPaintLabels(true);
        codeProcessingLevel.setAlignmentX(Component.CENTER_ALIGNMENT);
        codeProcessingLevel.setSize(300, 200);



        codeProcessingLevel.setBounds(30, 25, getWidth() - 60, 125);

        add(codeProcessingLevel);
        processAllCheckbox.setBounds(50, 150, getWidth() - 100, 25);
        add(processAllCheckbox);

        processAllCheckbox.setSelected(true);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setActionCommand("CANCEL");
        JButton okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setActionCommand("OK");
        okButton.setBounds(50, 200, 75, 25);
        cancelButton.setBounds(145, 200, 75, 25);
        cp.add(okButton);
        cp.add(cancelButton);
        setModal(true);
        View.centerScreen(this);
        View.setWindowIcon(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("OK")) {
            ok = true;
            setVisible(false);
        }
        if (e.getActionCommand().equals("CANCEL")) {
            ok = false;
            setVisible(false);
        }
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            ok = false;
        }
        super.setVisible(b);
    }
}

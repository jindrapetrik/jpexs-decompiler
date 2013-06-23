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
import com.jpexs.decompiler.flash.abc.RenameType;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author JPEXS
 */
public class RenameDialog extends JDialog implements ActionListener {

    private JRadioButton typeNumberRadioButton = new JRadioButton("Type + Number (class_27, method_456,...)");
    private JRadioButton randomWordRadioButton = new JRadioButton("Random word (abada, kof, supo, kosuri,...)");
    private JButton okButton = new JButton("OK");
    private JButton cancelButton = new JButton("Cancel");
    private boolean confirmed = false;

    public RenameType getRenameType() {
        if (!isConfirmed()) {
            return null;
        }
        if (typeNumberRadioButton.isSelected()) {
            return RenameType.TYPENUMBER;
        }
        return RenameType.RANDOMWORD;
    }

    public RenameDialog() {
        setSize(300, 150);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        int renameType = (Integer) Configuration.getConfig("lastRenameType", (Integer) 1);
        ButtonGroup group = new ButtonGroup();
        group.add(typeNumberRadioButton);
        group.add(randomWordRadioButton);
        JPanel pan = new JPanel();
        pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));
        pan.add(typeNumberRadioButton);
        pan.add(randomWordRadioButton);
        typeNumberRadioButton.setSelected(renameType == 1);
        randomWordRadioButton.setSelected(renameType == 2);
        setLayout(new BorderLayout());
        add(new JLabel("Rename type:"), BorderLayout.NORTH);
        add(pan, BorderLayout.CENTER);
        JPanel panButtons = new JPanel(new FlowLayout());
        panButtons.add(okButton);
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        panButtons.add(cancelButton);
        cancelButton.setActionCommand("CANCEL");
        cancelButton.addActionListener(this);
        add(panButtons, BorderLayout.SOUTH);
        setModalityType(ModalityType.APPLICATION_MODAL);
        View.centerScreen(this);
        View.setWindowIcon(this);
        setTitle("Rename Identifiers");
        getRootPane().setDefaultButton(okButton);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            confirmed = false;
        }
        super.setVisible(b);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "OK":
                confirmed = true;
                Configuration.setConfig("lastRenameType", (Integer) (getRenameType() == RenameType.TYPENUMBER ? 1 : 2));
                setVisible(false);
                break;
            case "CANCEL":
                confirmed = false;
                setVisible(false);
                break;
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public RenameType display() {
        setVisible(true);
        return getRenameType();
    }
}

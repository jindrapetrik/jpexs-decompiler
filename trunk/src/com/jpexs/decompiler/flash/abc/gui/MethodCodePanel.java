/*
 *  Copyright (C) 2011-2013 JPEXS
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

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.ConstantPool;
import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;

/**
 *
 * @author JPEXS
 */
public class MethodCodePanel extends JPanel implements ActionListener {

    private ASMSourceEditorPane sourceTextArea;
    public JPanel buttonsPanel;
    private JToggleButton hexButton;

    public void focusEditor() {
        sourceTextArea.requestFocusInWindow();
    }

    public String getTraitName() {
        return sourceTextArea.getName();
    }

    public void setIgnoreCarret(boolean ignoreCarret) {
        sourceTextArea.setIgnoreCarret(ignoreCarret);
    }

    public void hilighOffset(long offset) {
        sourceTextArea.hilighOffset(offset);
    }

    public void setBodyIndex(int bodyIndex, ABC abc) {
        sourceTextArea.setBodyIndex(bodyIndex, abc, sourceTextArea.getName());
    }

    public void setBodyIndex(int bodyIndex, ABC abc, String name) {
        sourceTextArea.setBodyIndex(bodyIndex, abc, name);
    }

    public int getBodyIndex() {
        return sourceTextArea.bodyIndex;
    }

    public void setCode(String text) {
        sourceTextArea.setText(text);
    }

    public boolean save(ConstantPool constants) {
        return sourceTextArea.save(constants);
    }

    public MethodCodePanel(DecompiledEditorPane decompiledEditor) {
        sourceTextArea = new ASMSourceEditorPane(decompiledEditor);

        setLayout(new BorderLayout());
        add(new JScrollPane(sourceTextArea), BorderLayout.CENTER);
        sourceTextArea.setContentType("text/flasm3");
        sourceTextArea.setFont(new Font("Monospaced", Font.PLAIN, sourceTextArea.getFont().getSize()));

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        JButton verifyButton = new JButton("Verify");
        verifyButton.setActionCommand("VERIFYBODY");
        verifyButton.addActionListener(this);

        JButton graphButton = new JButton(View.getIcon("graph16"));
        graphButton.setActionCommand("GRAPH");
        graphButton.addActionListener(this);
        graphButton.setToolTipText("View Graph");
        graphButton.setMargin(new Insets(3, 3, 3, 3));

        hexButton = new JToggleButton(View.getIcon("hex16"));
        hexButton.setActionCommand("HEX");
        hexButton.addActionListener(this);
        hexButton.setToolTipText("View Hex");
        hexButton.setMargin(new Insets(3, 3, 3, 3));

        JButton execButton = new JButton("Execute");
        execButton.setActionCommand("EXEC");
        execButton.addActionListener(this);

        buttonsPanel.add(graphButton);
        buttonsPanel.add(hexButton);
        buttonsPanel.add(new JPanel());
        // buttonsPanel.add(saveButton);
        // buttonsPan.add(execButton);

        add(buttonsPanel, BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Main.isWorking()) {
            return;
        }
        if (e.getActionCommand().equals("GRAPH")) {
            sourceTextArea.graph();
        }

        if (e.getActionCommand().equals("HEX")) {
            sourceTextArea.setHex(hexButton.isSelected());
        }

        if (e.getActionCommand().equals("EXEC")) {
            sourceTextArea.exec();
        }
    }

    public void setEditMode(boolean val) {
        if (val) {
            sourceTextArea.setHex(false);
        } else {
            if (hexButton.isSelected()) {
                sourceTextArea.setHex(true);
            }
        }

        sourceTextArea.setEditable(val);
        sourceTextArea.getCaret().setVisible(true);
        buttonsPanel.setVisible(!val);
    }
}

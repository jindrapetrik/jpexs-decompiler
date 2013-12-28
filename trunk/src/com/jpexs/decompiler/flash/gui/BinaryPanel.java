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
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.gui.abc.LineMarkedEditorPane;
import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public final class BinaryPanel extends JPanel implements ActionListener, ComponentListener {

    public LineMarkedEditorPane hexEditor = new LineMarkedEditorPane();
    private byte[] data;
    
    public BinaryPanel() {
        super(new BorderLayout());

        add(new JScrollPane(hexEditor), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
        addComponentListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public void setBinaryData(byte[] data) {
        this.data = data;
        hexEditor.setEditable(false);
        if (data != null) {
            int widthInChars = getWidth() / 7 - 3 - 11; // -3: scrollbar, -11: address in hex format
            int blockCount = widthInChars / 34;
            hexEditor.setFont(new Font("Monospaced", Font.PLAIN, hexEditor.getFont().getSize()));
            hexEditor.setContentType("text/plain");
            hexEditor.setText(Helper.byteArrayToHex(data, blockCount * 8));
            hexEditor.setCaretPosition(0);
        } else {
            hexEditor.setText("");
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        setBinaryData(data);
    }
    
    @Override
    public void componentMoved(ComponentEvent ce) {
    }

    @Override
    public void componentShown(ComponentEvent ce) {
    }

    @Override
    public void componentHidden(ComponentEvent ce) {
    }
}

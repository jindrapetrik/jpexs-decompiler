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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

public final class BinaryPanel extends JPanel implements ActionListener {

    public LineMarkedEditorPane hexEditor = new LineMarkedEditorPane();
    
    public BinaryPanel() {
        super(new BorderLayout());
        setOpaque(true);
        setBackground(View.DEFAULT_BACKGROUND_COLOR);
        add(hexEditor, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public void setBinaryData(byte[] data) {
        setBackground(View.swfBackgroundColor);
        hexEditor.setEditable(false);
        hexEditor.setText(Helper.byteArrayToHex(data, 32));
        //hexEditor.setContentType("text/plain"); //throws exception. why?
    }
}

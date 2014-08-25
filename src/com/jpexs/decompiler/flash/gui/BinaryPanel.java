/*
 *  Copyright (C) 2010-2014 JPEXS
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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public final class BinaryPanel extends JPanel implements ComponentListener {

    public HexView hexEditor = new HexView();
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

    public void setBinaryData(byte[] data) {
        this.data = data;
        if (data != null) {
            int widthInChars = getWidth() / 7 - 3 - 11; // -3: scrollbar, -11: address in hex format
            //int blockCount = widthInChars / 34;
            hexEditor.setData(data, null, null);
        } else {
            hexEditor.setData(new byte[0], null, null);
        }
        hexEditor.revalidate();
        hexEditor.repaint();
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

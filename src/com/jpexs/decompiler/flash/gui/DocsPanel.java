/*
 *  Copyright (C) 2016-2018 JPEXS
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

import com.jpexs.decompiler.flash.gui.abc.DocsListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class DocsPanel extends JPanel implements DocsListener {

    private final JEditorPane textDisplay = new JEditorPane();

    public DocsPanel() {
        setLayout(new BorderLayout(0, 0));
        JScrollPane sp = new JScrollPane(textDisplay);

        textDisplay.setMargin(new Insets(0, 0, 0, 0));
        add(sp, BorderLayout.CENTER);
        textDisplay.setContentType("text/html");
        textDisplay.setFocusable(false);
        textDisplay.setBackground(Color.white);
    }

    @Override
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public void docs(String identifier, String docs, Point screenLocation) {
        textDisplay.setText(docs);
    }

    @Override
    public void noDocs() {
        textDisplay.setText("<body></body>");
    }
}

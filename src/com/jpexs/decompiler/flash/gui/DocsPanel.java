/*
 * Copyright (C) 2016 Jindra
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

import com.jpexs.decompiler.flash.gui.abc.DocsListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Point;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class DocsPanel extends JPanel implements DocsListener {

    private JEditorPane textDisplay = new JEditorPane();
    //TODO: Make this use skin somehow (?)
    public static final Color HINT_COLOR = new Color(245, 245, 181);

    public DocsPanel() {
        setLayout(new BorderLayout());
        add(new JScrollPane(textDisplay), BorderLayout.CENTER);
        textDisplay.setContentType("text/html");
        textDisplay.setFocusable(false);
        textDisplay.setBackground(HINT_COLOR);
    }

    @Override
    public void docs(String identifier, String docs, Point screenLocation) {
        textDisplay.setText(docs.replace("\r\n", "<br />"));
    }

    @Override
    public void noDocs() {
        textDisplay.setText("");
    }

}

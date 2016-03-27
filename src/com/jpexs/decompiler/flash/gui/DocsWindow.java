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
import javax.swing.JRootPane;
import javax.swing.JScrollPane;

/**
 *
 * @author JPEXS
 */
public class DocsWindow extends JFrame implements DocsListener {

    private JEditorPane textDisplay = new JEditorPane();

    public DocsWindow() {
        setAlwaysOnTop(true);
        setSize(500, 250);
        setTitle("-");
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(textDisplay), BorderLayout.CENTER);
        textDisplay.setContentType("text/html");
        setAutoRequestFocus(false);
        textDisplay.setFocusable(false);
        View.setWindowIcon(this);
        this.getRootPane().setWindowDecorationStyle(JRootPane.PLAIN_DIALOG);
        textDisplay.setBackground(Color.white);
    }

    @Override
    public void docs(String identifier, String docs, Point screenLocation) {
        setTitle(identifier);
        textDisplay.setText(docs.replace("\r\n", "<br />"));
        if (screenLocation != null) {
            setLocation(screenLocation);
        }

        setFocusableWindowState(false);
        setVisible(true);
        View.execInEventDispatchLater(new Runnable() {
            @Override
            public void run() {
                setFocusableWindowState(true);
            }
        });

    }

    @Override
    public void noDocs() {
        setVisible(false);
        setTitle("-");
        textDisplay.setText("");
    }

}

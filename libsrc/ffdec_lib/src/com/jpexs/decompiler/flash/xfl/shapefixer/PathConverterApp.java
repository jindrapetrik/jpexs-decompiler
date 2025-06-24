/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.xfl.shapefixer;

import java.awt.Container;
import java.awt.FlowLayout;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * App that converts between FLA and SVG paths.
 *
 * @author JPEXS
 */
public class PathConverterApp {

    private static boolean updating = false;

    public static void main(String[] args) {
        JFrame fr = new JFrame();
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fr.setSize(800, 800);
        Container cnt = fr.getContentPane();
        cnt.setLayout(new FlowLayout());
        JTextArea t1 = new JTextArea(20, 50);
        JTextArea t2 = new JTextArea(20, 50);
        t1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                if (updating) {
                    return;
                }
                updating = true;
                //<Edge fillStyle0="0" fillStyle1="1" strokeStyle="0" edges="
                String newText = t1.getText();
                newText = newText.replaceAll(" *<Edge .* edges=\"", "");
                newText = newText.replaceAll("\"/>", "");

                StringBuffer resultString = new StringBuffer();
                Pattern regex = Pattern.compile("#([A-F0-9]+)\\.([A-F0-9]+)");
                Matcher m = regex.matcher(newText);
                while (m.find()) {
                    int p1 = Integer.parseInt(m.group(1), 16);
                    int p2 = Integer.parseInt(m.group(2), 16);

                    if ((p1 & 0x800000) > 0) {
                        p1 = 0xFF000000 | p1;
                    }

                    DecimalFormat df = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                    df.setGroupingUsed(false);
                    String strValue = "" + df.format((double) p1 + p2 / 256.0);
                    m.appendReplacement(resultString, strValue);
                }
                m.appendTail(resultString);

                newText = resultString.toString();

                t2.setText(newText.replace("!", "M").replace("|", "L").replace("[", "Q"));
                updating = false;
            }
        });
        t2.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                if (updating) {
                    return;
                }
                updating = true;
                t1.setText(t2.getText().replace("M", "!").replace("L", "|").replace("Q", "["));
                updating = false;
            }
        });
        cnt.add(new JScrollPane(t1));
        cnt.add(new JScrollPane(t2));
        fr.setVisible(true);
    }
}

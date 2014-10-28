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
package com.jpexs.decompiler.flash.gui.abc;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppStrings;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import jsyntaxpane.actions.ActionUtils;

/**
 *
 * @author JPEXS
 */
public class LineMarkedEditorPane extends UndoFixedEditorPane {

    private static final int truncateLimit = 8192;
    private int lastLine = -1;
    private boolean error = false;

    public int getLine() {
        return lastLine;
    }

    public void markError() {
        error = true;
    }

    public void gotoLine(int line) {
        setCaretPosition(ActionUtils.getDocumentPosition(this, line, 0));
    }

    public void selectLine(int line) {
        Document d = getDocument();
        String text = "";
        try {
            text = d.getText(0, d.getLength());
        } catch (BadLocationException ex) {
            //ignore
        }
        int lineCnt = 1;
        int lineStart = 0;
        int lineEnd = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineCnt++;
                if (lineCnt == line) {
                    lineStart = i + 1;
                }
                if (lineCnt == line + 1) {
                    lineEnd = i;
                }
            }
        }
        if (lineCnt == 1) {
            lineEnd = text.length() - 1;
            if (line > 1) {
                lineStart = text.length() - 1;
            }
        }

        select(lineStart, lineEnd);
        requestFocus();
    }

    public LineMarkedEditorPane() {
        setOpaque(false);
        addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                int caretPosition = getCaretPosition();
                Element root = getDocument().getDefaultRootElement();
                int currentLine = root.getElementIndex(caretPosition);
                if (currentLine != lastLine) {
                    lastLine = currentLine;
                    error = false;
                    repaint();
                }
            }
        });
    }

    @Override
    public void setText(String t) {
        lastLine = -1;
        error = false;
        if (Configuration.debugMode.get() && t.length() > truncateLimit) {
            t = t.substring(0, truncateLimit) + "\r\n" + AppStrings.translate("editorTruncateWarning").replace("%chars%", Integer.toString(truncateLimit));
        }
        super.setText(t);
        setCaretPosition(0); //scroll to top
    }

    @Override
    public void setText(String t, String contentType) {
        lastLine = -1;
        error = true;
        super.setText(t, contentType);
        setCaretPosition(0); //scroll to top
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (lastLine > 0) {
            FontMetrics fontMetrics = g.getFontMetrics();
            int lh = fontMetrics.getHeight();
            int a = fontMetrics.getAscent();
            int d = fontMetrics.getDescent();
            int h = a + d;
            int rH = h;
            if (error) {
                g.setColor(new Color(255, 200, 200));
            } else {
                g.setColor(new Color(0xee, 0xee, 0xee));
            }
            g.fillRect(0, d + lh * lastLine - 1, getWidth(), lh);            
        }
        super.paint(g);
    }
}

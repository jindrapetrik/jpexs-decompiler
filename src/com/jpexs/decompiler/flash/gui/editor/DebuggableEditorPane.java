/*
 *  Copyright (C) 2010-2018 JPEXS
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
package com.jpexs.decompiler.flash.gui.editor;

import com.jpexs.decompiler.flash.gui.Main;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Set;
import jsyntaxpane.components.BreakPointListener;
import jsyntaxpane.components.LineMarkerPainter;
import jsyntaxpane.components.LineNumbersBreakpointsRuler;

/**
 *
 * @author JPEXS
 */
public class DebuggableEditorPane extends LineMarkedEditorPane implements BreakPointListener, LineMarkerPainter {

    private static final Color BG_CURRENT_COLOR = new Color(0xd6, 0xe8, 0xe2);

    private static final Color BG_RULER_COLOR = new Color(0xe9, 0xe8, 0xe2);

    private static final Color BG_BREAKPOINT_COLOR = new Color(0xfc, 0x9d, 0x9f);

    private static final Color FG_BREAKPOINT_COLOR = null;

    private static final int PRIORITY_BREAKPOINT = 20;

    private static final Color BG_IP_COLOR = new Color(0xbd, 0xe6, 0xaa);

    private static final Color FG_IP_COLOR = null;

    private static final int PRIORITY_IP = 0;

    private static final Color BG_INVALID_BREAKPOINT_COLOR = new Color(0xdc, 0xdc, 0xd8);

    private static final Color FG_INVALID_BREAKPOINT_COLOR = null;

    private static final int PRIORITY_INVALID_BREAKPOINT = 10;

    public static final LineMarker BREAKPOINT_MARKER = new LineMarker(FG_BREAKPOINT_COLOR, BG_BREAKPOINT_COLOR, PRIORITY_BREAKPOINT);

    public static final LineMarker IP_MARKER = new LineMarker(FG_IP_COLOR, BG_IP_COLOR, PRIORITY_IP);

    public static final LineMarker INVALID_BREAKPOINT_MARKER = new LineMarker(FG_INVALID_BREAKPOINT_COLOR, BG_INVALID_BREAKPOINT_COLOR, PRIORITY_INVALID_BREAKPOINT);

    protected String scriptName = null;

    private LineNumbersBreakpointsRuler ruler;

    public synchronized void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    @Override
    public synchronized void toggled(int line) {
        if (scriptName == null) {
            return;
        }
        boolean on = Main.toggleBreakPoint(scriptName, line - firstLineOffset());
        removeColorMarker(line, INVALID_BREAKPOINT_MARKER);
        if (on) {
            if (Main.isBreakPointValid(scriptName, line - firstLineOffset())) {
                addColorMarker(line, BREAKPOINT_MARKER);
            } else {
                addColorMarker(line, INVALID_BREAKPOINT_MARKER);
            }
        } else {
            removeColorMarker(line, BREAKPOINT_MARKER);
            removeColorMarker(line, INVALID_BREAKPOINT_MARKER);
        }
    }

    public synchronized void refreshMarkers() {
        removeColorMarkerOnAllLines(BREAKPOINT_MARKER);
        removeColorMarkerOnAllLines(INVALID_BREAKPOINT_MARKER);
        removeColorMarkerOnAllLines(IP_MARKER);

        if (scriptName == null) {
            return;
        }

        Set<Integer> bkptLines = Main.getScriptBreakPoints(scriptName, false);

        for (int line : bkptLines) {
            if (Main.isBreakPointValid(scriptName, line)) {
                addColorMarker(line + firstLineOffset(), BREAKPOINT_MARKER);
            } else {
                addColorMarker(line + firstLineOffset(), INVALID_BREAKPOINT_MARKER);
            }
        }
        int ip = Main.getIp(scriptName);
        String ipPath = Main.getIpClass();
        if (ip > 0 && ipPath != null && ipPath.equals(scriptName)) {
            addColorMarker(ip + firstLineOffset(), IP_MARKER);
        }
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        refreshMarkers();
    }

    public String getScriptName() {
        return scriptName;
    }

    @Override
    public void paintLineMarker(Graphics g, int line, int x, int lineY, int textY, int lineHeight, boolean currentLine, int maxLines) {

        if (currentLine) {
            g.setColor(BG_CURRENT_COLOR);
        } else {
            g.setColor(View.getDefaultBackgroundColor());
        }
        int h = lineHeight;
        if (line == 1) {
            h += lineY;
        }
        if (line == maxLines) {
            h = getHeight() - lineY;
        }
        g.fillRect(0, line == 1 ? 0 : lineY, getWidth(), h);

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ((Graphics2D) g).setStroke(new BasicStroke(0.5f));

        boolean drawText = true;
        if (hasColorMarker(line, INVALID_BREAKPOINT_MARKER)) {
            g.setColor(BG_INVALID_BREAKPOINT_COLOR);
            g.fillOval(x + 5, textY - 10, 10, 10);
            g.setColor(Color.black);
            g.drawOval(x + 5, textY - 10, 10, 10);
            drawText = false;
        } else if (hasColorMarker(line, BREAKPOINT_MARKER)) {
            g.setColor(BG_BREAKPOINT_COLOR);
            g.fillOval(x + 5, textY - 10, 10, 10);
            g.setColor(Color.black);
            g.drawOval(x + 5, textY - 10, 10, 10);
            drawText = false;
        }
        if (hasColorMarker(line, IP_MARKER)) {
            int mx = x + 10;
            g.setColor(BG_IP_COLOR);
            g.fillPolygon(new int[]{mx, mx + 10, mx}, new int[]{textY - 10, textY - 5, textY}, 3);
            g.setColor(Color.black);
            g.drawPolygon(new int[]{mx, mx + 10, mx}, new int[]{textY - 10, textY - 5, textY}, 3);
            drawText = false;
        }
        if (drawText) {
            g.setColor(getForeground());
            g.drawString("" + line, x, textY);
        }

    }

    @Override
    public void installLineMarker(LineNumbersBreakpointsRuler ruler) {
        this.ruler = ruler;
    }

    @Override
    public void addColorMarker(int line, LineMarker lm) {
        super.addColorMarker(line, lm);
        ruler.repaint();
    }

    @Override
    public void removeColorMarker(int line, LineMarker lm) {
        super.removeColorMarker(line, lm);
        ruler.repaint();
    }
}

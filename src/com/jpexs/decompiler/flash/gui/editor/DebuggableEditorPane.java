/*
 *  Copyright (C) 2010-2025 JPEXS
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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.gui.Main;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import javax.swing.UIManager;
import jsyntaxpane.components.BreakPointListener;
import jsyntaxpane.components.LineMarkerPainter;
import jsyntaxpane.components.LineNumbersBreakpointsRuler;

/**
 * @author JPEXS
 */
public class DebuggableEditorPane extends LineMarkedEditorPane implements BreakPointListener, LineMarkerPainter {

    private static final Color BG_CURRENT_COLOR = new Color(0xd6, 0xe8, 0xe2);

    private static final Color BG_RULER_COLOR = new Color(0xe9, 0xe8, 0xe2);

    private static Color BG_BREAKPOINT_COLOR = new Color(0xfc, 0x9d, 0x9f);

    private static final Color BG_STACK_COLOR = new Color(0xe7, 0xe1, 0xef);

    private static final Color FG_BREAKPOINT_COLOR = null;

    private static final int PRIORITY_BREAKPOINT = 20;

    private static final Color BG_IP_COLOR = new Color(0xbd, 0xe6, 0xaa);

    private static final Color FG_IP_COLOR = null;

    private static final Color FG_STACK_COLOR = null;

    private static final int PRIORITY_STACK = 30;

    private static final int PRIORITY_IP = 0;

    private static final Color BG_INVALID_BREAKPOINT_COLOR = new Color(0xdc, 0xdc, 0xd8);

    private static final Color FG_INVALID_BREAKPOINT_COLOR = null;

    private static final int PRIORITY_INVALID_BREAKPOINT = 10;

    public static LineMarker BREAKPOINT_MARKER = new LineMarker(FG_BREAKPOINT_COLOR, BG_BREAKPOINT_COLOR, PRIORITY_BREAKPOINT);

    public static final LineMarker IP_MARKER = new LineMarker(FG_IP_COLOR, BG_IP_COLOR, PRIORITY_IP);

    public static final LineMarker INVALID_BREAKPOINT_MARKER = new LineMarker(FG_INVALID_BREAKPOINT_COLOR, BG_INVALID_BREAKPOINT_COLOR, PRIORITY_INVALID_BREAKPOINT);

    public static final LineMarker STACK_MARKER = new LineMarker(FG_STACK_COLOR, BG_STACK_COLOR, PRIORITY_STACK);

    protected String scriptName = null;

    protected String breakPointScriptName = null;

    private LineNumbersBreakpointsRuler ruler;

    private boolean showMarkers = true;
    

    public DebuggableEditorPane() {

        Color editorBackground = UIManager.getColor("EditorPane.background");
        int light = (editorBackground.getRed() + editorBackground.getGreen() + editorBackground.getBlue()) / 3;
        if (light < 128) {
            BG_BREAKPOINT_COLOR = new Color(0x88, 0x00, 0x00);
            BREAKPOINT_MARKER = new LineMarker(FG_BREAKPOINT_COLOR, BG_BREAKPOINT_COLOR, PRIORITY_BREAKPOINT);
        }
    }
    
    public synchronized void setScriptName(String scriptName, String breakPointScriptName) {
        this.scriptName = scriptName;
        this.breakPointScriptName = breakPointScriptName;
    }

    @Override
    public synchronized void toggled(int line) {
        if (breakPointScriptName == null) {
            return;
        }
        boolean on = Main.toggleBreakPoint(breakPointScriptName, line - firstLineOffset());
        removeColorMarker(line, INVALID_BREAKPOINT_MARKER);
        if (on) {
            if (Main.isBreakPointValid(breakPointScriptName, line - firstLineOffset())) {
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
        removeColorMarkerOnAllLines(STACK_MARKER);

        if (breakPointScriptName == null) {
            return;
        }
        if (!showMarkers) {
            return;
        }

        Set<Integer> bkptLines = Main.getScriptBreakPoints(breakPointScriptName, false);

        for (int line : bkptLines) {
            if (Main.isBreakPointValid(breakPointScriptName, line)) {
                addColorMarker(line + firstLineOffset(), BREAKPOINT_MARKER);
            } else {
                addColorMarker(line + firstLineOffset(), INVALID_BREAKPOINT_MARKER);
            }
        }
        int ip = Main.getIp(breakPointScriptName);
        String ipPath = Main.getIpClass();
        String ipHash = "main";
        if (ipPath != null && ipPath.contains(":")) {
            ipHash = ipPath.substring(0, ipPath.indexOf(":"));
            ipPath = ipPath.substring(ipPath.indexOf(":") + 1);
        }
        String myhash = Main.getSwfHash(Main.getMainFrame().getPanel().getCurrentSwf());
        if (ip > 0 && ipPath != null && ipHash.equals(myhash) && ipPath.equals(breakPointScriptName)) {
            addColorMarker(ip + firstLineOffset(), IP_MARKER);
        }
        List<Integer> stackLines = Main.getStackLines();
        List<String> stackClasses = Main.getStackClasses();
        for (int i = 1; i < stackClasses.size(); i++) {
            String cls = stackClasses.get(i);
            String clsHash = "main";
            if (cls.contains(":")) {
                clsHash = cls.substring(0, cls.indexOf(":"));
                cls = cls.substring(cls.indexOf(":") + 1);
            }
            int line = stackLines.get(i);
            if (clsHash.equals(myhash) && cls.equals(breakPointScriptName)) {
                addColorMarker(line + firstLineOffset(), STACK_MARKER);
            }
        }
    }

    public void setShowMarkers(boolean showMarkers) {
        this.showMarkers = showMarkers;
    }

    public boolean isShowMarkers() {
        return showMarkers;
    }

    @Override
    public void setText(String t) {
        super.setText(t);
        refreshMarkers();
    }

    public String getScriptName() {
        return scriptName;
    }

    public String getBreakPointScriptName() {
        return breakPointScriptName;
    }

    @Override
    public void paintLineMarker(Graphics g, int line, int x, int lineY, int textY, int lineHeight, boolean currentLine, int maxLines) {

        if (currentLine) {
            g.setColor(UIManager.getColor("List.selectionBackground"));
        } else {
            g.setColor(UIManager.getColor("Panel.background"));
        }
        int h = lineHeight;
        if (line == 1) {
            h += lineY;
        }
        /*if (line == maxLines) {
            h = getHeight() - lineY;
        }*/
        g.fillRect(0, line == 1 ? 0 : lineY, getWidth(), h);

        if (line == maxLines) {
            g.setColor(UIManager.getColor("Panel.background"));
            g.fillRect(0, line == 1 ? 0 : lineY + h, getWidth(), getHeight() - lineY - lineHeight);
        }

        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        ((Graphics2D) g).setStroke(new BasicStroke(0.5f));

        if (hasColorMarker(line, STACK_MARKER)) {
            g.setColor(BG_STACK_COLOR);
            g.fillPolygon(new int[]{x + 5, x + 15, x + 15}, new int[]{textY, textY, textY - 10}, 3);
            g.setColor(Color.black);
            g.drawPolygon(new int[]{x + 5, x + 15, x + 15}, new int[]{textY, textY, textY - 10}, 3);
        }
        if (hasColorMarker(line, INVALID_BREAKPOINT_MARKER)) {
            g.setColor(BG_INVALID_BREAKPOINT_COLOR);
            g.fillOval(x + 5, textY - 10, 10, 10);
            g.setColor(Color.black);
            g.drawOval(x + 5, textY - 10, 10, 10);
        } else if (hasColorMarker(line, BREAKPOINT_MARKER)) {
            g.setColor(BG_BREAKPOINT_COLOR);
            g.fillOval(x + 5, textY - 10, 10, 10);
            g.setColor(Color.black);
            g.drawOval(x + 5, textY - 10, 10, 10);
        }
        if (hasColorMarker(line, IP_MARKER)) {
            int mx = x + 10;
            g.setColor(BG_IP_COLOR);
            g.fillPolygon(new int[]{mx, mx + 10, mx}, new int[]{textY - 10, textY - 5, textY}, 3);
            g.setColor(Color.black);
            g.drawPolygon(new int[]{mx, mx + 10, mx}, new int[]{textY - 10, textY - 5, textY}, 3);
        }
        if (currentLine) {
            g.setColor(UIManager.getColor("List.selectionForeground"));
        } else {
            g.setColor(UIManager.getColor("Panel.foreground"));
        }
        g.drawString("" + line, x + 16 + 5, textY);
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

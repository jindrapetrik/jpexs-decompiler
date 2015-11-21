/*
 * Copyright (C) 2015 JPEXS
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
package com.jpexs.decompiler.flash.gui.editor;

import com.jpexs.decompiler.flash.gui.Main;
import java.awt.Color;
import java.util.Set;
import jsyntaxpane.components.BreakPointListener;

/**
 *
 * @author JPEXS
 */
public class DebuggableEditorPane extends LineMarkedEditorPane implements BreakPointListener {

    public static final Color BG_BREAKPOINT_COLOR = new Color(0xfc, 0x9d, 0x9f);
    public static final Color FG_BREAKPOINT_COLOR = null;
    public static final int PRIORITY_BREAKPOINT = 20;

    public static final Color BG_IP_COLOR = new Color(0xbd, 0xe6, 0xaa);
    public static final Color FG_IP_COLOR = null;
    public static final int PRIORITY_IP = 0;

    public static final Color BG_INVALID_BREAKPOINT_COLOR = new Color(0xdc, 0xdc, 0xd8);
    public static final Color FG_INVALID_BREAKPOINT_COLOR = null;
    public static final int PRIORITY_INVALID_BREAKPOINT = 10;

    protected String scriptName = null;

    public synchronized void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    @Override
    public synchronized void toggled(int line) {
        if (scriptName == null) {
            return;
        }
        boolean on = Main.toggleBreakPoint(scriptName, line);
        removeColorMarker(line, FG_INVALID_BREAKPOINT_COLOR, BG_INVALID_BREAKPOINT_COLOR, PRIORITY_INVALID_BREAKPOINT);
        if (on) {
            if (Main.isBreakPointValid(scriptName, line)) {
                addColorMarker(line, FG_BREAKPOINT_COLOR, BG_BREAKPOINT_COLOR, PRIORITY_BREAKPOINT);
            } else {
                addColorMarker(line, FG_INVALID_BREAKPOINT_COLOR, BG_INVALID_BREAKPOINT_COLOR, PRIORITY_INVALID_BREAKPOINT);
            }
        } else {
            removeColorMarker(line, FG_BREAKPOINT_COLOR, BG_BREAKPOINT_COLOR, PRIORITY_BREAKPOINT);
            removeColorMarker(line, FG_INVALID_BREAKPOINT_COLOR, BG_INVALID_BREAKPOINT_COLOR, PRIORITY_INVALID_BREAKPOINT);
        }
    }

    public synchronized void refreshMarkers() {
        removeColorMarkerOnAllLines(FG_BREAKPOINT_COLOR, BG_BREAKPOINT_COLOR, PRIORITY_BREAKPOINT);
        removeColorMarkerOnAllLines(FG_INVALID_BREAKPOINT_COLOR, BG_INVALID_BREAKPOINT_COLOR, PRIORITY_INVALID_BREAKPOINT);
        removeColorMarkerOnAllLines(FG_IP_COLOR, BG_IP_COLOR, PRIORITY_IP);

        if (scriptName == null) {
            return;
        }

        Set<Integer> bkptLines = Main.getScriptBreakPoints(scriptName);

        for (int line : bkptLines) {
            if (Main.isBreakPointValid(scriptName, line)) {
                addColorMarker(line, FG_BREAKPOINT_COLOR, BG_BREAKPOINT_COLOR, PRIORITY_BREAKPOINT);
            } else {
                addColorMarker(line, FG_INVALID_BREAKPOINT_COLOR, BG_INVALID_BREAKPOINT_COLOR, PRIORITY_INVALID_BREAKPOINT);
            }
        }
        int ip = Main.getIp(scriptName);
        String ipPath = Main.getIpClass();
        if (ip > 0 && ipPath != null && ipPath.equals(scriptName)) {
            addColorMarker(ip, FG_IP_COLOR, BG_IP_COLOR, PRIORITY_IP);
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

}

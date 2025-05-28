/*
 * Copyright (C) 2025 JPEXS
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JLayer;
import javax.swing.JScrollBar;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.ScrollBarUI;
import jsyntaxpane.actions.ActionUtils;

/**
 *
 * @author JPEXS
 */
public class ScrollbarOverlay extends LayerUI<JScrollBar> {

    private final LineMarkedEditorPane editorPane;
    private final Map<Integer, Color> markedLines = new LinkedHashMap<>();

    public ScrollbarOverlay(LineMarkedEditorPane editorPane) {
        this.editorPane = editorPane;
    }

    public void removeMarkers(Color color) {
        Set<Integer> lines = new HashSet<>(markedLines.keySet());
        for (int key : lines) {
            if (markedLines.get(key).equals(color)) {
                markedLines.remove(key);
            }
        }
    }

    public void clearMarkers() {
        markedLines.clear();
    }

    public void addMarker(int line, Color color) {
        markedLines.put(line, color);
    }

    public void setMarkedLines(Map<Integer, Color> markedLines) {
        this.markedLines.clear();
        this.markedLines.putAll(markedLines);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        JScrollBar bar = (JScrollBar) ((JLayer<?>) c).getView();
        ScrollBarUI ui = bar.getUI();
        Rectangle r;
        if (ui instanceof TrackRectSubstanceScrollbarUI) {
            r = ((TrackRectSubstanceScrollbarUI) ui).getTrackBounds();
        } else {
            r = new Rectangle(0, 16, 16, bar.getHeight() - 32);
        }

        int totalLineCount = ActionUtils.getLineCount(editorPane);
        for (Map.Entry<Integer, Color> entry : markedLines.entrySet()) {
            g.setColor(entry.getValue());

            float ratio = (float) entry.getKey() / totalLineCount;
            int y = r.y + (int) (ratio * r.height);
            g.drawLine(0, y, bar.getWidth(), y);
        }
    }
}

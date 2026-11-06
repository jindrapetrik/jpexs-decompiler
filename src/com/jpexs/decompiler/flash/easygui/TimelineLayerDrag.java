/*
 *  Copyright (C) 2026 JPEXS
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
package com.jpexs.decompiler.flash.easygui;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.dnd.DragSource;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/** Drag insertion shared by the display-list and shape timelines. */
final class TimelineLayerDrag extends MouseAdapter {

    private static final int ROW_HEIGHT = TimelineBodyPanel.FRAME_HEIGHT;
    private final TimelineDepthPanel depths;
    private final JScrollPane scroll;
    private final int firstRow;
    private final IntConsumer showFrameMarker;
    private final BiConsumer<Integer, Integer> moveLayer;
    private int sourceDepth;
    private int startY;
    private boolean dragged;

    TimelineLayerDrag(TimelineDepthPanel depths, JScrollPane scroll, int firstRow, IntConsumer showFrameMarker,
            BiConsumer<Integer, Integer> moveLayer) {
        this.depths = depths;
        this.scroll = scroll;
        this.firstRow = firstRow;
        this.showFrameMarker = showFrameMarker;
        this.moveLayer = moveLayer;
        depths.addMouseListener(this);
        depths.addMouseMotionListener(this);
        depths.setAutoscrolls(true);
    }

    boolean wasDragged() {
        return dragged;
    }

    void cancel() {
        sourceDepth = 0;
        depths.setDropRow(-1);
        showFrameMarker.accept(-1);
    }

    @Override
    public void mousePressed(MouseEvent event) {
        cancel();
        dragged = false;
        if (SwingUtilities.isLeftMouseButton(event) && depths.getDepthCount() > 1 && !depths.isOverControls(event)) {
            sourceDepth = depths.getDepthAt(event);
            startY = viewportPoint(event).y;
        }
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        int y = viewportPoint(event).y;
        if (sourceDepth == 0 || (!dragged && Math.abs(y - startY) < DragSource.getDragThreshold())) {
            return;
        }
        dragged = true;
        scrollDuringDrag(y);
        int row = firstRow + insertionRow(y);
        depths.setDropRow(row);
        showFrameMarker.accept(row);
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        int source = sourceDepth;
        cancel();
        if (source == 0 || !dragged || !SwingUtilities.isLeftMouseButton(event)) {
            return;
        }
        Point point = viewportPoint(event);
        boolean insideColumns = point.x >= 0 && point.x < scroll.getViewport().getWidth()
                || event.getX() >= 0 && event.getX() < depths.getWidth();
        if (!insideColumns || point.y < 0 || point.y >= scroll.getViewport().getHeight()) {
            return;
        }
        int count = depths.getDepthCount();
        int sourceRow = count - source;
        int row = insertionRow(point.y);
        int destination = count - (row > sourceRow ? row - 1 : row);
        if (source != destination) {
            moveLayer.accept(source, destination);
        }
    }

    private Point viewportPoint(MouseEvent event) {
        return SwingUtilities.convertPoint(depths, event.getPoint(), scroll.getViewport());
    }

    private int insertionRow(int y) {
        int row = (y + scroll.getVerticalScrollBar().getValue() + ROW_HEIGHT / 2) / ROW_HEIGHT - firstRow;
        return Math.max(0, Math.min(depths.getDepthCount(), row));
    }

    private void scrollDuringDrag(int y) {
        JScrollBar bar = scroll.getVerticalScrollBar();
        if (y < ROW_HEIGHT / 2) {
            bar.setValue(bar.getValue() - ROW_HEIGHT);
        } else if (y >= scroll.getViewport().getHeight() - ROW_HEIGHT / 2) {
            bar.setValue(bar.getValue() + ROW_HEIGHT);
        }
    }

    static void paintDropLine(Graphics g, int width, int y) {
        g.setColor(TimelineBodyPanel.SELECTED_BORDER_COLOR);
        g.fillRect(0, Math.max(0, y - 1), width, 2);
    }
}

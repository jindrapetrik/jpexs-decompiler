/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.gui.ImagePanel;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.function.BiConsumer;

/**
 * Multi-click Pen input, with a detached preview and one commit per path.
 *
 * @author JPEXS
 */
public final class BezierPenTool implements ImagePanel.ShapeDrawListener {

    private final ImagePanel panel;
    private final BiConsumer<BezierPen, Boolean> draw;
    private BezierPen path;

    public BezierPenTool(ImagePanel panel, BiConsumer<BezierPen, Boolean> draw) {
        this.panel = panel;
        this.draw = draw;
    }

    @Override
    public void begin(Point2D point) {
        begin(point, 0, 1);
    }

    @Override
    public void begin(Point2D point, int modifiers, int clickCount) {
        if (path == null) {
            path = new BezierPen();
        }
        path.press(point, modifiers, clickCount, panel.getTimelinedToPanelTransform());
        if (path.isFinished()) {
            commit();
        } else {
            refresh();
        }
    }

    @Override
    public void update(Point2D point, boolean finished) {
        update(point, finished, 0);
    }

    @Override
    public void update(Point2D point, boolean finished, int modifiers) {
        if (path == null) {
            return;
        }
        if (finished) {
            path.release(point, modifiers, panel.getTimelinedToPanelTransform());
        } else {
            path.drag(point, modifiers, panel.getTimelinedToPanelTransform());
        }
        if (path.isFinished()) {
            commit();
        } else {
            refresh();
        }
    }

    @Override
    public void move(Point2D point, int modifiers) {
        if (path != null) {
            path.move(point, modifiers, panel.getTimelinedToPanelTransform());
            refresh();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int modifiers) {
        if (!isPending()) {
            return false;
        }
        if (keyCode == KeyEvent.VK_ENTER) {
            finish((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0);
            return true;
        }
        if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE
                || keyCode == KeyEvent.VK_Z && (modifiers & InputEvent.CTRL_DOWN_MASK) != 0
                        && (modifiers & InputEvent.SHIFT_DOWN_MASK) == 0) {
            path.removeLastNode();
            if (path.isEmpty()) {
                cancel();
            } else {
                refresh();
            }
            return true;
        }
        return false;
    }

    public void finish(boolean close) {
        if (path != null) {
            path.finish(close);
            commit();
        }
    }

    private void commit() {
        BezierPen completed = path;
        path = null;
        panel.setShapeDrawingGuide(null);
        panel.setShapePreview(null);
        draw.accept(completed, true);
    }

    public void refresh() {
        if (path != null) {
            panel.setShapeDrawingGuide(path.getGuide(panel.getTimelinedToPanelTransform()), true);
            draw.accept(path, false);
        }
    }

    @Override
    public boolean isPending() {
        return path != null && !path.isEmpty();
    }

    @Override
    public void cancel() {
        path = null;
        panel.setShapeDrawingGuide(null);
        panel.setShapePreview(null);
    }
}

/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui.properties.panels;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;

/**
 * Shares the last drawing paint between tools without changing selected geometry.
 *
 * @author JPEXS
 */
public class DrawingStyleSettings {

    private final BrushPropertiesPanel brush;
    private final ShapePropertiesPanel pencil;
    private final DrawingShapesPropertiesPanel shapes;
    private final ShapePropertiesPanel paint;
    private final ShapePropertiesPanel ink;
    private final ShapePropertiesPanel pen;
    private final Runnable changeListener;
    private SWF swf;

    public DrawingStyleSettings(BrushPropertiesPanel brush, ShapePropertiesPanel pencil,
            DrawingShapesPropertiesPanel shapes, ShapePropertiesPanel paint, ShapePropertiesPanel ink,
            ShapePropertiesPanel pen, Runnable changeListener) {
        this.brush = brush;
        this.pencil = pencil;
        this.shapes = shapes;
        this.paint = paint;
        this.ink = ink;
        this.pen = pen;
        this.changeListener = changeListener;
        brush.setFillChangeListener(fill -> shareFill(fill, null, false));
        pencil.setPropertyChangeListener((stroke, property) -> changed(pencil, stroke, property));
        paint.setPropertyChangeListener((stroke, property) -> changed(paint, stroke, property));
        ink.setPropertyChangeListener((stroke, property) -> changed(ink, stroke, property));
        pen.setPropertyChangeListener((stroke, property) -> changed(pen, stroke, property));
        shapes.setStyleChangeListener((stroke, property) -> changed(shapes.getStyleSettings(), stroke, property));
        shareFill(paint.getFillStyle(null), paint);
        shareStroke(ink.getDrawingStrokeStyle(), ink);
    }

    private void changed(ShapePropertiesPanel source, boolean stroke, String property) {
        if (!source.isPropertyChangeValid(stroke, property)) {
            source.setDrawingSwf(swf);
        }
        if (stroke) {
            shareStroke(source.getDrawingStrokeStyle(), source);
        } else {
            shareFill(source.getFillStyle(null), source);
        }
        if (source != shapes.getStyleSettings()) {
            changeListener.run();
        }
    }

    private void shareFill(FILLSTYLE fill, ShapePropertiesPanel source) {
        shareFill(fill, source, true);
    }

    private void shareFill(FILLSTYLE fill, ShapePropertiesPanel source, boolean updateBrush) {
        if (updateBrush) {
            brush.setFillStyle(fill);
        }
        if (source != paint) {
            paint.setDrawingFillStyle(fill);
        }
        if (source != shapes.getStyleSettings()) {
            shapes.getStyleSettings().setDrawingFillStyle(fill);
        }
        if (source != pen) {
            pen.setDrawingFillStyle(fill);
        }
    }

    private void shareStroke(LINESTYLE2 line, ShapePropertiesPanel source) {
        for (ShapePropertiesPanel panel : new ShapePropertiesPanel[]{pencil, ink, shapes.getStyleSettings(), pen}) {
            if (panel != source) {
                panel.setDrawingStrokeStyle(line);
            }
        }
    }

    public void setSwf(SWF swf) {
        this.swf = swf;
        brush.setSwf(swf);
        pencil.setDrawingSwf(swf);
        shapes.setSwf(swf);
        paint.setDrawingSwf(swf);
        ink.setDrawingSwf(swf);
        pen.setDrawingSwf(swf);
        shareFill(paint.getFillStyle(null), paint);
        shareStroke(ink.getDrawingStrokeStyle(), ink);
    }

    /**
     * Brush cannot draw with None, so restore the last nonempty shared fill.
     */
    public void activateBrush() {
        if (paint.getFillStyle(null) == null) {
            shareFill(brush.getFillStyle(null), null);
        }
    }

    public void setSampledFill(FILLSTYLE fill) {
        shareFill(fill, null);
    }

    public void setSampledStroke(ILINESTYLE line) {
        shareStroke(line instanceof LINESTYLE2 ? (LINESTYLE2) line : ((LINESTYLE) line).toLineStyle2(), null);
    }
}

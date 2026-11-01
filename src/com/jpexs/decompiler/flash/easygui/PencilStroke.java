/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.types.LINESTYLE2;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * One open Pencil path in shape-local twips. Consecutive duplicate samples are
 * discarded after rounding to the coordinates stored in the SWF.
 *
 * @author JPEXS
 */
public final class PencilStroke {

    private final Path2D.Double path = new Path2D.Double();

    public PencilStroke(Point2D start) {
        path.moveTo(Math.rint(start.getX()), Math.rint(start.getY()));
    }

    public void add(Point2D point) {
        double x = Math.rint(point.getX());
        double y = Math.rint(point.getY());
        if (path.getCurrentPoint().getX() != x || path.getCurrentPoint().getY() != y) {
            path.lineTo(x, y);
        }
    }

    public Path2D.Double getPath() {
        return new Path2D.Double(path);
    }

    /**
     * A circular width indicator using the same scale modes as the renderer.
     * Rotation and skew affect the width, while the indicator remains circular.
     */
    public static Shape cursor(Point2D point, LINESTYLE2 line, AffineTransform placement, double minimumWidth) {
        double width = ShapeSelection.strokeWidth(line, placement);
        return BrushShape.CIRCLE.tip(point, Math.max(minimumWidth, width), placement);
    }
}

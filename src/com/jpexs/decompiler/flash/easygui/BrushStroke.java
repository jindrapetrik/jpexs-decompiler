/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * Accumulates one filled brush gesture without gaps or overlapping paint layers.
 *
 * @author JPEXS
 */
public final class BrushStroke {

    private final BrushShape shape;
    private final double size;
    private final AffineTransform shapeToBrush;
    private final AffineTransform brushToShape;
    private final Area area = new Area();
    private Point2D last;

    public BrushStroke(BrushShape shape, double size, Point2D start) {
        this(shape, size, start, new AffineTransform());
    }

    /**
     * Draws in stage axes and maps the result back to the edited shape.
     * Instance rotation, skew and scale must not distort the brush tip.
     */
    public BrushStroke(BrushShape shape, double size, Point2D start, AffineTransform shapeToBrush) {
        if (!Double.isFinite(size) || size <= 0) {
            throw new IllegalArgumentException("Brush size must be positive and finite");
        }
        this.shape = shape;
        this.size = size;
        this.shapeToBrush = new AffineTransform(shapeToBrush);
        try {
            brushToShape = shapeToBrush.createInverse();
        } catch (NoninvertibleTransformException ex) {
            throw new IllegalArgumentException("Shape placement must be invertible", ex);
        }
        last = shapeToBrush.transform(start, null);
        area.add(new Area(shape.tip(last, size)));
    }

    public void add(Point2D point) {
        Point2D brushPoint = shapeToBrush.transform(point, null);
        if (!last.equals(brushPoint)) {
            area.add(new Area(shape.sweep(last, brushPoint, size)));
            last = brushPoint;
        }
    }

    public Area getArea() {
        return new Area(brushToShape.createTransformedShape(area));
    }
}

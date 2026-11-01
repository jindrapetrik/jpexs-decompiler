/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Convex brush tips and their continuous sweep between two mouse samples.
 * Sizes and coordinates use twips; placed shapes are mapped through stage axes.
 *
 * @author JPEXS
 */
public enum BrushShape {
    CIRCLE(1, 1, true, 0),
    HORIZONTAL_OVAL(1, 0.35, true, 0),
    VERTICAL_OVAL(0.35, 1, true, 0),
    SQUARE(1, 1, false, 0),
    HORIZONTAL_RECTANGLE(1, 0.45, false, 0),
    VERTICAL_RECTANGLE(0.45, 1, false, 0),
    DIAGONAL_DOWN(1, 0.06, false, Math.PI / 4),
    DIAGONAL_UP(1, 0.06, false, -Math.PI / 4);

    private final double width;
    private final double height;
    private final boolean ellipse;
    private final double angle;

    BrushShape(double width, double height, boolean ellipse, double angle) {
        this.width = width;
        this.height = height;
        this.ellipse = ellipse;
        this.angle = angle;
    }

    public Shape tip(Point2D center, double size) {
        double scale = angle == 0 ? size : size / ((width + height) / Math.sqrt(2));
        Shape tip = ellipse ? new Ellipse2D.Double(-width / 2, -height / 2, width, height)
                : new Rectangle2D.Double(-width / 2, -height / 2, width, height);
        AffineTransform transform = AffineTransform.getTranslateInstance(center.getX(), center.getY());
        transform.rotate(angle);
        transform.scale(scale, scale);
        return transform.createTransformedShape(tip);
    }

    /**
     * Keeps the tip aligned with the stage while editing a placed shape.
     */
    public Shape tip(Point2D center, double size, AffineTransform shapeToBrush) {
        try {
            Point2D brushCenter = shapeToBrush.transform(center, null);
            return shapeToBrush.createInverse().createTransformedShape(tip(brushCenter, size));
        } catch (NoninvertibleTransformException ex) {
            throw new IllegalArgumentException("Shape placement must be invertible", ex);
        }
    }

    public Shape sweep(Point2D from, Point2D to, double size) {
        if (from.equals(to)) {
            return tip(to, size);
        }
        if (ellipse) {
            double rx = width * size / 2;
            double ry = height * size / 2;
            Shape path = new Line2D.Double(0, 0, (to.getX() - from.getX()) / rx, (to.getY() - from.getY()) / ry);
            Shape swept = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND).createStrokedShape(path);
            return new AffineTransform(rx, 0, 0, ry, from.getX(), from.getY()).createTransformedShape(swept);
        }
        List<Point2D> points = new ArrayList<>();
        for (Point2D center : new Point2D[]{from, to}) {
            double[] coords = new double[6];
            for (PathIterator it = tip(center, size).getPathIterator(null); !it.isDone(); it.next()) {
                if (it.currentSegment(coords) != PathIterator.SEG_CLOSE) {
                    points.add(new Point2D.Double(coords[0], coords[1]));
                }
            }
        }
        points.sort(Comparator.comparingDouble(Point2D::getX).thenComparingDouble(Point2D::getY));
        List<Point2D> hull = new ArrayList<>();
        for (Point2D point : points) {
            while (hull.size() >= 2 && cross(hull.get(hull.size() - 2), hull.get(hull.size() - 1), point) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(point);
        }
        int lower = hull.size();
        for (int i = points.size() - 2; i >= 0; i--) {
            Point2D point = points.get(i);
            while (hull.size() > lower && cross(hull.get(hull.size() - 2), hull.get(hull.size() - 1), point) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(point);
        }
        Path2D.Double path = new Path2D.Double();
        path.moveTo(hull.get(0).getX(), hull.get(0).getY());
        for (int i = 1; i < hull.size(); i++) {
            path.lineTo(hull.get(i).getX(), hull.get(i).getY());
        }
        path.closePath();
        return path;
    }

    private static double cross(Point2D a, Point2D b, Point2D c) {
        return (b.getX() - a.getX()) * (c.getY() - a.getY()) - (b.getY() - a.getY()) * (c.getX() - a.getX());
    }
}

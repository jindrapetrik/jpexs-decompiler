/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Draws primitives in stage axes and maps them to the edited shape. Stars use
 * two gestures sharing one center: the inner radius, followed by the outer one.
 *
 * @author JPEXS
 */
public final class ShapeDrawingGesture {

    private final DrawingShape kind;
    private final AffineTransform placement;
    private final AffineTransform toLocal;
    private final Point2D start;
    private Point2D end;
    private final int sides;
    private final int angles;
    private final double cornerRadius;
    private double innerRadius;
    private boolean awaitingOuterRadius;

    public ShapeDrawingGesture(DrawingShape kind, Point2D localStart, AffineTransform placement,
            int sides, int angles, double cornerRadius) {
        this.kind = kind;
        this.placement = new AffineTransform(placement);
        try {
            toLocal = placement.createInverse();
        } catch (NoninvertibleTransformException ex) {
            throw new IllegalArgumentException("Shape placement must be invertible", ex);
        }
        start = placement.transform(localStart, null);
        end = (Point2D) start.clone();
        this.sides = Math.max(3, Math.min(360, sides));
        this.angles = Math.max(3, Math.min(360, angles));
        this.cornerRadius = Math.max(0, cornerRadius);
    }

    public void update(Point2D localPoint) {
        end = placement.transform(localPoint, null);
    }

    /**
     * Returns false when the first star gesture needs a second radius.
     * No document edit is committed until this returns true.
     */
    public boolean release() {
        if (kind == DrawingShape.STAR && !awaitingOuterRadius && start.distance(end) > 0) {
            innerRadius = start.distance(end);
            awaitingOuterRadius = true;
            return false;
        }
        return true;
    }

    public boolean isAwaitingOuterRadius() {
        return awaitingOuterRadius;
    }

    public Shape getGeometry() {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double x = Math.min(start.getX(), end.getX());
        double y = Math.min(start.getY(), end.getY());
        double width = Math.abs(dx);
        double height = Math.abs(dy);
        double radius = start.distance(end);
        double angle = Math.atan2(dy, dx);
        Shape geometry;
        switch (kind) {
            case LINE:
                geometry = new Line2D.Double(start, end);
                break;
            case CIRCLE:
            case SQUARE:
                double size = Math.max(width, height);
                x = dx < 0 ? start.getX() - size : start.getX();
                y = dy < 0 ? start.getY() - size : start.getY();
                geometry = kind == DrawingShape.CIRCLE ? new Ellipse2D.Double(x, y, size, size)
                        : new Rectangle2D.Double(x, y, size, size);
                break;
            case OVAL:
                geometry = new Ellipse2D.Double(x, y, width, height);
                break;
            case ROUND_RECTANGLE:
                double arc = Math.min(2 * cornerRadius, Math.min(width, height));
                geometry = new RoundRectangle2D.Double(x, y, width, height, arc, arc);
                break;
            case TRIANGLE:
            case PENTAGON:
            case POLYGON:
                geometry = polygon(kind == DrawingShape.TRIANGLE ? 3 : kind == DrawingShape.PENTAGON ? 5 : sides,
                        radius, radius, angle, false);
                break;
            case STAR:
                geometry = awaitingOuterRadius ? polygon(angles, Math.max(innerRadius, radius), innerRadius, angle, true)
                        : new Ellipse2D.Double(start.getX() - radius, start.getY() - radius, radius * 2, radius * 2);
                break;
            default:
                geometry = new Rectangle2D.Double(x, y, width, height);
                break;
        }
        return toLocal.createTransformedShape(geometry);
    }

    private Shape polygon(int count, double outer, double inner, double angle, boolean star) {
        Path2D.Double path = new Path2D.Double();
        int vertices = star ? count * 2 : count;
        for (int i = 0; i < vertices; i++) {
            double r = star && i % 2 != 0 ? inner : outer;
            double a = angle + 2 * Math.PI * i / vertices;
            double x = start.getX() + Math.cos(a) * r;
            double y = start.getY() + Math.sin(a) * r;
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.closePath();
        return path;
    }

    public Shape getGuide() {
        if (kind != DrawingShape.STAR) {
            return null;
        }
        double radius = awaitingOuterRadius ? innerRadius : start.distance(end);
        Path2D.Double guide = new Path2D.Double();
        guide.append(new Ellipse2D.Double(start.getX() - radius, start.getY() - radius, radius * 2, radius * 2), false);
        guide.append(new Line2D.Double(start, end), false);
        return toLocal.createTransformedShape(guide);
    }

    public static Shape cursor(Point2D point, AffineTransform placement, double radius) {
        try {
            Point2D center = placement.transform(point, null);
            Path2D.Double cross = new Path2D.Double();
            cross.moveTo(center.getX() - radius, center.getY());
            cross.lineTo(center.getX() + radius, center.getY());
            cross.moveTo(center.getX(), center.getY() - radius);
            cross.lineTo(center.getX(), center.getY() + radius);
            return placement.createInverse().createTransformedShape(cross);
        } catch (NoninvertibleTransformException ex) {
            throw new IllegalArgumentException("Shape placement must be invertible", ex);
        }
    }
}

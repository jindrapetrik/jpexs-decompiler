/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.math.BezierEdge;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Subdivides paths at intersections while retaining quadratic curves and joins.
 * Cubics use the same quadratic approximation as the SWF writer.
 */
final class ShapeEdgeSplitter {

    private final Shape original;
    private final List<List<Edge>> paths = new ArrayList<>();
    private final List<Boolean> closed = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();
    private boolean converted;

    private static final class Edge {

        final BezierEdge curve;
        final List<Double> cuts = new ArrayList<>(Arrays.asList(0.0, 1.0));

        Edge(Point2D... points) {
            curve = new BezierEdge(Arrays.asList(points));
        }
    }

    ShapeEdgeSplitter(Shape shape) {
        original = new Path2D.Double(shape);
        Point2D current = null;
        Point2D start = null;
        List<Edge> path = null;
        double[] coords = new double[6];
        for (PathIterator it = original.getPathIterator(null); !it.isDone(); it.next()) {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO) {
                current = start = point(coords[0], coords[1]);
                path = new ArrayList<>();
                paths.add(path);
                closed.add(false);
                continue;
            }
            if (type == PathIterator.SEG_CUBICTO) {
                converted = true;
                double[][] quadratics = CubicToQuadratic.approximate(new double[]{current.getX(), current.getY(),
                    coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]});
                for (double[] quad : quadratics) {
                    Point2D end = point(quad[2], quad[3]);
                    add(path, new Edge(current, point(quad[0], quad[1]), end));
                    current = end;
                }
            } else if (type == PathIterator.SEG_QUADTO) {
                Point2D end = point(coords[2], coords[3]);
                add(path, new Edge(current, point(coords[0], coords[1]), end));
                current = end;
            } else if (type == PathIterator.SEG_LINETO) {
                Point2D end = point(coords[0], coords[1]);
                add(path, new Edge(current, end));
                current = end;
            } else if (type == PathIterator.SEG_CLOSE) {
                if (!current.equals(start)) {
                    add(path, new Edge(current, start));
                }
                current = start;
                closed.set(closed.size() - 1, true);
            }
        }
    }

    private static Point2D point(double x, double y) {
        return new Point2D.Double(x, y);
    }

    private void add(List<Edge> path, Edge edge) {
        path.add(edge);
        edges.add(edge);
    }

    void intersect(ShapeEdgeSplitter other) {
        for (int i = 0; i < edges.size(); i++) {
            Edge first = edges.get(i);
            for (int j = other == this ? i + 1 : 0; j < other.edges.size(); j++) {
                Edge second = other.edges.get(j);
                List<Double> a = new ArrayList<>();
                List<Double> b = new ArrayList<>();
                first.curve.intersects(second.curve, a, b, new ArrayList<>());
                addCuts(first.cuts, a);
                addCuts(second.cuts, b);
            }
        }
    }

    private static void addCuts(List<Double> cuts, List<Double> added) {
        for (double t : added) {
            if (t > 1e-8 && t < 1 - 1e-8 && cuts.stream().noneMatch(cut -> Math.abs(cut - t) < 1e-8)) {
                cuts.add(t);
            }
        }
    }

    Shape geometry() {
        if (!converted && edges.stream().noneMatch(edge -> edge.cuts.size() > 2)) {
            return original;
        }
        Path2D.Double result = new Path2D.Double(original.getPathIterator(null).getWindingRule());
        for (int i = 0; i < paths.size(); i++) {
            List<Edge> path = paths.get(i);
            if (path.isEmpty()) {
                continue;
            }
            Point2D start = path.get(0).curve.points.get(0);
            result.moveTo(start.getX(), start.getY());
            for (Edge edge : path) {
                Collections.sort(edge.cuts);
                double[][] points = edge.curve.points.stream().map(p -> new double[]{p.getX(), p.getY()}).toArray(double[][]::new);
                for (int cut = 1; cut < edge.cuts.size(); cut++) {
                    double[][] segment = ShapeSelection.subcurve(points, edge.cuts.get(cut - 1), edge.cuts.get(cut));
                    if (segment.length == 2) {
                        result.lineTo(segment[1][0], segment[1][1]);
                    } else {
                        result.quadTo(segment[1][0], segment[1][1], segment[2][0], segment[2][1]);
                    }
                }
            }
            if (closed.get(i)) {
                result.closePath();
            }
        }
        return result;
    }
}

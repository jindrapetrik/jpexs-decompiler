/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.math;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Distance calculation between two batches of Bezier edges.
 *
 * @author JPEXS
 */
public class Distances {

    /**
     * Get distance between two batches of Bezier edges.
     * @param batch1 Batch of Bezier edges 1
     * @param batch2 Batch of Bezier edges 2
     * @return Distance between two batches of Bezier edges
     */
    public static double getBatchDistance(List<BezierEdge> batch1, List<BezierEdge> batch2) {
        Area a1 = batchToArea(batch1);
        Area a2 = batchToArea(batch2);
        return Math.max(areaDist(a1, a2), areaDist(a2, a1));
    }

    private static Area batchToArea(List<BezierEdge> batch) {
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        if (batch.isEmpty()) {
            return new Area();
        }
        path.moveTo(batch.get(0).getBeginPoint().getX(), batch.get(0).getBeginPoint().getY());
        for (BezierEdge be : batch) {
            if (be.points.size() == 3) {
                path.quadTo(be.points.get(1).getX(),
                        be.points.get(1).getY(),
                        be.points.get(2).getX(),
                        be.points.get(2).getY());
            } else {
                path.lineTo(be.getEndPoint().getX(), be.getEndPoint().getY());
            }
        }
        path.closePath();
        return new Area(path);
    }

    private static double areaDist(Area a1, Area a2) {
        List<Point2D> points1 = getAreaPoints(a1);
        List<Point2D> points2 = getAreaPoints(a2);
        double maxDist = 0;
        for (Point2D p : points1) {
            double dist = Double.MAX_VALUE;
            for (Point2D p2 : points2) {
                double d = p.distance(p2);
                if (d < dist) {
                    dist = d;
                }
            }
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
        return maxDist;
    }

    private static List<Point2D> getAreaPoints(Area area) {
        double F = 1.0;
        List<Point2D> points = new ArrayList<>();
        PathIterator pi = area.getPathIterator(null, 0.1);
        double[] coords = new double[6];
        double xPrev = 0;
        double yPrev = 0;
        double xBegin = 0;
        double yBegin = 0;
        while (!pi.isDone()) {
            int code = pi.currentSegment(coords);
            switch (code) {
                case PathIterator.SEG_MOVETO:
                    xBegin = coords[0];
                    yBegin = coords[1];
                    xPrev = xBegin;
                    yPrev = yBegin;
                    points.add(new Point2D.Double(xBegin, yBegin));
                    break;
                case PathIterator.SEG_LINETO:
                    addLinePoints(points, xPrev, yPrev, coords[0], coords[1], F);
                    xPrev = coords[0];
                    yPrev = coords[1];
                    break;
                case PathIterator.SEG_CLOSE:
                    addLinePoints(points, xPrev, yPrev, xBegin, yBegin, F);
                    xPrev = xBegin;
                    yPrev = yBegin;
                    break;
                default:
                    throw new RuntimeException("Curved edge not expected");
            }
            pi.next();
        }
        return points;
    }

    private static void addLinePoints(List<Point2D> points, double x1, double y1, double x2, double y2, double spacing) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        int steps = Math.max(1, (int) Math.ceil(Math.sqrt(dx * dx + dy * dy) / spacing));
        for (int step = 1; step <= steps; step++) {
            points.add(new Point2D.Double(x1 + step * dx / steps, y1 + step * dy / steps));
        }
    }
}

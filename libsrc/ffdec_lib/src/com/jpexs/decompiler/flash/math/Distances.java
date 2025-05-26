/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
        return areaDist(a1, a2);
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
        double minDist = Double.MAX_VALUE;
        double maxDist = 0;
        for (Point2D p : points1) {
            double dist = Double.MAX_VALUE;
            for (Point2D p2 : points2) {
                double d = p.distance(p2);
                if (d < dist) {
                    dist = d;
                }
            }
            if (dist < minDist) {
                minDist = dist;
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
                    points.add(new Point2D.Double(xBegin, yBegin));
                    break;
                case PathIterator.SEG_LINETO:
                    //coords[0], coords[1]
                    //Point2D np = new Point2D.Double(coords[0], coords[1]);
                    double dx = coords[0] - xPrev;
                    double dy = coords[1] - yPrev;
                    double dz = Math.sqrt(dx * dx + dy * dy);
                    double divisor;
                    /*if (dy > dx) {
                        divisor = dy / F;
                    } else {
                        divisor = dx / F;
                    }*/
                    divisor = dz / F;
                    for (int d = 1; d <= divisor; d++) {
                        Point2D p2 = new Point2D.Double(xPrev + d * dx / divisor, yPrev + d * dy / divisor);
                        points.add(p2);
                    }
                    //points.add(np);
                    //System.err.println("dx, dy: "+dx+", "+dy);
                    break;
                case PathIterator.SEG_CLOSE:

                    break;
                default:
                    throw new RuntimeException("Curved edge not expected");
            }
            xPrev = coords[0];
            yPrev = coords[1];
            //System.err.println("pos: "+xPrev+", "+ yPrev);
            pi.next();
        }
        return points;
    }
}

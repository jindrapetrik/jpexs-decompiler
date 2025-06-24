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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Intersection calculating. Based on Node.js library kld-intersections:
 * https://github.com/thelonious/kld-intersections/
 */
public class Intersections {

    private static final double FLATNESS = 0.01;

    private static Point2D min(Point2D p1, Point2D p2) {
        return new Point2D.Double(Math.min(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()));
    }

    private static Point2D max(Point2D p1, Point2D p2) {
        return new Point2D.Double(Math.max(p1.getX(), p2.getX()), Math.max(p1.getY(), p2.getY()));
    }

    private static Point2D multiply(Point2D p, double scalar) {
        return new Point2D.Double(p.getX() * scalar, p.getY() * scalar);
    }

    private static Point2D add(Point2D p1, Point2D p2) {
        return new Point2D.Double(p1.getX() + p2.getX(), p1.getY() + p2.getY());
    }

    private static Point2D lerp(Point2D p1, Point2D p2, double t) {
        double omt = 1.0 - t;

        return new Point2D.Double(
                p1.getX() * omt + p2.getX() * t,
                p1.getY() * omt + p2.getY() * t
        );
    }

    private static List<Point2D> intersectLinePolyline(Point2D a1, Point2D a2, List<Point2D> points) {
        List<Point2D> result = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D b1 = points.get(i);
            Point2D b2 = points.get(i + 1);
            List<Point2D> inter = intersectLineLine(a1, a2, b1, b2, false);
            for (Point2D p : inter) { //???
                if (!result.contains(p)) {
                    result.add(p);
                }
            }
        }

        return result;
    }

    private static List<Point2D> intersectPolylinePolyline(List<Point2D> points1, List<Point2D> points2) {
        List<Point2D> result = new ArrayList<>();

        for (int i = 0; i < points1.size() - 1; i++) {
            Point2D a1 = points1.get(i);
            Point2D a2 = points1.get(i + 1);
            List<Point2D> inter = intersectLinePolyline(a1, a2, points2);

            result.addAll(inter);
        }

        return result;
    }

    private static Point2D vectorFromPoints(Point2D p1, Point2D p2) {
        return new Point2D.Double(
                p2.getX() - p1.getX(),
                p2.getY() - p1.getY()
        );
    }

    private static Point2D subtract(Point2D p1, Point2D p2) {
        return new Point2D.Double(p1.getX() - p2.getX(), p1.getY() - p2.getY());
    }

    private static Point2D project(Point2D p1, Point2D that) {
        double percent = dot(p1, that) / dot(that, that);

        return multiply(that, percent);
    }

    private static Point2D perpendicular(Point2D p1, Point2D that) {
        return subtract(p1, project(p1, that));
    }

    private static double vectorLength(Point2D p) {
        return Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY());
    }

    private static void tesselateInterior(double flatness, Point2D zeroVector, Point2D p1, Point2D p2, Point2D p3, List<Point2D> points) {
        // round 1
        Point2D p4 = lerp(p1, p2, 0.5);
        Point2D p5 = lerp(p2, p3, 0.5);

        // round 2
        Point2D p6 = lerp(p4, p5, 0.5);

        Point2D baseline = vectorFromPoints(p1, p3);
        Point2D tangent = vectorFromPoints(p1, p2);
        double dmax = 0;

        if (!zeroVector.equals(tangent)) {
            Point2D perpendicular = perpendicular(baseline, tangent);

            dmax = vectorLength(perpendicular);
        }

        if (dmax > flatness) {
            tesselateInterior(flatness, zeroVector, p1, p4, p6, points);
            points.add(new Point2D.Double(p6.getX(), p6.getY()));
            tesselateInterior(flatness, zeroVector, p6, p5, p3, points);
        } else {
            points.add(new Point2D.Double(p6.getX(), p6.getY()));
        }
    }

    /**
     * Convert quadratic bezier curve to polyline.
     * @param p1 Start point
     * @param p2 Control point
     * @param p3 End point
     * @return List of points
     */
    public static List<Point2D> quadraticBezierToToPolyline(Point2D p1, Point2D p2, Point2D p3) {
        return quadraticBezierToToPolyline(p1, p2, p3, null);
    }

    /**
     * Convert quadratic bezier curve to polyline.
     * @param p1 Start point
     * @param p2 Control point
     * @param p3 End point
     * @param flatness Flatness
     * @return List of points
     */
    public static List<Point2D> quadraticBezierToToPolyline(Point2D p1, Point2D p2, Point2D p3, Double flatness) {
        List<Point2D> points = new ArrayList<>();
        Point2D zeroVector = new Point2D.Double(0, 0);

        flatness = flatness != null ? flatness : 1.0;

        // add first point
        points.add(p1);

        // add interior points
        tesselateInterior(flatness, zeroVector, p1, p2, p3, points);

        // add last point
        points.add(p3);

        return points;
    }

    /**
     * Gets intersection of two quadratic bezier curves.
     * @param a1 Start point of first curve
     * @param a2 Control point of first curve
     * @param a3 End point of first curve
     * @param b1 Start point of second curve
     * @param b2 Control point of second curve
     * @param b3 End point of second curve
     * @return List of intersection points
     */
    public static List<Point2D> intersectBezier2Bezier2Slow(Point2D a1, Point2D a2, Point2D a3, Point2D b1, Point2D b2, Point2D b3) {
        List<Point2D> a = quadraticBezierToToPolyline(a1, a2, a3, FLATNESS);
        List<Point2D> b = quadraticBezierToToPolyline(b1, b2, b3, FLATNESS);
        return intersectPolylinePolyline(a, b);
    }

    /**
     * Checks intersection of two rectangles.
     * @param r1 Rectangle 1
     * @param r2 Rectangle 2
     * @return True if rectangles intersect
     */
    public static boolean rectIntersection(Rectangle2D r1, Rectangle2D r2) {
        double xmin = Math.max(r1.getX(), r2.getX());
        double xmax1 = r1.getX() + r1.getWidth();
        double xmax2 = r2.getX() + r2.getWidth();
        double xmax = Math.min(xmax1, xmax2);
        if (Double.compare(xmax, xmin) >= 0) {
            double ymin = Math.max(r1.getY(), r2.getY());
            double ymax1 = r1.getY() + r1.getHeight();
            double ymax2 = r2.getY() + r2.getHeight();
            double ymax = Math.min(ymax1, ymax2);
            if (Double.compare(ymax, ymin) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets bounding box of points.
     * @param points Points
     * @return Bounding box
     */
    public static Rectangle2D getBBox(Point2D... points) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        for (Point2D p : points) {
            if (p.getX() < minX) {
                minX = p.getX();
            }
            if (p.getX() > maxX) {
                maxX = p.getX();
            }
            if (p.getY() < minY) {
                minY = p.getY();
            }
            if (p.getY() > maxY) {
                maxY = p.getY();
            }
        }

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }

    /**
     * Gets intersection of two quadratic bezier curves.
     * @param a1 Start point of first curve
     * @param a2 Control point of first curve
     * @param a3 End point of first curve
     * @param b1 Start point of second curve
     * @param b2 Control point of second curve
     * @param b3 End point of second curve
     * @return List of intersection points
     */
    public static List<Point2D> intersectBezier2Bezier2(Point2D a1, Point2D a2, Point2D a3, Point2D b1, Point2D b2, Point2D b3) {
        Point2D pa;
        Point2D pb;
        List<Point2D> result = new ArrayList<>();
        pa = multiply(a2, -2);
        Point2D x = add(pa, a3);
        Point2D c12 = add(a1, x);

        pa = multiply(a1, -2);
        pb = multiply(a2, 2);
        Point2D c11 = add(pa, pb);

        Point2D c10 = new Point2D.Double(a1.getX(), a1.getY());

        pa = multiply(b2, -2);
        Point2D c22 = add(b1, add(pa, b3));

        pa = multiply(b1, -2);
        pb = multiply(b2, 2);
        Point2D c21 = add(pa, pb);

        Point2D c20 = new Point2D.Double(b1.getX(), b1.getY());

        // bezout
        double a = c12.getX() * c11.getY() - c11.getX() * c12.getY();
        double b = c22.getX() * c11.getY() - c11.getX() * c22.getY();
        double c = c21.getX() * c11.getY() - c11.getX() * c21.getY();
        double d = c11.getX() * (c10.getY() - c20.getY()) + c11.getY() * (-c10.getX() + c20.getX());
        double e = c22.getX() * c12.getY() - c12.getX() * c22.getY();
        double f = c21.getX() * c12.getY() - c12.getX() * c21.getY();
        double g = c12.getX() * (c10.getY() - c20.getY()) + c12.getY() * (-c10.getX() + c20.getX());

        // determinant
        Polynomial poly = new Polynomial(Arrays.asList(
                -e * e,
                -2 * e * f,
                a * b - f * f - 2 * e * g,
                a * c - 2 * f * g,
                a * d - g * g
        )
        );

        List<Double> roots;
        try {
            roots = poly.getRoots();
        } catch (RuntimeException rex) {
            /*
            Y values of bounds must be of opposite sign.  ??fixme??
            
            Samples where this happens:
            M 6369  13040 Q 6380 13030 6427 13018 and M 6338 13099 Q 6358 13050 6369 13040
            M 6369 13040 Q 6380 13030 6427 13018 and M 6338 13099 Q 6358 13050 6369 13040
             */
            roots = new ArrayList<>();
        }
        for (double s : roots) {
            if (0 <= s && s <= 1) {
                Polynomial xp = new Polynomial(Arrays.asList(
                        c12.getX(),
                        c11.getX(),
                        c10.getX() - c20.getX() - s * c21.getX() - s * s * c22.getX()
                )
                );
                xp.simplifyEquals();
                List<Double> xRoots = xp.getRoots();
                Polynomial yp = new Polynomial(Arrays.asList(
                        c12.getY(),
                        c11.getY(),
                        c10.getY() - c20.getY() - s * c21.getY() - s * s * c22.getY()
                )
                );
                yp.simplifyEquals();
                List<Double> yRoots = yp.getRoots();

                if (!xRoots.isEmpty() && !yRoots.isEmpty()) {
                    double TOLERANCE = 1e-4;

                    checkRoots:
                    for (double xRoot : xRoots) {
                        if (0 <= xRoot && xRoot <= 1) {
                            for (int k = 0; k < yRoots.size(); k++) {
                                if (Math.abs(xRoot - yRoots.get(k)) < TOLERANCE) {
                                    Point2D res = add(multiply(c22, s * s), (add(multiply(c21, s), c20)));
                                    if (!result.contains(res)) {
                                        result.add(res);
                                    }
                                    break checkRoots;
                                }
                            }
                        }
                    }
                }
            }
        }

        //JPEXS: fix rounding errors
        if (a1.equals(b1) && !result.contains(a1)) {
            result.add(0, a1);
        }
        if (a3.equals(b3) && !result.contains(a3)) {
            result.add(a3);
        }
        if (a1.equals(b3) && !result.contains(a1)) {
            result.add(0, a1);
        }
        if (a3.equals(b1) && !result.contains(a3)) {
            result.add(0, a3);
        }

        return result;
    }

    private static double dot(Point2D p1, Point2D p2) {
        return p1.getX() * p2.getX() + p1.getY() * p2.getY();
    }

    /**
     * Gets intersection of quadratic bezier curve and line. Slow version.
     * @param p1 Start point of bezier curve
     * @param p2 Control point of bezier curve
     * @param p3 End point of bezier curve
     * @param a1 Start point of line
     * @param a2 End point of line
     * @return List of intersection points
     */
    public static List<Point2D> intersectBezier2LineSlow(Point2D p1, Point2D p2, Point2D p3, Point2D a1, Point2D a2) {
        List<Point2D> p = quadraticBezierToToPolyline(p1, p2, p3, FLATNESS);
        return intersectLinePolyline(a1, a2, p);
    }

    /**
     * Gets intersection of quadratic bezier curve and line.
     * @param p1 Start point of bezier curve
     * @param p2 Control point of bezier curve
     * @param p3 End point of bezier curve
     * @param a1 Start point of line
     * @param a2 End point of line
     * @return List of intersection points
     */
    public static List<Point2D> intersectBezier2Line(Point2D p1, Point2D p2, Point2D p3, Point2D a1, Point2D a2) {
        Point2D a; // temporary variables
        Point2D min = min(a1, a2); // used to determine if point is on line segment
        Point2D max = max(a1, a2); // used to determine if point is on line segment
        List<Point2D> result = new ArrayList<>();

        a = multiply(p2, -2);
        Point2D c2 = add(p1, add(a, p3));

        a = multiply(p1, -2);
        Point2D b = multiply(p2, 2);
        Point2D c1 = add(a, b);

        Point2D c0 = new Point2D.Double(p1.getX(), p1.getY());

        // Convert line to normal form: ax + by + c = 0
        // Find normal to line: negative inverse of original line's slope
        Point2D n = new Point2D.Double(a1.getY() - a2.getY(), a2.getX() - a1.getX());

        // Determine new c coefficient
        double cl = a1.getX() * a2.getY() - a2.getX() * a1.getY();

        // Transform cubic coefficients to line's coordinate system and find roots
        // of cubic
        List<Double> roots = new Polynomial(
                Arrays.asList(
                        dot(n, c2),
                        dot(n, c1),
                        dot(n, c0) + cl
                )
        ).getRoots();

        // Any roots in closed interval [0,1] are intersections on Bezier, but
        // might not be on the line segment.
        // Find intersections and calculate point coordinates
        for (double t : roots) {
            if (0 <= t && t <= 1) {
                // We're within the Bezier curve
                // Find point on Bezier
                Point2D p4 = lerp(p1, p2, t);
                Point2D p5 = lerp(p2, p3, t);

                Point2D p6 = lerp(p4, p5, t);

                // See if point is on line segment
                // Had to make special cases for vertical and horizontal lines due
                // to slight errors in calculation of p6
                if (a1.getX() == a2.getX()) {
                    if (min.getY() <= p6.getY() && p6.getY() <= max.getY()) {
                        result.add(p6);
                    }
                } else if (a1.getY() == a2.getY()) {
                    if (min.getX() <= p6.getX() && p6.getX() <= max.getX()) {
                        result.add(p6);
                    }
                } else if (min.getX() <= p6.getX() && p6.getX() <= max.getX() && min.getY() <= p6.getY() && p6.getY() <= max.getY()) {
                    result.add(p6);
                }
            }
        }

        //JPEXS: fix rounding errors
        if (a1.equals(p1) && !result.contains(a1)) {
            result.add(0, a1);
        }
        if (a2.equals(p3) && !result.contains(a2)) {
            result.add(a2);
        }
        if (a1.equals(p3) && !result.contains(a1)) {
            result.add(0, a1);
        }
        if (a2.equals(p1) && !result.contains(a2)) {
            result.add(0, a2);
        }

        return result;
    }

    /**
     * Gets intersection of line and line.
     * @param a1 Start point of first line
     * @param a2 End point of first line
     * @param b1 Start point of second line
     * @param b2 End point of second line
     * @param addCoincident Add coincident points
     * @return List of intersection points
     */
    public static List<Point2D> intersectLineLine(Point2D a1, Point2D a2, Point2D b1, Point2D b2, boolean addCoincident) {
        List<Point2D> result = new ArrayList<>();

        double ua_t = (b2.getX() - b1.getX()) * (a1.getY() - b1.getY()) - (b2.getY() - b1.getY()) * (a1.getX() - b1.getX());
        double ub_t = (a2.getX() - a1.getX()) * (a1.getY() - b1.getY()) - (a2.getY() - a1.getY()) * (a1.getX() - b1.getX());
        double u_b = (b2.getY() - b1.getY()) * (a2.getX() - a1.getX()) - (b2.getX() - b1.getX()) * (a2.getY() - a1.getY());

        if (u_b != 0) {
            double ua = ua_t / u_b;
            double ub = ub_t / u_b;

            if (0 <= ua && ua <= 1 && 0 <= ub && ub <= 1) {
                result.add(
                        new Point2D.Double(
                                a1.getX() + ua * (a2.getX() - a1.getX()),
                                a1.getY() + ua * (a2.getY() - a1.getY())
                        )
                );
            } else {
                //No Intersection
            }
        } else if (ua_t == 0 || ub_t == 0) {
            if (!addCoincident) {
                return result;
            }
            //WARNING: This is actually not an intersection,
            //but Coincident. But we treat it equally

            double a1v;
            double a2v;
            double b1v;
            double b2v;

            if (!(a1.getX() == b1.getX() && a2.getX() == b2.getX() && a1.getX() == a2.getX())) {
                a1v = a1.getX();
                a2v = a2.getX();
                b1v = b1.getX();
                b2v = b2.getX();
            } else {
                a1v = a1.getY();
                a2v = a2.getY();
                b1v = b1.getY();
                b2v = b2.getY();
            }

            if (a1v > a2v) {
                double td;
                td = a1v;
                a1v = a2v;
                a2v = td;
            }

            if (b1v > b2v) {
                double td;
                td = b1v;
                b1v = b2v;
                b2v = td;
            }

            if (b1v < a1v) {
                //swap a, b

                Point2D t;

                t = b1;
                b1 = a1;
                a1 = t;

                t = b2;
                b2 = a2;
                a2 = t;

                double td;

                td = b1v;
                b1v = a1v;
                a1v = td;

                td = b2v;
                b2v = a2v;
                a2v = td;
            }

            if (a2v < b1v) {
                //no overlap
                return result;
            }

            if (a2v == b1v) {
                //single point, ignore
                return result;
            }
            //A1   B1    A2   B2
            // |----|----|----|
            result.add(b1);
            result.add(a2);
        } else {
            //Parallel            
        }

        return result;
    }
}

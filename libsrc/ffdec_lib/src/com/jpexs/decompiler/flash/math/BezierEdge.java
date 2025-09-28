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

import com.jpexs.helpers.Reference;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

/**
 * Bezier edge.
 *
 * @author JPEXS
 */
public class BezierEdge implements Serializable {

    /**
     * Points of the Bezier edge.
     */
    public List<Point2D> points = new ArrayList<>(3);

    /**
     * Points of the Bezier edge in reverse order.
     */
    private List<Point2D> revPoints = new ArrayList<>();

    /**
     * Hash
     */
    private int hash;

    /**
     * Hash of the reverse bezier edge.
     */
    private int revHash;

    /**
     * Bounding box
     */
    private Rectangle2D bbox;

    /**
     * Is the edge empty?
     */
    private boolean empty;

    /**
     * Constructor.
     *
     * @param points Points
     */
    public BezierEdge(List<Point2D> points) {
        this.points = points;
        calcParams();
    }

    @Override
    public BezierEdge clone() {
        return new BezierEdge(new ArrayList<>(points));
    }

    /**
     * Is the edge empty?
     *
     * @return True if the edge is empty, false otherwise.
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Constructor.
     *
     * @param x0 Start x
     * @param y0 Start y
     * @param x1 End x
     * @param y1 End y
     */
    public BezierEdge(double x0, double y0, double x1, double y1) {
        points.add(new Point2D.Double(x0, y0));
        points.add(new Point2D.Double(x1, y1));
        calcParams();
    }

    /**
     * Constructor.
     *
     * @param x0 Start x
     * @param y0 Start y
     * @param cx Control x
     * @param cy Control y
     * @param x1 End x
     * @param y1 End y
     */
    public BezierEdge(double x0, double y0, double cx, double cy, double x1, double y1) {
        points.add(new Point2D.Double(x0, y0));
        points.add(new Point2D.Double(cx, cy));
        points.add(new Point2D.Double(x1, y1));
        calcParams();
    }

    /**
     * Gets the begin point.
     *
     * @return Begin point
     */
    public Point2D getBeginPoint() {
        return points.get(0);
    }

    /**
     * Gets the end point.
     *
     * @return End point
     */
    public Point2D getEndPoint() {
        return points.get(points.size() - 1);
    }

    /**
     * Sets the begin point.
     *
     * @param p Begin point
     */
    public void setBeginPoint(Point2D p) {
        points.set(0, p);
        calcParams();
    }

    /**
     * Sets the end point.
     *
     * @param p End point
     */
    public void setEndPoint(Point2D p) {
        points.set(points.size() - 1, p);
        calcParams();
    }

    /**
     * Gets the point at specified position.
     *
     * @param t Position
     * @return Point at position
     */
    public Point2D pointAt(double t) {
        if (points.size() == 2) {
            double x = (1 - t) * points.get(0).getX() + t * points.get(1).getX();
            double y = (1 - t) * points.get(0).getY() + t * points.get(1).getY();
            return new Point2D.Double(x, y);
        }
        //points size == 3
        double x = (1 - t) * (1 - t) * points.get(0).getX()
                + 2 * t * (1 - t) * points.get(1).getX()
                + t * t * points.get(2).getX();
        double y = (1 - t) * (1 - t) * points.get(0).getY()
                + 2 * t * (1 - t) * points.get(1).getY()
                + t * t * points.get(2).getY();
        return new Point2D.Double(x, y);
    }

    private void calcParams() {
        if (points.size() == 2) {
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

            this.bbox = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        } else {
            this.bbox = quadraticBBox(points.get(0), points.get(1), points.get(2));
        }

        this.hash = points.hashCode();

        revPoints = new ArrayList<>();
        for (int i = points.size() - 1; i >= 0; i--) {
            revPoints.add(points.get(i));
        }
        this.revHash = revPoints.hashCode();

        empty = true;
        Point2D p1 = getBeginPoint();
        for (int i = 1; i < points.size(); i++) {
            if (!points.get(i).equals(p1)) {
                empty = false;
                break;
            }
        }
    }

    /**
     * Calculates the bounding box.
     *
     * @return Bounding box
     */
    public Rectangle2D bbox() {
        return bbox;
    }

    private final double MIN_SIZE = 0.5;

    /**
     * Calculates the area.
     *
     * @return Area
     */
    public double area() {
        Rectangle2D rect = bbox();
        double w = rect.getWidth();
        double h = rect.getHeight();

        if (w < MIN_SIZE) {
            w = MIN_SIZE;
        }
        if (h < MIN_SIZE) {
            h = MIN_SIZE;
        }
        return w * h;
    }

    private static boolean rectIntersection(Rectangle2D r1, Rectangle2D r2) {
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
                //out.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the intersections.
     *
     * @param b2 Bezier edge
     * @return List of intersections
     */
    public List<Point2D> getIntersections(BezierEdge b2) {
        if (!Intersections.rectIntersection(bbox, b2.bbox)) {
            return new ArrayList<>();
        }
        if (points.size() == 2) {
            if (b2.points.size() == 2) {
                return Intersections.intersectLineLine(points.get(0), points.get(1), b2.points.get(0), b2.points.get(1), true);
            } else {
                return Intersections.intersectBezier2Line(b2.points.get(0), b2.points.get(1), b2.points.get(2), points.get(0), points.get(1));
            }
        } else {
            if (b2.points.size() == 2) {
                return Intersections.intersectBezier2Line(points.get(0), points.get(1), points.get(2), b2.points.get(0), b2.points.get(1));
            } else {
                return Intersections.intersectBezier2Bezier2(points.get(0), points.get(1), points.get(2), b2.points.get(0), b2.points.get(1), b2.points.get(2));
            }
        }
    }

    /**
     * Gets the intersections. Old version.
     *
     * @param b2 Bezier edge 2
     * @param t1Ref T1 reference
     * @param t2Ref T2 reference
     * @return True if the edges intersect, false otherwise
     */
    public boolean intersectsOld(BezierEdge b2, List<Double> t1Ref, List<Double> t2Ref) {
        List<Point2D> interPoints = new ArrayList<>();
        List<Double> t1RefA = new ArrayList<>();
        List<Double> t2RefA = new ArrayList<>();
        boolean ret = intersects(b2,
                0,
                1,
                0,
                1, t1RefA, t2RefA, interPoints);
        Point2D last = new Point2D.Double(Double.MAX_VALUE, Double.MAX_VALUE);
        int numSame = 0;
        double sumT1 = 0;
        double sumT2 = 0;
        for (int i = 0; i < interPoints.size(); i++) {
            double dist = interPoints.get(i).distance(last);
            System.err.println("dist=" + dist);
            if (dist <= 5.0) {
                numSame++;
                sumT1 += t1RefA.get(i);
                sumT2 += t2RefA.get(i);
                last = interPoints.get(i);
            } else {
                if (numSame > 0) {
                    t1Ref.add(sumT1 / numSame);
                    t2Ref.add(sumT2 / numSame);
                }
                numSame = 1;
                last = interPoints.get(i);
                sumT1 = t1RefA.get(i);
                sumT2 = t2RefA.get(i);
            }
        }
        if (numSame > 0) {
            t1Ref.add(sumT1 / numSame);
            t2Ref.add(sumT2 / numSame);
        }

        return ret;
    }

    /**
     * Checks if the edges intersect.
     *
     * @param b2 Bezier edge 2
     * @param t1Ref T1 reference
     * @param t2Ref T2 reference
     * @param intPoints Intersection points
     * @return True if the edges intersect, false otherwise
     */
    public boolean intersects(BezierEdge b2, List<Double> t1Ref, List<Double> t2Ref, List<Point2D> intPoints) {
        List<Point2D> inter = getIntersections(b2);
        BezierUtils utils = new BezierUtils();
        for (Point2D p : inter) {
            Point2D p1;
            Point2D p3;
            Point2D p2;
            p1 = this.points.get(0);
            p3 = this.points.get(this.points.size() - 1);
            p2 = this.points.size() == 3 ? this.points.get(1) : new Point2D.Double((p1.getX() + p3.getX()) / 2, (p1.getY() + p3.getY()) / 2);

            t1Ref.add(utils.closestPointToBezier(p, p1, p2, p3));

            p1 = b2.points.get(0);
            p3 = b2.points.get(b2.points.size() - 1);
            p2 = b2.points.size() == 3 ? b2.points.get(1) : new Point2D.Double((p1.getX() + p3.getX()) / 2, (p1.getY() + p3.getY()) / 2);
            t2Ref.add(utils.closestPointToBezier(p, p1, p2, p3));
        }
        intPoints.addAll(inter);
        return !inter.isEmpty();
    }

    private boolean intersects(
            BezierEdge b2,
            double start1,
            double end1,
            double start2,
            double end2,
            List<Double> t1Ref,
            List<Double> t2Ref,
            List<Point2D> interPoints
    ) {
        final double threshold = MIN_SIZE * 2.0; //?
        Rectangle2D bb1 = bbox();
        Rectangle2D bb2 = b2.bbox();
        if (!rectIntersection(bb1, bb2)) {
            return false;
        }
        double sumAreas = area() + b2.area();
        //System.err.println("sumAreas="+sumAreas);
        if (Double.compare(sumAreas, threshold) <= 0) {
            double t1 = (start1 + end1) / 2;
            double t2 = (start2 + end2) / 2;
            Point2D selPoint = getBeginPoint();
            interPoints.add(selPoint);
            t1Ref.add(t1);
            t2Ref.add(t2);
            return true;
        }

        //System.err.println("subdividing "+this+ " and "+b2);
        BezierUtils bu = new BezierUtils();
        List<Point2D> b1aPoints = new ArrayList<>();
        List<Point2D> b1bPoints = new ArrayList<>();
        bu.subdivide(points, 0.5, b1aPoints, b1bPoints);

        List<Point2D> b2aPoints = new ArrayList<>();
        List<Point2D> b2bPoints = new ArrayList<>();
        bu.subdivide(b2.points, 0.5, b2aPoints, b2bPoints);
        BezierEdge b1a = new BezierEdge(b1aPoints);
        BezierEdge b1b = new BezierEdge(b1bPoints);
        BezierEdge b2a = new BezierEdge(b2aPoints);
        BezierEdge b2b = new BezierEdge(b2bPoints);

        double half1 = start1 + (end1 - start1) / 2.0;
        double half2 = start2 + (end2 - start2) / 2.0;

        boolean ok = false;
        if (b1a.intersects(b2a, start1, half1, start2, half2, t1Ref, t2Ref, interPoints)) {
            ok = true;
        }
        if (b1a.intersects(b2b, start1, half1, half2, end2, t1Ref, t2Ref, interPoints)) {
            ok = true;
        }
        if (b1b.intersects(b2a, half1, end1, start2, half2, t1Ref, t2Ref, interPoints)) {
            ok = true;
        }
        if (b1b.intersects(b2b, half1, end1, half2, end2, t1Ref, t2Ref, interPoints)) {
            ok = true;
        }
        return ok;
    }

    /**
     * Gets the length.
     *
     * @return Length
     */
    public double length() {
        double distance = 0;
        double epsilon = 1;

        Stack<BezierEdge> parts = new Stack<BezierEdge>();
        parts.push(this);

        while (!parts.isEmpty()) {
            BezierEdge curve = parts.pop();
            double d = curve.points.get(0).distance(curve.points.get(curve.points.size() - 1));
            if (d < epsilon) {
                distance += d;
            } else {
                Reference<BezierEdge> leftRef = new Reference<>(null);
                Reference<BezierEdge> rightRef = new Reference<>(null);
                curve.split(0.5, leftRef, rightRef);
                parts.add(leftRef.getVal());
                parts.add(rightRef.getVal());
            }
        }
        return distance;
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        for (Point2D p : points) {
            list.add("[" + p.getX() + "," + p.getY() + "]");
        }
        return "{" + String.join("-", list) + "}";
    }

    /**
     * Converts the edge to SVG.
     *
     * @return SVG string
     */
    public String toSvg() {

        DecimalFormat df = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setGroupingUsed(false);

        String ret = "";
        ret += "M ";
        ret += df.format(points.get(0).getX());
        ret += " ";
        ret += df.format(points.get(0).getY());
        ret += " ";
        if (points.size() == 3) {
            ret += "Q ";
        } else {
            ret += "L ";
        }
        ret += df.format(points.get(1).getX());
        ret += " ";
        ret += df.format(points.get(1).getY());
        if (points.size() == 3) {
            ret += " ";
            ret += df.format(points.get(2).getX());
            ret += " ";
            ret += df.format(points.get(2).getY());
        }
        return ret;
    }

    /**
     * Splits the edge.
     *
     * @param t Position
     * @param left Left edge
     * @param right Right edge
     */
    public void split(double t, Reference<BezierEdge> left, Reference<BezierEdge> right) {
        List<Point2D> leftPoints = new ArrayList<>();
        List<Point2D> rightPoints = new ArrayList<>();
        BezierUtils bu = new BezierUtils();
        bu.subdivide(points, t, leftPoints, rightPoints);
        left.setVal(new BezierEdge(leftPoints));
        rightPoints.set(0, leftPoints.get(leftPoints.size() - 1));
        right.setVal(new BezierEdge(rightPoints));
    }

    public List<BezierEdge> split(List<Double> tList) {
        List<BezierEdge> result = new ArrayList<>();

        double prevT = 0.0;
        BezierEdge remaining = this;

        for (double t : tList) {
            double localT = (t - prevT) / (1.0 - prevT);

            Reference<BezierEdge> leftRef = new Reference<>(null);
            Reference<BezierEdge> rightRef = new Reference<>(null);
            remaining.split(localT, leftRef, rightRef);
            result.add(leftRef.getVal());
            remaining = rightRef.getVal();
            prevT = t;
        }

        result.add(remaining);
        return result;
    }

    /**
     * Reverses the edge.
     *
     * @return Reversed edge
     */
    public BezierEdge reverse() {
        return new BezierEdge(revPoints);
    }

    /**
     * Rounds the edge.
     */
    public void round() {
        for (int i = 0; i < this.points.size(); i++) {
            this.points.set(i, new Point2D.Double(
                    Math.round(this.points.get(i).getX()),
                    Math.round(this.points.get(i).getY())
            ));
        }
        calcParams();
    }

    /**
     * Rounds the edge to half.
     */
    public void roundHalf() {
        for (int i = 0; i < this.points.size(); i++) {
            this.points.set(i, new Point2D.Double(
                    Math.round(this.points.get(i).getX() * 2.0) / 2.0,
                    Math.round(this.points.get(i).getY() * 2.0) / 2.0
            ));
        }
        calcParams();
    }        

    public static final double ROUND_VALUE = 2;

    public void roundX() {
        for (int i = 0; i < this.points.size(); i++) {
            this.points.set(i, new Point2D.Double(
                    Math.round(this.points.get(i).getX() * ROUND_VALUE) / ROUND_VALUE,
                    Math.round(this.points.get(i).getY() * ROUND_VALUE) / ROUND_VALUE
            ));
        }
        calcParams();
    }
    
    public void roundN(double n) {
        for (int i = 0; i < this.points.size(); i++) {
            this.points.set(i, new Point2D.Double(
                    Math.round(this.points.get(i).getX() * n) / n,
                    Math.round(this.points.get(i).getY() * n) / n
            ));
        }
        calcParams();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BezierEdge other = (BezierEdge) obj;
        if (other.hash != hash) {
            return false;
        }
        return Objects.equals(this.points, other.points);
    }

    /**
     * Checks if the edge is equal to another edge in reverse order.
     *
     * @param other Other edge
     * @return True if the edges are equal in reverse order, false otherwise
     */
    public boolean equalsReverse(BezierEdge other) {
        if (hash != other.revHash) {
            return false;
        }
        if (other.points.size() != points.size()) {
            return false;
        }
        for (int i = 0; i < points.size(); i++) {
            if (!points.get(i).equals(other.points.get(points.size() - 1 - i))) {
                return false;
            }
        }
        return true;
    }

    public void shrinkToLine() {
        if (points.size() == 3) {
            double det = (points.get(1).getX() - points.get(0).getX())
                    * (points.get(2).getY() - points.get(0).getY())
                    - (points.get(1).getY() - points.get(0).getY())
                    * (points.get(2).getX() - points.get(0).getX());
            if (det == 0) {
                points.remove(1);
                revPoints.remove(1);
                calcParams();
            }
        }
    }

    public Double findXCriticalT() {
        double EPS = 1e-12;

        if (points.size() < 3) {
            return null;
        }

        // dx/dt = 2 * ((x1 - x0) + t * (x2 - 2*x1 + x0))
        final double a = (points.get(2).getX() - 2.0 * points.get(1).getX() + points.get(0).getX()); // coefficient of t
        final double b = (points.get(1).getX() - points.get(0).getX());            // constant term

        // If a ~ 0, dx/dt is (almost) constant -> x(t) is (almost) linear -> x-monotone already.
        if (Math.abs(a) < EPS) {
            return null;
        }

        double t = -b / a; // (x0 - x1) / (x0 - 2*x1 + x2)

        // Accept strictly inside (0,1); relax by EPS to be robust.
        if (t > EPS && t < 1.0 - EPS) {
            return t;
        }

        return null;
    }

    private static double quad(double p0, double p1, double p2, double t) {
        double u = 1.0 - t;
        return u * u * p0 + 2.0 * u * t * p1 + t * t * p2;
    }

    public double yAt(double x) {
        double EPS = 1e-12;
        if (points.size() == 2) {
            //line
            double x0 = points.get(0).getX();
            double y0 = points.get(0).getY();
            double x1 = points.get(1).getX();
            double y1 = points.get(1).getY();
            double dx = x1 - x0;

            // Guard for near-vertical segments: return stable representative y
            if (Math.abs(dx) < EPS) {
                return (y0 + y1) * 0.5;
            }

            double t = (x - x0) / dx;
            if (t < 0) {
                t = 0;
            } else if (t > 1) {
                t = 1;
            }
            return y0 + t * (y1 - y0);
        }
        //quad
        final double A = (points.get(0).getX() - 2.0 * points.get(1).getX() + points.get(2).getX());
        final double B = 2.0 * (points.get(1).getX() - points.get(0).getX());
        final double C = points.get(0).getX() - x; // solve A t^2 + B t + C = 0

        double t;
        if (Math.abs(A) < EPS) {
            t = (Math.abs(B) < EPS) ? 0.5 : (-C / B);
        } else {
            double disc = B * B - 4.0 * A * C;
            if (disc < 0 && disc > -1e-14) {
                disc = 0;
            }
            if (disc < 0) {
                t = 0.5;
            } else {
                double sqrtD = Math.sqrt(disc);
                double q = -0.5 * (B + Math.copySign(sqrtD, B));
                double t1 = q / A;
                double t2 = (Math.abs(q) < EPS) ? t1 : (C / q);
                boolean t1In = t1 > -1e-9 && t1 < 1.0 + 1e-9;
                boolean t2In = t2 > -1e-9 && t2 < 1.0 + 1e-9;
                if (t1In && !t2In) {
                    t = t1;
                } else if (!t1In && t2In) {
                    t = t2;
                } else if (t1In && t2In) {
                    t = (Math.abs(t1 - 0.5) < Math.abs(t2 - 0.5)) ? t1 : t2;
                } else {
                    double c1 = Math.min(Math.max(t1, 0.0), 1.0);
                    double c2 = Math.min(Math.max(t2, 0.0), 1.0);
                    double x1 = quad(points.get(0).getX(), points.get(1).getX(), points.get(2).getX(), c1);
                    double x2 = quad(points.get(0).getX(), points.get(1).getX(), points.get(2).getX(), c2);
                    t = (Math.abs(x1 - x) <= Math.abs(x2 - x)) ? c1 : c2;
                }
            }
        }
        if (t < 0) {
            t = 0;
        } else if (t > 1) {
            t = 1;
        }
        return quad(points.get(0).getY(), points.get(1).getY(), points.get(2).getY(), t);
    }

    /**
     * Compute B(t) for a quadratic Bezier defined by P0, P1, P2.
     */
    private static Point2D.Double evalQuad(Point2D p0, Point2D p1, Point2D p2, double t) {
        // All math in double; t assumed in [0,1], but we'll still accept slightly outside due to numeric noise.
        double u = 1.0 - t;
        double x = u * u * p0.getX() + 2 * u * t * p1.getX() + t * t * p2.getX();
        double y = u * u * p0.getY() + 2 * u * t * p1.getY() + t * t * p2.getY();
        return new Point2D.Double(x, y);
    }

    /**
     * Compute the bounding box of a quadratic Bezier (P0, P1, P2). We include
     * endpoints and inner extrema where the derivative in x or y equals 0.
     */
    public static Rectangle2D quadraticBBox(Point2D p0, Point2D p1, Point2D p2) {
        // Collect candidate t values: endpoints and potential inner extrema for x and y.
        List<Double> candidates = new ArrayList<>();
        candidates.add(0.0);
        candidates.add(1.0);

        // Derivative B'(t) = 2( (1 - t)(P1 - P0) + t(P2 - P1) )
        // Setting x' = 0 gives linear equation in t with solution:
        // t = (P0x - P1x) / (P0x - 2P1x + P2x), if denominator != 0
        double denomX = p0.getX() - 2.0 * p1.getX() + p2.getX();
        double denomY = p0.getY() - 2.0 * p1.getY() + p2.getY();

        // Small epsilon to avoid floating point issues around 0 denominators
        final double EPS = 1e-12;

        if (Math.abs(denomX) > EPS) {
            double tx = (p0.getX() - p1.getX()) / denomX;
            if (tx > 0.0 && tx < 1.0) {
                candidates.add(tx);
            }
        }
        if (Math.abs(denomY) > EPS) {
            double ty = (p0.getY() - p1.getY()) / denomY;
            if (ty > 0.0 && ty < 1.0) {
                candidates.add(ty);
            }
        }

        // Evaluate all candidate points and take min/max
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (double t : candidates) {
            // Clamp t just in case of tiny numeric drifts
            double tt = Math.max(0.0, Math.min(1.0, t));
            Point2D.Double pt = evalQuad(p0, p1, p2, tt);
            double x = pt.x;
            double y = pt.y;
            if (x < minX) {
                minX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
        }

        return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    }
}

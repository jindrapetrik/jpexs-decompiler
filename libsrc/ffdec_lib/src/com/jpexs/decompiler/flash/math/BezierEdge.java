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
     * @return True if the edge is empty, false otherwise.
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Constructor.
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
     * @return Begin point
     */
    public Point2D getBeginPoint() {
        return points.get(0);
    }

    /**
     * Gets the end point.
     * @return End point
     */
    public Point2D getEndPoint() {
        return points.get(points.size() - 1);
    }

    /**
     * Sets the begin point.
     * @param p Begin point
     */
    public void setBeginPoint(Point2D p) {
        points.set(0, p);
        calcParams();
    }

    /**
     * Sets the end point.
     * @param p End point
     */
    public void setEndPoint(Point2D p) {
        points.set(points.size() - 1, p);
        calcParams();
    }

    /**
     * Gets the point at specified position.
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
     * @return Bounding box
     */
    public Rectangle2D bbox() {
        return bbox;
    }

    private final double MIN_SIZE = 0.5;

    /**
     * Calculates the area.
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


    /**
     * Reverses the edge.
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

    /**
     * Test main.
     * @param args Arguments
     */
    public static void main(String[] args) {
        List<Double> t1 = new ArrayList<>();
        List<Double> t2 = new ArrayList<>();
        /*/BezierEdge be1 = new BezierEdge(0,0,100,100);
        BezierEdge be2 = new BezierEdge(75,100,75,0);
        
        System.out.println("lines "+ be1+ " and "+be2);
        
        
        
        
        System.out.println("hasIntersection = "+be1.intersects(be2, t1, t2));
        System.out.println("intersection points: "+t1+", "+t2);
        
        
        BezierEdge be3 = new BezierEdge(0,0,100,0);
        BezierEdge be4 = new BezierEdge(0,100,100,100);
        
        System.out.println("lines "+ be3+ " and "+be4);
        
        System.out.println("hasIntersection = "+be3.intersects(be4, t1, t2));
        System.out.println("intersection points: "+t1+", "+t2);*/

        BezierEdge be5 = new BezierEdge(25, 0, 25, 100);
        BezierEdge be6 = new BezierEdge(0, 50, 100, 50);

        //System.err.println("lines " + be5 + " and " + be6);
        BezierEdge q1 = new BezierEdge(3469, 3124, 3320, 3148, 3355, 3215);
        BezierEdge q2 = new BezierEdge(3442, 3191, 3316, 3146, 3317, 3071);
        BezierEdge q3 = new BezierEdge(3310, 3222, 3450, 3172, 3300, 3181);
        BezierEdge li = new BezierEdge(3423, 3040, 3277, 3164);
        BezierEdge li2 = new BezierEdge(3399, 3095, 3365, 3039);

        List<Point2D> ints;
        /*ints = q2.getIntersections(q1);
        System.err.println("intersections is "+ints);
        ints = q2.getIntersections(li);
        System.err.println("intersections is "+ints);
        ints = li2.getIntersections(li);
        System.err.println("intersections is "+ints);
        ints = q1.getIntersections(q3);        
        System.err.println("intersections is "+ints);*/

        BezierEdge qa = new BezierEdge(-81.0, -78.0, -85.0, -76.0, -86.0, -66.0);
        BezierEdge qb = new BezierEdge(-166.0, 37.0, -172.0, -21.0, -81.0, -78.0);
        /*ints = qa.getIntersections(qb);
        System.err.println("intersections is " + ints);        
        BezierEdge qc = new BezierEdge(-106.0,39.0, -104.0,33.0);
        BezierEdge qd = new BezierEdge(-104.0,33.0,-105.0,36.0,-102.0,26.0);
        ints = qc.getIntersections(qd);
        System.err.println("intersections is " + ints);
        
        ints = Intersections.intersectLineLine(new Point2D.Double(0,0), new Point2D.Double(10,0), new Point2D.Double(2,0), new Point2D.Double(5,0), true); 
        System.err.println("intersections is " + ints);       
        BezierEdge qe = new BezierEdge(-104,33,-104.5,35,-104,32);
        BezierEdge qf = new BezierEdge(-106,39 ,-104,33);
        ints = qe.getIntersections(qf);*/
        BezierEdge qg = new BezierEdge(-66, 139, -67, 140, -61, 135);
        BezierEdge qh = new BezierEdge(-64, 169, -66.5, 139.5, -66, 139);

        //Error Y values of bounds must be of opposite sign
        BezierEdge qi = new BezierEdge(6369.0, 13040.0, 6380.0, 13030.0, 6427.0, 13018.0);
        BezierEdge qj = new BezierEdge(6338.0, 13099.0, 6358.0, 13050.0, 6369.0, 13040.0);

        //Error Y values of bounds must be of opposite sign
        BezierEdge qk = new BezierEdge(45334.0, 2421.0, 45348.0, 2373.0, 45348.0, 2330.0);
        BezierEdge ql = new BezierEdge(45348.0, 2330.0, 45348.0, 2263.0, 45314.0, 2223.0);

        BezierEdge qm = new BezierEdge(-1957, 2676, -1957, 2676.5, -1957.5, 2676.5);
        BezierEdge qn = new BezierEdge(-1957, 2675.5, -1957, 2676, -1957, 2676);

        /*List<Point2D> ps = new ArrayList<>();
        qm.intersects(qn, t1, t2, ps);
        System.err.println("t1 is " + t1);
        System.err.println("t2 is " + t2);
        System.err.println("intersections is " + ps);
         */
 /*Shape r1 = new Rectangle2D.Double(0, 0, 200, 100);
        GeneralPath r2 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        r2.moveTo(0, 0);
        r2.lineTo(100, 0);
        r2.lineTo(150, 0);
        r2.lineTo(200, 0);
        r2.lineTo(200, 100);
        r2.lineTo(0, 100);
        r2.closePath();
        
        GeneralPath sh1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        sh1.moveTo(0, 0);
        sh1.quadTo(100, 100, 0, 100);
        sh1.lineTo(0, 0);
        sh1.closePath();
        
        BezierEdge bex = new BezierEdge(0, 0, 100, 100, 0, 100);
        Reference<BezierEdge> bex1Ref = new Reference<>(null);
        Reference<BezierEdge> bex2Ref = new Reference<>(null);
        bex.split(0.2, bex1Ref, bex2Ref);
        BezierEdge bex1 = bex1Ref.getVal();
        BezierEdge bex2 = bex2Ref.getVal();
        GeneralPath sh2 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        sh2.moveTo(bex1.getBeginPoint().getX(), bex1.getBeginPoint().getY());
        sh2.quadTo(10+bex1.points.get(1).getX(), bex1.points.get(1).getY(), bex1.getEndPoint().getX(), bex1.getEndPoint().getY());
        sh2.quadTo(bex2.points.get(1).getX(), bex2.points.get(1).getY(), bex2.getEndPoint().getX(), bex2.getEndPoint().getY());
        sh2.lineTo(0, 0);
        sh2.closePath();
        
        Area a1 = new Area(r1);
        Area a2 = new Area(r2);
        //a1.exclusiveOr(a2);
                
        
         */
 /*GeneralPath p1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        p1.moveTo(0, 0);
        p1.lineTo(200, 0);
        p1.lineTo(200, 200);
        p1.lineTo(0, 200);
        p1.lineTo(0, 0);
        p1.closePath();
        p1.moveTo(50, 50);
        p1.lineTo(150, 50);
        p1.lineTo(150, 150);
        p1.lineTo(50, 150);
        p1.lineTo(50, 50);
        p1.closePath();

        System.err.println("cont:" + p1.contains(100, 100));
        System.err.println("cont:" + p1.contains(150, 150));  */
        //System.err.println("minDist = " + minDist+", maxDist = "+ maxDist);
        //System.err.println("eArea = " + Areas.calcArea(a1));

        /*Point2D c = new Point2D.Double(
                (1 - t1.get(0)) * be5.points.get(0).getX() + t1.get(0) * be5.points.get(1).getX(),
                (1 - t1.get(0)) * be5.points.get(0).getY() + t1.get(0) * be5.points.get(1).getY()
        );

        System.out.println("Intersection point: " + c);
        
        BezierEdge be = new BezierEdge(0, 0, 100, 50, 0, 100);
        
        System.out.println("be5.dist: " + be.length());*/
        //Rectangle2D out = new Rectangle2D.Double();
        //rectIntersection(new Rectangle2D.Double(0,0,50,50), new Rectangle2D.Double(0,50,50,50), out);
        //System.out.println("out = "+out);
    }
}

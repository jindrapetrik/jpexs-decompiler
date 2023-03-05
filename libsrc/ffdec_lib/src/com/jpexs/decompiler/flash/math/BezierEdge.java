/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class BezierEdge {

    public List<Point2D> points = new ArrayList<>();

    public BezierEdge(List<Point2D> points) {
        this.points = points;
    }       

    public BezierEdge(double x0, double y0, double x1, double y1) {        
        points.add(new Point2D.Double(x0, y0));
        points.add(new Point2D.Double(x1, y1));
    }
    
    public BezierEdge(double x0, double y0, double cx, double cy, double x1, double y1) {        
        points.add(new Point2D.Double(x0, y0));
        points.add(new Point2D.Double(cx, cy));
        points.add(new Point2D.Double(x1, y1));
    }

    public Point2D pointAt(double t) {
        if (points.size() == 2) {
            double x = (1-t)*points.get(0).getX() + t * points.get(1).getX();
            double y = (1-t)*points.get(0).getY() + t * points.get(1).getY();
            return new Point2D.Double(x, y);
        }
        //points size == 3
        double x = (1 - t) * (1 - t) * points.get(0).getX() +
                2 * t * (1 - t) * points.get(1).getX() +
                t * t * points.get(2).getX();
        double y = (1 - t) * (1 - t) * points.get(0).getY() +
                2 * t * (1 - t) * points.get(1).getY() +
                t * t * points.get(2).getY();
        return new Point2D.Double(x, y);
    }
    
    public Rectangle2D bbox() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        //System.err.println("calculating bbox");
        for (Point2D p : points) {
            //System.err.println(" point " + p);
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
        
        Rectangle2D b = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        /*System.err.println(" bbox = "+b);
        System.err.println(" maxx = "+maxX);
        System.err.println(" maxy = "+maxY);*/
        return b;
    }
    
    
    private final double MIN_SIZE = 1.0;
    
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
        return  w * h;
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
                if (Double.compare(ymax, ymin) >= 0)  {
                    //out.setRect(xmin, ymin, xmax-xmin, ymax-ymin);
                    return true;
                }
            }
            return false;
        }
    
    
    public boolean intersects(BezierEdge b2, List<Double> t1Ref, List<Double> t2Ref) {
        return intersects(b2, 
                0,
                1,
                0,
                1, t1Ref, t2Ref);
    }
    
    private boolean intersects(
            BezierEdge b2,
            double start1,
            double end1,
            double start2,
            double end2,
            List<Double> t1Ref,
            List<Double> t2Ref) {
        final double threshold = MIN_SIZE * 2.0; //?
        Rectangle2D bb1 = bbox();
        Rectangle2D bb2 = b2.bbox();
        if (!rectIntersection(bb1, bb2)) {
            return false;
        }
        double sumAreas = area()+b2.area();
        //System.err.println("sumAreas="+sumAreas);
        if(Double.compare(sumAreas, threshold) <= 0) {
            double t1 = (start1+end1) / 2;
            double t2 = (start2+end2) / 2;                
            
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
                
        double half1 = start1 + (end1- start1) / 2.0;
        double half2 = start2 + (end2- start2) / 2.0;
        
        boolean ok = false;
        if(b1a.intersects(b2a, start1, half1, start2, half2, t1Ref, t2Ref)) {
            ok = true;
        }
        if (b1a.intersects(b2b, start1, half1, half2, end2, t1Ref, t2Ref)) {
            ok = true;
        }
        if (b1b.intersects(b2a, half1, end1, start2, half2, t1Ref, t2Ref)) {
            ok = true;
        }
        if (b1b.intersects(b2b, half1, end1, half2, end2, t1Ref, t2Ref)) {
            ok = true;
        }
        return ok;
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        for(Point2D p:points) {
            list.add("["+p.getX()+","+p.getY()+"]");
        }
        return "{"+String.join("-", list)+"}";
    }
    
    public void split(double t, Reference<BezierEdge> left, Reference<BezierEdge> right) {
        List<Point2D> leftPoints = new ArrayList<>();
        List<Point2D> rightPoints = new ArrayList<>();
        BezierUtils bu = new BezierUtils();
        bu.subdivide(points, t, leftPoints, rightPoints);
        left.setVal(new BezierEdge(leftPoints));
        right.setVal(new BezierEdge(rightPoints));
    }
    
    
    
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
        
        
        BezierEdge be5 = new BezierEdge(25,0,25,100);
        BezierEdge be6 = new BezierEdge(0,50,100,50);
        
        System.out.println("lines "+ be5+ " and "+be6);
        
        System.out.println("hasIntersection = "+be5.intersects(be6, t1, t2));
        System.out.println("intersection ts: "+t1+", "+t2);
        
        Point2D c = new Point2D.Double(
                (1-t1.get(0))*be5.points.get(0).getX() + t1.get(0) * be5.points.get(1).getX(),
                (1-t1.get(0))*be5.points.get(0).getY() + t1.get(0) * be5.points.get(1).getY()
        );
        
        System.out.println("Intersection point: " +c);
        
        //Rectangle2D out = new Rectangle2D.Double();
        //rectIntersection(new Rectangle2D.Double(0,0,50,50), new Rectangle2D.Double(0,50,50,50), out);
        //System.out.println("out = "+out);
    }    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.helpers.Reference;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class Path {
        
        private static final double EPS = 1e-9;
        
        public List<BezierEdge> edges = new ArrayList<>();
        public int fillStyle0 = 0;
        public int fillStyle1 = 0;
        public int lineStyle = 0;       
        
        public Area area = null;
        
        public Double areaValue = null;
        public boolean counterClockWise = false;
        public Rectangle2D bbox = null;
        
        public List<Path> children = new ArrayList<>();
        public Path parent = null;
        public boolean filled = false;
        
        private void calculateOrientation() {
            Reference<PathArea.Orientation> orientationRef = new Reference<>(null);
            Reference<Double> areaRef = new Reference<>(0.0);
            PathArea.orientationSingleClosed(area, orientationRef, areaRef);
            areaValue = areaRef.getVal();
            this.counterClockWise = orientationRef.getVal() == PathArea.Orientation.COUNTER_CLOCKWISE;
            this.bbox = area.getBounds2D();
        }
        
        public boolean contains(Path other) {
            if (other.area.isEmpty()) {
                return false;
            }
            Area diff = new Area(other.area);
            diff.subtract(area);
            return diff.isEmpty();
        }
        
        public boolean contains(Point2D point) {
            if (area == null) {
                toArea();
            }
            return area.contains(point);
        }
        
        
        public boolean contains(double x, double y) {
            if (area == null) {
                toArea();
            }
            return area.contains(x, y);
        }
        
        public void round(boolean wasSmall) {
            for (int e = 0; e < edges.size(); e++) {
                BezierEdge be = edges.get(e);
                /*if (wasSmall) {
                    be.roundN(2); //this value works best for #1011, why? Also for #2532 it is okay.
                } else {
                    be.roundN(100); //this value works best for #2165, it's not multiplied by 20 like not small :-(
                }*/
                be.roundN(1);
                if (be.isEmpty()) {
                    edges.remove(e);
                    e--;
                }
            }
        }
        
        public void fromArea() {
            calculateOrientation();
            List<BezierEdge> newEdges = new ArrayList<>();
            PathIterator it = area.getPathIterator(null);
            double[] c = new double[6];

            double startX = 0.0;
            double startY = 0.0; // subpath start (for closing)
            double prevX = 0.0;
            double prevY = 0.0;   // previous "current point"

            while (!it.isDone()) {
                int type = it.currentSegment(c);

                switch (type) {
                    case PathIterator.SEG_MOVETO: {
                        // Start of a new subpath
                        startX = prevX = c[0];
                        startY = prevY = c[1];
                        break;
                    }
                    case PathIterator.SEG_LINETO: {
                        // Line from (prevX, prevY) to (x, y)
                        double x = c[0], y = c[1];
                        newEdges.add(new BezierEdge(prevX, prevY, x, y));
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case PathIterator.SEG_QUADTO: {
                        // Quadratic from (prevX, prevY) with control (cx, cy) to (x, y)
                        double cx = c[0], cy = c[1];
                        double x  = c[2], y  = c[3];
                        newEdges.add(new BezierEdge(prevX, prevY, cx, cy, x, y));
                        prevX = x;
                        prevY = y;
                        break;
                    }
                    case PathIterator.SEG_CUBICTO: {
                        // Area may contain cubics if the original shape had them.
                        // We only support lines and quadratics here.
                        throw new IllegalArgumentException("Cubic Bezier segments (SEG_CUBICTO) are not supported.");
                    }
                    case PathIterator.SEG_CLOSE: {
                        // Close current subpath: add a final edge back to start if not already there
                        if (!almostEqual(prevX, startX) || !almostEqual(prevY, startY)) {
                            newEdges.add(new BezierEdge(prevX, prevY, startX, startY));
                            prevX = startX;
                            prevY = startY;
                        }
                        break;
                    }
                    default:
                        // Should not happen for AWT paths
                        throw new IllegalStateException("Unknown PathIterator segment type: " + type);
                }

                it.next();
            }
            this.edges = newEdges;
        }
        
        private boolean almostEqual(double a, double b) {
            return Math.abs(a - b) <= EPS;
        }
        
        public void toArea() {
            GeneralPath gp = new GeneralPath();
            double x = Double.POSITIVE_INFINITY;
            double y = Double.POSITIVE_INFINITY;
            boolean empty = true;
            for (BezierEdge be : edges) {
                if (be.isEmpty()) {
                    continue;
                }
                if (x != be.getBeginPoint().getX() || y != be.getBeginPoint().getY()) {
                    gp.moveTo(be.getBeginPoint().getX(), be.getBeginPoint().getY());
                }
                if (be.isQuad()) {
                    gp.quadTo(be.points.get(1).getX(), be.points.get(1).getY(), be.getEndPoint().getX(), be.getEndPoint().getY());
                } else {
                    gp.lineTo(be.getEndPoint().getX(), be.getEndPoint().getY());
                }
                x = be.getEndPoint().getX();
                y = be.getEndPoint().getY();
                empty = false;
            }
            try {
                this.area = empty ? new Area() : new Area(gp);
            } catch (InternalError ie) {
                System.err.println("INTERNAL error on PATH " + toString());
                this.area = new Area();
            }
            calculateOrientation();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            Point2D lastPoint = null;
            for (BezierEdge e : edges) {
                if (lastPoint == null || !lastPoint.equals(e.getBeginPoint())) {
                    sb.append("M ").append(e.getBeginPoint().getX()).append(" ").append(e.getBeginPoint().getY()).append(" ");
                }
                if (e.isQuad()) {
                    sb.append("Q ").append(e.points.get(1).getX()).append(" ").append(e.points.get(1).getY()).append(" ");
                } else {
                    sb.append("L ");
                }
                sb.append(e.getEndPoint().getX()).append(" ").append(e.getEndPoint().getY()).append(" ");
                lastPoint = e.getEndPoint();
            }
            return sb.toString().trim();
        }
    }

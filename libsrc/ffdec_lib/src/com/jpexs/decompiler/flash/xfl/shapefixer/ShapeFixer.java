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
package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jpexs.helpers.Reference;
import java.awt.geom.Point2D;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 *
 * @author JPEXS
 */
public class ShapeFixer {

    private void addToEdgeMap(Map<Point2D, List<Edge>> edgeMap, Edge edge) {
        if (!edgeMap.containsKey(edge.getFrom())) {
            edgeMap.put(edge.getFrom(), new ArrayList<>());
        }
        if (!edgeMap.containsKey(edge.getTo())) {
            edgeMap.put(edge.getTo(), new ArrayList<>());
        }
        edgeMap.get(edge.getFrom()).add(edge);
        edgeMap.get(edge.getTo()).add(edge.invert());
    }

    private boolean fixSingleEdge(List<ShapeRecordAdvanced> records) {
        Map<Point2D, List<Edge>> edgeMap = new LinkedHashMap<>();
        double x = 0;
        double y = 0;
        int fillStyle0 = 0;
        int fillStyle1 = 0;

        for (int r = 0; r < records.size(); r++) {
            ShapeRecordAdvanced rec = records.get(r);
            if (rec instanceof StyleChangeRecordAdvanced) {
                StyleChangeRecordAdvanced scr = (StyleChangeRecordAdvanced) rec;
                if (scr.stateNewStyles) {
                    fillStyle0 = 0;
                    fillStyle1 = 0;
                }
                if (scr.stateFillStyle0) {
                    fillStyle0 = scr.fillStyle0;
                }
                if (scr.stateFillStyle1) {
                    fillStyle1 = scr.fillStyle1;
                }
            }
            if (rec instanceof StraightEdgeRecordAdvanced) {
                StraightEdgeRecordAdvanced ser = (StraightEdgeRecordAdvanced) rec;
                addToEdgeMap(edgeMap, new Edge(fillStyle0, fillStyle1, r, false, x, y, x + ser.deltaX, y + ser.deltaY));
            }
            if (rec instanceof CurvedEdgeRecordAdvanced) {
                CurvedEdgeRecordAdvanced cer = (CurvedEdgeRecordAdvanced) rec;
                addToEdgeMap(edgeMap, new Edge(fillStyle0, fillStyle1, r, false,
                        x, y,
                        x + cer.controlDeltaX, y + cer.controlDeltaY,
                        x + cer.controlDeltaX + cer.anchorDeltaX, y + cer.controlDeltaY + cer.anchorDeltaY
                ));
            }
            x = rec.changeX(x);
            y = rec.changeY(y);
        }

        for (Point2D p : edgeMap.keySet()) {
            List<Edge> edges = edgeMap.get(p);
            for (int i = 0; i < edges.size(); i++) {
                for (int j = 0; j < edges.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    Edge edge1 = edges.get(i);
                    Edge edge2 = edges.get(j);
                    List<Edge> newEdges1 = new ArrayList<>();
                    List<Edge> newEdges2 = new ArrayList<>();

                    if (edge1.intersection(edge2, newEdges1, newEdges2)) {

                        /*System.out.println("---------------------------------------");
                        System.out.println("intersection edge "+edge1 + " and " +edge2);
                        System.out.println("newEdges1:");
                        for(Edge e:newEdges1) {
                            System.out.println("..."+e);
                        }
                        System.out.println("newEdges2:");
                        for(Edge e:newEdges2) {
                            System.out.println("..."+e);
                        }*/
                        
                        Point2D a = edge1.getFrom();
                        Point2D b = edge1.getTo();
                        Point2D c = edge2.getTo();

                        boolean edge2isRight = ((b.getX() - a.getX()) * (c.getY() - a.getY())) - ((b.getY() - a.getY()) * (c.getX() - a.getX())) > 0;
                        //edge2isRight = !edge2isRight;
                        
                        fillStyle0 = edge2isRight ? edge1.fillStyle0 : edge2.fillStyle0;
                        fillStyle1 = edge2isRight ? edge2.fillStyle1 : edge1.fillStyle1;

                        StyleChangeRecordAdvanced moveCenter = new StyleChangeRecordAdvanced();
                        moveCenter.stateMoveTo = true;
                        moveCenter.moveDeltaX = edge1.getFrom().getX();
                        moveCenter.moveDeltaY = edge1.getFrom().getY();
                        moveCenter.stateFillStyle0 = true;
                        moveCenter.stateFillStyle1 = true;
                        moveCenter.fillStyle0 = fillStyle0;
                        moveCenter.fillStyle1 = fillStyle1;

                        //common line
                        StraightEdgeRecordAdvanced ser = new StraightEdgeRecordAdvanced();
                        ser.deltaX = newEdges1.get(0).getTo().getX() - edge1.getFrom().getX();
                        ser.deltaY = newEdges1.get(0).getTo().getY() - edge1.getFrom().getY();
                        
                        StyleChangeRecordAdvanced scrStart1 = new StyleChangeRecordAdvanced();
                        scrStart1.stateMoveTo = false;
                        scrStart1.stateFillStyle0 = true;
                        scrStart1.stateFillStyle1 = true;
                        scrStart1.fillStyle0 = newEdges1.get(1).fillStyle0;
                        scrStart1.fillStyle1 = newEdges1.get(1).fillStyle1;
                        
                        StyleChangeRecordAdvanced moveBack1 = new StyleChangeRecordAdvanced();
                        moveBack1.stateMoveTo = true;
                        Edge edge1NotInverted = edge1.inverted ? edge1.invert() : edge1;

                        moveBack1.moveDeltaX = edge1NotInverted.getTo().getX();
                        moveBack1.moveDeltaY = edge1NotInverted.getTo().getY();
                        moveBack1.stateFillStyle0 = true;
                        moveBack1.stateFillStyle1 = true;
                        moveBack1.fillStyle0 = edge1NotInverted.fillStyle0;
                        moveBack1.fillStyle1 = edge1NotInverted.fillStyle1;

                        records.remove(edge1.recordIndex);
                        records.add(edge1.recordIndex, moveCenter);
                        records.add(edge1.recordIndex + 1, ser);
                        records.add(edge1.recordIndex + 2, scrStart1);
                        records.add(edge1.recordIndex + 3, newEdges1.get(1).toShapeRecordAdvanced());
                        records.add(edge1.recordIndex + 4, moveBack1);

                        if (edge2.recordIndex > edge1.recordIndex) {
                            edge2.recordIndex += 4;
                        }

                        StyleChangeRecordAdvanced moveStart2 = new StyleChangeRecordAdvanced();
                        moveStart2.stateMoveTo = true;
                        moveStart2.moveDeltaX = newEdges2.get(1).getFrom().getX();
                        moveStart2.moveDeltaY = newEdges2.get(1).getFrom().getY();
                        moveStart2.stateFillStyle0 = true;
                        moveStart2.stateFillStyle1 = true;
                        moveStart2.fillStyle0 = newEdges2.get(1).fillStyle0;
                        moveStart2.fillStyle1 = newEdges2.get(1).fillStyle1;

                        StyleChangeRecordAdvanced moveBack2 = new StyleChangeRecordAdvanced();
                        moveBack2.stateMoveTo = true;
                        Edge edge2NotInverted = edge2.inverted ? edge2.invert() : edge2;

                        moveBack2.moveDeltaX = edge2NotInverted.getTo().getX();
                        moveBack2.moveDeltaY = edge2NotInverted.getTo().getY();
                        moveBack2.stateFillStyle0 = true;
                        moveBack2.stateFillStyle1 = true;
                        moveBack2.fillStyle0 = edge2NotInverted.fillStyle0;
                        moveBack2.fillStyle1 = edge2NotInverted.fillStyle1;

                        records.remove(edge2.recordIndex);
                        records.add(edge2.recordIndex, moveStart2);
                        records.add(edge2.recordIndex + 1, newEdges2.get(1).toShapeRecordAdvanced());
                        records.add(edge2.recordIndex + 2, moveBack2);                       
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<ShapeRecordAdvanced> fixShape(List<ShapeRecordAdvanced> records) {
        List<ShapeRecordAdvanced> ret = Helper.deepCopy(records);
        while(fixSingleEdge(ret)) {
            //nothing
        }
        for(ShapeRecordAdvanced rec:ret) {
            rec.round();
        }
        return ret;
    }
}

class Edge {

    int recordIndex;
    boolean inverted;
    int fillStyle0 = 0;
    int fillStyle1 = 0;

    List<Point2D> points = new ArrayList<>();

    public Point2D getFrom() {
        return points.get(0);
    }

    public Point2D getTo() {
        return points.get(points.size() - 1);
    }

    public BezierEdge toBezierEdge() {
        return new BezierEdge(new ArrayList<>(points));
    }

    public boolean intersection(Edge otherEdge, List<Edge> newThisEdges, List<Edge> newOtherEdges) {
        List<Double> t1s = new ArrayList<>();
        List<Double> t2s = new ArrayList<>();
        BezierEdge be1 = this.toBezierEdge();
        BezierEdge be2 = otherEdge.toBezierEdge();
        if (!be1.intersects(be2, t1s, t2s)) {
            return false;
        }
        Reference<BezierEdge> be1aRef = new Reference<>(null);
        Reference<BezierEdge> be1bRef = new Reference<>(null);
        Reference<BezierEdge> be2aRef = new Reference<>(null);
        Reference<BezierEdge> be2bRef = new Reference<>(null);

        t1s.sort(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return Double.compare(o1, o2);
            }
        });

        t2s.sort(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                return Double.compare(o1, o2);
            }
        });

        if (t1s.size() == 1) {
            return false;
        }

        double t1 = t1s.get(t1s.size() - 1);
        double t2 = t2s.get(t2s.size() - 1);
        /*System.out.println("t1 = "+t1);
        System.out.println("t2 = "+t2);*/

        be1.split(t1, be1aRef, be1bRef);
        be2.split(t2, be2aRef, be2bRef);

        newThisEdges.add(new Edge(this.fillStyle0, this.fillStyle1, -1, this.inverted, be1aRef.getVal()));
        newThisEdges.add(new Edge(this.fillStyle0, this.fillStyle1, -1, this.inverted, be1bRef.getVal()));

        newOtherEdges.add(new Edge(otherEdge.fillStyle0, otherEdge.fillStyle1, -1, otherEdge.inverted, be2aRef.getVal()));
        newOtherEdges.add(new Edge(otherEdge.fillStyle0, otherEdge.fillStyle1, -1, otherEdge.inverted, be2bRef.getVal()));

        if (newThisEdges.get(0).isEmpty() || newOtherEdges.get(0).isEmpty()) {
            newThisEdges.clear();
            newOtherEdges.clear();
            return false;
        }
        
        return true;
    }

    public Point2D pointAt(double t) {
        return toBezierEdge().pointAt(t);
    }

    public Edge invert() {
        List<Point2D> newPoints = new ArrayList<>();
        for (int i = points.size() - 1; i >= 0; i--) {
            newPoints.add(points.get(i));
        }
        return new Edge(fillStyle1, fillStyle0, recordIndex, !inverted, newPoints);
    }

    public Edge(int fillStyle0, int fillStyle1, int recordIndex, boolean inverted, BezierEdge be) {
        List<Point2D> points2D = be.points;
        List<Point2D> points = new ArrayList<>();
        for (Point2D p : points2D) {
            points.add(new Point2D.Double(p.getX(),p.getY()));
        }
        this.points = points;
        this.recordIndex = recordIndex;
        this.inverted = inverted;
        this.fillStyle0 = fillStyle0;
        this.fillStyle1 = fillStyle1;
    }

    public Edge(int fillStyle0, int fillStyle1, int recordIndex, boolean inverted, List<Point2D> points) {
        this.points = points;
        this.recordIndex = recordIndex;
        this.inverted = inverted;
        this.fillStyle0 = fillStyle0;
        this.fillStyle1 = fillStyle1;
    }

    public Edge(int fillStyle0, int fillStyle1, int recordIndex, boolean inverted, double fromX, double fromY, double toX, double toY) {
        points.add(new Point2D.Double(fromX, fromY));
        points.add(new Point2D.Double(toX, toY));
        this.recordIndex = recordIndex;
        this.inverted = inverted;
        this.fillStyle0 = fillStyle0;
        this.fillStyle1 = fillStyle1;
    }

    public Edge(int fillStyle0, int fillStyle1, int recordIndex, boolean inverted, double fromX, double fromY, double controlX, double controlY, double toX, double toY) {
        points.add(new Point2D.Double(fromX, fromY));
        points.add(new Point2D.Double(controlX, controlY));
        points.add(new Point2D.Double(toX, toY));
        this.recordIndex = recordIndex;
        this.inverted = inverted;
        this.fillStyle0 = fillStyle0;
        this.fillStyle1 = fillStyle1;

    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        for (Point2D p : points) {
            list.add("[" + p.getX() + "," + p.getY() + "]");
        }
        return "{" + String.join("-", list) + "}";
    }
    
    public boolean isEmpty() {
        return getFrom().equals(getTo());
    }

    public ShapeRecordAdvanced toShapeRecordAdvanced() {
        if (points.size() == 3) {
            CurvedEdgeRecordAdvanced cer = new CurvedEdgeRecordAdvanced();
            cer.controlDeltaX = points.get(1).getX() - points.get(0).getX();
            cer.controlDeltaY = points.get(1).getY() - points.get(0).getY();
            cer.anchorDeltaX = points.get(2).getX() - points.get(1).getX();
            cer.anchorDeltaY = points.get(2).getY() - points.get(1).getY();
            return cer;
        }
        StraightEdgeRecordAdvanced ser = new StraightEdgeRecordAdvanced();
        ser.deltaX = points.get(1).getX() - points.get(0).getX();
        ser.deltaY = points.get(1).getY() - points.get(0).getY();
        return ser;
    }
}

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
package com.jpexs.decompiler.flash.xfl.shapefixer;

import com.jpexs.decompiler.flash.math.BezierEdge;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Switched fill sides fixer. Float version. This will fix orientation of
 * fillstyle0 and fillstyle1 to be on left and right side of the vector.
 *
 * WIP: incomplete, use non-float version instead
 *
 * @author JPEXS
 */
public class SwitchedFillSidesFixerFloat {

    class Edge {

        int fromId;
        int controlId = -1;
        int toId;

        int fillStyleIdx;

        public Edge(int fromId, int controlId, int toId, int fillStyleIdx) {
            this.fromId = fromId;
            this.controlId = controlId;
            this.toId = toId;
            this.fillStyleIdx = fillStyleIdx;
        }

        public Edge(int fromId, int toId, int fillStyleIdx) {
            this.fromId = fromId;
            this.toId = toId;
            this.fillStyleIdx = fillStyleIdx;
        }

        public Edge reverseWithNewFillStyle(int newFillStyleIdx) {
            return new Edge(toId, controlId, fromId, newFillStyleIdx);
        }

        public Edge reverse() {
            return new Edge(toId, controlId, fromId, fillStyleIdx);
        }

        public Edge sameWithNewFillStyle(int newFillStyleIdx) {
            return new Edge(fromId, controlId, toId, newFillStyleIdx);
        }

        public BezierEdge toBezierEdge(List<Point2D> idToPoint) {
            Point2D from = idToPoint.get(fromId);
            Point2D to = idToPoint.get(toId);
            if (controlId != -1) {
                Point2D control = idToPoint.get(controlId);
                return new BezierEdge(Arrays.asList(from, control, to));
            }
            return new BezierEdge(Arrays.asList(from, to));
        }
    }

    boolean USE_REVERSE_LOOKUP = true;

    private Map<Integer, List<Edge>> createEdgeMap(
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> layers,
            int from,
            int to,
            List<Point2D> idToPoint,
            Map<Point2D, Integer> pointToId
    ) {

        Map<Integer, List<Edge>> currentFillEdgeMap = new HashMap<>();

        for (int i = from; i < to; i++) {
            List<Edge> subPath = new ArrayList<>();
            for (BezierEdge be : shapes.get(i)) {
                int fromId = pointToId.get(be.getBeginPoint());
                int toId = pointToId.get(be.getEndPoint());
                int controlId = -1;
                if (be.points.size() == 3) {
                    controlId = pointToId.get(be.points.get(1));
                }
                subPath.add(new Edge(fromId, controlId, toId, fillStyles1.get(i)));
            }
            processSubPath(subPath, fillStyles0.get(i), fillStyles1.get(i), currentFillEdgeMap);
        }

        cleanEdgeMap(currentFillEdgeMap);

        return currentFillEdgeMap;
    }

    private void processSubPath(List<Edge> subPath, int fillStyleIdx0, int fillStyleIdx1,
            Map<Integer, List<Edge>> currentFillEdgeMap) {
        List<Edge> path;
        if (fillStyleIdx0 != 0) {
            path = currentFillEdgeMap.get(fillStyleIdx0);
            if (path == null) {
                path = new ArrayList<>();
                currentFillEdgeMap.put(fillStyleIdx0, path);
            }
            for (int j = subPath.size() - 1; j >= 0; j--) {
                Edge rev = subPath.get(j).reverseWithNewFillStyle(fillStyleIdx0);
                path.add(rev);
            }

        }
        if (fillStyleIdx1 != 0) {
            path = currentFillEdgeMap.get(fillStyleIdx1);
            if (path == null) {
                path = new ArrayList<>();
                currentFillEdgeMap.put(fillStyleIdx1, path);
            }
            appendEdges(path, subPath);
        }
    }

    private List<Edge> createPathFromEdgeMap(Map<Integer, List<Edge>> edgeMap) {
        List<Edge> newPath = new ArrayList<>();
        List<Integer> styleIdxArray = new ArrayList<>();
        for (Integer styleIdx : edgeMap.keySet()) {
            styleIdxArray.add(styleIdx);
        }
        Collections.sort(styleIdxArray);
        for (int i = 0; i < styleIdxArray.size(); i++) {
            appendEdges(newPath, edgeMap.get(styleIdxArray.get(i)));
        }
        return newPath;
    }

    private void appendEdges(List<Edge> v1, List<Edge> v2) {
        for (int i = 0; i < v2.size(); i++) {
            v1.add(v2.get(i));
        }
    }

    private void cleanEdgeMap(Map<Integer, List<Edge>> edgeMap) {
        for (Integer styleIdx : edgeMap.keySet()) {
            List<Edge> subPath = edgeMap.get(styleIdx);
            if (subPath != null && !subPath.isEmpty()) {
                int idx;
                Edge prevEdge = null;
                List<Edge> tmpPath = new ArrayList<>();
                Map<Integer, List<Edge>> coordMap = createCoordMap(subPath);
                Map<Integer, List<Edge>> reverseCoordMap = createReverseCoordMap(subPath);
                while (!subPath.isEmpty()) {
                    idx = 0;
                    while (idx < subPath.size()) {
                        if (prevEdge != null) {
                            Edge subPathEdge = subPath.get(idx);
                            if (prevEdge.toId != subPathEdge.fromId) {
                                Edge edge = findNextEdgeInCoordMap(coordMap, prevEdge);
                                if (edge != null) {
                                    idx = subPath.indexOf(edge);
                                } else {
                                    Edge revEdge = findNextEdgeInCoordMap(reverseCoordMap, prevEdge);

                                    if (revEdge != null) {
                                        if (USE_REVERSE_LOOKUP) {
                                            idx = subPath.indexOf(revEdge);
                                            Edge r = revEdge.reverseWithNewFillStyle(revEdge.fillStyleIdx);
                                            updateEdgeInCoordMap(coordMap, revEdge, r);
                                            updateEdgeInReverseCoordMap(reverseCoordMap, revEdge, r);
                                            subPath.set(idx, r);
                                        } else {
                                            idx = 0;
                                            prevEdge = null;
                                        }
                                    } else {
                                        idx = 0;
                                        prevEdge = null;
                                    }
                                }
                                continue;
                            }
                        }

                        Edge edge = subPath.remove(idx);
                        tmpPath.add(edge);
                        removeEdgeFromCoordMap(coordMap, edge);
                        removeEdgeFromReverseCoordMap(reverseCoordMap, edge);
                        prevEdge = edge;
                    }
                }
                edgeMap.put(styleIdx, tmpPath);
            }
        }
    }

    private Map<Integer, List<Edge>> createCoordMap(List<Edge> path) {
        Map<Integer, List<Edge>> coordMap = new HashMap<>();
        for (int i = 0; i < path.size(); i++) {
            Edge edge = path.get(i);
            List<Edge> coordMapArray = coordMap.get(edge.fromId);
            if (coordMapArray == null) {
                List<Edge> list = new ArrayList<>();
                list.add(path.get(i));
                coordMap.put(edge.fromId, list);
            } else {
                coordMapArray.add(path.get(i));
            }
        }
        return coordMap;
    }

    private Map<Integer, List<Edge>> createReverseCoordMap(List<Edge> path) {
        Map<Integer, List<Edge>> coordMap = new HashMap<>();
        for (int i = 0; i < path.size(); i++) {
            Edge edge = path.get(i);
            List<Edge> coordMapArray = coordMap.get(edge.toId);
            if (coordMapArray == null) {
                List<Edge> list = new ArrayList<>();
                list.add(path.get(i));
                coordMap.put(edge.toId, list);
            } else {
                coordMapArray.add(path.get(i));
            }
        }
        return coordMap;
    }

    private void removeEdgeFromCoordMap(Map<Integer, List<Edge>> coordMap, Edge edge) {
        List<Edge> coordMapArray = coordMap.get(edge.fromId);
        if (coordMapArray != null) {
            if (coordMapArray.size() == 1) {
                coordMap.remove(edge.fromId);
            } else {
                int i = coordMapArray.indexOf(edge);
                if (i > -1) {
                    coordMapArray.remove(i);
                }
            }
        }
    }

    private void removeEdgeFromReverseCoordMap(Map<Integer, List<Edge>> coordMap, Edge edge) {
        List<Edge> coordMapArray = coordMap.get(edge.toId);
        if (coordMapArray != null) {
            if (coordMapArray.size() == 1) {
                coordMap.remove(edge.toId);
            } else {
                int i = coordMapArray.indexOf(edge);
                if (i > -1) {
                    coordMapArray.remove(i);
                }
            }
        }
    }

    private Edge findNextEdgeInCoordMap(Map<Integer, List<Edge>> coordMap, Edge edge) {
        List<Edge> coordMapArray = coordMap.get(edge.toId);
        if (coordMapArray != null && !coordMapArray.isEmpty()) {
            return coordMapArray.get(0);
        }
        return null;
    }

    private Edge updateEdgeInCoordMap(Map<Integer, List<Edge>> coordMap, Edge edge, Edge newEdge) {
        coordMap.get(edge.fromId).remove(edge);

        if (!coordMap.containsKey(newEdge.fromId)) {
            coordMap.put(newEdge.fromId, new ArrayList<>());
        }
        coordMap.get(newEdge.fromId).add(newEdge);
        return null;
    }

    private Edge updateEdgeInReverseCoordMap(Map<Integer, List<Edge>> coordMap, Edge edge, Edge newEdge) {

        coordMap.get(edge.toId).remove(edge);

        if (!coordMap.containsKey(newEdge.toId)) {
            coordMap.put(newEdge.toId, new ArrayList<>());
        }
        coordMap.get(newEdge.toId).add(newEdge);
        return null;
    }

    private void fixSidesInLayer(List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> layers,
            int from,
            int to) {
        Set<Point2D> allPoints = new LinkedHashSet<>();

        for (int i = from; i < to; i++) {
            for (BezierEdge be : shapes.get(i)) {
                for (Point2D p : be.points) {
                    allPoints.add(p);
                }
            }
        }
        List<Point2D> idToPoint = new ArrayList<>(allPoints);
        Map<Point2D, Integer> pointToId = new HashMap<>();
        for (int i = 0; i < idToPoint.size(); i++) {
            pointToId.put(idToPoint.get(i), i);
        }
        Map<Integer, List<Edge>> currentFillEdgeMap = createEdgeMap(shapes, fillStyles0, fillStyles1, layers, from, to, idToPoint, pointToId);

        List<Edge> edges = createPathFromEdgeMap(currentFillEdgeMap);

        //-------------------------------------
        int fillStyleIdx = Integer.MAX_VALUE;
        List<Edge> currentList = new ArrayList<>();
        List<List<Edge>> allLists = new ArrayList<>();
        List<Integer> listFills = new ArrayList<>();
        int lastTo = -1;
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            if (fillStyleIdx != e.fillStyleIdx) { //|| e.fromId != lastTo) {
                if (fillStyleIdx != Integer.MAX_VALUE) {
                    allLists.add(currentList);
                    listFills.add(fillStyleIdx);
                    currentList = new ArrayList<>();
                }
                fillStyleIdx = e.fillStyleIdx;
            }
            currentList.add(e);
            lastTo = e.toId;
        }
        if (!currentList.isEmpty()) {
            allLists.add(currentList);
            listFills.add(fillStyleIdx);
        }

        Map<BezierEdge, Integer> beToFillStyle0 = new LinkedHashMap<>();
        Map<BezierEdge, Integer> beToFillStyle1 = new LinkedHashMap<>();

        for (int i = 0; i < allLists.size(); i++) {
            List<Edge> list = allLists.get(i);
            fillStyleIdx = listFills.get(i);

            double poly = 0;
            for (Edge e : list) {
                Point2D fromP = idToPoint.get(e.fromId);
                Point2D toP;
                if (e.controlId != -1) {
                    toP = idToPoint.get(e.controlId);
                    poly += fromP.getX() * toP.getY() - toP.getX() * fromP.getY();
                    fromP = toP;
                }
                toP = idToPoint.get(e.toId);
                poly += fromP.getX() * toP.getY() - toP.getX() * fromP.getY();
            }

            boolean clockwise = poly > 0;
            for (Edge e : list) {
                BezierEdge be = e.toBezierEdge(idToPoint);
                BezierEdge beRev = be.reverse();

                /*if (be.getBeginPoint().equals(new Point2D.Double(12580.0,4280.0))
                   && be.getEndPoint().equals(new Point2D.Double(12680.0,4240.0))) {
                    System.err.println("xxx: " + be);
                    System.err.println("FS: " + fillStyleIdx);
                    System.err.println("ClockWise: " + clockwise);
                }
                
                if (be.getBeginPoint().equals(new Point2D.Double(12680.0,4240.0))
                   && be.getEndPoint().equals(new Point2D.Double(12580.0,4280.0))) {
                    System.err.println("xxx2: " + be);
                    System.err.println("FS: " + fillStyleIdx);
                    System.err.println("ClockWise: " + clockwise);
                }*/
                if (be.getBeginPoint().equals(new Point2D.Double(12500.0, 3580.0))
                        && be.points.get(1).equals(new Point2D.Double(12520.0, 3600.0))
                        && be.getEndPoint().equals(new Point2D.Double(12560.0, 3580.0))) {
                    System.err.println("xxx: " + be);
                    System.err.println("FS: " + fillStyleIdx);
                    System.err.println("ClockWise: " + clockwise);
                }

                if (be.getBeginPoint().equals(new Point2D.Double(12560.0, 3580.0))
                        && be.points.get(1).equals(new Point2D.Double(12520.0, 3600.0))
                        && be.getEndPoint().equals(new Point2D.Double(12500.0, 3580.0))) {
                    System.err.println("xxx2: " + be);
                    System.err.println("FS: " + fillStyleIdx);
                    System.err.println("ClockWise: " + clockwise);
                }

                if (clockwise) {
                    beToFillStyle1.put(be, fillStyleIdx);
                    beToFillStyle0.put(beRev, fillStyleIdx);
                } else {
                    beToFillStyle0.put(be, fillStyleIdx);
                    beToFillStyle1.put(beRev, fillStyleIdx);
                }
            }

        }

        for (int i = from; i < to; i++) {
            List<BezierEdge> shape = shapes.get(i);
            for (int j = 0; j < shape.size(); j++) {
                BezierEdge be = shape.get(j);
                Integer fs0before = fillStyles0.get(i);
                Integer fs1before = fillStyles1.get(i);

                if (fs0before == 0 && fs1before == 0) { //only strokes
                    break;
                }

                if (be.getBeginPoint().equals(new Point2D.Double(12580.0, 4280.0))
                        && be.getEndPoint().equals(new Point2D.Double(12680.0, 4240.0))) {
                    System.err.println("yyy");
                }

                Integer fs0after = beToFillStyle0.get(be);
                Integer fs1after = beToFillStyle1.get(be);

                if (fs0after == null) {
                    fs0after = 0;
                }
                if (fs1after == null) {
                    fs1after = 0;
                }

                fillStyles0.set(i, fs0after);
                fillStyles1.set(i, fs1after);

                if (!Objects.equals(fs0before, fs0after) || !Objects.equals(fs1before, fs1after)) {
                    Logger.getLogger(SwitchedFillSidesFixerFloat.class.getName()).log(Level.FINE, "Changed edge {0} - old: {1}, {2} new: {3}, {4}", new Object[]{be, fs0before, fs1before, fs0after, fs1after});
                }
                break;
            }
        }
    }

    public void fixSwitchedFills(
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> layers
    ) {

        int from = 0;
        for (int i = 1; i < layers.size(); i++) {
            if (!layers.get(i).equals(layers.get(i - 1))) {
                fixSidesInLayer(shapes, fillStyles0, fillStyles1, layers, from, i);
                from = i;
            }
        }
        if (!layers.isEmpty()) {
            fixSidesInLayer(shapes, fillStyles0, fillStyles1, layers, from, layers.size());
        }
    }
}

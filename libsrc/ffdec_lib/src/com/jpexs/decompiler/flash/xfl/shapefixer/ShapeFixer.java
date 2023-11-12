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
import com.jpexs.decompiler.flash.math.Distances;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Reference;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Shape fixer. This will walk a shape and split crossed edges so FLA editor can
 * properly load it. It also fixes morphshape - removes duplicated paths, calculate holes.
 *
 * @author JPEXS
 */
public class ShapeFixer {

    boolean DEBUG_PRINT = false;

    private class BezierPair {

        BezierEdge be1;
        BezierEdge be2;
        private Integer hash;

        public BezierPair(BezierEdge be1, BezierEdge be2) {
            this.be1 = be1;
            this.be2 = be2;
        }

        @Override
        public int hashCode() {
            if (hash != null) {
                return hash;
            }
            return hash = Objects.hashCode(this.be1) + Objects.hashCode(this.be2);
        }

        @Override
        public boolean equals(Object obj) {
            //simplified to be fast, not so neccessarily accurate
            return hashCode() == obj.hashCode();
            /*if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final BezierPair other = (BezierPair) obj;
            if ((Objects.equals(this.be1, other.be1) && Objects.equals(this.be2, other.be2))
                    || (Objects.equals(this.be1, other.be2) && Objects.equals(this.be2, other.be1))) {
                return true;
            }
            return false;*/
        }

    }

    private boolean isEmptyBatch(List<BezierEdge> batch) {
        for (BezierEdge be : batch) {
            if (!be.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public List<ShapeRecordAdvanced> fix(
            List<SHAPERECORD> records,
            boolean morphshape, 
            int shapeNum,
            FILLSTYLEARRAY baseFillStyles,
            LINESTYLEARRAY baseLineStyles
    ) {
        List<List<BezierEdge>> shapes = new ArrayList<>();
        List<BezierEdge> currentShape = new ArrayList<>();
        List<Integer> fillStyles0 = new ArrayList<>();
        List<Integer> fillStyles1 = new ArrayList<>();
        List<Integer> lineStyles = new ArrayList<>();
        List<Integer> layers = new ArrayList<>();
        List<FILLSTYLEARRAY> fillStyleLayers = new ArrayList<>();
        List<LINESTYLEARRAY> lineStyleLayers = new ArrayList<>();

        int fillStyle0 = 0;
        int fillStyle1 = 0;
        int lineStyle = 0;
        int layer = -1;
        int x = 0;
        int y = 0;
        for (SHAPERECORD rec : records) {
            if (rec instanceof StyleChangeRecord) {
                StyleChangeRecord scr = (StyleChangeRecord) rec;
                if (scr.stateMoveTo
                        || scr.stateNewStyles
                        || scr.stateFillStyle0
                        || scr.stateFillStyle1
                        || scr.stateLineStyle) {
                    if (!currentShape.isEmpty()) {
                        shapes.add(currentShape);
                        fillStyles0.add(fillStyle0);
                        fillStyles1.add(fillStyle1);
                        lineStyles.add(lineStyle);
                        layers.add(layer);
                        currentShape = new ArrayList<>();
                    }
                }
                if (scr.stateNewStyles) {
                    layer++;
                    fillStyle0 = 0;
                    fillStyle1 = 0;
                    lineStyle = 0;
                    fillStyleLayers.add(scr.fillStyles);
                    lineStyleLayers.add(scr.lineStyles);
                }
                if (scr.stateFillStyle0) {
                    fillStyle0 = scr.fillStyle0;
                }
                if (scr.stateFillStyle1) {
                    fillStyle1 = scr.fillStyle1;
                }
                if (scr.stateLineStyle) {
                    lineStyle = scr.lineStyle;
                }
            }
            if (rec instanceof StraightEdgeRecord) {
                int x2 = rec.changeX(x);
                int y2 = rec.changeY(y);
                BezierEdge be = new BezierEdge(x, y, x2, y2);
                currentShape.add(be);
            }
            if (rec instanceof CurvedEdgeRecord) {
                CurvedEdgeRecord cer = (CurvedEdgeRecord) rec;
                int cx = x + cer.controlDeltaX;
                int cy = y + cer.controlDeltaY;
                int ax = cx + cer.anchorDeltaX;
                int ay = cy + cer.anchorDeltaY;
                BezierEdge be = new BezierEdge(x, y, cx, cy, ax, ay);
                currentShape.add(be);
            }
            if (rec instanceof EndShapeRecord) {
                if (!currentShape.isEmpty()) {
                    shapes.add(currentShape);
                    fillStyles0.add(fillStyle0);
                    fillStyles1.add(fillStyle1);
                    lineStyles.add(lineStyle);
                    layers.add(layer);
                    currentShape = new ArrayList<>();
                }
            }
            x = rec.changeX(x);
            y = rec.changeY(y);
        }

        if (morphshape) {

            //Remove empty edges
            for (int i = 0; i < shapes.size(); i++) {
                List<BezierEdge> list = shapes.get(i);
                for (int j = 0; j < list.size(); j++) {
                    if (list.get(j).isEmpty()) {
                        list.remove(j);
                        j--;
                    }
                }
            }

            List<List<BezierEdge>> closedShapes = new ArrayList<>();
            List<Integer> closedFillStyle = new ArrayList<>();
            List<Integer> closedLineStyle = new ArrayList<>();
            List<Integer> closedLayers = new ArrayList<>();
            List<Integer> closedShapesI = new ArrayList<>();
            List<Integer> closedShapesJ = new ArrayList<>();

            Map<Integer, Integer> replacements = new LinkedHashMap<>();

            for (int i = 0; i < shapes.size(); i++) {
                Point2D lastMoveTo = null;
                BezierEdge lastEdge = null;
                int moveToIndex = 0;
                List<BezierEdge> batch = new ArrayList<>();
                for (int j = 0; j < shapes.get(i).size(); j++) {
                    BezierEdge be = shapes.get(i).get(j);
                    batch.add(be);
                    if (lastMoveTo != null && be.getEndPoint().equals(lastMoveTo)) {
                        closedFillStyle.add(fillStyles0.get(i));
                        closedLineStyle.add(lineStyles.get(i));
                        closedShapes.add(batch);
                        closedLayers.add(layers.get(i));
                        closedShapesI.add(i);
                        closedShapesJ.add(moveToIndex);
                        moveToIndex = j + 1;
                        lastMoveTo = be.getEndPoint();
                        batch = new ArrayList<>();
                    }
                    if (lastEdge != null) {
                        if (!lastEdge.getEndPoint().equals(be.getBeginPoint())) {
                            lastMoveTo = be.getBeginPoint();
                            moveToIndex = j;
                        }
                    } else {
                        lastMoveTo = be.getBeginPoint();
                    }
                    lastEdge = be;
                }
            }

            Set<Integer> removedShapes = new HashSet<>();
            for (int i1 = 0; i1 < closedShapes.size(); i1++) {
                if (replacements.containsKey(i1)) {
                    continue;
                }
                for (int i2 = 0; i2 < closedShapes.size(); i2++) {
                    if (i1 == i2) {
                        continue;
                    }
                    if (replacements.containsKey(i2)) {
                        continue;
                    }
                    if (closedLayers.get(i1) != closedLayers.get(i2)) {
                        continue;
                    }
                    if (closedFillStyle.get(i1) > 0 && closedFillStyle.get(i2) > 0) {
                        if (closedFillStyle.get(i1) != closedFillStyle.get(i2)) {
                            FILLSTYLEARRAY fa = closedLayers.get(i1) == -1 ? baseFillStyles : fillStyleLayers.get(closedLayers.get(i1));
                            FILLSTYLE fs1 = fa.fillStyles[closedFillStyle.get(i1) - 1];
                            FILLSTYLE fs2 = fa.fillStyles[closedFillStyle.get(i2) - 1];
                            if (!fs1.equals(fs2)) {
                                continue;
                            }
                        }
                    }
                    if (closedLineStyle.get(i1) > 0 && closedLineStyle.get(i2) > 0) {
                        if (closedLineStyle.get(i1) != closedLineStyle.get(i2)) {
                            LINESTYLEARRAY lsa = closedLayers.get(i1) == -1 ? baseLineStyles : lineStyleLayers.get(closedLayers.get(i1));
                            if (shapeNum <= 3) {
                                LINESTYLE ls1 = lsa.lineStyles[closedLineStyle.get(i1) - 1];
                                LINESTYLE ls2 = lsa.lineStyles[closedLineStyle.get(i2) - 1];
                                if (!ls1.equals(ls2)) {
                                    continue;
                                }
                            } else {
                                LINESTYLE2 ls1 = lsa.lineStyles2[closedLineStyle.get(i1) - 1];
                                LINESTYLE2 ls2 = lsa.lineStyles2[closedLineStyle.get(i2) - 1];
                                if (!ls1.equals(ls2)) {
                                    continue;
                                }
                            }
                        }
                    }

                    if (closedShapes.get(i1).size() <= 1 || closedShapes.get(i2).size() <= 1) {
                        continue;
                    }

                    if (closedLineStyle.get(i1) > 0 && closedLineStyle.get(i2) == 0) {
                        continue;
                    }

                    if (isEmptyBatch(closedShapes.get(i1)) || isEmptyBatch(closedShapes.get(i2))) {
                        continue;
                    }

                    double dist = Distances.getBatchDistance(closedShapes.get(i1), closedShapes.get(i2));

                    if (dist <= 10) { //magic
                        /*System.err.println("dist = " + dist);
                        System.err.println("removed");
                        System.err.println("removed shape["+closedShapesI.get(i2)+"]["+closedShapesJ.get(i2)+"], fs "+ closedFillStyle.get(i2) + ", ls " + closedLineStyle.get(i2));
                        System.err.println("left shape["+closedShapesI.get(i1)+"]["+closedShapesJ.get(i1)+"], fs "+ closedFillStyle.get(i1)+ ", ls " + closedLineStyle.get(i1));
                         */
                        replacements.put(i2, i1);
                        removedShapes.add(i2);
                    }
                }
            }

            for (int i = 0; i < closedShapes.size(); i++) {
                int repI = replacements.containsKey(i) ? replacements.get(i) : i;
                List<BezierEdge> listI = closedShapes.get(repI);

                for (int j = i + 1; j < closedShapes.size(); j++) {
                    /*if (i == j) {
                        break;
                    }*/
                    if (removedShapes.contains(j) && !replacements.containsKey(j)) {
                        continue;
                    }
                    int repJ = replacements.containsKey(j) ? replacements.get(j) : j;
                    List<BezierEdge> listJ = closedShapes.get(repJ);

                    if (closedFillStyle.get(i) != closedFillStyle.get(j)
                            || closedLineStyle.get(i) != closedLineStyle.get(j)) {
                        continue;
                    }

                    if (listI.equals(listJ)) {
                        replacements.remove(j);
                        removedShapes.add(j);
                    }
                }
            }

            for (int i = closedShapes.size() - 1; i >= 0; i--) {
                int to = closedShapesJ.get(i) + closedShapes.get(i).size() - 1;
                int from = closedShapesJ.get(i);
                List<BezierEdge> list = shapes.get(closedShapesI.get(i));

                if (removedShapes.contains(i)) {
                    //System.err.println("removing shape["+closedShapesI.get(i)+"]["+from+" to "+to+"]");
                    for (int j = to; j >= from; j--) {
                        list.remove(j);
                    }
                }
                if (replacements.containsKey(i)) {
                    list.addAll(from, closedShapes.get(replacements.get(i)));
                }

                if (!removedShapes.contains(i)) {
                    for (int j = to; j >= from; j--) {
                        if (list.get(j).isEmpty()) {
                            list.remove(j);
                        }
                    }
                }
            }

            /*
            * This will remove a stroked path with no fill which has same
            * stroke as subsequent path (or is its prefix). This happens in the
            * morphshape edges. This needs to be cleaned up before exporting to FLA.
             */
            List<BezierEdge> prevList = null;
            int prevI = -1;
            for (int i = 0; i < shapes.size(); i++) {
                List<BezierEdge> list = shapes.get(i);
                if (list.isEmpty()) {
                    continue;
                }
                if (prevList != null) {

                    int prevFillStyle0 = fillStyles0.get(i - 1);
                    int prevFillStyle1 = fillStyles1.get(i - 1);
                    int prevLineStyle = lineStyles.get(i - 1);
                    lineStyle = lineStyles.get(i);
                    fillStyle0 = fillStyles0.get(i);
                    fillStyle1 = fillStyles1.get(i);

                    if (fillStyle0 == 0 && fillStyle1 == 0 && lineStyle != 0 && lineStyle == prevLineStyle) {
                        if (prevList.size() >= list.size()) {
                            boolean isPrefix = true;
                            for (int j = 0; j < list.size(); j++) {
                                if (!prevList.get(j).equals(list.get(j))) {
                                    isPrefix = false;
                                    break;
                                }
                            }
                            if (isPrefix) {
                                shapes.get(i).clear();
                                continue;
                            }
                        }
                    } else if (prevFillStyle0 == 0 && prevFillStyle1 == 0 && prevLineStyle != 0 && lineStyle == prevLineStyle) {
                        //list startswitch prevList
                        if (list.size() >= prevList.size()) {
                            boolean isPrefix = true;
                            for (int j = 0; j < prevList.size(); j++) {
                                if (!prevList.get(j).equals(list.get(j))) {
                                    isPrefix = false;
                                    break;
                                }
                            }
                            if (isPrefix) {
                                shapes.get(prevI).clear();
                            }
                        }
                    }
                }
                prevI = i;
                prevList = list;
            }

            //Clear obvious duplicates
            //System.err.println("Clear obvious duplicates...");
            prevList = null;
            prevI = -1;
            //if (false)
            for (int i = 0; i < shapes.size(); i++) {
                List<BezierEdge> list = shapes.get(i);
                if (list.isEmpty()) {
                    continue;
                }
                if (prevList != null) {
                    if (fillStyles0.get(i) == fillStyles0.get(prevI)
                            && lineStyles.get(i) == lineStyles.get(prevI)) {
                        if (list.equals(prevList)) {
                            System.err.println("clearing " + i);
                            prevList.clear();
                        }
                    }
                }
                prevI = i;
                prevList = list;
            }

            //----------------------------FIND "holes" = apply wind even odd -------------
            closedShapes = new ArrayList<>();
            closedFillStyle = new ArrayList<>();
            closedLineStyle = new ArrayList<>();
            closedLayers = new ArrayList<>();
            closedShapesI = new ArrayList<>();
            closedShapesJ = new ArrayList<>();
            Set<Integer> closedHolesI = new LinkedHashSet<>();

            for (int i = 0; i < shapes.size(); i++) {
                Point2D lastMoveTo = null;
                BezierEdge lastEdge = null;
                int moveToIndex = 0;
                List<BezierEdge> batch = new ArrayList<>();
                for (int j = 0; j < shapes.get(i).size(); j++) {
                    BezierEdge be = shapes.get(i).get(j);
                    batch.add(be);
                    if (lastMoveTo != null && be.getEndPoint().equals(lastMoveTo)) {
                        closedFillStyle.add(fillStyles0.get(i));
                        closedLineStyle.add(lineStyles.get(i));
                        closedShapes.add(batch);
                        closedLayers.add(layers.get(i));
                        closedShapesI.add(i);
                        closedShapesJ.add(moveToIndex);
                        moveToIndex = j + 1;
                        lastMoveTo = be.getEndPoint();
                        batch = new ArrayList<>();
                    }
                    if (lastEdge != null) {
                        if (!lastEdge.getEndPoint().equals(be.getBeginPoint())) {
                            lastMoveTo = be.getBeginPoint();
                            moveToIndex = j;
                        }
                    } else {
                        lastMoveTo = be.getBeginPoint();
                    }
                    lastEdge = be;
                }
            }

            //reversing anti-clockwise
            for (int i = 0; i < closedShapes.size(); i++) {
                List<BezierEdge> list = closedShapes.get(i);
                if (list.isEmpty()) {
                    continue;
                }
                List<Point2D> points = new ArrayList<>();
                points.add(list.get(0).getBeginPoint());
                for (BezierEdge be : list) {
                    if (be.points.size() == 3) {
                        points.add(be.points.get(1));
                    }
                    points.add(be.getEndPoint());
                }
                double sum = 0;
                for (int j = 0; j < points.size(); j++) {
                    Point2D p1 = points.get(j);
                    Point2D p2 = points.get((j + 1) % points.size());
                    sum += (p1.getX() * p2.getY() - p2.getX() * p1.getY());
                }

                if (sum < 0) { //anti clockwise
                    //reverse the list
                    List<BezierEdge> rev = new ArrayList<>();
                    for (int j = 0; j < list.size(); j++) {
                        rev.add(list.get(list.size() - 1 - j).reverse());
                    }

                    int shapeI = closedShapesI.get(i);
                    int shapeJ = closedShapesJ.get(i);
                    for (int j = 0; j < list.size(); j++) {
                        shapes.get(shapeI).set(shapeJ + j, rev.get(j));
                    }
                }
            }

            Map<Integer, List<Integer>> fillStyleToClosed = new LinkedHashMap<>();
            for (int i = 0; i < closedShapes.size(); i++) {
                int fs = closedFillStyle.get(i);
                if (fs != 0) {
                    if (!fillStyleToClosed.containsKey(fs)) {
                        fillStyleToClosed.put(fs, new ArrayList<>());
                    }
                    fillStyleToClosed.get(fs).add(i);
                }
            }
            for (int fs : fillStyleToClosed.keySet()) {
                GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                Map<Integer, GeneralPath> closedPaths = new LinkedHashMap<>();
                for (int i : fillStyleToClosed.get(fs)) {
                    List<BezierEdge> closed = closedShapes.get(i);
                    if (closed.isEmpty()) {
                        continue;
                    }
                    GeneralPath closedPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

                    closedPath.moveTo(closed.get(0).getBeginPoint().getX(), closed.get(0).getBeginPoint().getY());
                    path.moveTo(closed.get(0).getBeginPoint().getX(), closed.get(0).getBeginPoint().getY());
                    boolean isEmpty = true;
                    for (BezierEdge be : closed) {
                        if (be.isEmpty()) {
                            continue;
                        }
                        isEmpty = false;
                        if (be.points.size() == 3) {
                            closedPath.quadTo(be.points.get(1).getX(), be.points.get(1).getY(), be.getEndPoint().getX(), be.getEndPoint().getY());
                            path.quadTo(be.points.get(1).getX(), be.points.get(1).getY(), be.getEndPoint().getX(), be.getEndPoint().getY());
                        } else {
                            closedPath.lineTo(be.getEndPoint().getX(), be.getEndPoint().getY());
                            path.lineTo(be.getEndPoint().getX(), be.getEndPoint().getY());
                        }
                    }
                    closedPath.closePath();
                    path.closePath();
                    if (!isEmpty) {
                        closedPaths.put(i, closedPath);
                    }
                }

                for (int i : closedPaths.keySet()) {
                    GeneralPath region = closedPaths.get(i);
                    Rectangle r = region.getBounds();
                    double px;
                    double py;
                    do {
                        px = r.getX() + r.getWidth() * Math.random();
                        py = r.getY() + r.getHeight() * Math.random();
                    } while (!region.contains(px, py));

                    if (!path.contains(px, py)) {
                        closedHolesI.add(i);
                    }
                }
            }

            for (int i = closedShapes.size() - 1; i >= 0; i--) {
                if (closedHolesI.contains(i)) {
                    int to = closedShapesJ.get(i) + closedShapes.get(i).size() - 1;
                    int from = closedShapesJ.get(i);
                    List<BezierEdge> list = shapes.get(closedShapesI.get(i));

                    //System.err.println("removing hole["+closedShapesI.get(i)+"]["+from+" to "+to+"]");
                    for (int j = to; j >= from; j--) {
                        list.remove(j);
                    }

                    int shapeI = closedShapesI.get(i);
                    shapes.add(shapeI + 1, closedShapes.get(i));
                    closedShapes.set(i, new ArrayList<>());
                    fillStyles0.add(shapeI + 1, 0);
                    fillStyles1.add(shapeI + 1, fillStyles0.get(shapeI));
                    lineStyles.add(shapeI + 1, lineStyles.get(shapeI));
                    layers.add(shapeI + 1, layers.get(shapeI));
                }
            }
        }

        
        //------------------- detecting overlapping edges --------------------
        List<BezierPair> splittedPairs = new ArrayList<>();

        loopi1:
        for (int i1 = 0; i1 < shapes.size(); i1++) {
            layer = layers.get(i1);
            loopj1:
            for (int j1 = 0; j1 < shapes.get(i1).size(); j1++) {
                BezierEdge be1 = shapes.get(i1).get(j1);
                for (int i2 = 0; i2 < shapes.size(); i2++) {
                    if (layers.get(i2) != layer) {
                        continue;
                    }
                    loopj2:
                    for (int j2 = 0; j2 < shapes.get(i2).size(); j2++) {
                        BezierEdge be2 = shapes.get(i2).get(j2);

                        if (i1 == i2 && j1 == j2) {
                            continue;
                        }

                        if (be1.isEmpty()) {
                            shapes.get(i1).remove(j1);
                            if (i1 == i2 && j2 > j1) {
                                j2--;
                            }
                            j1--;
                            continue loopj1;
                        }
                        if (be2.isEmpty()) {
                            shapes.get(i2).remove(j2);
                            if (i1 == i2 && j1 > j2) {
                                j1--;
                            }
                            j2--;
                            continue loopj2;
                        }

                        //duplicated edge
                        if (be1.equals(be2) || be1.equals(be2.reverse())) {
                            shapes.get(i2).remove(j2);
                            if (i1 == i2 && j1 > j2) {
                                j1--;
                            }
                            j2--;
                            if (DEBUG_PRINT) {
                                System.err.println("removing duplicate " + be1.toSvg() + " and " + be2.toSvg());
                            }
                            continue;
                        }

                        BezierPair pair = new BezierPair(be1, be2);
                        if (splittedPairs.contains(pair)) {
                            continue;
                        }

                        List<Double> t1Ref = new ArrayList<>();
                        List<Double> t2Ref = new ArrayList<>();
                        List<Point2D> intPoints = new ArrayList<>();

                        if (DEBUG_PRINT) {
                            //System.err.print("checking shape[" + i1 + "][" + j1 + "] to shape[" + i2 + "][" + j2 + "] : " + be1.toSvg() + " and " + be2.toSvg());
                        }

                        boolean isint = be1.intersects(be2, t1Ref, t2Ref, intPoints);
                        if (DEBUG_PRINT) {
                            //System.err.println(" " + isint);
                        }

                        if (!t1Ref.isEmpty()) {

                            if ((be1.getBeginPoint().equals(be2.getBeginPoint())
                                    || be1.getBeginPoint().equals(be2.getEndPoint())
                                    || be1.getEndPoint().equals(be2.getBeginPoint())
                                    || be1.getEndPoint().equals(be2.getEndPoint())) && (t1Ref.size() == 1)) {
                                continue;
                            }

                            if (DEBUG_PRINT) {
                                System.err.println("intersects " + be1.toSvg() + "   " + be2.toSvg());
                                System.err.println(" fillstyle0: " + fillStyles0.get(i1) + " , " + fillStyles0.get(i2));
                                System.err.println(" fillstyle1: " + fillStyles1.get(i1) + " , " + fillStyles1.get(i2));
                                System.err.println(" linestyle: " + lineStyles.get(i1) + " , " + lineStyles.get(i2));

                                for (int n = 0; n < t1Ref.size(); n++) {
                                    System.err.println("- " + t1Ref.get(n) + " , " + t2Ref.get(n) + " : " + intPoints.get(n));
                                }
                            }

                            if ((t1Ref.size() == 2) && !((t1Ref.get(0) == 0 || t1Ref.get(0) == 1)
                                    && (t2Ref.get(0) == 0 || t2Ref.get(0) == 1))) {
                                t1Ref.add(0, t1Ref.remove(1));
                                t2Ref.add(0, t2Ref.remove(1));
                                intPoints.add(0, intPoints.remove(1));
                            }

                            int splitPointIndex = 0;
                            if (intPoints.size() > 1) {
                                splitPointIndex = 1;
                            }

                            splittedPairs.add(pair);

                            Reference<BezierEdge> be1LRef = new Reference<>(null);
                            Reference<BezierEdge> be1RRef = new Reference<>(null);
                            be1.split(t1Ref.get(splitPointIndex), be1LRef, be1RRef);
                            Reference<BezierEdge> be2LRef = new Reference<>(null);
                            Reference<BezierEdge> be2RRef = new Reference<>(null);
                            be2.split(t2Ref.get(splitPointIndex), be2LRef, be2RRef);

                            BezierEdge be1L = be1LRef.getVal();
                            BezierEdge be1R = be1RRef.getVal();
                            BezierEdge be2L = be2LRef.getVal();
                            BezierEdge be2R = be2RRef.getVal();

                            Point2D intP = intPoints.get(splitPointIndex);

                            be1L.setEndPoint(intP);
                            be1R.setBeginPoint(intP);
                            be2L.setEndPoint(intP);
                            be2R.setBeginPoint(intP);

                            be1L.roundHalf();
                            be1R.roundHalf();
                            be2L.roundHalf();
                            be2R.roundHalf();
                            if (i1 == i2) {
                                if (j1 < j2) {
                                    shapes.get(i1).remove(j2);
                                    shapes.get(i2).remove(j1);
                                    j2--;
                                } else {
                                    shapes.get(i1).remove(j1);
                                    shapes.get(i2).remove(j2);
                                    j1--;
                                }
                            } else {
                                shapes.get(i1).remove(j1);
                                shapes.get(i2).remove(j2);
                            }
                            int n1 = j1;
                            int n2 = j2;

                            if (!be1L.isEmpty()) {
                                shapes.get(i1).add(n1, be1L);
                                if (DEBUG_PRINT) {
                                    System.err.println("added " + be1L.toSvg() + " to j1=" + n1);
                                }
                                if (i1 == i2 && n2 >= n1) {
                                    n2++;
                                }
                                n1++;

                            }

                            if (!be1R.isEmpty()) {
                                shapes.get(i1).add(n1, be1R);
                                if (DEBUG_PRINT) {
                                    System.err.println("added " + be1R.toSvg() + " to j1=" + n1);
                                }
                                if (i1 == i2 && n2 >= n1) {
                                    n2++;
                                }
                                n1++;

                            }

                            if (!be2L.isEmpty()) {
                                shapes.get(i2).add(n2, be2L);
                                if (DEBUG_PRINT) {
                                    System.err.println("added " + be2L.toSvg() + " to j2=" + n2);
                                }
                                n2++;

                            }

                            if (!be2R.isEmpty()) {
                                shapes.get(i2).add(n2, be2R);
                                if (DEBUG_PRINT) {
                                    System.err.println("added " + be2R.toSvg() + " to j2=" + n2);
                                }
                                n2++;
                            }

                            j1--;
                            continue loopj1;
                        }
                    }
                }
            }
        }

        List<ShapeRecordAdvanced> ret = new ArrayList<>();

        layer = -1;
        double dx = 0;
        double dy = 0;
        for (int i = 0; i < shapes.size(); i++) {
            List<BezierEdge> bes = shapes.get(i);
            if (bes.isEmpty()) {
                continue;
            }
            StyleChangeRecordAdvanced scr = new StyleChangeRecordAdvanced();
            scr.stateMoveTo = true;
            dx = scr.moveDeltaX = bes.get(0).points.get(0).getX();
            dy = scr.moveDeltaY = bes.get(0).points.get(0).getY();

            int newLayer = layers.get(i);
            if (newLayer != layer) {
                scr.stateNewStyles = true;
                scr.fillStyles = fillStyleLayers.get(newLayer);
                scr.lineStyles = lineStyleLayers.get(newLayer);
            }
            layer = newLayer;

            scr.stateFillStyle0 = true;
            scr.fillStyle0 = fillStyles0.get(i);
            scr.stateFillStyle1 = true;
            scr.fillStyle1 = fillStyles1.get(i);
            scr.stateLineStyle = true;
            scr.lineStyle = lineStyles.get(i);
            ret.add(scr);
            for (BezierEdge be : bes) {
                if (!be.getBeginPoint().equals(new Point2D.Double(dx, dy))) {
                    StyleChangeRecordAdvanced sm = new StyleChangeRecordAdvanced();
                    sm.stateMoveTo = true;
                    sm.moveDeltaX = be.getBeginPoint().getX();
                    sm.moveDeltaY = be.getBeginPoint().getY();
                    ret.add(sm);
                }
                dx = be.getEndPoint().getX();
                dy = be.getEndPoint().getY();

                ShapeRecordAdvanced sra = bezierToAdvancedRecord(be);
                ret.add(sra);
            }
        }
        return ret;
    }

    private ShapeRecordAdvanced bezierToAdvancedRecord(BezierEdge be) {
        if (be.points.size() == 2) {
            StraightEdgeRecordAdvanced ser = new StraightEdgeRecordAdvanced();
            ser.deltaX = be.points.get(1).getX() - be.points.get(0).getX();
            ser.deltaY = be.points.get(1).getY() - be.points.get(0).getY();
            return ser;
        }
        if (be.points.size() == 3) {
            CurvedEdgeRecordAdvanced cer = new CurvedEdgeRecordAdvanced();
            cer.controlDeltaX = be.points.get(1).getX() - be.points.get(0).getX();
            cer.controlDeltaY = be.points.get(1).getY() - be.points.get(0).getY();
            cer.anchorDeltaX = be.points.get(2).getX() - be.points.get(1).getX();
            cer.anchorDeltaY = be.points.get(2).getY() - be.points.get(1).getY();
            return cer;
        }
        return null;
    }
}

/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Reference;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Shape fixer. This will walk a shape and split crossed edges so FLA editor can
 * properly load it.
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
            //simplified to be fast, not so necessarily accurate
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

    protected void beforeHandle(
            int shapeNum,
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> lineStyles,
            List<Integer> layers,
            FILLSTYLEARRAY baseFillStyles,
            LINESTYLEARRAY baseLineStyles,
            List<FILLSTYLEARRAY> fillStyleLayers,
            List<LINESTYLEARRAY> lineStyleLayers
    ) {
    }

    private void detectOverlappingEdges(
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> lineStyles,
            List<Integer> layers
    ) {
        //------------------- detecting overlapping edges --------------------
        List<BezierPair> splittedPairs = new ArrayList<>();

        //long t1 = System.currentTimeMillis();
        //int oldpct = 0;
        loopi1:
        for (int i1 = 0; i1 < shapes.size(); i1++) {
            int layer = layers.get(i1);
            /*if (i1 % 10 == 0) {
                int pct = i1 * 100 / shapes.size();
                if (oldpct != pct) {
                    //System.err.println("pct: " + pct + "%");
                    if (pct == 10) {
                        long t2 = System.currentTimeMillis();
                        long dt = t2 - t1;
                        System.err.println("Time per 10%: " + Helper.formatTimeSec(dt));
                    }
                }
                oldpct = pct;
            }*/

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
                        if (be1.equals(be2) || be1.equalsReverse(be2)) {
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
                        if (!isint) {
                            continue;
                        }

                        if (t1Ref.isEmpty()) {
                            continue;
                        }

                        if ((be1.getBeginPoint().equals(be2.getBeginPoint())
                                || be1.getBeginPoint().equals(be2.getEndPoint())
                                || be1.getEndPoint().equals(be2.getBeginPoint())
                                || be1.getEndPoint().equals(be2.getEndPoint())) && (t1Ref.size() == 1)) {
                            continue;
                        }

                        if (t1Ref.size() == 2) {
                            if ((t1Ref.get(0) == 0 || t1Ref.get(0) == 1)
                                    && (t2Ref.get(0) == 0 || t2Ref.get(0) == 1)) {
                                continue;
                            }
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

    private void splitToLayers(
            List<SHAPERECORD> records,
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> lineStyles,
            List<Integer> layers,
            List<FILLSTYLEARRAY> fillStyleLayers,
            List<LINESTYLEARRAY> lineStyleLayers
    ) {
        List<BezierEdge> currentShape = new ArrayList<>();

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
    }

    private List<ShapeRecordAdvanced> combineLayers(
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> lineStyles,
            List<Integer> layers,
            List<FILLSTYLEARRAY> fillStyleLayers,
            List<LINESTYLEARRAY> lineStyleLayers
    ) {
        List<ShapeRecordAdvanced> ret = new ArrayList<>();
        int layer = -1;
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

    public List<ShapeRecordAdvanced> fix(
            List<SHAPERECORD> records,
            int shapeNum,
            FILLSTYLEARRAY baseFillStyles,
            LINESTYLEARRAY baseLineStyles
    ) {
        List<List<BezierEdge>> shapes = new ArrayList<>();
        List<Integer> fillStyles0 = new ArrayList<>();
        List<Integer> fillStyles1 = new ArrayList<>();
        List<Integer> lineStyles = new ArrayList<>();
        List<Integer> layers = new ArrayList<>();
        List<FILLSTYLEARRAY> fillStyleLayers = new ArrayList<>();
        List<LINESTYLEARRAY> lineStyleLayers = new ArrayList<>();

        splitToLayers(records, shapes, fillStyles0, fillStyles1, lineStyles, layers, fillStyleLayers, lineStyleLayers);

        beforeHandle(shapeNum, shapes, fillStyles0, fillStyles1, lineStyles, layers, baseFillStyles, baseLineStyles, fillStyleLayers, lineStyleLayers);

        if (Configuration.flaExportFixShapes.get()) {
            detectOverlappingEdges(shapes, fillStyles0, fillStyles1, lineStyles, layers);
        }

        return combineLayers(shapes, fillStyles0, fillStyles1, lineStyles, layers, fillStyleLayers, lineStyleLayers);
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

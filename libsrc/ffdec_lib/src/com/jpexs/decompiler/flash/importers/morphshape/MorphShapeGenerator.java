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
package com.jpexs.decompiler.flash.importers.morphshape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Reference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Morph shape generator.
 *
 * @author JPEXS
 */
public class MorphShapeGenerator {

    /**
     * Generates morph shape.
     *
     * @param morphShape Morph shape
     * @param startShape Start shape
     * @param endShape End shape
     * @throws StyleMismatchException On style mismatch
     */
    public void generate(DefineMorphShape2Tag morphShape, ShapeTag startShape, ShapeTag endShape) throws StyleMismatchException {

        SWF swf = morphShape.getSwf();

        ShapeForMorphExporter startExport = new ShapeForMorphExporter(startShape);
        startExport.export();
        ShapeForMorphExporter endExport = new ShapeForMorphExporter(endShape);
        endExport.export();

        List<List<BezierEdge>> startBeziers = new ArrayList<>();
        List<List<BezierEdge>> endBeziers = new ArrayList<>();
        List<FILLSTYLE> startFillStyles = new ArrayList<>();
        List<Integer> startFillstyleIndices = new ArrayList<>();
        List<LINESTYLE2> startLineStyles = new ArrayList<>();
        List<Integer> startLineStyleIndices = new ArrayList<>();

        List<FILLSTYLE> endFillStyles = new ArrayList<>();
        List<LINESTYLE2> endLineStyles = new ArrayList<>();

        Set<Integer> usedBs = new HashSet<>();

        List<Integer> startShapeIndices = new ArrayList<>();
        List<Integer> endShapeIndices = new ArrayList<>();

        for (int a = 0; a < startExport.shapes.size(); a++) {
            double minDistance = Double.MAX_VALUE;
            double minDistanceNoUsed = Double.MAX_VALUE;
            int selectedB = -1;
            int selectedBNoUsed = -1;

            int startFillStyleIndex = startExport.fillStyleIndices.get(a);
            FILLSTYLE startFillStyle = startFillStyleIndex == -1 ? null : startExport.fillStyles.get(startFillStyleIndex);
            int startLineStyleIndex = startExport.lineStyleIndices.get(a);
            LINESTYLE2 startLineStyle = startLineStyleIndex == -1 ? null : startExport.lineStyles.get(startLineStyleIndex);

            for (int b = 0; b < endExport.shapes.size(); b++) {
                int endFillStyleIndex = endExport.fillStyleIndices.get(b);
                FILLSTYLE endFillStyle = endFillStyleIndex == -1 ? null : endExport.fillStyles.get(endFillStyleIndex);
                int endLineStyleIndex = endExport.lineStyleIndices.get(b);
                LINESTYLE2 endLineStyle = endLineStyleIndex == -1 ? null : endExport.lineStyles.get(endLineStyleIndex);

                if (((endFillStyle == null && startFillStyle == null)
                        || (startFillStyle != null
                        && endFillStyle != null
                        && startFillStyle.isCompatibleFillStyle(endFillStyle, swf)))
                        && ((endLineStyle == null && startLineStyle == null)
                        || (startLineStyle != null
                        && endLineStyle != null
                        && startLineStyle.isCompatibleLineStyle(endLineStyle, swf)))) {
                    double distance = startExport.centralPos.get(a).distance(endExport.centralPos.get(b));
                    if (distance < minDistance) {
                        minDistance = distance;
                        selectedB = b;
                    }

                    if (distance < minDistanceNoUsed && !usedBs.contains(b)) {
                        minDistanceNoUsed = distance;
                        selectedBNoUsed = b;
                    }
                }
            }
            if (selectedB == -1) {
                throw new StyleMismatchException();
            }
            if (selectedBNoUsed != -1) {
                selectedB = selectedBNoUsed;
            }
            startShapeIndices.add(a);
            endShapeIndices.add(selectedB);
            usedBs.add(selectedB);
        }

        if (usedBs.size() < endExport.shapes.size()) {
            for (int b = 0; b < endExport.shapes.size(); b++) {
                if (!usedBs.contains(b)) {

                    double minDistance = Double.MAX_VALUE;
                    int selectedA = -1;

                    int endFillStyleIndex = endExport.fillStyleIndices.get(b);
                    FILLSTYLE endFillStyle = endFillStyleIndex == -1 ? null : endExport.fillStyles.get(endFillStyleIndex);
                    int endLineStyleIndex = endExport.lineStyleIndices.get(b);
                    LINESTYLE2 endLineStyle = endLineStyleIndex == -1 ? null : endExport.lineStyles.get(endLineStyleIndex);

                    for (int a = 0; a < startExport.shapes.size(); a++) {
                        int startFillStyleIndex = startExport.fillStyleIndices.get(a);
                        FILLSTYLE startFillStyle = startFillStyleIndex == -1 ? null : startExport.fillStyles.get(startFillStyleIndex);
                        int startLineStyleIndex = startExport.lineStyleIndices.get(a);
                        LINESTYLE2 startLineStyle = startLineStyleIndex == -1 ? null : startExport.lineStyles.get(startLineStyleIndex);

                        if (((endFillStyle == null && startFillStyle == null)
                                || (startFillStyle != null
                                && endFillStyle != null
                                && startFillStyle.isCompatibleFillStyle(endFillStyle, swf)))
                                && ((endLineStyle == null && startLineStyle == null)
                                || (startLineStyle != null
                                && endLineStyle != null
                                && startLineStyle.isCompatibleLineStyle(endLineStyle, swf)))) {
                            double distance = startExport.centralPos.get(a).distance(endExport.centralPos.get(b));
                            if (distance < minDistance) {
                                minDistance = distance;
                                selectedA = a;
                            }
                        }
                    }
                    if (selectedA == -1) {
                        throw new StyleMismatchException();
                    }

                    startShapeIndices.add(selectedA);
                    endShapeIndices.add(b);
                }
            }
        }

        for (int i = 0; i < startShapeIndices.size(); i++) {
            int a = startShapeIndices.get(i);
            int b = endShapeIndices.get(i);

            List<BezierEdge> shapeStart = Helper.deepCopy(startExport.shapes.get(a));
            split(shapeStart, startExport.pointsPosPercent.get(a), endExport.pointsPosPercent.get(b));
            List<BezierEdge> shapeEnd = Helper.deepCopy(endExport.shapes.get(b));
            split(shapeEnd, endExport.pointsPosPercent.get(b), startExport.pointsPosPercent.get(a));

            startBeziers.add(shapeStart);
            endBeziers.add(shapeEnd);

            if (startExport.fillStyleIndices.get(a) != -1) {
                startFillStyles.add(startExport.fillStyles.get(startExport.fillStyleIndices.get(a)));
            }
            startFillstyleIndices.add(startExport.fillStyleIndices.get(a));
            if (startExport.lineStyleIndices.get(a) != -1) {
                startLineStyles.add(startExport.lineStyles.get(startExport.lineStyleIndices.get(a)));
            }
            startLineStyleIndices.add(startExport.lineStyleIndices.get(a));

            if (endExport.fillStyleIndices.get(b) != -1) {
                endFillStyles.add(endExport.fillStyles.get(endExport.fillStyleIndices.get(b)));
            }
            if (endExport.lineStyleIndices.get(b) != -1) {
                endLineStyles.add(endExport.lineStyles.get(endExport.lineStyleIndices.get(b)));
            }
        }

        List<SHAPERECORD> startRecords = new ArrayList<>();
        List<SHAPERECORD> endRecords = new ArrayList<>();

        MORPHFILLSTYLEARRAY morphFillStyleArray = new MORPHFILLSTYLEARRAY();
        morphFillStyleArray.fillStyles = new MORPHFILLSTYLE[startFillStyles.size()];

        MORPHLINESTYLEARRAY morphLineStyleArray = new MORPHLINESTYLEARRAY();
        morphLineStyleArray.lineStyles2 = new MORPHLINESTYLE2[startLineStyles.size()];

        for (int i = 0; i < startFillStyles.size(); i++) {
            FILLSTYLE fsStart = startFillStyles.get(i);
            FILLSTYLE fsEnd = endFillStyles.get(i);
            MORPHFILLSTYLE morphFillStyle = fsStart.toMorphStyle(fsEnd, swf);
            if (morphFillStyle == null) {
                throw new StyleMismatchException();
            }
            morphFillStyleArray.fillStyles[i] = morphFillStyle;
        }

        for (int i = 0; i < endFillStyles.size(); i++) {
            FILLSTYLE fsStart = startFillStyles.get(i);
            FILLSTYLE fsEnd = endFillStyles.get(i);
            if (fsEnd.hasBitmap() && fsEnd.bitmapId != fsStart.bitmapId) {
                swf.removeTag(swf.getImage(fsEnd.bitmapId));
            }
        }

        for (int i = 0; i < startLineStyles.size(); i++) {
            LINESTYLE2 lsStart = startLineStyles.get(i);
            LINESTYLE2 lsEnd = endLineStyles.get(i);
            MORPHLINESTYLE2 morphLineStyle = lsStart.toMorphLineStyle2(lsEnd, swf);
            if (morphLineStyle == null) {
                throw new StyleMismatchException();
            }
            morphLineStyleArray.lineStyles2[i] = morphLineStyle;
            if (morphLineStyle.noHScaleFlag || morphLineStyle.noVScaleFlag) {
                morphShape.usesNonScalingStrokes = true;
            }
            if (!morphLineStyle.noHScaleFlag && !morphLineStyle.noVScaleFlag) {
                morphShape.usesScalingStrokes = true;
            }
        }

        for (int i = 0; i < startLineStyles.size(); i++) {
            LINESTYLE2 lsStart = startLineStyles.get(i);
            LINESTYLE2 lsEnd = endLineStyles.get(i);
            if (lsEnd.hasFillFlag && lsEnd.fillType.hasBitmap() && lsStart.fillType.bitmapId != lsEnd.fillType.bitmapId) {
                swf.removeTag(swf.getImage(lsEnd.fillType.bitmapId));
            }
        }

        for (int i = 0; i < startBeziers.size(); i++) {
            List<BezierEdge> beList = startBeziers.get(i);
            StyleChangeRecord scr = new StyleChangeRecord();
            scr.stateFillStyle0 = true;
            if (startFillstyleIndices.get(i) != -1) {
                scr.fillStyle0 = startFillstyleIndices.get(i) + 1;
            } else {
                scr.fillStyle0 = 0;
            }
            scr.stateLineStyle = true;
            if (startLineStyleIndices.get(i) != -1) {
                scr.lineStyle = startLineStyleIndices.get(i) + 1;
            } else {
                scr.lineStyle = 0;
            }
            startRecords.add(scr);

            BezierEdge firstBe = beList.get(0);
            StyleChangeRecord scrMove = new StyleChangeRecord();
            scrMove.stateMoveTo = true;
            scrMove.moveDeltaX = (int) Math.round(firstBe.getBeginPoint().getX());
            scrMove.moveDeltaY = (int) Math.round(firstBe.getBeginPoint().getY());
            startRecords.add(scrMove);

            for (BezierEdge be : beList) {
                SHAPERECORD rec = bezierToRecord(be);
                startRecords.add(rec);
            }
        }
        startRecords.add(new EndShapeRecord());

        for (int i = 0; i < endBeziers.size(); i++) {
            List<BezierEdge> beList = endBeziers.get(i);
            BezierEdge firstBe = beList.get(0);
            StyleChangeRecord scrMove = new StyleChangeRecord();
            scrMove.stateMoveTo = true;
            scrMove.moveDeltaX = (int) Math.round(firstBe.getBeginPoint().getX());
            scrMove.moveDeltaY = (int) Math.round(firstBe.getBeginPoint().getY());
            endRecords.add(scrMove);
            for (BezierEdge be : beList) {
                SHAPERECORD rec = bezierToRecord(be);
                endRecords.add(rec);
            }
        }
        endRecords.add(new EndShapeRecord());

        morphShape.morphFillStyles = morphFillStyleArray;
        morphShape.morphLineStyles = morphLineStyleArray;
        morphShape.startEdges = new SHAPE();
        morphShape.startEdges.shapeRecords = startRecords;
        morphShape.startEdges.numFillBits = SWFOutputStream.getNeededBitsU(morphFillStyleArray.fillStyles.length);
        morphShape.startEdges.numLineBits = SWFOutputStream.getNeededBitsU(morphLineStyleArray.lineStyles2.length);

        morphShape.endEdges = new SHAPE();
        morphShape.endEdges.numFillBits = 0;
        morphShape.endEdges.numLineBits = 0;
        morphShape.endEdges.shapeRecords = endRecords;

        morphShape.setModified(true);
        morphShape.updateBounds();
    }

    private void split(List<BezierEdge> shape, List<Double> originalPointsPosPercent, List<Double> newPointPosPercent) {
        List<Double> pointPointsPercent = new ArrayList<>(originalPointsPosPercent);

        int nppPos = 0;
        for (int i = 0; i < shape.size() && nppPos < newPointPosPercent.size(); i++) {
            BezierEdge be = shape.get(i);
            double pointPosPct = pointPointsPercent.get(i);
            double pointPosNextPct = pointPointsPercent.get(i + 1);

            double pct = newPointPosPercent.get(nppPos);
            if (pct > pointPosPct && pct < pointPosNextPct) {
                double deltaPct = pointPosNextPct - pointPosPct;
                double newPct = pct - pointPosPct;
                double insidePct = newPct / deltaPct;
                Reference<BezierEdge> leftRef = new Reference<>(null);
                Reference<BezierEdge> rightRef = new Reference<>(null);
                be.split(insidePct, leftRef, rightRef);
                shape.remove(i);
                shape.add(i, leftRef.getVal());
                shape.add(i + 1, rightRef.getVal());
                pointPointsPercent.add(i + 1, pct);
                nppPos++;
            } else if (pct == pointPosPct) {
                nppPos++;
                i--;
            }
        }
    }

    private SHAPERECORD bezierToRecord(BezierEdge be) {
        if (be.points.size() == 2) {
            StraightEdgeRecord ser = new StraightEdgeRecord();
            ser.deltaX = (int) Math.round(be.points.get(1).getX() - be.points.get(0).getX());
            ser.deltaY = (int) Math.round(be.points.get(1).getY() - be.points.get(0).getY());
            ser.generalLineFlag = true;
            ser.simplify();
            return ser;
        }
        if (be.points.size() == 3) {
            CurvedEdgeRecord cer = new CurvedEdgeRecord();
            cer.controlDeltaX = (int) Math.round(be.points.get(1).getX() - be.points.get(0).getX());
            cer.controlDeltaY = (int) Math.round(be.points.get(1).getY() - be.points.get(0).getY());
            cer.anchorDeltaX = (int) Math.round(be.points.get(2).getX() - be.points.get(1).getX());
            cer.anchorDeltaY = (int) Math.round(be.points.get(2).getY() - be.points.get(1).getY());
            return cer;
        }
        return null;
    }
}

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
import com.jpexs.decompiler.flash.math.Distances;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Morphshape fixer. Works as ShapeFixer but also removes duplicated paths,
 * calculates holes.
 *
 * @author JPEXS
 */
public class MorphShapeFixer extends ShapeFixer {

    /**
     * Hook from base ShapeFixer
     *
     * @param shapeNum Shape number (1 = DefineMorphShape, 2 = DefineMorphShape2)
     * @param shapes Shapes
     * @param fillStyles0 Fill styles 0
     * @param fillStyles1 Fill styles 1
     * @param lineStyles Line styles
     * @param layers Layers
     * @param baseFillStyles Base fill styles
     * @param baseLineStyles Base line styles
     * @param fillStyleLayers Fill style layers
     * @param lineStyleLayers Line style layers
     */
    @Override
    protected void beforeHandle(int shapeNum, List<List<BezierEdge>> shapes, List<Integer> fillStyles0, List<Integer> fillStyles1, List<Integer> lineStyles, List<Integer> layers, FILLSTYLEARRAY baseFillStyles, LINESTYLEARRAY baseLineStyles, List<FILLSTYLEARRAY> fillStyleLayers, List<LINESTYLEARRAY> lineStyleLayers) {
        removeEmptyEdges(shapes);
        mergeSimilar(shapeNum, shapes, fillStyles0, lineStyles, layers, baseFillStyles, baseLineStyles, fillStyleLayers, lineStyleLayers);
        mergeWithSamePrefix(shapes, fillStyles0, fillStyles1, lineStyles);
        clearDuplicatePathsNextToEachOther(shapes, fillStyles0, lineStyles);
        fixHolesAndAntiClockwise(shapes, fillStyles0, fillStyles1, lineStyles, layers);
        mergeSamePathsWithOppositeFillstyles(shapes, fillStyles0, fillStyles1, lineStyles, layers);
    }

    /**
     * shape 1 [FS0:A, FS1:-, LS:n], shape 2 [FS0:-, FS1:B, LS:n] => shape 1
     * [FS0:A, FS1:B], remove shape 2
     *
     * @param shapes Shapes
     * @param fillStyles0 Fill styles 0
     * @param fillStyles1 Fill styles 1
     * @param lineStyles Line styles
     * @param layers Layers
     */
    private void mergeSamePathsWithOppositeFillstyles(
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> lineStyles,
            List<Integer> layers
    ) {
        for (int i1 = 0; i1 < shapes.size(); i1++) {
            for (int i2 = 0; i2 < shapes.size(); i2++) {
                if (i1 == i2) {
                    continue;
                }
                if (layers.get(i1) != layers.get(i2)) {
                    continue;
                }
                if (lineStyles.get(i1) != lineStyles.get(i2)) {
                    continue;
                }
                if (!shapes.get(i1).equals(shapes.get(i2))) {
                    continue;
                }
                boolean doRemove = false;
                if (fillStyles0.get(i1) != 0 && fillStyles1.get(i1) == 0
                        && fillStyles1.get(i2) != 0 && fillStyles0.get(i2) == 0) {
                    fillStyles1.set(i1, fillStyles1.get(i2));
                    doRemove = true;
                } else if (fillStyles1.get(i1) != 0 && fillStyles0.get(i1) == 0
                        && fillStyles0.get(i2) != 0 && fillStyles1.get(i2) == 0) {
                    fillStyles0.set(i1, fillStyles0.get(i2));
                    doRemove = true;
                }

                if (doRemove) {
                    shapes.remove(i2);
                    fillStyles0.remove(i2);
                    fillStyles1.remove(i2);
                    lineStyles.remove(i2);
                    layers.remove(i2);
                    i2--;
                }
            }
        }
    }

    private boolean isEmptyPath(List<BezierEdge> path) {
        for (BezierEdge be : path) {
            if (!be.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void fixHolesAndAntiClockwise(
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> lineStyles,
            List<Integer> layers
    ) {
        List<List<BezierEdge>> closedShapes = new ArrayList<>();
        List<Integer> closedFillStyle = new ArrayList<>();
        List<Integer> closedShapesI = new ArrayList<>();
        List<Integer> closedShapesJ = new ArrayList<>();

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
                    closedShapes.add(batch);
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
                /*Rectangle r = region.getBounds();
                double px;
                double py;
                do {
                    px = r.getX() + r.getWidth() * Math.random();
                    py = r.getY() + r.getHeight() * Math.random();
                } while (!region.contains(px, py));*/

                PathIterator pi = region.getPathIterator(null);
                Rectangle2D bounds = region.getBounds2D();
                double centerX = bounds.getCenterX();
                double centerY = bounds.getCenterY();
                int numPoints = 0;
                int numContains = 0;
                int numNotContains = 0;
                double x = 0;
                double y = 0;
                while (!pi.isDone()) {
                    double[] points = new double[6];
                    int type = pi.currentSegment(points);
                    switch (type) {
                        case PathIterator.SEG_MOVETO:
                        case PathIterator.SEG_LINETO:
                            x = points[0];
                            y = points[1];
                            break;
                        case PathIterator.SEG_QUADTO:
                            x = points[2];
                            y = points[3];
                    }

                    numPoints++;
                    double p = Math.sqrt((centerX - x) * (centerX - x) + (centerY - y) * (centerY - y));
                    double x1 = (centerX - x) * 0.1 / p;
                    double y1 = (centerY - y) * 0.1 / p;
                    if (path.contains(x + x1, y + y1)) {
                        numContains++;
                    } else {
                        numNotContains++;
                    }
                    if (numPoints == 4) {
                        break;
                    }
                    pi.next();
                }
                if (numNotContains > numContains) {
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

                //add this path as new with fillstyle1 instead of fillstyle0
                shapes.add(shapeI + 1, closedShapes.get(i));
                closedShapes.set(i, new ArrayList<>());
                fillStyles0.add(shapeI + 1, 0);
                fillStyles1.add(shapeI + 1, fillStyles0.get(shapeI));
                lineStyles.add(shapeI + 1, lineStyles.get(shapeI));
                layers.add(shapeI + 1, layers.get(shapeI));
            }
        }
    }

    private void clearDuplicatePathsNextToEachOther(
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> lineStyles
    ) {
        List<BezierEdge> prevList = null;
        int prevI = -1;
        for (int i = 0; i < shapes.size(); i++) {
            List<BezierEdge> list = shapes.get(i);
            if (list.isEmpty()) {
                continue;
            }
            if (prevList != null) {
                if (fillStyles0.get(i) == fillStyles0.get(prevI)
                        && lineStyles.get(i) == lineStyles.get(prevI)) {
                    if (list.equals(prevList)) {
                        prevList.clear();
                    }
                }
            }
            prevI = i;
            prevList = list;
        }
    }

    /**
     * This will remove a stroked path with no fill which has same stroke as
     * subsequent path (or is its prefix). This happens in the morphshape edges.
     * This needs to be cleaned up before exporting to FLA.
     */
    private void mergeWithSamePrefix(
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> fillStyles1,
            List<Integer> lineStyles
    ) {
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
                int lineStyle = lineStyles.get(i);
                int fillStyle0 = fillStyles0.get(i);
                int fillStyle1 = fillStyles1.get(i);

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
    }

    /**
     * Merges similar paths. This happens in morphshapes when one shape is
     * transformed into multiple shapes.
     *
     * @param shapeNum Shape number (1 = DefineMorphShape, 2 = DefineMorphShape2)
     * @param shapes Shapes
     * @param fillStyles0 Fill styles 0
     * @param lineStyles Fill styles 1
     * @param layers Layers
     * @param baseFillStyles Base fill styles
     * @param baseLineStyles Base line styles
     * @param fillStyleLayers Fill style layers
     * @param lineStyleLayers Line style layers
     */
    private void mergeSimilar(
            int shapeNum,
            List<List<BezierEdge>> shapes,
            List<Integer> fillStyles0,
            List<Integer> lineStyles,
            List<Integer> layers,
            FILLSTYLEARRAY baseFillStyles,
            LINESTYLEARRAY baseLineStyles,
            List<FILLSTYLEARRAY> fillStyleLayers,
            List<LINESTYLEARRAY> lineStyleLayers
    ) {
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

                if (isEmptyPath(closedShapes.get(i1)) || isEmptyPath(closedShapes.get(i2))) {
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
    }

    /**
     * @param shapes Shapes
     */
    private void removeEmptyEdges(List<List<BezierEdge>> shapes) {
        for (int i = 0; i < shapes.size(); i++) {
            List<BezierEdge> list = shapes.get(i);
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).isEmpty()) {
                    list.remove(j);
                    j--;
                }
            }
        }
    }
}

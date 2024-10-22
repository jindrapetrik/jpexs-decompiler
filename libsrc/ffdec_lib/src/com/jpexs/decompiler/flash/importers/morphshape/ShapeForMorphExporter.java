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
package com.jpexs.decompiler.flash.importers.morphshape;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.shape.ShapeExporterBase;
import com.jpexs.decompiler.flash.math.BezierEdge;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.Helper;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Shape for morph exporter.
 *
 * @author JPEXS
 */
public class ShapeForMorphExporter extends ShapeExporterBase {

    /**
     * List of shapes.
     */
    public List<List<BezierEdge>> shapes = new ArrayList<>();
    /**
     * List of fill style indices.
     */
    public List<Integer> fillStyleIndices = new ArrayList<>();
    /**
     * List of line style indices.
     */
    public List<Integer> lineStyleIndices = new ArrayList<>();
    /**
     * List of points position percent.
     */
    public List<List<Double>> pointsPosPercent = new ArrayList<>();
    /**
     * List of central position.
     */
    public List<Point2D.Double> centralPos = new ArrayList<>();
    private final List<Point2D.Double> pointsSum = new ArrayList<>();
    private final List<Integer> segmentCount = new ArrayList<>();
    private final List<List<Double>> bezierLengths = new ArrayList<>();

    private List<BezierEdge> currentShape = new ArrayList<>();
    private List<Double> currentBezierLengths = new ArrayList<>();
    private Point2D.Double currentPointsSum = new Point2D.Double();
    private int currentSegmentCount = 0;
    private int currentLineStyle = -1;
    private int currentFillStyle = -1;

    /**
     * List of fill styles.
     */
    public List<FILLSTYLE> fillStyles = new ArrayList<>();
    /**
     * List of line styles.
     */
    public List<LINESTYLE2> lineStyles = new ArrayList<>();

    private double lastX = 0;
    private double lastY = 0;

    /**
     * Constructor.
     * @param shape Shape tag
     */
    public ShapeForMorphExporter(ShapeTag shape) {
        super(ShapeTag.WIND_EVEN_ODD, shape.getShapeNum(), shape.getSwf(), shape.shapes, new ColorTransform());
    }

    @Override
    public void beginShape() {
        shapes = new ArrayList<>();
    }

    @Override
    public void endShape() {
        endCurrent();

        for (int i = 0; i < segmentCount.size(); i++) {
            Point2D.Double center = new Point2D.Double();
            center.x = pointsSum.get(i).x / (double) segmentCount.get(i);
            center.y = pointsSum.get(i).y / (double) segmentCount.get(i);
            centralPos.add(center);
        }

        for (int i = 0; i < shapes.size(); i++) {
            List<BezierEdge> shape = shapes.get(i);
            List<Point2D> points = new ArrayList<>();
            BezierEdge be = null;
            for (int j = 0; j < shape.size(); j++) {
                be = shape.get(j);
                points.add(new Point2D.Double(
                        be.getBeginPoint().getX() - centralPos.get(i).getX(),
                        be.getBeginPoint().getY() - centralPos.get(i).getY()
                ));
            }
            points.add(new Point2D.Double(
                    be.getEndPoint().getX() - centralPos.get(i).getX(),
                    be.getEndPoint().getY() - centralPos.get(i).getY()
            ));

            double w = 0;
            for (int j = 0; j < points.size(); j++) {
                int secondPoint = j + 1 == points.size() ? 0 : j + 1;
                double x = points.get(j).getX();
                double xp1 = points.get(secondPoint).getX();
                double y = points.get(j).getY();
                double yp1 = points.get(secondPoint).getY();

                if (y * yp1 < 0) {
                    double r = x + ((y * (xp1 - x)) / (y - yp1));
                    if (r > 0) {
                        if (y < 0) {
                            w = w + 1;
                        } else {
                            w = w - 1;
                        }
                    }
                } else if ((y == 0) && (x > 0)) {
                    if (yp1 > 0) {
                        w = w + .5;
                    } else {
                        w = w - .5;
                    }
                } else if ((yp1 == 0) && (xp1 > 0)) {
                    if (y < 0) {
                        w = w + .5;
                    } else {
                        w = w - .5;
                    }
                }
            }

            if (w > 0) {
                //clockwise                
            } else {
                //counter clock wise
                List<BezierEdge> newShape = new ArrayList<>();
                List<Double> newBeLength = new ArrayList<>();
                for (int j = shape.size() - 1; j >= 0; j--) {
                    newShape.add(shape.get(j).reverse());
                    newBeLength.add(bezierLengths.get(i).get(j));
                }
                shape.clear();
                shape.addAll(newShape);
                bezierLengths.get(i).clear();
                bezierLengths.get(i).addAll(newBeLength);
            }
        }

        for (int i = 0; i < shapes.size(); i++) {
            List<BezierEdge> shape = shapes.get(i);
            //closed shape
            if (shape.get(0).getBeginPoint().equals(shape.get(shape.size() - 1).getEndPoint())) {

                //Find most top left point
                double minX = Double.MAX_VALUE;
                double minY = Double.MAX_VALUE;

                for (int j = 0; j < shape.size(); j++) {
                    if (shape.get(j).getBeginPoint().getX() < minX) {
                        minX = shape.get(j).getBeginPoint().getX();
                    }
                    if (shape.get(j).getBeginPoint().getY() < minY) {
                        minY = shape.get(j).getBeginPoint().getY();
                    }
                }
                double minDist = Double.MAX_VALUE;
                int minPos = -1;
                for (int j = 0; j < shape.size(); j++) {
                    double dist = shape.get(j).getBeginPoint().distance(minX, minY);
                    if (dist < minDist) {
                        minDist = dist;
                        minPos = j;
                    }
                }
                if (minPos > -1) {
                    //Rearrange shape to start with the top left point
                    for (int j = 0; j < minPos; j++) {
                        shape.add(shape.remove(0));
                        bezierLengths.get(i).add(bezierLengths.get(i).remove(0));
                    }
                }
            }
        }

        for (List<Double> pp : bezierLengths) {
            List<Double> ppPercent = new ArrayList<>();
            double len = 0.0;
            for (double bLength : pp) {
                len += bLength;
            }
            double pos = 0;
            for (double bLength : pp) {
                double pct = roundPct(pos / len);
                pos += bLength;
                ppPercent.add(pct);
            }
            ppPercent.add(1.0);
            pointsPosPercent.add(ppPercent);
        }
    }

    @Override
    public void beginAliasedFills() {

    }

    @Override
    public void beginFills() {

    }

    @Override
    public void endFills() {
        endCurrent();
    }

    @Override
    public void beginLines() {
        currentShape = null;
    }

    @Override
    public void endLines(boolean close) {
        endCurrent();
    }

    @Override
    public void beginFill(RGB color) {
        endCurrent();
        currentShape = new ArrayList<>();
        currentFillStyle = fillStyles.size();
        FILLSTYLE fillStyle = new FILLSTYLE();
        fillStyle.fillStyleType = FILLSTYLE.SOLID;
        fillStyle.color = color;
        fillStyles.add(fillStyle);
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        endCurrent();
        currentShape = new ArrayList<>();
        currentFillStyle = fillStyles.size();
        FILLSTYLE fillStyle = new FILLSTYLE();
        fillStyle.fillStyleType = type;
        fillStyle.gradient = focalPointRatio == 0 ? new GRADIENT() : new FOCALGRADIENT();
        fillStyle.gradient.gradientRecords = Helper.deepCopy(gradientRecords);
        fillStyle.gradientMatrix = matrix.toMATRIX();
        fillStyle.gradient.spreadMode = spreadMethod;
        fillStyle.gradient.interpolationMode = interpolationMethod;
        if (focalPointRatio != 0) {
            ((FOCALGRADIENT) fillStyle.gradient).focalPoint = focalPointRatio;
        }
        fillStyles.add(fillStyle);
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        endCurrent();
        currentShape = new ArrayList<>();
        currentFillStyle = fillStyles.size();
        FILLSTYLE fillStyle = new FILLSTYLE();
        if (repeat) {
            if (smooth) {
                fillStyle.fillStyleType = FILLSTYLE.REPEATING_BITMAP;
            } else {
                fillStyle.fillStyleType = FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP;
            }
        } else {
            if (smooth) {
                fillStyle.fillStyleType = FILLSTYLE.CLIPPED_BITMAP;
            } else {
                fillStyle.fillStyleType = FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP;
            }
        }
        fillStyle.bitmapMatrix = matrix.toMATRIX();
        fillStyle.bitmapId = bitmapId;
        fillStyles.add(fillStyle);
    }

    @Override
    public void endFill() {
        endCurrent();
    }

    private void endCurrent() {
        if (currentShape != null && !currentShape.isEmpty()) {
            shapes.add(currentShape);
            bezierLengths.add(currentBezierLengths);
            pointsSum.add(currentPointsSum);
            segmentCount.add(currentSegmentCount);
            fillStyleIndices.add(currentFillStyle);
            lineStyleIndices.add(currentLineStyle);
        }
        currentShape = new ArrayList<>();
        currentBezierLengths = new ArrayList<>();
        currentPointsSum = new Point2D.Double();
        currentSegmentCount = 0;
        currentFillStyle = -1;
        currentLineStyle = -1;
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
        endCurrent();
        currentLineStyle = lineStyles.size();
        LINESTYLE2 lineStyle = new LINESTYLE2();
        lineStyle.width = (int) thickness;
        lineStyle.color = color == null ? null : new RGBA(color);
        lineStyle.pixelHintingFlag = pixelHinting;
        switch (scaleMode) {
            case "NONE":
                lineStyle.noHScaleFlag = true;
                lineStyle.noVScaleFlag = true;
                break;
            case "VERTICAL":
                lineStyle.noHScaleFlag = true;
                break;
            case "HORIZONTAL":
                lineStyle.noVScaleFlag = true;
                break;
        }
        lineStyle.startCapStyle = startCaps;
        lineStyle.endCapStyle = endCaps;
        lineStyle.joinStyle = joints;
        lineStyle.miterLimitFactor = miterLimit;
        lineStyle.noClose = noClose;
        lineStyles.add(lineStyle);
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        LINESTYLE2 lineStyle = lineStyles.get(lineStyles.size() - 1);
        lineStyle.hasFillFlag = true;
        FILLSTYLE fillStyle = new FILLSTYLE();
        fillStyle.fillStyleType = type;
        fillStyle.gradient = focalPointRatio == 0 ? new FOCALGRADIENT() : new GRADIENT();
        fillStyle.gradient.gradientRecords = Helper.deepCopy(gradientRecords);
        fillStyle.gradientMatrix = matrix.toMATRIX();
        fillStyle.gradient.spreadMode = spreadMethod;
        fillStyle.gradient.interpolationMode = interpolationMethod;
        if (focalPointRatio != 0) {
            ((FOCALGRADIENT) fillStyle.gradient).focalPoint = focalPointRatio;
        }
        lineStyle.fillType = fillStyle;
    }

    @Override
    public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        LINESTYLE2 lineStyle = lineStyles.get(lineStyles.size() - 1);
        lineStyle.hasFillFlag = true;
        FILLSTYLE fillStyle = new FILLSTYLE();
        if (repeat) {
            if (smooth) {
                fillStyle.fillStyleType = FILLSTYLE.REPEATING_BITMAP;
            } else {
                fillStyle.fillStyleType = FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP;
            }
        } else {
            if (smooth) {
                fillStyle.fillStyleType = FILLSTYLE.CLIPPED_BITMAP;
            } else {
                fillStyle.fillStyleType = FILLSTYLE.NON_SMOOTHED_CLIPPED_BITMAP;
            }
        }
        fillStyle.bitmapMatrix = matrix.toMATRIX();
        fillStyle.bitmapId = bitmapId;
        lineStyle.fillType = fillStyle;
    }

    @Override
    public void moveTo(double x, double y) {
        int backupFillStyle = currentFillStyle;
        int backupLineStyle = currentLineStyle;
        endCurrent();
        currentFillStyle = backupFillStyle;
        currentLineStyle = backupLineStyle;
        lastX = (int) x;
        lastY = (int) y;
        //currentPointsPos.add(0.0);
    }

    @Override
    public void lineTo(double x, double y) {
        if (x == lastX && y == lastY) {
            return;
        }
        BezierEdge be = new BezierEdge(lastX, lastY, x, y);

        currentPointsSum.x += x; // - lastX;
        currentPointsSum.y += y; // - lastY;

        currentShape.add(be);
        lastX = x;
        lastY = y;
        currentBezierLengths.add(be.length());

        currentSegmentCount++;
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {

        if (anchorX == lastX && anchorY == lastY) {
            return;
        }

        BezierEdge be = new BezierEdge(lastX, lastY, controlX, controlY, anchorX, anchorY);

        currentShape.add(be);

        currentPointsSum.x += anchorX; // - lastX;
        currentPointsSum.y += anchorY; // - lastY;

        lastX = anchorX;
        lastY = anchorY;
        currentBezierLengths.add(be.length());

        currentSegmentCount++;
    }

    private double roundPct(double pct) {
        double precision = 1000000d;
        return Math.round(pct * precision) / precision;
    }
}

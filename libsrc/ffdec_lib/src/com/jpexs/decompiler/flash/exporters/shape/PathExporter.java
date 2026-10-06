/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.shape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.awt.BasicStroke;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 * Path exporter.
 *
 * @author JPEXS
 */
public class PathExporter extends ShapeExporterBase {

    private final List<GeneralPath> paths = new ArrayList<>();

    private final List<GeneralPath> strokes = new ArrayList<>();

    private double thickness = 0;

    private boolean aliasedFill = false;

    private GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

    /**
     * Exports shape to GeneralPath.
     * @param windingRule GeneralPath winding rule
     * @param shapeNum Shape number
     * @param swf SWF
     * @param shape Shape
     * @return List of GeneralPath
     */
    public static List<GeneralPath> export(int windingRule, int shapeNum, SWF swf, SHAPE shape) {
        return export(windingRule, shapeNum, swf, shape, new ArrayList<>());
    }

    /**
     * Exports shape to GeneralPath.
     * @param windingRule GeneralPath winding rule
     * @param shapeNum Shape number (1 for DefineShape, 2 for DefineShape2, etc.)
     * @param swf SWF
     * @param shape Shape
     * @param strokes List of strokes
     * @return List of GeneralPath
     */
    public static List<GeneralPath> export(int windingRule, int shapeNum, SWF swf, SHAPE shape, List<GeneralPath> strokes) {
        PathExporter exporter = new PathExporter(windingRule, shapeNum, swf, shape, null);
        exporter.export();
        strokes.addAll(exporter.strokes);
        return exporter.paths;
    }
    
    @Override
    public void export() {
        super.export();
    }

    /**
     * Exports the filled area which uses the specified one-based fill style
     * index. The index includes fill styles introduced by style change
     * records.
     *
     * @param windingRule GeneralPath winding rule
     * @param shapeNum Shape number (1 for DefineShape, 2 for DefineShape2, etc.)
     * @param swf SWF
     * @param shape Shape
     * @param fillStyleIndex One-based global fill style index
     * @return Filled area for the requested style
     */
    public static GeneralPath exportFillStyle(int windingRule, int shapeNum, SWF swf, SHAPE shape, int fillStyleIndex) {
        PathExporter exporter = new PathExporter(windingRule, shapeNum, swf, shape, null);
        GeneralPath result = new GeneralPath(windingRule == 0 ? GeneralPath.WIND_EVEN_ODD : GeneralPath.WIND_NON_ZERO);
        appendStylePath(result, exporter.getFillPaths(), fillStyleIndex, true);
        return result;
    }

    /**
     * Exports the center line which uses the specified one-based line style
     * index. The index includes line styles introduced by style change
     * records.
     *
     * @param windingRule GeneralPath winding rule
     * @param shapeNum Shape number (1 for DefineShape, 2 for DefineShape2, etc.)
     * @param swf SWF
     * @param shape Shape
     * @param lineStyleIndex One-based global line style index
     * @return Center line for the requested style
     */
    public static GeneralPath exportLineStyle(int windingRule, int shapeNum, SWF swf, SHAPE shape, int lineStyleIndex) {
        PathExporter exporter = new PathExporter(windingRule, shapeNum, swf, shape, null);
        GeneralPath result = new GeneralPath();
        appendStylePath(result, exporter.getLinePaths(), lineStyleIndex, false);
        return result;
    }

    private static void appendStylePath(GeneralPath result, List<List<IEdge>> paths, int styleIndex, boolean fillStyle) {
        int posX = Integer.MAX_VALUE;
        int posY = Integer.MAX_VALUE;
        for (List<IEdge> path : paths) {
            for (IEdge edge : path) {
                int edgeStyleIndex = fillStyle ? edge.getFillStyleIdx() : edge.getLineStyleIdx();
                if (edgeStyleIndex != styleIndex) {
                    continue;
                }
                if (posX != edge.getFromX() || posY != edge.getFromY()) {
                    result.moveTo(edge.getFromX(), edge.getFromY());
                }
                if (edge instanceof CurvedEdge) {
                    CurvedEdge curvedEdge = (CurvedEdge) edge;
                    result.quadTo(curvedEdge.getControlX(), curvedEdge.getControlY(), curvedEdge.getToX(), curvedEdge.getToY());
                } else {
                    result.lineTo(edge.getToX(), edge.getToY());
                }
                posX = edge.getToX();
                posY = edge.getToY();
            }
            posX = Integer.MAX_VALUE;
            posY = Integer.MAX_VALUE;
        }
    }

    /**
     * Constructor.
     * @param windingRule GeneralPath winding rule
     * @param shapeNum Shape number
     * @param swf SWF
     * @param shape Shape
     * @param colorTransform Color transform
     */
    protected PathExporter(int windingRule, int shapeNum, SWF swf, SHAPE shape, ColorTransform colorTransform) {
        super(windingRule, shapeNum, swf, shape, colorTransform);
    }   

    @Override
    public void beginShape() {

    }

    @Override
    public void endShape() {

    }

    @Override
    public void beginFills() {
        aliasedFill = false;
    }

    @Override
    public void endFills() {

    }

    @Override
    public void beginLines() {

    }

    @Override
    public void endLines(boolean close) {
        finalizePath();
    }

    @Override
    public void beginFill(RGB color) {
        finalizePath();
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        finalizePath();
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
    }

    @Override
    public void endFill() {
        finalizePath();
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
        finalizePath();
        this.thickness = thickness;
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {

    }

    @Override
    public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {

    }

    @Override
    public void moveTo(double x, double y) {
        path.moveTo(x, y);
    }

    @Override
    public void lineTo(double x, double y) {
        path.lineTo(x, y);
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        path.quadTo(controlX, controlY, anchorX, anchorY);
    }

    /**
     * Finalizes path.
     */
    protected void finalizePath() {
        if (thickness == 0) {
            strokes.add(new GeneralPath());
        } else {
            strokes.add(new GeneralPath(new BasicStroke((float) (thickness)).createStrokedShape(path)));
        }
        paths.add(path);
        path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);  //For correct intersections display
    }
}

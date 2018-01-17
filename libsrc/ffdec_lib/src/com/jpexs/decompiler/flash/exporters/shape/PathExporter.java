/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
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
 *
 * @author JPEXS
 */
public class PathExporter extends ShapeExporterBase {

    private final List<GeneralPath> paths = new ArrayList<>();

    private final List<GeneralPath> strokes = new ArrayList<>();

    private double thickness = 0;

    private GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

    public static List<GeneralPath> export(SWF swf, SHAPE shape) {
        return export(swf, shape, new ArrayList<>());
    }

    public static List<GeneralPath> export(SWF swf, SHAPE shape, List<GeneralPath> strokes) {
        PathExporter exporter = new PathExporter(swf, shape, null);
        exporter.export();
        strokes.addAll(exporter.strokes);
        return exporter.paths;
    }

    protected PathExporter(SWF swf, SHAPE shape, ColorTransform colorTransform) {
        super(swf, shape, colorTransform);
    }

    @Override
    public void export() {
        super.export();
    }

    @Override
    public void beginShape() {

    }

    @Override
    public void endShape() {

    }

    @Override
    public void beginFills() {

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
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit) {
        finalizePath();
        this.thickness = thickness;
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {

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

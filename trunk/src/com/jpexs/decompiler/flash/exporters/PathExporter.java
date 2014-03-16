/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class PathExporter extends ShapeExporterBase {

    private final List<GeneralPath> paths = new ArrayList<>();
    private GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

    private final double unitDivisor = 1;

    public static List<GeneralPath> export(SHAPE shape) {
        PathExporter exporter = new PathExporter(shape, new ColorTransform());
        exporter.export();
        return exporter.paths;
    }

    private PathExporter(SHAPE shape, ColorTransform colorTransform) {
        super(shape, colorTransform);
    }

    @Override
    public void export() {
        super.export();
    }

    @Override
    public void beginShape() {

    }

    @Override
    public void endShape(double xMin, double yMin, double xMax, double yMax) {

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
    public void endLines() {
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
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, int miterLimit) {
        finalizePath();
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {

    }

    @Override
    public void moveTo(double x, double y) {
        path.moveTo(x / unitDivisor, y / unitDivisor);
    }

    @Override
    public void lineTo(double x, double y) {
        path.lineTo(x / unitDivisor, y / unitDivisor);
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        path.quadTo(controlX / unitDivisor, controlY / unitDivisor,
                anchorX / unitDivisor, anchorY / unitDivisor);
    }

    protected void finalizePath() {
        paths.add(path);
        path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);  //For correct intersections display

    }
}

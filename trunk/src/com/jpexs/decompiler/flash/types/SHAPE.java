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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.exporters.Matrix;
import com.jpexs.decompiler.flash.exporters.ShapeExporterBase;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class SHAPE implements NeedsCharacters, Serializable {

    @SWFType(value = BasicType.UB, count = 4)
    public int numFillBits;
    @SWFType(value = BasicType.UB, count = 4)
    public int numLineBits;
    public List<SHAPERECORD> shapeRecords;

    @Override
    public Set<Integer> getNeededCharacters() {
        Set<Integer> ret = new HashSet<>();
        for (SHAPERECORD r : shapeRecords) {
            ret.addAll(r.getNeededCharacters());
        }
        return ret;
    }

    public RECT getBounds() {
        return SHAPERECORD.getBounds(shapeRecords);
    }

    public Shape getOutline() {
        final List<GeneralPath> paths = new ArrayList<>();
        ShapeExporterBase se = new ShapeExporterBase(this, new ColorTransform()) {
            private GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

            private double unitDivisor;

            @Override
            public void export() {
                unitDivisor = 1;
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
        };
        se.export();
        Area area = new Area();
        for (GeneralPath path : paths) {
            area.add(new Area(path));
        }

        return area;
    }
}

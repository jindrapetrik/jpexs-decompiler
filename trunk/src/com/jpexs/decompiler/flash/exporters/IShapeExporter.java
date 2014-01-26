/*
 *  Copyright (C) 2010-2013 JPEXS
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

import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGB;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface IShapeExporter {

    public void beginShape();

    public void endShape(double xMin, double yMin, double xMax, double yMax);

    public void beginFills();

    public void endFills();

    public void beginLines();

    public void endLines();

    public void beginFill(RGB color);

    public void beginGradientFill(int type, List<RGB> colors, List<Integer> ratios, MATRIX matrix, int spreadMethod, int interpolationMethod, float focalPointRatio);

    public void beginBitmapFill(int bitmapId, MATRIX matrix, boolean repeat, boolean smooth);

    public void endFill();

    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, int miterLimit);

    public void lineGradientStyle(int type, List<RGB> colors, List<Integer> ratios, MATRIX matrix, int spreadMethod, int interpolationMethod, float focalPointRatio);

    public void moveTo(double x, double y);

    public void lineTo(double x, double y);

    public void curveTo(double controlX, double controlY, double anchorX, double anchorY);
}

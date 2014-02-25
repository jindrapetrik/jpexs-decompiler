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

import com.jpexs.decompiler.flash.types.RECT;

/**
 *
 * @author JPEXS
 */
public class ExportRectangle {

    public double xMin;
    public double yMin;
    public double xMax;
    public double yMax;

    public ExportRectangle(double xMin, double yMin, double xMax, double yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    public ExportRectangle(RECT rect) {
        this.xMin = rect.Xmin;
        this.yMin = rect.Ymin;
        this.xMax = rect.Xmax;
        this.yMax = rect.Ymax;
    }

    public double getWidth() {
        return xMax - xMin;
    }

    public double getHeight() {
        return yMax - yMin;
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(xMin);
        bits += Double.doubleToLongBits(yMin) * 37;
        bits += Double.doubleToLongBits(xMax) * 43;
        bits += Double.doubleToLongBits(yMax) * 47;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExportRectangle) {
            ExportRectangle r = (ExportRectangle) obj;
            return (xMin == r.xMin) && (yMin == r.yMin) && (xMax == r.xMax) && (yMax == r.yMax);
        }
        return super.equals(obj);
    }
}

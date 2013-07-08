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
package com.jpexs.decompiler.flash.types;

import java.awt.Point;
import java.io.Serializable;

/**
 * Represents a standard 2x3 transformation matrix of the sort commonly used in
 * 2D graphics
 *
 * @author JPEXS
 */
public class MATRIX implements Serializable {

    /**
     * Has scale values
     */
    public boolean hasScale;
    /**
     * X scale value
     */
    public int scaleX;
    /**
     * Y scale value
     */
    public int scaleY;
    /**
     * Has rotate and skew values
     */
    public boolean hasRotate;
    /**
     * First rotate and skew value
     */
    public int rotateSkew0;
    /**
     * Second rotate and skew value
     */
    public int rotateSkew1;
    /**
     * X translate value in twips
     */
    public int translateX;
    /**
     * Y translate value in twips
     */
    public int translateY;
    public int bitsTranslate;
    public int bitsRotate;
    public int bitsScale;

    public MATRIX() {
    }

    public MATRIX(MATRIX m) {
        if (m == null) {
            return;
        }
        hasScale = m.hasScale;
        hasRotate = m.hasRotate;
        scaleX = m.scaleX;
        scaleY = m.scaleY;
        rotateSkew0 = m.rotateSkew0;
        rotateSkew1 = m.rotateSkew1;
        translateX = m.translateX;
        translateY = m.translateY;
    }

    @Override
    public String toString() {
        return "[MATRIX scale:" + getScaleXFloat() + "," + getScaleYFloat() + ", rotate:" + getRotateSkew0Float() + "," + getRotateSkew1Float() + ", translate:" + translateX + "," + translateY + "]";
    }

    public float toFloat(int i) {
        return ((float) i) / (1 << 16);
    }

    public Point apply(Point p) {
        Point ret = new Point();
        ret.x = (int) (p.x * (hasScale ? toFloat(scaleX) : 1) + p.y * (hasRotate ? toFloat(rotateSkew1) : 0) + translateX);
        ret.y = (int) (p.x * (hasRotate ? toFloat(rotateSkew0) : 0) + p.y * (hasScale ? toFloat(scaleY) : 1) + translateY);
        return ret;
    }

    public RECT apply(RECT r) {
        Point topLeft = apply(r.getTopLeft());
        Point bottomRight = apply(r.getBottomRight());
        int Xmin = Math.min(topLeft.x, bottomRight.x);
        int Ymin = Math.min(topLeft.y, bottomRight.y);
        int Xmax = Math.max(topLeft.x, bottomRight.x);
        int Ymax = Math.max(topLeft.y, bottomRight.y);
        return new RECT(Xmin, Xmax, Ymin, Ymax);

    }

    public int getRotateSkew0() {
        return hasRotate ? rotateSkew0 : 0;
    }

    public int getRotateSkew1() {
        return hasRotate ? rotateSkew1 : 0;
    }

    public float getRotateSkew0Float() {
        return (hasRotate ? toFloat(rotateSkew0) : 0);
    }

    public float getRotateSkew1Float() {
        return (hasRotate ? toFloat(rotateSkew1) : 0);
    }

    public float getScaleXFloat() {
        return (hasScale ? toFloat(scaleX) : 1);
    }

    public float getScaleYFloat() {
        return (hasScale ? toFloat(scaleY) : 1);
    }

    public int getScaleX() {
        return (hasScale ? (scaleX) : (1 << 16));
    }

    public int getScaleY() {
        return (hasScale ? (scaleY) : (1 << 16));
    }

    public MATRIX merge(MATRIX m) {
        MATRIX ret = new MATRIX();
        ret.translateX = m.translateX + this.translateX;
        ret.translateY = m.translateY + this.translateY;

        ret.scaleX = (m.hasScale ? m.scaleX : 1) * (this.hasScale ? this.scaleX : 1);
        ret.scaleY = (m.hasScale ? m.scaleY : 1) * (this.hasScale ? this.scaleY : 1);
        ret.rotateSkew0 = m.rotateSkew0 + this.rotateSkew0;
        ret.rotateSkew1 = m.rotateSkew1 + this.rotateSkew1;
        return ret;
    }

    public boolean isEmpty() {
        return (translateX == 0) && (translateY == 0) && (!hasRotate) && (!hasScale);
    }
}

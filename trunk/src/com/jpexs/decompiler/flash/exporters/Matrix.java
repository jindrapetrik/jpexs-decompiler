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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.geom.AffineTransform;

/**
 *
 * @author JPEXS
 */
public class Matrix {

    public double scaleX = 1;
    public double scaleY = 1;
    public double rotateSkew0;
    public double rotateSkew1;
    public double translateX;
    public double translateY;

    public static Matrix getScaleInstance(double scale) {
        Matrix mat = new Matrix();
        mat.scale(scale);
        return mat;
    }
    
    public Matrix() {
        scaleX = 1;
        scaleY = 1;
    }

    public Matrix(MATRIX matrix) {
        translateX = matrix.translateX;
        translateY = matrix.translateY;
        if (matrix.hasScale) {
            scaleX = matrix.getScaleXFloat();
            scaleY = matrix.getScaleYFloat();
        } else {
            scaleX = 1;
            scaleY = 1;
        }
        if (matrix.hasRotate) {
            rotateSkew0 = matrix.getRotateSkew0Float();
            rotateSkew1 = matrix.getRotateSkew1Float();
        }
    }

    @Override
    public Matrix clone() {
        Matrix mat = new Matrix();
        mat.translateX = translateX;
        mat.translateY = translateY;
        mat.scaleX = scaleX;
        mat.scaleY = scaleY;
        mat.rotateSkew0 = rotateSkew0;
        mat.rotateSkew1 = rotateSkew1;
        return mat;
    }

    public void translate(double x, double y) {
        translateX += x;
        translateY += y;
    }
    
    public void scale(double factor) {
        scaleX *= factor;
        scaleY *= factor;
        rotateSkew0 *= factor;
        rotateSkew1 *= factor;
    }
    
    public AffineTransform toTransform() {
        AffineTransform transform = new AffineTransform(scaleX, rotateSkew0,
                rotateSkew1, scaleY,
                translateX, translateY);
        return transform;
    }
}

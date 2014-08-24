/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.commonshape;

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

    public static Matrix getTranslateInstance(double x, double y) {
        Matrix mat = new Matrix();
        mat.translate(x, y);
        return mat;
    }

    public Matrix() {
        scaleX = 1;
        scaleY = 1;
    }

    public Matrix(MATRIX matrix) {
        if (matrix == null) {
            matrix = new MATRIX();
        }
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

    public Matrix(AffineTransform transform) {
        this();
        if (transform != null) {
            scaleX = transform.getScaleX();
            rotateSkew1 = transform.getShearX();
            translateX = transform.getTranslateX();
            rotateSkew0 = transform.getShearY();
            scaleY = transform.getScaleY();
            translateY = transform.getTranslateY();
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

    public Point transform(double x, double y) {
        Point result = new Point(
                scaleX * x + rotateSkew1 * y + translateX,
                rotateSkew0 * x + scaleY * y + translateY);
        return result;
    }

    public Point transform(Point point) {
        return transform(point.x, point.y);
    }

    public java.awt.Point transform(java.awt.Point point) {
        Point p = transform(point.x, point.y);
        return new java.awt.Point((int) p.x, (int) p.y);
    }

    public ExportRectangle transform(ExportRectangle rect) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        Point point;
        point = transform(rect.xMin, rect.yMin);
        if (point.x < minX) {
            minX = point.x;
        }
        if (point.x > maxX) {
            maxX = point.x;
        }
        if (point.y < minY) {
            minY = point.y;
        }
        if (point.y > maxY) {
            maxY = point.y;
        }
        point = transform(rect.xMax, rect.yMin);
        if (point.x < minX) {
            minX = point.x;
        }
        if (point.x > maxX) {
            maxX = point.x;
        }
        if (point.y < minY) {
            minY = point.y;
        }
        if (point.y > maxY) {
            maxY = point.y;
        }
        point = transform(rect.xMin, rect.yMax);
        if (point.x < minX) {
            minX = point.x;
        }
        if (point.x > maxX) {
            maxX = point.x;
        }
        if (point.y < minY) {
            minY = point.y;
        }
        if (point.y > maxY) {
            maxY = point.y;
        }
        point = transform(rect.xMax, rect.yMax);
        if (point.x < minX) {
            minX = point.x;
        }
        if (point.x > maxX) {
            maxX = point.x;
        }
        if (point.y < minY) {
            minY = point.y;
        }
        if (point.y > maxY) {
            maxY = point.y;
        }
        return new ExportRectangle(minX, minY, maxX, maxY);
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

    public Matrix concatenate(Matrix m) {
        Matrix result = new Matrix();
        result.scaleX = scaleX * m.scaleX + rotateSkew1 * m.rotateSkew0;
        result.rotateSkew0 = rotateSkew0 * m.scaleX + scaleY * m.rotateSkew0;
        result.rotateSkew1 = scaleX * m.rotateSkew1 + rotateSkew1 * m.scaleY;
        result.scaleY = rotateSkew0 * m.rotateSkew1 + scaleY * m.scaleY;
        result.translateX = scaleX * m.translateX + rotateSkew1 * m.translateY + translateX;
        result.translateY = rotateSkew0 * m.translateX + scaleY * m.translateY + translateY;
        return result;
    }

    public Matrix preConcatenate(Matrix m) {
        Matrix result = new Matrix();
        result.scaleX = m.scaleX * scaleX + m.rotateSkew1 * rotateSkew0;
        result.rotateSkew0 = m.rotateSkew0 * scaleX + m.scaleY * rotateSkew0;
        result.rotateSkew1 = m.scaleX * rotateSkew1 + m.rotateSkew1 * scaleY;
        result.scaleY = m.rotateSkew0 * rotateSkew1 + m.scaleY * scaleY;
        result.translateX = m.scaleX * translateX + m.rotateSkew1 * translateY + m.translateX;
        result.translateY = m.rotateSkew0 * translateX + m.scaleY * translateY + m.translateY;
        return result;
    }

    public AffineTransform toTransform() {
        AffineTransform transform = new AffineTransform(scaleX, rotateSkew0,
                rotateSkew1, scaleY,
                translateX, translateY);
        return transform;
    }

    public String getTransformationString(double translateDivisor, double unitDivisor) {
        double translateX = roundPixels400(this.translateX / translateDivisor);
        double translateY = roundPixels400(this.translateY / translateDivisor);
        double rotateSkew0 = roundPixels400(this.rotateSkew0 / unitDivisor);
        double rotateSkew1 = roundPixels400(this.rotateSkew1 / unitDivisor);
        double scaleX = roundPixels400(this.scaleX / unitDivisor);
        double scaleY = roundPixels400(this.scaleY / unitDivisor);
        return "matrix(" + scaleX + ", " + rotateSkew0 + ", "
                + rotateSkew1 + ", " + scaleY + ", " + translateX + ", " + translateY + ")";
    }

    private double roundPixels400(double pixels) {
        return Math.round(pixels * 10000) / 10000.0;
    }

    @Override
    public String toString() {
        return "[Matrix scale:" + scaleX + "," + scaleY + ", rotate:" + rotateSkew0 + "," + rotateSkew1 + ", translate:" + translateX + "," + translateY + "]";
    }
}

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
import com.jpexs.decompiler.flash.exporters.commonshape.Point;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;

/**
 * AS3 graphics shape exporter.
 *
 * @author JPEXS, Josh Tynjala
 */
public class AS3ShapeExporter extends ShapeExporterBase {
    /**
     * Shape data
     */
    protected StringBuilder shapeData = new StringBuilder();

    /**
     * Delta X
     */
    protected double deltaX = 0;

    /**
     * Delta Y
     */
    protected double deltaY = 0;

    /**
     * Fill matrix
     */
    protected Matrix fillMatrix = null;

    /**
     * Last gradient color
     */
    protected String lastGradColor = null;

    /**
     * SWF
     */
    protected SWF swf;

    /**
     * Repeat count
     */
    protected int repeatCnt = 0;

    /**
     * Unit divisor
     */
    protected double unitDivisor;

    /**
     * Basic fill
     */
    protected RGB basicFill;

    /**
     * Line fill data
     */
    protected StringBuilder lineFillData = null;

    /**
     * Line last gradient color
     */
    protected String lineLastGradColor = null;

    /**
     * Line fill matrix
     */
    protected Matrix lineFillMatrix = null;

    /**
     * Line repeat count
     */
    protected int lineRepeatCnt = 0;

    /**
     * Fill width
     */
    protected int fillWidth = 0;

    /**
     * Fill height
     */
    protected int fillHeight = 0;

    /**
     * Gets shape data.
     * @return Shape data
     */
    public String getShapeData() {
        return shapeData.toString();
    }

    /**
     * Constructor.
     * @param windingRule Winding rule
     * @param shapeNum Shape number
     * @param basicFill Basic fill
     * @param unitDivisor Unit divisor
     * @param swf SWF
     * @param shape Shape
     * @param colorTransform Color transform
     * @param deltaX Delta X
     * @param deltaY Delta Y
     */
    public AS3ShapeExporter(int windingRule, int shapeNum, RGB basicFill, double unitDivisor, SWF swf, SHAPE shape, ColorTransform colorTransform, int deltaX, int deltaY) {
        super(windingRule, shapeNum, swf, shape, colorTransform);
        this.swf = swf;
        this.unitDivisor = unitDivisor;
        this.basicFill = basicFill;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    @Override
    public void beginShape() {
        shapeData = new StringBuilder();
        shapeData.append("var shape:Shape = new Shape();\r\n");
    }

    @Override
    public void endShape() {
        shapeData.append("addChild(shape);\r\n");
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
    }

    protected String colorToHexString(RGB color) {
        if (color == null) {
            return "0x000000";
        }
        int rgbInt = ((color.red & 0xFF) << 16)
                | ((color.green & 0xFF) << 8)
                | (color.blue & 0xFF);
        return "0x" + Helper.padZeros(Integer.toHexString(rgbInt), 6);
    }

    protected float colorToAlpha(RGB color) {
        if (color == null) {
            return 0.0f;
        }
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            double alpha = colorA.alpha / 255.0;
            return colorA.getAlphaFloat();
        }
        return 1.0f;
    }

    @Override
    public void beginFill(RGB color) {
        shapeData.append("shape.graphics.beginFill(");
        shapeData.append(colorToHexString(color)).append(", ");
        shapeData.append(colorToAlpha(color));
        shapeData.append(");\r\n");
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        shapeData.append("shape.graphics.beginGradientFill(");

        switch (type) {
            case FILLSTYLE.LINEAR_GRADIENT:
                shapeData.append("GradientType.LINEAR");
                break;
            case FILLSTYLE.RADIAL_GRADIENT:
                shapeData.append("GradientType.RADIAL");
                break;
        }
        shapeData.append(", ");
        
        // colors
        shapeData.append("[");
        for (int i = 0; i < gradientRecords.length; i++) {
            if (i > 0) shapeData.append(", ");
            shapeData.append(colorToHexString(gradientRecords[i].color));
        }
        shapeData.append("], ");

        // alpha
        shapeData.append("[");
        for (int i = 0; i < gradientRecords.length; i++) {
            if (i > 0) shapeData.append(", ");
            shapeData.append(colorToAlpha(gradientRecords[i].color));
        }
        shapeData.append("], ");

        // ratios
        shapeData.append("[");
        for (int i = 0; i < gradientRecords.length; i++) {
            if (i > 0) shapeData.append(", ");
            shapeData.append(gradientRecords[i].ratio);
        }
        shapeData.append("], ");

        shapeData.append("new Matrix(");
        shapeData.append(matrix.scaleX / unitDivisor).append(", ")
                 .append(matrix.rotateSkew0 / unitDivisor).append(", ")
                 .append(matrix.rotateSkew1 / unitDivisor).append(", ")
                 .append(matrix.scaleY / unitDivisor).append(", ")
                 .append(matrix.translateX / unitDivisor).append(", ")
                 .append(matrix.translateY / unitDivisor);
        shapeData.append("), ");

        switch (spreadMethod) {
            case GRADIENT.SPREAD_PAD_MODE:
                shapeData.append("SpreadMethod.PAD");
                break;
            case GRADIENT.SPREAD_REFLECT_MODE:
                shapeData.append("SpreadMethod.REFLECT");
                break;
            case GRADIENT.SPREAD_REPEAT_MODE:
                shapeData.append("SpreadMethod.REPEAT");
                break;
        }
        shapeData.append(", ");

        switch (interpolationMethod) {
            case GRADIENT.INTERPOLATION_RGB_MODE:
                shapeData.append("InterpolationMethod.RGB");
                break;
            case GRADIENT.INTERPOLATION_LINEAR_RGB_MODE:
                shapeData.append("InterpolationMethod.LINEAR_RGB");
                break;
        }
        shapeData.append(", ");

        shapeData.append(focalPointRatio);

        shapeData.append(");\r\n");
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        shapeData.append("shape.graphics.beginBitmapFill(");
        shapeData.append("bitmapData").append(bitmapId);
        shapeData.append(", ");

        shapeData.append("new Matrix(");
        shapeData.append(matrix.scaleX / unitDivisor).append(", ")
                 .append(matrix.rotateSkew0 / unitDivisor).append(", ")
                 .append(matrix.rotateSkew1 / unitDivisor).append(", ")
                 .append(matrix.scaleY / unitDivisor).append(", ")
                 .append(matrix.translateX / unitDivisor).append(", ")
                 .append(matrix.translateY / unitDivisor);
        shapeData.append("), ");

        shapeData.append(repeat);
        shapeData.append(", ");

        shapeData.append(smooth);

        shapeData.append(");\r\n");
    }

    @Override
    public void endFill() {
        shapeData.append("shape.graphics.endFill();\r\n");
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
        thickness /= SWF.unitDivisor;

        shapeData.append("shape.graphics.lineStyle(");
        shapeData.append(thickness).append(", ");        
        shapeData.append(colorToHexString(color)).append(", ");
        shapeData.append(colorToAlpha(color)).append(", ");
        shapeData.append(pixelHinting).append(", ");
        switch (scaleMode)
        {
            case "VERTICAL":
                shapeData.append("LineScaleMode.VERTICAL").append(", ");
                break;
            case "HORIZONTAL":
                shapeData.append("LineScaleMode.HORIZONTAL").append(", ");
                break;
            case "NONE":
                shapeData.append("LineScaleMode.NONE").append(", ");
                break;
            default:
                shapeData.append("LineScaleMode.NORMAL").append(", ");
                break;
        }
        switch (startCaps) {
            case LINESTYLE2.NO_CAP:
                shapeData.append("CapStyle.NONE").append(", ");
                break;
            case LINESTYLE2.SQUARE_CAP:
                shapeData.append("CapStyle.SQUARE").append(", ");
                break;
            default:
                shapeData.append("CapStyle.ROUND").append(", ");
                break;
        }
        switch (joints) {
            case LINESTYLE2.BEVEL_JOIN:
                shapeData.append("JointStyle.BEVEL").append(", ");
                break;
            case LINESTYLE2.ROUND_JOIN:
                shapeData.append("JointStyle.ROUND").append(", ");
                break;
            default:
                shapeData.append("JointStyle.MITER").append(", ");
                break;
        }
        shapeData.append(miterLimit);
        shapeData.append(");\r\n");
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        shapeData.append("shape.graphics.lineGradientStyle(");

        switch (type) {
            case FILLSTYLE.LINEAR_GRADIENT:
                shapeData.append("GradientType.LINEAR");
                break;
            case FILLSTYLE.RADIAL_GRADIENT:
                shapeData.append("GradientType.RADIAL");
                break;
        }
        shapeData.append(", ");
        
        // colors
        shapeData.append("[");
        for (int i = 0; i < gradientRecords.length; i++) {
            if (i > 0) shapeData.append(", ");
            shapeData.append(colorToHexString(gradientRecords[i].color));
        }
        shapeData.append("], ");

        // alpha
        shapeData.append("[");
        for (int i = 0; i < gradientRecords.length; i++) {
            if (i > 0) shapeData.append(", ");
            shapeData.append(colorToAlpha(gradientRecords[i].color));
        }
        shapeData.append("], ");

        // ratios
        shapeData.append("[");
        for (int i = 0; i < gradientRecords.length; i++) {
            if (i > 0) shapeData.append(", ");
            shapeData.append(gradientRecords[i].ratio);
        }
        shapeData.append("], ");

        shapeData.append("new Matrix(");
        shapeData.append(matrix.scaleX / unitDivisor).append(", ")
                 .append(matrix.rotateSkew0 / unitDivisor).append(", ")
                 .append(matrix.rotateSkew1 / unitDivisor).append(", ")
                 .append(matrix.scaleY / unitDivisor).append(", ")
                 .append(matrix.translateX / unitDivisor).append(", ")
                 .append(matrix.translateY / unitDivisor);
        shapeData.append("), ");

        switch (spreadMethod) {
            case GRADIENT.SPREAD_PAD_MODE:
                shapeData.append("SpreadMethod.PAD");
                break;
            case GRADIENT.SPREAD_REFLECT_MODE:
                shapeData.append("SpreadMethod.REFLECT");
                break;
            case GRADIENT.SPREAD_REPEAT_MODE:
                shapeData.append("SpreadMethod.REPEAT");
                break;
        }
        shapeData.append(", ");

        switch (interpolationMethod) {
            case GRADIENT.INTERPOLATION_RGB_MODE:
                shapeData.append("InterpolationMethod.RGB");
                break;
            case GRADIENT.INTERPOLATION_LINEAR_RGB_MODE:
                shapeData.append("InterpolationMethod.LINEAR_RGB");
                break;
        }
        shapeData.append(", ");

        shapeData.append(focalPointRatio);

        shapeData.append(");\r\n");
    }

    @Override
    public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        shapeData.append("shape.graphics.lineBitmapStyle(");
        shapeData.append("bitmapData").append(bitmapId);
        shapeData.append(", ");

        shapeData.append("new Matrix(");
        shapeData.append(matrix.scaleX / unitDivisor).append(", ")
                 .append(matrix.rotateSkew0 / unitDivisor).append(", ")
                 .append(matrix.rotateSkew1 / unitDivisor).append(", ")
                 .append(matrix.scaleY / unitDivisor).append(", ")
                 .append(matrix.translateX / unitDivisor).append(", ")
                 .append(matrix.translateY / unitDivisor);
        shapeData.append("), ");

        shapeData.append(repeat);
        shapeData.append(", ");

        shapeData.append(smooth);

        shapeData.append(");\r\n");
    }

    @Override
    public void moveTo(double x, double y) {
        x += deltaX;
        y += deltaY;
        shapeData.append("shape.graphics.moveTo(");
        shapeData.append(Helper.doubleStr(x / unitDivisor)).append(", ")
                 .append(Helper.doubleStr(y / unitDivisor));
        shapeData.append(");\r\n");
    }

    @Override
    public void lineTo(double x, double y) {
        x += deltaX;
        y += deltaY;
        shapeData.append("shape.graphics.lineTo(");
        shapeData.append(Helper.doubleStr(x / unitDivisor)).append(", ")
                 .append(Helper.doubleStr(y / unitDivisor));
        shapeData.append(");\r\n");
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        controlX += deltaX;
        anchorX += deltaX;
        controlY += deltaY;
        anchorY += deltaY;
        shapeData.append("shape.graphics.curveTo(");
        shapeData.append(Helper.doubleStr(controlX / unitDivisor)).append(", ")
                 .append(Helper.doubleStr(controlY / unitDivisor)).append(", ")
                 .append(Helper.doubleStr(anchorX / unitDivisor)).append(", ")
                 .append(Helper.doubleStr(anchorY / unitDivisor));
        shapeData.append(");\r\n");
    }
}

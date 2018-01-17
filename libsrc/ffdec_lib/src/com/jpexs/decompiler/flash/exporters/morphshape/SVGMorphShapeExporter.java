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
package com.jpexs.decompiler.flash.exporters.morphshape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import org.w3c.dom.Element;

/**
 *
 * @author JPEXS, Claus Wahlers
 */
public class SVGMorphShapeExporter extends DefaultSVGMorphShapeExporter {

    protected Element path;

    protected int id;

    protected int lastPatternId;

    private final Color defaultColor;

    private final SWF swf;

    private final SVGExporter exporter;

    public SVGMorphShapeExporter(SWF swf, SHAPE shape, SHAPE endShape, int id, SVGExporter exporter, Color defaultColor, ColorTransform colorTransform, double zoom) {
        super(shape, endShape, colorTransform, zoom);
        this.swf = swf;
        this.id = id;
        this.defaultColor = defaultColor;
        this.exporter = exporter;
    }

    @Override
    public void beginFill(RGB color, RGB colorEnd) {
        if (color == null) {
            color = new RGB(defaultColor == null ? Color.black : defaultColor);
        }
        if (colorEnd == null) {
            colorEnd = new RGB(defaultColor == null ? Color.black : defaultColor);
        }
        finalizePath();
        path.setAttribute("stroke", "none");
        path.setAttribute("fill", color.toHexRGB());
        path.setAttribute("fill-rule", "evenodd");
        path.appendChild(createAnimateElement("fill", color.toHexRGB(), colorEnd.toHexRGB()));
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            if (colorA.alpha != 255) {
                path.setAttribute("fill-opacity", Float.toString(colorA.getAlphaFloat()));
            }
            RGBA colorAEnd = (RGBA) colorEnd;
            path.appendChild(createAnimateElement("fill-opacity", colorA.getAlphaFloat(), colorAEnd.getAlphaFloat()));
        }
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio, float focalPointRatioEnd) {
        finalizePath();
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? exporter.createElement("linearGradient")
                : exporter.createElement("radialGradient");
        populateGradientElement(gradient, type, gradientRecords, gradientRecordsEnd, matrix, matrixEnd, spreadMethod, interpolationMethod, focalPointRatio);
        int id = exporter.gradients.indexOf(gradient);
        if (id < 0) {
            // todo: filter same gradients
            id = exporter.gradients.size();
            exporter.gradients.add(gradient);
        }
        gradient.setAttribute("id", "gradient" + id);
        path.setAttribute("stroke", "none");
        path.setAttribute("fill", "url(#gradient" + id + ")");
        path.setAttribute("fill-rule", "evenodd");
        exporter.addToDefs(gradient);
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, Matrix matrixEnd, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
        ImageTag image = swf.getImage(bitmapId);
        if (image != null) {
            SerializableImage img = image.getImageCached();
            if (img != null) {
                if (colorTransform != null) {
                    colorTransform.apply(img);
                }

                int width = img.getWidth();
                int height = img.getHeight();
                lastPatternId++;
                String patternId = "PatternID_" + id + "_" + lastPatternId;
                ImageFormat format = image.getImageFormat();
                byte[] imageData = Helper.readStream(image.getImageData());
                String base64ImgData = Helper.byteArrayToBase64String(imageData);
                path.setAttribute("style", "fill:url(#" + patternId + ")");
                Element pattern = exporter.createElement("pattern");
                pattern.setAttribute("id", patternId);
                pattern.setAttribute("patternUnits", "userSpaceOnUse");
                pattern.setAttribute("overflow", "visible");
                pattern.setAttribute("width", "" + width);
                pattern.setAttribute("height", "" + height);
                pattern.setAttribute("viewBox", "0 0 " + width + " " + height);
                if (matrix != null) {
                    matrix = matrix.clone();
                    matrix.rotateSkew0 *= zoom / SWF.unitDivisor;
                    matrix.rotateSkew1 *= zoom / SWF.unitDivisor;
                    matrix.scaleX *= zoom / SWF.unitDivisor;
                    matrix.scaleY *= zoom / SWF.unitDivisor;
                    matrixEnd = matrixEnd.clone();
                    matrixEnd.rotateSkew0 *= zoom / SWF.unitDivisor;
                    matrixEnd.rotateSkew1 *= zoom / SWF.unitDivisor;
                    matrixEnd.scaleX *= zoom / SWF.unitDivisor;
                    matrixEnd.scaleY *= zoom / SWF.unitDivisor;
                    addMatrixAnimation(pattern, "patternTransform", matrix, matrixEnd);
                }
                Element imageElement = exporter.createElement("image");
                imageElement.setAttribute("width", "" + width);
                imageElement.setAttribute("height", "" + height);
                imageElement.setAttribute("xlink:href", "data:image/" + format + ";base64," + base64ImgData);
                pattern.appendChild(imageElement);
                exporter.addToGroup(pattern);
            }
        }
    }

    @Override
    public void lineStyle(double thickness, double thicknessEnd, RGB color, RGB colorEnd, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit) {
        finalizePath();
        thickness *= zoom / SWF.unitDivisor;
        thicknessEnd *= zoom / SWF.unitDivisor;
        path.setAttribute("fill", "none");
        if (color != null) {
            path.setAttribute("stroke", color.toHexRGB());
            path.appendChild(createAnimateElement("stroke", color.toHexRGB(), colorEnd.toHexRGB()));
        }
        path.setAttribute("stroke-width", Double.toString(thickness == 0 ? 1 : thickness));
        path.appendChild(createAnimateElement("stroke-width", thickness, thicknessEnd));

        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            if (colorA.alpha != 255) {
                path.setAttribute("stroke-opacity", Float.toString(colorA.getAlphaFloat()));
            }
            RGBA colorAEnd = (RGBA) colorEnd;
            path.appendChild(createAnimateElement("fill-opacity", colorA.getAlphaFloat(), colorAEnd.getAlphaFloat()));
        }
        switch (startCaps) {
            case LINESTYLE2.NO_CAP:
                path.setAttribute("stroke-linecap", "butt");
                break;
            case LINESTYLE2.SQUARE_CAP:
                path.setAttribute("stroke-linecap", "square");
                break;
            default:
                path.setAttribute("stroke-linecap", "round");
                break;
        }
        switch (joints) {
            case LINESTYLE2.BEVEL_JOIN:
                path.setAttribute("stroke-linejoin", "bevel");
                break;
            case LINESTYLE2.ROUND_JOIN:
                path.setAttribute("stroke-linejoin", "round");
                break;
            default:
                path.setAttribute("stroke-linejoin", "miter");
                if (miterLimit >= 1 && miterLimit != 4f) {
                    path.setAttribute("stroke-miterlimit", Double.toString(miterLimit));
                }
                break;
        }
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio, float focalPointRatioEnd) {
        path.removeAttribute("stroke-opacity");
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? exporter.createElement("linearGradient")
                : exporter.createElement("radialGradient");
        populateGradientElement(gradient, type, gradientRecords, gradientRecordsEnd, matrix, matrixEnd, spreadMethod, interpolationMethod, focalPointRatio);
        int id = exporter.gradients.indexOf(gradient);
        if (id < 0) {
            // todo: filter same gradients
            id = exporter.gradients.size();
            exporter.gradients.add(gradient);
        }
        gradient.setAttribute("id", "gradient" + id);
        path.setAttribute("stroke", "url(#gradient" + id + ")");
        path.setAttribute("fill", "none");
        exporter.addToDefs(gradient);
    }

    private Element createAnimateElement(String attributeName, Object startValue, Object endValue) {
        Element animate = exporter.createElement("animate");
        animate.setAttribute("dur", "2s"); // todo
        animate.setAttribute("repeatCount", "indefinite");
        animate.setAttribute("attributeName", attributeName);
        animate.setAttribute("values", startValue + ";" + endValue);
        return animate;
    }

    @Override
    protected void finalizePath() {
        if (path != null && pathData != null && pathData.length() > 0) {
            path.setAttribute("d", pathData.toString().trim());
            path.appendChild(createAnimateElement("d", pathData.toString().trim(), pathDataEnd.toString().trim()));
            exporter.addToGroup(path);
        }
        path = exporter.createElement("path");
        super.finalizePath();
    }

    private void addMatrixAnimation(Element element, String attribute, Matrix matrix, Matrix matrixEnd) {
        final int animationLength = 2; // todo
        final String animationLengthStr = animationLength + "s";

        element.setAttribute(attribute, matrix.getSvgTransformationString(SWF.unitDivisor / zoom, 1));

        // QR decomposition
        double translateX = roundPixels400(matrix.translateX * zoom / SWF.unitDivisor);
        double translateY = roundPixels400(matrix.translateY * zoom / SWF.unitDivisor);
        double a = matrix.scaleX;
        double b = matrix.rotateSkew0;
        double c = matrix.rotateSkew1;
        double d = matrix.scaleY;
        double r = Math.sqrt(a * a + b * b);
        double det = a * d - b * c;
        double rotate = roundPixels400(Math.signum(b) * Math.acos(a / r) * 180 / Math.PI);
        double scaleX = roundPixels400(r);
        double scaleY = roundPixels400(det / r);
        double skewX = roundPixels400(Math.atan((a * c + b * d) / (r * r)) * 180 / Math.PI);

        double translateXEnd = roundPixels400(matrixEnd.translateX * zoom / SWF.unitDivisor);
        double translateYEnd = roundPixels400(matrixEnd.translateY * zoom / SWF.unitDivisor);
        a = matrixEnd.scaleX;
        b = matrixEnd.rotateSkew0;
        c = matrixEnd.rotateSkew1;
        d = matrixEnd.scaleY;
        double rEnd = Math.sqrt(a * a + b * b);
        double detEnd = a * d - b * c;
        double rotateEnd = roundPixels400(Math.signum(b) * Math.acos(a / rEnd) * 180 / Math.PI);
        double scaleXEnd = roundPixels400(rEnd);
        double scaleYEnd = roundPixels400(detEnd / rEnd);
        double skewXEnd = roundPixels400(Math.atan((a * c + b * d) / (rEnd * rEnd)) * 180 / Math.PI);

        Element animateClear = exporter.createElement("animateTransform");
        animateClear.setAttribute("dur", animationLengthStr);
        animateClear.setAttribute("repeatCount", "indefinite");
        animateClear.setAttribute("attributeName", attribute);
        animateClear.setAttribute("type", "scale");
        animateClear.setAttribute("additive", "replace");
        animateClear.setAttribute("from", "1");
        animateClear.setAttribute("to", "1");

        Element animateTranslate = exporter.createElement("animateTransform");
        animateTranslate.setAttribute("dur", animationLengthStr);
        animateTranslate.setAttribute("repeatCount", "indefinite");
        animateTranslate.setAttribute("attributeName", attribute);
        animateTranslate.setAttribute("type", "translate");
        animateTranslate.setAttribute("additive", "sum");
        animateTranslate.setAttribute("from", translateX + " " + translateY);
        animateTranslate.setAttribute("to", translateXEnd + " " + translateYEnd);

        Element animateRotate = exporter.createElement("animateTransform");
        animateRotate.setAttribute("dur", animationLengthStr);
        animateRotate.setAttribute("repeatCount", "indefinite");
        animateRotate.setAttribute("attributeName", attribute);
        animateRotate.setAttribute("type", "rotate");
        animateRotate.setAttribute("additive", "sum");
        animateRotate.setAttribute("from", Double.toString(rotate));
        animateRotate.setAttribute("to", Double.toString(rotateEnd));

        Element animateScale = exporter.createElement("animateTransform");
        animateScale.setAttribute("dur", animationLengthStr);
        animateScale.setAttribute("repeatCount", "indefinite");
        animateScale.setAttribute("attributeName", attribute);
        animateScale.setAttribute("type", "scale");
        animateScale.setAttribute("additive", "sum");
        animateScale.setAttribute("from", scaleX + " " + scaleY);
        animateScale.setAttribute("to", scaleXEnd + " " + scaleYEnd);

        Element animateSkewX = exporter.createElement("animateTransform");
        animateSkewX.setAttribute("dur", animationLengthStr);
        animateSkewX.setAttribute("repeatCount", "indefinite");
        animateSkewX.setAttribute("attributeName", attribute);
        animateSkewX.setAttribute("type", "skewX");
        animateSkewX.setAttribute("additive", "sum");
        animateSkewX.setAttribute("from", Double.toString(skewX));
        animateSkewX.setAttribute("to", Double.toString(skewXEnd));

        element.appendChild(animateClear);
        element.appendChild(animateTranslate);
        element.appendChild(animateRotate);
        element.appendChild(animateScale);
        element.appendChild(animateSkewX);

        /*
         // LDU decomposition

         double translateX = roundPixels400(matrix.translateX * zoom / SWF.unitDivisor);
         double translateY = roundPixels400(matrix.translateY * zoom / SWF.unitDivisor);
         double a = matrix.scaleX;
         double b = matrix.rotateSkew0;
         double c = matrix.rotateSkew1;
         double d = matrix.scaleY;
         double det = a * d - b * c;
         double skewY = roundPixels400(Math.atan2(b, a) * 180 / Math.PI);
         double scaleX = roundPixels400(a);
         double scaleY =  roundPixels400(det / a);
         double skewX = roundPixels400(Math.atan2(c, a) * 180 / Math.PI);

         double translateXEnd = roundPixels400(matrixEnd.translateX * zoom / SWF.unitDivisor);
         double translateYEnd = roundPixels400(matrixEnd.translateY * zoom / SWF.unitDivisor);
         a = matrixEnd.scaleX;
         b = matrixEnd.rotateSkew0;
         c = matrixEnd.rotateSkew1;
         d = matrixEnd.scaleY;
         double detEnd = a * d - b * c;
         double skewYEnd = roundPixels400(Math.atan2(b, a) * 180 / Math.PI);
         double scaleXEnd = roundPixels400(a);
         double scaleYEnd =  roundPixels400(detEnd / a);
         double skewXEnd = roundPixels400(Math.atan2(c, a) * 180 / Math.PI);

         Element animateClear = exporter.createElement("animateTransform");
         animateClear.setAttribute("dur", animationLengthStr);
         animateClear.setAttribute("repeatCount", "indefinite");
         animateClear.setAttribute("attributeName", attribute);
         animateClear.setAttribute("type", "scale");
         animateClear.setAttribute("additive", "replace");
         animateClear.setAttribute("from", "1");
         animateClear.setAttribute("to", "1");

         Element animateTranslate = exporter.createElement("animateTransform");
         animateTranslate.setAttribute("dur", animationLengthStr);
         animateTranslate.setAttribute("repeatCount", "indefinite");
         animateTranslate.setAttribute("attributeName", attribute);
         animateTranslate.setAttribute("type", "translate");
         animateTranslate.setAttribute("additive", "sum");
         animateTranslate.setAttribute("from", translateX + " " + translateY);
         animateTranslate.setAttribute("to", translateXEnd + " " + translateYEnd);

         Element animateSkewY = exporter.createElement("animateTransform");
         animateSkewY.setAttribute("dur", animationLengthStr);
         animateSkewY.setAttribute("repeatCount", "indefinite");
         animateSkewY.setAttribute("attributeName", attribute);
         animateSkewY.setAttribute("type", "skewY");
         animateSkewY.setAttribute("additive", "sum");
         animateSkewY.setAttribute("from", Double.toString(skewY));
         animateSkewY.setAttribute("to", Double.toString(skewYEnd));

         Element animateScale = exporter.createElement("animateTransform");
         animateScale.setAttribute("dur", animationLengthStr);
         animateScale.setAttribute("repeatCount", "indefinite");
         animateScale.setAttribute("attributeName", attribute);
         animateScale.setAttribute("type", "scale");
         animateScale.setAttribute("additive", "sum");
         animateScale.setAttribute("from", scaleX + " " + scaleY);
         animateScale.setAttribute("to", scaleXEnd + " " + scaleYEnd);

         Element animateSkewX = exporter.createElement("animateTransform");
         animateSkewX.setAttribute("dur", animationLengthStr);
         animateSkewX.setAttribute("repeatCount", "indefinite");
         animateSkewX.setAttribute("attributeName", attribute);
         animateSkewX.setAttribute("type", "skewX");
         animateSkewX.setAttribute("additive", "sum");
         animateSkewX.setAttribute("from", Double.toString(skewX));
         animateSkewX.setAttribute("to", Double.toString(skewXEnd));

         element.appendChild(animateClear);
         element.appendChild(animateTranslate);
         element.appendChild(animateSkewY);
         element.appendChild(animateScale);
         element.appendChild(animateSkewX);*/
    }

    protected void populateGradientElement(Element gradient, int type, GRADRECORD[] gradientRecords, GRADRECORD[] gradientRecordsEnd, Matrix matrix, Matrix matrixEnd, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        gradient.setAttribute("gradientUnits", "userSpaceOnUse");
        if (type == FILLSTYLE.LINEAR_GRADIENT) {
            gradient.setAttribute("x1", "-819.2");
            gradient.setAttribute("x2", "819.2");
        } else {
            gradient.setAttribute("r", "819.2");
            gradient.setAttribute("cx", "0");
            gradient.setAttribute("cy", "0");
            if (focalPointRatio != 0) {
                gradient.setAttribute("fx", Double.toString(819.2 * focalPointRatio));
                gradient.setAttribute("fy", "0");
            }
        }
        switch (spreadMethod) {
            case GRADIENT.SPREAD_PAD_MODE:
                gradient.setAttribute("spreadMethod", "pad");
                break;
            case GRADIENT.SPREAD_REFLECT_MODE:
                gradient.setAttribute("spreadMethod", "reflect");
                break;
            case GRADIENT.SPREAD_REPEAT_MODE:
                gradient.setAttribute("spreadMethod", "repeat");
                break;
        }
        if (interpolationMethod == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE) {
            gradient.setAttribute("color-interpolation", "linearRGB");
        }
        if (matrix != null) {
            addMatrixAnimation(gradient, "gradientTransform", matrix, matrixEnd);
        }
        for (int i = 0; i < gradientRecords.length; i++) {
            GRADRECORD record = gradientRecords[i];
            GRADRECORD recordEnd = gradientRecordsEnd[i];
            Element gradientEntry = exporter.createElement("stop");
            gradientEntry.setAttribute("offset", Double.toString(record.ratio / 255.0));
            gradientEntry.appendChild(createAnimateElement("offset", record.ratio / 255.0, recordEnd.ratio / 255.0));
            RGB color = record.color;
            RGB colorEnd = recordEnd.color;
            //if(colors.get(i) != 0) {
            gradientEntry.setAttribute("stop-color", color.toHexRGB());
            gradientEntry.appendChild(createAnimateElement("stop-color", color.toHexRGB(), colorEnd.toHexRGB()));
            //}
            if (color instanceof RGBA) {
                RGBA colorA = (RGBA) color;
                if (colorA.alpha != 255) {
                    gradientEntry.setAttribute("stop-opacity", Float.toString(colorA.getAlphaFloat()));
                }
                RGBA colorAEnd = (RGBA) colorEnd;
                gradientEntry.appendChild(createAnimateElement("stop-opacity", colorA.getAlphaFloat(), colorAEnd.getAlphaFloat()));
            }
            gradient.appendChild(gradientEntry);
        }
    }

    protected double roundPixels400(double pixels) {
        return Math.round(pixels * 10000) / 10000.0;
    }
}

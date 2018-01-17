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
public class SVGShapeExporter extends DefaultSVGShapeExporter {

    protected Element path;

    protected int id;

    protected int lastPatternId;

    private final Color defaultColor;

    private final SWF swf;

    private final SVGExporter exporter;

    public SVGShapeExporter(SWF swf, SHAPE shape, int id, SVGExporter exporter, Color defaultColor, ColorTransform colorTransform, double zoom) {
        super(swf, shape, colorTransform, zoom);
        this.swf = swf;
        this.id = id;
        this.defaultColor = defaultColor;
        this.exporter = exporter;
    }

    @Override
    public void beginFill(RGB color) {
        if (color == null && defaultColor != null) {
            color = new RGB(defaultColor);
        }
        finalizePath();
        path.setAttribute("stroke", "none");
        if (color != null) {
            path.setAttribute("fill", color.toHexRGB());
        }
        path.setAttribute("fill-rule", "evenodd");
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            if (colorA.alpha != 255) {
                path.setAttribute("fill-opacity", Float.toString(colorA.getAlphaFloat()));
            }
        }
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        finalizePath();
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? exporter.createElement("linearGradient")
                : exporter.createElement("radialGradient");
        populateGradientElement(gradient, type, gradientRecords, matrix, spreadMethod, interpolationMethod, focalPointRatio);
        int id = exporter.gradients.indexOf(gradient);
        if (id < 0) {
            // todo: filter same gradients
            id = exporter.gradients.size();
            exporter.gradients.add(gradient);
        }
        String gradientId = "gradient" + id;
        gradient.setAttribute("id", gradientId);
        path.setAttribute("stroke", "none");
        path.setAttribute("fill", "url(#" + gradientId + ")");
        path.setAttribute("fill-rule", "evenodd");
        exporter.addToDefs(gradient);
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
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
                    pattern.setAttribute("patternTransform", matrix.getSvgTransformationString(SWF.unitDivisor / zoom, SWF.unitDivisor / zoom));
                }
                Element imageElement = exporter.createElement("image");
                imageElement.setAttribute("width", "" + width);
                imageElement.setAttribute("height", "" + height);
                imageElement.setAttribute("xlink:href", "data:image/" + format + ";base64," + base64ImgData);
                pattern.appendChild(imageElement);
                exporter.addToGroup(pattern);
                return;
            }
        }

        path.setAttribute("fill", "#ff0000");
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit) {
        finalizePath();
        thickness *= zoom / SWF.unitDivisor;
        path.setAttribute("fill", "none");
        if (color != null) {
            path.setAttribute("stroke", color.toHexRGB());
        }
        path.setAttribute("stroke-width", Double.toString(thickness == 0 ? 1 : thickness));
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            if (colorA.alpha != 255) {
                path.setAttribute("stroke-opacity", Float.toString(colorA.getAlphaFloat()));
            }
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
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        path.removeAttribute("stroke-opacity");
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? exporter.createElement("linearGradient")
                : exporter.createElement("radialGradient");
        populateGradientElement(gradient, type, gradientRecords, matrix, spreadMethod, interpolationMethod, focalPointRatio);
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

    @Override
    protected void finalizePath() {
        if (path != null && pathData != null && pathData.length() > 0) {
            path.setAttribute("d", pathData.toString().trim());
            exporter.addToGroup(path);
        }
        path = exporter.createElement("path");
        super.finalizePath();
    }

    protected void populateGradientElement(Element gradient, int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
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
            gradient.setAttribute("gradientTransform", matrix.getSvgTransformationString(SWF.unitDivisor / zoom, 1));
        }
        for (int i = 0; i < gradientRecords.length; i++) {
            GRADRECORD record = gradientRecords[i];
            Element gradientEntry = exporter.createElement("stop");
            gradientEntry.setAttribute("offset", Double.toString(record.ratio / 255.0));
            RGB color = record.color;
            //if(colors.get(i) != 0) {
            gradientEntry.setAttribute("stop-color", color.toHexRGB());
            //}
            if (color instanceof RGBA) {
                RGBA colorA = (RGBA) color;
                if (colorA.alpha != 255) {
                    gradientEntry.setAttribute("stop-opacity", Float.toString(colorA.getAlphaFloat()));
                }
            }
            gradient.appendChild(gradientEntry);
        }
    }
}

/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.w3c.dom.Element;

/**
 * SVG shape exporter.
 *
 * @author JPEXS, Claus Wahlers
 */
public class SVGShapeExporter extends DefaultSVGShapeExporter {

    /**
     * Path
     */
    protected Element path;

    /**
     * Id
     */
    protected int id;

    /**
     * Last pattern id
     */
    protected int lastPatternId;

    /**
     * Default color
     */
    private final Color defaultColor;

    /**
     * SWF
     */
    private final SWF swf;

    /**
     * Exporter
     */
    private final SVGExporter exporter;

    /**
     * Constructor.
     * @param windingRule Winding rule
     * @param shapeNum Shape number
     * @param swf SWF
     * @param shape Shape
     * @param id Id
     * @param exporter Exporter
     * @param defaultColor Default color
     * @param colorTransform Color transform
     * @param zoom Zoom
     */
    public SVGShapeExporter(int windingRule, int shapeNum, SWF swf, SHAPE shape, int id, SVGExporter exporter, Color defaultColor, ColorTransform colorTransform, double zoom) {
        super(windingRule, shapeNum, swf, shape, colorTransform, zoom);
        this.swf = swf;
        this.id = id;
        this.defaultColor = defaultColor;
        this.exporter = exporter;
    }

    @Override
    public void beginFill(RGB color) {
        if (aliasedFill) {
            return;
        }
        if (color == null && defaultColor != null) {
            color = new RGB(defaultColor);
        }
        finalizePath();
        path.setAttribute("stroke", "none");
        if (color != null) {
            path.setAttribute("fill", color.toHexRGB());
        }
        path.setAttribute("fill-rule", windingRule == ShapeTag.WIND_NONZERO ? "nonzero" : "evenodd");
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            if (colorA.alpha != 255) {
                path.setAttribute("fill-opacity", Float.toString(colorA.getAlphaFloat()));
            }
        }
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        if (aliasedFill) {
            return;
        }
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

    private String getPattern(int bitmapId, Matrix matrix, ColorTransform colorTransform, boolean smoothed) {
        ImageTag image = swf.getImage(bitmapId);
        if (image != null) {
            SerializableImage img = image.getImageCached();
            if (img != null) {
                int width = img.getWidth();
                int height = img.getHeight();
                lastPatternId++;
                String patternId = "PatternID_" + id + "_" + lastPatternId;
                ImageFormat format = image.getImageFormat();
                byte[] imageData = Helper.readStream(image.getConvertedImageData());
                
                if (colorTransform != null) {
                    //Apply transform and convert it to PNG.
                    //If we use the same format as input, then we would lose quality for JPEGs.
                    //This will also make significantly larger files for JPEGs.
                    //It would be better if we use a SVG filter for colormatrix transform,
                    //but that will be problematic during importing it back (we cannot properly parse filters)
                    
                    img = colorTransform.apply(img);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(img.getBufferedImage(), "PNG", baos);
                    } catch (IOException iex) {
                        //ignore
                    }
                    imageData = baos.toByteArray();
                    format = ImageFormat.PNG;
                }
                
                String base64ImgData = Helper.byteArrayToBase64String(imageData);

                Element pattern = exporter.createElement("pattern");
                pattern.setAttribute("id", patternId);
                pattern.setAttribute("patternUnits", "userSpaceOnUse");
                pattern.setAttribute("overflow", "visible");
                pattern.setAttribute("width", "" + width);
                pattern.setAttribute("height", "" + height);
                pattern.setAttribute("viewBox", "0 0 " + width + " " + height);
                //Smoothed attribute was used in older FFDec versions - it is now only used for reading, for backwards compatibility
                //pattern.setAttribute("ffdec:smoothed", smoothed ? "true" : "false");
                if (matrix != null) {
                    pattern.setAttribute("patternTransform", matrix.getSvgTransformationString(SWF.unitDivisor / zoom, SWF.unitDivisor / zoom));
                }
                Element imageElement = exporter.createElement("image");
                imageElement.setAttribute("width", "" + width);
                imageElement.setAttribute("height", "" + height);
                imageElement.setAttribute("xlink:href", "data:image/" + format + ";base64," + base64ImgData);
                if (smoothed) {
                    imageElement.setAttribute("style", "image-rendering:optimizeQuality");                    
                } else {
                    //https://stackoverflow.com/questions/50184674/stop-auto-image-smoothing-inside-an-svgz                
                    imageElement.setAttribute("style", "image-rendering:optimizeSpeed; image-rendering:pixelated");
                }
                pattern.appendChild(imageElement);
                exporter.addToGroup(pattern);
                return patternId;
            }
        }
        return null;
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        if (aliasedFill) {
            return;
        }
        finalizePath();
        String patternId = getPattern(bitmapId, matrix, colorTransform, smooth);
        path.setAttribute("ffdec:fill-bitmapId", "" + bitmapId);
        if (patternId != null) {
            path.setAttribute("style", "fill:url(#" + patternId + ")");
            return;
        }
        path.setAttribute("fill", "#ff0000");
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
        finalizePath();
        thickness *= zoom / SWF.unitDivisor;
        path.setAttribute("fill", "none");
        if (color != null) {
            path.setAttribute("stroke", color.toHexRGB());
        }
        path.setAttribute("stroke-width", Double.toString(thickness));
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
                path.setAttribute("stroke-linejoin", "miter-clip");
                if (miterLimit >= 1) {
                    path.setAttribute("stroke-miterlimit", Double.toString(miterLimit));
                }
                break;
        }
        if ("NONE".equals(scaleMode)) {
            path.setAttribute("vector-effect", "non-scaling-stroke");
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
    public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        String patternId = getPattern(bitmapId, matrix, colorTransform, smooth);
        path.setAttribute("ffdec:stroke-bitmapId", "" + bitmapId);
        if (patternId != null) {
            path.setAttribute("stroke", "url(#" + patternId + ")");
            return;
        }
        path.setAttribute("stroke", "#ff0000");
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

    /**
     * Populates gradient element.
     * @param gradient Gradient
     * @param type Type
     * @param gradientRecords Gradient records
     * @param matrix Matrix
     * @param spreadMethod Spread method
     * @param interpolationMethod Interpolation method
     * @param focalPointRatio Focal point ratio
     */
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

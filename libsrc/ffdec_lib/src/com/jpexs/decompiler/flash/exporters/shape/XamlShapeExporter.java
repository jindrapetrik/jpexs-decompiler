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
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPE;
import java.awt.Color;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author JPEXS
 */
public class XamlShapeExporter extends ShapeExporterBase {

    /**
     * Draw command L
     */
    protected static final String DRAW_COMMAND_L = "L";

    /**
     * Draw command Q
     */
    protected static final String DRAW_COMMAND_Q = "Q";

    /**
     * Path data
     */
    protected Element pathData;

    /**
     * Path geometry
     */
    protected Element pathGeometry;

    /**
     * Path figure
     */
    protected Element pathFigure;

    /**
     * Zoom
     */
    protected double zoom;

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
     * Display zoom
     */
    private final double displayZoom;

    /**
     * Thickness scale
     */
    private final double thicknessScale;

    /**
     * XML document
     */
    private Document document;

    /**
     * XML root element
     */
    private Element root;

    /**
     * Image files
     */
    private final Map<Integer, String> imageFiles;

    /**
     * Geometry only
     */
    private final boolean geometryOnly;

    /**
     * Drawing lines?
     */
    private boolean inLines = false;
    private final Matrix geometryMatrix;

    /**
     * Constructor.
     *
     * @param windingRule Winding rule
     * @param shapeNum Shape number
     * @param swf SWF
     * @param shape Shape
     * @param id Id
     * @param defaultColor Default color
     * @param colorTransform Color transform
     * @param zoom Zoom - shape zoom
     * @param displayZoom Display zoom - overall SVG zoom
     * @param strokeTransformation Stroke transformation
     * @param imageFiles Image files
     * @param geometryOnly Geometry only
     * @param geometryMatrix Geometry matrix
     */
    public XamlShapeExporter(int windingRule, int shapeNum, SWF swf, SHAPE shape, int id, Color defaultColor, ColorTransform colorTransform, double zoom, double displayZoom, Matrix strokeTransformation, Map<Integer, String> imageFiles, boolean geometryOnly, Matrix geometryMatrix) {
        super(windingRule, shapeNum, swf, shape, colorTransform);
        this.swf = swf;
        this.id = id;
        this.defaultColor = defaultColor;
        this.zoom = zoom;
        this.displayZoom = displayZoom;
        this.imageFiles = imageFiles;
        this.geometryOnly = geometryOnly;
        this.geometryMatrix = geometryMatrix;

        thicknessScale = Math.sqrt(Math.abs(strokeTransformation.scaleX * strokeTransformation.scaleY - strokeTransformation.rotateSkew0 * strokeTransformation.rotateSkew1));

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            document = docBuilder.newDocument();

            if (geometryOnly) {
                pathGeometry = document.createElement("PathGeometry");
                pathGeometry.setAttribute("FillRule", windingRule == ShapeTag.WIND_NONZERO ? "Nonzero" : "EvenOdd");
                applyMatrix();
                document.appendChild(pathGeometry);
                root = pathGeometry;
            } else {
                root = document.createElement("Canvas");
                document.appendChild(root);
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(XamlShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void applyMatrix() {
        if (geometryMatrix == null) {
            return;
        }
        Element transform = document.createElement("PathGeometry.Transform");
        Element matrixTransform = document.createElement("MatrixTransform");
        matrixTransform.setAttribute("Matrix", geometryMatrix.getXamlTransformationString(SWF.unitDivisor / zoom, 1)); //zoom is applied to individual coordinates
        transform.appendChild(matrixTransform);
        pathGeometry.appendChild(transform);
    }

    @Override
    public void beginShape() {
    }

    @Override
    public void endShape() {
    }

    @Override
    public void beginFills() {
    }

    @Override
    public void endFills() {
    }

    @Override
    public void beginLines() {
        inLines = true;
    }

    @Override
    public void endLines(boolean close) {
        inLines = false;
        if (close && !geometryOnly) {
            pathFigure.setAttribute("IsClosed", "True");
        }

        finalizePath();
    }

    @Override
    public void beginFill(RGB color) {
        if (color == null && defaultColor != null) {
            color = new RGB(defaultColor);
        }
        finalizePath();
        //path.setAttribute("Stroke", "");
        if (color != null) {
            String colorHex;
            if (color instanceof RGBA) {
                colorHex = ((RGBA) color).toHexARGB();
            } else {
                colorHex = color.toHexRGB();
            }
            path.setAttribute("Fill", colorHex);
        }
    }

    @Override
    public void beginGradientFill(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        finalizePath();
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? document.createElement("LinearGradientBrush")
                : document.createElement("RadialGradientBrush");
        populateGradientElement(gradient, type, gradientRecords, matrix, spreadMethod, interpolationMethod, focalPointRatio);

        //path.setAttribute("Stroke", "");
        Element pathFill = document.createElement("Path.Fill");
        pathFill.appendChild(gradient);
        path.appendChild(pathFill);
    }

    @Override
    public void beginBitmapFill(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        finalizePath();
        Element imageBrush = document.createElement("ImageBrush");
        if (matrix != null) {
            Element transform = document.createElement("ImageBrush.Transform");
            Element matrixTransform = document.createElement("MatrixTransform");
            matrixTransform.setAttribute("Matrix", matrix.getXamlTransformationString(SWF.unitDivisor / zoom, SWF.unitDivisor));
            transform.appendChild(matrixTransform);
            imageBrush.appendChild(transform);
        }
        imageBrush.setAttribute("ImageSource", imageFiles.get(bitmapId));
        ImageTag image = (ImageTag) swf.getCharacter(bitmapId);
        imageBrush.setAttribute("Viewport", "0,0," + image.getImageDimension().width + "," + image.getImageDimension().height);
        imageBrush.setAttribute("ViewportUnits", "Absolute");
        if (repeat) {
            imageBrush.setAttribute("TileMode", "Tile");
        }
        if (smooth) {
            path.setAttribute("RenderOptions.BitmapScalingMode", "HighQuality");
        } else {
            path.setAttribute("RenderOptions.BitmapScalingMode", "NearestNeighbor");
        }
        Element pathFill = document.createElement("Path.Fill");
        pathFill.appendChild(imageBrush);
        path.appendChild(pathFill);
    }

    @Override
    public void endFill() {
        finalizePath();
    }

    @Override
    public void lineStyle(double thickness, RGB color, boolean pixelHinting, String scaleMode, int startCaps, int endCaps, int joints, float miterLimit, boolean noClose) {
        finalizePath();

        //always display minimum stroke of 1 pixel, no matter how zoomed it is
        if (thickness * displayZoom * thicknessScale < 1 * SWF.unitDivisor) {
            //path.setAttribute("ffdec:has-small-stroke", "true");
            //path.setAttribute("ffdec:original-stroke-width", Double.toString(thickness * displayZoom / SWF.unitDivisor));
            thickness = 1 * SWF.unitDivisor / displayZoom / thicknessScale;
        }

        thickness *= zoom / SWF.unitDivisor;
        //path.setAttribute("Fill", "");
        if (color instanceof RGBA) {
            RGBA colorA = (RGBA) color;
            path.setAttribute("Stroke", colorA.toHexARGB());
        } else if (color != null) {
            path.setAttribute("Stroke", color.toHexRGB());
        }
        path.setAttribute("StrokeThickness", formatDouble(thickness));

        switch (startCaps) {
            case LINESTYLE2.NO_CAP:
                path.setAttribute("StrokeStartLineCap", "Flat");
                path.setAttribute("StrokeEndLineCap", "Flat");
                break;
            case LINESTYLE2.SQUARE_CAP:
                path.setAttribute("StrokeStartLineCap", "Square");
                path.setAttribute("StrokeEndLineCap", "Square");
                break;
            default:
                path.setAttribute("StrokeStartLineCap", "Round");
                path.setAttribute("StrokeEndLineCap", "Round");
                break;
        }
        switch (joints) {
            case LINESTYLE2.BEVEL_JOIN:
                path.setAttribute("StrokeLineJoin", "Bevel");
                break;
            case LINESTYLE2.ROUND_JOIN:
                path.setAttribute("StrokeLineJoin", "Round");
                break;
            default:
                path.setAttribute("StrokeLineJoin", "Miter");
                if (miterLimit >= 1) {
                    path.setAttribute("StrokeMiterLimit", formatDouble(miterLimit));
                }
                break;
        }
    }

    @Override
    public void lineGradientStyle(int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        Element gradient = (type == FILLSTYLE.LINEAR_GRADIENT)
                ? document.createElement("LinearGradientBrush")
                : document.createElement("RadialGradientBrush");
        populateGradientElement(gradient, type, gradientRecords, matrix, spreadMethod, interpolationMethod, focalPointRatio);
        Element pathStroke = document.createElement("Path.Stroke");
        pathStroke.appendChild(gradient);
        path.appendChild(pathStroke);
        //path.setAttribute("Fill", "");
    }

    @Override
    public void lineBitmapStyle(int bitmapId, Matrix matrix, boolean repeat, boolean smooth, ColorTransform colorTransform) {
        Element imageBrush = document.createElement("ImageBrush");
        if (matrix != null) {
            Element transform = document.createElement("ImageBrush.Transform");
            Element matrixTransform = document.createElement("MatrixTransform");
            matrixTransform.setAttribute("Matrix", matrix.getXamlTransformationString(SWF.unitDivisor / zoom, SWF.unitDivisor));
            transform.appendChild(matrixTransform);
            imageBrush.appendChild(transform);
        }
        imageBrush.setAttribute("ImageSource", imageFiles.get(bitmapId));
        ImageTag image = (ImageTag) swf.getCharacter(bitmapId);
        imageBrush.setAttribute("Viewport", "0,0," + image.getImageDimension().width + "," + image.getImageDimension().height);
        imageBrush.setAttribute("ViewportUnits", "Absolute");
        if (repeat) {
            imageBrush.setAttribute("TileMode", "Tile");
        }
        if (smooth) {
            imageBrush.setAttribute("RenderOptions.BitmapScalingMode", "HighQuality");
        } else {
            path.setAttribute("RenderOptions.BitmapScalingMode", "NearestNeighbor");
        }
        Element pathStroke = document.createElement("Path.Stroke");
        pathStroke.appendChild(imageBrush);
        path.appendChild(pathStroke);
    }

    @Override
    public void moveTo(double x, double y) {
        if (geometryOnly && inLines) {
            return;
        }
        pathFigure = document.createElement("PathFigure");
        pathFigure.setAttribute("StartPoint", formatDouble(roundPixels20(x * zoom / SWF.unitDivisor)) + "," + formatDouble(roundPixels20(y * zoom / SWF.unitDivisor)));
        pathGeometry.appendChild(pathFigure);
    }

    @Override
    public void lineTo(double x, double y) {
        if (geometryOnly && inLines) {
            return;
        }
        Element lineSegment = document.createElement("LineSegment");
        lineSegment.setAttribute("Point", formatDouble(roundPixels20(x * zoom / SWF.unitDivisor)) + "," + formatDouble(roundPixels20(y * zoom / SWF.unitDivisor)));
        pathFigure.appendChild(lineSegment);
    }

    @Override
    public void curveTo(double controlX, double controlY, double anchorX, double anchorY) {
        if (geometryOnly && inLines) {
            return;
        }
        Element quadraticBezierSegment = document.createElement("QuadraticBezierSegment");
        quadraticBezierSegment.setAttribute("Point1", formatDouble(roundPixels20(controlX * zoom / SWF.unitDivisor)) + "," + formatDouble(roundPixels20(controlY * zoom / SWF.unitDivisor)));
        quadraticBezierSegment.setAttribute("Point2", formatDouble(roundPixels20(anchorX * zoom / SWF.unitDivisor)) + "," + formatDouble(roundPixels20(anchorY * zoom / SWF.unitDivisor)));
        pathFigure.appendChild(quadraticBezierSegment);
    }

    /**
     * Finalizes path.
     */
    protected void finalizePath() {
        if (!geometryOnly && path != null && pathData != null && pathGeometry.getChildNodes().getLength() > 0) {
            root.appendChild(path);
            path.appendChild(pathData);
            if (!geometryOnly) {
                pathData.appendChild(pathGeometry);
            }
        }
        path = document.createElement("Path");
        pathData = document.createElement("Path.Data");
        if (!geometryOnly) {
            pathGeometry = document.createElement("PathGeometry");
            pathGeometry.setAttribute("FillRule", windingRule == ShapeTag.WIND_NONZERO ? "Nonzero" : "EvenOdd");
            applyMatrix();
        }
        pathFigure = document.createElement("PathFigure");
    }

    /**
     * Rounds pixels to 20.
     *
     * @param pixels Pixels
     * @return Rounded pixels
     */
    protected double roundPixels20(double pixels) {
        return Math.round(pixels * 100) / 100.0;
    }

    /**
     * Populates gradient element.
     *
     * @param gradient Gradient
     * @param type Type
     * @param gradientRecords Gradient records
     * @param matrix Matrix
     * @param spreadMethod Spread method
     * @param interpolationMethod Interpolation method
     * @param focalPointRatio Focal point ratio
     */
    protected void populateGradientElement(Element gradient, int type, GRADRECORD[] gradientRecords, Matrix matrix, int spreadMethod, int interpolationMethod, float focalPointRatio) {
        gradient.setAttribute("MappingMode", "Absolute");
        if (type == FILLSTYLE.LINEAR_GRADIENT) {
            gradient.setAttribute("StartPoint", "-819.2,0");
            gradient.setAttribute("EndPoint", "819.2,0");
        } else {
            gradient.setAttribute("RadiusX", "819.2");
            gradient.setAttribute("RadiusY", "819.2");
            gradient.setAttribute("Center", "0,0");
            if (focalPointRatio != 0) {
                gradient.setAttribute("GradientOrigin", Double.toString(819.2 * focalPointRatio) + ",0");
            }
        }
        switch (spreadMethod) {
            case GRADIENT.SPREAD_PAD_MODE:
                gradient.setAttribute("SpreadMethod", "Pad");
                break;
            case GRADIENT.SPREAD_REFLECT_MODE:
                gradient.setAttribute("SpreadMethod", "Reflect");
                break;
            case GRADIENT.SPREAD_REPEAT_MODE:
                gradient.setAttribute("SpreadMethod", "Repeat");
                break;
        }
        if (interpolationMethod == GRADIENT.INTERPOLATION_LINEAR_RGB_MODE) {
            gradient.setAttribute("ColorInterpolationMode", "ScRgbLinearInterpolation");
        }
        //default is SRgbLinearInterpolation
        if (matrix != null) {
            String prefix;
            if (type == FILLSTYLE.LINEAR_GRADIENT) {
                prefix = "LinearGradientBrush";
            } else {
                prefix = "RadialGradientBrush";
            }
            Element gradientTransformElement = document.createElement(prefix + ".Transform");
            Element matrixTransformElement = document.createElement("MatrixTransform");
            matrixTransformElement.setAttribute("Matrix", matrix.getXamlTransformationString(SWF.unitDivisor / zoom, 1 / zoom));
            gradientTransformElement.appendChild(matrixTransformElement);
            gradient.appendChild(gradientTransformElement);
        }
        for (int i = 0; i < gradientRecords.length; i++) {
            GRADRECORD record = gradientRecords[i];
            Element gradientEntry = document.createElement("GradientStop");
            gradientEntry.setAttribute("Offset", Double.toString(record.ratio / 255.0));
            RGB color = record.color;
            if (color instanceof RGBA) {
                RGBA colorA = (RGBA) color;
                gradientEntry.setAttribute("Color", colorA.toHexARGB());
            } else {
                gradientEntry.setAttribute("Color", color.toHexRGB());
            }
            gradient.appendChild(gradientEntry);
        }
    }

    public String getResultAsString() {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));

            return writer.toString();
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(XamlShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(XamlShapeExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    protected String formatDouble(double val) {
        String ret = "" + val;
        if (ret.endsWith(".0")) {
            ret = ret.substring(0, ret.length() - 2);
        }
        return ret;
    }
}

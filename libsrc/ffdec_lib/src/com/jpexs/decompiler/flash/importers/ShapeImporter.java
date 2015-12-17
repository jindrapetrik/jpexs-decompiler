/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.Point;
import com.jpexs.decompiler.flash.helpers.ImageHelper;
import com.jpexs.decompiler.flash.importers.svg.CubicToQuad;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG3Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsJPEG4Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLossless2Tag;
import com.jpexs.decompiler.flash.tags.DefineBitsLosslessTag;
import com.jpexs.decompiler.flash.tags.DefineShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineShapeTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.tags.enums.ImageFormat;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author JPEXS
 */
public class ShapeImporter {

    private boolean cubicWarning = false;

    public Tag importImage(ShapeTag st, byte[] newData) throws IOException {
        return importImage(st, newData, 0, true);
    }

    public Tag importImage(ShapeTag st, byte[] newData, int tagType, boolean fill) throws IOException {
        SWF swf = st.getSwf();

        if (newData[0] == 'B' && newData[1] == 'M') {
            BufferedImage b = ImageHelper.read(newData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageHelper.write(b, ImageFormat.PNG, baos);
            newData = baos.toByteArray();
        }

        if (tagType == 0) {
            if (ImageTag.getImageFormat(newData) == ImageFormat.JPEG) {
                tagType = DefineBitsJPEG2Tag.ID;
            } else {
                tagType = DefineBitsLossless2Tag.ID;
            }
        }

        ImageTag imageTag;
        switch (tagType) {
            case DefineBitsJPEG2Tag.ID: {
                imageTag = new DefineBitsJPEG2Tag(swf, null, swf.getNextCharacterId(), newData);
                break;
            }
            case DefineBitsJPEG3Tag.ID: {
                imageTag = new DefineBitsJPEG3Tag(swf, null, swf.getNextCharacterId(), newData);
                break;
            }
            case DefineBitsJPEG4Tag.ID: {
                imageTag = new DefineBitsJPEG4Tag(swf, null, swf.getNextCharacterId(), newData);
                break;
            }
            case DefineBitsLosslessTag.ID: {
                DefineBitsLosslessTag losslessTag = new DefineBitsLosslessTag(swf);
                losslessTag.setImage(newData);
                imageTag = losslessTag;
                break;
            }
            case DefineBitsLossless2Tag.ID: {
                DefineBitsLossless2Tag lossless2Tag = new DefineBitsLossless2Tag(swf);
                lossless2Tag.setImage(newData);
                imageTag = lossless2Tag;
                break;
            }
            default:
                throw new Error("Unsupported image type tag.");
        }

        int idx = swf.tags.indexOf(st);
        if (idx != -1) {
            swf.tags.add(idx, imageTag);
        } else {
            swf.tags.add(imageTag);
        }

        swf.updateCharacters();
        st.setModified(true);

        RECT rect = st.getRect();
        if (!fill) {
            Dimension dimension = imageTag.getImageDimension();
            rect.Xmax = rect.Xmin + (int) (SWF.unitDivisor * dimension.getWidth());
            rect.Ymax = rect.Ymin + (int) (SWF.unitDivisor * dimension.getHeight());
        }

        SHAPEWITHSTYLE shapes = imageTag.getShape(rect, fill);
        st.shapes = shapes;
        return (Tag) st;
    }

    public Tag importSvg(ShapeTag st, String svgXml) {
        return importSvg(st, svgXml, true);
    }

    public Tag importSvg(ShapeTag st, String svgXml, boolean fill) {
        SWF swf = st.getSwf();

        SHAPEWITHSTYLE shapes = new SHAPEWITHSTYLE();
        shapes.fillStyles = new FILLSTYLEARRAY();
        shapes.lineStyles = new LINESTYLEARRAY();
        shapes.fillStyles.fillStyles = new FILLSTYLE[0];
        shapes.lineStyles.lineStyles = new LINESTYLE[0];

        int shapeNum = st.getShapeNum();
        shapes.shapeRecords = new ArrayList<>();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(svgXml)));
            Element rootElement = doc.getDocumentElement();

            if (!"svg".equals(rootElement.getTagName())) {
                throw new IOException("SVG root element should be 'svg'");
            }

            SvgStyle style = new SvgStyle();
            style = style.apply(rootElement);
            Matrix transform = Matrix.getScaleInstance(SWF.unitDivisor);
            processSvgObject(shapeNum, shapes, rootElement, transform, style);
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(ShapeImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        shapes.shapeRecords.add(new EndShapeRecord());

        RECT rect = st.getRect();
        int origXmin = rect.Xmin;
        int origYmin = rect.Ymin;
        rect.Xmin -= origXmin;
        rect.Xmax -= origXmin;
        rect.Ymin -= origYmin;
        rect.Ymax -= origYmin;

        if (!fill) {
            // todo: how to calulate the real SVG size?
            RECT bounds = shapes.getBounds();
            rect.Xmax = rect.Xmin + bounds.Xmax - Math.min(0, bounds.Xmin);
            rect.Ymax = rect.Ymin + bounds.Ymax - Math.min(0, bounds.Ymin);
        }

        st.shapes = shapes;
        st.setModified(true);

        return (Tag) st;
    }

    private void processSvgObject(int shapeNum, SHAPEWITHSTYLE shapes, Element element, Matrix transform, SvgStyle style) {
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                String tagName = childElement.getTagName();
                SvgStyle newStyle = style.apply(childElement);
                if ("g".equals(tagName)) {
                    Matrix m = Matrix.parseSvgMatrix(childElement.getAttribute("transform"), SWF.unitDivisor, 1);
                    Matrix m2 = m == null ? transform : m.concatenate(transform);
                    processSvgObject(shapeNum, shapes, childElement, m2, newStyle);
                } else if ("path".equals(tagName)) {
                    processPath(shapeNum, shapes, childElement, transform, newStyle);
                }
            }
        }
    }

    private void processPath(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style) {
        String data = childElement.getAttribute("d");

        char command = 0;
        Point prevPoint = new Point(0, 0);
        Point startPoint = prevPoint;
        Point prevCControlPoint = null;
        double x0 = 0;
        double y0 = 0;

        boolean newShape = true;
        SvgPathReader pathReader = new SvgPathReader(data);
        while (pathReader.hasNext()) {
            char newCommand;
            if ((newCommand = pathReader.readCommand()) != 0) {
                command = newCommand;
            }

            boolean isRelative = Character.isLowerCase(command);

            double x = x0;
            double y = y0;

            Point p = null;
            char cmd = Character.toUpperCase(command);
            switch (cmd) {
                case 'M':
                    StyleChangeRecord scr;
                    if (newShape) {
                        newShape = false;
                        scr = getStyleChangeRecord(shapeNum, style);
                    } else {
                        scr = new StyleChangeRecord();
                    }

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    p = transform.transform(x, y);
                    scr.moveDeltaX = (int) Math.round(p.x);
                    scr.moveDeltaY = (int) Math.round(p.y);
                    prevPoint = p;
                    System.out.println("M" + scr.moveDeltaX + "," + scr.moveDeltaY);
                    scr.stateMoveTo = true;

                    shapes.shapeRecords.add(scr);
                    startPoint = p;
                    break;
                case 'Z':
                    StraightEdgeRecord serz = new StraightEdgeRecord();
                    p = startPoint;
                    serz.deltaX = (int) Math.round(p.x - prevPoint.x);
                    serz.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;
                    System.out.println("Z" + serz.deltaX + "," + serz.deltaY);
                    serz.generalLineFlag = true;
                    shapes.shapeRecords.add(serz);
                    break;
                case 'L':
                    StraightEdgeRecord serl = new StraightEdgeRecord();
                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    p = transform.transform(x, y);
                    serl.deltaX = (int) Math.round(p.x - prevPoint.x);
                    serl.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;
                    System.out.println("L" + serl.deltaX + "," + serl.deltaY);
                    serl.generalLineFlag = true;
                    shapes.shapeRecords.add(serl);
                    break;
                case 'H':
                    StraightEdgeRecord serh = new StraightEdgeRecord();
                    x = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                    }

                    p = transform.transform(x, y);
                    serh.deltaX = (int) Math.round(p.x - prevPoint.x);
                    prevPoint = p;
                    System.out.println("H" + serh.deltaX);
                    shapes.shapeRecords.add(serh);
                    break;
                case 'V':
                    StraightEdgeRecord serv = new StraightEdgeRecord();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        y += y0;
                    }

                    p = transform.transform(x, y);
                    serv.deltaY = (int) Math.round(p.x - prevPoint.x);
                    prevPoint = p;
                    System.out.println("V" + serv.deltaX);
                    serv.vertLineFlag = true;
                    shapes.shapeRecords.add(serv);
                    break;
                case 'Q':
                    CurvedEdgeRecord cer = new CurvedEdgeRecord();
                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    p = transform.transform(x, y);
                    cer.controlDeltaX = (int) Math.round(p.x - prevPoint.x);
                    cer.controlDeltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    p = transform.transform(x, y);
                    cer.anchorDeltaX = (int) Math.round(p.x - prevPoint.x);
                    cer.anchorDeltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;
                    System.out.println("Q" + cer.controlDeltaX + "," + cer.controlDeltaY + "," + cer.anchorDeltaX + "," + cer.controlDeltaY);
                    shapes.shapeRecords.add(cer);
                    break;
                case 'C':
                case 'S':
                    if (!cubicWarning) {
                        cubicWarning = true;
                        Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, "Cubic curves are not supported by Flash.");
                    }

                    // create at least something...
                    Point pStart = prevPoint;
                    Point pControl1;

                    if (cmd == 'C') {
                        x = pathReader.readDouble();
                        y = pathReader.readDouble();
                        if (isRelative) {
                            x += x0;
                            y += y0;
                        }

                        pControl1 = transform.transform(x, y);
                    } else {
                        if (prevCControlPoint != null) {
                            pControl1 = new Point(2 * pStart.x - prevCControlPoint.x, 2 * pStart.y - prevCControlPoint.y);
                        } else {
                            pControl1 = pStart;
                        }
                    }

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    Point pControl2 = transform.transform(x, y);
                    prevCControlPoint = pControl2;

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    p = transform.transform(x, y);

                    //StraightEdgeRecord serc = new StraightEdgeRecord();
                    //serc.generalLineFlag = true;
                    //serc.deltaX = (int) Math.round(p.x - prevPoint.x);
                    //serc.deltaY = (int) Math.round(p.y - prevPoint.y);
                    //shapes.shapeRecords.add(serc);
                    List<Double> quadCoordinates = new CubicToQuad().cubicToQuad(pStart.x, pStart.y, pControl1.x, pControl1.y, pControl2.x, pControl2.y, p.x, p.y, 0.0006);
                    for (int i = 2; i < quadCoordinates.size();) {
                        CurvedEdgeRecord cerc = new CurvedEdgeRecord();
                        p = new Point(quadCoordinates.get(i++), quadCoordinates.get(i++));
                        cerc.controlDeltaX = (int) Math.round(p.x - prevPoint.x);
                        cerc.controlDeltaY = (int) Math.round(p.y - prevPoint.y);
                        prevPoint = p;

                        p = new Point(quadCoordinates.get(i++), quadCoordinates.get(i++));
                        cerc.anchorDeltaX = (int) Math.round(p.x - prevPoint.x);
                        cerc.anchorDeltaY = (int) Math.round(p.y - prevPoint.y);
                        prevPoint = p;
                        shapes.shapeRecords.add(cerc);
                    }

                    break;
                default:
                    Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, "Unknown command: {0}", command);
                    return;
            }

            if (cmd != 'C') {
                prevCControlPoint = null;
            }

            x0 = x;
            y0 = y;
        }
    }

    private StyleChangeRecord getStyleChangeRecord(int shapeNum, SvgStyle style) {
        StyleChangeRecord scr = new StyleChangeRecord();

        scr.stateNewStyles = true;
        scr.fillStyles = new FILLSTYLEARRAY();
        scr.stateFillStyle1 = true;
        scr.stateLineStyle = true;
        Color fillColor = style.getFillColorWithOpacity();
        if (fillColor != null) {
            scr.fillStyles.fillStyles = new FILLSTYLE[1];
            scr.fillStyles.fillStyles[0] = new FILLSTYLE();
            scr.fillStyles.fillStyles[0].color = shapeNum >= 3 ? new RGBA(fillColor) : new RGB(fillColor);
            scr.fillStyles.fillStyles[0].fillStyleType = FILLSTYLE.SOLID;
            scr.fillStyle1 = 1;
        } else {
            scr.fillStyles.fillStyles = new FILLSTYLE[0];
            scr.fillStyle1 = 0;
        }

        scr.lineStyles = new LINESTYLEARRAY();
        Color lineColor = style.getStrokeColorWithOpacity();
        if (lineColor != null) {
            scr.lineStyles.lineStyles = new LINESTYLE[1];
            scr.lineStyles.lineStyles[0] = shapeNum <= 3 ? new LINESTYLE() : new LINESTYLE2();
            scr.lineStyles.lineStyles[0].color = shapeNum >= 3 ? new RGBA(lineColor) : new RGB(lineColor);
            scr.lineStyles.lineStyles[0].width = (int) Math.round(style.strokeWidth * SWF.unitDivisor);
            scr.lineStyle = 1;
        } else {
            scr.lineStyles.lineStyles = new LINESTYLE[0];
            scr.lineStyle = 0;
        }

        return scr;
    }

    private Color parseColor(String rgbStr) {
        if (rgbStr == null) {
            return null;
        }

        // todo: honfika: named colors: http://www.w3.org/TR/SVG/types.html#ColorKeywords
        switch (rgbStr) {
            case "none":
                return new Color(0, true);
        }

        if (rgbStr.startsWith("#")) {
            String s = rgbStr.substring(1);
            if (s.length() == 3) {
                s = "" + s.charAt(0) + s.charAt(0) + s.charAt(1) + s.charAt(1) + s.charAt(2) + s.charAt(2);
            }

            int i = Integer.parseInt(s, 16);
            return new Color(i, false);
        }

        return null;
    }

    class SvgStyle {

        public Color fillColor;

        public double opacity;

        public double fillOpacity;

        public Color strokeColor;

        public double strokeWidth;

        public SvgStyle() {
            fillColor = Color.BLACK;
            fillOpacity = 1;
            strokeColor = null;
            strokeWidth = 1;
            opacity = 1;
        }

        public Color getFillColorWithOpacity() {
            if (fillColor == null) {
                return null;
            }

            int opacity = (int) Math.round(this.opacity * fillOpacity * 255);
            if (opacity == 255) {
                return fillColor;
            }

            return new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), opacity);
        }

        public Color getStrokeColorWithOpacity() {
            if (strokeColor == null) {
                return null;
            }

            int opacity = (int) Math.round(this.opacity * 255);
            if (opacity == 255) {
                return strokeColor;
            }

            return new Color(strokeColor.getRed(), strokeColor.getGreen(), strokeColor.getBlue(), opacity);
        }

        private SvgStyle apply(Element element) {
            SvgStyle result = new SvgStyle();
            result.fillColor = fillColor;
            result.fillOpacity = fillOpacity;
            result.strokeColor = strokeColor;
            result.strokeWidth = strokeWidth;

            String attr = element.getAttribute("fill");
            Color fillColor = parseColor(attr);
            if (fillColor != null) {
                result.fillColor = fillColor;
            }

            attr = element.getAttribute("fill-opacity");
            if (attr.length() > 0) {
                double opacity = Double.parseDouble(attr);
                result.fillOpacity = opacity;
            }

            attr = element.getAttribute("stroke");
            Color strokeColor = parseColor(attr);
            if (strokeColor != null) {
                result.strokeColor = strokeColor;
            }

            attr = element.getAttribute("stroke-width");
            if (attr.length() > 0) {
                double strokeWidth = Double.parseDouble(attr);
                result.strokeWidth = strokeWidth;
            }

            attr = element.getAttribute("opacity");
            if (attr.length() > 0) {
                double opacity = Double.parseDouble(attr);
                result.opacity = opacity;
            }

            return result;
        }
    }

    public static int getShapeTagType(String format) {
        int res = 0;
        switch (format) {
            case "shape":
                res = DefineShapeTag.ID;
                break;
            case "shape2":
                res = DefineShape2Tag.ID;
                break;
            case "shape3":
                res = DefineShape3Tag.ID;
                break;
            case "shape4":
                res = DefineShape4Tag.ID;
                break;
        }

        return res;
    }
}

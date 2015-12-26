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
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.Point;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
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
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
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
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author JPEXS
 */
public class ShapeImporter {

    private final Set<String> shownWarnings = new HashSet<>();

    private final SvgColor TRANSPARENT = new SvgColor(new Color(0, true));

    private final Random random = new Random();

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

    // Generate id-element map, because getElementById does not work in some cases (namespaces?)
    protected void populateIds(Element el, Map<String, Element> out) {
        if (el.hasAttribute("id")) {
            out.put(el.getAttribute("id"), el);
        }
        NodeList nodes = el.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i) instanceof Element) {
                populateIds((Element) nodes.item(i), out);
            }
        }
    }

    public Tag importSvg(ShapeTag st, String svgXml, boolean fill) {
        SHAPEWITHSTYLE shapes = new SHAPEWITHSTYLE();
        shapes.fillStyles = new FILLSTYLEARRAY();
        shapes.lineStyles = new LINESTYLEARRAY();
        shapes.fillStyles.fillStyles = new FILLSTYLE[0];
        shapes.lineStyles.lineStyles = new LINESTYLE[0];

        int shapeNum = st.getShapeNum();
        shapes.shapeRecords = new ArrayList<>();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            /*docFactory.setValidating(false);
             docFactory.setNamespaceAware(true);
             docFactory.setFeature("http://xml.org/sax/features/namespaces", false);
             docFactory.setFeature("http://xml.org/sax/features/validation", false);
             docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);*/
            docFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.parse(new InputSource(new StringReader(svgXml)));
            Element rootElement = doc.getDocumentElement();

            Map<String, Element> idMap = new HashMap<>();
            populateIds(rootElement, idMap);

            if (!"svg".equals(rootElement.getTagName())) {
                throw new IOException("SVG root element should be 'svg'");
            }

            SvgStyle style = new SvgStyle();
            style = style.apply(rootElement, idMap);
            Matrix transform = new Matrix();
            processSvgObject(idMap, shapeNum, shapes, rootElement, transform, style);
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

    private void processSvgObject(Map<String, Element> idMap, int shapeNum, SHAPEWITHSTYLE shapes, Element element, Matrix transform, SvgStyle style) {
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                String tagName = childElement.getTagName();
                SvgStyle newStyle = style.apply(childElement, idMap);
                Matrix m = Matrix.parseSvgMatrix(childElement.getAttribute("transform"), 1, 1);
                Matrix m2 = m == null ? transform : transform.concatenate(m);
                if ("g".equals(tagName)) {
                    processSvgObject(idMap, shapeNum, shapes, childElement, m2, newStyle);
                } else if ("path".equals(tagName)) {
                    processPath(shapeNum, shapes, childElement, m2, newStyle);
                } else if ("circle".equals(tagName)) {
                    processCircle(shapeNum, shapes, childElement, m2, newStyle);
                } else if ("ellipse".equals(tagName)) {
                    processEllipse(shapeNum, shapes, childElement, m2, newStyle);
                } else if ("rect".equals(tagName)) {
                    processRect(shapeNum, shapes, childElement, m2, newStyle);
                } else if ("line".equals(tagName)) {
                    processLine(shapeNum, shapes, childElement, m2, newStyle);
                } else if ("polyline".equals(tagName)) {
                    processPolyline(shapeNum, shapes, childElement, m2, newStyle);
                } else if ("polygon".equals(tagName)) {
                    processPolygon(shapeNum, shapes, childElement, m2, newStyle);
                } else if ("defs".equals(tagName) || "title".equals(tagName) || "desc".equals(tagName)
                        || "radialGradient".equals(tagName) || "linearGradient".equals(tagName)) {
                    // ignore
                } else {
                    showWarning(tagName + "tagNotSupported", "The SVG tag '" + tagName + "' is not supported.");
                }
            }
        }
    }

    private void processCommands(int shapeNum, SHAPEWITHSTYLE shapes, List<PathCommand> commands, Matrix transform, SvgStyle style) {
        Matrix transform2 = transform.preConcatenate(Matrix.getScaleInstance(SWF.unitDivisor));
        Point prevPoint = new Point(0, 0);
        Point startPoint = prevPoint;
        double x0 = 0;
        double y0 = 0;

        StyleChangeRecord scrStyle = getStyleChangeRecord(shapeNum, style);
        int fillStyle = scrStyle.fillStyle1;
        int lineStyle = scrStyle.lineStyle;
        scrStyle.stateFillStyle0 = true;
        scrStyle.stateFillStyle1 = true;
        scrStyle.stateLineStyle = true;
        scrStyle.fillStyle0 = 0;
        scrStyle.fillStyle1 = 0;
        scrStyle.lineStyle = 0;
        shapes.shapeRecords.add(scrStyle);

        for (PathCommand command : commands) {

            double x = x0;
            double y = y0;
            Point p;
            char cmd = Character.toUpperCase(command.command);
            switch (cmd) {
                case 'M':
                    StyleChangeRecord scr = new StyleChangeRecord();
                    if (fillStyle != 0) {
                        scr.stateFillStyle1 = true;
                        scr.fillStyle1 = fillStyle;
                    }
                    if (lineStyle != 0) {
                        scr.lineStyle = lineStyle;
                        scr.stateLineStyle = true;
                    }

                    x = command.params[0];
                    y = command.params[1];

                    p = transform2.transform(x, y);
                    scr.moveDeltaX = (int) Math.round(p.x);
                    scr.moveDeltaY = (int) Math.round(p.y);
                    prevPoint = p;
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
                    serz.generalLineFlag = true;
                    shapes.shapeRecords.add(serz);
                    break;
                case 'L':
                    StraightEdgeRecord serl = new StraightEdgeRecord();
                    x = command.params[0];
                    y = command.params[1];

                    p = transform2.transform(x, y);
                    serl.deltaX = (int) Math.round(p.x - prevPoint.x);
                    serl.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;
                    serl.generalLineFlag = true;
                    serl.simplify();
                    shapes.shapeRecords.add(serl);
                    break;
                case 'H':
                    StraightEdgeRecord serh = new StraightEdgeRecord();
                    x = command.params[0];

                    p = transform2.transform(x, y);
                    serh.deltaX = (int) Math.round(p.x - prevPoint.x);
                    prevPoint = p;
                    shapes.shapeRecords.add(serh);
                    break;
                case 'V':
                    StraightEdgeRecord serv = new StraightEdgeRecord();
                    y = command.params[0];

                    p = transform2.transform(x, y);
                    serv.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;
                    serv.vertLineFlag = true;
                    shapes.shapeRecords.add(serv);
                    break;
                case 'Q':
                    CurvedEdgeRecord cer = new CurvedEdgeRecord();
                    x = command.params[0];
                    y = command.params[1];

                    p = transform2.transform(x, y);
                    cer.controlDeltaX = (int) Math.round(p.x - prevPoint.x);
                    cer.controlDeltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;

                    x = command.params[2];
                    y = command.params[3];

                    p = transform2.transform(x, y);
                    cer.anchorDeltaX = (int) Math.round(p.x - prevPoint.x);
                    cer.anchorDeltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;
                    shapes.shapeRecords.add(cer);
                    break;
                case 'C':
                    showWarning("cubicCurvesNotSupported", "Cubic curves are not supported by Flash.");

                    // create at least something...
                    Point pStart = prevPoint;
                    Point pControl1;

                    x = command.params[0];
                    y = command.params[1];

                    pControl1 = transform2.transform(x, y);

                    x = command.params[2];
                    y = command.params[3];

                    Point pControl2 = transform2.transform(x, y);

                    x = command.params[4];
                    y = command.params[5];

                    p = transform2.transform(x, y);

                    //StraightEdgeRecord serc = new StraightEdgeRecord();
                    //serc.generalLineFlag = true;
                    //serc.deltaX = (int) Math.round(p.x - prevPoint.x);
                    //serc.deltaY = (int) Math.round(p.y - prevPoint.y);
                    //shapes.shapeRecords.add(serc);
                    List<Double> quadCoordinates = new CubicToQuad().cubicToQuad(pStart.x, pStart.y, pControl1.x, pControl1.y, pControl2.x, pControl2.y, p.x, p.y, 1);
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

            x0 = x;
            y0 = y;
        }
        applyStyleGradients(shapes.getBounds(), scrStyle, transform2, shapeNum, style);
    }

    private void processPath(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style) {
        String data = childElement.getAttribute("d");

        char command = 0;
        Point startPoint = new Point(0, 0);
        Point prevCControlPoint = null;
        Point prevQControlPoint = null;
        double x0 = 0;
        double y0 = 0;

        List<PathCommand> pathCommands = new ArrayList<>();
        SvgPathReader pathReader = new SvgPathReader(data);
        while (pathReader.hasNext()) {
            char newCommand;
            if ((newCommand = pathReader.readCommand()) != 0) {
                command = newCommand;
            }

            boolean isRelative = Character.isLowerCase(command);

            double x = x0;
            double y = y0;

            char cmd = Character.toUpperCase(command);
            switch (cmd) {
                case 'M':
                    PathCommand scr = new PathCommand();
                    scr.command = 'M';

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    scr.params = new double[]{x, y};

                    pathCommands.add(scr);
                    startPoint = new Point(x, y);

                    command = isRelative ? 'l' : 'L';
                    break;
                case 'Z':
                    PathCommand serz = new PathCommand();
                    serz.command = 'Z';
                    x = startPoint.x;
                    y = startPoint.y;
                    serz.params = new double[]{x, y};
                    pathCommands.add(serz);
                    break;
                case 'L':
                    PathCommand serl = new PathCommand();
                    serl.command = 'L';
                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    serl.params = new double[]{x, y};
                    pathCommands.add(serl);
                    break;
                case 'H':
                    PathCommand serh = new PathCommand();
                    serh.command = 'H';
                    x = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                    }

                    serh.params = new double[]{x};
                    pathCommands.add(serh);
                    break;
                case 'V':
                    PathCommand serv = new PathCommand();
                    serv.command = 'V';
                    y = pathReader.readDouble();
                    if (isRelative) {
                        y += y0;
                    }

                    serv.params = new double[]{y};
                    pathCommands.add(serv);
                    break;
                case 'Q':
                case 'T':
                    PathCommand cer = new PathCommand();
                    cer.command = 'Q';

                    Point pControl;
                    if (cmd == 'Q') {
                        x = pathReader.readDouble();
                        y = pathReader.readDouble();
                        if (isRelative) {
                            x += x0;
                            y += y0;
                        }

                        pControl = new Point(x, y);
                    } else if (prevQControlPoint != null) {
                        pControl = new Point(2 * x0 - prevQControlPoint.x, 2 * y0 - prevQControlPoint.y);
                    } else {
                        pControl = new Point(x0, y0);
                    }

                    prevQControlPoint = pControl;
                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    cer.params = new double[]{pControl.x, pControl.y, x, y};
                    pathCommands.add(cer);
                    break;
                case 'C':
                case 'S':
                    showWarning("cubicCurvesNotSupported", "Cubic curves are not supported by Flash.");

                    // create at least something...
                    Point pControl1;
                    if (cmd == 'C') {
                        x = pathReader.readDouble();
                        y = pathReader.readDouble();
                        if (isRelative) {
                            x += x0;
                            y += y0;
                        }

                        pControl1 = new Point(x, y);
                    } else if (prevCControlPoint != null) {
                        pControl1 = new Point(2 * x0 - prevCControlPoint.x, 2 * y0 - prevCControlPoint.y);
                    } else {
                        pControl1 = new Point(x0, y0);
                    }

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    Point pControl2 = new Point(x, y);
                    prevCControlPoint = pControl2;

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    PathCommand cerc = new PathCommand();
                    cerc.command = 'C';
                    cerc.params = new double[]{pControl1.x, pControl1.y, pControl2.x, pControl2.y, x, y};
                    pathCommands.add(cerc);
                    break;
                case 'A':
                    double rx = pathReader.readDouble();
                    double ry = pathReader.readDouble();
                    double fi = pathReader.readDouble() * Math.PI / 180;
                    boolean largeFlag = (int) pathReader.readDouble() != 0;
                    boolean sweepFlag = (int) pathReader.readDouble() != 0;

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    if (rx == 0 || ry == 0) {
                        // straight line to (x, y)
                        PathCommand sera = new PathCommand();
                        sera.command = 'L';
                        sera.params = new double[]{x, y};
                        pathCommands.add(sera);
                    } else {
                        rx = Math.abs(rx);
                        ry = Math.abs(ry);

                        double x1 = x0;
                        double y1 = y0;
                        double x2 = x;
                        double y2 = y;

                        double d1 = (x1 - x2) / 2;
                        double d2 = (y1 - y2) / 2;
                        double x1Comma = Math.cos(fi) * d1 + Math.sin(fi) * d2;
                        double y1Comma = -Math.sin(fi) * d1 + Math.cos(fi) * d2;

                        // Correction of out-of-range radii
                        double lambda = x1Comma * x1Comma / (rx * rx) + y1Comma * y1Comma / (ry * ry);
                        if (lambda > 1) {
                            double sqrtLambda = Math.sqrt(lambda);
                            rx = sqrtLambda * rx;
                            ry = sqrtLambda * ry;
                        }

                        double c = Math.sqrt((rx * rx * ry * ry - rx * rx * y1Comma * y1Comma - ry * ry * x1Comma * x1Comma) / (rx * rx * y1Comma * y1Comma + ry * ry * x1Comma * x1Comma));
                        double cxComma = c * rx * y1Comma / ry;
                        double cyComma = c * -ry * x1Comma / rx;

                        if (largeFlag == sweepFlag) {
                            cxComma = -cxComma;
                            cyComma = -cyComma;
                        }

                        double cx = Math.cos(fi) * cxComma - Math.sin(fi) * cyComma + (x1 + x2) / 2;
                        double cy = Math.sin(fi) * cxComma + Math.cos(fi) * cyComma + (y1 + y2) / 2;

                        double px1 = (x1Comma - cxComma) / rx;
                        double py1 = (y1Comma - cyComma) / ry;
                        double theta1 = calcAngle(1, 0, px1, py1);

                        double px2 = (-x1Comma - cxComma) / rx;
                        double py2 = (-y1Comma - cyComma) / ry;
                        double deltaTheta = calcAngle(px1, py1, px2, py2);
                        if (sweepFlag) {
                            if (deltaTheta < 0) {
                                deltaTheta += 2 * Math.PI;
                            }
                        } else if (deltaTheta > 0) {
                            deltaTheta -= 2 * Math.PI;
                        }

                        double rcp = Math.sqrt(4 - 2 * Math.sqrt(2));
                        double delta = Math.signum(deltaTheta) * Math.PI / 4;

                        int segmentCount = (int) Math.ceil(deltaTheta / delta);
                        double theta = theta1;

                        PathCommand sera;
                        for (int i = 0; i < segmentCount - 1; i++) {
                            theta += delta;
                            /*sera = new PathCommand();
                             sera.command = 'L';
                             double x12 = Math.cos(theta) * rx;
                             double y12 = Math.sin(theta) * ry;
                             x1Comma = Math.cos(fi) * x12 - Math.sin(fi) * y12;
                             y1Comma = Math.sin(fi) * x12 + Math.cos(fi) * y12;
                             sera.params = new double[]{cx + x1Comma, cy + y1Comma};
                             pathCommands.add(sera);*/

                            sera = new PathCommand();
                            sera.command = 'Q';
                            double x12 = Math.cos(theta) * rx;
                            double y12 = Math.sin(theta) * ry;
                            x1Comma = Math.cos(fi) * x12 - Math.sin(fi) * y12;
                            y1Comma = Math.sin(fi) * x12 + Math.cos(fi) * y12;

                            double theta2 = theta - delta / 2;
                            x12 = Math.cos(theta2) * rx * rcp;
                            y12 = Math.sin(theta2) * ry * rcp;
                            double x1Comma2 = Math.cos(fi) * x12 - Math.sin(fi) * y12;
                            double y1Comma2 = Math.sin(fi) * x12 + Math.cos(fi) * y12;
                            sera.params = new double[]{cx + x1Comma2, cy + y1Comma2, cx + x1Comma, cy + y1Comma};
                            pathCommands.add(sera);
                        }

                        sera = new PathCommand();
                        sera.command = 'Q';

                        theta += delta;
                        double diff = theta1 + deltaTheta - theta;
                        diff = -delta - diff;
                        theta = theta - delta - diff / 2;

                        double rcpm = 1 + (rcp - 1) * (diff / delta) * (diff / delta);
                        double x12 = Math.cos(theta) * rx * rcpm;
                        double y12 = Math.sin(theta) * ry * rcpm;
                        x1Comma = Math.cos(fi) * x12 - Math.sin(fi) * y12;
                        y1Comma = Math.sin(fi) * x12 + Math.cos(fi) * y12;
                        sera.params = new double[]{cx + x1Comma, cy + y1Comma, x, y};
                        pathCommands.add(sera);
                        /*sera = new PathCommand();
                         sera.command = 'L';
                         sera.params = new double[]{x, y};
                         pathCommands.add(sera);*/
                    }
                    break;
                default:
                    Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, "Unknown command: {0}", command);
                    return;
            }

            if (cmd != 'C' && cmd != 'S') {
                prevCControlPoint = null;
            }

            if (cmd != 'Q' && cmd != 'T') {
                prevQControlPoint = null;
            }

            x0 = x;
            y0 = y;
        }

        processCommands(shapeNum, shapes, pathCommands, transform, style);
    }

    private double calcAngle(double ux, double uy, double vx, double vy) {
        double lu = Math.sqrt(ux * ux + uy * uy);
        double lv = Math.sqrt(ux * ux + uy * uy);
        double sign = Math.signum(ux * vy - uy * vx);
        if (sign == 0) {
            sign = 1;
        }

        return sign * Math.acos(ux * vx + uy * vy / (lu * lv));
    }

    private void processCircle(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style) {
        String attr = childElement.getAttribute("cx");
        double cx = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("cy");
        double cy = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("r");
        double r = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        processEllipse(shapeNum, shapes, transform, style, cx, cy, r, r);
    }

    private void processEllipse(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style) {
        String attr = childElement.getAttribute("cx");
        double cx = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("cy");
        double cy = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("rx");
        double rx = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("ry");
        double ry = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        processEllipse(shapeNum, shapes, transform, style, cx, cy, rx, ry);
    }

    private void processEllipse(int shapeNum, SHAPEWITHSTYLE shapes, Matrix transform, SvgStyle style, double cx, double cy, double rx, double ry) {
        double sqrt2RXHalf = Math.sqrt(2) * rx / 2;
        double sqrt2Minus1RX = (Math.sqrt(2) - 1) * rx;
        double sqrt2RYHalf = Math.sqrt(2) * ry / 2;
        double sqrt2Minus1RY = (Math.sqrt(2) - 1) * ry;

        List<PathCommand> pathCommands = new ArrayList<>();
        PathCommand scr = new PathCommand();
        scr.command = 'M';
        scr.params = new double[]{cx + rx, cy};
        pathCommands.add(scr);

        double[] points = new double[]{
            rx, -sqrt2Minus1RY,
            sqrt2RXHalf, -sqrt2RYHalf,
            sqrt2Minus1RX, -ry,
            0, -ry,
            -sqrt2Minus1RX, -ry,
            -sqrt2RXHalf, -sqrt2RYHalf,
            -rx, -sqrt2Minus1RY,
            -rx, 0,
            -rx, sqrt2Minus1RY,
            -sqrt2RXHalf, sqrt2RYHalf,
            -sqrt2Minus1RX, ry,
            0, ry,
            sqrt2Minus1RX, ry,
            sqrt2RXHalf, sqrt2RYHalf,
            rx, sqrt2Minus1RY,
            rx, 0};

        for (int i = 0; i < points.length; i += 4) {
            PathCommand cer = new PathCommand();
            cer.command = 'Q';
            cer.params = new double[]{cx + points[i], cy + points[i + 1], cx + points[i + 2], cy + points[i + 3]};

            /*double tetha = 30;
             tetha *= Math.PI / 180;
             double x1 = points[i];
             double y1 = points[i + 1];
             double x2 = points[i + 2];
             double y2 = points[i + 3];

             double x1Comma = Math.cos(tetha) * x1 + Math.sin(tetha) * y1;
             double y1Comma = -Math.sin(tetha) * x1 + Math.cos(tetha) * y1;
             double x2Comma = Math.cos(tetha) * x2 + Math.sin(tetha) * y2;
             double y2Comma = -Math.sin(tetha) * x2 + Math.cos(tetha) * y2;

             cer.params = new double[]{cx + x1Comma, cy + y1Comma, cx + x2Comma, cy + y2Comma};*/
            pathCommands.add(cer);
        }

        processCommands(shapeNum, shapes, pathCommands, transform, style);
    }

    private void processRect(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style) {
        String attr = childElement.getAttribute("x");
        double x = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("y");
        double y = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("width");
        double width = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("height");
        double height = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("rx");
        double rx = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("ry");
        double ry = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        if (rx == 0 && ry != 0) {
            rx = ry;
        } else if (rx != 0 && ry == 0) {
            ry = rx;
        }

        if (rx > width / 2) {
            rx = width / 2;
        }

        if (ry > height / 2) {
            ry = height / 2;
        }

        List<PathCommand> pathCommands = new ArrayList<>();

        if (rx > 0 || ry > 0) {
            PathCommand scr = new PathCommand();
            scr.command = 'M';
            scr.params = new double[]{x + width, y + ry};
            pathCommands.add(scr);

            double sqrt2RXHalf = Math.sqrt(2) * rx / 2;
            double sqrt2Minus1RX = (Math.sqrt(2) - 1) * rx;
            double sqrt2RYHalf = Math.sqrt(2) * ry / 2;
            double sqrt2Minus1RY = (Math.sqrt(2) - 1) * ry;

            double[] points = new double[]{
                x + width, y + ry - sqrt2Minus1RY,
                x + width - rx + sqrt2RXHalf, y + ry - sqrt2RYHalf,
                x + width - rx + sqrt2Minus1RX, y,
                x + width - rx, y,
                x + rx, y,
                x + rx - sqrt2Minus1RX, y,
                x + rx - sqrt2RXHalf, y + ry - sqrt2RYHalf,
                x, y + ry - sqrt2Minus1RY,
                x, y + ry,
                x, y + height - ry,
                x, y + height - ry + sqrt2Minus1RY,
                x + rx - sqrt2RXHalf, y + height - ry + sqrt2RYHalf,
                x + rx - sqrt2Minus1RX, y + height,
                x + rx, y + height,
                x + width - rx, y + height,
                x + width - rx + sqrt2Minus1RX, y + height,
                x + width - rx + sqrt2RXHalf, y + height - ry + sqrt2RYHalf,
                x + width, y + height - ry + sqrt2Minus1RY,
                x + width, y + height - ry,
                x + width, y + ry};

            for (int i = 0; i < points.length;) {
                if (i % 10 == 8) {
                    PathCommand cer = new PathCommand();
                    cer.command = 'L';
                    cer.params = new double[]{points[i], points[i + 1]};
                    pathCommands.add(cer);
                    i += 2;
                } else {
                    PathCommand cer = new PathCommand();
                    cer.command = 'Q';
                    cer.params = new double[]{points[i], points[i + 1], points[i + 2], points[i + 3]};
                    pathCommands.add(cer);
                    i += 4;
                }
            }
        } else {
            PathCommand scr = new PathCommand();
            scr.command = 'M';
            scr.params = new double[]{x, y};
            pathCommands.add(scr);

            double[] points = new double[]{
                x + width, y,
                x + width, y + height,
                x, y + height,
                x, y};

            for (int i = 0; i < points.length; i += 2) {
                PathCommand cer = new PathCommand();
                cer.command = 'L';
                cer.params = new double[]{points[i], points[i + 1]};

                pathCommands.add(cer);
            }
        }

        processCommands(shapeNum, shapes, pathCommands, transform, style);
    }

    private void processLine(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style) {
        String attr = childElement.getAttribute("x1");
        double x1 = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("y1");
        double y1 = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("x2");
        double x2 = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("y2");
        double y2 = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        List<PathCommand> pathCommands = new ArrayList<>();
        PathCommand scr = new PathCommand();
        scr.command = 'M';
        scr.params = new double[]{x1, y1};
        pathCommands.add(scr);

        PathCommand cer = new PathCommand();
        cer.command = 'L';
        cer.params = new double[]{x2, y2};

        pathCommands.add(cer);

        processCommands(shapeNum, shapes, pathCommands, transform, style);
    }

    private void processPolyline(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style) {
        processPolyline(shapeNum, shapes, childElement, transform, style, false);
    }

    private void processPolygon(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style) {
        processPolyline(shapeNum, shapes, childElement, transform, style, true);
    }

    private void processPolyline(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style, boolean close) {
        String data = childElement.getAttribute("points");

        char command = 'M';
        Point startPoint = new Point(0, 0);
        double x0 = 0;
        double y0 = 0;

        List<PathCommand> pathCommands = new ArrayList<>();
        SvgPathReader pathReader = new SvgPathReader(data);
        while (pathReader.hasNext()) {
            double x = x0;
            double y = y0;

            Point p = null;
            switch (command) {
                case 'M':
                    PathCommand scr = new PathCommand();
                    scr.command = 'M';

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    scr.params = new double[]{x, y};

                    pathCommands.add(scr);
                    startPoint = new Point(x, y);
                    break;
                case 'L':
                    PathCommand serl = new PathCommand();
                    serl.command = 'L';
                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    serl.params = new double[]{x, y};
                    pathCommands.add(serl);
                    break;
            }

            x0 = x;
            y0 = y;
            command = 'L';
        }

        if (close) {
            PathCommand serz = new PathCommand();
            serz.command = 'Z';
            serz.params = new double[]{startPoint.x, startPoint.y};
            pathCommands.add(serz);
        }

        processCommands(shapeNum, shapes, pathCommands, transform, style);
    }

    //Stub for w3 test. TODO: refactor and move to test directory. It's here because of easy access - compiling single file
    private static void svgTest(String name) throws IOException, InterruptedException {
        URL svgUrl = new URL("http://www.w3.org/Graphics/SVG/Test/20061213/svggen/" + name + ".svg");
        byte[] svgData = Helper.readStream(svgUrl.openStream());
        Helper.writeFile(name + ".original.svg", svgData);

        URL pngUrl = new URL("http://www.w3.org/Graphics/SVG/Test/20061213/png/full-" + name + ".png");
        byte[] pngData = Helper.readStream(pngUrl.openStream());
        Helper.writeFile(name + ".original.png", pngData);

        String svgDataS = new String(svgData);
        //String svgDataS = Helper.readTextFile(name + ".original.svg");

        SWF swf = new SWF();
        DefineShape4Tag st = new DefineShape4Tag(swf);
        st = (DefineShape4Tag) (new ShapeImporter().importSvg(st, svgDataS));
        swf.tags.add(st);
        SerializableImage si = new SerializableImage(800, 600, BufferedImage.TYPE_4BYTE_ABGR);
        BitmapExporter.export(swf, st.shapes, Color.yellow, si, new Matrix(), new ColorTransform());
        List<Tag> li = new ArrayList<>();
        li.add(st);
        ImageIO.write(si.getBufferedImage(), "PNG", new File(name + ".imported.png"));
        new ShapeExporter().exportShapes(null, "./outex/", li, new ShapeExportSettings(ShapeExportMode.SVG, 1), null);
    }

    //Test for SVG
    public static void main(String[] args) throws IOException, InterruptedException {
        //svgTest("pservers-grad-01-b");
        svgTest("pservers-grad-04-b");
    }

    private void applyStyleGradients(RECT bounds, StyleChangeRecord scr, Matrix transform, int shapeNum, SvgStyle style) {
        SvgFill fill = style.getFillWithOpacity();
        if (fill != null) {
            if (fill instanceof SvgGradient) {
                FILLSTYLE fillStyle = scr.fillStyles.fillStyles[0];
                SvgGradient gfill = (SvgGradient) fill;
                Matrix gradientMatrix = Matrix.parseSvgMatrix(gfill.gradientTransform, SWF.unitDivisor, 1);
                gradientMatrix = transform.concatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor)).concatenate(gradientMatrix);
                fillStyle.gradientMatrix = gradientMatrix.toMATRIX();
                if (fill instanceof SvgLinearGradient) {
                    SvgLinearGradient lgfill = (SvgLinearGradient) fill;
                    fillStyle.fillStyleType = FILLSTYLE.LINEAR_GRADIENT;
                    fillStyle.gradient = new GRADIENT();
                    double x1;
                    if (lgfill.x1.endsWith("%")) {
                        x1 = Double.parseDouble(lgfill.x1.substring(0, lgfill.x1.length() - 1)) / 100;
                    } else {
                        x1 = Double.parseDouble(lgfill.x1);
                    }
                    //x1 = x1 - (-819.2);

                    double y1;
                    if (lgfill.y1.endsWith("%")) {
                        y1 = Double.parseDouble(lgfill.y1.substring(0, lgfill.y1.length() - 1)) / 100;
                    } else {
                        y1 = Double.parseDouble(lgfill.y1);
                    }
                    double x2;
                    if (lgfill.x2.endsWith("%")) {
                        x2 = Double.parseDouble(lgfill.x2.substring(0, lgfill.x2.length() - 1)) / 100;
                    } else {
                        x2 = Double.parseDouble(lgfill.x2);
                    }
                    //x2 = x2 - 819.2;
                    double y2;
                    if (lgfill.y2.endsWith("%")) {
                        y2 = Double.parseDouble(lgfill.y2.substring(0, lgfill.y2.length() - 1)) / 100;
                    } else {
                        y2 = Double.parseDouble(lgfill.y2);
                    }
                    if (lgfill.gradientUnits == SvgGradientUnits.OBJECT_BOUNDING_BOX) {
                        x1 = bounds.Xmin + bounds.getWidth() * x1;
                        x2 = bounds.Xmin + bounds.getWidth() * x2;
                        y1 = bounds.Ymin + bounds.getHeight() * y1;
                        y2 = bounds.Ymin + bounds.getHeight() * y2;
                    } else {
                        x1 = x1 * SWF.unitDivisor;
                        y1 = y1 * SWF.unitDivisor;
                        x2 = x2 * SWF.unitDivisor;
                        y2 = y2 * SWF.unitDivisor;
                    }

                    //FIXME: make following transformations correct for "pservers-grad-04-b"
                    Matrix xyMatrix = new Matrix();
                    xyMatrix.scaleX = x2 - x1;
                    xyMatrix.rotateSkew0 = y2 - y1;
                    xyMatrix.rotateSkew1 = -xyMatrix.rotateSkew0;
                    xyMatrix.scaleY = xyMatrix.scaleX;

                    Matrix zeroStartMatrix = Matrix.getTranslateInstance(0.5, 0);

                    Matrix scaleMatrix = Matrix.getScaleInstance(1 / 16384.0 / 2);
                    Matrix transMatrix = Matrix.getTranslateInstance(x1, y1);

                    Matrix tMatrix = new Matrix();
                    tMatrix = tMatrix.preConcatenate(scaleMatrix);
                    tMatrix = tMatrix.preConcatenate(zeroStartMatrix);
                    tMatrix = tMatrix.preConcatenate(xyMatrix);

                    tMatrix = tMatrix.preConcatenate(transMatrix);
                    Point p1 = tMatrix.transform(new Point(-16384, 0));
                    Point p2 = tMatrix.transform(new Point(16384, 0));

                    tMatrix = tMatrix.preConcatenate(new Matrix(fillStyle.gradientMatrix));
                    fillStyle.gradientMatrix = tMatrix.toMATRIX();
                } else if (fill instanceof SvgRadialGradient) {
                    SvgRadialGradient rgfill = (SvgRadialGradient) fill;
                    double cx;
                    if (rgfill.cx.endsWith("%")) {
                        cx = Double.parseDouble(rgfill.cx.substring(0, rgfill.cx.length() - 1)) / 100;
                    } else {
                        cx = Double.parseDouble(rgfill.cx);
                    }
                    double cy;
                    if (rgfill.cy.endsWith("%")) {
                        cy = Double.parseDouble(rgfill.cy.substring(0, rgfill.cy.length() - 1)) / 100;
                    } else {
                        cy = Double.parseDouble(rgfill.cy);
                    }

                    double r;
                    if (rgfill.r.endsWith("%")) {
                        r = Double.parseDouble(rgfill.r.substring(0, rgfill.r.length() - 1)) / 100;
                    } else {
                        r = Double.parseDouble(rgfill.r);
                    }

                    //TODO: apply cx,cy,r to matrix
                    fillStyle.gradientMatrix = Matrix.getTranslateInstance(SWF.unitDivisor * cx, SWF.unitDivisor * cy).concatenate(Matrix.getScaleInstance(r / 819.2)).concatenate(new Matrix(fillStyle.gradientMatrix)).toMATRIX();

                    double fx;
                    if (rgfill.fx.endsWith("%")) {
                        fx = Double.parseDouble(rgfill.fx.substring(0, rgfill.fx.length() - 1)) / 100;
                    } else {
                        fx = Double.parseDouble(rgfill.fx);
                    }
                    double fy;
                    if (rgfill.fy.endsWith("%")) {
                        fy = Double.parseDouble(rgfill.fy.substring(0, rgfill.fy.length() - 1)) / 100;
                    } else {
                        fy = Double.parseDouble(rgfill.fy);
                    }
                    if (!rgfill.fx.equals(rgfill.cx) || !rgfill.fy.equals(rgfill.cy)) {
                        fillStyle.fillStyleType = FILLSTYLE.FOCAL_RADIAL_GRADIENT;
                        fillStyle.gradient = new FOCALGRADIENT();
                        FOCALGRADIENT fg = (FOCALGRADIENT) fillStyle.gradient;
                        double f = Math.sqrt((fx - cx) * (fx - cx) + (fy - cy) * (fy - cy)) / 819.2;
                        fg.focalPoint = (float) f;
                    } else {
                        fillStyle.fillStyleType = FILLSTYLE.RADIAL_GRADIENT;
                        fillStyle.gradient = new GRADIENT();
                    }
                }
                switch (gfill.spreadMethod) {
                    case PAD:
                        fillStyle.gradient.spreadMode = GRADIENT.SPREAD_PAD_MODE;
                        break;
                    case REFLECT:
                        fillStyle.gradient.spreadMode = GRADIENT.SPREAD_REFLECT_MODE;
                        break;
                    case REPEAT:
                        fillStyle.gradient.spreadMode = GRADIENT.SPREAD_REPEAT_MODE;
                        break;
                }
                switch (gfill.interpolation) {
                    case LINEAR_RGB:
                        fillStyle.gradient.interpolationMode = GRADIENT.INTERPOLATION_LINEAR_RGB_MODE;
                        break;
                    case SRGB:
                        fillStyle.gradient.interpolationMode = GRADIENT.INTERPOLATION_RGB_MODE;
                        break;
                }

                fillStyle.gradient.gradientRecords = new GRADRECORD[gfill.stops.size()];
                for (int i = 0; i < gfill.stops.size(); i++) {
                    SvgStop stop = gfill.stops.get(i);
                    Color color = stop.color;
                    color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) Math.round(color.getAlpha() * style.opacity));
                    fillStyle.gradient.gradientRecords[i] = new GRADRECORD();
                    fillStyle.gradient.gradientRecords[i].inShape3 = shapeNum >= 3;
                    fillStyle.gradient.gradientRecords[i].color = shapeNum >= 3 ? new RGBA(color) : new RGB(color);
                    fillStyle.gradient.gradientRecords[i].ratio = (int) Math.round(stop.offset * 255);
                }
            }
        }
    }

    private StyleChangeRecord getStyleChangeRecord(int shapeNum, SvgStyle style) {
        StyleChangeRecord scr = new StyleChangeRecord();

        scr.stateNewStyles = true;
        scr.fillStyles = new FILLSTYLEARRAY();
        scr.stateFillStyle1 = true;
        scr.stateLineStyle = true;
        SvgFill fill = style.getFillWithOpacity();
        if (fill != null) {
            scr.fillStyles.fillStyles = new FILLSTYLE[1];
            scr.fillStyles.fillStyles[0] = new FILLSTYLE();
            if (fill instanceof SvgColor) {
                Color colorFill = fill.toColor();
                scr.fillStyles.fillStyles[0].color = shapeNum >= 3 ? new RGBA(colorFill) : new RGB(colorFill);
                scr.fillStyles.fillStyles[0].fillStyleType = FILLSTYLE.SOLID;
            } else if (fill instanceof SvgGradient) {
                //...aply in second step - applyStyleGradients
            }

            scr.fillStyle1 = 1;
        } else {
            scr.fillStyles.fillStyles = new FILLSTYLE[0];
            scr.fillStyle1 = 0;
        }

        scr.lineStyles = new LINESTYLEARRAY();
        SvgFill strokeFill = style.getStrokeColorWithOpacity();
        if (strokeFill != null) {
            Color lineColor = strokeFill.toColor();

            scr.lineStyles.lineStyles = new LINESTYLE[1];
            LINESTYLE lineStyle = shapeNum <= 3 ? new LINESTYLE() : new LINESTYLE2();;
            lineStyle.color = shapeNum >= 3 ? new RGBA(lineColor) : new RGB(lineColor);
            lineStyle.width = (int) Math.round(style.strokeWidth * SWF.unitDivisor);
            SvgLineCap lineCap = style.strokeLineCap;
            SvgLineJoin lineJoin = style.strokeLineJoin;
            if (lineStyle instanceof LINESTYLE2) {
                LINESTYLE2 lineStyle2 = (LINESTYLE2) lineStyle;
                int swfCap = lineCap == SvgLineCap.BUTT ? LINESTYLE2.NO_CAP
                        : lineCap == SvgLineCap.ROUND ? LINESTYLE2.ROUND_CAP
                                : lineCap == SvgLineCap.SQUARE ? LINESTYLE2.SQUARE_CAP : 0;
                lineStyle2.startCapStyle = swfCap;
                lineStyle2.endCapStyle = swfCap;

                int swfJoin = lineJoin == SvgLineJoin.MITER ? LINESTYLE2.MITER_JOIN
                        : lineJoin == SvgLineJoin.ROUND ? LINESTYLE2.ROUND_JOIN
                                : lineJoin == SvgLineJoin.BEVEL ? LINESTYLE2.BEVEL_JOIN : 0;
                lineStyle2.joinStyle = swfJoin;
                lineStyle2.miterLimitFactor = (int) style.strokeMiterLimit;
            } else {
                if (lineCap != SvgLineCap.ROUND) {
                    showWarning("lineCapNotSupported", "LineCap style not supported in shape " + shapeNum);
                }
                if (lineJoin != SvgLineJoin.ROUND) {
                    showWarning("lineJoinNotSupported", "LineJoin style not supported in shape " + shapeNum);
                }
            }

            scr.lineStyles.lineStyles[0] = lineStyle;
            scr.lineStyle = 1;
        } else {
            scr.lineStyles.lineStyles = new LINESTYLE[0];
            scr.lineStyle = 0;
        }

        return scr;
    }

    private class SvgStop implements Comparable<SvgStop> {

        public Color color;

        public double offset;

        public SvgStop(Color color, double offset) {
            this.color = color;
            this.offset = offset;
        }

        @Override
        public int compareTo(SvgStop o) {
            return (int) Math.signum(offset - o.offset);
        }
    }

    //FIXME - matrices
    private SvgFill parseGradient(Map<String, Element> idMap, Element el, SvgStyle style) {
        SvgGradientUnits gradientUnits = null;
        String gradientTransform = null;
        SvgSpreadMethod spreadMethod = null;
        SvgInterpolation interpolation = null;

        String x1 = null;
        String y1 = null;
        String x2 = null;
        String y2 = null;

        String cx = null;
        String cy = null;
        String fx = null;
        String fy = null;
        String r = null;

        List<SvgStop> stops = new ArrayList<>();
        //inheritance:
        if (el.hasAttribute("xlink:href")) {
            String parent = el.getAttribute("xlink:href");
            if (parent.startsWith("#")) {
                String parentId = parent.substring(1);
                Element parent_el = idMap.get(parentId);
                if (parent_el == null) {
                    showWarning("fillNotSupported", "Parent gradient not found.");
                    return new SvgColor(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                }

                if ("linearGradient".equals(el.getTagName()) && parent_el.getTagName().equals(el.getTagName())) {
                    SvgLinearGradient parentFill = (SvgLinearGradient) parseGradient(idMap, parent_el, style);
                    gradientUnits = parentFill.gradientUnits;
                    gradientTransform = parentFill.gradientTransform;
                    spreadMethod = parentFill.spreadMethod;

                    x1 = parentFill.x1;
                    y1 = parentFill.y1;
                    x2 = parentFill.x2;
                    y2 = parentFill.y2;
                    interpolation = parentFill.interpolation;
                    stops = parentFill.stops;
                }
                if ("radialGradient".equals(el.getTagName()) && parent_el.getTagName().equals(el.getTagName())) {
                    SvgRadialGradient parentFill = (SvgRadialGradient) parseGradient(idMap, parent_el, style);
                    gradientUnits = parentFill.gradientUnits;
                    gradientTransform = parentFill.gradientTransform;
                    spreadMethod = parentFill.spreadMethod;

                    cx = parentFill.cx;
                    cy = parentFill.cy;
                    fx = parentFill.fx;
                    fy = parentFill.fy;
                    r = parentFill.r;
                    interpolation = parentFill.interpolation;
                    stops = parentFill.stops;
                }

            } else {
                showWarning("fillNotSupported", "Parent gradient invalid.");
                return new SvgColor(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            }
        }

        if (el.hasAttribute("gradientUnits")) {
            switch (el.getAttribute("gradientUnits")) {
                case "userSpaceOnUse":
                    gradientUnits = SvgGradientUnits.USER_SPACE_ON_USE;
                    break;
                case "objectBoundingBox":
                    gradientUnits = SvgGradientUnits.OBJECT_BOUNDING_BOX;
                    break;
                default:
                    showWarning("fillNotSupported", "Unsupported  gradientUnits: " + el.getAttribute("gradientUnits"));
                    return new SvgColor(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            }

        }
        if (el.hasAttribute("gradientTransform")) {
            gradientTransform = el.getAttribute("gradientTransform");
        }
        if (el.hasAttribute("spreadMethod")) {
            switch (el.getAttribute("spreadMethod")) {
                case "pad":
                    spreadMethod = SvgSpreadMethod.PAD;
                    break;
                case "reflect":
                    spreadMethod = SvgSpreadMethod.REFLECT;
                    break;
                case "repeat":
                    spreadMethod = SvgSpreadMethod.REPEAT;
                    break;
            }
        }
        if (el.hasAttribute("x1")) {
            x1 = el.getAttribute("x1").trim();
        }
        if (el.hasAttribute("y1")) {
            y1 = el.getAttribute("y1").trim();
        }
        if (el.hasAttribute("x2")) {
            x2 = el.getAttribute("x2").trim();
        }
        if (el.hasAttribute("y2")) {
            y2 = el.getAttribute("y2").trim();
        }

        if (el.hasAttribute("cx")) {
            cx = el.getAttribute("cx").trim();
        }
        if (el.hasAttribute("cy")) {
            cy = el.getAttribute("cy").trim();
        }
        if (el.hasAttribute("fx")) {
            fx = el.getAttribute("fx").trim();
        }
        if (el.hasAttribute("fy")) {
            fy = el.getAttribute("fy").trim();
        }
        if (el.hasAttribute("r")) {
            r = el.getAttribute("r").trim();
        }
        if (el.hasAttribute("color-interpolation") && interpolation == null) { //prefer inherit
            switch (el.getAttribute("color-interpolation")) {
                case "sRGB":
                    interpolation = SvgInterpolation.SRGB;
                    break;
                case "linearRGB":
                    interpolation = SvgInterpolation.LINEAR_RGB;
                    break;
                case "auto": //without preference, put SRGB there
                    interpolation = SvgInterpolation.SRGB;
                    break;
            }
        }
        if (interpolation == null) {
            interpolation = SvgInterpolation.SRGB;
        }

        if (gradientUnits == null) {
            gradientUnits = SvgGradientUnits.OBJECT_BOUNDING_BOX;
        }
        if (spreadMethod == null) {
            spreadMethod = SvgSpreadMethod.PAD;
        }

        if (gradientTransform == null) {
            gradientTransform = "";
        }

        if (x1 == null) {
            x1 = "0%";
        }
        if (y1 == null) {
            y1 = "0%";
        }

        if (x2 == null) {
            x2 = "100%";
        }
        if (y2 == null) {
            y2 = "0%";
        }

        if (cx == null) {
            cx = "50%";
        }
        if (cy == null) {
            cy = "50%";
        }

        if (r == null) {
            r = "50%";
        }
        if (fx == null) {
            fx = cx;
        }
        if (fy == null) {
            fy = cy;
        }

        NodeList stopNodes = el.getElementsByTagName("stop");
        boolean stopsCleared = false;
        for (int i = 0; i < stopNodes.getLength(); i++) {
            Node node = stopNodes.item(i);
            if (node instanceof Element) {
                Element stopEl = (Element) node;
                SvgStyle newStyle = style.apply(stopEl, idMap);

                String offsetStr = stopEl.getAttribute("offset");
                double offset;
                if (offsetStr.endsWith("%")) {
                    offset = Double.parseDouble(offsetStr.substring(0, offsetStr.length() - 1)) / 100;
                } else {
                    offset = Double.parseDouble(offsetStr);
                }
                Color color = newStyle.stopColor;
                if (color == null) {
                    color = Color.BLACK;
                }

                int alpha = (int) Math.round(newStyle.stopOpacity * 255);
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
                if (!stopsCleared) { //It has some stop nodes -> remove all inherited stops
                    stopsCleared = true;
                    stops = new ArrayList<>();
                }
                stops.add(new SvgStop(color, offset));
            }
        }

        if ("linearGradient".equals(el.getTagName())) {
            SvgLinearGradient ret = new SvgLinearGradient();
            ret.x1 = x1;
            ret.y1 = y1;
            ret.x2 = x2;
            ret.y2 = y2;
            ret.spreadMethod = spreadMethod;
            ret.gradientTransform = gradientTransform;
            ret.gradientUnits = gradientUnits;
            ret.stops = stops;
            ret.interpolation = interpolation;
            return ret;
        } else if ("radialGradient".equals(el.getTagName())) {
            SvgRadialGradient ret = new SvgRadialGradient();
            ret.cx = cx;
            ret.cy = cy;
            ret.fx = fx;
            ret.fy = fy;
            ret.r = r;
            ret.spreadMethod = spreadMethod;
            ret.gradientTransform = gradientTransform;
            ret.gradientUnits = gradientUnits;
            ret.stops = stops;
            ret.interpolation = interpolation;
            return ret;
        } else {
            return null;
        }
    }

    private Color parseColor(String rgbStr) {
        SvgFill fill = parseFill(new HashMap<>(), rgbStr);
        return fill.toColor();
    }

    private SvgFill parseFill(Map<String, Element> idMap, String rgbStr) {
        if (rgbStr == null) {
            return null;
        }

        // named colors from: http://www.w3.org/TR/SVG/types.html#ColorKeywords
        switch (rgbStr) {
            case "none":
                return TRANSPARENT;
            case "aliceblue":
                return new SvgColor(240, 248, 255);
            case "antiquewhite":
                return new SvgColor(250, 235, 215);
            case "aqua":
                return new SvgColor(0, 255, 255);
            case "aquamarine":
                return new SvgColor(127, 255, 212);
            case "azure":
                return new SvgColor(240, 255, 255);
            case "beige":
                return new SvgColor(245, 245, 220);
            case "bisque":
                return new SvgColor(255, 228, 196);
            case "black":
                return new SvgColor(0, 0, 0);
            case "blanchedalmond":
                return new SvgColor(255, 235, 205);
            case "blue":
                return new SvgColor(0, 0, 255);
            case "blueviolet":
                return new SvgColor(138, 43, 226);
            case "brown":
                return new SvgColor(165, 42, 42);
            case "burlywood":
                return new SvgColor(222, 184, 135);
            case "cadetblue":
                return new SvgColor(95, 158, 160);
            case "chartreuse":
                return new SvgColor(127, 255, 0);
            case "chocolate":
                return new SvgColor(210, 105, 30);
            case "coral":
                return new SvgColor(255, 127, 80);
            case "cornflowerblue":
                return new SvgColor(100, 149, 237);
            case "cornsilk":
                return new SvgColor(255, 248, 220);
            case "crimson":
                return new SvgColor(220, 20, 60);
            case "cyan":
                return new SvgColor(0, 255, 255);
            case "darkblue":
                return new SvgColor(0, 0, 139);
            case "darkcyan":
                return new SvgColor(0, 139, 139);
            case "darkgoldenrod":
                return new SvgColor(184, 134, 11);
            case "darkgray":
                return new SvgColor(169, 169, 169);
            case "darkgreen":
                return new SvgColor(0, 100, 0);
            case "darkgrey":
                return new SvgColor(169, 169, 169);
            case "darkkhaki":
                return new SvgColor(189, 183, 107);
            case "darkmagenta":
                return new SvgColor(139, 0, 139);
            case "darkolivegreen":
                return new SvgColor(85, 107, 47);
            case "darkorange":
                return new SvgColor(255, 140, 0);
            case "darkorchid":
                return new SvgColor(153, 50, 204);
            case "darkred":
                return new SvgColor(139, 0, 0);
            case "darksalmon":
                return new SvgColor(233, 150, 122);
            case "darkseagreen":
                return new SvgColor(143, 188, 143);
            case "darkslateblue":
                return new SvgColor(72, 61, 139);
            case "darkslategray":
                return new SvgColor(47, 79, 79);
            case "darkslategrey":
                return new SvgColor(47, 79, 79);
            case "darkturquoise":
                return new SvgColor(0, 206, 209);
            case "darkviolet":
                return new SvgColor(148, 0, 211);
            case "deeppink":
                return new SvgColor(255, 20, 147);
            case "deepskyblue":
                return new SvgColor(0, 191, 255);
            case "dimgray":
                return new SvgColor(105, 105, 105);
            case "dimgrey":
                return new SvgColor(105, 105, 105);
            case "dodgerblue":
                return new SvgColor(30, 144, 255);
            case "firebrick":
                return new SvgColor(178, 34, 34);
            case "floralwhite":
                return new SvgColor(255, 250, 240);
            case "forestgreen":
                return new SvgColor(34, 139, 34);
            case "fuchsia":
                return new SvgColor(255, 0, 255);
            case "gainsboro":
                return new SvgColor(220, 220, 220);
            case "ghostwhite":
                return new SvgColor(248, 248, 255);
            case "gold":
                return new SvgColor(255, 215, 0);
            case "goldenrod":
                return new SvgColor(218, 165, 32);
            case "gray":
                return new SvgColor(128, 128, 128);
            case "grey":
                return new SvgColor(128, 128, 128);
            case "green":
                return new SvgColor(0, 128, 0);
            case "greenyellow":
                return new SvgColor(173, 255, 47);
            case "honeydew":
                return new SvgColor(240, 255, 240);
            case "hotpink":
                return new SvgColor(255, 105, 180);
            case "indianred":
                return new SvgColor(205, 92, 92);
            case "indigo":
                return new SvgColor(75, 0, 130);
            case "ivory":
                return new SvgColor(255, 255, 240);
            case "khaki":
                return new SvgColor(240, 230, 140);
            case "lavender":
                return new SvgColor(230, 230, 250);
            case "lavenderblush":
                return new SvgColor(255, 240, 245);
            case "lawngreen":
                return new SvgColor(124, 252, 0);
            case "lemonchiffon":
                return new SvgColor(255, 250, 205);
            case "lightblue":
                return new SvgColor(173, 216, 230);
            case "lightcoral":
                return new SvgColor(240, 128, 128);
            case "lightcyan":
                return new SvgColor(224, 255, 255);
            case "lightgoldenrodyellow":
                return new SvgColor(250, 250, 210);
            case "lightgray":
                return new SvgColor(211, 211, 211);
            case "lightgreen":
                return new SvgColor(144, 238, 144);
            case "lightgrey":
                return new SvgColor(211, 211, 211);
            case "lightpink":
                return new SvgColor(255, 182, 193);
            case "lightsalmon":
                return new SvgColor(255, 160, 122);
            case "lightseagreen":
                return new SvgColor(32, 178, 170);
            case "lightskyblue":
                return new SvgColor(135, 206, 250);
            case "lightslategray":
                return new SvgColor(119, 136, 153);
            case "lightslategrey":
                return new SvgColor(119, 136, 153);
            case "lightsteelblue":
                return new SvgColor(176, 196, 222);
            case "lightyellow":
                return new SvgColor(255, 255, 224);
            case "lime":
                return new SvgColor(0, 255, 0);
            case "limegreen":
                return new SvgColor(50, 205, 50);
            case "linen":
                return new SvgColor(250, 240, 230);
            case "magenta":
                return new SvgColor(255, 0, 255);
            case "maroon":
                return new SvgColor(128, 0, 0);
            case "mediumaquamarine":
                return new SvgColor(102, 205, 170);
            case "mediumblue":
                return new SvgColor(0, 0, 205);
            case "mediumorchid":
                return new SvgColor(186, 85, 211);
            case "mediumpurple":
                return new SvgColor(147, 112, 219);
            case "mediumseagreen":
                return new SvgColor(60, 179, 113);
            case "mediumslateblue":
                return new SvgColor(123, 104, 238);
            case "mediumspringgreen":
                return new SvgColor(0, 250, 154);
            case "mediumturquoise":
                return new SvgColor(72, 209, 204);
            case "mediumvioletred":
                return new SvgColor(199, 21, 133);
            case "midnightblue":
                return new SvgColor(25, 25, 112);
            case "mintcream":
                return new SvgColor(245, 255, 250);
            case "mistyrose":
                return new SvgColor(255, 228, 225);
            case "moccasin":
                return new SvgColor(255, 228, 181);
            case "navajowhite":
                return new SvgColor(255, 222, 173);
            case "navy":
                return new SvgColor(0, 0, 128);
            case "oldlace":
                return new SvgColor(253, 245, 230);
            case "olive":
                return new SvgColor(128, 128, 0);
            case "olivedrab":
                return new SvgColor(107, 142, 35);
            case "orange":
                return new SvgColor(255, 165, 0);
            case "orangered":
                return new SvgColor(255, 69, 0);
            case "orchid":
                return new SvgColor(218, 112, 214);
            case "palegoldenrod":
                return new SvgColor(238, 232, 170);
            case "palegreen":
                return new SvgColor(152, 251, 152);
            case "paleturquoise":
                return new SvgColor(175, 238, 238);
            case "palevioletred":
                return new SvgColor(219, 112, 147);
            case "papayawhip":
                return new SvgColor(255, 239, 213);
            case "peachpuff":
                return new SvgColor(255, 218, 185);
            case "peru":
                return new SvgColor(205, 133, 63);
            case "pink":
                return new SvgColor(255, 192, 203);
            case "plum":
                return new SvgColor(221, 160, 221);
            case "powderblue":
                return new SvgColor(176, 224, 230);
            case "purple":
                return new SvgColor(128, 0, 128);
            case "red":
                return new SvgColor(255, 0, 0);
            case "rosybrown":
                return new SvgColor(188, 143, 143);
            case "royalblue":
                return new SvgColor(65, 105, 225);
            case "saddlebrown":
                return new SvgColor(139, 69, 19);
            case "salmon":
                return new SvgColor(250, 128, 114);
            case "sandybrown":
                return new SvgColor(244, 164, 96);
            case "seagreen":
                return new SvgColor(46, 139, 87);
            case "seashell":
                return new SvgColor(255, 245, 238);
            case "sienna":
                return new SvgColor(160, 82, 45);
            case "silver":
                return new SvgColor(192, 192, 192);
            case "skyblue":
                return new SvgColor(135, 206, 235);
            case "slateblue":
                return new SvgColor(106, 90, 205);
            case "slategray":
                return new SvgColor(112, 128, 144);
            case "slategrey":
                return new SvgColor(112, 128, 144);
            case "snow":
                return new SvgColor(255, 250, 250);
            case "springgreen":
                return new SvgColor(0, 255, 127);
            case "steelblue":
                return new SvgColor(70, 130, 180);
            case "tan":
                return new SvgColor(210, 180, 140);
            case "teal":
                return new SvgColor(0, 128, 128);
            case "thistle":
                return new SvgColor(216, 191, 216);
            case "tomato":
                return new SvgColor(255, 99, 71);
            case "turquoise":
                return new SvgColor(64, 224, 208);
            case "violet":
                return new SvgColor(238, 130, 238);
            case "wheat":
                return new SvgColor(245, 222, 179);
            case "white":
                return new SvgColor(255, 255, 255);
            case "whitesmoke":
                return new SvgColor(245, 245, 245);
            case "yellow":
                return new SvgColor(255, 255, 0);
            case "yellowgreen":
                return new SvgColor(154, 205, 50);
        }

        Pattern idPat = Pattern.compile("url\\(#([^)]+)\\).*");
        java.util.regex.Matcher mPat = idPat.matcher(rgbStr);

        if (mPat.matches()) {
            String elementId = mPat.group(1);
            Element e = idMap.get(elementId);
            if (e != null) {
                String tagName = e.getTagName();
                if ("linearGradient".equals(tagName)) {
                    return parseGradient(idMap, e, new SvgStyle()); //? new style
                } else if ("radialGradient".equals(tagName)) {
                    return parseGradient(idMap, e, new SvgStyle()); //? new style
                } else {
                    showWarning("fillNotSupported", "Unknown fill style. Random color assigned.");
                    return new SvgColor(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                }
            }

            rgbStr = rgbStr.substring(elementId.length() + 6).trim(); // remove url(#...)
        }

        if (rgbStr.startsWith("#")) {
            String s = rgbStr.substring(1);
            if (s.length() == 3) {
                s = "" + s.charAt(0) + s.charAt(0) + s.charAt(1) + s.charAt(1) + s.charAt(2) + s.charAt(2);
            }

            int i = Integer.parseInt(s, 16);
            return new SvgColor(new Color(i, false));
        } else if (rgbStr.startsWith("rgb")) {
            rgbStr = rgbStr.substring(3).trim();
            if (rgbStr.startsWith("(") && rgbStr.endsWith(")")) {
                rgbStr = rgbStr.substring(1, rgbStr.length() - 1);
                String[] args = rgbStr.split(",");
                if (args.length == 3) {
                    String a0 = args[0].trim();
                    String a1 = args[1].trim();
                    String a2 = args[2].trim();
                    if (a0.endsWith("%") && a1.endsWith("%") && a2.endsWith("%")) {
                        int r = (int) Math.round(Integer.parseInt(a0.substring(0, a0.length() - 1)) * 255.0 / 100);
                        int g = (int) Math.round(Integer.parseInt(a1.substring(0, a1.length() - 1)) * 255.0 / 100);
                        int b = (int) Math.round(Integer.parseInt(a2.substring(0, a2.length() - 1)) * 255.0 / 100);
                        return new SvgColor(r, g, b);
                    } else {
                        int r = Integer.parseInt(a0);
                        int g = Integer.parseInt(a1);
                        int b = Integer.parseInt(a2);
                        return new SvgColor(r, g, b);
                    }
                }
            }
        } else {
            showWarning("fillNotSupported", "Only solid fills are supported. Random color assigned.");
            //showWarning("fillNotSupported", "Unknown fill style. Random color assigned.");
            return new SvgColor(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        return null;
    }

    class PathCommand {

        public char command;

        public double[] params;
    }

    enum SvgLineCap {

        BUTT, ROUND, SQUARE
    }

    enum SvgLineJoin {

        MITER, ROUND, BEVEL
    }

    enum SvgSpreadMethod {

        PAD, REFLECT, REPEAT
    }

    enum SvgGradientUnits {

        USER_SPACE_ON_USE, OBJECT_BOUNDING_BOX
    }

    abstract class SvgFill implements Cloneable {

        public abstract Color toColor();
    }

    enum SvgInterpolation {

        SRGB, LINEAR_RGB
    }

    abstract class SvgGradient extends SvgFill {

        public List<SvgStop> stops;

        public SvgGradientUnits gradientUnits;

        public String gradientTransform;

        public SvgSpreadMethod spreadMethod;

        public SvgInterpolation interpolation;

        @Override
        public Color toColor() {
            if (stops.isEmpty()) {
                return Color.BLACK;
            }
            return stops.get(0).color;
        }
    }

    class SvgLinearGradient extends SvgGradient {

        public String x1;

        public String y1;

        public String x2;

        public String y2;
        //xlink?
    }

    class SvgRadialGradient extends SvgGradient {

        public String cx;

        public String cy;

        public String r;

        public String fx;

        public String fy;
        //xlink?
    }

    class SvgColor extends SvgFill {

        public Color color;

        public SvgColor(int r, int g, int b, int opacity) {
            this(new Color(r, g, b, opacity));
        }

        public SvgColor(int r, int g, int b) {
            this(new Color(r, g, b));
        }

        public SvgColor(Color color) {
            this.color = color;
        }

        @Override
        public Color toColor() {
            return this.color;
        }
    }

    class SvgStyle implements Cloneable {

        public SvgFill fill;

        public double opacity;

        public double fillOpacity;

        public SvgFill strokeFill;

        public Color stopColor;

        public double stopOpacity;

        public double strokeWidth;

        public double strokeOpacity;

        public SvgLineCap strokeLineCap;

        public SvgLineJoin strokeLineJoin;

        public double strokeMiterLimit;

        public SvgStyle() {
            fill = new SvgColor(Color.black);
            fillOpacity = 1;
            strokeFill = null;
            strokeWidth = 1;
            strokeOpacity = 1;
            opacity = 1;
            stopOpacity = 1;
            stopColor = null;
            strokeLineCap = SvgLineCap.BUTT;
            strokeLineJoin = SvgLineJoin.MITER;
            strokeMiterLimit = 4;
        }

        public SvgFill getFillWithOpacity() {
            if (fill == null) {
                return null;
            }
            if (!(fill instanceof SvgColor)) {
                return fill;
            }
            Color fillColor = ((SvgColor) fill).color;

            int opacity = (int) Math.round(this.opacity * fillOpacity * 255);
            if (opacity == 255) {
                return fill;
            }

            return new SvgColor(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), opacity);
        }

        public SvgFill getStrokeColorWithOpacity() {
            if (strokeFill == null) {
                return null;
            }
            if (!(strokeFill instanceof SvgColor)) {
                return strokeFill;
            }

            Color strokeColor = ((SvgColor) strokeFill).color;

            int opacity = (int) Math.round(this.opacity * strokeOpacity * 255);
            if (opacity == 255) {
                return strokeFill;
            }

            return new SvgColor(strokeColor.getRed(), strokeColor.getGreen(), strokeColor.getBlue(), opacity);
        }

        @Override
        public SvgStyle clone() {
            try {
                SvgStyle ret = (SvgStyle) super.clone();
                return ret;
            } catch (CloneNotSupportedException ex) {
                throw new RuntimeException();
            }
        }

        private void applyStyle(Map<String, Element> idMap, SvgStyle style, String name, String value) {
            if (value == null || value.length() == 0) {
                return;
            }

            switch (name) {
                case "fill": {
                    SvgFill fill = parseFill(idMap, value);
                    if (fill != null) {
                        style.fill = fill == TRANSPARENT ? null : fill;
                    }
                }
                break;
                case "stop-color": {
                    if ("inherit".equals(value) || "currentColor".equals(value)) {
                        showWarning(value + "StopColorNotSupported", "The stop color value '" + value + "' is not supported.");
                    } else {
                        style.stopColor = parseColor(value);
                    }
                }
                break;
                case "fill-opacity": {
                    double opacity = Double.parseDouble(value);
                    style.fillOpacity = opacity;
                }
                break;
                case "stop-opacity": {
                    if ("inherit".equals(value)) {
                        showWarning(value + "StopOpacityNotSupported", "The stop opacity value '" + value + "' is not supported.");
                    } else {
                        double stopOpacity = Double.parseDouble(value);
                        style.stopOpacity = stopOpacity;
                    }
                }
                break;
                case "stroke": {
                    SvgFill strokeFill = parseFill(idMap, value);
                    if (strokeFill != null) {
                        style.strokeFill = strokeFill == TRANSPARENT ? null : strokeFill;
                    }
                }
                break;
                case "stroke-width": {
                    double strokeWidth = Double.parseDouble(value);
                    style.strokeWidth = strokeWidth;
                }
                break;
                case "stroke-opacity": {
                    double opacity = Double.parseDouble(value);
                    style.strokeOpacity = opacity;
                }
                break;
                case "stroke-linecap": {
                    switch (value) {
                        case "butt":
                            style.strokeLineCap = SvgLineCap.BUTT;
                            break;
                        case "round":
                            style.strokeLineCap = SvgLineCap.ROUND;
                            break;
                        case "square":
                            style.strokeLineCap = SvgLineCap.SQUARE;
                            break;
                    }
                }
                break;
                case "stroke-linejoin": {
                    switch (value) {
                        case "miter":
                            style.strokeLineJoin = SvgLineJoin.MITER;
                            break;
                        case "round":
                            style.strokeLineJoin = SvgLineJoin.ROUND;
                            break;
                        case "bevel":
                            style.strokeLineJoin = SvgLineJoin.BEVEL;
                            break;
                    }
                }
                break;
                case "stroke-miterlimit": {
                    double strokeMiterLimit = Double.parseDouble(value);
                    style.strokeMiterLimit = strokeMiterLimit;
                }
                case "opacity": {
                    double opacity = Double.parseDouble(value);
                    style.opacity = opacity;
                }
                break;
            }
        }

        private SvgStyle apply(Element element, Map<String, Element> idMap) {
            SvgStyle result = clone();

            String[] styles = new String[]{
                "fill", "fill-opacity",
                "stroke", "stroke-width", "stroke-opacity", "stroke-linecap", "stroke-linejoin", "stroke-miterlimit",
                "opacity", "stop-color", "stop-opacity"
            };

            for (String style : styles) {
                if (element.hasAttribute(style)) {
                    String attr = element.getAttribute(style).trim();
                    applyStyle(idMap, result, style, attr);
                }
            }

            if (element.hasAttribute("style")) {
                String[] styleDefs = element.getAttribute("style").split(";");
                for (String styleDef : styleDefs) {
                    String[] parts = styleDef.split(":", 2);
                    applyStyle(idMap, result, parts[0].trim(), parts[1].trim());
                }
            }

            return result;
        }
    }

    private void showWarning(String name, String text) {
        if (!shownWarnings.contains(name)) {
            Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, text);
            shownWarnings.add(name);
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

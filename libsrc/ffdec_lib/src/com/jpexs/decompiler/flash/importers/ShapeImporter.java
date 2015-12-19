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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
                Matrix m = Matrix.parseSvgMatrix(childElement.getAttribute("transform"), SWF.unitDivisor, 1);
                Matrix m2 = m == null ? transform : m.concatenate(transform);
                if ("g".equals(tagName)) {
                    processSvgObject(shapeNum, shapes, childElement, m2, newStyle);
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
                } else {
                    showWarning(tagName + "tagNotSupported", "The SVG tag '" + tagName + "' is not supported.");
                }
            }
        }
    }

    private void processCommands(int shapeNum, SHAPEWITHSTYLE shapes, List<PathCommand> commands, Matrix transform, SvgStyle style) {
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
                    x = command.params[0];
                    y = command.params[1];

                    p = transform.transform(x, y);
                    serl.deltaX = (int) Math.round(p.x - prevPoint.x);
                    serl.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;
                    System.out.println("L" + serl.deltaX + "," + serl.deltaY);
                    serl.generalLineFlag = true;
                    serl.simplify();
                    shapes.shapeRecords.add(serl);
                    break;
                case 'H':
                    StraightEdgeRecord serh = new StraightEdgeRecord();
                    x = command.params[0];

                    p = transform.transform(x, y);
                    serh.deltaX = (int) Math.round(p.x - prevPoint.x);
                    prevPoint = p;
                    System.out.println("H" + serh.deltaX);
                    shapes.shapeRecords.add(serh);
                    break;
                case 'V':
                    StraightEdgeRecord serv = new StraightEdgeRecord();
                    y = command.params[0];

                    p = transform.transform(x, y);
                    serv.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;
                    System.out.println("V" + serv.deltaY);
                    serv.vertLineFlag = true;
                    shapes.shapeRecords.add(serv);
                    break;
                case 'Q':
                    CurvedEdgeRecord cer = new CurvedEdgeRecord();
                    x = command.params[0];
                    y = command.params[1];

                    p = transform.transform(x, y);
                    cer.controlDeltaX = (int) Math.round(p.x - prevPoint.x);
                    cer.controlDeltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;

                    x = command.params[2];
                    y = command.params[3];

                    p = transform.transform(x, y);
                    cer.anchorDeltaX = (int) Math.round(p.x - prevPoint.x);
                    cer.anchorDeltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = p;
                    System.out.println("Q" + cer.controlDeltaX + "," + cer.controlDeltaY + "," + cer.anchorDeltaX + "," + cer.controlDeltaY);
                    shapes.shapeRecords.add(cer);
                    break;
                case 'C':
                    showWarning("cubicCurvesNotSupported", "Cubic curves are not supported by Flash.");

                    // create at least something...
                    Point pStart = prevPoint;
                    Point pControl1;

                    x = command.params[0];
                    y = command.params[1];

                    pControl1 = transform.transform(x, y);

                    x = command.params[2];
                    y = command.params[3];

                    Point pControl2 = transform.transform(x, y);

                    x = command.params[4];
                    y = command.params[5];

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

            x0 = x;
            y0 = y;
        }
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

                    double x1 = x0;
                    double y1 = y0;
                    double x2 = x;
                    double y2 = y;

                    double d1 = (x1 - x2) / 2;
                    double d2 = (y1 - y2) / 2;
                    double x1Comma = Math.cos(fi) * d1 + Math.sin(fi) * d2;
                    double y1Comma = -Math.sin(fi) * d1 + Math.cos(fi) * d2;

                    double c = Math.sqrt((rx * rx * ry * ry - rx * rx * y1Comma * y1Comma - ry * ry * x1Comma * x1Comma) / (rx * rx * y1Comma * y1Comma + ry * ry * x1Comma * x1Comma));
                    double cxComma = c * rx * y1Comma / ry;
                    double cyComma = c * -ry * x1Comma / rx;

                    if (largeFlag == sweepFlag) {
                        cxComma = -cxComma;
                        cyComma = -cyComma;
                    }

                    double cx = Math.cos(fi) * cxComma - Math.sin(fi) * cyComma + (x1 + x2) / 2;
                    double cy = Math.sin(fi) * cxComma + Math.cos(fi) * cyComma + (y1 + y2) / 2;

                    // todo: draw arc, now draw only a line
                    PathCommand sera = new PathCommand();
                    sera.command = 'L';
                    sera.params = new double[]{cx, cy};
                    pathCommands.add(sera);

                    sera = new PathCommand();
                    sera.command = 'L';
                    sera.params = new double[]{x, y};
                    pathCommands.add(sera);

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

    private void processCircle(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style) {
        String attr = childElement.getAttribute("cx");
        double cx = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("cy");
        double cy = attr.length() > 0 ? Double.parseDouble(attr) : 0;

        attr = childElement.getAttribute("r");
        double r = attr.length() > 0 ? Double.parseDouble(attr) : 0;
        double sqrt2RHalf = Math.sqrt(2) * r / 2;
        double sqrt2Minus1R = (Math.sqrt(2) - 1) * r;

        List<PathCommand> pathCommands = new ArrayList<>();
        PathCommand scr = new PathCommand();
        scr.command = 'M';
        scr.params = new double[]{cx + r, cy};
        pathCommands.add(scr);

        double[] points = new double[]{
            cx + r, cy - sqrt2Minus1R,
            cx + sqrt2RHalf, cy - sqrt2RHalf,
            cx + sqrt2Minus1R, cy - r,
            cx, cy - r,
            cx - sqrt2Minus1R, cy - r,
            cx - sqrt2RHalf, cy - sqrt2RHalf,
            cx - r, cy - sqrt2Minus1R,
            cx - r, cy,
            cx - r, cy + sqrt2Minus1R,
            cx - sqrt2RHalf, cy + sqrt2RHalf,
            cx - sqrt2Minus1R, cy + r,
            cx, cy + r,
            cx + sqrt2Minus1R, cy + r,
            cx + sqrt2RHalf, cy + sqrt2RHalf,
            cx + r, cy + sqrt2Minus1R,
            cx + r, cy};

        for (int i = 0; i < points.length; i += 4) {
            PathCommand cer = new PathCommand();
            cer.command = 'Q';
            cer.params = new double[]{points[i], points[i + 1], points[i + 2], points[i + 3]};

            pathCommands.add(cer);
        }

        processCommands(shapeNum, shapes, pathCommands, transform, style);
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
            cx + rx, cy - sqrt2Minus1RY,
            cx + sqrt2RXHalf, cy - sqrt2RYHalf,
            cx + sqrt2Minus1RX, cy - ry,
            cx, cy - ry,
            cx - sqrt2Minus1RX, cy - ry,
            cx - sqrt2RXHalf, cy - sqrt2RYHalf,
            cx - rx, cy - sqrt2Minus1RY,
            cx - rx, cy,
            cx - rx, cy + sqrt2Minus1RY,
            cx - sqrt2RXHalf, cy + sqrt2RYHalf,
            cx - sqrt2Minus1RX, cy + ry,
            cx, cy + ry,
            cx + sqrt2Minus1RX, cy + ry,
            cx + sqrt2RXHalf, cy + sqrt2RYHalf,
            cx + rx, cy + sqrt2Minus1RY,
            cx + rx, cy};

        for (int i = 0; i < points.length; i += 4) {
            PathCommand cer = new PathCommand();
            cer.command = 'Q';
            cer.params = new double[]{points[i], points[i + 1], points[i + 2], points[i + 3]};

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
            Color colorFill = fill.toColor();
            scr.fillStyles.fillStyles[0].color = shapeNum >= 3 ? new RGBA(colorFill) : new RGB(colorFill);
            scr.fillStyles.fillStyles[0].fillStyleType = FILLSTYLE.SOLID;
            //TODO: handle different fills
            if (fill instanceof SvgColor) {
                //TODO
            } else if (fill instanceof SvgLinearGradient) {
                //TODO
            } else if (fill instanceof SvgRadialGradient) {
                //TODO
            }

            scr.fillStyle1 = 1;
        } else {
            scr.fillStyles.fillStyles = new FILLSTYLE[0];
            scr.fillStyle1 = 0;
        }

        scr.lineStyles = new LINESTYLEARRAY();
        Color lineColor = style.getStrokeColorWithOpacity().toColor();
        if (lineColor != null) {
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

    private SvgFill parseFill(String rgbStr) {
        if (rgbStr == null) {
            return null;
        }

        // todo: honfika: named colors: http://www.w3.org/TR/SVG/types.html#ColorKeywords
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

    abstract class SvgGradient extends SvgFill {

        public List<String> gradOffsets;
        public List<Color> gradColors;

        public SvgGradientUnits gradientUnits;
        public String gradientTransform;
        public SvgSpreadMethod spreadMethod;

        @Override
        public Color toColor() {
            if (gradColors.isEmpty()) {
                return Color.BLACK;
            }
            return gradColors.get(0);
        }

    }

    class SvgLinearGradient extends SvgGradient {

        public double x1;
        public double y1;
        public double x2;
        public double y2;

        //xlink?
    }

    class SvgRadialGradient extends SvgGradient {

        public double cx;
        public double cy;
        public double r;
        public double fx;
        public double fy;

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

        private void applyStyle(SvgStyle style, String name, String value) {
            if (value == null || value.length() == 0) {
                return;
            }

            switch (name) {
                case "fill": {
                    SvgFill fill = parseFill(value);
                    if (fill != null) {
                        style.fill = fill == TRANSPARENT ? null : fill;
                    }
                }
                break;
                case "fill-opacity": {
                    double opacity = Double.parseDouble(value);
                    style.fillOpacity = opacity;
                }
                break;
                case "stroke": {
                    SvgFill strokeFill = parseFill(value);
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

        private SvgStyle apply(Element element) {
            SvgStyle result = clone();

            String[] styles = new String[]{
                "fill", "fill-opacity",
                "stroke", "stroke-width", "stroke-opacity", "stroke-linecap", "stroke-linejoin", "stroke-miterlimit",
                "opacity"
            };

            for (String style : styles) {
                if (element.hasAttribute(style)) {
                    String attr = element.getAttribute(style);
                    applyStyle(result, style, attr);

                }
            }

            if (element.hasAttribute("style")) {
                String[] styleDefs = element.getAttribute("style").split(";");
                for (String styleDef : styleDefs) {
                    String[] parts = styleDef.split(":", 2);
                    applyStyle(result, parts[0], parts[1].trim());
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

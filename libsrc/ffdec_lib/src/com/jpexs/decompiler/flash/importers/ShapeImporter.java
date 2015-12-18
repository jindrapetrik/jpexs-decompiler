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

    private static final Color TRANSPARENT = new Color(0, true);

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
            Point p = null;
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
                    } else {
                        if (prevQControlPoint != null) {
                            pControl = new Point(2 * x0 - prevQControlPoint.x, 2 * y0 - prevQControlPoint.y);
                        } else {
                            pControl = new Point(x0, y0);
                        }
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
                    } else {
                        if (prevCControlPoint != null) {
                            pControl1 = new Point(2 * x0 - prevCControlPoint.x, 2 * y0 - prevCControlPoint.y);
                        } else {
                            pControl1 = new Point(x0, y0);
                        }
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
                    double xRot = pathReader.readDouble();
                    int largeFlag = (int) pathReader.readDouble();
                    int sweepFlag = (int) pathReader.readDouble();

                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    // todo: draw arc, now draw only a line
                    PathCommand sera = new PathCommand();
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

    private Color parseColor(String rgbStr) {
        if (rgbStr == null) {
            return null;
        }

        // todo: honfika: named colors: http://www.w3.org/TR/SVG/types.html#ColorKeywords
        switch (rgbStr) {
            case "none":
                return TRANSPARENT;
        }

        if (rgbStr.startsWith("#")) {
            String s = rgbStr.substring(1);
            if (s.length() == 3) {
                s = "" + s.charAt(0) + s.charAt(0) + s.charAt(1) + s.charAt(1) + s.charAt(2) + s.charAt(2);
            }

            int i = Integer.parseInt(s, 16);
            return new Color(i, false);
        } else {
            showWarning("fillNotSupported", "Only solid fills are supported. Random color assigned.");
            return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
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

    class SvgStyle implements Cloneable {

        public Color fillColor;

        public double opacity;

        public double fillOpacity;

        public Color strokeColor;

        public double strokeWidth;

        public double strokeOpacity;

        public SvgLineCap strokeLineCap;

        public SvgLineJoin strokeLineJoin;

        public double strokeMiterLimit;

        public SvgStyle() {
            fillColor = Color.BLACK;
            fillOpacity = 1;
            strokeColor = null;
            strokeWidth = 1;
            strokeOpacity = 1;
            opacity = 1;
            strokeLineCap = SvgLineCap.BUTT;
            strokeLineJoin = SvgLineJoin.MITER;
            strokeMiterLimit = 4;
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

            int opacity = (int) Math.round(this.opacity * strokeOpacity * 255);
            if (opacity == 255) {
                return strokeColor;
            }

            return new Color(strokeColor.getRed(), strokeColor.getGreen(), strokeColor.getBlue(), opacity);
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
                    Color fillColor = parseColor(value);
                    if (fillColor != null) {
                        style.fillColor = fillColor == TRANSPARENT ? null : fillColor;
                    }
                }
                break;
                case "fill-opacity": {
                    double opacity = Double.parseDouble(value);
                    style.fillOpacity = opacity;
                }
                break;
                case "stroke": {
                    Color strokeColor = parseColor(value);
                    if (strokeColor != null) {
                        style.strokeColor = strokeColor == TRANSPARENT ? null : strokeColor;
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

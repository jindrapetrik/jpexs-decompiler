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
import com.jpexs.decompiler.flash.helpers.ImageHelper;
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

            processSvgObject(shapeNum, shapes, rootElement);

        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(ShapeImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        shapes.shapeRecords.add(new EndShapeRecord());

        st.shapes = shapes;
        st.setModified(true);

        return (Tag) st;
    }

    private void processSvgObject(int shapeNum, SHAPEWITHSTYLE shapes, Element element) {
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                String tagName = childElement.getTagName();
                if ("g".equals(tagName)) {
                    processSvgObject(shapeNum, shapes, childElement);
                } else if ("path".equals(tagName)) {
                    processPath(shapeNum, shapes, childElement);
                }
            }
        }
    }

    private void processPath(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement) {
        String data = childElement.getAttribute("d");
        if (data == null) {
            return;
        }

        String attr = childElement.getAttribute("fill");
        Color fillColor = parseColor(attr);

        attr = childElement.getAttribute("fill-opacity");
        if (fillColor != null && attr != null && attr.length() > 0) {
            double opacity = Double.parseDouble(attr);
            fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), (int) Math.round(opacity * 255));
        }

        attr = childElement.getAttribute("stroke");
        Color lineColor = parseColor(attr);

        attr = childElement.getAttribute("stroke-width");
        int lineWidth = (int) Math.round((attr == null || attr.length() == 0 ? 1 : Double.parseDouble(attr)) * SWF.unitDivisor);

        char command = 0;
        double x0 = 0;
        double y0 = 0;

        boolean newShape = true;
        SvgPathReader pathReader = new SvgPathReader(data);
        while (pathReader.hasNext()) {
            char newCommand;
            if ((newCommand = pathReader.readCommand()) != 0) {
                command = newCommand;
                continue;
            }

            boolean isRelative = Character.isLowerCase(command);

            double x = pathReader.readDouble();
            double y = pathReader.readDouble();
            if (isRelative) {
                x += x0;
                y += y0;
            }

            switch (Character.toUpperCase(command)) {
                case 'M':
                    StyleChangeRecord scr = new StyleChangeRecord();
                    scr.moveDeltaX = (int) Math.round((x - x0) * SWF.unitDivisor);
                    scr.moveDeltaY = (int) Math.round((y - y0) * SWF.unitDivisor);
                    scr.stateMoveTo = true;

                    if (newShape) {
                        newShape = false;
                        scr.stateNewStyles = true;
                        scr.fillStyles = new FILLSTYLEARRAY();
                        scr.stateFillStyle1 = true;
                        scr.stateLineStyle = true;
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
                        if (lineColor != null) {
                            scr.lineStyles.lineStyles = new LINESTYLE[1];
                            scr.lineStyles.lineStyles[0] = shapeNum <= 3 ? new LINESTYLE() : new LINESTYLE2();
                            scr.lineStyles.lineStyles[0].color = shapeNum >= 3 ? new RGBA(lineColor) : new RGB(lineColor);
                            scr.lineStyles.lineStyles[0].width = lineWidth;
                            scr.lineStyle = 1;
                        } else {
                            scr.lineStyles.lineStyles = new LINESTYLE[0];
                            scr.lineStyle = 0;
                        }
                    }

                    shapes.shapeRecords.add(scr);
                    break;
                case 'L':
                    StraightEdgeRecord ser = new StraightEdgeRecord();
                    ser.deltaX = (int) Math.round((x - x0) * SWF.unitDivisor);
                    ser.deltaY = (int) Math.round((y - y0) * SWF.unitDivisor);
                    ser.generalLineFlag = true;
                    shapes.shapeRecords.add(ser);
                    break;
                case 'Q':
                    CurvedEdgeRecord cer = new CurvedEdgeRecord();
                    cer.controlDeltaX = (int) Math.round((x - x0) * SWF.unitDivisor);
                    cer.controlDeltaY = (int) Math.round((y - y0) * SWF.unitDivisor);
                    x0 = x;
                    y0 = y;
                    x = pathReader.readDouble();
                    y = pathReader.readDouble();
                    if (isRelative) {
                        x += x0;
                        y += y0;
                    }

                    cer.anchorDeltaX = (int) Math.round((x - x0) * SWF.unitDivisor);
                    cer.anchorDeltaY = (int) Math.round((y - y0) * SWF.unitDivisor);
                    shapes.shapeRecords.add(cer);
                    break;
            }

            x0 = x;
            y0 = y;
        }
    }

    private Color parseColor(String rgbStr) {
        if (rgbStr == null) {
            return null;
        }

        if (rgbStr.startsWith("#")) {
            String s = rgbStr.substring(1);
            int i = Integer.parseInt(s, 16);
            return new Color(i, s.length() > 6);
        }

        return null;
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

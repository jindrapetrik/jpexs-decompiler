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

        int shapeNum = st.getShapeNum();
        shapes.fillStyles.fillStyles = new FILLSTYLE[1];
        shapes.fillStyles.fillStyles[0] = new FILLSTYLE();
        shapes.fillStyles.fillStyles[0].color = shapeNum >= 3 ? new RGBA(0, 255, 0, 255) : new RGB(0, 255, 0);
        shapes.fillStyles.fillStyles[0].fillStyleType = FILLSTYLE.SOLID;
        shapes.lineStyles.lineStyles = new LINESTYLE[1];
        shapes.lineStyles.lineStyles[0] = shapeNum <= 3 ? new LINESTYLE() : new LINESTYLE2();
        shapes.lineStyles.lineStyles[0].color = shapeNum >= 3 ? new RGBA(255, 0, 0, 255) : new RGB(255, 0, 0);
        shapes.lineStyles.lineStyles[0].width = (int) SWF.unitDivisor;

        shapes.shapeRecords = new ArrayList<>();

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(svgXml)));
            Element rootElement = doc.getDocumentElement();

            if (!"svg".equals(rootElement.getTagName())) {
                throw new IOException("SVG root element should be 'svg'");
            }

            processSvgObject(shapes, rootElement);

        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(ShapeImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        shapes.shapeRecords.add(new EndShapeRecord());

        st.shapes = shapes;
        st.setModified(true);

        return (Tag) st;
    }

    private void processSvgObject(SHAPEWITHSTYLE shapes, Element element) {
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                String tagName = childElement.getTagName();
                if ("g".equals(tagName)) {
                    processSvgObject(shapes, childElement);
                } else if ("path".equals(tagName)) {
                    processPath(shapes, childElement);
                }
            }
        }
    }

    private void processPath(SHAPEWITHSTYLE shapes, Element childElement) {
        String data = childElement.getAttribute("d");
        if (data == null) {
            return;
        }

        char command = 0;
        String[] parts = data.split(" ");
        double x0 = 0;
        double y0 = 0;
        for (int i = 0; i < parts.length; i += 2) {
            String part = parts[i];
            if (part.length() == 0) {
                continue;
            }

            char ch = part.charAt(0);
            if (ch == 'M' || ch == 'L' || ch == 'Q') {
                command = ch;
                part = part.substring(1);
            }

            double x = Double.parseDouble(part);

            part = parts[i + 1];
            double y = Double.parseDouble(part);

            switch (command) {
                case 'M':
                    StyleChangeRecord scr = new StyleChangeRecord();
                    scr.moveDeltaX = (int) ((x - x0) * SWF.unitDivisor);
                    scr.moveDeltaY = (int) ((y - y0) * SWF.unitDivisor);
                    scr.stateMoveTo = true;
                    scr.stateFillStyle0 = true;
                    scr.stateLineStyle = true;
                    scr.fillStyle0 = 1;
                    scr.lineStyle = 1;
                    shapes.shapeRecords.add(scr);
                    break;
                case 'L':
                    StraightEdgeRecord ser = new StraightEdgeRecord();
                    ser.deltaX = (int) ((x - x0) * SWF.unitDivisor);
                    ser.deltaY = (int) ((y - y0) * SWF.unitDivisor);
                    ser.generalLineFlag = true;
                    shapes.shapeRecords.add(ser);
                    break;
                case 'Q':
                    CurvedEdgeRecord cer = new CurvedEdgeRecord();
                    cer.controlDeltaX = (int) ((x - x0) * SWF.unitDivisor);
                    cer.controlDeltaY = (int) ((y - y0) * SWF.unitDivisor);
                    x0 = x;
                    y0 = y;
                    part = parts[i + 2];
                    x = Double.parseDouble(part);
                    part = parts[i + 3];
                    y = Double.parseDouble(part);
                    i += 2;
                    cer.anchorDeltaX = (int) ((x - x0) * SWF.unitDivisor);
                    cer.anchorDeltaY = (int) ((y - y0) * SWF.unitDivisor);
                    shapes.shapeRecords.add(cer);
                    break;
            }

            x0 = x;
            y0 = y;
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

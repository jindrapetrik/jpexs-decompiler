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
package com.jpexs.decompiler.flash.importers.svg;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.exporters.ShapeExporter;
import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.Point;
import com.jpexs.decompiler.flash.exporters.modes.ShapeExportMode;
import com.jpexs.decompiler.flash.exporters.settings.ShapeExportSettings;
import com.jpexs.decompiler.flash.exporters.shape.BitmapExporter;
import com.jpexs.decompiler.flash.importers.ShapeImporter;
import com.jpexs.decompiler.flash.importers.svg.css.CssParseException;
import com.jpexs.decompiler.flash.importers.svg.css.CssParser;
import com.jpexs.decompiler.flash.importers.svg.css.CssSelectorToXPath;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.ExportAssetsTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.FOCALGRADIENT;
import com.jpexs.decompiler.flash.types.GRADIENT;
import com.jpexs.decompiler.flash.types.GRADRECORD;
import com.jpexs.decompiler.flash.types.ILINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLE2;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * SVG importer.
 *
 * @author JPEXS
 */
public class SvgImporter {

    private final Set<String> shownWarnings = new HashSet<>();

    /**
     * Shape or morphshape tag
     */
    Tag shapeTag;

    ShapeTag endShape;

    private Rectangle2D.Double viewBox;

    /**
     * Constructor.
     * @param st Shape tag
     * @param svgXml SVG XML
     * @return Imported tag
     */
    public Tag importSvg(ShapeTag st, String svgXml) {
        return importSvg((Tag) st, null, svgXml, true);
    }

    /**
     * Constructor.
     * @param mst Morph shape tag
     * @param svgXml SVG XML
     * @return Imported tag
     */
    public Tag importSvg(MorphShapeTag mst, String svgXml) {
        return importSvg((Tag) mst, null, svgXml, true);
    }

    /**
     * Constructor.
     * @param st Shape tag
     * @param svgXml SVG XML
     * @param fill Fill flag
     * @return Imported tag
     */
    public Tag importSvg(ShapeTag st, String svgXml, boolean fill) {
        return importSvg((Tag) st, null, svgXml, fill);
    }

    /**
     * Constructor.
     * @param mst Morph shape tag
     * @param svgXml SVG XML
     * @param fill Fill flag
     * @return Imported tag
     */
    public Tag importSvg(MorphShapeTag mst, String svgXml, boolean fill) {
        return importSvg((Tag) mst, null, svgXml, fill);
    }

    /**
     * Constructor.
     * @param startShape Start shape tag
     * @param endShape End shape tag
     * @param svgXml SVG XML
     * @param fill Fill flag
     * @return Imported tag
     */
    public Tag importSvg(ShapeTag startShape, ShapeTag endShape, String svgXml, boolean fill) {
        return importSvg((Tag) startShape, endShape, svgXml, fill);
    }

    /**
     * Constructor.
     * @param st Start shape tag
     * @param endShape End shape tag
     * @param svgXml SVG XML
     * @param fill Fill flag
     * @return Imported tag
     */
    private Tag importSvg(Tag st, ShapeTag endShape, String svgXml, boolean fill) {
        shapeTag = st;
        this.endShape = endShape;

        boolean morphShape = st instanceof MorphShapeTag;

        if (st instanceof DefineShape4Tag) {
            DefineShape4Tag shape4 = (DefineShape4Tag) st;
            shape4.usesFillWindingRule = false;
            shape4.usesNonScalingStrokes = false;
            shape4.usesScalingStrokes = false;
        }

        SHAPEWITHSTYLE shapes = new SHAPEWITHSTYLE();
        shapes.fillStyles = new FILLSTYLEARRAY();
        shapes.lineStyles = new LINESTYLEARRAY();
        shapes.fillStyles.fillStyles = new FILLSTYLE[0];
        shapes.lineStyles.lineStyles = new LINESTYLE[0];

        SHAPEWITHSTYLE shapes2 = new SHAPEWITHSTYLE();
        shapes2.fillStyles = new FILLSTYLEARRAY();
        shapes2.lineStyles = new LINESTYLEARRAY();
        shapes2.fillStyles.fillStyles = new FILLSTYLE[0];
        shapes2.lineStyles.lineStyles = new LINESTYLE[0];

        int shapeNum = 0;
        RECT rect = null;

        if (st instanceof ShapeTag) {
            shapeNum = ((ShapeTag) st).getShapeNum();
            rect = ((ShapeTag) st).getRect();
        }
        if (st instanceof MorphShapeTag) {
            int morphShapeNum = ((MorphShapeTag) st).getShapeNum();
            if (morphShapeNum == 2) {
                shapeNum = 4;
            } else {
                shapeNum = 3;
            }
            rect = ((MorphShapeTag) st).getRect();
        }

        int origXmin = rect.Xmin;
        int origYmin = rect.Ymin;

        shapes.shapeRecords = new ArrayList<>();
        shapes2.shapeRecords = new ArrayList<>();

        Rectangle2D.Double viewBox = null;
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

            double width = 800;
            double height = 600;

            if (rootElement.hasAttribute("viewBox")) {
                String params = rootElement.getAttribute("viewBox");
                String[] args = Matrix.parseSvgNumberList(params);
                viewBox = new Rectangle2D.Double();
                if (args.length > 0) {
                    viewBox.x = parseNumber(args[0]);
                }
                if (args.length > 1) {
                    viewBox.y = parseNumber(args[1]);
                }
                if (args.length > 2) {
                    viewBox.width = parseNumber(args[2]);
                }
                if (args.length > 3) {
                    viewBox.height = parseNumber(args[3]);
                }

                width = viewBox.width;
                height = viewBox.height;
            }

            if (rootElement.hasAttribute("width")) {
                width = parseLength(rootElement.getAttribute("width"), width);
            }

            if (rootElement.hasAttribute("height")) {
                height = parseLength(rootElement.getAttribute("height"), height);
            }

            if (viewBox == null) {
                viewBox = new Rectangle2D.Double();
                viewBox.width = width;
                viewBox.height = height;
            }

            this.viewBox = viewBox;

            Map<String, Integer> cachedBitmaps = new HashMap<>();
            SvgStyle style = new SvgStyle(this, idMap, rootElement, cachedBitmaps);
            Matrix transform = new Matrix();

            if (fill) {
                double ratioX = rect.getWidth() / width / SWF.unitDivisor;
                double ratioY = rect.getHeight() / height / SWF.unitDivisor;
                transform = Matrix.getScaleInstance(ratioX, ratioY);
                transform.translate(origXmin / SWF.unitDivisor / ratioX, origYmin / SWF.unitDivisor / ratioY);
            }

            transform = transform.preConcatenate(Matrix.getTranslateInstance(-viewBox.x, -viewBox.y));
            if (viewBox.height != 0 && viewBox.width != 0) {
                transform = transform.preConcatenate(Matrix.getScaleInstance(width / viewBox.width, height / viewBox.height));
            }

            processSvgObject(idMap, shapeNum, shapes, rootElement, transform, style, morphShape, cachedBitmaps, false);
            if (rootElement.hasAttribute("ffdec:objectType")
                    && "morphshape".equals(rootElement.getAttribute("ffdec:objectType"))
                    && applyAnimation(rootElement)) {
                processSvgObject(idMap, shapeNum, shapes2, rootElement, transform, style, morphShape, cachedBitmaps, true);
            }
        } catch (SAXException | IOException | ParserConfigurationException ex) {
            Logger.getLogger(ShapeImporter.class.getName()).log(Level.SEVERE, null, ex);
        }

        shapes.shapeRecords.add(new EndShapeRecord());
        shapes2.shapeRecords.add(new EndShapeRecord());

        if (st instanceof ShapeTag) {
            ShapeTag shape = (ShapeTag) st;
            shape.shapes = shapes;
            if (!fill) {
                shape.updateBounds();
            }
        }
        if (endShape != null) {

            endShape.shapes = shapes2;
            endShape.updateBounds();
        }
        if (st instanceof MorphShapeTag) {
            shapes.updateMorphShapeTag((MorphShapeTag) st, fill);
        }

        st.setModified(true);
        if (endShape != null) {
            endShape.setModified(true);
        }

        return (Tag) st;
    }

    /**
     * Applies animation to the element.
     * @param element Element
     * @return True if animation was applied
     */
    protected boolean applyAnimation(Element element) {
        NodeList nodeList = element.getChildNodes();
        boolean result = false;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                Element childElement = (Element) node;
                if ("animate".equals(childElement.getTagName())) {
                    if (childElement.hasAttribute("attributeName") && childElement.hasAttribute("values")) {
                        String values = childElement.getAttribute("values");
                        String attributeName = childElement.getAttribute("attributeName");
                        if (values.contains(";")) {
                            String[] parts = values.split(";");
                            if (parts.length >= 2) {
                                element.setAttribute(attributeName, parts[1]);
                                result = true;
                            }
                        }
                    }
                } else if ("animateTransform".equals(childElement.getTagName())) {
                    if (childElement.hasAttribute("attributeName")
                            && childElement.hasAttribute("type")
                            && (childElement.hasAttribute("to") || childElement.hasAttribute("values"))) {
                        String type = childElement.getAttribute("type");
                        String additive = childElement.hasAttribute("additive") ? childElement.getAttribute("additive") : "replace";
                        String attributeName = childElement.getAttribute("attributeName");
                        Matrix originalMatrix = Matrix.parseSvgMatrix(element.getAttribute(attributeName), 1, 1);
                        String to = "";
                        if (childElement.hasAttribute("values") && childElement.getAttribute("values").contains(";")) {
                            to = childElement.getAttribute("values").split(";")[1];
                        } else if (childElement.hasAttribute("to")) {
                            to = childElement.getAttribute("to");
                        }
                        String[] toParts = Matrix.parseSvgNumberList(to);

                        Matrix newMatrix = null;
                        switch (type) {
                            case "scale":
                                double scaleX;
                                double scaleY;
                                if (toParts.length == 2) {
                                    scaleX = parseNumber(toParts[0]);
                                    scaleY = parseNumber(toParts[1]);
                                } else if (toParts.length == 1) {
                                    scaleX = parseNumber(toParts[0]);
                                    scaleY = scaleX;
                                } else {
                                    break;
                                }
                                newMatrix = Matrix.getScaleInstance(scaleX, scaleY);
                                break;
                            case "translate":
                                if (toParts.length == 2) {
                                    double translateX = parseNumber(toParts[0]);
                                    double translateY = parseNumber(toParts[1]);
                                    newMatrix = Matrix.getTranslateInstance(translateX, translateY);
                                }
                                break;
                            case "rotate":
                                if (toParts.length == 1 || toParts.length == 3) {
                                    double rotateAngle = parseNumber(toParts[0]);
                                    double tx = 0;
                                    double ty = 0;
                                    if (toParts.length == 3) {
                                        tx = parseNumber(toParts[1]);
                                        ty = parseNumber(toParts[2]);
                                    }
                                    newMatrix = Matrix.getRotateInstance(rotateAngle, tx, ty);
                                }
                                break;
                            case "skewX":
                                if (toParts.length == 1) {
                                    double skewXAngle = parseNumber(toParts[0]);
                                    newMatrix = Matrix.getSkewXInstance(skewXAngle);
                                }
                                break;
                            case "skewY":
                                if (toParts.length == 1) {
                                    double skewYAngle = parseNumber(toParts[0]);
                                    newMatrix = Matrix.getSkewYInstance(skewYAngle);
                                }
                                break;
                        }
                        if (newMatrix != null) {
                            if ("replace".equals(additive)) {
                                element.setAttribute(attributeName, newMatrix.getSvgTransformationString(1, 1));
                            }
                            if ("sum".equals(additive)) {
                                element.setAttribute(attributeName, originalMatrix.concatenate(newMatrix).getSvgTransformationString(1, 1));
                            }
                        }
                    }
                } else {
                    if (applyAnimation(childElement)) {
                        result = true;
                    }
                }
            }

        }
        return result;
    }

    /**
     * Populates IDs.
     * Generates id-element map, because getElementById does not work in some cases (namespaces?)
     * @param el Element
     * @param out Output map
     */
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

    private void processStyle(Element element) {
        String styleSheet = element.getTextContent().trim();
        CssParser cssParser = new CssParser(styleSheet);
        CssSelectorToXPath selectorToXPath = new CssSelectorToXPath();
        Document doc = element.getOwnerDocument();
        try {
            cssParser.stylesheet();
            XPath xPath = XPathFactory.newInstance().newXPath();
            for (int i = 0; i < cssParser.getCountRulesets(); i++) {
                String selector = cssParser.getSelector(i);
                String xPathExpression = selectorToXPath.css2xpath(selector);
                try {
                    NodeList nodeList = (NodeList) xPath.compile(xPathExpression).evaluate(doc, XPathConstants.NODESET);
                    for (int j = 0; j < nodeList.getLength(); j++) {
                        Node node = nodeList.item(j);
                        NamedNodeMap attrs = node.getAttributes();
                        Node styleAttr = attrs.getNamedItem("ffdec-style");
                        if (styleAttr != null) {
                            styleAttr.setNodeValue(styleAttr.getNodeValue() + ";" + "{" + cssParser.getSpecificity(i) + "}" + cssParser.getDeclarations(i));
                            attrs.setNamedItem(styleAttr);
                        } else {
                            Node styleNode = doc.createAttribute("ffdec-style");
                            styleNode.setNodeValue("{" + cssParser.getSpecificity(i) + "}" + cssParser.getDeclarations(i));
                            attrs.setNamedItem(styleNode);
                        }
                    }
                } catch (XPathExpressionException ex) {
                    Logger.getLogger(SvgImporter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException | CssParseException ex) {
            showWarning("CannotParseCSSStyle", "Cannot parse CSS style: " + ex.getMessage());
        }
    }

    private void processDefs(Element element) {
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                String tagName = childElement.getTagName();
                if ("style".equals(tagName)) {
                    processStyle(childElement);
                } else if ("defs".equals(tagName)) {
                    processDefs(childElement);
                }
            }
        }
    }

    private void processSwitch(Element element, Map<String, Element> idMap, int shapeNum, SHAPEWITHSTYLE shapes, Matrix transform, SvgStyle style, boolean morphShape, Map<String, Integer> cachedBitmaps, boolean shape2) {
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                if (childElement.hasAttribute("requiredExtensions") && !childElement.getAttribute("requiredExtensions").isEmpty()) {
                    continue;
                }
                if (childElement.hasAttribute("systemLanguage")) {
                    String systemLanguage = childElement.getAttribute("systemLanguage");
                    if (systemLanguage.equals("en-us") || systemLanguage.equals("en")) {
                        processElement(childElement, idMap, shapeNum, shapes, transform, style, morphShape, cachedBitmaps, shape2);
                        return;
                    }
                    continue;
                }
                processElement(childElement, idMap, shapeNum, shapes, transform, style, morphShape, cachedBitmaps, shape2);
                return;
            }
        }
    }

    private void processElement(Element element, Map<String, Element> idMap, int shapeNum, SHAPEWITHSTYLE shapes, Matrix transform, SvgStyle style, boolean morphShape, Map<String, Integer> cachedBitmaps, boolean shape2) {
        if (element.hasAttribute("requiredExtensions") && !element.getAttribute("requiredExtensions").isEmpty()) {
            return;
        }
        String tagName = element.getTagName();
        SvgStyle newStyle = new SvgStyle(this, idMap, element, cachedBitmaps);
        Matrix m = Matrix.parseSvgMatrix(element.getAttribute("transform"), 1, 1);
        Matrix m2 = m == null ? transform : transform.concatenate(m);
        if ("switch".equals(tagName)) {
            processSwitch(element, idMap, shapeNum, shapes, transform, style, morphShape, cachedBitmaps, shape2);
        } else if ("style".equals(tagName)) {
            processStyle(element);
        } else if ("g".equals(tagName)) {
            processSvgObject(idMap, shapeNum, shapes, element, m2, newStyle, morphShape, cachedBitmaps, shape2);
        } else if ("path".equals(tagName)) {
            processPath(shapeNum, shapes, element, m2, newStyle, morphShape, shape2);
        } else if ("circle".equals(tagName)) {
            processCircle(shapeNum, shapes, element, m2, newStyle, morphShape);
        } else if ("ellipse".equals(tagName)) {
            processEllipse(shapeNum, shapes, element, m2, newStyle, morphShape);
        } else if ("rect".equals(tagName)) {
            processRect(shapeNum, shapes, element, m2, newStyle, morphShape);
        } else if ("line".equals(tagName)) {
            processLine(shapeNum, shapes, element, m2, newStyle, morphShape);
        } else if ("polyline".equals(tagName)) {
            processPolyline(shapeNum, shapes, element, m2, newStyle, morphShape);
        } else if ("polygon".equals(tagName)) {
            processPolygon(shapeNum, shapes, element, m2, newStyle, morphShape);
        } else if ("defs".equals(tagName)) {
            processDefs(element);
        } else if ("title".equals(tagName) || "desc".equals(tagName)
                || "radialGradient".equals(tagName) || "linearGradient".equals(tagName)) {
            // ignore
        } else {
            showWarning(tagName + "tagNotSupported", "The SVG tag '" + tagName + "' is not supported.");
        }
    }

    private void processSvgObject(Map<String, Element> idMap, int shapeNum, SHAPEWITHSTYLE shapes, Element element, Matrix transform, SvgStyle style, boolean morphShape, Map<String, Integer> cachedFills, boolean shape2) {
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element) {
                Element childElement = (Element) childNode;
                processElement(childElement, idMap, shapeNum, shapes, transform, style, morphShape, cachedFills, shape2);
            }
        }
    }

    private void autoCloseFillPath(boolean empty, List<SHAPERECORD> newRecords, int fillStyle, int lineStyle, Point startPoint, Point prevPoint) {
        if (!empty) {
            if (!startPoint.equals(prevPoint)) {
                if (fillStyle != 0) {
                    if (lineStyle != 0) {
                        StyleChangeRecord scr = new StyleChangeRecord();
                        scr.lineStyle = 0; //no line
                        scr.stateLineStyle = true;
                        newRecords.add(scr);
                    }

                    StraightEdgeRecord serclose = new StraightEdgeRecord();
                    Point p = startPoint;
                    serclose.deltaX = (int) Math.round(p.x - prevPoint.x);
                    serclose.deltaY = (int) Math.round(p.y - prevPoint.y);
                    serclose.generalLineFlag = true;
                    serclose.calculateBits();
                    newRecords.add(serclose);

                    if (lineStyle != 0) {
                        StyleChangeRecord scr = new StyleChangeRecord();
                        scr.lineStyle = lineStyle;
                        scr.stateLineStyle = true;
                        newRecords.add(scr);
                    }
                }
            }
        }
    }

    private void processCommands(int shapeNum, SHAPEWITHSTYLE shapes, List<PathCommand> commands, Matrix transform, SvgStyle style, boolean morphShape, boolean shape2) {

        if ("nonzero".equals(style.getFillRule())) {
            SvgFill fill = style.getFill();
            if (fill != null && !(fill instanceof SvgTransparentFill)) {
                if (!shape2 && (shapeTag instanceof DefineShape4Tag)) {
                    DefineShape4Tag shape4 = (DefineShape4Tag) shapeTag;
                    shape4.usesFillWindingRule = true;
                }
                if (shape2 && (endShape instanceof DefineShape4Tag)) {
                    DefineShape4Tag shape4 = (DefineShape4Tag) endShape;
                    shape4.usesFillWindingRule = true;
                }
            }
        }

        Matrix transform2 = transform.preConcatenate(Matrix.getScaleInstance(SWF.unitDivisor));
        Point prevPoint = new Point(0, 0);
        Point startPoint = prevPoint;
        double x0 = 0;
        double y0 = 0;

        StyleChangeRecord scrStyle = getStyleChangeRecord(shapeNum, style, morphShape);
        int fillStyle = morphShape ? scrStyle.fillStyle0 : scrStyle.fillStyle1;
        int lineStyle = scrStyle.lineStyle;
        scrStyle.stateFillStyle0 = true;
        if (!morphShape) {
            scrStyle.stateFillStyle1 = true;
        }
        scrStyle.stateLineStyle = true;
        scrStyle.fillStyle0 = 0;
        if (!morphShape) {
            scrStyle.fillStyle1 = 0;
        }
        scrStyle.lineStyle = 0;

        List<SHAPERECORD> newRecords = new ArrayList<>();

        newRecords.add(scrStyle);

        LINESTYLE2 lineStyle2Obj = null;
        if (shapeNum == 4) {
            lineStyle2Obj = scrStyle.lineStyles.lineStyles2.length < 1 ? null : scrStyle.lineStyles.lineStyles2[0];
            if (lineStyle2Obj != null) {
                lineStyle2Obj.noClose = true;
            }
        }

        boolean empty = true;

        for (PathCommand command : commands) {
            double x = x0;
            double y = y0;
            Point p;

            boolean isRelative = Character.isLowerCase(command.command);
            if (isRelative) {
                throw new Error("processCommands is called with relative command");
            }

            char cmd = command.command;
            switch (cmd) {
                case 'M':
                    autoCloseFillPath(empty, newRecords, fillStyle, lineStyle, startPoint, prevPoint);

                    StyleChangeRecord scr = new StyleChangeRecord();
                    if (fillStyle != 0) {
                        if (morphShape) {
                            scr.stateFillStyle0 = true;
                            scr.fillStyle0 = fillStyle;
                        } else {
                            scr.stateFillStyle1 = true;
                            scr.fillStyle1 = fillStyle;
                        }
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
                    prevPoint = new Point(scr.moveDeltaX, scr.moveDeltaY);
                    scr.stateMoveTo = true;

                    newRecords.add(scr);
                    startPoint = prevPoint;
                    empty = true;
                    break;
                case 'Z':
                    StraightEdgeRecord serz = new StraightEdgeRecord();
                    p = startPoint;
                    serz.deltaX = (int) Math.round(p.x - prevPoint.x);
                    serz.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = new Point(prevPoint.x + serz.deltaX, prevPoint.y + serz.deltaY);
                    serz.generalLineFlag = true;
                    serz.simplify();
                    serz.calculateBits();
                    newRecords.add(serz);
                    if (lineStyle2Obj != null) {
                        lineStyle2Obj.noClose = false;
                    }
                    empty = true;
                    break;
                case 'L':
                    StraightEdgeRecord serl = new StraightEdgeRecord();
                    x = command.params[0];
                    y = command.params[1];

                    p = transform2.transform(x, y);
                    serl.deltaX = (int) Math.round(p.x - prevPoint.x);
                    serl.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = new Point(prevPoint.x + serl.deltaX, prevPoint.y + serl.deltaY);
                    serl.generalLineFlag = true;
                    serl.simplify();
                    serl.calculateBits();
                    newRecords.add(serl);
                    empty = false;
                    break;
                case 'H':
                    StraightEdgeRecord serh = new StraightEdgeRecord();
                    x = command.params[0];

                    p = transform2.transform(x, y);
                    serh.deltaX = (int) Math.round(p.x - prevPoint.x);
                    //deltaX is not enough as transformation can make deltaY difference
                    serh.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = new Point(prevPoint.x + serh.deltaX, prevPoint.y + serh.deltaY);
                    serh.generalLineFlag = true;
                    serh.simplify();
                    serh.calculateBits();
                    newRecords.add(serh);
                    empty = false;
                    break;
                case 'V':
                    StraightEdgeRecord serv = new StraightEdgeRecord();
                    y = command.params[0];

                    p = transform2.transform(x, y);

                    //deltaY is not enough as transformation can make deltaX difference
                    serv.deltaX = (int) Math.round(p.x - prevPoint.x);
                    serv.deltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = new Point(prevPoint.x + serv.deltaX, prevPoint.y + serv.deltaY);
                    serv.generalLineFlag = true;
                    serv.simplify();
                    serv.calculateBits();
                    newRecords.add(serv);
                    empty = false;
                    break;
                case 'Q':
                    CurvedEdgeRecord cer = new CurvedEdgeRecord();
                    x = command.params[0];
                    y = command.params[1];

                    p = transform2.transform(x, y);
                    cer.controlDeltaX = (int) Math.round(p.x - prevPoint.x);
                    cer.controlDeltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = new Point(prevPoint.x + cer.controlDeltaX, prevPoint.y + cer.controlDeltaY);

                    x = command.params[2];
                    y = command.params[3];

                    p = transform2.transform(x, y);
                    cer.anchorDeltaX = (int) Math.round(p.x - prevPoint.x);
                    cer.anchorDeltaY = (int) Math.round(p.y - prevPoint.y);
                    prevPoint = new Point(prevPoint.x + cer.anchorDeltaX, prevPoint.y + cer.anchorDeltaY);
                    cer.calculateBits();
                    newRecords.add(cer);
                    empty = false;
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

                    List<Double> quadCoordinates = new CubicToQuad().cubicToQuad(pStart.x, pStart.y, pControl1.x, pControl1.y, pControl2.x, pControl2.y, p.x, p.y, 1);
                    for (int i = 2; i < quadCoordinates.size();) {
                        CurvedEdgeRecord cerc = new CurvedEdgeRecord();
                        p = new Point(quadCoordinates.get(i++), quadCoordinates.get(i++));
                        cerc.controlDeltaX = (int) Math.round(p.x - prevPoint.x);
                        cerc.controlDeltaY = (int) Math.round(p.y - prevPoint.y);
                        prevPoint = new Point(prevPoint.x + cerc.controlDeltaX, prevPoint.y + cerc.controlDeltaY);

                        p = new Point(quadCoordinates.get(i++), quadCoordinates.get(i++));
                        cerc.anchorDeltaX = (int) Math.round(p.x - prevPoint.x);
                        cerc.anchorDeltaY = (int) Math.round(p.y - prevPoint.y);
                        prevPoint = new Point(prevPoint.x + cerc.anchorDeltaX, prevPoint.y + cerc.anchorDeltaY);
                        cerc.calculateBits();
                        newRecords.add(cerc);
                    }

                    empty = false;
                    break;
                default:
                    Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, "Unknown command: {0}", command);
                    return;
            }

            x0 = x;
            y0 = y;
        }
        autoCloseFillPath(empty, newRecords, fillStyle, lineStyle, startPoint, prevPoint);
        applyStyleGradients(SHAPERECORD.getBounds(newRecords, shapes.lineStyles, shapeNum, false), scrStyle, transform2, shapeNum, style);
        shapes.shapeRecords.addAll(newRecords);
    }

    private List<PathCommand> processPathD(String data) {
        char command = 0;
        Point startPoint = new Point(0, 0);
        Point prevCControlPoint = null;
        Point prevQControlPoint = null;
        double x0 = 0;
        double y0 = 0;

        List<PathCommand> pathCommands = new ArrayList<>();
        SvgPathReader pathReader = new SvgPathReader(data);
        try {
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
                        return new ArrayList<>();
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
        } catch (NumberFormatException e) {
            // ignore remaining data as specified in SVG Specification F.2 Error processing
        }
        return pathCommands;
    }

    private void processPath(int shapeNum, SHAPEWITHSTYLE shapes, Element element, Matrix transform, SvgStyle style, boolean morphShape, boolean shape2) {
        String data = element.getAttribute("d");
        List<PathCommand> pathCommands = processPathD(data);
        processCommands(shapeNum, shapes, pathCommands, transform, style, morphShape, shape2);
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

    private void processCircle(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style, boolean morphShape) {
        String attr = childElement.getAttribute("cx");
        double cx = attr.length() > 0 ? parseCoordinate(attr, viewBox.width) : 0;

        attr = childElement.getAttribute("cy");
        double cy = attr.length() > 0 ? parseCoordinate(attr, viewBox.height) : 0;

        attr = childElement.getAttribute("r");
        double r = attr.length() > 0 ? parseLength(attr, (viewBox.width + viewBox.height) / 2) : 0;

        processEllipse(shapeNum, shapes, transform, style, cx, cy, r, r, morphShape);
    }

    private void processEllipse(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style, boolean morphShape) {
        String attr = childElement.getAttribute("cx");
        double cx = attr.length() > 0 ? parseCoordinate(attr, viewBox.width) : 0;

        attr = childElement.getAttribute("cy");
        double cy = attr.length() > 0 ? parseCoordinate(attr, viewBox.height) : 0;

        attr = childElement.getAttribute("rx");
        double rx = attr.length() > 0 ? parseLength(attr, viewBox.width) : 0;

        attr = childElement.getAttribute("ry");
        double ry = attr.length() > 0 ? parseLength(attr, viewBox.height) : 0;

        processEllipse(shapeNum, shapes, transform, style, cx, cy, rx, ry, morphShape);
    }

    private void processEllipse(int shapeNum, SHAPEWITHSTYLE shapes, Matrix transform, SvgStyle style, double cx, double cy, double rx, double ry, boolean morphShape) {
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

        PathCommand serz = new PathCommand();
        serz.command = 'Z';
        pathCommands.add(serz);

        processCommands(shapeNum, shapes, pathCommands, transform, style, morphShape, false);
    }

    private void processRect(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style, boolean morphShape) {
        String attr = childElement.getAttribute("x");
        double x = attr.length() > 0 ? parseCoordinate(attr, viewBox.width) : 0;

        attr = childElement.getAttribute("y");
        double y = attr.length() > 0 ? parseCoordinate(attr, viewBox.height) : 0;

        attr = childElement.getAttribute("width");
        double width = attr.length() > 0 ? parseLength(attr, viewBox.width) : 0;

        attr = childElement.getAttribute("height");
        double height = attr.length() > 0 ? parseLength(attr, viewBox.height) : 0;

        attr = childElement.getAttribute("rx");
        double rx = attr.length() > 0 ? parseLength(attr, viewBox.width) : 0;

        attr = childElement.getAttribute("ry");
        double ry = attr.length() > 0 ? parseLength(attr, viewBox.height) : 0;

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

        PathCommand serz = new PathCommand();
        serz.command = 'Z';
        pathCommands.add(serz);

        processCommands(shapeNum, shapes, pathCommands, transform, style, morphShape, false);
    }

    private void processLine(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style, boolean morphShape) {
        String attr = childElement.getAttribute("x1");
        double x1 = attr.length() > 0 ? parseCoordinate(attr, viewBox.width) : 0;

        attr = childElement.getAttribute("y1");
        double y1 = attr.length() > 0 ? parseCoordinate(attr, viewBox.height) : 0;

        attr = childElement.getAttribute("x2");
        double x2 = attr.length() > 0 ? parseCoordinate(attr, viewBox.width) : 0;

        attr = childElement.getAttribute("y2");
        double y2 = attr.length() > 0 ? parseCoordinate(attr, viewBox.height) : 0;

        List<PathCommand> pathCommands = new ArrayList<>();
        PathCommand scr = new PathCommand();
        scr.command = 'M';
        scr.params = new double[]{x1, y1};
        pathCommands.add(scr);

        PathCommand cer = new PathCommand();
        cer.command = 'L';
        cer.params = new double[]{x2, y2};

        pathCommands.add(cer);

        processCommands(shapeNum, shapes, pathCommands, transform, style, morphShape, false);
    }

    private void processPolygon(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style, boolean morphShape) {
        processPolyline(shapeNum, shapes, childElement, transform, style, true, morphShape);
    }

    private void processPolyline(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style, boolean morphShape) {
        processPolyline(shapeNum, shapes, childElement, transform, style, false, morphShape);
    }

    private void processPolyline(int shapeNum, SHAPEWITHSTYLE shapes, Element childElement, Matrix transform, SvgStyle style, boolean close, boolean morphShape) {
        String data = childElement.getAttribute("points");

        char command = 'M';
        double x0 = 0;
        double y0 = 0;

        List<PathCommand> pathCommands = new ArrayList<>();
        SvgPathReader pathReader = new SvgPathReader(data);
        try {
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
        } catch (NumberFormatException e) {
            // ignore remaining data as specified in SVG Specification F.2 Error processing
        }

        if (close) {
            PathCommand serz = new PathCommand();
            serz.command = 'Z';
            pathCommands.add(serz);
        }

        processCommands(shapeNum, shapes, pathCommands, transform, style, morphShape, false);
    }

    //Stub for w3 test. TODO: refactor and move to test directory. It's here because of easy access - compiling single file
    private static void svgTest(String name) throws IOException, InterruptedException {
        System.err.println("running test " + name);
        if (!new File(name + ".original.svg").exists()) {
            URL svgUrl = new URL("http://www.w3.org/Graphics/SVG/Test/20061213/svggen/" + name + ".svg");
            byte[] svgData = Helper.readStream(svgUrl.openStream());
            Helper.writeFile(name + ".orig.svg", svgData);

            URL pngUrl = new URL("http://www.w3.org/Graphics/SVG/Test/20061213/png/full-" + name + ".png");
            byte[] pngData = Helper.readStream(pngUrl.openStream());
            Helper.writeFile(name + ".orig.png", pngData);
        }

        String svgDataS = Helper.readTextFile(name + ".orig.svg");
        SWF swf = new SWF();
        DefineShape4Tag st = new DefineShape4Tag(swf);
        st = (DefineShape4Tag) (new SvgImporter().importSvg(st, svgDataS, false));
        swf.addTag(st);
        SerializableImage si = new SerializableImage(480, 360, BufferedImage.TYPE_4BYTE_ABGR);
        BitmapExporter.export(st.getWindingRule(), st.getShapeNum(), swf, st.shapes, Color.yellow, si, 1, new Matrix(), new Matrix(), null, true, true);
        List<Tag> li = new ArrayList<>();
        li.add(st);
        ImageIO.write(si.getBufferedImage(), "PNG", new File(name + ".imported.png"));
        ExportAssetsTag eat = new ExportAssetsTag(swf);
        eat.tags.add(st.getCharacterId());
        eat.names.add(name);
        swf.addTag(eat);
        swf.assignExportNamesToSymbols();
        st.shapeBounds.Xmax = (int) (si.getWidth() * SWF.unitDivisor);
        st.shapeBounds.Ymax = (int) (si.getHeight() * SWF.unitDivisor);
        new ShapeExporter().exportShapes(null, "./outex/", swf, new ReadOnlyTagList(li), new ShapeExportSettings(ShapeExportMode.SVG, 1), null, 1);
    }

    /**
     * Test for SVG.
     * @param args The command line arguments
     * @throws IOException On I/O error
     * @throws InterruptedException On interrupt
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        //svgTest("animate-elem-02-t");
        //svgTest("animate-elem-03-t");
        //svgTest("animate-elem-04-t");
        //svgTest("animate-elem-05-t");
        //svgTest("animate-elem-06-t");
        //svgTest("animate-elem-07-t");
        //svgTest("animate-elem-08-t");
        //svgTest("animate-elem-09-t");
        //svgTest("animate-elem-10-t");
        //svgTest("animate-elem-11-t");
        //svgTest("animate-elem-12-t");
        //svgTest("animate-elem-13-t");
        //svgTest("animate-elem-14-t");
        //svgTest("animate-elem-15-t");
        //svgTest("animate-elem-17-t");
        //svgTest("animate-elem-19-t");
        //svgTest("animate-elem-20-t");
        //svgTest("animate-elem-21-t");
        //svgTest("animate-elem-22-b");
        //svgTest("animate-elem-23-t");
        //svgTest("animate-elem-24-t");
        //svgTest("animate-elem-25-t");
        //svgTest("animate-elem-26-t");
        //svgTest("animate-elem-27-t");
        //svgTest("animate-elem-28-t");
        //svgTest("animate-elem-29-b");
        //svgTest("animate-elem-30-t");
        //svgTest("animate-elem-31-t");
        //svgTest("animate-elem-32-t");
        //svgTest("animate-elem-33-t");
        //svgTest("animate-elem-34-t");
        //svgTest("animate-elem-36-t");
        //svgTest("animate-elem-37-t");
        //svgTest("animate-elem-39-t");
        //svgTest("animate-elem-40-t");
        //svgTest("animate-elem-41-t");
        //svgTest("animate-elem-44-t");
        //svgTest("animate-elem-46-t");
        //svgTest("animate-elem-52-t");
        //svgTest("animate-elem-60-t");
        //svgTest("animate-elem-61-t");
        //svgTest("animate-elem-62-t");
        //svgTest("animate-elem-63-t");
        //svgTest("animate-elem-64-t");
        //svgTest("animate-elem-65-t");
        //svgTest("animate-elem-66-t");
        //svgTest("animate-elem-67-t");
        //svgTest("animate-elem-68-t");
        //svgTest("animate-elem-69-t");
        //svgTest("animate-elem-70-t");
        //svgTest("animate-elem-77-t");
        //svgTest("animate-elem-78-t");
        //svgTest("animate-elem-80-t");
        //svgTest("animate-elem-81-t");
        //svgTest("animate-elem-82-t");
        //svgTest("animate-elem-83-t");
        //svgTest("animate-elem-84-t");
        //svgTest("animate-elem-85-t");
        svgTest("color-prof-01-f");
        svgTest("color-prop-01-b");
        svgTest("color-prop-02-f");
        svgTest("color-prop-03-t");
        svgTest("coords-coord-01-t");
        svgTest("coords-coord-02-t");
        svgTest("coords-trans-01-b");
        svgTest("coords-trans-02-t");
        svgTest("coords-trans-03-t");
        svgTest("coords-trans-04-t");
        svgTest("coords-trans-05-t");
        svgTest("coords-trans-06-t");
        svgTest("coords-units-01-b");
        svgTest("coords-units-02-b");
        svgTest("coords-units-03-b");
        svgTest("coords-viewattr-01-b");
        svgTest("coords-viewattr-02-b");
        svgTest("coords-viewattr-03-b");
        svgTest("extend-namespace-01-f");
        //svgTest("filters-blend-01-b");
        //svgTest("filters-color-01-b");
        //svgTest("filters-composite-02-b");
        //svgTest("filters-comptran-01-b");
        //svgTest("filters-conv-01-f");
        //svgTest("filters-diffuse-01-f");
        //svgTest("filters-displace-01-f");
        //svgTest("filters-example-01-b");
        //svgTest("filters-felem-01-b");
        //svgTest("filters-gauss-01-b");
        //svgTest("filters-image-01-b");
        //svgTest("filters-light-01-f");
        //svgTest("filters-morph-01-f");
        //svgTest("filters-offset-01-b");
        //svgTest("filters-specular-01-f");
        //svgTest("filters-tile-01-b");
        //svgTest("filters-turb-01-f");
        //svgTest("fonts-desc-02-t");
        //svgTest("fonts-elem-01-t");
        //svgTest("fonts-elem-02-t");
        //svgTest("fonts-elem-03-b");
        //svgTest("fonts-elem-04-b");
        //svgTest("fonts-elem-05-t");
        //svgTest("fonts-elem-06-t");
        //svgTest("fonts-elem-07-b");
        //svgTest("fonts-glyph-02-t");
        //svgTest("fonts-glyph-03-t");
        //svgTest("fonts-glyph-04-t");
        //svgTest("fonts-kern-01-t");
        //svgTest("interact-cursor-01-f");
        //svgTest("interact-dom-01-b");
        //svgTest("interact-events-01-b");
        //svgTest("interact-order-01-b");
        //svgTest("interact-order-02-b");
        //svgTest("interact-order-03-b");
        //svgTest("interact-zoom-01-t");
        //svgTest("linking-a-01-b");
        //svgTest("linking-a-02-b");
        //svgTest("linking-a-03-b");
        //svgTest("linking-a-04-t");
        //svgTest("linking-a-05-t");
        //svgTest("linking-a-07-t");
        //svgTest("linking-uri-01-b");
        //svgTest("linking-uri-02-b");
        //svgTest("linking-uri-03-t");
        //svgTest("masking-intro-01-f");
        //svgTest("masking-mask-01-b");
        //svgTest("masking-opacity-01-b");
        //svgTest("masking-path-01-b");
        //svgTest("masking-path-02-b");
        //svgTest("masking-path-03-b");
        //svgTest("masking-path-04-b");
        //svgTest("masking-path-05-f");
        //svgTest("metadata-example-01-b");
        svgTest("painting-fill-01-t");
        svgTest("painting-fill-02-t");
        svgTest("painting-fill-03-t");
        svgTest("painting-fill-04-t");
        svgTest("painting-fill-05-b");
        svgTest("painting-marker-01-f");
        svgTest("painting-marker-02-f");
        svgTest("painting-marker-03-f");
        svgTest("painting-render-01-b");
        svgTest("painting-stroke-01-t");
        svgTest("painting-stroke-02-t");
        svgTest("painting-stroke-03-t");
        svgTest("painting-stroke-04-t");
        svgTest("painting-stroke-07-t");
        svgTest("paths-data-01-t");
        svgTest("paths-data-02-t");
        svgTest("paths-data-03-f");
        svgTest("paths-data-04-t");
        svgTest("paths-data-05-t");
        svgTest("paths-data-06-t");
        svgTest("paths-data-07-t");
        svgTest("paths-data-08-t");
        svgTest("paths-data-09-t");
        svgTest("paths-data-10-t");
        svgTest("paths-data-12-t");
        svgTest("paths-data-13-t");
        svgTest("paths-data-14-t");
        svgTest("paths-data-15-t");
        svgTest("pservers-grad-01-b");
        svgTest("pservers-grad-02-b");
        svgTest("pservers-grad-03-b");
        svgTest("pservers-grad-04-b");
        svgTest("pservers-grad-05-b");
        svgTest("pservers-grad-06-b");
        svgTest("pservers-grad-07-b");
        svgTest("pservers-grad-08-b");
        svgTest("pservers-grad-09-b");
        svgTest("pservers-grad-10-b");
        svgTest("pservers-grad-11-b");
        svgTest("pservers-grad-12-b");
        svgTest("pservers-grad-13-b");
        svgTest("pservers-grad-14-b");
        svgTest("pservers-grad-15-b");
        svgTest("pservers-grad-16-b");
        svgTest("pservers-grad-17-b");
        svgTest("pservers-grad-18-b");
        svgTest("pservers-grad-19-b");
        svgTest("pservers-pattern-01-b");
        svgTest("render-elems-01-t");
        svgTest("render-elems-02-t");
        svgTest("render-elems-03-t");
        svgTest("render-elems-06-t");
        svgTest("render-elems-07-t");
        svgTest("render-elems-08-t");
        svgTest("render-groups-01-b");
        svgTest("render-groups-03-t");
        //svgTest("script-handle-01-b");
        //svgTest("script-handle-02-b");
        //svgTest("script-handle-03-b");
        //svgTest("script-handle-04-b");
        svgTest("shapes-circle-01-t");
        svgTest("shapes-circle-02-t");
        svgTest("shapes-ellipse-01-t");
        svgTest("shapes-ellipse-02-t");
        svgTest("shapes-intro-01-t");
        svgTest("shapes-line-01-t");
        svgTest("shapes-polygon-01-t");
        svgTest("shapes-polyline-01-t");
        svgTest("shapes-rect-01-t");
        svgTest("shapes-rect-02-t");
        //svgTest("struct-cond-01-t");
        //svgTest("struct-cond-02-t");
        //svgTest("struct-cond-03-t");
        //svgTest("struct-defs-01-t");
        //svgTest("struct-dom-01-b");
        //svgTest("struct-dom-02-b");
        //svgTest("struct-dom-03-b");
        //svgTest("struct-dom-04-b");
        //svgTest("struct-dom-05-b");
        //svgTest("struct-dom-06-b");
        //svgTest("struct-frag-01-t");
        //svgTest("struct-frag-02-t");
        //svgTest("struct-frag-03-t");
        //svgTest("struct-frag-04-t");
        //svgTest("struct-frag-05-t");
        //svgTest("struct-frag-06-t");
        //svgTest("struct-group-01-t");
        //svgTest("struct-group-02-b");
        //svgTest("struct-group-03-t");
        //svgTest("struct-image-01-t");
        //svgTest("struct-image-02-b");
        //svgTest("struct-image-03-t");
        //svgTest("struct-image-04-t");
        //svgTest("struct-image-05-b");
        //svgTest("struct-image-06-t");
        //svgTest("struct-image-07-t");
        //svgTest("struct-image-08-t");
        //svgTest("struct-image-09-t");
        //svgTest("struct-image-10-t");
        //svgTest("struct-symbol-01-b");
        //svgTest("struct-use-01-t");
        //svgTest("struct-use-03-t");
        //svgTest("struct-use-05-b");
        svgTest("styling-css-01-b");
        svgTest("styling-css-02-b");
        svgTest("styling-css-03-b");
        svgTest("styling-css-04-f");
        //svgTest("styling-css-05-b");
        //svgTest("styling-css-06-b");
        svgTest("styling-inherit-01-b");
        //svgTest("styling-pres-01-t");
        //svgTest("text-align-01-b");
        //svgTest("text-align-02-b");
        //svgTest("text-align-03-b");
        //svgTest("text-align-04-b");
        //svgTest("text-align-05-b");
        //svgTest("text-align-06-b");
        //svgTest("text-align-08-b");
        //svgTest("text-altglyph-01-b");
        //svgTest("text-deco-01-b");
        //svgTest("text-fonts-01-t");
        //svgTest("text-fonts-02-t");
        //svgTest("text-fonts-03-t");
        //svgTest("text-intro-01-t");
        //svgTest("text-intro-02-b");
        //svgTest("text-intro-03-b");
        //svgTest("text-intro-04-t");
        //svgTest("text-intro-05-t");
        //svgTest("text-path-01-b");
        //svgTest("text-spacing-01-b");
        //svgTest("text-text-01-b");
        //svgTest("text-text-03-b");
        //svgTest("text-text-04-t");
        //svgTest("text-text-05-t");
        //svgTest("text-text-06-t");
        //svgTest("text-text-07-t");
        //svgTest("text-text-08-b");
        //svgTest("text-tref-01-b");
        //svgTest("text-tselect-01-b");
        //svgTest("text-tselect-02-f");
        //svgTest("text-tspan-01-b");
        //svgTest("text-ws-01-t");
        //svgTest("text-ws-02-t");
        //svgTest("types-basicDOM-01-b");            
    }

    private double angle(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            return 0;
        }
        return Math.atan2(dy, dx);
    }

    private void applyFillGradients(SvgFill fill, FILLSTYLE fillStyle, RECT bounds, StyleChangeRecord scr, Matrix transform, int shapeNum, SvgStyle style) {
        if (fill == null || fillStyle == null) {
            return;
        }
        if (fill instanceof SvgGradient) {
            SvgGradient gfill = (SvgGradient) fill;
            Matrix gradientMatrix = Matrix.parseSvgMatrix(gfill.gradientTransform, SWF.unitDivisor, 1);
            gradientMatrix = transform.concatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor)).concatenate(gradientMatrix);
            fillStyle.gradientMatrix = gradientMatrix.toMATRIX();
            if (fill instanceof SvgLinearGradient) {
                SvgLinearGradient lgfill = (SvgLinearGradient) fill;
                fillStyle.fillStyleType = FILLSTYLE.LINEAR_GRADIENT;
                fillStyle.gradient = new GRADIENT();
                double x1 = parseCoordinate(lgfill.x1, 1);
                double y1 = parseCoordinate(lgfill.y1, 1);
                double x2 = parseCoordinate(lgfill.x2, 1);
                double y2 = parseCoordinate(lgfill.y2, 1);

                Matrix tMatrix = new Matrix();
                if (lgfill.gradientUnits == SvgGradientUnits.OBJECT_BOUNDING_BOX) {
                    Matrix xyMatrix = new Matrix();
                    xyMatrix.scaleX = (x2 - x1) * SWF.unitDivisor;
                    xyMatrix.rotateSkew0 = (y2 - y1) * SWF.unitDivisor;
                    xyMatrix.rotateSkew1 = -xyMatrix.rotateSkew0;
                    xyMatrix.scaleY = xyMatrix.scaleX;

                    Matrix gmatrix = new Matrix();

                    gmatrix.scaleX = (bounds.Xmax - bounds.Xmin) / SWF.unitDivisor;
                    gmatrix.rotateSkew0 = 0;
                    gmatrix.rotateSkew1 = 0;
                    gmatrix.scaleY = (bounds.Ymax - bounds.Ymin) / SWF.unitDivisor;
                    gmatrix.translateX = bounds.Xmin;
                    gmatrix.translateY = bounds.Ymin;
                    x1 *= bounds.getWidth();
                    y1 *= bounds.getHeight();

                    Matrix zeroStartMatrix = Matrix.getTranslateInstance(0.5, 0);
                    Matrix scaleMatrix = Matrix.getScaleInstance(1 / 16384.0 / 2);
                    Matrix transMatrix = Matrix.getTranslateInstance(x1, y1);

                    tMatrix = tMatrix.concatenate(transMatrix)
                            .concatenate(gmatrix)
                            .concatenate(xyMatrix)
                            .concatenate(zeroStartMatrix)
                            .concatenate(scaleMatrix);
                } else {

                    x1 *= SWF.unitDivisor;
                    y1 *= SWF.unitDivisor;
                    x2 *= SWF.unitDivisor;
                    y2 *= SWF.unitDivisor;

                    Point a = new Point(-16384.0, 0.0);
                    Point b = new Point(16384.0, 0.0);
                    Point c = new Point(x1, y1);
                    Point d = new Point(x2, y2);

                    if (!(a.equals(c) && b.equals(d))) {
                        double AdeltaX = b.x - a.x;
                        double AdeltaY = b.y - a.y;

                        double BdeltaX = d.x - c.x;
                        double BdeltaY = d.y - c.y;

                        double lenAB = Math.sqrt(AdeltaX * AdeltaX + AdeltaY * AdeltaY);
                        double lenCD = Math.sqrt(BdeltaX * BdeltaX + BdeltaY * BdeltaY);

                        double rotation = angle(c.x, c.y, d.x, d.y) - angle(a.x, a.y, b.x, b.y);

                        double scale = lenCD / lenAB;

                        tMatrix = tMatrix
                                .concatenate(gradientMatrix)
                                .concatenate(Matrix.getTranslateInstance(c.x, c.y))
                                .concatenate(Matrix.getRotateInstance(rotation * 180 / Math.PI))
                                .concatenate(Matrix.getScaleInstance(scale))
                                .concatenate(Matrix.getTranslateInstance(-a.x, -a.y));
                    }
                }
                fillStyle.gradientMatrix = tMatrix.toMATRIX();
            } else if (fill instanceof SvgRadialGradient) {
                SvgRadialGradient rgfill = (SvgRadialGradient) fill;
                double cx = parseCoordinate(rgfill.cx, 1);
                double cy = parseCoordinate(rgfill.cy, 1);
                double r = parseLength(rgfill.r, 1);

                Matrix gmatrix;
                if (rgfill.gradientUnits == SvgGradientUnits.OBJECT_BOUNDING_BOX) {
                    gmatrix = new Matrix();
                    gmatrix.scaleX = (bounds.Xmax - bounds.Xmin) / SWF.unitDivisor;
                    gmatrix.rotateSkew0 = 0;
                    gmatrix.rotateSkew1 = 0;
                    gmatrix.scaleY = 1 / SWF.unitDivisor;
                    gmatrix.scaleY = (bounds.Ymax - bounds.Ymin) / SWF.unitDivisor;
                    gmatrix.translateX = bounds.Xmin;
                    gmatrix.translateY = bounds.Ymin;
                } else {
                    gmatrix = new Matrix(fillStyle.gradientMatrix);
                }
                gmatrix.translate(SWF.unitDivisor * cx, SWF.unitDivisor * cy);
                gmatrix = gmatrix.concatenate(Matrix.getScaleInstance(r / 819.2));

                fillStyle.gradientMatrix = gmatrix.toMATRIX();

                double fx = parseCoordinate(rgfill.fx, 1);
                double fy = parseCoordinate(rgfill.fy, 1);
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

            int prevRatio = -1;

            int recCount = 0;
            for (int i = 0; i < gfill.stops.size(); i++) {
                SvgStop stop = gfill.stops.get(i);

                int ratio = Math.max((int) Math.round(stop.offset * 255), prevRatio + 1);
                recCount++;
                //two finish stops
                if (ratio == 255 && i + 1 < gfill.stops.size()) {
                    if (prevRatio == 254) {
                        break;
                    }
                    ratio = 254;
                }
                prevRatio = ratio;
                if (prevRatio == 255) {
                    break;
                }
            }

            prevRatio = -1;
            fillStyle.gradient.gradientRecords = new GRADRECORD[recCount];
            for (int i = 0; i < recCount; i++) {
                SvgStop stop = gfill.stops.get(i);
                Color color = stop.color;
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) Math.round(color.getAlpha() * style.getOpacity()));
                fillStyle.gradient.gradientRecords[i] = new GRADRECORD();
                fillStyle.gradient.gradientRecords[i].inShape3 = shapeNum >= 3;
                fillStyle.gradient.gradientRecords[i].color = getRGB(shapeNum, color);
                int ratio = Math.max((int) Math.round(stop.offset * 255), prevRatio + 1);
                if (ratio == 255 && i + 1 < recCount) {
                    ratio = 254;
                }
                fillStyle.gradient.gradientRecords[i].ratio = ratio;
                prevRatio = ratio;
            }
        } else if (fill instanceof SvgBitmapFill) {
            SvgBitmapFill bfill = (SvgBitmapFill) fill;
            fillStyle.fillStyleType = bfill.smoothed ? FILLSTYLE.REPEATING_BITMAP : FILLSTYLE.NON_SMOOTHED_REPEATING_BITMAP;
            fillStyle.bitmapId = bfill.characterId;
            Matrix fillMatrix = Matrix.parseSvgMatrix(bfill.patternTransform, SWF.unitDivisor, SWF.unitDivisor);
            fillMatrix = transform.concatenate(Matrix.getScaleInstance(1 / SWF.unitDivisor)).concatenate(fillMatrix);
            fillStyle.bitmapMatrix = fillMatrix.toMATRIX();
        }
    }

    private void applyStyleGradients(RECT bounds, StyleChangeRecord scr, Matrix transform, int shapeNum, SvgStyle style) {
        SvgFill fill = style.getFillWithOpacity();
        if (fill != null && fill != SvgTransparentFill.INSTANCE) {
            applyFillGradients(fill, scr.fillStyles.fillStyles[0], bounds, scr, transform, shapeNum, style);
        }
        SvgFill strokeFill = style.getStrokeFillWithOpacity();
        if (strokeFill != null) {
            if (shapeNum == 4 && scr.lineStyles.lineStyles2.length > 0 && scr.lineStyles.lineStyles2[0] instanceof LINESTYLE2) {
                applyFillGradients(strokeFill, ((LINESTYLE2) scr.lineStyles.lineStyles2[0]).fillType, bounds, scr, transform, shapeNum, style);
            }
        }
    }

    private StyleChangeRecord getStyleChangeRecord(int shapeNum, SvgStyle style, boolean morphShape) {
        StyleChangeRecord scr = new StyleChangeRecord();

        scr.stateNewStyles = true;
        scr.fillStyles = new FILLSTYLEARRAY();
        if (morphShape) {
            scr.stateFillStyle0 = true;
        } else {
            scr.stateFillStyle1 = true;
        }
        scr.stateLineStyle = true;
        SvgFill fill = style.getFillWithOpacity();
        if (fill != null && fill != SvgTransparentFill.INSTANCE) {
            scr.fillStyles.fillStyles = new FILLSTYLE[1];
            scr.fillStyles.fillStyles[0] = new FILLSTYLE();
            if (fill instanceof SvgColor) {
                Color colorFill = fill.toColor();
                scr.fillStyles.fillStyles[0].color = getRGB(shapeNum, colorFill);
                scr.fillStyles.fillStyles[0].fillStyleType = FILLSTYLE.SOLID;
            } else if (fill instanceof SvgGradient) {
                //...apply in second step - applyStyleGradients
            }

            if (morphShape) {
                scr.fillStyle0 = 1;
            } else {
                scr.fillStyle1 = 1;
            }
        } else {
            scr.fillStyles.fillStyles = new FILLSTYLE[0];
            if (morphShape) {
                scr.fillStyle0 = 0;
            } else {
                scr.fillStyle1 = 0;
            }
        }

        scr.lineStyles = new LINESTYLEARRAY();
        SvgFill strokeFill = style.getStrokeFillWithOpacity();
        if (strokeFill != null && strokeFill != SvgTransparentFill.INSTANCE) {
            Color lineColor = strokeFill.toColor();

            ILINESTYLE lineStyle = shapeNum <= 3 ? new LINESTYLE() : new LINESTYLE2();
            lineStyle.setColor(getRGB(shapeNum, lineColor));
            lineStyle.setWidth((int) Math.round(style.getStrokeWidth() * SWF.unitDivisor));
            SvgLineCap lineCap = style.getStrokeLineCap();
            SvgLineJoin lineJoin = style.getStrokeLineJoin();
            if (lineStyle instanceof LINESTYLE2) {
                LINESTYLE2 lineStyle2 = (LINESTYLE2) lineStyle;

                String vectorEffect = style.getVectorEffect();
                if ("non-scaling-stroke".equals(vectorEffect)) {
                    lineStyle2.noHScaleFlag = true;
                    lineStyle2.noVScaleFlag = true;
                    if (shapeTag instanceof DefineShape4Tag) {
                        DefineShape4Tag shape4 = (DefineShape4Tag) shapeTag;
                        shape4.usesNonScalingStrokes = true;
                    }
                    if (shapeTag instanceof DefineMorphShape2Tag) {
                        DefineMorphShape2Tag morph2 = (DefineMorphShape2Tag) shapeTag;
                        morph2.usesNonScalingStrokes = true;
                    }
                } else {
                    if (shapeTag instanceof DefineShape4Tag) {
                        DefineShape4Tag shape4 = (DefineShape4Tag) shapeTag;
                        shape4.usesScalingStrokes = true;
                    }
                    if (shapeTag instanceof DefineMorphShape2Tag) {
                        DefineMorphShape2Tag morph2 = (DefineMorphShape2Tag) shapeTag;
                        morph2.usesScalingStrokes = true;
                    }
                }

                int swfCap = lineCap == SvgLineCap.BUTT ? LINESTYLE2.NO_CAP
                        : lineCap == SvgLineCap.ROUND ? LINESTYLE2.ROUND_CAP
                                : lineCap == SvgLineCap.SQUARE ? LINESTYLE2.SQUARE_CAP : 0;
                lineStyle2.startCapStyle = swfCap;
                lineStyle2.endCapStyle = swfCap;
                if (!(strokeFill instanceof SvgColor)) {
                    lineStyle2.hasFillFlag = true;
                    lineStyle2.fillType = new FILLSTYLE();
                    //...apply in second step - applyStyleGradients
                } // Single color does not need fillType attribute

                int swfJoin = lineJoin == SvgLineJoin.MITER ? LINESTYLE2.MITER_JOIN
                        : lineJoin == SvgLineJoin.ROUND ? LINESTYLE2.ROUND_JOIN
                                : lineJoin == SvgLineJoin.BEVEL ? LINESTYLE2.BEVEL_JOIN : 0;
                lineStyle2.joinStyle = swfJoin;
                lineStyle2.miterLimitFactor = (float) style.getStrokeMiterLimit();
                scr.lineStyles.lineStyles2 = new LINESTYLE2[1];
                scr.lineStyles.lineStyles2[0] = lineStyle2;
            } else {
                if (lineCap != SvgLineCap.ROUND) {
                    showWarning("lineCapNotSupported", "LineCap style not supported in shape " + shapeNum);
                }
                if (lineJoin != SvgLineJoin.ROUND) {
                    showWarning("lineJoinNotSupported", "LineJoin style not supported in shape " + shapeNum);
                }
                scr.lineStyles.lineStyles = new LINESTYLE[1];
                scr.lineStyles.lineStyles[0] = (LINESTYLE) lineStyle;
            }

            scr.lineStyle = 1;
        } else {
            scr.lineStyles.lineStyles = new LINESTYLE[0];
            scr.lineStyles.lineStyles2 = new LINESTYLE2[0];
            scr.lineStyle = 0;
        }

        return scr;
    }

    private RGB getRGB(int shapeNum, Color color) {
        if (shapeNum < 3 && color.getAlpha() != 0xff) {
            showWarning("transparentColorNotSupported", "Transparent color is not supported in shape " + shapeNum);
        }

        return shapeNum >= 3 ? new RGBA(color) : new RGB(color);
    }

    private double parseCoordinate(String value, double relativeTo) {
        return parseLength(value, relativeTo);
    }

    private double parseLength(String value, double relativeTo) {
        if (value == null) {
            throw new NumberFormatException();
        }

        value = value.toLowerCase();
        String unit = null;
        if (value.endsWith("em")
                || value.endsWith("ex")
                || value.endsWith("px")
                || value.endsWith("in")
                || value.endsWith("cm")
                || value.endsWith("mm")
                || value.endsWith("pt")
                || value.endsWith("pc")) {
            unit = value.substring(value.length() - 2);
            value = value.substring(0, value.length() - 2);
        } else if (value.endsWith("%")) {
            unit = "%";
            value = value.substring(0, value.length() - 1);
        }

        double result = Double.parseDouble(value);
        if (unit != null) {
            switch (unit) {
                case "em":
                case "ex":
                    // todo: font things
                    break;
                case "in":
                    result *= getDpi();
                    break;
                case "pt":
                    result *= getDpi() / 72;
                    break;
                case "pc":
                    result *= getDpi() / 6;
                    break;
                case "cm":
                    result *= getDpi() / 2.54;
                    break;
                case "mm":
                    result *= getDpi() / 25.4;
                    break;
                case "%":
                    result = relativeTo * result / 100;
                    break;
            }
        }

        return result;
    }

    /**
     * Parses a number from a string.
     * @param value The string
     * @return The number
     */
    public double parseNumber(String value) {
        if (value == null) {
            throw new NumberFormatException();
        }

        double result = Double.parseDouble(value);
        return result;
    }

    /**
     * Parses a number or percent from a string.
     * @param value The string
     * @return The number
     */
    public double parseNumberOrPercent(String value) {
        if (value == null) {
            throw new NumberFormatException();
        }

        boolean percent = value.endsWith("%");
        if (percent) {
            value = value.substring(0, value.length() - 1);
        }

        double result = Double.parseDouble(value);
        if (percent) {
            result /= 100;
        }

        return result;
    }

    private double getDpi() {
        return 96;

    }

    class PathCommand {

        public char command;

        public double[] params;
    }

    void showWarning(String name, String text) {
        if (!shownWarnings.contains(name)) {
            Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, text);
            shownWarnings.add(name);
        }
    }
}

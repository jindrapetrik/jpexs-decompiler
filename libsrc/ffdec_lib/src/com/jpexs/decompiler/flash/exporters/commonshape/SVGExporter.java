/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters.commonshape;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.FontExportMode;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.BlendMode;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * SVG exporter.
 *
 * @author JPEXS
 */
public class SVGExporter {

    protected static final String sNamespace = "http://www.w3.org/2000/svg";

    protected static final String xlinkNamespace = "http://www.w3.org/1999/xlink";

    protected static final String ffdecNamespace = "https://www.free-decompiler.com/flash";

    protected Document _svg;

    protected Element _svgDefs;

    protected CDATASection _svgStyle;

    protected Stack<Element> _svgGs = new Stack<>();

    public List<Element> gradients;

    protected int lastPatternId;

    protected int lastClipId;

    public Map<ExportKey, String> exportedTags = new HashMap<>();

    public Map<Tag, Map<Integer, String>> exportedChars = new HashMap<>();

    private final Map<String, Integer> lastIds = new HashMap<>();

    private final HashSet<String> fontFaces = new HashSet<>();

    public boolean useTextTag = Configuration.textExportExportFontFace.get();

    private double zoom;

    public static class ExportKey {

        private final Tag tag;
        public final ColorTransform colorTransform;
        public final int ratio;
        public final boolean clipped;

        public ExportKey(Tag tag, ColorTransform colorTransform, int ratio, boolean clipped) {
            this.tag = tag;
            this.colorTransform = colorTransform;
            this.ratio = ratio;
            this.clipped = clipped;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + Objects.hashCode(this.tag);
            hash = 79 * hash + Objects.hashCode(this.colorTransform);
            hash = 79 * hash + this.ratio;
            hash = 79 * hash + (this.clipped ? 1 : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ExportKey other = (ExportKey) obj;
            if (this.ratio != other.ratio) {
                return false;
            }
            if (this.clipped != other.clipped) {
                return false;
            }
            if (!Objects.equals(this.tag, other.tag)) {
                return false;
            }
            return Objects.equals(this.colorTransform, other.colorTransform);
        }
    }

    public SVGExporter(ExportRectangle bounds, double zoom, String objectType) {
        this(bounds, zoom, objectType, null);
    }

    public SVGExporter(ExportRectangle bounds, double zoom, String objectType, Color backgroundColor) {
        this.zoom = zoom;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            DOMImplementation impl = docBuilder.getDOMImplementation();
            DocumentType svgDocType = impl.createDocumentType("svg", "-//W3C//DTD SVG 1.0//EN",
                    "http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd");
            _svg = impl.createDocument(sNamespace, "svg", svgDocType);
            Element svgRoot = _svg.getDocumentElement();
            svgRoot.setAttribute("xmlns:xlink", xlinkNamespace);
            svgRoot.setAttribute("xmlns:ffdec", ffdecNamespace);
            if (bounds != null) {
                if (Configuration.svgRetainBounds.get()) {
                    svgRoot.setAttribute("width", (bounds.xMax / SWF.unitDivisor) + "px");
                    svgRoot.setAttribute("height", (bounds.yMax / SWF.unitDivisor) + "px");
                } else {
                    svgRoot.setAttribute("width", (bounds.getWidth() / SWF.unitDivisor) + "px");
                    svgRoot.setAttribute("height", (bounds.getHeight() / SWF.unitDivisor) + "px");
                }
                createDefGroup(bounds, null, zoom);

                if (backgroundColor != null) {
                    Element rect = _svg.createElement("rect");
                    rect.setAttribute("fill", new RGBA(backgroundColor).toHexRGB());
                    if (Configuration.svgRetainBounds.get()) {
                        rect.setAttribute("width", (bounds.xMax / SWF.unitDivisor) + "px");
                        rect.setAttribute("height", (bounds.yMax / SWF.unitDivisor) + "px");
                    } else {
                        rect.setAttribute("width", (bounds.getWidth() / SWF.unitDivisor) + "px");
                        rect.setAttribute("height", (bounds.getHeight() / SWF.unitDivisor) + "px");
                    }
                    _svgGs.peek().appendChild(rect);
                }
            }
            svgRoot.setAttribute("ffdec:objectType", objectType);

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SVGExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        gradients = new ArrayList<>();
    }

    public double getZoom() {
        return zoom;
    }

    private Element getDefs() {
        if (_svgDefs == null) {
            _svgDefs = _svg.createElement("defs");
            _svg.getDocumentElement().appendChild(_svgDefs);
        }
        return _svgDefs;
    }

    private CDATASection getStyle() {
        if (_svgStyle == null) {
            Element style = _svg.createElement("style");
            _svgStyle = _svg.createCDATASection("");
            style.appendChild(_svgStyle);
            getDefs().appendChild(style);
        }
        return _svgStyle;
    }
    
    public final boolean hasSmallStrokes() {
        NodeList nodes = _svgGs.peek().getElementsByTagName("path");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            if ("true".equals(e.getAttribute("ffdec:has-small-stroke"))) {
                return true;
            }
        }
        nodes = _svgGs.peek().getElementsByTagName("use");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element e = (Element) nodes.item(i);
            if ("true".equals(e.getAttribute("ffdec:has-small-stroke"))) {
                return true;
            }
        }
        return false;
    }

    public final void createDefGroup(ExportRectangle bounds, String id) {
        createDefGroup(bounds, id, 1);
    }   

    public final void createDefGroup(ExportRectangle bounds, String id, double zoom) {
        Element g = _svg.createElement("g");
        if (bounds != null) {
            Matrix mat;
            if (Configuration.svgRetainBounds.get()) {
                mat = new Matrix();
            } else {
                mat = Matrix.getTranslateInstance(-bounds.xMin, -bounds.yMin);
            }
            mat.scale(zoom);
            g.setAttribute("transform", mat.getSvgTransformationString(SWF.unitDivisor, 1));
        }
        if (id != null) {
            g.setAttribute("id", id);
        }
        if (_svgGs.size() == 0) {
            _svg.getDocumentElement().appendChild(g);
        } else {
            getDefs().appendChild(g);
        }
        _svgGs.add(g);
    }

    public boolean endGroup() {
        Element g = _svgGs.pop();
        if (g.getChildNodes().getLength() == 0) {
            g.getParentNode().removeChild(g);
            return false;
        }

        return true;
    }

    public final Element createClipPath(Matrix transform, String id) {
        Element group = createSubGroup(id, "clipPath");
        if (transform != null) {
            group.setAttribute("transform", transform.getSvgTransformationString(SWF.unitDivisor, 1));
        }
        return group;
    }

    public final Element createSubGroup(Matrix transform, String id) {
        Element group = createSubGroup(id, "g");
        if (transform != null) {
            group.setAttribute("transform", transform.getSvgTransformationString(SWF.unitDivisor, 1));
        }

        return group;
    }

    private Element createSubGroup(String id, String tagName) {
        Element group = _svg.createElement(tagName);
        if (id != null) {
            group.setAttribute("id", id);
        }
        _svgGs.peek().appendChild(group);
        _svgGs.add(group);
        return group;
    }

    public void addToGroup(Node newChild) {
        _svgGs.peek().appendChild(newChild);
    }

    public void addToDefs(Node newChild) {
        getDefs().appendChild(newChild);
    }

    public Element createElement(String tagName) {
        return _svg.createElement(tagName);
    }

    public String getSVG() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StringWriter writer = new StringWriter();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(_svg);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Logger.getLogger(SVGExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString();
    }

    /*public void setBackGroundColor(Color backGroundColor) {
        Attr attr = _svg.createAttribute("style");
        attr.setValue("background: " + new RGBA(backGroundColor).toHexARGB());
    }*/
    private String addClip(String path) {
        lastClipId++;
        Element clipPathElement = _svg.createElement("clipPath");
        clipPathElement.setAttribute("id", "clip" + lastClipId);
        Element pathElement = _svg.createElement("path");
        pathElement.setAttribute("d", path);
        clipPathElement.appendChild(pathElement);
        _svgGs.peek().appendChild(clipPathElement);
        return "url(#clip" + lastClipId + ")";

    }

    private void addScalingGridUse(Matrix transform, RECT boundRect, String href, String instanceName, RECT scalingRect, String characterId, String characterName, int blendMode, List<FILTER> filters) {

        Element image = _svg.createElement("g");

        ExportRectangle exRect = new ExportRectangle(boundRect);
        ExportRectangle newRect = exRect;
        if (transform == null) {
            transform = new Matrix();
        }
        Matrix transform2;
        newRect = transform.transform(exRect);
        transform = transform.clone();

        transform = Matrix.getTranslateInstance(newRect.xMin, newRect.yMin);

        double scaleWidth = newRect.getWidth() - scalingRect.Xmin - (boundRect.Xmax - scalingRect.Xmax);
        double originalWidth = boundRect.getWidth() - scalingRect.Xmin - (boundRect.Xmax - scalingRect.Xmax);
        double scaleX = scaleWidth / originalWidth;

        double scaleHeight = newRect.getHeight() - scalingRect.Ymin - (boundRect.Ymax - scalingRect.Ymax);
        double originalHeight = boundRect.getHeight() - scalingRect.Ymin - (boundRect.Ymax - scalingRect.Ymax);
        double scaleY = scaleHeight / originalHeight;

        Element leftTopCorner = _svg.createElement("use");
        leftTopCorner.setAttribute("transform", transform.getSvgTransformationString(SWF.unitDivisor, 1));

        leftTopCorner.setAttribute("clip-path", addClip("M 0,0 "
                + "L " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + ",0 "
                + "L " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " "
                + "L 0," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " Z"
        ));
        leftTopCorner.setAttribute("xlink:href", "#" + href);
        image.appendChild(leftTopCorner);

        Element bottomLeftCorner = _svg.createElement("use");
        transform2 = transform.clone();
        transform2.translateY += newRect.getHeight() - boundRect.getHeight();
        bottomLeftCorner.setAttribute("transform", transform2.getSvgTransformationString(SWF.unitDivisor, 1));
        bottomLeftCorner.setAttribute("clip-path", addClip(
                "M 0," + Math.rint((boundRect.getHeight() - (boundRect.Ymax - scalingRect.Ymax)) / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint((boundRect.getHeight() - (boundRect.Ymax - scalingRect.Ymax)) / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint(boundRect.getHeight() / (double) SWF.unitDivisor) + " "
                + "L 0," + Math.rint(boundRect.getHeight() / (double) SWF.unitDivisor) + " Z"
        ));

        bottomLeftCorner.setAttribute("xlink:href", "#" + href);
        image.appendChild(bottomLeftCorner);

        Element topRightCorner = _svg.createElement("use");
        transform2 = transform.clone();
        transform2.translateX += newRect.getWidth() - boundRect.getWidth();
        topRightCorner.setAttribute("transform", transform2.getSvgTransformationString(SWF.unitDivisor, 1));

        topRightCorner.setAttribute("clip-path", addClip("M " + Math.rint((boundRect.getWidth() - (exRect.xMax - scalingRect.Xmax)) / (double) SWF.unitDivisor) + ",0 "
                + "L " + Math.rint(boundRect.getWidth() / (double) SWF.unitDivisor) + ",0 "
                + "L " + Math.rint(boundRect.getWidth() / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint((boundRect.getWidth() - (exRect.xMax - scalingRect.Xmax)) / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " Z"
        ));
        topRightCorner.setAttribute("xlink:href", "#" + href);
        image.appendChild(topRightCorner);

        Element bottomRightCorner = _svg.createElement("use");
        transform2 = transform.clone();
        transform2.translateX += newRect.getWidth() - boundRect.getWidth();
        transform2.translateY += newRect.getHeight() - boundRect.getHeight();
        bottomRightCorner.setAttribute("transform", transform2.getSvgTransformationString(SWF.unitDivisor, 1));

        bottomRightCorner.setAttribute("clip-path", addClip("M " + Math.rint((boundRect.getWidth() - (exRect.xMax - scalingRect.Xmax)) / (double) SWF.unitDivisor) + "," + Math.rint((boundRect.getHeight() - (boundRect.Ymax - scalingRect.Ymax)) / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(boundRect.getWidth() / (double) SWF.unitDivisor) + "," + Math.rint((boundRect.getHeight() - (boundRect.Ymax - scalingRect.Ymax)) / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(boundRect.getWidth() / (double) SWF.unitDivisor) + "," + Math.rint(boundRect.getHeight() / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint((boundRect.getWidth() - (exRect.xMax - scalingRect.Xmax)) / (double) SWF.unitDivisor) + "," + Math.rint(boundRect.getHeight() / (double) SWF.unitDivisor) + " Z"
        ));
        bottomRightCorner.setAttribute("xlink:href", "#" + href);
        image.appendChild(bottomRightCorner);

        Element top = _svg.createElement("use");
        transform2 = transform.clone();

        transform2.translate(scalingRect.Xmin, 0);
        transform2.scale(scaleX, 1);
        transform2.translate(-scalingRect.Xmin, 0);

        top.setAttribute("transform", transform2.getSvgTransformationString(SWF.unitDivisor, 1));

        top.setAttribute("clip-path", addClip(
                "M " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + ",0 "
                + "L " + Math.rint(scalingRect.Xmax / (double) SWF.unitDivisor) + ",0 "
                + "L " + Math.rint(scalingRect.Xmax / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " Z"
        ));

        top.setAttribute("xlink:href", "#" + href);
        image.appendChild(top);

        Element left = _svg.createElement("use");
        transform2 = transform.clone();

        transform2.translate(0, scalingRect.Ymin);
        transform2.scale(1, scaleY);
        transform2.translate(0, -scalingRect.Ymin);

        left.setAttribute("transform", transform2.getSvgTransformationString(SWF.unitDivisor, 1));

        left.setAttribute("clip-path", addClip("M 0," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymax / (double) SWF.unitDivisor) + " "
                + "L 0," + Math.rint(scalingRect.Ymax / (double) SWF.unitDivisor) + " Z"
        ));

        left.setAttribute("xlink:href", "#" + href);
        image.appendChild(left);

        Element bottom = _svg.createElement("use");
        transform2 = transform.clone();

        transform2.translate(scalingRect.Xmin, 0);
        transform2.scale(scaleX, 1);
        transform2.translate(-scalingRect.Xmin, 0);

        transform2.translateY += newRect.getHeight() - boundRect.getHeight();

        bottom.setAttribute("transform", transform2.getSvgTransformationString(SWF.unitDivisor, 1));

        bottom.setAttribute("clip-path", addClip("M " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymax / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmax / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymax / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmax / (double) SWF.unitDivisor) + "," + Math.rint(boundRect.Ymax / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint(boundRect.Ymax / (double) SWF.unitDivisor) + " Z"
        ));

        bottom.setAttribute("xlink:href", "#" + href);
        image.appendChild(bottom);

        Element right = _svg.createElement("use");
        transform2 = transform.clone();

        transform2.translate(0, scalingRect.Ymin);
        transform2.scale(1, scaleY);
        transform2.translate(0, -scalingRect.Ymin);

        transform2.translateX += newRect.getWidth() - boundRect.getWidth();

        right.setAttribute("transform", transform2.getSvgTransformationString(SWF.unitDivisor, 1));

        right.setAttribute("clip-path", addClip("M " + Math.rint(scalingRect.Xmax / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(boundRect.Xmax / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(boundRect.Xmax / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymax / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmax / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymax / (double) SWF.unitDivisor) + " Z"
        ));

        right.setAttribute("xlink:href", "#" + href);
        image.appendChild(right);

        Element center = _svg.createElement("use");

        transform2 = transform.clone();

        transform2.translate(scalingRect.Xmin, scalingRect.Ymin);
        transform2.scale(scaleX, scaleY);
        transform2.translate(-scalingRect.Xmin, -scalingRect.Ymin);

        center.setAttribute("transform", transform2.getSvgTransformationString(SWF.unitDivisor, 1));

        center.setAttribute("clip-path", addClip(
                "M " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmax / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymin / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmax / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymax / (double) SWF.unitDivisor) + " "
                + "L " + Math.rint(scalingRect.Xmin / (double) SWF.unitDivisor) + "," + Math.rint(scalingRect.Ymax / (double) SWF.unitDivisor) + " Z"
        ));

        center.setAttribute("xlink:href", "#" + href);
        image.appendChild(center);

        if (instanceName != null) {
            image.setAttribute("id", instanceName);
        }
        if (characterId != null) {
            image.setAttribute("ffdec:characterId", characterId);
        }
        if (characterName != null && !characterName.isEmpty()) {
            image.setAttribute("ffdec:characterName", characterName);
        }
        setBlendMode(image, blendMode);
        handleFilters(image, filters);
        _svgGs.peek().appendChild(image);
    }

    private void handleFilters(Element image, List<FILTER> filters) {
        if (filters == null) {
            return;
        }
        Element filtersElement = _svg.createElement("filter");
        String filterId = getUniqueId("filter");
        String in = "SourceGraphic";
        boolean empty = true;
        for (FILTER filter : filters) {
            String result = filter.toSvg(_svg, filtersElement, this, in);
            if (result != null) {
                empty = false;
                in = result;
            }
        }
        if (empty) {
            return;
        }

        filtersElement.setAttribute("id", filterId);
        image.setAttribute("filter", "url(#" + filterId + ")");
        _svgGs.peek().appendChild(filtersElement);
    }

    public Element addUse(Matrix transform, RECT boundRect, String href, String instanceName, RECT scalingRect) {
        return addUse(transform, boundRect, href, instanceName, scalingRect, null, null, BlendMode.NORMAL, new ArrayList<>());
    }

    public Element addUse(Matrix transform, RECT boundRect, String href, String instanceName, RECT scalingRect, String characterId, String characterName, int blendMode, List<FILTER> filters) {
        if (scalingRect != null && (transform == null || (Double.compare(transform.rotateSkew0, 0.0) == 0 && Double.compare(transform.rotateSkew1, 0.0) == 0))) {
            addScalingGridUse(transform, boundRect, href, instanceName, scalingRect, characterId, characterName, blendMode, filters);
            return null; //??
        }
        Element image = _svg.createElement("use");
        if (transform != null) {
            image.setAttribute("transform", transform.getSvgTransformationString(SWF.unitDivisor, 1));
            image.setAttribute("width", Double.toString(boundRect.getWidth() / (double) SWF.unitDivisor));
            image.setAttribute("height", Double.toString(boundRect.getHeight() / (double) SWF.unitDivisor));
        }
        if (instanceName != null) {
            image.setAttribute("id", instanceName);
        }
        if (characterId != null) {
            image.setAttribute("ffdec:characterId", characterId);
        }
        if (characterName != null && !characterName.isEmpty()) {
            image.setAttribute("ffdec:characterName", characterName);
        }
        
        setBlendMode(image, blendMode);

        handleFilters(image, filters);

        image.setAttribute("xlink:href", "#" + href);
        _svgGs.peek().appendChild(image);
        return image;
    }

    private void setBlendMode(Element element, int blendMode) {
        switch (blendMode) {
            case BlendMode.MULTIPLY:
                element.setAttribute("style", "mix-blend-mode: multiply");
                break;
            case BlendMode.SCREEN:
                element.setAttribute("style", "mix-blend-mode: screen");
                break;
            case BlendMode.OVERLAY:
                element.setAttribute("style", "mix-blend-mode: overlay");
                break;
            case BlendMode.DARKEN:
                element.setAttribute("style", "mix-blend-mode: darken");
                break;
            case BlendMode.LIGHTEN:
                element.setAttribute("style", "mix-blend-mode: lighten");
                break;
            case BlendMode.HARDLIGHT:
                element.setAttribute("style", "mix-blend-mode: hard-light");
                break;
            case BlendMode.DIFFERENCE:
                element.setAttribute("style", "mix-blend-mode: difference");
                break;
            case BlendMode.ADD:
            case BlendMode.ALPHA:
            case BlendMode.ERASE:
            case BlendMode.INVERT:
            case BlendMode.LAYER:
            case BlendMode.SUBTRACT:
                //unsupported
                break;
        }
    }

    public void addStyle(String fontFace, byte[] data, FontExportMode mode) {
        if (!fontFaces.contains(fontFace)) {
            fontFaces.add(fontFace);
            String base64Data = Helper.byteArrayToBase64String(data);
            String value = getStyle().getTextContent();
            value += Helper.newLine;
            value += "      @font-face {" + Helper.newLine;
            value += "        font-family: \"" + fontFace + "\";" + Helper.newLine;
            switch (mode) {
                case TTF:
                    value += "        src: url('data:font/truetype;base64," + base64Data + "') format(\"truetype\");" + Helper.newLine;
                    break;
                case WOFF:
                    value += "        src: url('data:font/woff;base64," + base64Data + "') format(\"woff\");" + Helper.newLine;
                    break;
            }
            value += "      }" + Helper.newLine;
            getStyle().setTextContent(value);
        }
    }

    public String getUniqueId(String prefix) {
        Integer lastId = lastIds.get(prefix);
        if (lastId == null) {
            lastId = 0;
        } else {
            lastId++;
        }
        lastIds.put(prefix, lastId);
        return prefix + lastId;
    }

    protected static double roundPixels20(double pixels) {
        return Math.round(pixels * 100) / 100.0;
    }
}

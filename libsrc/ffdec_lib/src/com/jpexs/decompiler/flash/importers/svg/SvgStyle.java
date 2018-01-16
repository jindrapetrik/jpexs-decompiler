/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.importers.svg;

import com.jpexs.decompiler.flash.importers.ShapeImporter;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.helpers.Helper;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author JPEXS
 */
class SvgStyle {

    private final Element element;

    private final SvgImporter importer;

    private final Map<String, Element> idMap;

    private final double epsilon = 0.001;

    private final Random random = new Random();

    public SvgStyle(SvgImporter importer, Map<String, Element> idMap, Element element) {
        this.importer = importer;
        this.idMap = idMap;
        this.element = element;
    }

    private Map<String, String> getStyleAttributeValues(Element element) {
        // todo: cache
        Map<String, String> styleValues = new HashMap<>();
        if (element.hasAttribute("style")) {
            String[] styleDefs = element.getAttribute("style").split(";");
            for (String styleDef : styleDefs) {
                if (!styleDef.contains(":")) {
                    continue;
                }

                String[] parts = styleDef.split(":", 2);
                String name = parts[0].trim();
                String value = parts[1].trim();
                SvgStyleProperty styleProperty = SvgStyleProperty.getByName(name);
                if (styleProperty == null) {
                    importer.showWarning(name + "StyleNotSupported", "The style '" + name + "' is not supported.");
                } else {
                    styleValues.put(name, value);
                }
            }
        }

        return styleValues;
    }

    private <E> E getValue(Element element, String name) {
        return getValue(element, name, false);
    }

    @SuppressWarnings("unchecked")
    private <E> E getValue(Element element, String name, boolean inherit) {
        Map<String, String> styleValues = getStyleAttributeValues(element);
        if (styleValues.containsKey(name)) {
            String value = styleValues.get(name);
            if ("inherit".equals(value)) {
                if (element.getParentNode() instanceof Element) {
                    return getValue((Element) element.getParentNode(), name, true);
                }
            } else {
                Object result = getStyleValue(this, name, value);
                if (result != null) {
                    return (E) result;
                }
            }
        }

        if (element.hasAttribute(name)) {
            String value = element.getAttribute(name).trim();
            if ("inherit".equals(value)) {
                if (element.getParentNode() instanceof Element) {
                    return getValue((Element) element.getParentNode(), name, true);
                }
            } else {
                Object result = getStyleValue(this, name, value);
                if (result != null) {
                    return (E) result;
                }
            }
        }

        SvgStyleProperty p = SvgStyleProperty.getByName(name);
        if (inherit || p.isInherited() && element.getParentNode() instanceof Element) {
            return getValue((Element) element.getParentNode(), name);
        }

        return (E) p.getInitialValue();
    }

    public Color getColor() {
        return getValue(element, "color");
    }

    public SvgFill getFill() {
        return getValue(element, "fill");
    }

    public double getFillOpacity() {
        return getValue(element, "fill-opacity");
    }

    public SvgFill getStroke() {
        return getValue(element, "stroke");
    }

    public double getStrokeWidth() {
        return getValue(element, "stroke-width");
    }

    public double getStrokeOpacity() {
        return getValue(element, "stroke-opacity");
    }

    public SvgLineCap getStrokeLineCap() {
        return getValue(element, "stroke-linecap");
    }

    public SvgLineJoin getStrokeLineJoin() {
        return getValue(element, "stroke-linejoin");
    }

    public double getStrokeMiterLimit() {
        return getValue(element, "stroke-miterlimit");
    }

    public double getOpacity() {
        return getValue(element, "opacity");
    }

    public Color getStopColor() {
        return getValue(element, "stop-color");
    }

    public double getStopOpacity() {
        return getValue(element, "stop-opacity");
    }

    public SvgFill getFillWithOpacity() {
        SvgFill fill = getFill();
        if (fill == null) {
            return null;
        }
        if (!(fill instanceof SvgColor)) {
            return fill;
        }
        Color fillColor = ((SvgColor) fill).color;

        int opacity = (int) Math.round(getOpacity() * getFillOpacity() * 255);
        if (opacity > 255) {
            opacity = 255;
        }

        if (opacity < 0) {
            opacity = 0;
        }

        if (opacity == 255) {
            return fill;
        }

        return new SvgColor(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), opacity);
    }

    public SvgFill getStrokeFillWithOpacity() {
        SvgFill strokeFill = getStroke();
        if (strokeFill == null) {
            return null;
        }
        if (!(strokeFill instanceof SvgColor)) {
            return strokeFill;
        }
        Color strokeFillColor = ((SvgColor) strokeFill).color;

        int opacity = (int) Math.round(getOpacity() * getStopOpacity() * 255);
        if (opacity > 255) {
            opacity = 255;
        }

        if (opacity < 0) {
            opacity = 0;
        }

        if (opacity == 255) {
            return strokeFill;
        }

        return new SvgColor(strokeFillColor.getRed(), strokeFillColor.getGreen(), strokeFillColor.getBlue(), opacity);
    }

    public SvgFill getStrokeColorWithOpacity() {
        SvgFill strokeFill = getStroke();
        if (strokeFill == null) {
            return null;
        }
        if (!(strokeFill instanceof SvgColor)) {
            return strokeFill;
        }

        Color strokeColor = ((SvgColor) strokeFill).color;

        int opacity = (int) Math.round(getOpacity() * getStrokeOpacity() * 255);
        if (opacity == 255) {
            return strokeFill;
        }

        return new SvgColor(strokeColor.getRed(), strokeColor.getGreen(), strokeColor.getBlue(), opacity);
    }

    //FIXME - matrices
    private SvgFill parseGradient(Map<String, Element> idMap, Element el) {
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
                    importer.showWarning("fillNotSupported", "Parent gradient not found.");
                    return new SvgColor(random.nextInt(256), random.nextInt(256), random.nextInt(256));
                }

                if ("linearGradient".equals(el.getTagName()) && parent_el.getTagName().equals(el.getTagName())) {
                    SvgLinearGradient parentFill = (SvgLinearGradient) parseGradient(idMap, parent_el);
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
                    SvgRadialGradient parentFill = (SvgRadialGradient) parseGradient(idMap, parent_el);
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
                importer.showWarning("fillNotSupported", "Parent gradient invalid.");
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
                    importer.showWarning("fillNotSupported", "Unsupported  gradientUnits: " + el.getAttribute("gradientUnits"));
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
                SvgStyle newStyle = new SvgStyle(importer, idMap, stopEl);

                String offsetStr = stopEl.getAttribute("offset");
                double offset = importer.parseNumberOrPercent(offsetStr);
                Color color = newStyle.getStopColor();
                if (color == null) {
                    color = Color.BLACK;
                }

                int alpha = (int) Math.round(newStyle.getStopOpacity() * 255);
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
            ret.stops = fixStops(stops);
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
            ret.stops = fixStops(stops);
            ret.interpolation = interpolation;
            return ret;
        } else {
            return null;
        }
    }

    private List<SvgStop> fixStops(List<SvgStop> stops) {
        if (stops.isEmpty()) {
            stops.add(new SvgStop(SvgTransparentFill.INSTANCE.toColor(), 0));
            stops.add(new SvgStop(SvgTransparentFill.INSTANCE.toColor(), 1));
        } else if (stops.size() == 1) {
            SvgStop stop0 = stops.get(0);
            stop0.offset = 0;
            stops.add(new SvgStop(stop0.color, 1));
        }

        double offset = 0;
        for (SvgStop stop : stops) {
            if (stop.offset < offset) {
                stop.offset = offset;
            }

            if (stop.offset > 1) {
                stop.offset = 1;
            }

            offset = stop.offset;
        }

        if (Math.abs(offset - 1) > epsilon) {
            stops.add(new SvgStop(stops.get(stops.size() - 1).color, 1));
        }

        return stops;
    }

    private SvgFill parseFill(Map<String, Element> idMap, String fillStr) {
        if (fillStr == null) {
            return null;
        }

        if (fillStr.equals("none")) {
            return SvgTransparentFill.INSTANCE;
        }

        Pattern idPat = Pattern.compile("url\\(#([^)]+)\\).*");
        java.util.regex.Matcher mPat = idPat.matcher(fillStr);

        if (mPat.matches()) {
            String elementId = mPat.group(1);
            Element e = idMap.get(elementId);
            if (e != null) {
                String tagName = e.getTagName();
                if ("linearGradient".equals(tagName)) {
                    return parseGradient(idMap, e);
                }

                if ("radialGradient".equals(tagName)) {
                    return parseGradient(idMap, e);
                }

                if ("pattern".equals(tagName)) {
                    Element element = null;
                    NodeList childNodes = e.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        if (childNodes.item(i) instanceof Element) {
                            if (element != null) {
                                element = null;
                                break;
                            }

                            element = (Element) childNodes.item(i);
                        }
                    }

                    if (element != null && "image".equals(element.getTagName())) {
                        String attr = element.getAttribute("xlink:href").trim();
                        // this is ugly, but supports the format which is exported by FFDec
                        if (attr.startsWith("data:image/") && attr.contains("base64,")) {
                            String base64 = attr.substring(attr.indexOf("base64,") + 7);
                            byte[] data = Helper.base64StringToByteArray(base64);
                            try {
                                ImageTag imageTag = new ShapeImporter().addImage(importer.shapeTag, data, 0);
                                SvgBitmapFill bitmapFill = new SvgBitmapFill();
                                bitmapFill.characterId = imageTag.characterID;
                                if (e.hasAttribute("patternTransform")) {
                                    bitmapFill.patternTransform = e.getAttribute("patternTransform");
                                }

                                return bitmapFill;
                            } catch (IOException ex) {
                                Logger.getLogger(SvgStyle.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }

                importer.showWarning("fillNotSupported", "Unknown fill style. Random color assigned.");
                return new SvgColor(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            }

            fillStr = fillStr.substring(elementId.length() + 6).trim(); // remove url(#...)
        }

        SvgColor result = SvgColor.parse(fillStr);
        if (result == null) {
            importer.showWarning("fillNotSupported", "Unknown fill style. Random color assigned.");
            return new SvgColor(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }

        return result;
    }

    private Object getStyleValue(SvgStyle style, String name, String value) {
        if (value == null || value.length() == 0) {
            return null;
        }

        try {
            switch (name) {
                case "color": {
                    Color color = SvgColor.parse(value).toColor();
                    if (color != null) {
                        return color;
                    }
                }
                break;
                case "fill": {
                    if ("currentColor".equals(value)) {
                        return new SvgColor(style.getColor());
                    } else {
                        SvgFill fill = parseFill(idMap, value);
                        if (fill != null) {
                            return fill;
                        }
                    }
                }
                break;
                case "fill-opacity": {
                    double opacity = Double.parseDouble(value);
                    return opacity;
                }
                case "stroke": {
                    if ("currentColor".equals(value)) {
                        return new SvgColor(style.getColor());
                    } else {
                        SvgFill stroke = parseFill(idMap, value);
                        if (stroke != null) {
                            return stroke;
                        }
                    }
                }
                break;
                case "stroke-width": {
                    double strokeWidth = Double.parseDouble(value);
                    return strokeWidth;
                }
                case "stroke-opacity": {
                    double opacity = Double.parseDouble(value);
                    return opacity;
                }
                case "stroke-linecap": {
                    switch (value) {
                        case "butt":
                            return SvgLineCap.BUTT;
                        case "round":
                            return SvgLineCap.ROUND;
                        case "square":
                            return SvgLineCap.SQUARE;
                    }
                }
                break;
                case "stroke-linejoin": {
                    switch (value) {
                        case "miter":
                            return SvgLineJoin.MITER;
                        case "round":
                            return SvgLineJoin.ROUND;
                        case "bevel":
                            return SvgLineJoin.BEVEL;
                    }
                }
                break;
                case "stroke-miterlimit": {
                    double strokeMiterLimit = Double.parseDouble(value);
                    return strokeMiterLimit;
                }
                case "opacity": {
                    double opacity = Double.parseDouble(value);
                    return opacity;
                }
                case "stop-color": {
                    if ("currentColor".equals(value)) {
                        return style.getColor();
                    } else {
                        return SvgColor.parse(value).toColor();
                    }
                }
                case "stop-opacity": {
                    double stopOpacity = Double.parseDouble(value);
                    return stopOpacity;
                }
            }
        } catch (NumberFormatException ex) {
            //ignore
        }

        return null;
    }
}

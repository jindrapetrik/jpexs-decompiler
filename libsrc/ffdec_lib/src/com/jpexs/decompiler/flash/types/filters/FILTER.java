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
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ConcreteClasses;
import com.jpexs.helpers.SerializableImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for filters.
 *
 * @author JPEXS
 */
@ConcreteClasses({BLURFILTER.class, GRADIENTBEVELFILTER.class, GLOWFILTER.class, GRADIENTGLOWFILTER.class, COLORMATRIXFILTER.class, CONVOLUTIONFILTER.class, BEVELFILTER.class, DROPSHADOWFILTER.class})
public abstract class FILTER implements Serializable {

    /**
     * Identifier of type of the filter
     */
    @SWFType(BasicType.UI8)
    public int id;

    /**
     * Constructor
     *
     * @param id Type identifier
     */
    public FILTER(int id) {
        this.id = id;
    }

    /**
     * Applies filter to image.
     * @param src Image to apply filter to
     * @param zoom Zoom level
     * @param srcX X coordinate of the source rectangle
     * @param srcY Y coordinate of the source rectangle
     * @param srcW Width of the source rectangle
     * @param srcH Height of the source rectangle
     * @return Filtered image
     */
    public abstract SerializableImage apply(SerializableImage src, double zoom, int srcX, int srcY, int srcW, int srcH);

    /**
     * Gets delta X
     * @return Delta X
     */
    public abstract double getDeltaX();

    /**
     * Gets delta Y
     * @return Delta Y
     */
    public abstract double getDeltaY();

    /**
     * Converts filter to SVG.
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter SVG exporter
     * @param in Input
     * @return SVG representation of the filter
     */
    public abstract String toSvg(Document document, Element filtersElement, SVGExporter exporter, String in);

    /**
     * Converts drop shadow to SVG.
     * @param distance Distance
     * @param angle Angle
     * @param dropShadowColor Drop shadow color
     * @param innerShadow Inner shadow
     * @param knockout Knockout
     * @param compositeSource Composite source
     * @param blurX Blur X
     * @param blurY Blur Y
     * @param strength Strength
     * @param iterations Iterations
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter SVG exporter
     * @param in Input
     * @return SVG id of the drop shadow
     */
    protected String dropShadowSvg(
            double distance,
            double angle,
            RGBA dropShadowColor,
            boolean innerShadow,
            boolean knockout,
            boolean compositeSource,
            double blurX,
            double blurY,
            double strength,
            int iterations,
            Document document,
            Element filtersElement,
            SVGExporter exporter,
            String in
    ) {
        double dx = distance * Math.cos(angle);
        double dy = distance * Math.sin(angle);

        if (innerShadow) {
            Element feFlood = document.createElement("feFlood");
            feFlood.setAttribute("flood-color", dropShadowColor.toHexRGB());
            feFlood.setAttribute("flood-opacity", "" + dropShadowColor.getAlphaFloat());
            filtersElement.appendChild(feFlood);
            String feFloodResult = exporter.getUniqueId("filterResult");
            feFlood.setAttribute("result", feFloodResult);

            Element feOffset = document.createElement("feOffset");
            feOffset.setAttribute("dx", "" + dx);
            feOffset.setAttribute("dy", "" + dy);
            feOffset.setAttribute("in", in);
            String feOffsetResult = exporter.getUniqueId("filterResult");
            feOffset.setAttribute("result", feOffsetResult);
            filtersElement.appendChild(feOffset);

            Element feComposite1 = document.createElement("feComposite");
            feComposite1.setAttribute("in", feFloodResult);
            feComposite1.setAttribute("in2", feOffsetResult);
            feComposite1.setAttribute("operator", "out");
            String feComposite1Result = exporter.getUniqueId("filterResult");
            feComposite1.setAttribute("result", feComposite1Result);
            filtersElement.appendChild(feComposite1);

            String blurResult = blurSvg(blurX, blurY, iterations, document, filtersElement, exporter, feComposite1Result);

            Element feComposite2 = document.createElement("feComposite");
            feComposite2.setAttribute("in", blurResult);
            feComposite2.setAttribute("in2", in);
            feComposite2.setAttribute("operator", "in");

            String result = exporter.getUniqueId("filterResult");
            feComposite2.setAttribute("result", result);
            filtersElement.appendChild(feComposite2);

            if (Double.compare(strength, 1.0) != 0) {
                Element feColorMatrix2 = document.createElement("feColorMatrix");
                feColorMatrix2.setAttribute("type", "matrix");
                feColorMatrix2.setAttribute("in", result);
                feColorMatrix2.setAttribute("values",
                        "1 0 0 0 0,"
                        + "0 1 0 0 0,"
                        + "0 0 1 0 0,"
                        + "0 0 0 " + strength + " 0"
                );
                result = exporter.getUniqueId("filterResult");
                feColorMatrix2.setAttribute("result", result);
                filtersElement.appendChild(feColorMatrix2);
            }

            if (knockout || !compositeSource) {
                return result;
            }
            Element feComposite3 = document.createElement("feComposite");
            feComposite3.setAttribute("in", result);
            feComposite3.setAttribute("in2", in);
            feComposite3.setAttribute("operator", "over");

            result = exporter.getUniqueId("filterResult");
            feComposite3.setAttribute("result", result);
            filtersElement.appendChild(feComposite3);
            return result;
        } else {
            Element feOffset = document.createElement("feOffset");
            feOffset.setAttribute("dx", "" + dx);
            feOffset.setAttribute("dy", "" + dy);
            feOffset.setAttribute("in", in);
            String feOffsetResult = exporter.getUniqueId("filterResult");
            feOffset.setAttribute("result", feOffsetResult);
            filtersElement.appendChild(feOffset);

            Element feColorMatrix = document.createElement("feColorMatrix");
            feColorMatrix.setAttribute("type", "matrix");
            feColorMatrix.setAttribute("in", feOffsetResult);
            feColorMatrix.setAttribute("values",
                    "0 0 0 0 " + (dropShadowColor.red / 255f) + ","
                    + "0 0 0 0 " + (dropShadowColor.green / 255f) + ","
                    + "0 0 0 0 " + (dropShadowColor.blue / 255f) + ","
                    + "0 0 0 1 0"
            );
            String feColorMatrixResult = exporter.getUniqueId("filterResult");
            feColorMatrix.setAttribute("result", feColorMatrixResult);
            filtersElement.appendChild(feColorMatrix);

            String result = blurSvg(blurX, blurY, iterations, document, filtersElement, exporter, feColorMatrixResult);

            if (Double.compare(strength, 1.0) != 0) {
                Element feColorMatrix2 = document.createElement("feColorMatrix");
                feColorMatrix2.setAttribute("type", "matrix");
                feColorMatrix2.setAttribute("in", result);
                feColorMatrix2.setAttribute("values",
                        "1 0 0 0 0,"
                        + "0 1 0 0 0,"
                        + "0 0 1 0 0,"
                        + "0 0 0 " + strength + " 0"
                );
                result = exporter.getUniqueId("filterResult");
                feColorMatrix2.setAttribute("result", result);
                filtersElement.appendChild(feColorMatrix2);
            }

            if (!knockout && !compositeSource) {
                return result;
            }

            Element feComposite = document.createElement("feComposite");
            if (knockout) {
                feComposite.setAttribute("in", result);
                feComposite.setAttribute("in2", in);
                feComposite.setAttribute("operator", "out");
            } else {
                feComposite.setAttribute("in", in);
                feComposite.setAttribute("in2", result);
                feComposite.setAttribute("operator", "over");
            }

            result = exporter.getUniqueId("filterResult");
            feComposite.setAttribute("result", result);
            filtersElement.appendChild(feComposite);
            return result;
        }
    }

    /**
     * Converts blur to SVG.
     * @param blurX Blur X
     * @param blurY Blur Y
     * @param iterations Iterations
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter SVG exporter
     * @param in Input
     * @return SVG id of the blur
     */
    protected String blurSvg(double blurX, double blurY, int iterations, Document document, Element filtersElement, SVGExporter exporter, String in) {
        if (iterations == 0) {
            return in;
        }
        if (Double.compare(blurX, 0.0) == 0 && Double.compare(blurY, 0.0) == 0) {
            return in;
        }
        Element element = document.createElement("feConvolveMatrix");

        List<String> parts = new ArrayList<>();

        double divisor;
        if (Double.compare(blurX, 0.0) == 0) {
            int orderX = (int) Math.ceil(blurX);
            divisor = orderX;
            element.setAttribute("order", "" + orderX + " 1");

        } else if (Double.compare(blurY, 0.0) == 0) {
            int orderY = (int) Math.ceil(blurY);
            divisor = orderY;
            element.setAttribute("order", "1 " + orderY);
        } else {
            int orderX = (int) Math.ceil(blurX);
            int orderY = (int) Math.ceil(blurY);
            divisor = orderX * orderY;
            element.setAttribute("order", "" + orderX + " " + orderY);
        }

        for (int i = 0; i < divisor; i++) {
            parts.add("1");
        }

        element.setAttribute("divisor", "" + divisor);

        element.setAttribute("kernelMatrix", String.join(" ", parts));
        element.setAttribute("in", in);

        String result = exporter.getUniqueId("filterResult");
        element.setAttribute("result", result);

        filtersElement.appendChild(element);

        return blurSvg(blurX, blurY, iterations - 1, document, filtersElement, exporter, result);
    }
}

/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.helpers.GradientUtil;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * SVG filtering
 *
 * @author JPEXS
 */
public class SvgFiltering {

    /**
     * Blur filter
     *
     * @param blurX Blur X
     * @param blurY Blur Y
     * @param iterations Iterations
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter SVG exporter
     * @param in Input
     * @return SVG id of the blur
     */
    public static String blur(double blurX, double blurY, int iterations, Document document, Element filtersElement, SVGExporter exporter, String in) {
        if (iterations == 0) {
            return in;
        }
        if (Double.compare(blurX, 0.0) == 0 && Double.compare(blurY, 0.0) == 0) {
            return in;
        }
        Element element;
        if (Configuration.svgExportGaussianBlur.get()) {
            double blurXScaled = blurX * exporter.getZoom();
            double blurYScaled = blurY * exporter.getZoom();

            double deviationX = Math.sqrt((blurXScaled * blurXScaled - 1) / 12);
            double deviationY = Math.sqrt((blurYScaled * blurYScaled - 1) / 12);

            element = document.createElement("feGaussianBlur");

            element.setAttribute("stdDeviation", "" + deviationX + " " + deviationY);
        } else {
            element = document.createElement("feConvolveMatrix");

            List<String> parts = new ArrayList<>();

            int orderX = (int) Math.ceil(blurX * exporter.getZoom());
            int orderY = (int) Math.ceil(blurY * exporter.getZoom());

            if (orderX % 2 == 0) {
                orderX++;
            }

            if (orderY % 2 == 0) {
                orderY++;
            }

            double divisor = orderX * orderY;
            element.setAttribute("order", "" + orderX + " " + orderY);

            for (int i = 0; i < divisor; i++) {
                parts.add("1");
            }

            element.setAttribute("divisor", "" + divisor);

            element.setAttribute("kernelMatrix", String.join(" ", parts));
            element.setAttribute("kernelUnitLength", "1");
        }
        element.setAttribute("in", in);

        String result = exporter.getUniqueId("filterResult");
        element.setAttribute("result", result);

        filtersElement.appendChild(element);

        return blur(blurX, blurY, iterations - 1, document, filtersElement, exporter, result);
    }

    /**
     * Compose result according to INNER/OUTER/FULL
     *
     * @param src Source
     * @param result Result
     * @param type Type
     * @param knockout Knockout
     * @param compositeSource Composite source
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter Exporter
     * @return Result
     */
    private static String compose(
            String src,
            String result,
            int type,
            boolean knockout,
            boolean compositeSource,
            Document document,
            Element filtersElement,
            SVGExporter exporter
    ) {
        String in = "";
        String in2 = "";
        String operator = "";
        String dst = result;

        if (type == Filtering.FULL && !knockout && compositeSource) {
            operator = "over";
            in = dst;
            in2 = src;
            //dstover            
        } else if (type == Filtering.INNER) {
            if (knockout || !compositeSource) {
                operator = "in";
                //dstin
            } else {
                operator = "atop";
                //dstatop
            }
            in = dst;
            in2 = src;
        } else if (type == Filtering.OUTER) {
            if (knockout) {
                in = dst;
                in2 = src;
                operator = "out";
                //dstout
            } else if (compositeSource) {
                in = src;
                in2 = dst;
                operator = "over";
                //srcover
            }
        } else {
            return result;
        }

        Element feComposite = document.createElement("feComposite");
        feComposite.setAttribute("in", in);
        feComposite.setAttribute("in2", in2);
        feComposite.setAttribute("operator", operator);
        result = exporter.getUniqueId("filterResult");
        feComposite.setAttribute("result", result);
        filtersElement.appendChild(feComposite);
        return result;
    }

    /**
     * Glow filter
     *
     * @param glowColor Glow color
     * @param knockout Knockout
     * @param innerGlow Inner glow
     * @param compositeSource Composite source
     * @param blurX Blur X
     * @param blurY Blur Y
     * @param strength Strength
     * @param iterations Iterations
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter Exporter
     * @param in In
     * @return Result
     */
    public static String glow(RGBA glowColor, boolean knockout, boolean innerGlow, boolean compositeSource, double blurX, double blurY, double strength, int iterations, Document document, Element filtersElement, SVGExporter exporter, String in) {
        return dropShadow(0, 45, glowColor, innerGlow, knockout, true, blurX, blurY, strength, iterations, document, filtersElement, exporter, in);
    }

    /**
     * Gradient glow
     *
     * @param distance Distance
     * @param angle Angle
     * @param gradientColors Gradient colors
     * @param gradientRatio Gradient ratio
     * @param knockout Knockout
     * @param type Type
     * @param compositeSource Composite source
     * @param innerGlow Inner glow
     * @param blurX Blur X
     * @param blurY Blur Y
     * @param strength Strength
     * @param iterations Iterations
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter Exporter
     * @param in In
     * @return Result
     */
    public static String gradientGlow(double distance, double angle, RGBA[] gradientColors, int[] gradientRatio, boolean knockout, int type, boolean compositeSource, boolean innerGlow, double blurX, double blurY, double strength, int iterations, Document document, Element filtersElement, SVGExporter exporter, String in) {
        RGBA glowColor = new RGBA(255, 0, 0, 255);

        double dx = distance * exporter.getZoom() * Math.cos(angle);
        double dy = distance * exporter.getZoom() * Math.sin(angle);

        Element feFloodBlack = document.createElement("feFlood");
        feFloodBlack.setAttribute("flood-color", "black");
        feFloodBlack.setAttribute("flood-opacity", "1");
        String black = exporter.getUniqueId("filterResult");
        feFloodBlack.setAttribute("result", black);
        filtersElement.appendChild(feFloodBlack);

        Element feOffset = document.createElement("feOffset");
        feOffset.setAttribute("dx", "" + dx);
        feOffset.setAttribute("dy", "" + dy);
        feOffset.setAttribute("in", in);
        String feOffsetResult = exporter.getUniqueId("filterResult");
        feOffset.setAttribute("result", feOffsetResult);
        filtersElement.appendChild(feOffset);

        String result;

        Element feColorMatrix = document.createElement("feColorMatrix");
        feColorMatrix.setAttribute("type", "matrix");
        feColorMatrix.setAttribute("in", feOffsetResult);
        feColorMatrix.setAttribute("values",
                "0 0 0 0 " + (glowColor.red / 255f) + ","
                + "0 0 0 0 " + (glowColor.green / 255f) + ","
                + "0 0 0 0 " + (glowColor.blue / 255f) + ","
                + "0 0 0 " + (glowColor.alpha / 255f) + " 0" //Note: it is not last one here
        );
        result = exporter.getUniqueId("filterResult");
        feColorMatrix.setAttribute("result", result);
        filtersElement.appendChild(feColorMatrix);

        Element feComposite = document.createElement("feComposite");
        feComposite.setAttribute("in", result);
        feComposite.setAttribute("in2", black);
        feComposite.setAttribute("operator", "over");
        result = exporter.getUniqueId("filterResult");
        feComposite.setAttribute("result", result);
        filtersElement.appendChild(feComposite);

        if (Double.compare(blurX, 0.0) != 0 || Double.compare(blurY, 0.0) != 0) {
            result = blur(blurX, blurY, iterations, document, filtersElement, exporter, result);
        }

        Element feColorMatrix2 = document.createElement("feColorMatrix");
        feColorMatrix2.setAttribute("type", "matrix");
        feColorMatrix2.setAttribute("in", result);
        String matrixRow = "" + strength + " 0 0 0 0";
        feColorMatrix2.setAttribute("values",
                matrixRow + " "
                + matrixRow + " "
                + matrixRow + " "
                + matrixRow
        );
        result = exporter.getUniqueId("filterResult");
        feColorMatrix2.setAttribute("result", result);
        filtersElement.appendChild(feColorMatrix2);

        result = prepareFeComponentTransfer(gradientColors, gradientRatio, document, filtersElement, exporter, result);

        return compose(in, result, type, knockout, compositeSource, document, filtersElement, exporter);
    }

    /**
     * Bevel filter
     *
     * @param distance Distance
     * @param angle Angle
     * @param highlightColor Highlight color
     * @param shadowColor Shadow color
     * @param knockout Knockout
     * @param compositeSource Composite source
     * @param type Type
     * @param blurX Blur X
     * @param blurY Blur Y
     * @param strength Strength
     * @param iterations Iterations
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter Exporter
     * @param in In
     * @return Result
     */
    public static String bevel(double distance, double angle, RGBA highlightColor, RGBA shadowColor, boolean knockout, boolean compositeSource, int type, double blurX, double blurY, double strength, int iterations, Document document, Element filtersElement, SVGExporter exporter, String in) {
        RGBA shadowColorTransparent = new RGBA(shadowColor);
        shadowColorTransparent.alpha = 0;
        RGBA highlightColorTransparent = new RGBA(highlightColor);
        highlightColorTransparent.alpha = 0;

        return SvgFiltering.gradientBevel(distance, angle, new RGBA[]{shadowColor, shadowColorTransparent, highlightColorTransparent, highlightColor}, new int[]{0, 127, 128, 255}, knockout, compositeSource, type, blurX, blurY, strength, iterations, document, filtersElement, exporter, in);

    }

    /**
     * Gradient bevel
     *
     * @param distance Distance
     * @param angle Angle
     * @param gradientColors Gradient colors
     * @param gradientRatio Gradient ratio
     * @param knockout Knockout
     * @param compositeSource Composite source
     * @param type Type
     * @param blurX Blur X
     * @param blurY Blur Y
     * @param strength Strength
     * @param iterations Iterations
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter Exporter
     * @param in In
     * @return Result
     */
    public static String gradientBevel(double distance, double angle, RGBA[] gradientColors, int[] gradientRatio, boolean knockout, boolean compositeSource, int type, double blurX, double blurY, double strength, int iterations, Document document, Element filtersElement, SVGExporter exporter, String in) {
        RGBA highlightColor = new RGBA(255, 0, 0, 255);
        RGBA shadowColor = new RGBA(0, 0, 255, 255);

        String shadowInner = null;
        String hilightInner = null;
        if (type != Filtering.OUTER) {
            String hilight = dropShadow(distance, angle, highlightColor, true, true, true, 0, 0, strength, iterations, document, filtersElement, exporter, in);
            String shadow = dropShadow(distance, angle + Math.PI, shadowColor, true, true, true, 0, 0, strength, iterations, document, filtersElement, exporter, in);

            Element feComposite1 = document.createElement("feComposite");
            feComposite1.setAttribute("in", hilight);
            feComposite1.setAttribute("in2", shadow);
            feComposite1.setAttribute("operator", "out");
            hilightInner = exporter.getUniqueId("filterResult");
            feComposite1.setAttribute("result", hilightInner);
            filtersElement.appendChild(feComposite1);

            Element feComposite2 = document.createElement("feComposite");
            feComposite2.setAttribute("in", shadow);
            feComposite2.setAttribute("in2", hilight);
            feComposite2.setAttribute("operator", "out");
            shadowInner = exporter.getUniqueId("filterResult");
            feComposite2.setAttribute("result", shadowInner);
            filtersElement.appendChild(feComposite2);
        }

        String shadowOuter = null;
        String hilightOuter = null;

        if (type != Filtering.INNER) {
            String hilight = dropShadow(distance, angle + Math.PI, highlightColor, false, true, true, 0, 0, strength, iterations, document, filtersElement, exporter, in);
            String shadow = dropShadow(distance, angle, shadowColor, false, true, true, 0, 0, strength, iterations, document, filtersElement, exporter, in);

            Element feComposite1 = document.createElement("feComposite");
            feComposite1.setAttribute("in", hilight);
            feComposite1.setAttribute("in2", shadow);
            feComposite1.setAttribute("operator", "out");
            hilightOuter = exporter.getUniqueId("filterResult");
            feComposite1.setAttribute("result", hilightOuter);
            filtersElement.appendChild(feComposite1);

            Element feComposite2 = document.createElement("feComposite");
            feComposite2.setAttribute("in", shadow);
            feComposite2.setAttribute("in2", hilight);
            feComposite2.setAttribute("operator", "out");
            shadowOuter = exporter.getUniqueId("filterResult");
            feComposite2.setAttribute("result", shadowOuter);
            filtersElement.appendChild(feComposite2);
        }

        String hilight = null;
        String shadow = null;

        switch (type) {
            case Filtering.OUTER:
                hilight = hilightOuter;
                shadow = shadowOuter;
                break;
            case Filtering.INNER:
                hilight = hilightInner;
                shadow = shadowInner;
                break;
            case Filtering.FULL:
                Element feComposite1 = document.createElement("feComposite");
                feComposite1.setAttribute("in", hilightInner);
                feComposite1.setAttribute("in2", hilightOuter);
                feComposite1.setAttribute("operator", "over");
                hilight = exporter.getUniqueId("filterResult");
                feComposite1.setAttribute("result", hilight);
                filtersElement.appendChild(feComposite1);

                Element feComposite2 = document.createElement("feComposite");
                feComposite2.setAttribute("in", shadowInner);
                feComposite2.setAttribute("in2", shadowOuter);
                feComposite2.setAttribute("operator", "over");
                shadow = exporter.getUniqueId("filterResult");
                feComposite2.setAttribute("result", shadow);
                filtersElement.appendChild(feComposite2);
                break;
        }

        Element feFlood = document.createElement("feFlood");
        feFlood.setAttribute("flood-color", "black");
        feFlood.setAttribute("flood-opacity", "1");
        String black = exporter.getUniqueId("filterResult");
        feFlood.setAttribute("result", black);
        filtersElement.appendChild(feFlood);

        String result;

        Element feComposite4 = document.createElement("feComposite");
        feComposite4.setAttribute("in", shadow);
        feComposite4.setAttribute("in2", black);
        feComposite4.setAttribute("operator", "over");
        result = exporter.getUniqueId("filterResult");
        feComposite4.setAttribute("result", result);
        filtersElement.appendChild(feComposite4);

        Element feComposite5 = document.createElement("feComposite");
        feComposite5.setAttribute("in", hilight);
        feComposite5.setAttribute("in2", result);
        feComposite5.setAttribute("operator", "over");
        result = exporter.getUniqueId("filterResult");
        feComposite5.setAttribute("result", result);
        filtersElement.appendChild(feComposite5);

        result = blur(blurX, blurY, iterations, document, filtersElement, exporter, result);

        Element feColorMatrix = document.createElement("feColorMatrix");
        feColorMatrix.setAttribute("type", "matrix");
        feColorMatrix.setAttribute("in", result);
        double halfStrength = strength / 2;
        String matrixRow = "" + halfStrength + " 0 " + (-halfStrength) + " 0 0.5";
        feColorMatrix.setAttribute("values",
                matrixRow + " "
                + matrixRow + " "
                + matrixRow + " "
                + matrixRow
        );
        result = exporter.getUniqueId("filterResult");
        feColorMatrix.setAttribute("result", result);
        filtersElement.appendChild(feColorMatrix);

        result = prepareFeComponentTransfer(gradientColors, gradientRatio, document, filtersElement, exporter, result);

        return compose(in, result, type, knockout, compositeSource, document, filtersElement, exporter);
    }

    private static String prepareFeComponentTransfer(RGBA[] gradientColors, int[] gradientRatio, Document document, Element filtersElement, SVGExporter exporter, String in) {
        Element feComponentTransfer = document.createElement("feComponentTransfer");
        feComponentTransfer.setAttribute("in", in);

        List<String> redValues = new ArrayList<>();
        List<String> greenValues = new ArrayList<>();
        List<String> blueValues = new ArrayList<>();
        List<String> alphaValues = new ArrayList<>();

        for (int i = 0; i < 256; i++) {
            RGBA color = GradientUtil.colorAt(gradientColors, gradientRatio, i, GradientUtil.ColorInterpolation.SRGB);
            redValues.add("" + (color.red / 255f));
            greenValues.add("" + (color.green / 255f));
            blueValues.add("" + (color.blue / 255f));
            alphaValues.add("" + color.getAlphaFloat());
        }

        /*
        //In case we want to map 128 to center
        
        for (int i = 0; i < 126; i++) { //126 colors
            RGBA color = GradientUtil.colorAt(gradientColors, gradientRatio, i * 127f / 125f, GradientUtil.ColorInterpolation.SRGB);
            redValues.add("" + (color.red / 255f));
            greenValues.add("" + (color.green / 255f));
            blueValues.add("" + (color.blue / 255f));
            alphaValues.add("" + color.getAlphaFloat());
        }
        for (int i = 128; i < 256; i++) { //1 center + 126 colors
            RGBA color = GradientUtil.colorAt(gradientColors, gradientRatio, i, GradientUtil.ColorInterpolation.SRGB);
            redValues.add("" + (color.red / 255f));
            greenValues.add("" + (color.green / 255f));
            blueValues.add("" + (color.blue / 255f));
            alphaValues.add("" + color.getAlphaFloat());
        }
         */
        Element feFuncR = document.createElement("feFuncR");
        feFuncR.setAttribute("type", "table");
        feFuncR.setAttribute("tableValues", String.join(" ", redValues));
        Element feFuncG = document.createElement("feFuncG");
        feFuncG.setAttribute("type", "table");
        feFuncG.setAttribute("tableValues", String.join(" ", greenValues));
        Element feFuncB = document.createElement("feFuncB");
        feFuncB.setAttribute("type", "table");
        feFuncB.setAttribute("tableValues", String.join(" ", blueValues));
        Element feFuncA = document.createElement("feFuncA");
        feFuncA.setAttribute("type", "table");
        feFuncA.setAttribute("tableValues", String.join(" ", alphaValues));
        feComponentTransfer.appendChild(feFuncR);
        feComponentTransfer.appendChild(feFuncG);
        feComponentTransfer.appendChild(feFuncB);
        feComponentTransfer.appendChild(feFuncA);

        String result = exporter.getUniqueId("filterResult");
        feComponentTransfer.setAttribute("result", result);
        filtersElement.appendChild(feComponentTransfer);
        return result;
    }

    /**
     * Drop shadow
     *
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
    public static String dropShadow(
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
        double dx = distance * exporter.getZoom() * Math.cos(angle);
        double dy = distance * exporter.getZoom() * Math.sin(angle);

        Element feOffset = document.createElement("feOffset");
        feOffset.setAttribute("dx", "" + dx);
        feOffset.setAttribute("dy", "" + dy);
        feOffset.setAttribute("in", in);
        String feOffsetResult = exporter.getUniqueId("filterResult");
        feOffset.setAttribute("result", feOffsetResult);
        filtersElement.appendChild(feOffset);

        String result;
        if (innerShadow) {
            Element feFlood = document.createElement("feFlood");
            feFlood.setAttribute("flood-color", dropShadowColor.toHexRGB());
            feFlood.setAttribute("flood-opacity", "" + dropShadowColor.getAlphaFloat());
            filtersElement.appendChild(feFlood);
            String feFloodResult = exporter.getUniqueId("filterResult");
            feFlood.setAttribute("result", feFloodResult);

            Element feComposite1 = document.createElement("feComposite");
            feComposite1.setAttribute("in", feFloodResult);
            feComposite1.setAttribute("in2", feOffsetResult);
            feComposite1.setAttribute("operator", "out");
            result = exporter.getUniqueId("filterResult");
            feComposite1.setAttribute("result", result);
            filtersElement.appendChild(feComposite1);
        } else {
            Element feColorMatrix = document.createElement("feColorMatrix");
            feColorMatrix.setAttribute("type", "matrix");
            feColorMatrix.setAttribute("in", feOffsetResult);
            feColorMatrix.setAttribute("values",
                    "0 0 0 0 " + (dropShadowColor.red / 255f) + ","
                    + "0 0 0 0 " + (dropShadowColor.green / 255f) + ","
                    + "0 0 0 0 " + (dropShadowColor.blue / 255f) + ","
                    + "0 0 0 " + (dropShadowColor.alpha / 255f) + " 0" //Note: it is not last one here
            );
            result = exporter.getUniqueId("filterResult");
            feColorMatrix.setAttribute("result", result);
            filtersElement.appendChild(feColorMatrix);
        }

        if (Double.compare(blurX, 0.0) != 0 || Double.compare(blurY, 0.0) != 0) {
            result = blur(blurX, blurY, iterations, document, filtersElement, exporter, result);
        }

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
        return compose(in, result, innerShadow ? Filtering.INNER : Filtering.OUTER, knockout, compositeSource, document, filtersElement, exporter);
    }
}

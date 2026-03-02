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
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ConcreteClasses;
import com.jpexs.helpers.GradientUtil;
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
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
     * FILTER is enabled. Used internally by Simple editor GUI. Not a real SWF
     * field.
     */
    @Internal
    public boolean enabled = true;

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
     *
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
     *
     * @return Delta X
     */
    public abstract double getDeltaX();

    /**
     * Gets delta Y
     *
     * @return Delta Y
     */
    public abstract double getDeltaY();

    /**
     * Converts filter to SVG.
     *
     * @param document Document
     * @param filtersElement Filters element
     * @param exporter SVG exporter
     * @param in Input
     * @return SVG representation of the filter
     */
    public abstract String toSvg(Document document, Element filtersElement, SVGExporter exporter, String in);

    /**
     * Converts drop shadow to SVG.
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
        return dropShadowSvg(
                distance,
                angle,
                new RGBA[]{dropShadowColor},
                new int[0],
                innerShadow,
                knockout,
                compositeSource,
                blurX,
                blurY,
                strength,
                iterations,
                document,
                filtersElement,
                exporter,
                in
        );
    }

    /**
     * Converts drop shadow to SVG.
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
    protected String dropShadowSvg(
            double distance,
            double angle,
            RGBA[] gradientColors,
            int[] gradientRatio,
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

        RGBA dropShadowColor = gradientColors.length == 1 ? gradientColors[0] : new RGBA(0, 0, 0, 255);

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
    protected String blurSvg(double blurX, double blurY, int iterations, Document document, Element filtersElement, SVGExporter exporter, String in) {
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

        return blurSvg(blurX, blurY, iterations - 1, document, filtersElement, exporter, result);
    }

    /**
     * Converts gradient ratios to Java format float ratios.
     *
     * @param input 0-255 values
     * @return 0f - 1f values, strictly increasing
     */
    public static float[] convertRatiosToJavaGradient(int[] input) {
        int n = input.length;
        float[] output = new float[n];
        final float max = 1.0f;
        final float epsilon = 0.000001f;

        for (int i = 0; i < n; i++) {
            output[i] = input[i] / 255f;
        }

        for (int i = 1; i < n; i++) {
            if (output[i] <= output[i - 1]) {
                float proposed = output[i - 1] + epsilon;

                if (proposed > max) {
                    int j = i - 1;
                    while (j >= 0 && output[j] >= (max - (i - j) * epsilon)) {
                        j--;
                    }

                    if (j < 0) {
                        for (int k = i - (i); k <= i; k++) {
                            output[k] = max - (i - k) * epsilon;
                        }
                    } else {
                        for (int k = j + 1; k <= i; k++) {
                            output[k] = output[k - 1] + epsilon;
                        }
                    }
                } else {
                    output[i] = proposed;
                }
            }
        }

        return output;
    }

    protected String bevelSvg(double distance, double angle, RGBA[] gradientColors, int[] gradientRatio, boolean knockout, boolean onTop, boolean innerShadow, double blurX, double blurY, double strength, int passes, Document document, Element filtersElement, SVGExporter exporter, String in) {
        RGBA highlightColor = new RGBA(255, 0, 0, 255);
        RGBA shadowColor = new RGBA(0, 0, 255, 255);

        int type = Filtering.INNER;
        if (onTop && !innerShadow) {
            type = Filtering.FULL;
        } else if (!innerShadow) {
            type = Filtering.OUTER;
        }

        String shadowInner = null;
        String hilightInner = null;
        if (type != Filtering.OUTER) {
            String hilight = dropShadowSvg(distance, angle, highlightColor, true, true, true, 0, 0, strength, passes, document, filtersElement, exporter, in);
            String shadow = dropShadowSvg(distance, angle + Math.PI, shadowColor, true, true, true, 0, 0, strength, passes, document, filtersElement, exporter, in);

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
            String hilight = dropShadowSvg(distance, angle + Math.PI, highlightColor, false, true, true, 0, 0, strength, passes, document, filtersElement, exporter, in);
            String shadow = dropShadowSvg(distance, angle, shadowColor, false, true, true, 0, 0, strength, passes, document, filtersElement, exporter, in);

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

        result = blurSvg(blurX, blurY, passes, document, filtersElement, exporter, result);

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

        if (type == Filtering.INNER) {
            Element feComposite6 = document.createElement("feComposite");
            feComposite6.setAttribute("in", result);
            feComposite6.setAttribute("in2", in);
            feComposite6.setAttribute("operator", "in");
            result = exporter.getUniqueId("filterResult");
            feComposite6.setAttribute("result", result);
            filtersElement.appendChild(feComposite6);
        }
        if (type == Filtering.OUTER) {
            Element feComposite6 = document.createElement("feComposite");
            feComposite6.setAttribute("in", result);
            feComposite6.setAttribute("in2", in);
            feComposite6.setAttribute("operator", "out");
            result = exporter.getUniqueId("filterResult");
            feComposite6.setAttribute("result", result);
            filtersElement.appendChild(feComposite6);
        }

        if (!knockout) {
            Element feComposite7 = document.createElement("feComposite");
            feComposite7.setAttribute("in", result);
            feComposite7.setAttribute("in2", in);
            feComposite7.setAttribute("operator", "over");
            result = exporter.getUniqueId("filterResult");
            feComposite7.setAttribute("result", result);
            filtersElement.appendChild(feComposite7);
        }
        return result;
    }

    private String prepareFeComponentTransfer(RGBA[] gradientColors, int[] gradientRatio, Document document, Element filtersElement, SVGExporter exporter, String in) {
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
}

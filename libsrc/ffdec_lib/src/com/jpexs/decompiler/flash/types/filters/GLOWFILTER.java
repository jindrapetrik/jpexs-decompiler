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
import com.jpexs.helpers.SerializableImage;
import java.awt.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Glow filter.
 *
 * @author JPEXS
 */
public class GLOWFILTER extends FILTER {

    /**
     * Color of the shadow
     */
    public RGBA glowColor = new RGBA(Color.RED);

    /**
     * Horizontal blur amount
     */
    @SWFType(BasicType.FIXED)
    public double blurX = 5;

    /**
     * Vertical blur amount
     */
    @SWFType(BasicType.FIXED)
    public double blurY = 5;

    /**
     * Strength of the glow
     */
    @SWFType(BasicType.FIXED8)
    public float strength = 1;

    /**
     * Inner glow mode
     */
    public boolean innerGlow = false;

    /**
     * Knockout mode
     */
    public boolean knockout = false;

    /**
     * Composite source
     */
    public boolean compositeSource = true;

    /**
     * Number of blur passes
     */
    @SWFType(value = BasicType.UB, count = 5)
    public int passes = 1;

    /**
     * Constructor
     */
    public GLOWFILTER() {
        super(2);
    }

    @Override
    public SerializableImage apply(SerializableImage src, double zoom, int srcX, int srcY, int srcW, int srcH) {
        return Filtering.glow(src, (int) Math.round(blurX * zoom), (int) Math.round(blurY * zoom), strength, glowColor.toColor(), innerGlow, knockout, passes);
    }

    @Override
    public double getDeltaX() {
        return blurX;
    }

    @Override
    public double getDeltaY() {
        return blurY;
    }

    @Override
    public String toSvg(Document document, Element filtersElement, SVGExporter exporter, String in) {
        /*Element element = document.createElement("feDropShadow");
        element.setAttribute("dx", "0");
        element.setAttribute("dy", "0");

        double blur = ((blurX + blurY) / 2);
        element.setAttribute("stdDeviation", "" + blur);

        element.setAttribute("flood-color", glowColor.toHexRGB());
        element.setAttribute("flood-opacity", "" + glowColor.getAlphaFloat());

        element.setAttribute("in", in);
        
        String result = exporter.getUniqueId("filterResult");
        element.setAttribute("result", result);
        
        filtersElement.appendChild(element);
        
        return result;*/

        if (innerGlow) {
            Element feFlood = document.createElement("feFlood");
            feFlood.setAttribute("flood-color", glowColor.toHexRGB());
            feFlood.setAttribute("flood-opacity", "" + glowColor.getAlphaFloat());
            filtersElement.appendChild(feFlood);
            String feFloodResult = exporter.getUniqueId("filterResult");
            feFlood.setAttribute("result", feFloodResult);

            Element feComposite1 = document.createElement("feComposite");
            feComposite1.setAttribute("in", feFloodResult);
            feComposite1.setAttribute("in2", in);
            feComposite1.setAttribute("operator", "out");
            String feComposite1Result = exporter.getUniqueId("filterResult");
            feComposite1.setAttribute("result", feComposite1Result);
            filtersElement.appendChild(feComposite1);

            String blurResult = blurSvg(blurX, blurY, passes, document, filtersElement, exporter, feComposite1Result);

            Element feComposite2 = document.createElement("feComposite");
            feComposite2.setAttribute("in", blurResult);
            feComposite2.setAttribute("in2", in);
            feComposite2.setAttribute("operator", "in");

            String feComposite2Result = exporter.getUniqueId("filterResult");
            feComposite2.setAttribute("result", feComposite2Result);
            filtersElement.appendChild(feComposite2);

            if (knockout || !compositeSource) {
                return feComposite2Result;
            }
            Element feComposite3 = document.createElement("feComposite");
            feComposite3.setAttribute("in", feComposite2Result);
            feComposite3.setAttribute("in2", in);
            feComposite3.setAttribute("operator", "over");

            String feComposite3Result = exporter.getUniqueId("filterResult");
            feComposite3.setAttribute("result", feComposite3Result);
            filtersElement.appendChild(feComposite3);
            return feComposite3Result;
        } else {

            Element feColorMatrix = document.createElement("feColorMatrix");
            feColorMatrix.setAttribute("type", "matrix");
            feColorMatrix.setAttribute("in", in);
            feColorMatrix.setAttribute("values",
                    "0 0 0 0 " + (glowColor.red / 255f) + ","
                    + "0 0 0 0 " + (glowColor.green / 255f) + ","
                    + "0 0 0 0 " + (glowColor.blue / 255f) + ","
                    + "0 0 0 1 0"
            );
            String feColorMatrixResult = exporter.getUniqueId("filterResult");
            feColorMatrix.setAttribute("result", feColorMatrixResult);
            filtersElement.appendChild(feColorMatrix);

            String blurResult = blurSvg(blurX, blurY, passes, document, filtersElement, exporter, feColorMatrixResult);

            if (!knockout && !compositeSource) {
                return blurResult;
            }

            Element feComposite = document.createElement("feComposite");
            if (knockout) {
                feComposite.setAttribute("in", blurResult);
                feComposite.setAttribute("in2", in);
                feComposite.setAttribute("operator", "out");
            } else {
                feComposite.setAttribute("in", in);
                feComposite.setAttribute("in2", blurResult);
                feComposite.setAttribute("operator", "over");
            }

            String feCompositeResult = exporter.getUniqueId("filterResult");
            feComposite.setAttribute("result", feCompositeResult);
            filtersElement.appendChild(feComposite);
            return feCompositeResult;
        }
    }
}

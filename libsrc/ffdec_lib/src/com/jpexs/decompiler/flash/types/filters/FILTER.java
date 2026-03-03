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

import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.ConcreteClasses;
import com.jpexs.helpers.SerializableImage;
import java.io.Serializable;
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
}

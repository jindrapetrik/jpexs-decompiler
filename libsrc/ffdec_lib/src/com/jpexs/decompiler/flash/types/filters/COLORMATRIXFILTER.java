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
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Applies a color transformation on the pixels of a display list object.
 *
 * @author JPEXS
 */
public class COLORMATRIXFILTER extends FILTER {

    /**
     * Color matrix values
     */
    @SWFType(BasicType.FLOAT)
    @SWFArray(count = 20)
    public float[] matrix = new float[]{
        1, 0, 0, 0, 0,
        0, 1, 0, 0, 0,
        0, 0, 1, 0, 0,
        0, 0, 0, 1, 0
    };

    /**
     * Constructor
     */
    public COLORMATRIXFILTER() {
        super(6);
    }

    @Override
    public SerializableImage apply(SerializableImage src, double zoom, int srcX, int srcY, int srcW, int srcH) {
        float[][] matrix2 = new float[4][5];
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 5; x++) {
                matrix2[y][x] = matrix[y * 5 + x];
            }
        }
        return Filtering.colorMatrix(src, matrix2);
    }

    @Override
    public double getDeltaX() {
        return 0;
    }

    @Override
    public double getDeltaY() {
        return 0;
    }

    @Override
    public String toSvg(Document document, Element filtersElement, SVGExporter exporter, String in) {
        Element element = document.createElement("feColorMatrix");
        element.setAttribute("type", "matrix");
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            if (i % 5 == 4) {
                parts.add("" + (matrix[i] / 255f));
            } else {
                parts.add("" + matrix[i]);
            }
        }
        element.setAttribute("values", String.join(" ", parts));
        element.setAttribute("in", in);

        String result = exporter.getUniqueId("filterResult");
        element.setAttribute("result", result);

        filtersElement.appendChild(element);

        return result;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Arrays.hashCode(this.matrix);
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
        final COLORMATRIXFILTER other = (COLORMATRIXFILTER) obj;
        return Arrays.equals(this.matrix, other.matrix);
    }

    
}

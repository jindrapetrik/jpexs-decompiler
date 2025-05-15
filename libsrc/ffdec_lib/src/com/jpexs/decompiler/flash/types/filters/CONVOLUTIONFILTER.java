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
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Two-dimensional discrete convolution filter.
 *
 * @author JPEXS
 */
public class CONVOLUTIONFILTER extends FILTER {

    /**
     * Horizontal matrix size
     */
    @SWFType(BasicType.UI8)
    public int matrixX = 3;

    /**
     * Vertical matrix size
     */
    @SWFType(BasicType.UI8)
    public int matrixY = 3;

    /**
     * Divisor applied to the matrix values
     */
    @SWFType(BasicType.FLOAT)
    public float divisor = 1f;

    /**
     * Bias applied to the matrix values
     */
    @SWFType(BasicType.FLOAT)
    public float bias = 0f;

    /**
     * Matrix values
     */
    @SWFType(value = BasicType.FLOAT, countField = "matrixX * matrixY", canAdd = false)
    public float[] matrix = new float[]{
        0, 0, 0,
        0, 1, 0,
        0, 0, 0
    };

    /**
     * Default color for pixels outside the image
     */
    public RGBA defaultColor = new RGBA(0, 0, 0, 0);

    @Reserved
    @SWFType(value = BasicType.UB, count = 6)
    public int reserved;

    /**
     * Clamp mode
     */
    public boolean clamp = true;

    /**
     * Preserve the alpha
     */
    public boolean preserveAlpha = false;

    /**
     * Constructor
     */
    public CONVOLUTIONFILTER() {
        super(5);
    }

    @Override
    public SerializableImage apply(SerializableImage src, double zoom, int srcX, int srcY, int srcW, int srcH) {
        return Filtering.convolution(src, matrix, matrixX, matrixY, divisor, bias, defaultColor.toColor(), clamp, preserveAlpha, srcX, srcY, srcW, srcH);
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
        Element element = document.createElement("feConvolveMatrix");
        element.setAttribute("order", "" + matrixX + " " + matrixY);
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            parts.add("" + matrix[i]);
        }
        element.setAttribute("kernelMatrix", String.join(" ", parts));
        if (clamp) {
            element.setAttribute("edgeMode", "duplicate");
        } else {
            element.setAttribute("edgeMode", "none");
        }

        element.setAttribute("preserveAlpha", preserveAlpha ? "true" : "false");

        element.setAttribute("divisor", "" + divisor);
        element.setAttribute("bias", "" + bias);

        element.setAttribute("in", in);

        String result = exporter.getUniqueId("filterResult");
        element.setAttribute("result", result);

        filtersElement.appendChild(element);

        return result;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.matrixX;
        hash = 53 * hash + this.matrixY;
        hash = 53 * hash + Float.floatToIntBits(this.divisor);
        hash = 53 * hash + Float.floatToIntBits(this.bias);
        hash = 53 * hash + Arrays.hashCode(this.matrix);
        hash = 53 * hash + Objects.hashCode(this.defaultColor);
        hash = 53 * hash + this.reserved;
        hash = 53 * hash + (this.clamp ? 1 : 0);
        hash = 53 * hash + (this.preserveAlpha ? 1 : 0);
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
        final CONVOLUTIONFILTER other = (CONVOLUTIONFILTER) obj;
        if (this.matrixX != other.matrixX) {
            return false;
        }
        if (this.matrixY != other.matrixY) {
            return false;
        }
        if (Float.floatToIntBits(this.divisor) != Float.floatToIntBits(other.divisor)) {
            return false;
        }
        if (Float.floatToIntBits(this.bias) != Float.floatToIntBits(other.bias)) {
            return false;
        }
        if (this.reserved != other.reserved) {
            return false;
        }
        if (this.clamp != other.clamp) {
            return false;
        }
        if (this.preserveAlpha != other.preserveAlpha) {
            return false;
        }
        if (!Arrays.equals(this.matrix, other.matrix)) {
            return false;
        }
        return Objects.equals(this.defaultColor, other.defaultColor);
    }
    
    
}

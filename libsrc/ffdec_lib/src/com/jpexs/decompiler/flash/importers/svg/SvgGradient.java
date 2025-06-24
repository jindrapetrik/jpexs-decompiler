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
package com.jpexs.decompiler.flash.importers.svg;

import java.awt.Color;
import java.util.List;

/**
 * SVG gradient.
 *
 * @author JPEXS
 */
abstract class SvgGradient extends SvgFill {

    /**
     * Stops
     */
    public List<SvgStop> stops;

    /**
     * Gradient units
     */
    public SvgGradientUnits gradientUnits;

    /**
     * Gradient transform
     */
    public String gradientTransform;

    /**
     * Spread method
     */
    public SvgSpreadMethod spreadMethod;

    /**
     * Interpolation
     */
    public SvgInterpolation interpolation;

    /**
     * Constructor.
     */
    public SvgGradient() {

    }

    @Override
    public Color toColor() {
        if (stops.isEmpty()) {
            return Color.BLACK;
        }
        return stops.get(0).color;
    }
}

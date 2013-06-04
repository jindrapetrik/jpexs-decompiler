/*
 *  Copyright (C) 2010-2013 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.types.RGBA;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Bevel filter with gradient instead of single color
 *
 * @author JPEXS
 */
public class GRADIENTBEVELFILTER extends FILTER {

    /**
     * Gradient colors
     */
    public RGBA gradientColors[];
    /**
     * Gradient ratios
     */
    public int gradientRatio[];
    /**
     * Horizontal blur amount
     */
    public double blurX;
    /**
     * Vertical blur amount
     */
    public double blurY;
    /**
     * Radian angle of the gradient bevel
     */
    public double angle;
    /**
     * Distance of the gradient bevel
     */
    public double distance;
    /**
     * Strength of the gradient bevel
     */
    public float strength;
    /**
     * Inner bevel mode
     */
    public boolean innerShadow;
    /**
     * Knockout mode
     */
    public boolean knockout;
    /**
     * Composite source
     */
    public boolean compositeSource;
    /**
     * OnTop mode
     */
    public boolean onTop;
    /**
     * Number of blur passes
     */
    public int passes;

    public GRADIENTBEVELFILTER() {
        super(7);
    }

    @Override
    public BufferedImage apply(BufferedImage src) {
        List<Color> colors = new ArrayList<>();
        List<Float> ratios = new ArrayList<>();
        for (int i = 0; i < gradientColors.length; i++) {
            if ((i > 0) && (gradientRatio[i - 1] == gradientRatio[i])) {
                continue;
            }
            colors.add(gradientColors[i].toColor());
            ratios.add(gradientRatio[i] / 255f);
        }
        float ratiosAr[] = new float[ratios.size()];
        for (int i = 0; i < ratios.size(); i++) {
            ratiosAr[i] = ratios.get(i);
        }
        Color colorsArr[] = colors.toArray(new Color[colors.size()]);
        int type = Filtering.INNER;
        if (onTop && !innerShadow) {
            type = Filtering.FULL;
        } else if (!innerShadow) {
            type = Filtering.OUTER;
        }

        return Filtering.gradientBevel(src, colorsArr, ratiosAr, (int) blurX, (int) blurY, strength, type, (int) (angle * 180 / Math.PI), (float) distance, knockout, passes);
    }
}

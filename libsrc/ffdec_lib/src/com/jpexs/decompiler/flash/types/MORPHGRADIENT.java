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
package com.jpexs.decompiler.flash.types;

import static com.jpexs.decompiler.flash.types.GRADIENT.INTERPOLATION_LINEAR_RGB_MODE;
import static com.jpexs.decompiler.flash.types.GRADIENT.INTERPOLATION_RGB_MODE;
import static com.jpexs.decompiler.flash.types.GRADIENT.SPREAD_PAD_MODE;
import static com.jpexs.decompiler.flash.types.GRADIENT.SPREAD_REFLECT_MODE;
import static com.jpexs.decompiler.flash.types.GRADIENT.SPREAD_REPEAT_MODE;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;

/**
 * Morph gradient.
 *
 * @author JPEXS
 */
public class MORPHGRADIENT implements Serializable {

    /**
     * Spread mode. See GRADIENT.SPREAD_* constants
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = SPREAD_PAD_MODE, text = "Pad")
    @EnumValue(value = SPREAD_REFLECT_MODE, text = "Reflect")
    @EnumValue(value = SPREAD_REPEAT_MODE, text = "Repeat")
    public int spreadMode;

    /**
     * Interpolation mode. See GRADIENT.INTERPOLATION_* constants
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = INTERPOLATION_RGB_MODE, text = "Normal RGB")
    @EnumValue(value = INTERPOLATION_LINEAR_RGB_MODE, text = "Linear RGB")
    public int interpolationMode;

    /**
     * Gradient records
     */
    public MORPHGRADRECORD[] gradientRecords;

    /**
     * Morphs two colors at given ratio.
     * @param c1 Color 1
     * @param c2 Color 2
     * @param ratio Ratio
     * @return Morphed color
     */
    public static RGBA morphColor(RGBA c1, RGBA c2, int ratio) {
        int r = (int) (c1.red + (c2.red - c1.red) * ratio / 65535.0 + 0.5);
        int g = (int) (c1.green + (c2.green - c1.green) * ratio / 65535.0 + 0.5);
        int b = (int) (c1.blue + (c2.blue - c1.blue + 0.5) * ratio / 65535.0 + 0.5);
        int a = (int) (c1.alpha + (c2.alpha - c1.alpha) * ratio / 65535.0f + 0.5);
        if (r > 255) {
            r = 255;
        }
        if (g > 255) {
            g = 255;
        }
        if (b > 255) {
            b = 255;
        }
        if (a > 255) {
            a = 255;
        }
        return new RGBA(r, g, b, a);
    }

    /**
     * Gets gradient at given ratio.
     * @param ratio Ratio
     * @return Gradient
     */
    public GRADIENT getGradientAt(int ratio) {
        GRADIENT ret = new GRADIENT();
        ret.spreadMode = spreadMode;
        ret.interpolationMode = interpolationMode;
        ret.gradientRecords = new GRADRECORD[gradientRecords.length];
        for (int m = 0; m < gradientRecords.length; m++) {

            int gratio = (gradientRecords[m].startRatio + (gradientRecords[m].endRatio - gradientRecords[m].startRatio) * ratio / 65535);
            ret.gradientRecords[m] = new GRADRECORD();
            ret.gradientRecords[m].color = morphColor(gradientRecords[m].startColor, gradientRecords[m].endColor, ratio);
            ret.gradientRecords[m].ratio = gratio;
        }
        return ret;
    }

    /**
     * Gets start gradient.
     * @return Start gradient
     */
    public GRADIENT getStartGradient() {
        GRADIENT ret = new GRADIENT();
        ret.spreadMode = spreadMode;
        ret.interpolationMode = interpolationMode;
        ret.gradientRecords = new GRADRECORD[gradientRecords.length];
        for (int m = 0; m < gradientRecords.length; m++) {
            ret.gradientRecords[m] = gradientRecords[m].getStartRecord();
        }
        return ret;
    }

    /**
     * Gets end gradient.
     * @return End gradient
     */
    public GRADIENT getEndGradient() {
        GRADIENT ret = new GRADIENT();
        ret.spreadMode = spreadMode;
        ret.interpolationMode = interpolationMode;
        ret.gradientRecords = new GRADRECORD[gradientRecords.length];
        for (int m = 0; m < gradientRecords.length; m++) {
            ret.gradientRecords[m] = gradientRecords[m].getEndRecord();
        }
        return ret;
    }
}

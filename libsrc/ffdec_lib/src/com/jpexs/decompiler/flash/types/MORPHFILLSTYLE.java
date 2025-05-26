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
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.Set;

/**
 * Morph fill style.
 *
 * @author JPEXS
 */
public class MORPHFILLSTYLE implements NeedsCharacters, Serializable {

    /**
     * Fill style type
     */
    @SWFType(BasicType.UI8)
    @EnumValue(value = SOLID, text = "Solid")
    @EnumValue(value = LINEAR_GRADIENT, text = "Linear gradient")
    @EnumValue(value = RADIAL_GRADIENT, text = "Radial gradient")
    @EnumValue(value = FOCAL_RADIAL_GRADIENT, text = "Focal radial gradient", minSwfVersion = 8)
    @EnumValue(value = REPEATING_BITMAP, text = "Repeating bitmap")
    @EnumValue(value = CLIPPED_BITMAP, text = "Clipped bitmap")
    @EnumValue(value = NON_SMOOTHED_REPEATING_BITMAP, text = "Non smoothed repeating bitmap")
    @EnumValue(value = NON_SMOOTHED_CLIPPED_BITMAP, text = "Non smoothed clipped bitmap")
    public int fillStyleType;

    /**
     * Type - solid
     */
    public static final int SOLID = 0x0;

    /**
     * Type - linear gradient
     */
    public static final int LINEAR_GRADIENT = 0x10;

    /**
     * Type - radial gradient
     */
    public static final int RADIAL_GRADIENT = 0x12;

    /**
     * Type - focal radial gradient
     */
    public static final int FOCAL_RADIAL_GRADIENT = 0x13;

    /**
     * Type - repeating bitmap
     */
    public static final int REPEATING_BITMAP = 0x40;

    /**
     * Type - clipped bitmap
     */
    public static final int CLIPPED_BITMAP = 0x41;

    /**
     * Type - non smoothed repeating bitmap
     */
    public static final int NON_SMOOTHED_REPEATING_BITMAP = 0x42;

    /**
     * Type - non smoothed clipped bitmap
     */
    public static final int NON_SMOOTHED_CLIPPED_BITMAP = 0x43;

    /**
     * Start color
     */
    @Conditional(value = "fillStyleType", options = {SOLID})
    public RGBA startColor;

    /**
     * End color
     */
    @Conditional(value = "fillStyleType", options = {SOLID})
    public RGBA endColor;

    /**
     * Start gradient matrix
     */
    @Conditional(value = "fillStyleType", options = {LINEAR_GRADIENT, RADIAL_GRADIENT, FOCAL_RADIAL_GRADIENT})
    public MATRIX startGradientMatrix;

    /**
     * End gradient matrix
     */
    @Conditional(value = "fillStyleType", options = {LINEAR_GRADIENT, RADIAL_GRADIENT, FOCAL_RADIAL_GRADIENT})
    public MATRIX endGradientMatrix;

    /**
     * Gradient
     */
    @Conditional(value = "fillStyleType", options = {LINEAR_GRADIENT, RADIAL_GRADIENT, FOCAL_RADIAL_GRADIENT})
    public MORPHGRADIENT gradient;

    /**
     * Bitmap id
     */
    @Conditional(value = "fillStyleType", options = {REPEATING_BITMAP, CLIPPED_BITMAP, NON_SMOOTHED_REPEATING_BITMAP, NON_SMOOTHED_CLIPPED_BITMAP})
    public int bitmapId;

    /**
     * Start bitmap matrix
     */
    @Conditional(value = "fillStyleType", options = {REPEATING_BITMAP, CLIPPED_BITMAP, NON_SMOOTHED_REPEATING_BITMAP, NON_SMOOTHED_CLIPPED_BITMAP})
    public MATRIX startBitmapMatrix;

    /**
     * End bitmap matrix
     */
    @Conditional(value = "fillStyleType", options = {REPEATING_BITMAP, CLIPPED_BITMAP, NON_SMOOTHED_REPEATING_BITMAP, NON_SMOOTHED_CLIPPED_BITMAP})
    public MATRIX endBitmapMatrix;

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        if ((fillStyleType == REPEATING_BITMAP)
                || (fillStyleType == CLIPPED_BITMAP)
                || (fillStyleType == NON_SMOOTHED_REPEATING_BITMAP)
                || (fillStyleType == NON_SMOOTHED_CLIPPED_BITMAP)) {
            if (bitmapId == 65535) { //In some cases, this special value is used, but is not used. Ignore it. (#1851)
                return;
            }
            CharacterTag character = swf.getCharacter(bitmapId);
            if (character instanceof ImageTag) {
                needed.add(bitmapId);
            }
        }
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        if (bitmapId == oldCharacterId) {
            bitmapId = newCharacterId;
            return true;
        }
        return false;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        if (bitmapId == characterId) {
            if ((fillStyleType == REPEATING_BITMAP)
                    || (fillStyleType == CLIPPED_BITMAP)
                    || (fillStyleType == NON_SMOOTHED_REPEATING_BITMAP)
                    || (fillStyleType == NON_SMOOTHED_CLIPPED_BITMAP)) {
                fillStyleType = SOLID;
            }
            bitmapId = 0;
            return true;
        }
        return false;
    }

    private MATRIX morphMatrix(MATRIX a, MATRIX b, int ratio) {
        if (a == null) {
            return null;
        }
        if (b == null) {
            return null;
        }
        MATRIX ret = new MATRIX();
        double ratio_d = ratio / 65535.0;
        ret.scaleX = MATRIX.toFloat((int) Math.round(a.getScaleXInteger() + (b.getScaleXInteger() - a.getScaleXInteger()) * ratio_d));
        ret.scaleY = MATRIX.toFloat((int) Math.round(a.getScaleYInteger() + (b.getScaleYInteger() - a.getScaleYInteger()) * ratio_d));
        ret.rotateSkew0 = MATRIX.toFloat((int) Math.round(a.getRotateSkew0Integer() + (b.getRotateSkew0Integer() - a.getRotateSkew0Integer()) * ratio_d));
        ret.rotateSkew1 = MATRIX.toFloat((int) Math.round(a.getRotateSkew1Integer() + (b.getRotateSkew1Integer() - a.getRotateSkew1Integer()) * ratio_d));
        ret.translateX = (int) Math.round(a.translateX + (b.translateX - a.translateX) * ratio_d);
        ret.translateY = (int) Math.round(a.translateY + (b.translateY - a.translateY) * ratio_d);
        ret.hasRotate = true;
        ret.hasScale = true;
        return ret;
    }

    /**
     * Gets fill style at given ratio.
     * @param ratio Ratio
     * @return Fill style
     */
    public FILLSTYLE getFillStyleAt(int ratio) {
        FILLSTYLE ret = new FILLSTYLE();
        ret.bitmapId = bitmapId;
        if (startBitmapMatrix != null) {
            ret.bitmapMatrix = morphMatrix(startBitmapMatrix, endBitmapMatrix, ratio);
        }
        if (startColor != null) {
            ret.color = MORPHGRADIENT.morphColor(startColor, endColor, ratio);
        }
        ret.fillStyleType = fillStyleType;
        if (gradient != null) {
            ret.gradient = gradient.getGradientAt(ratio);
        }
        if (startGradientMatrix != null) {
            ret.gradientMatrix = morphMatrix(startGradientMatrix, endGradientMatrix, ratio);
        }
        return ret;
    }

    /**
     * Gets start fill style.
     * @return Start fill style
     */
    public FILLSTYLE getStartFillStyle() {
        FILLSTYLE ret = new FILLSTYLE();
        ret.bitmapId = bitmapId;
        ret.bitmapMatrix = startBitmapMatrix;
        ret.color = startColor;
        ret.fillStyleType = fillStyleType;
        if (gradient != null) {
            ret.gradient = gradient.getStartGradient();
        }
        ret.gradientMatrix = startGradientMatrix;
        return ret;
    }

    /**
     * Gets end fill style.
     * @return End fill style
     */
    public FILLSTYLE getEndFillStyle() {
        FILLSTYLE ret = new FILLSTYLE();
        ret.bitmapId = bitmapId;
        ret.bitmapMatrix = endBitmapMatrix;
        ret.color = endColor;
        ret.fillStyleType = fillStyleType;
        if (gradient != null) {
            ret.gradient = gradient.getEndGradient();
        }
        ret.gradientMatrix = endGradientMatrix;
        return ret;
    }
}

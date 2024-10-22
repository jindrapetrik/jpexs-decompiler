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

import com.jpexs.decompiler.flash.types.annotations.EnumValue;
import com.jpexs.decompiler.flash.types.annotations.SWFArray;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Gradient.
 *
 * @author JPEXS
 */
public class GRADIENT implements Serializable {

    /**
     * Spread mode
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = SPREAD_PAD_MODE, text = "Pad")
    @EnumValue(value = SPREAD_REFLECT_MODE, text = "Reflect")
    @EnumValue(value = SPREAD_REPEAT_MODE, text = "Repeat")
    public int spreadMode;

    /**
     * Spread mode - Pad
     */
    public static final int SPREAD_PAD_MODE = 0;

    /**
     * Spread mode - Reflect
     */
    public static final int SPREAD_REFLECT_MODE = 1;

    /**
     * Spread mode - Repeat
     */
    public static final int SPREAD_REPEAT_MODE = 2;

    /**
     * Spread mode - Reserved
     */
    public static final int SPREAD_RESERVED = 3;

    /**
     * Interpolation mode
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = INTERPOLATION_RGB_MODE, text = "Normal RGB")
    @EnumValue(value = INTERPOLATION_LINEAR_RGB_MODE, text = "Linear RGB")
    public int interpolationMode;

    /**
     * Interpolation mode - RGB
     */
    public static final int INTERPOLATION_RGB_MODE = 0;

    /**
     * Interpolation mode - Linear RGB
     */
    public static final int INTERPOLATION_LINEAR_RGB_MODE = 1;

    /**
     * Interpolation mode - Reserved 1
     */
    public static final int INTERPOLATION_RESERVED1 = 2;

    /**
     * Interpolation mode - Reserved 2
     */
    public static final int INTERPOLATION_RESERVED2 = 3;

    /**
     * Gradient records
     */
    @SWFArray(value = "record")
    public GRADRECORD[] gradientRecords = new GRADRECORD[0];

    /**
     * Checks if this gradient is compatible with other gradient.
     * @param otherGradient Other gradient
     * @return True if compatible
     */
    public boolean isCompatibleGradient(GRADIENT otherGradient) {
        if (interpolationMode != otherGradient.interpolationMode) {
            return false;
        }
        if (spreadMode != otherGradient.spreadMode) {
            return false;
        }
        if (gradientRecords.length != otherGradient.gradientRecords.length) {
            return false;
        }
        return true;
    }

    /**
     * Converts this gradient to morph gradient.
     * @return Morph gradient
     */
    public MORPHGRADIENT toMorphGradient() {
        MORPHGRADIENT morphGradient = new MORPHGRADIENT();
        morphGradient.interpolationMode = interpolationMode;
        morphGradient.spreadMode = spreadMode;
        morphGradient.gradientRecords = new MORPHGRADRECORD[gradientRecords.length];
        for (int i = 0; i < gradientRecords.length; i++) {
            morphGradient.gradientRecords[i] = gradientRecords[i].toMorphGradRecord();
        }
        return morphGradient;
    }

    /**
     * Converts this gradient to morph gradient.
     * @param endGradient End gradient
     * @return Morph gradient
     */
    public MORPHGRADIENT toMorphGradient(GRADIENT endGradient) {
        if (!isCompatibleGradient(endGradient)) {
            return null;
        }
        MORPHGRADIENT morphGradient;
        if (endGradient instanceof FOCALGRADIENT) {
            morphGradient = new MORPHFOCALGRADIENT();
        } else {
            morphGradient = new MORPHGRADIENT();
        }
        morphGradient.interpolationMode = interpolationMode;
        morphGradient.spreadMode = spreadMode;
        morphGradient.gradientRecords = new MORPHGRADRECORD[gradientRecords.length];
        for (int i = 0; i < gradientRecords.length; i++) {
            morphGradient.gradientRecords[i] = gradientRecords[i].toMorphGradRecord(endGradient.gradientRecords[i]);
        }
        if (endGradient instanceof FOCALGRADIENT) {
            ((MORPHFOCALGRADIENT) morphGradient).startFocalPoint = 0;
            ((MORPHFOCALGRADIENT) morphGradient).endFocalPoint = ((FOCALGRADIENT) endGradient).focalPoint;
        }
        return morphGradient;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.spreadMode;
        hash = 97 * hash + this.interpolationMode;
        hash = 97 * hash + Arrays.deepHashCode(this.gradientRecords);
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
        final GRADIENT other = (GRADIENT) obj;
        if (this.spreadMode != other.spreadMode) {
            return false;
        }
        if (this.interpolationMode != other.interpolationMode) {
            return false;
        }
        return Arrays.deepEquals(this.gradientRecords, other.gradientRecords);
    }
    
    public GRADIENT toShapeNum(int shapeNum) {
        GRADIENT result = Helper.deepCopy(this);
        if (shapeNum < 4) {
            result.spreadMode = 0;
            result.interpolationMode = 0;
            if (gradientRecords.length > 8) {
                result.gradientRecords = Arrays.copyOfRange(result.gradientRecords, 0, 8);
            }
        }
        return result;
    }

    public int getMinShapeNum() {
        if (gradientRecords.length > 8) {
            return 4;
        }
        if (spreadMode > 0) {
            return 4;
        }
        if (interpolationMode > 0) {
            return 4;
        }
        for (GRADRECORD rec : gradientRecords) {
            if (rec.color instanceof RGBA) {
                RGBA col = (RGBA) rec.color;
                if (col.alpha != 255) {
                    return 3;
                }
            }
        }
        return 1;
    }
}

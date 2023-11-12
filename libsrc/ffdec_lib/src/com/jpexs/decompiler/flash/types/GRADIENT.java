/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
import java.io.Serializable;
import java.util.Arrays;

/**
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

    public static final int SPREAD_PAD_MODE = 0;

    public static final int SPREAD_REFLECT_MODE = 1;

    public static final int SPREAD_REPEAT_MODE = 2;

    public static final int SPREAD_RESERVED = 3;

    /**
     * Interpolation mode
     */
    @SWFType(value = BasicType.UB, count = 2)
    @EnumValue(value = INTERPOLATION_RGB_MODE, text = "Normal RGB")
    @EnumValue(value = INTERPOLATION_LINEAR_RGB_MODE, text = "Linear RGB")
    public int interpolationMode;

    public static final int INTERPOLATION_RGB_MODE = 0;

    public static final int INTERPOLATION_LINEAR_RGB_MODE = 1;

    public static final int INTERPOLATION_RESERVED1 = 2;

    public static final int INTERPOLATION_RESERVED2 = 3;

    @SWFArray(value = "record")
    public GRADRECORD[] gradientRecords = new GRADRECORD[0];        
    
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
    
    public MORPHGRADIENT toMorphGradient() {
        MORPHGRADIENT morphGradient = new MORPHGRADIENT();
        morphGradient.interPolationMode = interpolationMode;
        morphGradient.spreadMode = spreadMode;
        morphGradient.gradientRecords = new MORPHGRADRECORD[gradientRecords.length];
        for (int i = 0; i < gradientRecords.length; i++) {
            morphGradient.gradientRecords[i] = gradientRecords[i].toMorphGradRecord();
        }
        return morphGradient;
    }
    
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
        morphGradient.interPolationMode = interpolationMode;
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
    
    
}

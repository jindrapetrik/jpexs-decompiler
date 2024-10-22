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

import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.Helper;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Focal gradient. Gradient with focal point. Used in radial gradients.
 *
 * @author JPEXS
 */
public class FOCALGRADIENT extends GRADIENT implements Serializable {

    /**
     * Focal point
     */
    @SWFType(BasicType.FIXED8)
    public float focalPoint;

    @Override
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

    @Override
    public MORPHGRADIENT toMorphGradient() {
        MORPHFOCALGRADIENT morphGradient = new MORPHFOCALGRADIENT();
        morphGradient.interpolationMode = interpolationMode;
        morphGradient.spreadMode = spreadMode;
        morphGradient.gradientRecords = new MORPHGRADRECORD[gradientRecords.length];
        for (int i = 0; i < gradientRecords.length; i++) {
            morphGradient.gradientRecords[i] = gradientRecords[i].toMorphGradRecord();
        }
        morphGradient.startFocalPoint = focalPoint;
        morphGradient.startFocalPoint = focalPoint;

        return morphGradient;
    }

    @Override
    public MORPHGRADIENT toMorphGradient(GRADIENT endGradient) {
        if (!isCompatibleGradient(endGradient)) {
            return null;
        }
        MORPHFOCALGRADIENT morphGradient = new MORPHFOCALGRADIENT();
        morphGradient.interpolationMode = interpolationMode;
        morphGradient.spreadMode = spreadMode;
        morphGradient.gradientRecords = new MORPHGRADRECORD[gradientRecords.length];
        for (int i = 0; i < gradientRecords.length; i++) {
            morphGradient.gradientRecords[i] = gradientRecords[i].toMorphGradRecord(endGradient.gradientRecords[i]);
        }
        morphGradient.startFocalPoint = focalPoint;
        if (endGradient instanceof FOCALGRADIENT) {
            morphGradient.endFocalPoint = ((FOCALGRADIENT) endGradient).focalPoint;
        } else {
            morphGradient.endFocalPoint = 0;
        }
        return morphGradient;
    }
    
    public GRADIENT toShapeNum(int shapeNum) {                        
        if (shapeNum < 4) {
            GRADIENT result = new GRADIENT();
            result.gradientRecords = Helper.deepCopy(gradientRecords);
            result.spreadMode = 0;
            result.interpolationMode = 0;
            if (result.gradientRecords.length > 8) {
                result.gradientRecords = Arrays.copyOfRange(result.gradientRecords, 0, 8);
            }
            return result;
        }
        return Helper.deepCopy(this);
    }
}

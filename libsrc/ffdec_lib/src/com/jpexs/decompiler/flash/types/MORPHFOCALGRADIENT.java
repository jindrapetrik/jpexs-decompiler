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
import java.io.Serializable;

/**
 * Morph focal gradient. Undocumented structure, but it exists.
 *
 * @author JPEXS
 */
public class MORPHFOCALGRADIENT extends MORPHGRADIENT implements Serializable {

    /**
     * Start focal point
     */
    @SWFType(BasicType.FIXED8)
    public float startFocalPoint;

    /**
     * End focal point
     */
    @SWFType(BasicType.FIXED8)
    public float endFocalPoint;

    @Override
    public GRADIENT getEndGradient() {
        FOCALGRADIENT ret = new FOCALGRADIENT();
        ret.spreadMode = spreadMode;
        ret.interpolationMode = interpolationMode;
        ret.gradientRecords = new GRADRECORD[gradientRecords.length];
        for (int m = 0; m < gradientRecords.length; m++) {
            ret.gradientRecords[m] = gradientRecords[m].getEndRecord();
        }
        ret.focalPoint = endFocalPoint;

        return ret;

    }

    @Override
    public GRADIENT getStartGradient() {
        FOCALGRADIENT ret = new FOCALGRADIENT();
        ret.spreadMode = spreadMode;
        ret.interpolationMode = interpolationMode;
        ret.gradientRecords = new GRADRECORD[gradientRecords.length];
        for (int m = 0; m < gradientRecords.length; m++) {
            ret.gradientRecords[m] = gradientRecords[m].getStartRecord();
        }
        ret.focalPoint = startFocalPoint;

        return ret;

    }

    @Override
    public GRADIENT getGradientAt(int ratio) {
        FOCALGRADIENT ret = new FOCALGRADIENT();
        ret.spreadMode = spreadMode;
        ret.interpolationMode = interpolationMode;
        ret.gradientRecords = new GRADRECORD[gradientRecords.length];
        for (int m = 0; m < gradientRecords.length; m++) {

            int gratio = (gradientRecords[m].startRatio + (gradientRecords[m].endRatio - gradientRecords[m].startRatio) * ratio / 65535);
            ret.gradientRecords[m] = new GRADRECORD();
            ret.gradientRecords[m].color = morphColor(gradientRecords[m].startColor, gradientRecords[m].endColor, ratio);
            ret.gradientRecords[m].ratio = gratio;
        }
        ret.focalPoint = (startFocalPoint + (endFocalPoint - startFocalPoint) * ratio / 65535);
        return ret;
    }
}

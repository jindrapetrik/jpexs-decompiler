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

import com.jpexs.decompiler.flash.types.annotations.Internal;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Gradient record.
 *
 * @author JPEXS
 */
public class GRADRECORD implements Serializable {

    /**
     * Ratio
     */
    @SWFType(BasicType.UI8)
    public int ratio;

    /**
     * In shape 3
     */
    @Internal
    public boolean inShape3;

    /**
     * Color
     */
    public RGB color;

    /**
     * Gets the ratio as a float
     * @return Ratio as a float
     */
    public float getRatioFloat() {
        return ((float) ratio) / 255.0f;
    }

    /**
     * Converts this record to a morph gradient record
     * @return Morph gradient record
     */
    public MORPHGRADRECORD toMorphGradRecord() {
        MORPHGRADRECORD morphGradRecord = new MORPHGRADRECORD();
        morphGradRecord.startColor = new RGBA(color);
        morphGradRecord.endColor = new RGBA(color);
        morphGradRecord.startRatio = ratio;
        morphGradRecord.endRatio = ratio;
        return morphGradRecord;
    }

    /**
     * Converts this record to a morph gradient record
     * @param endGradRecord End gradient record
     * @return Morph gradient record
     */
    public MORPHGRADRECORD toMorphGradRecord(GRADRECORD endGradRecord) {
        MORPHGRADRECORD morphGradRecord = new MORPHGRADRECORD();
        morphGradRecord.startColor = new RGBA(color);
        morphGradRecord.endColor = new RGBA(endGradRecord.color);
        morphGradRecord.startRatio = ratio;
        morphGradRecord.endRatio = endGradRecord.ratio;
        return morphGradRecord;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.ratio;
        hash = 19 * hash + (this.inShape3 ? 1 : 0);
        hash = 19 * hash + Objects.hashCode(this.color);
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
        final GRADRECORD other = (GRADRECORD) obj;
        if (this.ratio != other.ratio) {
            return false;
        }
        if (this.inShape3 != other.inShape3) {
            return false;
        }
        return Objects.equals(this.color, other.color);
    }

}

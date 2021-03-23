/*
 *  Copyright (C) 2010-2021 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author JPEXS
 */
public class SOUNDINFO implements Serializable {

    @Reserved
    @SWFType(value = BasicType.UB, count = 2)
    public int reserved;

    public boolean syncStop;

    public boolean syncNoMultiple;

    public boolean hasEnvelope;

    public boolean hasLoops;

    public boolean hasOutPoint;

    public boolean hasInPoint;

    @Conditional("hasInPoint")
    @SWFType(BasicType.UI32)
    public long inPoint;

    @Conditional("hasOutPoint")
    @SWFType(BasicType.UI32)
    public long outPoint;

    @Conditional("hasLoops")
    @SWFType(BasicType.UI16)
    public int loopCount;

    @Conditional("hasEnvelope")
    public SOUNDENVELOPE[] envelopeRecords = new SOUNDENVELOPE[0];

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + this.reserved;
        hash = 17 * hash + (this.syncStop ? 1 : 0);
        hash = 17 * hash + (this.syncNoMultiple ? 1 : 0);
        hash = 17 * hash + (this.hasEnvelope ? 1 : 0);
        hash = 17 * hash + (this.hasLoops ? 1 : 0);
        hash = 17 * hash + (this.hasOutPoint ? 1 : 0);
        hash = 17 * hash + (this.hasInPoint ? 1 : 0);
        hash = 17 * hash + (int) (this.inPoint ^ (this.inPoint >>> 32));
        hash = 17 * hash + (int) (this.outPoint ^ (this.outPoint >>> 32));
        hash = 17 * hash + this.loopCount;
        hash = 17 * hash + Arrays.deepHashCode(this.envelopeRecords);
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
        final SOUNDINFO other = (SOUNDINFO) obj;
        if (this.reserved != other.reserved) {
            return false;
        }
        if (this.syncStop != other.syncStop) {
            return false;
        }
        if (this.syncNoMultiple != other.syncNoMultiple) {
            return false;
        }
        if (this.hasEnvelope != other.hasEnvelope) {
            return false;
        }
        if (this.hasLoops != other.hasLoops) {
            return false;
        }
        if (this.hasOutPoint != other.hasOutPoint) {
            return false;
        }
        if (this.hasInPoint != other.hasInPoint) {
            return false;
        }
        if (this.inPoint != other.inPoint) {
            return false;
        }
        if (this.outPoint != other.outPoint) {
            return false;
        }
        if (this.loopCount != other.loopCount) {
            return false;
        }
        if (!Arrays.deepEquals(this.envelopeRecords, other.envelopeRecords)) {
            return false;
        }
        return true;
    }

}

/*
 *  Copyright (C) 2010-2018 JPEXS, All rights reserved.
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
 * License along with this library. */
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.Reserved;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;

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
}

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

import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.Conditional;
import com.jpexs.decompiler.flash.types.annotations.SWFType;

/**
 * Defines a transform that can be applied to the color space of a graphic
 * object.
 *
 * @author JPEXS
 */
public class CXFORM extends ColorTransform {

    /**
     * Has color addition values
     */
    public boolean hasAddTerms;

    /**
     * Has color multiply values
     */
    public boolean hasMultTerms;

    @Calculated
    @SWFType(value = BasicType.UB, count = 4)
    public int nbits;

    /**
     * Red multiply value
     */
    @Conditional("hasMultTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int redMultTerm;

    /**
     * Green multiply value
     */
    @Conditional("hasMultTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int greenMultTerm;

    /**
     * Blue multiply value
     */
    @Conditional("hasMultTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int blueMultTerm;

    /**
     * Red addition value
     */
    @Conditional("hasAddTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int redAddTerm;

    /**
     * Green addition value
     */
    @Conditional("hasAddTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int greenAddTerm;

    /**
     * Blue addition value
     */
    @Conditional("hasAddTerms")
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int blueAddTerm;

    @Override
    public int getRedAdd() {
        return hasAddTerms ? redAddTerm : super.getRedAdd();
    }

    @Override
    public int getGreenAdd() {
        return hasAddTerms ? greenAddTerm : super.getGreenAdd();
    }

    @Override
    public int getBlueAdd() {
        return hasAddTerms ? blueAddTerm : super.getBlueAdd();
    }

    @Override
    public int getRedMulti() {
        return hasMultTerms ? redMultTerm : super.getRedMulti();
    }

    @Override
    public int getGreenMulti() {
        return hasMultTerms ? greenMultTerm : super.getGreenMulti();
    }

    @Override
    public int getBlueMulti() {
        return hasMultTerms ? blueMultTerm : super.getBlueMulti();
    }

    public CXFORM() {
    }

    public CXFORM(ColorTransform colorTransform) {
        redMultTerm = colorTransform.getRedMulti();
        greenMultTerm = colorTransform.getGreenMulti();
        blueMultTerm = colorTransform.getBlueMulti();
        redAddTerm = colorTransform.getRedAdd();
        greenAddTerm = colorTransform.getGreenAdd();
        blueAddTerm = colorTransform.getBlueAdd();
        hasAddTerms = redAddTerm != 0 || greenAddTerm != 0 || blueAddTerm != 0;
        hasMultTerms = redMultTerm != 255 || greenMultTerm != 255 || blueMultTerm != 255;
    }

    @Override
    public CXFORM clone() {
        CXFORM ret = (CXFORM) super.clone();
        ret.hasAddTerms = hasAddTerms;
        ret.hasMultTerms = hasMultTerms;
        ret.nbits = nbits;
        ret.redMultTerm = redMultTerm;
        ret.greenMultTerm = greenMultTerm;
        ret.blueMultTerm = blueMultTerm;
        ret.redAddTerm = redAddTerm;
        ret.greenAddTerm = greenAddTerm;
        ret.blueAddTerm = blueAddTerm;
        return ret;
    }
}

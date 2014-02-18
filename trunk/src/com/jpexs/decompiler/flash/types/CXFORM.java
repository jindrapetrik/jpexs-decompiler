/*
 *  Copyright (C) 2010-2014 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types;

import com.jpexs.decompiler.flash.types.annotations.Calculated;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.filters.Filtering;
import com.jpexs.helpers.SerializableImage;
import java.io.Serializable;

/**
 * Defines a transform that can be applied to the color space of a graphic
 * object.
 *
 * @author JPEXS
 */
public class CXFORM implements Serializable {

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
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int redMultTerm;
    /**
     * Green multiply value
     */
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int greenMultTerm;
    /**
     * Blue multiply value
     */
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int blueMultTerm;
    /**
     * Red addition value
     */
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int redAddTerm;
    /**
     * Green addition value
     */
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int greenAddTerm;
    /**
     * Blue addition value
     */
    @SWFType(value = BasicType.SB, countField = "nbits")
    public int blueAddTerm;

    public SerializableImage apply(SerializableImage src) {
        return Filtering.colorEffect(src, hasAddTerms ? redAddTerm : 0, hasAddTerms ? greenAddTerm : 0, hasAddTerms ? blueAddTerm : 0, 0, hasMultTerms ? redMultTerm : 255, hasMultTerms ? greenMultTerm : 255, hasMultTerms ? blueMultTerm : 255, 1);
    }
}

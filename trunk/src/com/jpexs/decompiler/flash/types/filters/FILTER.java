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
package com.jpexs.decompiler.flash.types.filters;

import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.helpers.SerializableImage;
import java.io.Serializable;

/**
 * Bitmap filter
 *
 * @author JPEXS
 */
public abstract class FILTER implements Serializable {

    /**
     * Identificator of type of the filter
     */
    @SWFType(BasicType.UI8)
    public int id;

    /**
     * Constructor
     *
     * @param id Type identificator
     */
    public FILTER(int id) {
        this.id = id;
    }

    public abstract SerializableImage apply(SerializableImage src);
    
    public abstract double getDeltaX();

    public abstract double getDeltaY();
}

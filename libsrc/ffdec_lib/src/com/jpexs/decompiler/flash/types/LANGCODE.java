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

import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;

/**
 * Language code.
 *
 * @author JPEXS
 */
public class LANGCODE implements Serializable {

    /**
     * Language code.
     */
    @SWFType(BasicType.UI8)
    public int languageCode;

    /**
     * Constructor.
     */
    public LANGCODE() {
    }

    /**
     * Constructor.
     *
     * @param languageCode Language code
     */
    public LANGCODE(int languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public String toString() {
        return "[LANGCODE:" + languageCode + "]";
    }
}

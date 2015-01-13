/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.helpers.hilight;

import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
public class HighlightData implements Cloneable, Serializable {

    public boolean declaration;

    public String declaredType;

    public String localName;

    public HighlightSpecialType subtype;

    public String specialValue;

    public long index;

    public long offset;

    public boolean isEmpty() {
        return !declaration && declaredType == null && localName == null
                && subtype == null && specialValue == null
                && index == 0 && offset == 0;
    }

    public void merge(HighlightData data) {
        if (data.declaration) {
            declaration = data.declaration;
        }
        if (data.declaredType != null) {
            declaredType = data.declaredType;
        }
        if (data.localName != null) {
            localName = data.localName;
        }
        if (data.subtype != null) {
            subtype = data.subtype;
        }
        if (data.specialValue != null) {
            specialValue = data.specialValue;
        }
        if (data.index != 0) {
            index = data.index;
        }
        if (data.offset != 0) {
            offset = data.offset;
        }
    }

    @Override
    public HighlightData clone() {
        try {
            return (HighlightData) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }
}

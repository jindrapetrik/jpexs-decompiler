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
package com.jpexs.decompiler.flash.helpers.hilight;

import com.jpexs.decompiler.graph.DottedChain;
import java.io.Serializable;

/**
 *
 * @author JPEXS
 */
public class HighlightData implements Cloneable, Serializable {

    public boolean declaration;

    public DottedChain declaredType;

    public String localName;

    public HighlightSpecialType subtype;

    public String specialValue;

    public long index;

    public long offset;

    public long fileOffset = -1;

    public long firstLineOffset = -1;

    public int regIndex = -1;

    public boolean isEmpty() {
        return !declaration && declaredType == null && localName == null
                && subtype == null && specialValue == null
                && index == 0 && offset == 0 && regIndex == -1 && firstLineOffset == -1
                && fileOffset == -1;
    }

    public void merge(HighlightData data) {
        if (data == null) {
            return;
        }
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
        if (data.regIndex != -1) {
            regIndex = data.regIndex;
        }
        if (data.firstLineOffset != -1) {
            firstLineOffset = data.firstLineOffset;
        }
        if (data.fileOffset != -1) {
            fileOffset = data.fileOffset;
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

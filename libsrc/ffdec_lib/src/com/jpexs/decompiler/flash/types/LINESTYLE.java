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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.ConditionalType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

/**
 * Line style.
 *
 * @author JPEXS
 */
public class LINESTYLE implements NeedsCharacters, Serializable, ILINESTYLE {

    /**
     * Width
     */
    @SWFType(BasicType.UI16)
    public int width;

    /**
     * Color
     */
    @ConditionalType(type = RGBA.class, tags = {DefineShape3Tag.ID, DefineShape4Tag.ID})
    public RGB color;

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        return false;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        return false;
    }

    @Override
    public int getNum() {
        return 1;
    }

    @Override
    public RGB getColor() {
        return color;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setColor(RGB color) {
        this.color = color;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Converts to MORPHLINESTYLE
     * @return MORPHLINESTYLE
     */
    public MORPHLINESTYLE toMorphLineStyle() {
        MORPHLINESTYLE morphLineStyle = new MORPHLINESTYLE();
        morphLineStyle.startColor = new RGBA(color);
        morphLineStyle.endColor = new RGBA(color);
        morphLineStyle.startWidth = width;
        morphLineStyle.endWidth = width;
        return morphLineStyle;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.width;
        hash = 53 * hash + Objects.hashCode(this.color);
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
        final LINESTYLE other = (LINESTYLE) obj;
        if (this.width != other.width) {
            return false;
        }
        return Objects.equals(this.color, other.color);
    }
    
    public LINESTYLE2 toLineStyle2() {
        LINESTYLE2 result = new LINESTYLE2();
        result.color = new RGBA(color);
        result.width = width;
        return result;
    }

    public int getMinShapeNum() {
        if (color instanceof RGBA) {
            RGBA acolor = (RGBA) color;
            if (acolor.alpha != 255) {
                return 3;
            }
        }
        return 1;
    }
}

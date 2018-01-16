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

import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.base.NeedsCharacters;
import com.jpexs.decompiler.flash.types.annotations.ConditionalType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class LINESTYLE implements NeedsCharacters, Serializable {

    @SWFType(BasicType.UI16)
    public int width;

    @ConditionalType(type = RGBA.class, tags = {DefineShape3Tag.ID, DefineShape4Tag.ID})
    public RGB color;

    @Override
    public void getNeededCharacters(Set<Integer> needed) {
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        return false;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        return false;
    }
}

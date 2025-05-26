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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.SWF;
import java.util.Set;

/**
 * Interface for classes that need another characters.
 *
 * @author JPEXS
 */
public interface NeedsCharacters {

    /**
     * Get needed characters.
     *
     * @param needed Result
     * @param swf SWF file
     */
    public void getNeededCharacters(Set<Integer> needed, SWF swf);

    /**
     * Replace character.
     *
     * @param oldCharacterId Old character ID
     * @param newCharacterId New character ID
     * @return True if character was replaced
     */
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId);

    /**
     * Remove character.
     *
     * @param characterId Character ID
     * @return True if character was removed
     */
    public boolean removeCharacter(int characterId);
}

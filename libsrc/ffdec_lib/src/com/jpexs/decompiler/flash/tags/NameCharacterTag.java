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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.base.CharacterModifier;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Set;

/**
 * NameCharacter tag - Assign a Library name to a character.
 * Used in Flash Templates.
 *
 * @author JPEXS
 */
@SWFVersion(from = 3)
public class NameCharacterTag extends Tag implements CharacterModifier {

    public static final int ID = 40;

    public static final String NAME = "NameCharacter";

    
    public static final int TYPE_BITMAP = 1;
    public static final int TYPE_SYMBOL = 6;
    public static final int TYPE_SOUND = 0xFFFF;
    
    
    /**
     * ID of character to name
     */
    @SWFType(BasicType.UI16)
    public int characterId = 0;

    /**
     * Name of the character
     */
    public String name = "Symbol";

    @SWFType(BasicType.UI16)    
    public int type = TYPE_SYMBOL;
    
    /**
     * Constructor
     *
     * @param swf SWF
     */
    public NameCharacterTag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    public NameCharacterTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        characterId = sis.readUI16("characterId");
        name = sis.readString("name");
        type = sis.readUI16("type");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(characterId);
        sos.writeString(name);
        sos.writeUI16(type);
    }

    @Override
    public int getCharacterId() {
        return characterId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, Set<String> neededClasses, SWF swf) {
        needed.add(characterId);
    }
}

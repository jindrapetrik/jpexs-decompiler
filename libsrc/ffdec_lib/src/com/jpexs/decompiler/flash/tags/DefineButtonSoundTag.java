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
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * DefineButtonSound tag - defines sound effects for a button.
 *
 * @author JPEXS
 */
@SWFVersion(from = 2)
public class DefineButtonSoundTag extends Tag implements CharacterModifier {

    public static final int ID = 17;

    public static final String NAME = "DefineButtonSound";

    @SWFType(BasicType.UI16)
    public int buttonId;

    @SWFType(BasicType.UI16)
    public int buttonSoundChar0; // OverUpToIdle

    public SOUNDINFO buttonSoundInfo0;

    @SWFType(BasicType.UI16)
    public int buttonSoundChar1; // IdleToOverUp

    public SOUNDINFO buttonSoundInfo1;

    @SWFType(BasicType.UI16)
    public int buttonSoundChar2; // OverUpToOverDown

    public SOUNDINFO buttonSoundInfo2;

    @SWFType(BasicType.UI16)
    public int buttonSoundChar3; // OverDownToOverUp

    public SOUNDINFO buttonSoundInfo3;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public DefineButtonSoundTag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public DefineButtonSoundTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        buttonId = sis.readUI16("buttonId");
        buttonSoundChar0 = sis.readUI16("buttonSoundChar0");
        if (buttonSoundChar0 != 0) {
            buttonSoundInfo0 = sis.readSOUNDINFO("buttonSoundInfo0");
        }
        buttonSoundChar1 = sis.readUI16("buttonSoundChar1");
        if (buttonSoundChar1 != 0) {
            buttonSoundInfo1 = sis.readSOUNDINFO("buttonSoundInfo1");
        }
        buttonSoundChar2 = sis.readUI16("buttonSoundChar2");
        if (buttonSoundChar2 != 0) {
            buttonSoundInfo2 = sis.readSOUNDINFO("buttonSoundInfo2");
        }
        buttonSoundChar3 = sis.readUI16("buttonSoundChar3");
        if (buttonSoundChar3 != 0) {
            buttonSoundInfo3 = sis.readSOUNDINFO("buttonSoundInfo3");
        }
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(buttonId);
        sos.writeUI16(buttonSoundChar0);
        if (buttonSoundChar0 != 0) {
            sos.writeSOUNDINFO(buttonSoundInfo0);
        }
        sos.writeUI16(buttonSoundChar1);
        if (buttonSoundChar1 != 0) {
            sos.writeSOUNDINFO(buttonSoundInfo1);
        }
        sos.writeUI16(buttonSoundChar2);
        if (buttonSoundChar2 != 0) {
            sos.writeSOUNDINFO(buttonSoundInfo2);
        }
        sos.writeUI16(buttonSoundChar3);
        if (buttonSoundChar3 != 0) {
            sos.writeSOUNDINFO(buttonSoundInfo3);
        }
    }

    @Override
    public int getCharacterId() {
        return buttonId;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.buttonId = characterId;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        if (buttonSoundChar0 != 0) {
            needed.add(buttonSoundChar0);
        }
        if (buttonSoundChar1 != 0) {
            needed.add(buttonSoundChar1);
        }
        if (buttonSoundChar2 != 0) {
            needed.add(buttonSoundChar2);
        }
        if (buttonSoundChar3 != 0) {
            needed.add(buttonSoundChar3);
        }
        needed.add(buttonId);
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        ret.put("bid", "" + buttonId);
        return ret;
    }

    @Override
    public boolean removeCharacter(int characterId) {
        boolean modified = false;

        if (buttonSoundChar0 == characterId) {
            buttonSoundChar0 = 0;
            buttonSoundInfo0 = null;
            modified = true;
        }
        if (buttonSoundChar1 == characterId) {
            buttonSoundChar1 = 0;
            buttonSoundInfo1 = null;
            modified = true;
        }
        if (buttonSoundChar2 == characterId) {
            buttonSoundChar2 = 0;
            buttonSoundInfo2 = null;
            modified = true;
        }
        if (buttonSoundChar3 == characterId) {
            buttonSoundChar3 = 0;
            buttonSoundInfo3 = null;
            modified = true;
        }

        if (modified) {
            setModified(true);
        }
        return modified;
    }

    @Override
    public boolean replaceCharacter(int oldCharacterId, int newCharacterId) {
        boolean modified = false;

        if (buttonId == oldCharacterId) {
            buttonId = newCharacterId;
            modified = true;
        }
        if (buttonSoundChar0 == oldCharacterId) {
            buttonSoundChar0 = newCharacterId;
            modified = true;
        }
        if (buttonSoundChar1 == oldCharacterId) {
            buttonSoundChar1 = newCharacterId;
            modified = true;
        }
        if (buttonSoundChar2 == oldCharacterId) {
            buttonSoundChar2 = newCharacterId;
            modified = true;
        }
        if (buttonSoundChar3 == oldCharacterId) {
            buttonSoundChar3 = newCharacterId;
            modified = true;
        }

        if (modified) {
            setModified(true);
        }
        return modified;
    }
}

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
import com.jpexs.decompiler.flash.types.SOUNDINFO;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * StartSound2 tag - starts a sound playing. Extends functionality of StartSound
 * tag.
 *
 * @author JPEXS
 */
@SWFVersion(from = 9)
public class StartSound2Tag extends Tag {

    public static final int ID = 89;

    public static final String NAME = "StartSound2";

    public String soundClassName;

    public SOUNDINFO soundInfo;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public StartSound2Tag(SWF swf) {
        super(swf, ID, NAME, null);
        soundClassName = "NewSoundClass";
        soundInfo = new SOUNDINFO();
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public StartSound2Tag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        soundClassName = sis.readString("soundClassName");
        soundInfo = sis.readSOUNDINFO("soundInfo");
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeString(soundClassName);
        sos.writeSOUNDINFO(soundInfo);
    }

    @Override
    public Map<String, String> getNameProperties() {
        Map<String, String> ret = super.getNameProperties();
        ret.put("cls", "" + soundClassName);
        return ret;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        int characterId = swf.getCharacterId(swf.getCharacterByClass(soundClassName));
        needed.add(characterId);
    }        
}

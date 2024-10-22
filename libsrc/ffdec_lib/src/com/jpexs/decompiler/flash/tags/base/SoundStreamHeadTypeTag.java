/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.timeline.SoundStreamFrameRange;
import com.jpexs.decompiler.flash.types.sound.MP3FRAME;
import com.jpexs.decompiler.flash.types.sound.MP3SOUNDDATA;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.List;

/**
 * Base class for sound stream head tags.
 *
 * @author JPEXS
 */
public abstract class SoundStreamHeadTypeTag extends Tag implements CharacterIdTag, SoundTag {

    /**
     * Constructor.
     * @param swf SWF
     * @param id ID
     * @param name Name
     * @param data Data
     */
    public SoundStreamHeadTypeTag(SWF swf, int id, String name, ByteArrayRange data) {
        super(swf, id, name, data);
    }

    @Override
    public abstract boolean getSoundSize();

    public abstract long getSoundSampleCount();

    public abstract List<SoundStreamFrameRange> getRanges();

    /**
     * Checks if the MP3 sound is greater than 160 kbps.
     * @return True if the MP3 sound is greater than 160 kbps, false otherwise
     */
    protected boolean isMp3HigherThan160Kbps() {
        List<SoundStreamFrameRange> ranges = getRanges();
        if (ranges.isEmpty()) {
            return false;
        }
        try {
            SWFInputStream sis = new SWFInputStream(swf, ranges.get(0).blocks.get(0).streamSoundData.getRangeData());
            MP3SOUNDDATA s = new MP3SOUNDDATA(sis, false);
            if (!s.frames.isEmpty()) {
                MP3FRAME frame = s.frames.get(0);
                int bitRate = frame.getBitRate() / 1000;
                if (bitRate > 160) {
                    return true;
                }
            }
        } catch (IOException ex) {
            //ignore
        }
        return false;
    }
}

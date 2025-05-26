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
import com.jpexs.decompiler.flash.tags.base.CharacterIdTag;
import com.jpexs.decompiler.flash.types.BasicType;
import com.jpexs.decompiler.flash.types.annotations.SWFType;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.io.IOException;
import java.util.Set;

/**
 * Video frame tag - contains video data for a movie frame.
 *
 * @author JPEXS
 */
@SWFVersion(from = 6)
public class VideoFrameTag extends Tag implements CharacterIdTag {

    public static final int ID = 61;

    public static final String NAME = "VideoFrame";

    @SWFType(BasicType.UI16)
    public int streamID;

    @SWFType(BasicType.UI16)
    public int frameNum;

    public ByteArrayRange videoData;

    /**
     * Constructor
     *
     * @param swf SWF
     */
    public VideoFrameTag(SWF swf) {
        super(swf, ID, NAME, null);
        videoData = ByteArrayRange.EMPTY;
    }

    /**
     * Constructor
     *
     * @param sis SWF input stream
     * @param data Data
     * @throws IOException On I/O error
     */
    public VideoFrameTag(SWFInputStream sis, ByteArrayRange data) throws IOException {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) throws IOException {
        streamID = sis.readUI16("streamID");
        frameNum = sis.readUI16("frameNum");
        videoData = sis.readByteRangeEx(sis.available(), "videoData"); //TODO: Parse video packets
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     * @throws IOException On I/O error
     */
    @Override
    public void getData(SWFOutputStream sos) throws IOException {
        sos.writeUI16(streamID);
        sos.writeUI16(frameNum);
        sos.write(videoData);
    }

    @Override
    public int getCharacterId() {
        return streamID;
    }

    @Override
    public void setCharacterId(int characterId) {
        this.streamID = characterId;
    }

    @Override
    public void getNeededCharacters(Set<Integer> needed, SWF swf) {
        needed.add(streamID);
    }
}

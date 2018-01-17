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
package com.jpexs.decompiler.flash.tags;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.types.annotations.SWFVersion;
import com.jpexs.helpers.ByteArrayRange;
import java.util.ArrayList;
import java.util.List;

/**
 * Instructs Flash Player to display the contents of the display list
 *
 * @author JPEXS
 */
@SWFVersion(from = 1)
public class ShowFrameTag extends Tag {

    public static final int ID = 1;

    public static final String NAME = "ShowFrame";

    private static List<Integer> nestedTagTypeIds = new ArrayList<Integer>() {
        {
            add(PlaceObjectTag.ID);
            add(PlaceObject2Tag.ID);
            add(PlaceObject3Tag.ID);
            add(PlaceObject4Tag.ID);
            add(RemoveObjectTag.ID);
            add(RemoveObject2Tag.ID);
            add(FrameLabelTag.ID);
            add(StartSoundTag.ID);
            add(StartSound2Tag.ID);
            add(VideoFrameTag.ID);
            add(SoundStreamBlockTag.ID);
            /*add(SoundStreamHeadTag.ID);
             add(SoundStreamHead2Tag.ID);*/
        }
    };

    /**
     * Constructor
     *
     * @param swf
     */
    public ShowFrameTag(SWF swf) {
        super(swf, ID, NAME, null);
    }

    /**
     * Constructor
     *
     * @param sis
     * @param data
     */
    public ShowFrameTag(SWFInputStream sis, ByteArrayRange data) {
        super(sis.getSwf(), ID, NAME, data);
        readData(sis, data, 0, false, false, false);
    }

    @Override
    public final void readData(SWFInputStream sis, ByteArrayRange data, int level, boolean parallel, boolean skipUnusualTags, boolean lazy) {
    }

    /**
     * Gets data bytes
     *
     * @param sos SWF output stream
     */
    @Override
    public void getData(SWFOutputStream sos) {
    }

    public static boolean isNestedTagType(int tagTypeId) {
        return nestedTagTypeIds.contains(tagTypeId);
    }
}

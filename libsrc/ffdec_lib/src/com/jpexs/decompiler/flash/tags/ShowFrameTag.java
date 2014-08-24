/*
 *  Copyright (C) 2010-2014 JPEXS, All rights reserved.
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
import com.jpexs.helpers.ByteArrayRange;
import java.util.ArrayList;
import java.util.List;

/**
 * Instructs Flash Player to display the contents of the display list
 *
 * @author JPEXS
 */
public class ShowFrameTag extends Tag {

    public static final int ID = 1;

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
     * @param data
     */
    public ShowFrameTag(SWF swf, ByteArrayRange data) {
        super(swf, ID, "ShowFrame", data);
    }

    public ShowFrameTag(SWF swf) {
        super(swf, ID, "ShowFrame", null);
    }

    /**
     * Gets data bytes
     *
     * @return Bytes of data
     */
    @Override
    public byte[] getData() {
        return new byte[0];
    }

    public static boolean isNestedTagType(int tagTypeId) {
        return nestedTagTypeIds.contains(tagTypeId);
    }
}

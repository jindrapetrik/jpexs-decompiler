/*
 *  Copyright (C) 2010-2023 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.timeline;

import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.treeitems.Openable;
import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.sound.SoundExportFormat;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.ByteArrayRange;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SoundStreamFrameRange implements TreeItem, SoundTag {
    public int startFrame;
    public int endFrame;
    public List<SoundStreamBlockTag> blocks = new ArrayList<>();        
    
    private final SoundStreamHeadTypeTag head;
    
    public SoundStreamFrameRange(SoundStreamHeadTypeTag head) {
        this.head = head;
    }       

    @Override
    public Openable getOpenable() {
        return head.getOpenable();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public SoundExportFormat getExportFormat() {
        return head.getExportFormat();
    }

    @Override
    public boolean importSupported() {
        return false; //??
    }

    @Override
    public int getSoundRate() {
        return head.getSoundRate();
    }

    @Override
    public boolean getSoundType() {
        return head.getSoundType();
    }

    @Override
    public List<ByteArrayRange> getRawSoundData() {
        List<ByteArrayRange> ret = new ArrayList<>();
        for (SoundStreamBlockTag block : blocks) {
            ByteArrayRange data = block.streamSoundData;
            if (getSoundFormatId() == SoundFormat.FORMAT_MP3) {
                ret.add(data.getSubRange(4, data.getLength() - 4));
            } else {
                ret.add(data);
            }
        }
        return ret;
    }

    @Override
    public int getSoundFormatId() {
        return head.getSoundFormatId();
    }

    @Override
    public long getTotalSoundSampleCount() {
        return blocks.size() * head.getSoundSampleCount();
    }

    @Override
    public boolean getSoundSize() {
        return head.getSoundSize();
    }

    @Override
    public String getCharacterExportFileName() {
        return head.getCharacterExportFileName() + "_" + (startFrame + 1) + "-" + (endFrame + 1);
    }
    
    @Override
    public String getName() {
        return "SoundStreamBlocks";
    }

    @Override
    public SoundFormat getSoundFormat() {
        return head.getSoundFormat();
    }

    @Override
    public void setSoundSize(boolean soundSize) {
        //?
    }

    @Override
    public void setSoundType(boolean soundType) {
        //?
    }

    @Override
    public void setSoundSampleCount(long soundSampleCount) {
        //?
    }

    @Override
    public void setSoundCompression(int soundCompression) {
        //?
    }

    @Override
    public void setSoundRate(int soundRate) {
        //?
    }

    @Override
    public int getCharacterId() {
        return head.getCharacterId();
    }

    public SoundStreamHeadTypeTag getHead() {
        return head;
    }

    @Override
    public String toString() {
        return "SoundStreamBlocks (frame " + (startFrame + 1) + " - " + (endFrame + 1) + ")";
    }                

    @Override
    public boolean isReadOnly() {
        return head.isReadOnly();
    }        

    @Override
    public String getFlaExportName() {
        return head.getFlaExportName() + "_" + (startFrame + 1) + "-" + (endFrame + 1);
    }        
    
    @Override
    public int getInitialLatency() {
        return 0;
    }
}

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
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.sound.SoundExportFormat;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.ByteArrayRange;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public interface SoundTag extends TreeItem {

    public SoundExportFormat getExportFormat();

    public boolean importSupported();

    public int getSoundRate();

    public boolean getSoundType();

    public List<ByteArrayRange> getRawSoundData();

    public int getSoundFormatId();

    public long getTotalSoundSampleCount();

    public boolean getSoundSize();

    public String getCharacterExportFileName();

    public SoundFormat getSoundFormat();

    public void setSoundSize(boolean soundSize);

    public void setSoundType(boolean soundType);

    public void setSoundSampleCount(long soundSampleCount);

    public void setSoundCompression(int soundCompression);

    public void setSoundRate(int soundRate);

    public int getCharacterId();
    
    public boolean isReadOnly();
    
    public String getName();
    
    public String getFlaExportName();
    
    public int getInitialLatency();
}

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

import com.jpexs.decompiler.flash.treeitems.TreeItem;
import com.jpexs.decompiler.flash.types.sound.SoundExportFormat;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.ByteArrayRange;
import java.util.List;

/**
 * Base class for sound tags.
 *
 * @author JPEXS
 */
public interface SoundTag extends TreeItem {

    /**
     * Gets export format.
     * @return Export format
     */
    public SoundExportFormat getExportFormat();

    /**
     * Checks if sound import is supported.
     * @return True if sound import is supported
     */
    public boolean importSupported();

    /**
     * Gets sound rate.
     *
     * @return Sound rate. 0 = 5.5 kHz, 1 = 11 kHz, 2 = 22 kHz, 3 = 44 kHz
     */
    public int getSoundRate();

    /**
     * Gets sound type.
     * @return Sound type. True = Stereo, False = Mono
     */
    public boolean getSoundType();

    /**
     * Gets raw sound data.
     * @return Raw sound data
     */
    public List<ByteArrayRange> getRawSoundData();

    /**
     * Gets sound format id.
     *
     * See SoundFormat.FORMAT_* constants.
     *
     * @return Sound format id
     */
    public int getSoundFormatId();

    /**
     * Gets total sound sample count.
     * @return Total sound sample count
     */
    public long getTotalSoundSampleCount();

    /**
     * Gets sound size.
     * True = 16-bit, False = 8-bit
     * @return Sound size
     */
    public boolean getSoundSize();

    /**
     * Gets character export file name.
     * @return Character export file name
     */
    public String getCharacterExportFileName();

    /**
     * Gets sound format.
     * See SoundFormat.FORMAT_* constants.
     * @return Sound format
     */
    public SoundFormat getSoundFormat();

    /**
     * Sets sound size.
     * True = 16-bit, False = 8-bit
     * @param soundSize Sound size
     */
    public void setSoundSize(boolean soundSize);

    /**
     * Sets sound type.
     * True = Stereo, False = Mono
     * @param soundType Sound type
     */
    public void setSoundType(boolean soundType);

    /**
     * Sets sound sample count.
     * @param soundSampleCount Sound sample count
     */
    public void setSoundSampleCount(long soundSampleCount);

    /**
     * Sets sound compression.
     * See SoundFormat.FORMAT_* constants.
     * @param soundCompression Sound compression
     */
    public void setSoundCompression(int soundCompression);

    /**
     * Sets sound rate.
     * 0 = 5.5 kHz, 1 = 11 kHz, 2 = 22 kHz, 3 = 44 kHz
     * @param soundRate Sound rate.
     */
    public void setSoundRate(int soundRate);

    /**
     * Gets character id.
     * @return Character id
     */
    public int getCharacterId();

    /**
     * Checks if sound is read only.
     * @return True if sound is read only
     */
    public boolean isReadOnly();

    /**
     * Gets sound name.
     * @return Sound name
     */
    public String getName();

    /**
     * Gets FLA export name.
     * @return FLA export name
     */
    public String getFlaExportName();

    /**
     * Gets initial latency.
     * @return Initial latency
     */
    public int getInitialLatency();
}

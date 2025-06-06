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

import com.jpexs.decompiler.flash.AppResources;

/**
 *
 * @author JPEXS
 */
public class SoundParametersMismatchException extends SoundImportException {
    
    /**
     *
     * @param expectedSoundType
     * @param expectedSoundSize
     * @param expectedSoundRate
     * @param expectedSoundFormat
     */
    public SoundParametersMismatchException(
            boolean expectedSoundType, 
            boolean expectedSoundSize, 
            int expectedSoundRate,
            int expectedSoundFormat,
            boolean actualSoundType, 
            boolean actualSoundSize, 
            int actualSoundRate,
            int actualSoundFormat) {
        super(AppResources.translate("exception.soundFormat.expected").replace("%expected%",  
                (expectedSoundType ? "stereo" : "mono") + " "
                + (expectedSoundSize ? "16bit" : "8bit") + " " 
                + new int[]{5512, 11025, 22050, 44100}[expectedSoundRate] + " Hz" 
                + " " +  new String[]{
                    "uncompressed native endian", 
                    "adpcm", 
                    "mp3",
                    "uncompressed little endian", 
                    "nellymoser 16 kHz",
                    "nellymoser 8 kHz",
                    "nellymoser",
                    "",
                    "",
                    "",
                    "",
                    "speex"
                }[expectedSoundFormat]).replace("%actual%",  
                (actualSoundType ? "stereo" : "mono") + " "
                + (actualSoundSize ? "16bit" : "8bit") + " " 
                + new int[]{5512, 11025, 22050, 44100}[actualSoundRate] + " Hz" 
                + " " +  new String[]{
                    "uncompressed native endian", 
                    "adpcm", 
                    "mp3",
                    "uncompressed little endian", 
                    "nellymoser 16 kHz",
                    "nellymoser 8 kHz",
                    "nellymoser",
                    "",
                    "",
                    "",
                    "",
                    "speex"
                }[actualSoundFormat]));
    }       
}

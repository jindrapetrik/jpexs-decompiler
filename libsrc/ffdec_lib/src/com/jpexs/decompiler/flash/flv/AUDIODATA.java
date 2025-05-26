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
package com.jpexs.decompiler.flash.flv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Audio data.
 *
 * @author JPEXS
 */
public class AUDIODATA extends DATA {

    /**
     * Sound format - uncompressed, native endian
     */
    public static final int SOUNDFORMAT_UNCOMPRESSED_NE = 0;

    /**
     * Sound format - ADPCM
     */
    public static final int SOUNDFORMAT_ADPCM = 1;

    /**
     * Sound format - MP3
     */
    public static final int SOUNDFORMAT_MP3 = 2;

    /**
     * Sound format - uncompressed, little endian
     */
    public static final int SOUNDFORMAT_UNCOMPRESSED_LE = 3;

    /**
     * Sound format - Nellymoser 16 kHz mono
     */
    public static final int SOUNDFORMAT_NELLYMOSER_16 = 4;

    /**
     * Sound format - Nellymoser 8 kHz mono
     */
    public static final int SOUNDFORMAT_NELLYMOSER_8 = 5;

    /**
     * Sound format - Nellymoser
     */
    public static final int SOUNDFORMAT_NELLYMOSER = 6;

    /**
     * Sound format - Speex
     */
    public static final int  SOUNDFORMAT_SPEEX = 11;

    /**
     * Sound rate - 5.5 kHz
     */
    public static final int SOUNDRATE_5K5 = 0;

    /**
     * Sound rate - 11 kHz
     */
    public static final int SOUNDRATE_11K = 1;

    /**
     * Sound rate - 22 kHz
     */
    public static final int SOUNDRATE_22K = 2;

    /**
     * Sound rate - 44 kHz
     */
    public static final int SOUNDRATE_44K = 3;

    /**
     * Sound size - 8 bit
     */
    public static final int SOUNDSIZE_8BIT = 0;

    /**
     * Sound size - 16 bit
     */
    public static final int SOUNDSIZE_16BIT = 1;

    /**
     * Sound type - mono
     */
    public static final int SOUNDTYPE_MONO = 0;

    /**
     * Sound type - stereo
     */
    public static final int SOUNDTYPE_STEREO = 1;

    /**
     * Sound format
     */
    public int soundFormat;

    /**
     * Sound rate
     */
    public int soundRate;

    /**
     * Sound size.
     * True = 16 bit, false = 8 bit
     */
    public boolean soundSize;

    /**
     * Sound type.
     * True = stereo, false = mono
     */
    public boolean soundType;

    /**
     * Sound data
     */
    public byte[] soundData;

    /**
     * Constructor.
     * @param soundFormat Sound format
     * @param soundRate Sound rate
     * @param soundSize Sound size
     * @param soundType Sound type
     * @param soundData Sound data
     */
    public AUDIODATA(int soundFormat, int soundRate, boolean soundSize, boolean soundType, byte[] soundData) {
        this.soundFormat = soundFormat;
        this.soundRate = soundRate;
        this.soundSize = soundSize;
        this.soundType = soundType;
        this.soundData = soundData;
    }

    @Override
    public byte[] getBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            FLVOutputStream flv = new FLVOutputStream(baos);
            flv.writeUB(4, soundFormat);
            flv.writeUB(2, soundRate);
            flv.writeUB(1, soundSize ? 1 : 0);
            flv.writeUB(1, soundType ? 1 : 0);
            flv.write(soundData);
        } catch (IOException ex) {
            // ignore
        }
        return baos.toByteArray();
    }
}

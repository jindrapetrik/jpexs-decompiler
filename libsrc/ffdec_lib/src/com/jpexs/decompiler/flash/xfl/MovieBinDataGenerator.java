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
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.flv.AUDIODATA;
import com.jpexs.decompiler.flash.flv.FLVInputStream;
import com.jpexs.decompiler.flash.flv.FLVTAG;
import com.jpexs.decompiler.flash.flv.SCRIPTDATA;
import com.jpexs.decompiler.flash.flv.SCRIPTDATAVARIABLE;
import com.jpexs.decompiler.flash.flv.VIDEODATA;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.Reference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Generates bin/*.dat file for movies.
 * @author JPEXS
 */
public class MovieBinDataGenerator {

    /**
     * Generates empty bin data.
     * @return Byte array
     */
    public byte[] generateEmptyBinData() {
        return new byte[]{
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xA0, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x78, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x59, (byte) 0x40, (byte) 0x18, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF, (byte) 0xFE, (byte) 0xFF,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
        };
    }

    /**
     * Generates data
     * @param is Input stream
     * @param os Output stream
     * @param fps SWF fps
     * @throws IOException On I/O error
     */
    public void generateBinData(InputStream is, OutputStream os, float fps) throws IOException {
        BinDataOutputStream df = new BinDataOutputStream(os);
        FLVInputStream flvIs = new FLVInputStream(is);
        int width = 0;
        int height = 0;
        Reference<Boolean> audioPresent = new Reference<>(false);
        Reference<Boolean> videoPresent = new Reference<>(false);
        flvIs.readHeader(audioPresent, videoPresent);
        List<FLVTAG> flvTags = flvIs.readTags();
        long lastOffset = 0L;
        long videoFrameCount = 0;

        int soundFormat = 0;
        int samplingRate = 0;
        boolean stereo = false;
        int videoCodec = 0;
        boolean hasAudio = false;
        long maxTimestamp = 0L;
        for (FLVTAG tag : flvTags) {
            if (tag.timeStamp > maxTimestamp) {
                maxTimestamp = tag.timeStamp;
            }
            if (tag.data instanceof VIDEODATA) {
                videoFrameCount++;
                VIDEODATA vd = (VIDEODATA) tag.data;
                videoCodec = vd.codecId;
            }
            if (tag.data instanceof AUDIODATA) {
                AUDIODATA ad = (AUDIODATA) tag.data;
                soundFormat = ad.soundFormat;
                samplingRate = ad.soundRate;
                stereo = ad.soundType;
                hasAudio = true;
            }
        }

        long videoDataIndex = 0;
        for (FLVTAG tag : flvTags) {
            if (tag.data instanceof SCRIPTDATA) {
                SCRIPTDATA sd = (SCRIPTDATA) tag.data;
                if (sd.name.type != 2) {
                    continue;
                }
                if (!"onMetaData".equals(sd.name.value)) {
                    continue;
                }
                if (sd.value.type != 8) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                List<Object> subVals = (List) sd.value.value;
                for (Object o : subVals) {
                    if (o instanceof SCRIPTDATAVARIABLE) {
                        SCRIPTDATAVARIABLE v = (SCRIPTDATAVARIABLE) o;
                        if ("width".equals(v.variableName)) {
                            width = (int) (double) v.variableData.value;
                        }
                        if ("height".equals(v.variableName)) {
                            height = (int) (double) v.variableData.value;
                        }
                    }
                }
                df.write(0x03, videoCodec, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x28, 0x40,
                        0x00, 0x00, 0x00, 0x00);
                if (hasAudio) {
                    df.write(0x80, 0x88, 0xE5, 0x40, 0x10);
                } else {
                    df.write(0x00, 0x00, 0x00, 0x00, 0x00);
                }
                df.write(0x00, 0x00, 0x00);
                if (hasAudio) {
                    if (stereo) {
                        df.write(2);
                    } else {
                        df.write(1);
                    }
                } else {
                    df.write(0);
                }
                df.write(0x00, 0x00, 0x00);

                df.writeUI32(width);
                df.writeUI32(height);
                df.writeDouble(videoFrameCount / fps);

                df.write(
                        0x00, 0x00, 0x00,
                        0x00, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x59, 0x40, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01
                );

            }
            if (tag.data instanceof VIDEODATA) {
                VIDEODATA vd = (VIDEODATA) tag.data;
                long startOffset = lastOffset;
                df.writeUI32(startOffset);
                df.writeUI32(vd.videoData.length);
                lastOffset = startOffset + vd.videoData.length;
                df.write(0x01, 0x00, 0x00, 0x00, 0x04 + (vd.frameType == 1 ? 1 : 0), 0x00, 0x00, 0x00);
                if (videoDataIndex < videoFrameCount - 1) {
                    df.write(0x01);
                } else {
                    df.write(0x00);
                }
                videoDataIndex++;
            }
        }
        df.writeUI32(lastOffset);
        for (FLVTAG tag : flvTags) {
            if (tag.data instanceof VIDEODATA) {
                VIDEODATA vd = (VIDEODATA) tag.data;
                df.write(vd.videoData);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (FLVTAG tag : flvTags) {
            if (tag.data instanceof AUDIODATA) {
                AUDIODATA ad = (AUDIODATA) tag.data;
                baos.write(ad.soundData);
            }
        }

        df.writeUI32(1);
        if (hasAudio) {
            SoundFormat sf = new SoundFormat(soundFormat, samplingRate, stereo);
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            sf.decode(new SWFInputStream(new ByteArrayInputStream(baos.toByteArray())), baos2);
            df.writeUI32(1);
            df.write(0);
            df.writeUI32(baos2.size());
            df.writeUI32(1);
            df.writeUI32(0);
            df.write(0);
            df.writeUI32(baos2.size());
            df.write(baos2.toByteArray());
        } else {
            df.writeUI32(0);
            df.write(0);
        }
        df.write(0x00, 0x00, 0x00, 0x00,
                0xFF, 0xFE, 0xFF, 0x00,
                0x00, 0x00, 0x00, 0x00);

    } 
}

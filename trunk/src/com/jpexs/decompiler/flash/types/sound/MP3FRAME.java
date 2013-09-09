/*
 * Copyright (C) 2013 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.types.sound;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author JPEXS
 */
public class MP3FRAME {

    public int syncWord;
    public int mpegVersion;
    public int layer;
    public boolean protectionBit;
    public int bitRate;
    public int samplingRate;
    public boolean paddingBit;
    public int reserved;
    public int channelMode;
    public int modelExtension;
    public boolean copyright;
    public boolean original;
    public int emphasis;

    public MP3FRAME(InputStream is) throws IOException {
        SWFInputStream sis = new SWFInputStream(is, SWF.DEFAULT_VERSION);
        syncWord = (int) sis.readUB(11);
        mpegVersion = (int) sis.readUB(2);
        layer = (int) sis.readUB(2);
        protectionBit = sis.readUB(1) == 1;
        bitRate = (int) sis.readUB(4);
        samplingRate = (int) sis.readUB(2);
        paddingBit = sis.readUB(1) == 1;
        reserved = (int) sis.readUB(1);
        channelMode = (int) sis.readUB(2);
        modelExtension = (int) sis.readUB(2);
        //TODO:read sample data
    }

    public boolean isMPEG2() {
        return mpegVersion == 2;
    }

    public boolean isMPEG1() {
        return mpegVersion == 3;
    }

    public boolean isLayerI() {
        return layer == 3;
    }

    public boolean isLayerII() {
        return layer == 2;
    }

    public boolean isLayerIII() {
        return layer == 1;
    }

    public int getBitrate() {
        int[] v1_l1_map = {0, 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448, -1};
        int[] v1_l2_map = {0, 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384, -1};
        int[] v1_l3_map = {0, 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, -1};
        int[] v2_l1_map = {0, 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256, -1};
        int[] v2_l2l3_map = {0, 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, -1};

        int[][][] map = {
            {v1_l1_map, v1_l2_map, v1_l3_map},
            {v2_l1_map, v2_l2l3_map, v2_l2l3_map}
        };
        int layer = 0;
        if (isLayerI()) {
            layer = 0;
        }
        if (isLayerII()) {
            layer = 1;
        }
        if (isLayerIII()) {
            layer = 2;
        }
        int mver = 0;
        if (isMPEG1()) {
            mver = 0;
        }
        if (isMPEG2()) {
            mver = 1;
        }
        return map[mver][layer][bitRate];
    }
}

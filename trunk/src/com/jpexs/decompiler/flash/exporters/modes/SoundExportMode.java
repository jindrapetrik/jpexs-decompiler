/*
 * Copyright (C) 2014 JPEXS
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
package com.jpexs.decompiler.flash.exporters.modes;

/**
 *
 * @author JPEXS
 */
public enum SoundExportMode {

    MP3_WAV_FLV(true, true, true), FLV(false, false, true), MP3_WAV(true, true, false), WAV(false, true, false);

    private boolean mp3, wav, flv;

    private SoundExportMode(boolean mp3, boolean wav, boolean flv) {
        this.mp3 = mp3;
        this.wav = wav;
        this.flv = flv;
    }

    public boolean hasMP3() {
        return mp3;
    }

    public boolean hasWav() {
        return wav;
    }

    public boolean hasFlv() {
        return flv;
    }
}

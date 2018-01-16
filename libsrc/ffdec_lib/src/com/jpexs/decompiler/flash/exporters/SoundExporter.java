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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.settings.SoundExportSettings;
import com.jpexs.decompiler.flash.flv.AUDIODATA;
import com.jpexs.decompiler.flash.flv.FLVOutputStream;
import com.jpexs.decompiler.flash.flv.FLVTAG;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.types.sound.SoundExportFormat;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class SoundExporter {

    public List<File> exportSounds(AbortRetryIgnoreHandler handler, String outdir, ReadOnlyTagList tags, final SoundExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        int count = 0;
        for (Tag t : tags) {
            if (t instanceof SoundTag) {
                count++;
            }
        }

        if (count == 0) {
            return ret;
        }

        int currentIndex = 1;
        for (Tag t : tags) {
            if (t instanceof SoundTag) {
                if (evl != null) {
                    evl.handleExportingEvent("sound", currentIndex, count, t.getName());
                }

                final SoundTag st = (SoundTag) t;

                String ext = "wav";
                SoundFormat fmt = st.getSoundFormat();
                switch (fmt.getNativeExportFormat()) {
                    case MP3:
                        if (settings.mode.hasMP3()) {
                            ext = "mp3";
                        }
                        break;
                    case FLV:
                        if (settings.mode.hasFlv()) {
                            ext = "flv";
                        }
                        break;
                }
                if (settings.mode == SoundExportMode.FLV) {
                    ext = "flv";
                }

                final File file = new File(outdir + File.separator + Helper.makeFileName(st.getCharacterExportFileName()) + "." + ext);
                new RetryTask(() -> {
                    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                        exportSound(os, st, settings.mode);
                    }
                }, handler).run();

                ret.add(file);

                if (evl != null) {
                    evl.handleExportedEvent("sound", currentIndex, count, t.getName());
                }

                currentIndex++;
            }
        }
        return ret;
    }

    public byte[] exportSound(SoundTag t, SoundExportMode mode) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exportSound(baos, t, mode);
        return baos.toByteArray();
    }

    public void exportSound(OutputStream fos, SoundTag st, SoundExportMode mode) throws IOException {
        SoundFormat fmt = st.getSoundFormat();
        SoundExportFormat nativeFormat = fmt.getNativeExportFormat();

        if (nativeFormat == SoundExportFormat.MP3 && mode.hasMP3()) {
            List<ByteArrayRange> datas = st.getRawSoundData();
            for (ByteArrayRange data : datas) {
                fos.write(data.getRangeData());
            }
        } else if ((nativeFormat == SoundExportFormat.FLV && mode.hasFlv()) || mode == SoundExportMode.FLV) {
            if (st instanceof DefineSoundTag) {
                FLVOutputStream flv = new FLVOutputStream(fos);
                flv.writeHeader(true, false);
                List<ByteArrayRange> datas = st.getRawSoundData();
                for (ByteArrayRange data : datas) {
                    flv.writeTag(new FLVTAG(0, new AUDIODATA(st.getSoundFormatId(), st.getSoundRate(), st.getSoundSize(), st.getSoundType(), data.getRangeData())));
                }
            } else if (st instanceof SoundStreamHeadTypeTag) {
                SoundStreamHeadTypeTag sh = (SoundStreamHeadTypeTag) st;
                FLVOutputStream flv = new FLVOutputStream(fos);
                flv.writeHeader(true, false);
                List<SoundStreamBlockTag> blocks = sh.getBlocks();

                int ms = (int) (1000.0 / ((Tag) st).getSwf().frameRate);
                for (int b = 0; b < blocks.size(); b++) {
                    byte[] data = blocks.get(b).streamSoundData.getRangeData();
                    if (st.getSoundFormatId() == 2) { //MP3
                        data = Arrays.copyOfRange(data, 4, data.length);
                    }
                    flv.writeTag(new FLVTAG(ms * b, new AUDIODATA(st.getSoundFormatId(), st.getSoundRate(), st.getSoundSize(), st.getSoundType(), data)));
                }
            }
        } else {
            List<ByteArrayRange> soundData = st.getRawSoundData();
            fmt.createWav(soundData, fos);
        }
    }
}

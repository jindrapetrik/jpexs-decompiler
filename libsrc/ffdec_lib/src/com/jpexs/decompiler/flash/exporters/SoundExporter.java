/*
 *  Copyright (C) 2014 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.RunnableIOEx;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
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
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
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

    public List<File> exportSounds(AbortRetryIgnoreHandler handler, String outdir, List<Tag> tags, final SoundExportSettings settings) throws IOException {
        List<File> ret = new ArrayList<>();
        if (tags.isEmpty()) {
            return ret;
        }
        File foutdir = new File(outdir);
        if (!foutdir.exists()) {
            if (!foutdir.mkdirs()) {
                if (!foutdir.exists()) {
                    throw new IOException("Cannot create directory " + outdir);
                }
            }
        }
        for (Tag t : tags) {
            File newfile = null;
            int id = 0;
            if (t instanceof DefineSoundTag) {
                id = ((DefineSoundTag) t).soundId;
            }

            if (t instanceof SoundTag) {
                final SoundTag st = (SoundTag) t;

                String ext = "wav";
                SoundFormat fmt = st.getSoundFormat();
                switch (fmt.getNativeExportFormat()) {
                    case SoundFormat.EXPORT_MP3:
                        if (settings.mode.hasMP3()) {
                            ext = "mp3";
                        }
                        break;
                    case SoundFormat.EXPORT_FLV:
                        if (settings.mode.hasFlv()) {
                            ext = "flv";
                        }
                        break;
                }
                if (settings.mode == SoundExportMode.FLV) {
                    ext = "flv";
                }

                final File file = new File(outdir + File.separator + st.getCharacterExportFileName() + "." + ext);
                newfile = file;
                new RetryTask(new RunnableIOEx() {
                    @Override
                    public void run() throws IOException {
                        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                            exportSound(os, st, settings.mode);
                        }
                    }
                }, handler).run();

                ret.add(newfile);

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
        int nativeFormat = fmt.getNativeExportFormat();

        if (nativeFormat == SoundFormat.EXPORT_MP3 && mode.hasMP3()) {
            List<byte[]> datas = st.getRawSoundData();
            for (byte[] data : datas) {
                fos.write(data);
            }
        } else if ((nativeFormat == SoundFormat.EXPORT_FLV && mode.hasFlv()) || mode == SoundExportMode.FLV) {
            if (st instanceof DefineSoundTag) {
                FLVOutputStream flv = new FLVOutputStream(fos);
                flv.writeHeader(true, false);
                List<byte[]> datas = st.getRawSoundData();
                for (byte[] data : datas) {
                    flv.writeTag(new FLVTAG(0, new AUDIODATA(st.getSoundFormatId(), st.getSoundRate(), st.getSoundSize(), st.getSoundType(), data)));
                }
            } else if (st instanceof SoundStreamHeadTypeTag) {
                SoundStreamHeadTypeTag sh = (SoundStreamHeadTypeTag) st;
                FLVOutputStream flv = new FLVOutputStream(fos);
                flv.writeHeader(true, false);
                List<SoundStreamBlockTag> blocks = sh.getBlocks();

                int ms = (int) (1000.0f / ((float) ((Tag) st).getSwf().frameRate));
                for (int b = 0; b < blocks.size(); b++) {
                    byte[] data = blocks.get(b).streamSoundData.getRangeData();
                    if (st.getSoundFormatId() == 2) { //MP3
                        data = Arrays.copyOfRange(data, 4, data.length);
                    }
                    flv.writeTag(new FLVTAG(ms * b, new AUDIODATA(st.getSoundFormatId(), st.getSoundRate(), st.getSoundSize(), st.getSoundType(), data)));
                }
            }
        } else {
            List<byte[]> soundData = st.getRawSoundData();
            SWF swf = ((Tag) st).getSwf();
            List<SWFInputStream> siss = new ArrayList<>();
            for (byte[] data : soundData) {
                siss.add(new SWFInputStream(swf, data));
            }
            fmt.createWav(siss, fos);
        }
    }

}

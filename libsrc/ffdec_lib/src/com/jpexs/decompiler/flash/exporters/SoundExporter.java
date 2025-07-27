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
package com.jpexs.decompiler.flash.exporters;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.EventListener;
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.RetryTask;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.SoundExportMode;
import com.jpexs.decompiler.flash.exporters.settings.SoundExportSettings;
import com.jpexs.decompiler.flash.flv.AUDIODATA;
import com.jpexs.decompiler.flash.flv.FLVOutputStream;
import com.jpexs.decompiler.flash.flv.FLVTAG;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalSound;
import com.jpexs.decompiler.flash.tags.gfx.DefineExternalStreamSound;
import com.jpexs.decompiler.flash.timeline.SoundStreamFrameRange;
import com.jpexs.decompiler.flash.types.sound.SoundExportFormat;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import com.jpexs.helpers.Path;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Sound exporter.
 *
 * @author JPEXS
 */
public class SoundExporter {

    public List<File> exportSounds(AbortRetryIgnoreHandler handler, String outdir, ReadOnlyTagList tags, final SoundExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<SoundTag> sounds = new ArrayList<>();
        for (Tag t : tags) {
            if (t instanceof SoundTag) {
                sounds.add((SoundTag) t);
            }
        }
        return exportSounds(handler, outdir, sounds, settings, evl);
    }

    public List<File> exportSounds(AbortRetryIgnoreHandler handler, String outdir, List<SoundTag> tags, final SoundExportSettings settings, EventListener evl) throws IOException, InterruptedException {
        List<File> ret = new ArrayList<>();
        if (CancellableWorker.isInterrupted()) {
            return ret;
        }

        if (tags.isEmpty()) {
            return ret;
        }

        File foutdir = new File(outdir);
        Path.createDirectorySafe(foutdir);

        if (tags.isEmpty()) {
            return ret;
        }

        int currentIndex = 1;
        for (SoundTag st : tags) {
            if (evl != null) {
                evl.handleExportingEvent("sound", currentIndex, tags.size(), st.getName());
            }

            String ext = ".wav";
            SoundFormat fmt = st.getSoundFormat();
            switch (fmt.getNativeExportFormat()) {
                case MP3:
                    if (settings.mode.hasMP3()) {
                        ext = ".mp3";
                    }
                    break;
                case FLV:
                    if (settings.mode.hasFlv()) {
                        ext = ".flv";
                    }
                    break;
            }
            if (settings.mode == SoundExportMode.FLV) {
                ext = ".flv";
            }

            final File file = new File(outdir + File.separator + Helper.makeFileName(st.getCharacterExportFileName()) + ext);
            new RetryTask(() -> {
                try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                    exportSound(os, st, settings.mode, settings.resampleWav);
                }
            }, handler).run();

            Set<String> classNames = (st instanceof CharacterTag) ? ((CharacterTag) st).getClassNames() : new HashSet<>();
            if (Configuration.as3ExportNamesUseClassNamesOnly.get() && !classNames.isEmpty()) {
                for (String className : classNames) {
                    if (Configuration.autoDeobfuscateIdentifiers.get()) {
                        className = DottedChain.parseNoSuffix(className).toPrintableString(new LinkedHashSet<>(), st.getSwf(), true);
                    }
                    File classFile = new File(outdir + File.separator + Helper.makeFileName(className + ext));
                    new RetryTask(() -> {
                        Files.copy(file.toPath(), classFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }, handler).run();
                    ret.add(classFile);
                }
                file.delete();
            } else {
                ret.add(file);
            }

            if (CancellableWorker.isInterrupted()) {
                break;
            }

            if (evl != null) {
                evl.handleExportedEvent("sound", currentIndex, tags.size(), st.getName());
            }

            currentIndex++;
        }
        return ret;
    }

    public byte[] exportSound(SoundTag t, SoundExportMode mode, boolean resampleWav) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exportSound(baos, t, mode, resampleWav);
        return baos.toByteArray();
    }

    public void exportSound(OutputStream fos, SoundTag st, SoundExportMode mode, boolean resampleWav) throws IOException {
        SoundFormat fmt = st.getSoundFormat();
        SoundExportFormat nativeFormat = fmt.getNativeExportFormat();

        if (nativeFormat == SoundExportFormat.MP3 && mode.hasMP3()) {
            List<ByteArrayRange> datas = st.getRawSoundData();
            for (ByteArrayRange data : datas) {
                fos.write(data.getRangeData());
            }
        } else if ((nativeFormat == SoundExportFormat.FLV && mode.hasFlv()) || mode == SoundExportMode.FLV) {
            if ((st instanceof DefineSoundTag) || (st instanceof DefineExternalSound) || (st instanceof DefineExternalStreamSound)) {
                FLVOutputStream flv = new FLVOutputStream(fos);
                flv.writeHeader(true, false);
                List<ByteArrayRange> datas = st.getRawSoundData();
                for (ByteArrayRange data : datas) {
                    flv.writeTag(new FLVTAG(0, new AUDIODATA(st.getSoundFormatId(), st.getSoundRate(), st.getSoundSize(), st.getSoundType(), data.getRangeData())));
                }
            } else if ((st instanceof SoundStreamFrameRange) || (st instanceof SoundStreamHeadTypeTag)) {
                List<SoundStreamBlockTag> blocks;
                if (st instanceof SoundStreamHeadTypeTag) {
                    blocks = new ArrayList<>();
                    SoundStreamHeadTypeTag head = (SoundStreamHeadTypeTag) st;
                    for (SoundStreamFrameRange range : head.getRanges()) {
                        blocks.addAll(range.blocks);
                    }
                } else {
                    blocks = ((SoundStreamFrameRange) st).blocks;
                }

                SoundStreamFrameRange sh = (SoundStreamFrameRange) st;
                FLVOutputStream flv = new FLVOutputStream(fos);
                flv.writeHeader(true, false);

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
            fmt.createWav(null, soundData, fos, st.getInitialLatency(), resampleWav);
        }
    }
}

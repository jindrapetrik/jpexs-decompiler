/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
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
package com.jpexs.decompiler.flash.importers;

import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SoundStreamBlockTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.CharacterTag;
import com.jpexs.decompiler.flash.tags.base.SoundImportException;
import com.jpexs.decompiler.flash.tags.base.SoundStreamHeadTypeTag;
import com.jpexs.decompiler.flash.tags.base.SoundTag;
import com.jpexs.decompiler.flash.tags.base.UnsupportedSamplingRateException;
import com.jpexs.decompiler.flash.timeline.SoundStreamFrameRange;
import com.jpexs.decompiler.flash.timeline.Timelined;
import com.jpexs.decompiler.flash.types.sound.MP3FRAME;
import com.jpexs.decompiler.flash.types.sound.MP3SOUNDDATA;
import com.jpexs.decompiler.flash.types.sound.SoundFormat;
import com.jpexs.helpers.ByteArrayRange;
import com.jpexs.helpers.Helper;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Sound importer.
 *
 * @author JPEXS
 */
public class SoundImporter {

    /**
     * Imports sound from input stream.
     * @param soundTag Sound tag
     * @param is Input stream
     * @param newSoundFormat New sound format
     * @return True if sound was imported successfully
     * @throws SoundImportException On sound import error
     */
    public boolean importDefineSound(DefineSoundTag soundTag, InputStream is, int newSoundFormat) throws SoundImportException {
        int newSoundRate = -1;
        boolean newSoundSize = false;
        boolean newSoundType = false;
        long newSoundSampleCount = -1;
        byte[] newSoundData;
        switch (newSoundFormat) {
            case SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN:
                try (AudioInputStream audioIs = AudioSystem.getAudioInputStream(new BufferedInputStream(is))) {
                    AudioFormat fmt = audioIs.getFormat();
                    newSoundType = fmt.getChannels() == 2;
                    newSoundSize = fmt.getSampleSizeInBits() == 16;
                    newSoundSampleCount = audioIs.getFrameLength();
                    newSoundData = Helper.readStream(audioIs);
                    newSoundRate = (int) Math.round(fmt.getSampleRate());
                    switch (newSoundRate) {
                        case 5512:
                            newSoundRate = 0;
                            break;
                        case 11025:
                            newSoundRate = 1;
                            break;
                        case 22050:
                            newSoundRate = 2;
                            break;
                        case 44100:
                            newSoundRate = 3;
                            break;
                        default:
                            throw new UnsupportedSamplingRateException(newSoundRate, new int[]{5512, 11025, 22050, 44100});
                    }
                } catch (UnsupportedAudioFileException | IOException ex) {
                    return false;
                }
                break;
            case SoundFormat.FORMAT_MP3:
                BufferedInputStream bis = new BufferedInputStream(is);
                loadID3v2(bis);
                byte[] mp3data = Helper.readStream(bis);

                final int ID3_V1_LENTGH = 128;
                final int ID3_V1_EXT_LENGTH = 227;

                if (mp3data.length > ID3_V1_LENTGH) {
                    //ID3v1
                    if (mp3data[mp3data.length - ID3_V1_LENTGH] == 'T' && mp3data[mp3data.length - ID3_V1_LENTGH + 1] == 'A' && mp3data[mp3data.length - ID3_V1_LENTGH + 2] == 'G') {
                        mp3data = Arrays.copyOf(mp3data, mp3data.length - ID3_V1_LENTGH);
                        if (mp3data.length > ID3_V1_EXT_LENGTH) {
                            //ID3v1 extended
                            if (mp3data[mp3data.length - ID3_V1_EXT_LENGTH] == 'T' && mp3data[mp3data.length - ID3_V1_EXT_LENGTH + 1] == 'A' && mp3data[mp3data.length - ID3_V1_EXT_LENGTH + 2] == 'G' && mp3data[mp3data.length - ID3_V1_EXT_LENGTH + 3] == '+') {
                                mp3data = Arrays.copyOf(mp3data, mp3data.length - ID3_V1_EXT_LENGTH);
                            }
                        }
                    }
                }
                try {
                    MP3SOUNDDATA snd = new MP3SOUNDDATA(new SWFInputStream(soundTag.getSwf(), mp3data), true);
                    if (!snd.frames.isEmpty()) {
                        MP3FRAME fr = snd.frames.get(0);
                        newSoundRate = fr.getSamplingRate();
                        switch (newSoundRate) {
                            case 11025:
                                newSoundRate = 1;
                                break;
                            case 22050:
                                newSoundRate = 2;
                                break;
                            case 44100:
                                newSoundRate = 3;
                                break;
                            default:
                                throw new UnsupportedSamplingRateException(newSoundRate, new int[]{11025, 22050, 44100});
                        }

                        newSoundSize = true;
                        newSoundType = fr.isStereo();
                        int len = snd.sampleCount();
                        if (fr.isStereo()) {
                            len = len / 2;
                        }

                        newSoundSampleCount = len;
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION, null);
                    sos.writeSI16(0); //Latency - how to calculate it?
                    sos.write(mp3data);
                    newSoundData = baos.toByteArray();
                } catch (IOException ex) {
                    return false;
                }
                break;
            default:
                return false;
        }
        if (newSoundData != null) {
            soundTag.setSoundSize(newSoundSize);
            soundTag.setSoundRate(newSoundRate);
            soundTag.setSoundSampleCount(newSoundSampleCount);
            soundTag.soundData = new ByteArrayRange(newSoundData);
            soundTag.setSoundType(newSoundType);
            soundTag.setSoundCompression(newSoundFormat);
            soundTag.setModified(true);
            return true;
        }
        return false;
    }

    private void loadID3v2(InputStream in) {
        int size = -1;
        try {
            // Read ID3v2 header (10 bytes).
            in.mark(10);
            size = readID3v2Header(in);
        } catch (IOException e) {
            //ignored
        } finally {
            try {
                // Unread ID3v2 header (10 bytes).
                in.reset();
            } catch (IOException e) {
                //ignored
            }
        }
        // Load ID3v2 tags.
        try {
            if (size > 0) {
                byte[] rawid3v2 = new byte[size];
                in.read(rawid3v2, 0, rawid3v2.length);
            }
        } catch (IOException e) {
            //ignored
        }
    }

    /**
     * Parse ID3v2 tag header to find out size of ID3v2 frames.
     *
     * @param in MP3 InputStream
     * @return size of ID3v2 frames + header
     * @throws IOException On I/O error
     * @author JavaZOOM
     */
    private int readID3v2Header(InputStream in) throws IOException {
        byte[] id3header = new byte[4];
        int size = -10;
        in.read(id3header, 0, 3);
        // Look for ID3v2
        if ((id3header[0] == 'I') && (id3header[1] == 'D') && (id3header[2] == '3')) {
            in.read(id3header, 0, 3);
            int majorVersion = id3header[0];
            int revision = id3header[1];
            in.read(id3header, 0, 4);
            size = (int) (id3header[0] << 21) + (id3header[1] << 14) + (id3header[2] << 7) + (id3header[3]);
        }
        return (size + 10);
    }

    /**
     * Imports sound stream from input stream.
     * @param streamHead Sound stream head
     * @param is Input stream
     * @param newSoundFormat New sound format
     * @return True if sound stream was imported successfully
     * @throws UnsupportedSamplingRateException On unsupported sampling rate
     */
    public boolean importSoundStream(SoundStreamHeadTypeTag streamHead, InputStream is, int newSoundFormat) throws UnsupportedSamplingRateException {
        List<MP3FRAME> mp3Frames = null;
        int newSoundRate = -1;
        boolean newSoundSize = false;
        boolean newSoundType = false;
        long newSoundSampleCount = -1;
        byte[] uncompressedSoundData = null;
        int bytesPerSwfFrame = -1;
        SWF swf = streamHead.getSwf();
        int sampleLen = 0;
        int soundRateHz = 0;
        switch (newSoundFormat) {
            case SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN:
                try (AudioInputStream audioIs = AudioSystem.getAudioInputStream(new BufferedInputStream(is))) {
                    AudioFormat fmt = audioIs.getFormat();
                    newSoundType = fmt.getChannels() == 2;
                    newSoundSize = fmt.getSampleSizeInBits() == 16;
                    //newSoundSampleCount = audioIs.getFrameLength();
                    uncompressedSoundData = Helper.readStream(audioIs);
                    sampleLen = (newSoundType ? 2 : 1) * (newSoundSize ? 2 : 1);
                    soundRateHz = (int) Math.round(fmt.getSampleRate());
                    newSoundSampleCount = (int) Math.ceil(soundRateHz / swf.frameRate);

                    bytesPerSwfFrame = (int) Math.ceil(soundRateHz / swf.frameRate) * sampleLen;
                    switch (soundRateHz) {
                        case 5512:
                            newSoundRate = 0;
                            break;
                        case 11025:
                            newSoundRate = 1;
                            break;
                        case 22050:
                            newSoundRate = 2;
                            break;
                        case 44100:
                            newSoundRate = 3;
                            break;
                        default:
                            throw new UnsupportedSamplingRateException(newSoundRate, new int[]{5512, 11025, 22050, 44100});
                    }

                } catch (UnsupportedAudioFileException | IOException ex) {
                    return false;
                }
                break;
            case SoundFormat.FORMAT_MP3:
                BufferedInputStream bis = new BufferedInputStream(is);
                loadID3v2(bis);
                byte[] mp3data = Helper.readStream(bis);

                final int ID3_V1_LENTGH = 128;
                final int ID3_V1_EXT_LENGTH = 227;

                if (mp3data.length > ID3_V1_LENTGH) {
                    //ID3v1
                    if (mp3data[mp3data.length - ID3_V1_LENTGH] == 'T' && mp3data[mp3data.length - ID3_V1_LENTGH + 1] == 'A' && mp3data[mp3data.length - ID3_V1_LENTGH + 2] == 'G') {
                        mp3data = Arrays.copyOf(mp3data, mp3data.length - ID3_V1_LENTGH);
                        if (mp3data.length > ID3_V1_EXT_LENGTH) {
                            //ID3v1 extended
                            if (mp3data[mp3data.length - ID3_V1_EXT_LENGTH] == 'T' && mp3data[mp3data.length - ID3_V1_EXT_LENGTH + 1] == 'A' && mp3data[mp3data.length - ID3_V1_EXT_LENGTH + 2] == 'G' && mp3data[mp3data.length - ID3_V1_EXT_LENGTH + 3] == '+') {
                                mp3data = Arrays.copyOf(mp3data, mp3data.length - ID3_V1_EXT_LENGTH);
                            }
                        }
                    }
                }
                try {
                    MP3SOUNDDATA snd = new MP3SOUNDDATA(new SWFInputStream(swf, mp3data), true);
                    if (!snd.frames.isEmpty()) {
                        MP3FRAME fr = snd.frames.get(0);
                        soundRateHz = fr.getSamplingRate();
                        switch (soundRateHz) {
                            case 11025:
                                newSoundRate = 1;
                                break;
                            case 22050:
                                newSoundRate = 2;
                                break;
                            case 44100:
                                newSoundRate = 3;
                                break;
                            default:
                                throw new UnsupportedSamplingRateException(newSoundRate, new int[]{11025, 22050, 44100});
                        }

                        newSoundSize = true;
                        newSoundType = fr.isStereo();
                        /*int len = snd.sampleCount();
                        if (fr.isStereo()) {
                            len = len / 2;
                        }*/

                        newSoundSampleCount = (int) Math.ceil(soundRateHz / swf.frameRate);
                        //newSoundSampleCount = len;
                    }

                    mp3Frames = snd.frames;
                } catch (IOException ex) {
                    return false;
                }
                break;
            default:
                return false;
        }

        ByteArrayInputStream bais = uncompressedSoundData == null ? null : new ByteArrayInputStream(uncompressedSoundData);

        List<SoundStreamFrameRange> ranges = streamHead.getRanges();

        List<SoundStreamBlockTag> existingBlocks = new ArrayList<>();
        for (SoundStreamFrameRange range : ranges) {
            existingBlocks.addAll(range.blocks);
        }

        int startFrame = 0;
        Timelined timelined = streamHead.getTimelined();
        if (!existingBlocks.isEmpty()) {
            ReadOnlyTagList tags = timelined.getTags();
            for (Tag t : tags) {
                if (t instanceof ShowFrameTag) {
                    startFrame++;
                }
                if (t instanceof SoundStreamBlockTag) {
                    break;
                }
            }
        }
        for (SoundStreamBlockTag block : existingBlocks) {
            timelined.removeTag(block);
        }

        List<SoundStreamBlockTag> blocks = new ArrayList<>();
        if (bais != null) { //Uncompressed
            DataInputStream dais = new DataInputStream(bais);
            long pos = 0;
            int frame = 0;
            long lastNumSamplesLong = 0;
            try {
                while (dais.available() > 0) {

                    float timeAfterFrame = (frame + 1) / swf.frameRate;
                    float numSamplesAfterFrame = (frame + 1) * soundRateHz / swf.frameRate;

                    long numSamplesAfterFrameLong = (long) Math.ceil(numSamplesAfterFrame);

                    long deltaNumSamples = numSamplesAfterFrameLong - lastNumSamplesLong;

                    lastNumSamplesLong = numSamplesAfterFrameLong;

                    if (deltaNumSamples > 0) {
                        byte[] buf = new byte[(int) deltaNumSamples * sampleLen];
                        dais.readFully(buf);
                        SoundStreamBlockTag block = new SoundStreamBlockTag(swf);
                        block.streamSoundData = new ByteArrayRange(buf);
                        blocks.add(block);
                    } else {
                        SoundStreamBlockTag block = new SoundStreamBlockTag(swf);
                        block.streamSoundData = new ByteArrayRange(new byte[0]);
                        blocks.add(block);
                    }
                    frame++;
                }
            } catch (IOException ex) {
                //ignore
            }
        }
        if (mp3Frames != null) {

            int frame = 0;

            int mp3FrameNum = 0;
            long lastNumSamplesLong = 0;
            while (mp3FrameNum < mp3Frames.size()) {
                float timeAfterFrame = (frame + 1) / swf.frameRate;
                float numSamplesAfterFrame = (frame + 1) * soundRateHz / swf.frameRate;
                long numSamplesBeforeFrameLong = Math.round(frame * soundRateHz / swf.frameRate);

                int seekSamples = (int) (lastNumSamplesLong - numSamplesBeforeFrameLong);

                SoundStreamBlockTag block = new SoundStreamBlockTag(swf);

                List<MP3FRAME> blockMp3Frames = new ArrayList<>();
                int blockSamples = 0;
                while (lastNumSamplesLong < numSamplesAfterFrame && mp3FrameNum < mp3Frames.size()) {
                    MP3FRAME mp3Frame = mp3Frames.get(mp3FrameNum);
                    lastNumSamplesLong += mp3Frame.getSampleCount();
                    blockSamples += mp3Frame.getSampleCount();
                    blockMp3Frames.add(mp3Frame);
                    mp3FrameNum++;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                SWFOutputStream sos = new SWFOutputStream(baos, SWF.DEFAULT_VERSION, null);
                try {
                    sos.writeUI16(blockSamples);
                    sos.writeSI16(seekSamples);
                    for (MP3FRAME mp3Frame : blockMp3Frames) {
                        sos.write(mp3Frame.getBytes());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SoundStreamHeadTypeTag.class.getName()).log(Level.SEVERE, null, ex);
                }
                block.streamSoundData = new ByteArrayRange(baos.toByteArray());
                blocks.add(block);
                frame++;
            }
        }

        ReadOnlyTagList tags = timelined.getTags();
        int frame = -1;
        for (int i = 0; i < tags.size(); i++) {
            Tag t = tags.get(i);
            if (t instanceof ShowFrameTag) {
                frame++;
                if (frame >= startFrame && !blocks.isEmpty()) {
                    SoundStreamBlockTag block = blocks.remove(0);
                    block.setTimelined(timelined);
                    timelined.addTag(i, block);
                    tags = timelined.getTags();
                    i++;
                }
            }
        }

        int framesBefore = timelined.getFrameCount();
        //enlarge timeline when necessary
        while (!blocks.isEmpty()) {
            SoundStreamBlockTag block = blocks.remove(0);
            block.setTimelined(timelined);
            timelined.addTag(block);
            ShowFrameTag sft = new ShowFrameTag(swf);
            sft.setTimelined(timelined);
            timelined.addTag(sft);
            framesBefore++;
        }
        timelined.setFrameCount(framesBefore);
        streamHead.setSoundCompression(newSoundFormat);
        streamHead.setSoundSampleCount((int) newSoundSampleCount);
        streamHead.setSoundSize(newSoundSize);
        streamHead.setSoundType(newSoundType);
        streamHead.setSoundRate(newSoundRate);

        streamHead.setModified(true);
        timelined.resetTimeline();
        swf.resetTimeline(); //to reload blocks
        return true;
    }

    /**
     * Imports sound from input stream.
     * @param soundTag Sound tag
     * @param is Input stream
     * @param newSoundFormat New sound format
     * @return True if sound was imported successfully
     * @throws SoundImportException On sound import error
     */
    public boolean importSound(SoundTag soundTag, InputStream is, int newSoundFormat) throws SoundImportException {
        if (soundTag instanceof DefineSoundTag) {
            return importDefineSound((DefineSoundTag) soundTag, is, newSoundFormat);
        }
        if (soundTag instanceof SoundStreamHeadTypeTag) {
            return importSoundStream((SoundStreamHeadTypeTag) soundTag, is, newSoundFormat);
        }
        return false;
    }

    /**
     * Bulk imports sounds from directory.
     * @param soundDir Sound directory
     * @param swf SWF
     * @param printOut Print out
     * @return Number of imported sounds
     */
    public int bulkImport(File soundDir, SWF swf, boolean printOut) {

        Map<Integer, CharacterTag> characters = swf.getCharacters(false);
        int soundCount = 0;
        List<String> extensions = Arrays.asList("mp3", "wav");
        File[] allFiles = soundDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String nameLower = name.toLowerCase();
                for (String ext : extensions) {
                    if (nameLower.endsWith("." + ext)) {
                        return true;
                    }
                }
                return false;
            }
        });

        List<SoundTag> soundTags = new ArrayList<>();
        for (int characterId : characters.keySet()) {
            CharacterTag tag = characters.get(characterId);
            if (tag instanceof DefineSoundTag) {
                soundTags.add((DefineSoundTag) tag);
            }
            if (tag instanceof DefineSpriteTag) {
                DefineSpriteTag sprite = (DefineSpriteTag) tag;
                for (Tag subTag : sprite.getTags()) {
                    if (subTag instanceof SoundStreamHeadTypeTag) {
                        soundTags.add((SoundStreamHeadTypeTag) subTag);
                    }
                }
            }
        }
        for (Tag tag : swf.getTags()) {
            if (tag instanceof SoundStreamHeadTypeTag) {
                soundTags.add((SoundStreamHeadTypeTag) tag);
            }
        }

        for (SoundTag tag : soundTags) {
            int characterId = tag.getCharacterId();
            List<File> existingFilesForSoundTag = new ArrayList<>();

            List<String> classNameExpectedFileNames = new ArrayList<>();
            if (tag instanceof CharacterTag) {
                for (String className : ((CharacterTag) tag).getClassNames()) {
                    classNameExpectedFileNames.add(Helper.makeFileName(className));
                }
            }

            for (File f : allFiles) {
                if (f.getName().startsWith("" + characterId + ".") || f.getName().startsWith("" + characterId + "_")) {
                    existingFilesForSoundTag.add(f);
                } else {
                    String nameNoExt = f.getName();
                    if (nameNoExt.contains(".")) {
                        nameNoExt = nameNoExt.substring(0, nameNoExt.lastIndexOf("."));
                    }
                    if (classNameExpectedFileNames.contains(nameNoExt)) {
                        existingFilesForSoundTag.add(f);
                    }
                }
            }
            existingFilesForSoundTag.sort(new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    String ext1 = o1.getName().substring(o1.getName().lastIndexOf(".") + 1);
                    String ext2 = o2.getName().substring(o2.getName().lastIndexOf(".") + 1);
                    int ret = extensions.indexOf(ext1) - extensions.indexOf(ext2);
                    if (ret == 0) {
                        return o1.getName().compareTo(o2.getName());
                    }
                    return ret;
                }
            });

            if (existingFilesForSoundTag.isEmpty()) {
                continue;
            }

            if (existingFilesForSoundTag.size() > 1) {
                Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, "Multiple matching files for sound tag {0} exists, {1} selected", new Object[]{characterId, existingFilesForSoundTag.get(0).getName()});
            }
            File sourceFile = existingFilesForSoundTag.get(0);

            try {
                if (printOut) {
                    System.out.println("Importing character " + characterId + " from file " + sourceFile.getName());
                }
                int soundFormat = SoundFormat.FORMAT_UNCOMPRESSED_LITTLE_ENDIAN;
                if (sourceFile.getAbsolutePath().toLowerCase(Locale.ENGLISH).endsWith(".mp3")) {
                    soundFormat = SoundFormat.FORMAT_MP3;
                }
                try (FileInputStream fis = new FileInputStream(sourceFile)) {
                    importSound(tag, fis, soundFormat);
                    soundCount++;
                }
            } catch (IOException | SoundImportException ex) {
                Logger.getLogger(ShapeImporter.class.getName()).log(Level.WARNING, "Cannot import sound " + characterId + " from file " + sourceFile.getName(), ex);
            }
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
        return soundCount;
    }
}

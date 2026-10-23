/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
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

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineSoundTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.testng.Assert.*;

public class XflSoundExportTest {

    @Test
    public void pcmCacheUsesDecodedSampleCountAndBitDepth() throws Exception {
        SWF swf = new SWF();
        swf.frameRate = 24;
        Map<String, byte[]> expected = new LinkedHashMap<>();
        int id = 1;
        for (int codec : new int[]{0, 3}) {
            for (boolean stereo : new boolean[]{false, true}) {
                for (boolean pcm16 : new boolean[]{false, true}) {
                    DefineSoundTag sound = new DefineSoundTag(swf);
                    sound.soundId = id++;
                    sound.soundFormat = codec;
                    sound.soundRate = 1;
                    sound.soundSize = pcm16;
                    sound.soundType = stereo;
                    sound.soundSampleCount = 1; // Deliberately differs from the complete buffer.
                    byte[] data = new byte[5 * (stereo ? 2 : 1) * (pcm16 ? 2 : 1)];
                    for (int i = 0; i < data.length; i++) {
                        data[i] = (byte) (128 + i);
                    }
                    sound.soundData = new ByteArrayRange(data);
                    swf.addTag(sound);
                    expected.put(sound.getFlaExportName() + ".wav", sound.getSoundFormat().decode(null, sound.getRawSoundData(), 0));
                }
            }
        }
        swf.addTag(new ShowFrameTag(swf));
        assertSoundCaches(swf, expected);
        for (com.jpexs.decompiler.flash.tags.Tag tag : swf.getTags()) {
            if (tag instanceof DefineSoundTag) {
                assertEquals(((DefineSoundTag) tag).soundSampleCount, 1L, "Export must not change the SWF");
            }
        }
    }

    @Test
    public void compressedSoundCountsMatchTheEntirePcmCache() throws Exception {
        try (FileInputStream input = new FileInputStream("testdata/sounds/sounds.swf")) {
            assertSoundCaches(new SWF(input, false), null);
        }
    }

    private static void assertSoundCaches(SWF swf, Map<String, byte[]> expected) throws Exception {
        Path output = Files.createTempFile("ffdec-xfl-sounds-", ".fla");
        try {
            swf.exportFla(new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable thrown) {
                    throw new AssertionError("XFL export failed", thrown);
                }

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }
            }, output.toString(), "sounds.swf", "Test", "Test", "1", false, FLAVersion.CS6, null);
            try (ZipFile archive = new ZipFile(output.toFile()); InputStream xml = archive.getInputStream(archive.getEntry("DOMDocument.xml"))) {
                NodeList items = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml).getElementsByTagName("DOMSoundItem");
                assertTrue(items.getLength() > 0, "Exercise real exported sound items");
                if (expected != null) {
                    assertEquals(items.getLength(), expected.size());
                }
                for (int i = 0; i < items.getLength(); i++) {
                    Element item = (Element) items.item(i);
                    String name = item.getAttribute("name");
                    byte[] cache;
                    try (InputStream in = archive.getInputStream(archive.getEntry("bin/" + item.getAttribute("soundDataHRef")))) {
                        cache = read(in);
                    }
                    String format = item.getAttribute("format");
                    assertTrue(format.contains("8bit") || format.contains("16bit"), name);
                    int sampleBytes = format.contains("16bit") ? 2 : 1;
                    int frameBytes = sampleBytes * (format.contains("Stereo") ? 2 : 1);
                    assertEquals(cache.length % frameBytes, 0, name);
                    assertEquals(Long.parseLong(item.getAttribute("sampleCount")) * frameBytes, (long) cache.length, name);
                    if (expected != null) {
                        assertEquals(cache, expected.get(name), name + ": retain all decoded samples");
                        assertEquals(Long.parseLong(item.getAttribute("sampleCount")), 5L, name);
                    }
                    if (item.getAttribute("href").endsWith(".wav")) {
                        byte[] wave;
                        try (InputStream in = archive.getInputStream(archive.getEntry("LIBRARY/" + item.getAttribute("href")))) {
                            wave = read(in);
                        }
                        try (AudioInputStream pcm = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wave))) {
                            assertEquals(pcm.getFormat().getSampleSizeInBits(), sampleBytes * 8, name);
                            assertEquals(pcm.getFrameLength(), Long.parseLong(item.getAttribute("sampleCount")), name);
                            assertEquals(read(pcm), cache, name + ": WAV and cache must agree");
                        }
                    }
                }
            }
        } finally {
            Files.deleteIfExists(output);
        }
    }

    private static byte[] read(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int count; (count = in.read(buffer)) != -1;) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }
}

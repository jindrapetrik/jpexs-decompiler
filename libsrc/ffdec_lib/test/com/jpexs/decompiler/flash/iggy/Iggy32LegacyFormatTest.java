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
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Iggy32LegacyFormatTest {

    private static void put(MemoryDataStream s, long position, long value, int width) throws IOException {
        long saved = s.position();
        s.seek(position, SeekMode.SET);
        if (width == 2) {
            s.writeUI16((int) value);
        } else {
            s.writeUI32(value);
        }
        s.seek(saved, SeekMode.SET);
    }

    private static void ref(MemoryDataStream s, long field, long target) throws IOException {
        put(s, field, target == 0 ? 1 : target - field, 4);
    }

    private static long wide(MemoryDataStream s, String text, Fixture f) throws IOException {
        long address = s.position();
        s.writeWChar(text);
        s.pad8bytes();
        for (int i = 0; i <= text.length(); i++) {
            f.field(address + 2L * i, 4);
        }
        return address;
    }

    private static class Fixture {

        byte[] data;
        List<IggyFixup.Field> fields = new ArrayList<>();
        long font, text, shape, source, dictionary;

        void field(long offset, int operation) {
            fields.add(new IggyFixup.Field(offset, operation));
        }
    }

    // Offsets below come from x86 DLL accesses, not the Java serializers.
    private Fixture fixture() throws IOException {
        Fixture f = new Fixture();
        MemoryDataStream s = new MemoryDataStream();
        s.writeBytes(new byte[64]);
        for (int offset : new int[]{0, 8, 12, 44, 56, 60}) {
            ref(s, offset, 0);
            f.field(offset, 2);
        }
        for (int offset : new int[]{4, 16, 20, 24, 28, 32, 36, 48, 52}) {
            f.field(offset, 5);
        }
        put(s, 4, 4, 4);
        put(s, 16, -25, 4);
        put(s, 20, -10, 4);
        put(s, 24, 640, 4);
        put(s, 28, 480, 4);
        put(s, 32, 2, 4);
        put(s, 40, 0x332211, 4);
        put(s, 48, 1, 4);
        put(s, 52, 1, 4);
        f.source = s.position();
        s.writeBytes(new byte[]{'x', '8', '6', '.', 's', 'w', 'f', 0});
        ref(s, 60, f.source);
        long label = wide(s, "intro", f), scene = wide(s, "scene", f);
        long labels = s.position();
        s.writeBytes(new byte[8]);
        ref(s, labels, label);
        put(s, labels + 4, 1, 4);
        f.field(labels, 2);
        f.field(labels + 4, 5);
        ref(s, 44, labels);
        long scenes = s.position();
        s.writeBytes(new byte[8]);
        ref(s, scenes, scene);
        put(s, scenes + 4, 0, 4);
        f.field(scenes, 2);
        f.field(scenes + 4, 5);
        ref(s, 56, scenes);
        f.dictionary = s.position();
        for (int i = 0; i < 4; i++) {
            f.field(s.position(), 2);
            s.writeUI32(1);
        }
        ref(s, 0, f.dictionary);
        f.font = s.position();
        s.writeBytes(new byte[208]);
        put(s, f.font, 0x0001ff16, 4);
        for (int offset : new int[]{4, 8, 12, 28, 32, 36, 60, 68, 76, 84, 104}) {
            ref(s, f.font + offset, 0);
            f.field(f.font + offset, 2);
        }
        ref(s, f.font + 84, f.source); // opaque, backward pointer: must survive moving the font
        for (int offset : new int[]{16, 18, 20, 22}) {
            f.field(f.font + offset, 4);
        }
        for (int offset : new int[]{40, 44, 48, 52, 56, 100}) {
            f.field(f.font + offset, 5);
        }
        f.field(f.font + 168, 8); // runtime context is four bytes here
        put(s, f.font + 16, 1, 2);
        put(s, f.font + 18, 1000, 2);
        put(s, f.font + 20, 200, 2);
        put(s, f.font + 24, 0x10103, 4);
        put(s, f.font + 100, 1, 4);
        ref(s, f.dictionary + 4, f.font);
        long name = wide(s, "Font32", f);
        ref(s, f.font + 76, name);
        long descriptor = s.position();
        s.writeBytes(new byte[16]);
        put(s, descriptor + 4, 1, 2);
        f.field(descriptor + 4, 4);
        f.field(descriptor + 12, 2);
        ref(s, f.font + 28, descriptor);
        f.field(descriptor + 8, 9);
        f.shape = s.position();
        s.writeBytes(new byte[40]);
        put(s, f.shape + 8, Float.floatToIntBits(1), 4);
        put(s, f.shape + 12, Float.floatToIntBits(1), 4);
        for (int offset : new int[]{0, 4, 8, 12, 20}) {
            f.field(f.shape + offset, 5);
        }
        for (int offset : new int[]{16, 24, 28, 32}) {
            ref(s, f.shape + offset, 0);
            f.field(f.shape + offset, 2);
        }
        ref(s, f.shape + 24, f.source); // auxiliary data must remain reachable
        put(s, f.shape + 20, 1, 4);
        ref(s, descriptor + 12, f.shape);
        long nodes = s.position();
        s.writeBytes(new byte[24]);
        put(s, nodes + 16, 1, 2);
        ref(s, f.shape + 16, nodes);
        for (int offset : new int[]{0, 4, 8, 12}) {
            f.field(nodes + offset, 5);
        }
        for (int offset : new int[]{18, 20, 22}) {
            f.field(nodes + offset, 4);
        }
        long codes = s.position();
        s.writeUI16('A');
        f.field(codes, 4);
        s.pad8bytes();
        ref(s, f.font + 32, codes);
        long advances = s.position();
        s.writeFloat(0.75f);
        f.field(advances, 5);
        s.pad8bytes();
        ref(s, f.font + 36, advances);
        long kernings = s.position();
        s.writeUI16('A');
        s.writeUI16('A');
        s.writeSI16(-40);
        s.pad8bytes();
        for (int i = 0; i < 3; i++) {
            f.field(kernings + 2L * i, 4);
        }
        ref(s, f.font + 60, kernings);
        put(s, f.font + 40, 1, 4);
        f.text = s.position();
        s.writeBytes(new byte[68]);
        put(s, f.text, 0x0002ff06, 4);
        ref(s, f.dictionary + 8, f.text);
        for (int offset : new int[]{4, 8, 12, 36, 64}) {
            ref(s, f.text + offset, 0);
            f.field(f.text + offset, 2);
        }
        long initial = wide(s, "hello32", f);
        ref(s, f.text + 64, initial);
        long sprite = s.position();
        s.writeBytes(new byte[36]);
        put(s, sprite, 0x0003ff03, 4);
        ref(s, f.dictionary + 12, sprite);
        for (int offset : new int[]{4, 8, 12, 24, 28, 32}) {
            ref(s, sprite + offset, 0);
            f.field(sprite + offset, 2);
        }
        put(s, sprite + 20, 2, 2);
        long show = s.position();
        s.writeUI32(1);
        s.writeUI32(1);
        f.field(show, 2);
        ref(s, sprite + 28, show);
        long remove = s.position();
        s.writeUI32(1);
        s.writeUI32(3);
        s.writeUI32(7);
        f.field(remove, 2);
        ref(s, show, remove);
        ref(s, 8, show);
        f.data = s.getAllBytes();
        return f;
    }

    private static byte[] index(Fixture f) throws IOException {
        IggyIndexBuilder b = new IggyIndexBuilder();
        IggyFixup.writeFields(b, f.fields, f.data.length);
        return b.getIndexBytes();
    }

    private static byte[] container(Fixture f, boolean bigEndian) throws IOException {
        byte[] payload = f.data.clone();
        if (bigEndian) {
            IggyFixup.parse(false, index(f), payload.length).normalize(payload, false, true);
        }
        MemoryDataStream s = new MemoryDataStream();
        new IggyHeader(0x900, bigEndian ? 2 : 1, 32, 1, 0, 0, new byte[12], 4).writeToDataStream(s);
        int offset = 96;
        byte[][] data = {new byte[]{7, 8, 9, 10}, payload, new byte[]{11, 12, 13}, index(f)};
        int[] type = {2, 1, 8, 0};
        for (int i = 0; i < 4; i++) {
            s.writeUI8(type[i]);
            s.writeUI8(1);
            s.writeUI8(i == 3 ? 1 : 0);
            s.writeUI8(0);
            s.writeUI32(bigEndian ? Integer.reverseBytes(data[i].length) & 0xffffffffL : data[i].length);
            s.writeUI32(0x12345678); // opaque descriptor word remains little-endian
            s.writeUI32(bigEndian ? Integer.reverseBytes(offset) & 0xffffffffL : offset);
            offset += data[i].length;
        }
        for (byte[] block : data) {
            s.writeBytes(block);
        }
        if (bigEndian) {
            // These byte-only opaque blocks require no scalar fixups. Add empty
            // programs so the explicit platform coverage check is satisfied.
            // Tests of opposite endianness use a minimal main-only container below.
            throw new AssertionError("Use endianContainer");
        }
        return s.getAllBytes();
    }

    private static byte[] endianContainer(Fixture f) throws IOException {
        byte[] payload = f.data.clone();
        IggyFixup.parse(false, index(f), payload.length).normalize(payload, false, true);
        MemoryDataStream s = new MemoryDataStream();
        new IggyHeader(0x900, 2, 32, 1, 0, 0, new byte[12], 2).writeToDataStream(s);
        int offset = 64;
        for (byte[] block : new byte[][]{payload, index(f)}) {
            s.writeUI8(offset == 64 ? 1 : 0);
            s.writeUI8(1);
            s.writeUI16(0);
            s.writeUI32(Integer.reverseBytes(block.length) & 0xffffffffL);
            s.writeUI32(0x12345678);
            s.writeUI32(Integer.reverseBytes(offset) & 0xffffffffL);
            offset += block.length;
        }
        s.writeBytes(payload);
        s.writeBytes(index(f));
        return s.getAllBytes();
    }

    private static IggyFile load(byte[] bytes) throws IOException {
        File temporary = File.createTempFile("iggy32-", ".iggy");
        temporary.deleteOnExit();
        Files.write(temporary.toPath(), bytes);
        return new IggyFile(temporary);
    }

    private static IggySwf loadMain(Fixture f) throws IOException {
        return new IggySwf(new MemoryDataStream(f.data), f.fields, false);
    }

    @Test
    public void readsDllLayoutAndBackwardsReferences() throws Exception {
        IggyFile file = load(container(fixture(), false));
        IggySwf swf = file.getSwf();
        Assert.assertTrue(swf.getHdr() instanceof IggyFlashHeader32Legacy);
        Assert.assertEquals(swf.getHdr().getFrameRate(), 24f);
        Assert.assertEquals(swf.getHdr().getXMin(), -25L);
        Assert.assertEquals(swf.getName(), "x86.swf");
        Assert.assertEquals(swf.getDictionary().size(), 4);
        Assert.assertTrue(swf.getDictionary().get(0).isNull());
        Assert.assertEquals(swf.getFrameLabels().get(0).getName(), "intro");
        Assert.assertEquals(swf.getScenes().get(0).getName(), "scene");
        Assert.assertEquals(swf.getTimeline().getCommands().get(0).getData().length, 8);
        Assert.assertEquals(swf.getSpriteTimelines().get(3).getCommands().get(1).getData().length, 12);
        Assert.assertEquals(swf.getFonts().get(0).getName(), "Font32");
        Assert.assertEquals(swf.getFonts().get(0).getCharIndices().getChars().get(0).charValue(), 'A');
        Assert.assertEquals(swf.getFonts().get(0).getChars().get(0).getNodes().size(), 1);
        Assert.assertEquals(swf.getFonts().get(0).getCharKernings().getKerningOffsets().get(0).shortValue(), (short) -40);
        Assert.assertEquals(swf.getTexts().get(0).getInitialText(), "hello32");
        SWF converted = IggyToSwfConvertor.getSwf(file);
        Assert.assertEquals(converted.frameCount, 2);
    }

    @Test
    public void unchangedRoundTripPreservesAllBlocks() throws Exception {
        byte[] bytes = container(fixture(), false);
        IggyFile file = load(bytes);
        file.updateFlashEntry();
        MemoryDataStream s = new MemoryDataStream();
        file.writeToDataStream(s);
        Assert.assertEquals(s.getAllBytes(), bytes);
    }

    @Test
    public void editsFontAndTextAndRetainsOpaqueReferences() throws Exception {
        Fixture f = fixture();
        IggyFile file = load(container(f, false));
        IggySwf swf = file.getSwf();
        swf.getFonts().get(0).setName("Renamed 32-bit font");
        swf.getTexts().get(0).setInitialText("new text");
        swf.getHdr().setMovieProperties(-50, -20, 800, 600, 24, 3, 0x123456);
        file.updateFlashEntry();
        MemoryDataStream saved = new MemoryDataStream();
        file.writeToDataStream(saved);
        IggyFile reopened = load(saved.getAllBytes());
        IggySwf result = reopened.getSwf();
        Assert.assertEquals(result.getFonts().get(0).getName(), "Renamed 32-bit font");
        Assert.assertEquals(result.getTexts().get(0).getInitialText(), "new text");
        Assert.assertEquals(result.getHdr().getXMin(), -50L);
        MemoryDataStream data = new MemoryDataStream(result.getOriginalData());
        long font = result.getDictionary().get(1).getAddress();
        data.seek(font + 84, SeekMode.SET);
        Assert.assertEquals(IggyLayout.readPointer(data, false), f.source);
        data.seek(font + 28, SeekMode.SET);
        long desc = IggyLayout.readPointer(data, false);
        data.seek(desc + 12, SeekMode.SET);
        long shape = IggyLayout.readPointer(data, false);
        data.seek(shape + 24, SeekMode.SET);
        Assert.assertEquals(IggyLayout.readPointer(data, false), f.source);
        byte[] actual = result.getOriginalData();
        byte[] original = f.data.clone();
        for (IggyFixup.Field field : f.fields) {
            if (field.operation == 2) {
                System.arraycopy(actual, (int) field.offset, original, (int) field.offset, 4);
            }
        }
        Assert.assertEquals(Arrays.copyOfRange(actual, (int) f.font, f.data.length),
                Arrays.copyOfRange(original, (int) f.font, f.data.length));
        Assert.assertEquals(reopened.getEntryData(0), new byte[]{7, 8, 9, 10});
        Assert.assertEquals(reopened.getEntryData(2), new byte[]{11, 12, 13});
        reopened.updateFlashEntry();
        MemoryDataStream again = new MemoryDataStream();
        reopened.writeToDataStream(again);
        Assert.assertEquals(again.getAllBytes(), saved.getAllBytes());
    }

    @Test
    public void conversionWritesNewGlyphsInX86Layout() throws Exception {
        IggyFile file = load(container(fixture(), false));
        SWF swf = IggyToSwfConvertor.getSwf(file);
        SwfToIggyConvertor.updateIggy(file.getSwf(), swf);
        file.updateFlashEntry();
        MemoryDataStream saved = new MemoryDataStream();
        file.writeToDataStream(saved);
        IggySwf reopened = load(saved.getAllBytes()).getSwf();
        Assert.assertEquals(reopened.getFonts().get(0).getCharacterCount(), 1L);
        Assert.assertEquals(reopened.getFonts().get(0).getCharIndices().getChars().get(0).charValue(), 'A');
    }

    @Test
    public void endianHeaderDescriptorsAndPayloadAreNormalized() throws Exception {
        Fixture f = fixture();
        IggyFile file = load(endianContainer(f));
        Assert.assertEquals(file.getSwf().getOriginalData(), f.data);
        MemoryDataStream saved = new MemoryDataStream();
        file.writeToDataStream(saved);
        Assert.assertEquals(saved.getAllBytes()[8], (byte) 1);
        Assert.assertEquals(load(saved.getAllBytes()).getSwf().getFonts().get(0).getName(), "Font32");
    }

    @Test
    public void contextOperationsUseNativePointerWidth() throws Exception {
        for (int op = 8; op <= 11; op++) {
            byte[] program = {0, (byte) 0xfd, 4, 1, 0, (byte) op};
            Assert.assertEquals(IggyFixup.parse(false, program, 4).getFields().size(), 1);
            try {
                IggyFixup.parse(true, program, 4);
                Assert.fail();
            } catch (IOException expected) {
            }
        }
    }

    @Test
    public void rejectsInvalidReferencesAndUnsupportedChanges() throws Exception {
        Fixture f = fixture();
        IggySwf swf = loadMain(f);
        try {
            swf.getHdr().setMovieProperties(0, 0, 1, 1, 30, 1, 0);
            Assert.fail();
        } catch (IOException expected) {
        }
        MemoryDataStream bytes = new MemoryDataStream();
        bytes.writeBytes(f.data);
        put(bytes, 4, 0x40000000, 4);
        f.data = bytes.getAllBytes();
        try {
            loadMain(f);
            Assert.fail();
        } catch (IOException expected) {
        }
    }
}

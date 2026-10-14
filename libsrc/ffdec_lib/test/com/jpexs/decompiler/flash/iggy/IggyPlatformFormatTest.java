/*
 *  Copyright (C) 2010-2026 JPEXS, All rights reserved.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library.
 */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyPlatformFormatTest {

    private static final byte[] OPAQUE = {(byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef};

    @DataProvider(name = "platforms")
    public Object[][] platforms() {
        return new Object[][]{{1, 1}, {2, 1}, {1, 2}, {2, 2}};
    }

    private static void put(MemoryDataStream s, long at, long value, int width) throws IOException {
        long end = s.position();
        s.seek(at, SeekMode.SET);
        if (width == 4) {
            s.writeUI32(value);
        } else {
            s.writeUI16((int) value);
        }
        s.seek(end, SeekMode.SET);
    }

    private static void ref(MemoryDataStream s, long at, long target) throws IOException {
        put(s, at, target == 0 ? 1 : target - at, 4);
    }

    private static byte[] program(List<IggyFixup.Field> fields, int size) throws IOException {
        IggyIndexBuilder builder = new IggyIndexBuilder();
        IggyFixup.writeFields(builder, fields, size);
        return builder.getIndexBytes();
    }

    private static final class Fixture {

        byte[] main, mainProgram, textures, textureProgram;
    }

    // Independent offsets from sub_1003B9D0, sub_1006BEB0 and the resource API.
    private static Fixture fixture(String source) throws IOException {
        Fixture f = new Fixture();
        MemoryDataStream s = new MemoryDataStream();
        List<IggyFixup.Field> fields = new ArrayList<>();
        s.writeBytes(new byte[128]);
        for (int offset : new int[]{0, 4, 8, 12, 16, 20, 80, 84, 96, 100, 104, 108}) {
            ref(s, offset, 0);
            fields.add(new IggyFixup.Field(offset, 2));
        }
        for (int offset : new int[]{24, 28, 32, 36, 40, 44, 48, 52, 56, 64, 88, 120}) {
            fields.add(new IggyFixup.Field(offset, 5));
        }
        put(s, 24, -15, 4);
        put(s, 32, 640, 4);
        put(s, 36, 480, 4);
        put(s, 40, 2, 4);
        put(s, 44, 1, 4);
        put(s, 60, 0x336699, 4);
        put(s, 64, Float.floatToIntBits(30), 4);
        // Exercise 64-bit scalar and bitfield fixups with nonzero field offsets.
        put(s, 72, 0x12345678, 4);
        put(s, 76, 0x87654321L, 4);
        fields.add(new IggyFixup.Field(72, 6));
        s.seek(124, SeekMode.SET);
        s.writeUI8(0x96);
        s.seek(128, SeekMode.SET);
        fields.add(new IggyFixup.Field(124, 3));
        long dictionary = s.position();
        ref(s, 0, dictionary);
        for (int i = 0; i < 2; i++) {
            fields.add(new IggyFixup.Field(s.position(), 2));
            s.writeUI32(1);
        }
        long text = s.position();
        s.writeBytes(new byte[68]);
        put(s, text, 0x0001ff06, 4);
        fields.add(new IggyFixup.Field(text + 2, 4));
        ref(s, dictionary + 4, text);
        for (int offset : new int[]{8, 36, 64}) {
            ref(s, text + offset, 0);
            fields.add(new IggyFixup.Field(text + offset, 2));
        }
        for (int offset : new int[]{16, 20, 24, 28}) {
            put(s, text + offset, Float.floatToIntBits(offset * 0.25f), 4);
            fields.add(new IggyFixup.Field(text + offset, 5));
        }
        for (int offset : new int[]{32, 34, 44, 46, 48, 50, 52, 54}) {
            fields.add(new IggyFixup.Field(text + offset, 4));
        }
        put(s, text + 32, 0x105, 2);
        put(s, text + 54, -150, 2);
        put(s, text + 40, 0x80443322L, 4); // RGBA stays in byte order.
        long initial = s.position();
        ref(s, text + 64, initial);
        String value = "Native žluťoučký";
        s.writeWChar(value);
        for (int i = 0; i <= value.length(); i++) {
            fields.add(new IggyFixup.Field(initial + 2 * i, 4));
        }
        ref(s, 4, s.position());
        s.writeBytes(source.getBytes(StandardCharsets.UTF_8));
        s.writeUI8(0);
        f.main = s.getAllBytes();
        f.mainProgram = program(fields, f.main.length);

        MemoryDataStream textures = new MemoryDataStream();
        textures.writeBytes(new byte[36]); // Header 12 + two records of 12.
        put(textures, 0, 2, 2);
        put(textures, 12 + 10, 7, 2);
        put(textures, 24 + 10, 0x1234, 2);
        f.textures = textures.getAllBytes();
        f.textureProgram = program(Arrays.asList(new IggyFixup.Field(0, 4),
                new IggyFixup.Field(22, 4), new IggyFixup.Field(34, 4)), f.textures.length);
        return f;
    }

    private static byte[] container(Fixture f, int endian, int bitfields, int flags, boolean metadata) throws IOException {
        byte[] main = f.main.clone(), textures = f.textures.clone();
        IggyFixup.parse(false, f.mainProgram, main.length).normalize(main, endian == 1, bitfields == 1);
        IggyFixup.parse(false, f.textureProgram, textures.length).normalize(textures, endian == 1, bitfields == 1);
        byte[][] blocks = metadata ? new byte[][]{OPAQUE, textures, f.textureProgram, main, f.mainProgram}
                : new byte[][]{OPAQUE, main, f.mainProgram};
        long[] types = metadata ? new long[]{2, 8, 0, 1, 0x10000} : new long[]{2, 1, 0};
        MemoryDataStream s = new MemoryDataStream();
        new IggyHeader(0x900, endian, 32, bitfields, flags, 0, new byte[12], blocks.length).writeToDataStream(s);
        // The 32-bit layout's outer header and descriptors are LE even for a BE payload.
        put(s, 28, blocks.length, 4);
        int offset = 32 + 16 * blocks.length;
        for (int i = 0; i < blocks.length; i++) {
            new IggySubFileEntry(types[i], blocks[i].length, 0x10203040, offset).writeToDataStream(s);
            offset += blocks[i].length;
        }
        for (byte[] block : blocks) {
            s.writeBytes(block);
        }
        return s.getAllBytes();
    }

    private static IggyFile load(byte[] bytes) throws IOException {
        Path file = Files.createTempFile("iggy-native32", ".iggy");
        try {
            Files.write(file, bytes);
            return new IggyFile(file.toFile());
        } finally {
            Files.delete(file);
        }
    }

    private static byte[] save(IggyFile file) throws IOException {
        file.updateFlashEntry();
        MemoryDataStream out = new MemoryDataStream();
        file.writeToDataStream(out);
        return out.getAllBytes();
    }

    @Test(dataProvider = "platforms")
    public void selectsOnlyMainAndTexturesAndNormalizesThePayload(int endian, int bitfields) throws Exception {
        Fixture f = fixture("native32-ui.swf");
        IggyFile file = load(container(f, endian, bitfields, 3, true));
        Assert.assertTrue(file.getSwf().getHdr() instanceof IggyFlashHeader32);
        Assert.assertEquals(file.getSwf().getOriginalData(), f.main);
        Assert.assertEquals(file.getSwf().getSourceName(), "native32-ui.swf");
        Assert.assertEquals(file.getSwfName(), "native32-ui.swf");
        Assert.assertEquals(file.getSwfFrameRate(), 30f);
        Assert.assertEquals(file.getSwf().getTexts().get(0).getLeading(), -150);
        Assert.assertEquals(file.getEntryData(0), OPAQUE);
        Assert.assertEquals(file.getEntryData(1), f.textures);
        Assert.assertEquals(file.getTextureMetadata().size(), 1);
        List<IggyTextureMetadata.Record> records = file.getTextureMetadata().get(0).getRecords();
        Assert.assertEquals(records.size(), 2);
        Assert.assertEquals(records.get(0).getCharacterId(), 7);
        Assert.assertEquals(records.get(1).getCharacterId(), 0x1234);
        Assert.assertEquals(records.get(0).getData(), Arrays.copyOfRange(f.textures, 12, 24));
        Assert.assertEquals(file.getSubFileEntry(2).getTargetIndex(), 0);
        Assert.assertEquals(file.getSubFileEntry(4).getTargetIndex(), 1);
        byte[] saved = save(file);
        Assert.assertEquals(saved[8], (byte) 1);
        Assert.assertEquals(saved[10], (byte) 1);
        IggyFile reopened = load(saved);
        Assert.assertEquals(reopened.getSwf().getOriginalData(), f.main);
        Assert.assertEquals(reopened.getEntryData(0), OPAQUE);
        Assert.assertEquals(save(reopened), saved);
    }

    @Test(dataProvider = "platforms")
    public void editsKeepFixupTargetsAndResourceBlocks(int endian, int bitfields) throws Exception {
        Fixture f = fixture("native32-ui.swf");
        IggyFile file = load(container(f, endian, bitfields, 3, true));
        SWF swf = IggyToSwfConvertor.getSwf(file);
        for (Tag t : swf.getTags()) {
            if (t instanceof DefineEditTextTag) {
                DefineEditTextTag text = (DefineEditTextTag) t;
                text.initialText = "Native edited text with a longer name";
                text.hasFontClass = true;
                text.fontClass = "fonts.NativeFont";
                text.fontHeight = 600;
            }
        }
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        SwfToIggyConvertor.updateIggy(file.getSwf(), new SWF(new ByteArrayInputStream(bytes.toByteArray()), false));
        byte[] saved = save(file);
        IggyFile reopened = load(saved);
        Assert.assertEquals(reopened.getSwf().getTexts().get(0).getInitialText(), "Native edited text with a longer name");
        Assert.assertEquals(reopened.getSwf().getTexts().get(0).getFontClassName(), "fonts.NativeFont");
        Assert.assertEquals(reopened.getSwf().getSourceName(), "native32-ui.swf");
        Assert.assertEquals(reopened.getEntryData(0), OPAQUE);
        Assert.assertEquals(reopened.getEntryData(1), f.textures);
        Assert.assertEquals(reopened.getEntryData(2), f.textureProgram);
        Assert.assertEquals(reopened.getSubFileEntry(4).getTargetIndex(), 1);
        Assert.assertEquals(save(reopened), saved);
    }

    @Test
    public void sourceUtf8EmptyNamesAndUnchangedFiles() throws Exception {
        Fixture f = fixture("české-native32.swf");
        byte[] original = container(f, 1, 1, 0, false);
        IggyFile file = load(original);
        Assert.assertEquals(file.getSwfName(), "české-native32.swf");
        Assert.assertEquals(file.getTextureMetadata().size(), 0);
        Assert.assertEquals(save(file), original);
        Fixture empty = fixture("");
        IggyFile noName = load(container(empty, 1, 1, 0, false));
        Assert.assertEquals(noName.getSwf().getSourceName(), "");
        Assert.assertTrue(noName.getSwfName().startsWith("iggy-native32"));
        Assert.assertTrue(noName.getSwfName().endsWith(".swf"));
    }

    @Test
    public void rebuildingIndexKeepsTheSelectedBlockOrdinal() throws Exception {
        Fixture f = fixture("native32-ui.swf");
        IggyFile file = load(container(f, 1, 1, 0, true));
        file.removeIndexEntries();
        file.getSwf().getTexts().get(0).setInitialText("Changed after removing fixup entries");
        IggyFile reopened = load(save(file));
        Assert.assertEquals(reopened.getNumEntries(), 4);
        Assert.assertEquals(reopened.getSubFileEntry(3).getType(), 0);
        Assert.assertEquals(reopened.getSubFileEntry(3).getTargetIndex(), 1);
        Assert.assertEquals(reopened.getSwf().getTexts().get(0).getInitialText(),
                "Changed after removing fixup entries");
        Assert.assertEquals(reopened.getEntryData(0), OPAQUE);
        Assert.assertEquals(reopened.getEntryData(1), f.textures);
    }

    @Test
    public void toleratesEmptyTextureDescriptorAndRejectsTruncatedMetadataInContainer() throws Exception {
        Fixture f = fixture("native32-ui.swf");
        f.textures = new byte[0];
        f.textureProgram = program(new ArrayList<>(), 0);
        byte[] original = container(f, 1, 1, 0, true);
        IggyFile file = load(original);
        Assert.assertTrue(file.getTextureMetadata().get(0).getRecords().isEmpty());
        Assert.assertEquals(save(file), original);

        Fixture truncated = fixture("native32-ui.swf");
        truncated.textures = Arrays.copyOf(truncated.textures, 35);
        truncated.textureProgram = program(new ArrayList<>(), 35);
        try {
            load(container(truncated, 1, 1, 0, true));
            Assert.fail("Expected metadata length error");
        } catch (IOException expected) {
            Assert.assertTrue(expected.getMessage().contains("texture metadata"), expected.getMessage());
        }
    }

    @Test
    public void rejectsMissingPlatformFlagsWithoutFallingBackToLegacy() throws Exception {
        Fixture f = fixture("native32-ui.swf");
        for (int[] platform : new int[][]{{2, 1, 0}, {2, 1, 2}, {1, 2, 0}, {1, 2, 1}, {2, 2, 1}, {2, 2, 2}}) {
            try {
                load(container(f, platform[0], platform[1], platform[2], false));
                Assert.fail("Expected platform fixup error");
            } catch (IOException expected) {
                Assert.assertTrue(expected.getMessage().contains("required fixup"), expected.getMessage());
            }
        }
    }

    @Test
    public void rejectsTruncatedMetadataAndDoesNotExposeMutableRecords() throws Exception {
        Fixture f = fixture("native32-ui.swf");
        Assert.assertTrue(new IggyTextureMetadata(new byte[0], false).getRecords().isEmpty());
        Assert.assertTrue(new IggyTextureMetadata(new byte[12], false).getRecords().isEmpty());
        for (int size : new int[]{1, 2, 11, 12, 23, 35}) {
            try {
                new IggyTextureMetadata(Arrays.copyOf(f.textures, size), false);
                Assert.fail("Expected metadata length error");
            } catch (IOException expected) {
                Assert.assertTrue(expected.getMessage().contains("texture metadata"));
            }
        }
        IggyTextureMetadata metadata = new IggyTextureMetadata(f.textures, false);
        byte[] record = metadata.getRecords().get(0).getData();
        record[10] = 99;
        Assert.assertEquals(metadata.getRecords().get(0).getCharacterId(), 7);
        Assert.assertEquals(metadata.getRecords().get(0).getData()[10], (byte) 7);
    }
}

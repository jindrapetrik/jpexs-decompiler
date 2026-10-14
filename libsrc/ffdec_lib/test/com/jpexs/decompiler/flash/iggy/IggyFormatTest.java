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
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;

public class IggyFormatTest {

    private interface IOAction {

        void run() throws IOException;
    }

    private void invalid(IOAction action) throws IOException {
        try {
            action.run();
            Assert.fail("Expected IOException");
        } catch (IOException expected) {
        }
    }

    private static void value(MemoryDataStream stream, long position, long value, int width) throws IOException {
        long saved = stream.position();
        stream.seek(position, SeekMode.SET);
        if (width == 8) {
            stream.writeUI64(value);
        } else {
            stream.writeUI32(value);
        }
        stream.seek(saved, SeekMode.SET);
    }

    private static void pointer(MemoryDataStream stream, long position, long address) throws IOException {
        value(stream, position, address == 0 ? 1 : address - position, 8);
    }

    private static long string(MemoryDataStream stream, String text) throws IOException {
        long result = stream.position();
        stream.writeWChar(text);
        stream.pad8bytes();
        return result;
    }

    private static byte[] abc(int minor) {
        return minor == 18
                ? new byte[]{18, 0, 46, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0}
                : new byte[]{(byte) minor, 0, 46, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
    }

    private static final class Fixture {

        byte[] data;
        byte[] fixups;
        List<IggyFixup.Field> fields;
        long dictionary, unknown, text, firstAbc;
    }

    private Fixture fixture(boolean withAbc) throws IOException {
        Fixture f = new Fixture();
        MemoryDataStream s = new MemoryDataStream();
        s.writeBytes(new byte[184]);
        List<IggyFixup.Field> fields = new ArrayList<>();
        int[] pointers = {0, 8, 16, 24, 32, 40, 0x70, 0x78, 0x88, 0x90, 0x98, 0xA0};
        for (int position : pointers) {
            pointer(s, position, 0);
            fields.add(new IggyFixup.Field(position, 2));
        }
        for (int position : new int[]{0x30, 0x34, 0x38, 0x3C, 0x40, 0x44, 0x48, 0x4C, 0x50, 0x58, 0x80, 0xB0}) {
            fields.add(new IggyFixup.Field(position, 5));
        }
        value(s, 0x30, -25, 4);
        value(s, 0x38, 640, 4);
        value(s, 0x3C, 480, 4);
        value(s, 0x40, 4, 4);
        value(s, 0x44, 2, 4);
        value(s, 0x54, 0x00332211, 4);
        value(s, 0x58, Float.floatToIntBits(24), 4);
        f.dictionary = s.position();
        pointer(s, 0, f.dictionary);
        for (int i = 0; i < 4; i++) {
            fields.add(new IggyFixup.Field(s.position(), 2));
            s.writeUI64(1);
        }
        f.unknown = s.position();
        byte[] unknown = new byte[16];
        unknown[0] = 99;
        unknown[1] = (byte) 255;
        unknown[2] = 1;
        s.writeBytes(unknown);
        pointer(s, f.dictionary + 8, f.unknown);
        f.text = s.position();
        s.writeBytes(new byte[104]);
        value(s, f.text, 0x0003FF06, 4);
        for (int offset : new int[]{16, 56, 96}) {
            pointer(s, f.text + offset, 0);
            fields.add(new IggyFixup.Field(f.text + offset, 2));
        }
        long textString = string(s, "old");
        pointer(s, f.text + 96, textString);
        pointer(s, f.dictionary + 24, f.text);
        long name = string(s, "movie.swf");
        pointer(s, 0x90, name);
        long source = s.position();
        s.writeBytes(new byte[]{'s', 'o', 'u', 'r', 'c', 'e', 0});
        s.pad8bytes();
        pointer(s, 8, source);
        long label = s.position();
        s.writeUI64(1);
        s.writeUI32(1);
        s.writeUI32(0x87654321L);
        fields.add(new IggyFixup.Field(label, 2));
        fields.add(new IggyFixup.Field(label + 8, 5));
        long labelName = string(s, "second");
        pointer(s, label, labelName);
        pointer(s, 0x20, label);
        value(s, 0x4C, 1, 4);
        long scene = s.position();
        s.writeUI64(1);
        s.writeUI32(0);
        s.writeUI32(0);
        fields.add(new IggyFixup.Field(scene, 2));
        fields.add(new IggyFixup.Field(scene + 8, 5));
        long sceneName = string(s, "main");
        pointer(s, scene, sceneName);
        pointer(s, 0x28, scene);
        value(s, 0x50, 1, 4);
        long symbols = s.position();
        for (int id : new int[]{0, 3}) {
            fields.add(new IggyFixup.Field(s.position(), 2));
            s.writeUI64(1);
            fields.add(new IggyFixup.Field(s.position(), 2));
            s.writeUI64(1);
            fields.add(new IggyFixup.Field(s.position(), 5));
            s.writeUI32(id);
            s.writeUI32(0);
        }
        long className = string(s, "Main");
        pointer(s, symbols + 8, className);
        long textClass = string(s, "TextClass");
        pointer(s, symbols + 24, f.text);
        pointer(s, symbols + 32, textClass);
        pointer(s, 0xA0, symbols);
        value(s, 0xB0, 2, 4);
        long timeline = s.position();
        s.writeUI64(1);
        s.writeUI64(1);
        fields.add(new IggyFixup.Field(timeline, 2));
        pointer(s, 16, timeline);
        if (withAbc) {
            f.firstAbc = s.position();
            long previous = -1;
            for (int minor : new int[]{16, 18}) {
                long address = s.position();
                if (previous >= 0) {
                    pointer(s, previous, address);
                }
                fields.add(new IggyFixup.Field(address, 2));
                fields.add(new IggyFixup.Field(address + 8, 5));
                s.writeUI64(1);
                byte[] bytes = abc(minor);
                s.writeUI32(bytes.length);
                s.writeBytes(bytes);
                s.pad8bytes();
                previous = address;
            }
            pointer(s, 0x98, f.firstAbc);
            value(s, 0xA8, 1, 4);
        }
        f.data = s.getAllBytes();
        f.fields = fields;
        IggyIndexBuilder builder = new IggyIndexBuilder();
        IggyFixup.writeFields(builder, fields, f.data.length);
        f.fixups = builder.getIndexBytes();
        return f;
    }

    private byte[] container(Fixture f) throws IOException {
        return container(f, false);
    }

    private byte[] container(Fixture f, boolean emptyTexture) throws IOException {
        byte[] metadata = emptyTexture ? new byte[0] : new byte[28];
        if (!emptyTexture) {
            metadata[0] = 1;
            metadata[26] = 3;
        }
        IggyIndexBuilder textureFixups = new IggyIndexBuilder();
        if (!emptyTexture) {
            IggyFixup.writeFields(textureFixups, Arrays.asList(new IggyFixup.Field(0, 4), new IggyFixup.Field(26, 4)), metadata.length);
        }
        byte[][] blocks = {metadata, f.data, textureFixups.getIndexBytes(), f.fixups, f.fixups};
        //Split main-program operations between two blocks; no double application.
        IggyIndexBuilder empty = new IggyIndexBuilder();
        blocks[4] = empty.getIndexBytes();
        long[] types = {8 | 0x01000000L, 1 | 0x100, 0, 0x10000, 0x10000};
        MemoryDataStream s = new MemoryDataStream();
        new IggyHeader(0x900, 1, 64, 1, 3, 0, new byte[12], blocks.length).writeToDataStream(s);
        long offset = 32 + 16 * blocks.length;
        for (int i = 0; i < blocks.length; i++) {
            new IggySubFileEntry(types[i], blocks[i].length, 0x12345678, offset).writeToDataStream(s);
            offset += blocks[i].length;
        }
        for (byte[] block : blocks) {
            s.writeBytes(block);
        }
        return s.getAllBytes();
    }

    private IggyFile load(byte[] bytes) throws IOException {
        File temporary = File.createTempFile("iggy-regression", ".iggy");
        temporary.deleteOnExit();
        Files.write(temporary.toPath(), bytes);
        return new IggyFile(temporary);
    }

    @Test
    public void unsignedValuesAndTruncatedStrings() throws IOException {
        MemoryDataStream s = new MemoryDataStream();
        s.writeUI32(0xffffffffL);
        s.writeUI64(0x0000000080000000L);
        s.writeUI64(-9);
        MemoryDataStream input = new MemoryDataStream(s.getAllBytes());
        Assert.assertEquals(input.readUI32(), 0xffffffffL);
        Assert.assertEquals(input.readUI64(), 0x80000000L);
        Assert.assertEquals(input.readUI64(), -9);
        invalid(() -> new MemoryDataStream(new byte[]{65, 0}).readWChar());
    }

    @Test
    public void packedDescriptorFlags() throws IOException {
        MemoryDataStream s = new MemoryDataStream();
        new IggySubFileEntry(0x12030101L, 8, 17, 100).writeToDataStream(s);
        IggySubFileEntry entry = new IggySubFileEntry(new MemoryDataStream(s.getAllBytes()));
        Assert.assertEquals(entry.getType(), 1);
        Assert.assertEquals(entry.getCopyFlag(), 1);
        Assert.assertEquals(entry.getTargetIndex(), 3);
        Assert.assertEquals(entry.getMetadata(), 18);
    }

    @Test
    public void signed16BitValues() throws IOException {
        int[] values = {Short.MIN_VALUE, -129, -1, 0, 1, 128, Short.MAX_VALUE};
        MemoryDataStream output = new MemoryDataStream();
        for (int value : values) {
            Assert.assertTrue(output.writeSI16(value));
        }
        Assert.assertEquals(output.getAllBytes(), new byte[]{0, (byte) 128, 127, (byte) 255, (byte) 255, (byte) 255,
            0, 0, 1, 0, (byte) 128, 0, (byte) 255, 127});
        MemoryDataStream input = new MemoryDataStream(output.getAllBytes());
        for (int value : values) {
            Assert.assertEquals(input.readSI16(), value);
        }
        invalid(() -> input.readSI16());
        invalid(() -> new MemoryDataStream(new byte[]{(byte) 255}).readSI16());
    }

    @Test
    public void signed32BitValues() throws IOException {
        long[] values = {Integer.MIN_VALUE, -129, -1, 0, 1, 0x12345678, Integer.MAX_VALUE};
        MemoryDataStream output = new MemoryDataStream();
        for (long value : values) {
            Assert.assertTrue(output.writeSI32(value));
        }
        Assert.assertEquals(output.getAllBytes(), new byte[]{0, 0, 0, (byte) 128, 127, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255, 0, 0, 0, 0, 1, 0, 0, 0,
            0x78, 0x56, 0x34, 0x12, (byte) 255, (byte) 255, (byte) 255, 127});
        MemoryDataStream input = new MemoryDataStream(output.getAllBytes());
        for (long value : values) {
            Assert.assertEquals(input.readSI32(), value);
        }
        invalid(() -> input.readSI32());
        for (int length = 1; length < 4; length++) {
            final byte[] truncated = new byte[length];
            invalid(() -> new MemoryDataStream(truncated).readSI32());
        }
    }

    @Test
    public void fixupArrayBatchingAndValidation() throws IOException {
        for (int count : new int[]{0, 1, 256, 257, 1024}) {
            IggyIndexBuilder builder = new IggyIndexBuilder();
            builder.write64bitPointerArray(count);
            IggyFixup program = IggyFixup.parse(true, builder.getIndexBytes(), 8L * count);
            Assert.assertEquals(program.getFields().size(), count);
            Assert.assertEquals(program.getLength(), 8L * count);
        }
        invalid(() -> IggyFixup.parse(true, new byte[]{0, (byte) 0xD3, 0}, 16));
        invalid(() -> IggyFixup.parse(true, new byte[]{0, (byte) 0xD2, 1}, 8));
        invalid(() -> IggyFixup.parse(true, new byte[]{0, (byte) 0xFD, 8, 1, 0}, 8));
        IggyIndexBuilder builder = new IggyIndexBuilder();
        builder.skipOneInIndex();
        builder.write64bitArray(1);
        Assert.assertEquals(IggyFixup.parse(true, builder.getIndexBytes(), 8).getLength(), 8L);
    }

    @Test
    public void dictionaryHolesAndMovieMetadata() throws IOException {
        Fixture f = fixture(true);
        IggySwf movie = new IggySwf(new MemoryDataStream(f.data), f.fields);
        Assert.assertEquals(movie.getDictionary().size(), 4);
        Assert.assertTrue(movie.getDictionary().get(2).isNull());
        Assert.assertEquals(movie.getDictionary().get(1).getType(), 99);
        Assert.assertEquals(movie.getTexts().size(), 1);
        Assert.assertEquals(movie.getTexts().get(0).getTextIndex(), 3);
        Assert.assertEquals(movie.getSourceName(), "source");
        Assert.assertEquals(movie.getHdr().getXMin(), -25L);
        Assert.assertEquals(movie.getFrameLabels().get(0).getName(), "second");
        Assert.assertEquals(movie.getScenes().get(0).getName(), "main");
        Assert.assertEquals(movie.getSymbolClasses().size(), 2);
        Assert.assertEquals(movie.getTimeline().getCommands().get(0).getType(), 1);
        Assert.assertEquals(movie.getAbcBlocks().size(), 2);
        Assert.assertEquals(movie.getAbcBlocks().get(1).getData(), abc(18));
        MemoryDataStream out = new MemoryDataStream();
        movie.writeToDataStream(out);
        Assert.assertEquals(out.getAllBytes(), f.data);
    }

    @Test
    public void abcStartsAtTwelveAndCyclesAreRejected() throws IOException {
        MemoryDataStream out = new MemoryDataStream();
        out.setIndexing(new IggyIndexBuilder());
        new IggyDeclStrings(abc(18)).writeToDataStream(out);
        Assert.assertEquals(Arrays.copyOfRange(out.getAllBytes(), 12, 12 + abc(18).length), abc(18));
        Assert.assertEquals(new IggyDeclStrings(new MemoryDataStream(out.getAllBytes())).getData(), abc(18));
        Fixture f = fixture(true);
        MemoryDataStream data = new MemoryDataStream();
        data.writeBytes(f.data);
        pointer(data, f.firstAbc, f.firstAbc);
        invalid(() -> new IggySwf(new MemoryDataStream(data.getAllBytes()), f.fields));
        Fixture noAbc = fixture(false);
        Assert.assertTrue(new IggySwf(new MemoryDataStream(noAbc.data), noAbc.fields).getAbcBlocks().isEmpty());
    }

    @Test
    public void editsPreserveObjectsIdsAndOtherFixupTargets() throws IOException {
        Fixture f = fixture(true);
        IggyFile file = load(container(f));
        byte[] before = Files.readAllBytes(file.getOriginalFile().toPath());
        Assert.assertEquals(file.getTextureMetadata().get(0).getRecords().get(0).getCharacterId(), 3);
        Assert.assertEquals(Files.readAllBytes(file.getOriginalFile().toPath()), before);
        file.getSwf().getTexts().get(0).setInitialText("new and substantially longer");
        file.getSwf().getAbcBlocks().get(0).setData(abc(18));
        Assert.assertTrue(file.updateFlashEntry());
        MemoryDataStream serialized = new MemoryDataStream();
        file.writeToDataStream(serialized);
        IggyFile reloaded = load(serialized.getAllBytes());
        Assert.assertEquals(reloaded.getHeader().getNumSubfiles(), 4L);
        Assert.assertEquals(reloaded.getNumEntries(), 4);
        Assert.assertEquals(reloaded.getSubFileEntry(1).getCopyFlag(), 1);
        Assert.assertEquals(reloaded.getSubFileEntry(2).getTargetIndex(), 0);
        Assert.assertEquals(reloaded.getSubFileEntry(3).getTargetIndex(), 1);
        Assert.assertEquals(reloaded.getEntryData(0), file.getEntryData(0));
        Assert.assertEquals(reloaded.getSwf().getDictionary().get(1).getAddress(), f.unknown);
        Assert.assertTrue(reloaded.getSwf().getDictionary().get(2).isNull());
        Assert.assertEquals(reloaded.getSwf().getTexts().get(0).getInitialText(), "new and substantially longer");
        Assert.assertEquals(reloaded.getSwf().getAbcBlocks().get(0).getData(), abc(18));
        Assert.assertEquals(reloaded.getSwf().getAbcBlocks().get(1).getData(), abc(18));
        Assert.assertEquals(reloaded.getSwf().getSymbolClasses().get(1).getObjectAddress(), reloaded.getSwf().getDictionary().get(3).getAddress());
        Assert.assertEquals(reloaded.getSwf().getFrameLabels().get(0).getName(), "second");
        Assert.assertTrue(file.updateFlashEntry());
        MemoryDataStream again = new MemoryDataStream();
        file.writeToDataStream(again);
        Assert.assertEquals(again.getAllBytes(), serialized.getAllBytes());
    }

    @Test
    public void swfExportPreservesAbcVersionsAndCharacterIds() throws IOException {
        IggyFile file = load(container(fixture(true)));
        SWF swf = IggyToSwfConvertor.getSwf(file);
        Assert.assertEquals(swf.frameCount, 2);
        int abcCount = 0;
        boolean symbols = false;
        for (Tag tag : swf.getTags()) {
            if (tag instanceof DoABC2Tag) {
                abcCount++;
            }
            if (tag instanceof SymbolClassTag) {
                Assert.assertEquals(((SymbolClassTag) tag).tags, Arrays.asList(0, 3));
                symbols = true;
            }
        }
        Assert.assertEquals(abcCount, 2);
        Assert.assertTrue(symbols);
        SwfToIggyConvertor.updateIggy(file.getSwf(), swf);
        Assert.assertEquals(file.getSwf().getAbcBlocks().get(0).getData(), abc(16));
        Assert.assertEquals(file.getSwf().getAbcBlocks().get(1).getData(), abc(18));
    }

    @Test
    public void invalidBlockRangesAndTargets() throws IOException {
        byte[] bytes = container(fixture(false));
        bytes[32 + 12] = (byte) 255;
        bytes[32 + 13] = (byte) 255;
        invalid(() -> load(bytes));
        byte[] target = container(fixture(false));
        target[32 + 2 * 16 + 2] = 10;
        invalid(() -> load(target));
        byte[] version = container(fixture(false));
        version[4] = 1;
        invalid(() -> load(version));
    }

    @Test
    public void fontEditsPreserveExternalReferences() throws IOException {
        Fixture f = fixture(false);
        MemoryDataStream s = new MemoryDataStream();
        s.writeBytes(f.data);
        s.pad8bytes();
        long font = s.position();
        s.writeBytes(new byte[352]);
        value(s, font, 0x0001FF16, 4);
        for (int offset : new int[]{16, 48, 56, 64, 96, 128}) {
            pointer(s, font + offset, 0);
            f.fields.add(new IggyFixup.Field(font + offset, 2));
        }
        //Opaque font header link points to another object, outside the replacement.
        pointer(s, font + 16, f.text);
        pointer(s, f.dictionary + 8, font);
        f.data = s.getAllBytes();
        IggyIndexBuilder index = new IggyIndexBuilder();
        IggyFixup.writeFields(index, f.fields, f.data.length);
        f.fixups = index.getIndexBytes();
        IggyFile file = load(container(f));
        Assert.assertEquals(file.getSwf().getFonts().size(), 1);
        file.getSwf().getFonts().get(0).setName("Renamed font");
        file.getSwf().getTexts().get(0).setInitialText("Changed text");
        file.updateFlashEntry();
        MemoryDataStream output = new MemoryDataStream();
        file.writeToDataStream(output);
        IggyFile reloaded = load(output.getAllBytes());
        Assert.assertEquals(reloaded.getSwf().getFonts().get(0).getName(), "Renamed font");
        Assert.assertEquals(reloaded.getSwf().getFonts().get(0).getFontId(), 1);
        long newFont = reloaded.getSwf().getDictionary().get(1).getAddress();
        MemoryDataStream payload = new MemoryDataStream(reloaded.getEntryData(1));
        payload.seek(newFont + 16, SeekMode.SET);
        Assert.assertEquals(newFont + 16 + payload.readUI64(), reloaded.getSwf().getDictionary().get(3).getAddress());
    }

    @Test
    public void platformNormalizationUsesFixups() throws IOException {
        byte[] bytes = new byte[]{1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 1, (byte) 0x80};
        IggyIndexBuilder index = new IggyIndexBuilder();
        IggyFixup.writeFields(index, Arrays.asList(new IggyFixup.Field(0, 5), new IggyFixup.Field(4, 2), new IggyFixup.Field(12, 3)), bytes.length);
        IggyFixup program = IggyFixup.parse(true, index.getIndexBytes(), bytes.length);
        program.normalize(bytes, false, false);
        Assert.assertEquals(Arrays.copyOfRange(bytes, 0, 4), new byte[]{4, 3, 2, 1});
        Assert.assertEquals(new MemoryDataStream(Arrays.copyOfRange(bytes, 4, 12)).readUI64(), 1L);
        Assert.assertEquals(bytes[12], (byte) 1);
    }

    @Test
    public void emptyTextureBlockIsAllowed() throws IOException {
        Assert.assertTrue(new IggyTextureMetadata(new byte[0]).getRecords().isEmpty());
        invalid(() -> new IggyTextureMetadata(new byte[]{0}));
        invalid(() -> new IggyTextureMetadata(new byte[]{0, 0}));
        byte[] truncated = new byte[16];
        truncated[0] = 1;
        invalid(() -> new IggyTextureMetadata(truncated));
        IggyFile file = load(container(fixture(false), true));
        Assert.assertTrue(file.getTextureMetadata().get(0).getRecords().isEmpty());
        MemoryDataStream output = new MemoryDataStream();
        file.writeToDataStream(output);
        IggyFile reloaded = load(output.getAllBytes());
        Assert.assertTrue(reloaded.getTextureMetadata().get(0).getRecords().isEmpty());
    }

    @Test
    public void removingAbcAndSavingRepeatedly() throws IOException {
        IggyFile file = load(container(fixture(true)));
        file.getSwf().getAbcBlocks().clear();
        file.saveChanges();
        IggyFile reloaded = new IggyFile(file.getOriginalFile());
        Assert.assertTrue(reloaded.getSwf().getAbcBlocks().isEmpty());
        Assert.assertNull(reloaded.getSwf().getDeclStrings());
        byte[] first = Files.readAllBytes(file.getOriginalFile().toPath());
        file.saveChanges();
        Assert.assertEquals(Files.readAllBytes(file.getOriginalFile().toPath()), first);
    }

    @Test
    public void moviePropertiesAndClassRenamesRoundTrip() throws IOException {
        IggyFile file = load(container(fixture(true)));
        SWF swf = IggyToSwfConvertor.getSwf(file);
        swf.frameRate = 30;
        for (Tag tag : swf.getTags()) {
            if (tag instanceof SymbolClassTag) {
                ((SymbolClassTag) tag).names.set(0, "RenamedMain");
            }
            if (tag instanceof SetBackgroundColorTag) {
                ((SetBackgroundColorTag) tag).backgroundColor.red = 127;
            }
        }
        SwfToIggyConvertor.updateIggy(file.getSwf(), swf);
        file.updateFlashEntry();
        MemoryDataStream output = new MemoryDataStream();
        file.writeToDataStream(output);
        IggyFile reloaded = load(output.getAllBytes());
        Assert.assertEquals(reloaded.getSwf().getHdr().getFrameRate(), 30f);
        Assert.assertEquals(reloaded.getSwf().getHdr().getBackgroundColor() & 255, 127L);
        Assert.assertEquals(reloaded.getSwf().getSymbolClasses().get(0).getName(), "RenamedMain");
    }
}

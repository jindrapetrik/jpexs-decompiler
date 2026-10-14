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

import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.IggyFixup;
import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class Iggy32FormatTest {

    private static byte[] abc(int minor) {
        return minor == 18
                ? new byte[]{18, 0, 46, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0}
                : new byte[]{16, 0, 46, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
    }

    private static void word(MemoryDataStream s, long position, long value) throws IOException {
        long saved = s.position();
        s.seek(position, SeekMode.SET);
        s.writeUI32(value);
        s.seek(saved, SeekMode.SET);
    }

    private static void pointer(MemoryDataStream s, long position, long target) throws IOException {
        word(s, position, target == 0 ? 1 : target - position);
    }

    private static long wide(MemoryDataStream s, String value) throws IOException {
        long result = s.position();
        s.writeWChar(value);
        s.pad8bytes();
        return result;
    }

    private static final class Fixture {

        byte[] data, program;
        long font, text, firstAbc;
        final List<IggyFixup.Field> fields = new ArrayList<>();

        void reference(long position) {
            fields.add(new IggyFixup.Field(position, 2));
        }
    }

    // The shipped files begin with this 128-byte layout. In particular +0x3c
    // is RGB, +0x40 is float FPS, and ABC has next@0/length@4/data@8.
    private static Fixture fixture() throws IOException {
        Fixture f = new Fixture();
        MemoryDataStream s = new MemoryDataStream();
        s.writeBytes(new byte[128]);
        for (int offset : new int[]{0, 4, 8, 12, 16, 20, 80, 84, 96, 100, 104, 108}) {
            pointer(s, offset, 0);
            f.reference(offset);
        }
        word(s, 0x18, -25);
        word(s, 0x20, 1280);
        word(s, 0x24, 720);
        word(s, 0x28, 3);
        word(s, 0x2c, 2);
        word(s, 0x34, 1);
        word(s, 0x38, 1);
        word(s, 0x3c, 0x00ff7f00);
        word(s, 0x40, Float.floatToIntBits(60));
        word(s, 0x70, 1);
        word(s, 0x78, 3);
        long dictionary = s.position();
        pointer(s, 0, dictionary);
        for (int i = 0; i < 3; i++) {
            f.reference(s.position());
            s.writeUI32(1);
        }
        f.font = s.position();
        s.writeBytes(new byte[216]);
        word(s, f.font, 0x0001ff16);
        for (int offset : new int[]{4, 8, 12, 28, 32, 36, 60, 68, 76, 84, 104}) {
            pointer(s, f.font + offset, 0);
            f.reference(f.font + offset);
        }
        word(s, f.font + 208, 0x11223344);
        word(s, f.font + 212, 0x55667788);
        long fontName = wide(s, "Font216");
        pointer(s, f.font + 76, fontName);
        pointer(s, dictionary + 4, f.font);
        f.text = s.position();
        s.writeBytes(new byte[68]);
        word(s, f.text, 0x0002ff06);
        for (int offset : new int[]{4, 8, 12, 36, 64}) {
            pointer(s, f.text + offset, 0);
            f.reference(f.text + offset);
        }
        long text = wide(s, "original text");
        pointer(s, f.text + 64, text);
        pointer(s, dictionary + 8, f.text);
        long show = s.position();
        s.writeUI32(1);
        s.writeUI32(1);
        f.reference(show);
        pointer(s, 8, show);
        pointer(s, 12, show);
        long labelName = wide(s, "label"), sceneName = wide(s, "scene");
        long label = s.position();
        s.writeBytes(new byte[8]);
        pointer(s, label, labelName);
        f.reference(label);
        word(s, label + 4, 1);
        long scene = s.position();
        s.writeBytes(new byte[8]);
        pointer(s, scene, sceneName);
        f.reference(scene);
        pointer(s, 16, label);
        pointer(s, 20, scene);
        long context = s.position();
        s.writeUI32(0);
        pointer(s, 4, context);
        long symbols = s.position();
        s.writeBytes(new byte[36]);
        pointer(s, 108, symbols);
        for (int id = 0; id < 3; id++) {
            long record = symbols + 12L * id;
            word(s, record, 0);
            word(s, record + 8, id);
            long name = wide(s, "Class" + id);
            pointer(s, record + 4, name);
            f.reference(record + 4);
        }
        f.firstAbc = s.position();
        s.writeUI32(1);
        s.writeUI32(abc(16).length);
        s.writeBytes(abc(16));
        f.reference(f.firstAbc);
        s.pad8bytes();
        long second = s.position();
        s.writeUI32(1);
        s.writeUI32(abc(18).length);
        s.writeBytes(abc(18));
        f.reference(second);
        pointer(s, f.firstAbc, second);
        pointer(s, 104, f.firstAbc);
        f.data = s.getAllBytes();
        IggyIndexBuilder builder = new IggyIndexBuilder();
        IggyFixup.writeFields(builder, f.fields, f.data.length);
        f.program = builder.getIndexBytes();
        return f;
    }

    private static byte[] container(Fixture fixture) throws IOException {
        MemoryDataStream s = new MemoryDataStream();
        new IggyHeader(0x900, 1, 32, 1, 0, 0, new byte[12], 2).writeToDataStream(s);
        s.writeUI32(1);
        s.writeUI32(fixture.data.length);
        s.writeUI32(fixture.data.length);
        s.writeUI32(64);
        s.writeUI32(0);
        s.writeUI32(fixture.program.length);
        s.writeUI32(fixture.program.length);
        s.writeUI32(64 + fixture.data.length);
        s.writeBytes(fixture.data);
        s.writeBytes(fixture.program);
        return s.getAllBytes();
    }

    private static IggyFile load(byte[] bytes) throws IOException {
        File file = File.createTempFile("iggy128-", ".iggy");
        file.deleteOnExit();
        Files.write(file.toPath(), bytes);
        return new IggyFile(file);
    }

    private static byte[] save(IggyFile file) throws IOException {
        file.updateFlashEntry();
        MemoryDataStream s = new MemoryDataStream();
        file.writeToDataStream(s);
        return s.getAllBytes();
    }

    @Test
    public void readsExtendedRootInsteadOfTreatingRgbAsSourceReference() throws Exception {
        IggyFile file = load(container(fixture()));
        IggySwf movie = file.getSwf();
        Assert.assertTrue(movie.getHdr() instanceof IggyFlashHeader32);
        Assert.assertEquals(movie.getHdr().getFrameRate(), 60f);
        Assert.assertEquals(movie.getHdr().getBackgroundColor(), 0x00ff7f00L);
        Assert.assertEquals(movie.getHdr().getXMin(), -25L);
        Assert.assertEquals(movie.getHdr().getXMax(), 1280L);
        Assert.assertEquals(movie.getHdr().getFrameCount(), 2L);
        Assert.assertEquals(movie.getSourceName(), "");
        Assert.assertEquals(movie.getFrameLabels().get(0).getName(), "label");
        Assert.assertEquals(movie.getScenes().get(0).getName(), "scene");
        Assert.assertEquals(movie.getFonts().get(0).getName(), "Font216");
        Assert.assertEquals(movie.getTexts().get(0).getInitialText(), "original text");
        Assert.assertEquals(movie.getSymbolClasses().get(2).getName(), "Class2");
        Assert.assertEquals(movie.getAbcBlocks().size(), 2);
        Assert.assertEquals(movie.getAbcBlocks().get(0).getData(), abc(16));
        Assert.assertEquals(movie.getAbcBlocks().get(1).getData(), abc(18));
    }

    @Test
    public void savesExtendedLayoutWithoutChangingAnyBytes() throws Exception {
        byte[] original = container(fixture());
        Assert.assertEquals(save(load(original)), original);
    }

    @Test
    public void editsExtendedMetadataFontsTextsClassesAndAbc() throws Exception {
        IggyFile file = load(container(fixture()));
        IggySwf movie = file.getSwf();
        movie.getFonts().get(0).setName("New font name");
        movie.getTexts().get(0).setInitialText("new text");
        movie.setSymbolClassName(2, "RenamedClass");
        movie.getAbcBlocks().get(0).setData(abc(18));
        movie.getAbcBlocks().add(new IggyDeclStrings(abc(16)));
        movie.getHdr().setMovieProperties(-50, -25, 800, 600, 25, 7, 0x123456);
        byte[] saved = save(file);
        IggyFile reopened = load(saved);
        IggySwf result = reopened.getSwf();
        Assert.assertEquals(result.getHdr().getFrameRate(), 25f);
        Assert.assertEquals(result.getHdr().getFrameCount(), 7L);
        Assert.assertEquals(result.getFonts().get(0).getName(), "New font name");
        Assert.assertEquals(result.getTexts().get(0).getInitialText(), "new text");
        Assert.assertEquals(result.getSymbolClasses().get(2).getName(), "RenamedClass");
        Assert.assertEquals(result.getAbcBlocks().size(), 3);
        Assert.assertEquals(result.getAbcBlocks().get(0).getData(), abc(18));
        Assert.assertEquals(result.getAbcBlocks().get(2).getData(), abc(16));
        MemoryDataStream data = new MemoryDataStream(result.getOriginalData());
        data.seek(result.getDictionary().get(1).getAddress() + 208, SeekMode.SET);
        Assert.assertEquals(data.readUI32(), 0x11223344L);
        Assert.assertEquals(data.readUI32(), 0x55667788L);
        Assert.assertEquals(save(reopened), saved);
    }

    @Test
    public void readsExtendedRootWithoutRelocationsButRefusesAnEdit() throws Exception {
        Fixture f = fixture();
        IggySwf movie = new IggySwf(new MemoryDataStream(f.data), new ArrayList<>(), false);
        Assert.assertTrue(movie.getHdr() instanceof IggyFlashHeader32);
        movie.getTexts().get(0).setInitialText("new text");
        try {
            MemoryDataStream out = new MemoryDataStream();
            movie.writeToDataStream(out);
            Assert.fail();
        } catch (IOException expected) {
            Assert.assertTrue(expected.getMessage().contains("relocation"));
        }
    }

    @Test
    public void rejectsTruncatedExtendedRoot() throws Exception {
        Fixture f = fixture();
        try {
            new IggySwf(new MemoryDataStream(Arrays.copyOf(f.data, 100)), f.fields, false);
            Assert.fail();
        } catch (IOException expected) {
        }
    }

    // Enable with -Dffdec.iggy32.samples=<directory>. No proprietary assets are
    // required in the repository: the synthetic regression cases always run.
    @DataProvider(name = "real32Samples")
    public Object[][] real32Samples() throws IOException {
        String directory = System.getProperty("ffdec.iggy32.samples");
        if (directory == null) {
            return new Object[0][];
        }
        List<Object[]> cases = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(directory))) {
            paths.filter(p -> p.toString().endsWith(".iggy")).sorted().forEach(p -> cases.add(new Object[]{p}));
        }
        Assert.assertFalse(cases.isEmpty(), "No Iggy samples found in " + directory);
        return cases.toArray(new Object[0][]);
    }

    @Test(dataProvider = "real32Samples")
    public void suppliedSampleLoadsConvertsSavesAndEdits(Path sample) throws Exception {
        IggyFile file = new IggyFile(sample.toFile());
        IggySwf movie = file.getSwf();
        Assert.assertTrue(movie.getHdr() instanceof IggyFlashHeader32);
        IggyToSwfConvertor.getSwf(file);
        Assert.assertEquals(save(file), Files.readAllBytes(sample));
        SwfToIggyConvertor.updateIggy(movie, IggyToSwfConvertor.getSwf(file));
        for (IggyFont font : movie.getFonts()) {
            font.setName("Renamed font " + font.getFontId());
        }
        for (IggyText text : movie.getTexts()) {
            text.setInitialText("Edited text " + text.getTextIndex());
        }
        for (IggySymbolClass symbol : movie.getSymbolClasses()) {
            movie.setSymbolClassName(symbol.getCharacterId(), "Class" + symbol.getCharacterId());
        }
        movie.getAbcBlocks().add(new IggyDeclStrings(abc(16)));
        byte[] saved = save(file);
        IggyFile reopened = load(saved);
        IggySwf result = reopened.getSwf();
        for (IggyFont font : result.getFonts()) {
            Assert.assertEquals(font.getName(), "Renamed font " + font.getFontId());
        }
        for (IggyText text : result.getTexts()) {
            Assert.assertEquals(text.getInitialText(), "Edited text " + text.getTextIndex());
        }
        for (IggySymbolClass symbol : result.getSymbolClasses()) {
            Assert.assertEquals(symbol.getName(), "Class" + symbol.getCharacterId());
        }
        Assert.assertEquals(result.getAbcBlocks().size(), movie.getAbcBlocks().size());
        for (int i = 0; i < result.getAbcBlocks().size(); i++) {
            Assert.assertEquals(result.getAbcBlocks().get(i).getData(), movie.getAbcBlocks().get(i).getData());
        }
        IggyToSwfConvertor.getSwf(reopened);
        Assert.assertEquals(save(reopened), saved);
    }
}

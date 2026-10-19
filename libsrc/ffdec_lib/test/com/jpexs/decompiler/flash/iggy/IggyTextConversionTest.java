/*
 * Copyright (C) 2010-2026 JPEXS, All rights reserved.
 * This library is free software under the GNU Lesser General Public License 3.0 or later.
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
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyTextConversionTest {

    @DataProvider(name = "layouts")
    public Object[][] layouts() {
        return new Object[][]{{true}, {false}};
    }

    private static void put(MemoryDataStream s, long at, long value, int width) throws Exception {
        long end = s.position();
        s.seek(at, SeekMode.SET);
        if (width == 8) {
            s.writeUI64(value);
        } else if (width == 4) {
            s.writeUI32(value);
        } else {
            s.writeUI16((int) value);
        }
        s.seek(end, SeekMode.SET);
    }

    private static void ref(MemoryDataStream s, long at, long target, boolean wide) throws Exception {
        put(s, at, target == 0 ? 1 : target - at, wide ? 8 : 4);
    }

    private static void name(MemoryDataStream s, long field, String text, boolean wide,
            List<IggyFixup.Field> fields) throws Exception {
        long start = s.position();
        ref(s, field, start, wide);
        s.writeWChar(text);
        for (int i = 0; i <= text.length(); i++) {
            fields.add(new IggyFixup.Field(start + i * 2, 4));
        }
        s.pad8bytes();
    }

    // Independent fixed offsets from the DLL, with nonzero opaque data and three distinct strings.
    private static IggyFile fixture(boolean wide) throws Exception {
        MemoryDataStream s = new MemoryDataStream();
        List<IggyFixup.Field> fields = new ArrayList<>();
        s.writeBytes(new byte[wide ? 184 : 64]);
        for (int at : wide ? new int[]{0, 8, 16, 24, 32, 40, 112, 120, 136, 144, 152, 160}
                : new int[]{0, 8, 12, 44, 56, 60}) {
            ref(s, at, 0, wide);
            fields.add(new IggyFixup.Field(at, 2));
        }
        put(s, wide ? 64 : 4, 2, 4);
        put(s, wide ? 68 : 32, 1, 4);
        if (wide) {
            put(s, 88, Float.floatToIntBits(24), 4);
        }
        long dictionary = s.position();
        ref(s, 0, dictionary, wide);
        for (int i = 0; i < 2; i++) {
            fields.add(new IggyFixup.Field(s.position(), 2));
            if (wide) {
                s.writeUI64(1);
            } else {
                s.writeUI32(1);
            }
        }
        long text = s.position();
        s.writeBytes(new byte[wide ? 104 : 68]);
        ref(s, dictionary + (wide ? 8 : 4), text, wide);
        put(s, text, 0x0001ff06, 4);
        put(s, text + (wide ? 8 : 4), 0x12345678, 4);
        if (wide) {
            put(s, text + 4, 0xabcdef, 4);
            put(s, text + 12, 0x87654321L, 4);
            put(s, text + 52, 0x10203040, 4);
        }
        int bounds = wide ? 32 : 16;
        float[] values = {-2.25f, 3.5f, 40.75f, 60.5f};
        for (int i = 0; i < 4; i++) {
            put(s, text + bounds + 4 * i, Float.floatToIntBits(values[i]), 4);
            fields.add(new IggyFixup.Field(text + bounds + 4 * i, 5));
        }
        int flags = wide ? 48 : 32;
        put(s, text + flags, 0xa955, 2);
        put(s, text + flags + 2, 23, 2); // Dictionary ID, not a font-list index.
        fields.add(new IggyFixup.Field(text + flags, 4));
        fields.add(new IggyFixup.Field(text + flags + 2, 4));
        int style = wide ? 64 : 40;
        s.seek(text + style, SeekMode.SET);
        s.writeBytes(new byte[]{17, 34, 51, (byte) 128});
        for (int value : new int[]{600, 125, 17, 31, 200, -120}) {
            fields.add(new IggyFixup.Field(s.position(), 4));
            s.writeUI16(value & 65535);
        }
        s.writeUI8(2);
        for (int i = 0; i < (wide ? 15 : 7); i++) {
            s.writeUI8(160 + i);
        }
        s.seek(text + (wide ? 104 : 68), SeekMode.SET);
        for (int at : wide ? new int[]{16, 56, 96} : new int[]{8, 36, 64}) {
            fields.add(new IggyFixup.Field(text + at, 2));
        }
        name(s, text + (wide ? 16 : 8), "ui.TextField", wide, fields);
        name(s, text + (wide ? 56 : 36), "fonts.BodyFont", wide, fields);
        name(s, text + (wide ? 96 : 64), "Příliš žluťoučký", wide, fields);
        IggyIndexBuilder builder = new IggyIndexBuilder();
        IggyFixup.writeFields(builder, fields, s.position());
        byte[][] blocks = {s.getAllBytes(), builder.getIndexBytes()};
        MemoryDataStream container = new MemoryDataStream();
        new IggyHeader(0x900, 1, wide ? 64 : 32, 1, 0, 0, new byte[12], 2).writeToDataStream(container);
        int offset = 64;
        for (int i = 0; i < 2; i++) {
            new IggySubFileEntry(i == 0 ? 0x101 : 0, blocks[i].length, 0, offset).writeToDataStream(container);
            offset += blocks[i].length;
        }
        for (byte[] block : blocks) {
            container.writeBytes(block);
        }
        return load(container.getAllBytes());
    }

    private static IggyFile load(byte[] data) throws Exception {
        Path path = Files.createTempFile("iggy-text-conversion", ".iggy");
        try {
            Files.write(path, data);
            return new IggyFile(path.toFile());
        } finally {
            Files.delete(path);
        }
    }

    private static byte[] save(IggyFile file) throws Exception {
        file.updateFlashEntry();
        MemoryDataStream out = new MemoryDataStream();
        file.writeToDataStream(out);
        return out.getAllBytes();
    }

    private static SWF swfRoundTrip(SWF swf) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        swf.saveTo(out);
        return new SWF(new ByteArrayInputStream(out.toByteArray()), false);
    }

    private static DefineEditTextTag text(SWF swf) {
        for (Tag tag : swf.getTags()) {
            if (tag instanceof DefineEditTextTag) {
                return (DefineEditTextTag) tag;
            }
        }
        throw new AssertionError("Missing edit text");
    }

    private static void assertFlags(DefineEditTextTag tag, int flags) {
        boolean[] actual = {tag.wordWrap, tag.multiline, tag.password, tag.readOnly, tag.autoSize,
            tag.noSelect, tag.border, tag.wasStatic, tag.html, tag.useOutlines};
        for (int i = 0; i < actual.length; i++) {
            Assert.assertEquals(actual[i], (flags & (1 << i)) != 0, "Flag " + i);
        }
    }

    @Test(dataProvider = "layouts")
    public void exportsEveryKnownPropertyAndPreservesUnchangedIggy(boolean wide) throws Exception {
        IggyFile file = fixture(wide);
        IggyText original = file.getSwf().getTexts().get(0);
        byte[] before = save(file);
        SWF swf = swfRoundTrip(IggyToSwfConvertor.getSwf(file));
        DefineEditTextTag tag = text(swf);
        Assert.assertEquals(tag.bounds.Xmin, -45);
        Assert.assertEquals(tag.bounds.Ymin, 70);
        Assert.assertEquals(tag.bounds.Xmax, 815);
        Assert.assertEquals(tag.bounds.Ymax, 1210);
        assertFlags(tag, 0xa955);
        Assert.assertTrue(tag.hasFont);
        Assert.assertEquals(tag.fontId, 23);
        Assert.assertTrue(tag.hasFontClass);
        Assert.assertEquals(tag.fontClass, "fonts.BodyFont");
        Assert.assertEquals(tag.fontHeight, 600);
        Assert.assertTrue(tag.hasTextColor);
        Assert.assertEquals(tag.textColor.red, 17);
        Assert.assertEquals(tag.textColor.green, 34);
        Assert.assertEquals(tag.textColor.blue, 51);
        Assert.assertEquals(tag.textColor.alpha, 128);
        Assert.assertTrue(tag.hasMaxLength);
        Assert.assertEquals(tag.maxLength, 125);
        Assert.assertTrue(tag.hasLayout);
        Assert.assertEquals(tag.align, 2);
        Assert.assertEquals(tag.leftMargin, 17);
        Assert.assertEquals(tag.rightMargin, 31);
        Assert.assertEquals(tag.indent, 200);
        Assert.assertEquals(tag.leading, -120);
        Assert.assertEquals(tag.initialText, "Příliš žluťoučký");
        Assert.assertEquals(tag.variableName, "");
        boolean symbolFound = false;
        for (Tag t : swf.getTags()) {
            if (t instanceof SymbolClassTag) {
                SymbolClassTag symbol = (SymbolClassTag) t;
                Assert.assertEquals(symbol.names.get(symbol.tags.indexOf(1)), "ui.TextField");
                symbolFound = true;
            }
        }
        Assert.assertTrue(symbolFound);
        SwfToIggyConvertor.updateIggy(file.getSwf(), swf);
        Assert.assertEquals(save(file), before);
        Assert.assertEquals(original.getUnknownHeaderWord(), 0x12345678L);
    }

    @Test(dataProvider = "layouts")
    public void editsSurviveContainerRelocationAndClassNamesCanBeCleared(boolean wide) throws Exception {
        IggyFile file = fixture(wide);
        IggyText original = file.getSwf().getTexts().get(0);
        byte[] opaque = original.getUnknownLayoutData();
        SWF swf = IggyToSwfConvertor.getSwf(file);
        DefineEditTextTag tag = text(swf);
        tag.bounds = new RECT(-151, 1023, -53, 721);
        tag.wordWrap = false;
        tag.multiline = true;
        tag.password = false;
        tag.readOnly = true;
        tag.autoSize = false;
        tag.noSelect = true;
        tag.border = false;
        tag.wasStatic = true;
        tag.html = false;
        tag.useOutlines = true;
        tag.fontId = 42;
        tag.fontClass = "fonts.AMuchLongerReplacementFont";
        tag.fontHeight = 999;
        tag.textColor = new RGBA(250, 130, 70, 5);
        tag.maxLength = 65535;
        tag.align = 3;
        tag.leftMargin = 123;
        tag.rightMargin = 456;
        tag.indent = 789;
        tag.leading = -32768;
        tag.initialText = "Changed text with a much longer UTF-16 payload \uD83D\uDE00";
        for (Tag t : swf.getTags()) {
            if (t instanceof SymbolClassTag) {
                ((SymbolClassTag) t).names.set(0, "ui.AMuchLongerReplacementTextField");
            }
        }
        SwfToIggyConvertor.updateIggy(file.getSwf(), swfRoundTrip(swf));
        byte[] saved = save(file);
        IggyFile loaded = load(saved);
        IggyText edited = loaded.getSwf().getTexts().get(0);
        Assert.assertEquals(edited.getEnum_hex(), 0xaaaa);
        Assert.assertEquals(edited.getFontIndex(), 42);
        Assert.assertEquals(edited.getFontClassName(), tag.fontClass);
        Assert.assertEquals(edited.getSymbolClassName(), "ui.AMuchLongerReplacementTextField");
        Assert.assertEquals(edited.getTextColorRgba(), new byte[]{(byte) 250, (byte) 130, 70, 5});
        Assert.assertEquals(edited.getFontHeight(), 999);
        Assert.assertEquals(edited.getMaxLength(), 65535);
        Assert.assertEquals(edited.getAlign(), 3);
        Assert.assertEquals(edited.getLeftMargin(), 123);
        Assert.assertEquals(edited.getRightMargin(), 456);
        Assert.assertEquals(edited.getIndent(), 789);
        Assert.assertEquals(edited.getLeading(), -32768);
        Assert.assertEquals(edited.getInitialText(), tag.initialText);
        Assert.assertEquals(edited.getUnknownLayoutData(), opaque);
        Assert.assertEquals(edited.getUnknownHeaderWord(), original.getUnknownHeaderWord());
        Assert.assertEquals(edited.getCommonHeaderAlignmentPadding(), original.getCommonHeaderAlignmentPadding());
        Assert.assertEquals(edited.getUnknownHeaderUpperWord(), original.getUnknownHeaderUpperWord());
        Assert.assertEquals(edited.getRuntimeClassPointer(), original.getRuntimeClassPointer());
        Assert.assertEquals(edited.getFontClassNameAlignmentPadding(), original.getFontClassNameAlignmentPadding());
        DefineEditTextTag again = text(swfRoundTrip(IggyToSwfConvertor.getSwf(loaded)));
        Assert.assertEquals(again.bounds.Xmin, -151);
        Assert.assertEquals(again.bounds.Ymin, -53);
        Assert.assertEquals(again.bounds.Xmax, 1023);
        Assert.assertEquals(again.bounds.Ymax, 721);
        assertFlags(again, 0x2aa);
        Assert.assertEquals(save(loaded), saved);

        again.hasText = false;
        again.hasFont = false;
        again.hasFontClass = false;
        again.hasTextColor = false;
        again.hasMaxLength = false;
        again.hasLayout = false;
        SwfToIggyConvertor.updateIggyText(edited, again);
        edited.setSymbolClassName(null);
        IggyText cleared = load(save(loaded)).getSwf().getTexts().get(0);
        Assert.assertNull(cleared.getInitialText());
        Assert.assertNull(cleared.getFontClassName());
        Assert.assertNull(cleared.getSymbolClassName());
        Assert.assertEquals(cleared.getFontIndex(), 0);
        Assert.assertEquals(cleared.getFontHeight(), 999); // Latent height absent from SWF is preserved.
        Assert.assertEquals(cleared.getMaxLength(), 0);
        Assert.assertEquals(cleared.getTextColorRgba(), new byte[]{-1, -1, -1, -1});
        Assert.assertEquals(cleared.getAlign(), 0);
        Assert.assertEquals(cleared.getLeftMargin(), 0);
        Assert.assertEquals(cleared.getRightMargin(), 0);
        Assert.assertEquals(cleared.getIndent(), 0);
        Assert.assertEquals(cleared.getLeading(), 0);
        Assert.assertEquals(cleared.getUnknownLayoutData(), opaque);
    }

    @Test(dataProvider = "layouts")
    public void unchangedClassReferencesSurviveMovingText(boolean wide) throws Exception {
        IggyFile file = fixture(wide);
        IggyText text = file.getSwf().getTexts().get(0);
        text.setInitialText("A longer text moves the object while its original class strings stay in place");
        IggyFile loaded = load(save(file));
        IggyText moved = loaded.getSwf().getTexts().get(0);
        Assert.assertEquals(moved.getSymbolClassName(), "ui.TextField");
        Assert.assertEquals(moved.getFontClassName(), "fonts.BodyFont");
        Assert.assertEquals(moved.getInitialText(), text.getInitialText());
    }

    @Test(dataProvider = "layouts")
    public void eachBehaviorBitAndFontClassWithoutFontId(boolean wide) throws Exception {
        IggyFile file = fixture(wide);
        IggyText source = file.getSwf().getTexts().get(0);
        source.setFontIndex(0);
        for (int bit = 0; bit < 10; bit++) {
            source.setEnum_hex(0xc000 | (1 << bit));
            DefineEditTextTag tag = IggyToSwfConvertor.convertText(new SWF(), source);
            assertFlags(tag, 1 << bit);
            Assert.assertFalse(tag.hasFont);
            Assert.assertTrue(tag.hasFontClass);
            SwfToIggyConvertor.updateIggyText(source, tag);
            Assert.assertEquals(source.getEnum_hex(), 0xc000 | (1 << bit));
            Assert.assertEquals(source.getFontHeight(), 600);
        }
        DefineEditTextTag tag = text(swfRoundTrip(IggyToSwfConvertor.getSwf(file)));
        Assert.assertFalse(tag.hasFont);
        Assert.assertTrue(tag.hasFontClass);
        Assert.assertEquals(tag.fontHeight, 600);
        Assert.assertEquals(tag.fontClass, "fonts.BodyFont");
    }
}

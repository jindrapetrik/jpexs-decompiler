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

import com.jpexs.decompiler.flash.iggy.streams.IggyIndexBuilder;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.iggy.streams.SeekMode;
import java.io.IOException;
import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyTextTest {

    @DataProvider(name = "pointerWidths")
    public Object[][] pointerWidths() {
        return new Object[][]{{true}, {false}};
    }

    private static void word(MemoryDataStream s, long offset, long value, boolean wide) throws IOException {
        long saved = s.position();
        s.seek(offset, SeekMode.SET);
        if (wide) {
            s.writeUI64(value);
        } else {
            s.writeUI32(value);
        }
        s.seek(saved, SeekMode.SET);
    }

    // Fixed offsets independently follow sub_18000EDA0 / sub_1000CB90.
    private static byte[] fixture(boolean is64) throws IOException {
        MemoryDataStream s = new MemoryDataStream();
        s.writeBytes(new byte[is64 ? 104 : 68]);
        word(s, 0, 0x0007ff06, false);
        if (is64) {
            word(s, 4, 0x12345678, false);
            word(s, 8, 0x89abcdefL, false);
            word(s, 12, 0x76543210, false);
            word(s, 16, -16, true);
            word(s, 24, 0x1020304050607080L, true);
            word(s, 52, 0x13572468, false);
            word(s, 56, -24, true);
        } else {
            // A backward reference must still point inside the block (8 - 8 = 0).
            word(s, 4, 0x89abcdefL, false);
            word(s, 8, 0xfffffff8L, false);
            word(s, 12, 0x50607080, false);
            word(s, 36, 0xffffffe8L, false);
        }
        int style = is64 ? 64 : 40;
        s.seek(style, SeekMode.SET);
        s.writeBytes(new byte[]{0x11, 0x22, 0x33, (byte) 0x80});
        s.writeUI16(600);
        s.writeUI16(65535);
        s.writeUI16(17);
        s.writeUI16(31);
        s.writeUI16(200);
        s.writeSI16(-120);
        s.writeUI8(2);
        for (int i = 0; i < (is64 ? 15 : 7); i++) {
            s.writeUI8(0xa0 + i);
        }
        word(s, is64 ? 96 : 64, 1, is64);
        return s.getAllBytes();
    }

    private static IggyText read(byte[] data, boolean is64) throws IOException {
        MemoryDataStream stream = new MemoryDataStream(data);
        return is64 ? new IggyText(stream) : new IggyText32(stream);
    }

    private static byte[] write(IggyText text) throws IOException {
        MemoryDataStream out = new MemoryDataStream();
        out.setIndexing(new IggyIndexBuilder());
        text.writeToDataStream(out);
        return out.getAllBytes();
    }

    @Test(dataProvider = "pointerWidths")
    public void decodesCommonHeaderAndStyle(boolean is64) throws Exception {
        IggyText text = read(fixture(is64), is64);
        Assert.assertEquals(text.getUnknownHeaderWord(), 0x89abcdefL);
        Assert.assertEquals(text.getCommonHeaderAlignmentPadding(), is64 ? 0x12345678L : 0L);
        Assert.assertEquals(text.getUnknownHeaderUpperWord(), is64 ? 0x76543210L : 0L);
        Assert.assertEquals(text.getSymbolClassNameOffset(), is64 ? -16L : 0xfffffff8L);
        Assert.assertEquals(text.getRuntimeClassPointer(), is64 ? 0x1020304050607080L : 0x50607080L);
        Assert.assertEquals(text.getFontClassNameAlignmentPadding(), is64 ? 0x13572468L : 0L);
        Assert.assertEquals(text.getFontClassNameOffset(), is64 ? -24L : 0xffffffe8L);
        Assert.assertEquals(text.getTextColorRgba(), new byte[]{0x11, 0x22, 0x33, (byte) 0x80});
        Assert.assertEquals(text.getFontHeight(), 600);
        Assert.assertEquals(text.getMaxLength(), 65535);
        Assert.assertEquals(text.getLeftMargin(), 17);
        Assert.assertEquals(text.getRightMargin(), 31);
        Assert.assertEquals(text.getIndent(), 200);
        Assert.assertEquals(text.getLeading(), -120);
        Assert.assertEquals(text.getAlign(), 2);
        Assert.assertEquals(text.getUnknownLayoutData().length, is64 ? 15 : 7);
    }

    @Test(dataProvider = "pointerWidths")
    public void preservesNonzeroPaddingRuntimeSlotsAndUnknownBytes(boolean is64) throws Exception {
        byte[] original = fixture(is64);
        IggyText text = read(original, is64);
        Assert.assertEquals(write(text), original);
        text.setInitialText("Changed text");
        IggyText reloaded = read(write(text), is64);
        Assert.assertEquals(reloaded.getUnknownHeaderWord(), text.getUnknownHeaderWord());
        Assert.assertEquals(reloaded.getSymbolClassNameOffset(), text.getSymbolClassNameOffset());
        Assert.assertEquals(reloaded.getRuntimeClassPointer(), text.getRuntimeClassPointer());
        Assert.assertEquals(reloaded.getUnknownLayoutData(), text.getUnknownLayoutData());
        Assert.assertEquals(reloaded.getLeading(), -120);
        Assert.assertEquals(reloaded.getInitialText(), "Changed text");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void legacyConstructorAndAccessorsRetainRawData() throws Exception {
        byte[] original = fixture(true);
        IggyText parsed = read(original, true);
        byte[] common = Arrays.copyOfRange(original, 4, 32), style = Arrays.copyOfRange(original, 64, 96);
        Assert.assertEquals(parsed.getZeroone(), common);
        Assert.assertEquals(parsed.getSome(), style);
        IggyText rebuilt = new IggyText(parsed.getType(), parsed.getTextIndex(), common,
                parsed.getPar1(), parsed.getPar2(), parsed.getPar3(), parsed.getPar4(),
                parsed.getEnum_hex(), parsed.getFontIndex(), parsed.getZero(), parsed.getOne(), style, 1, null);
        Assert.assertEquals(write(rebuilt), original);
        Assert.assertEquals(rebuilt.getLeading(), -120);
    }
}

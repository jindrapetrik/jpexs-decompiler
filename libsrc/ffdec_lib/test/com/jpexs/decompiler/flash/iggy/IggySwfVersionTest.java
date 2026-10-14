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
import com.jpexs.decompiler.flash.iggy.conversion.IggySwfBundle;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.EndTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.types.RECT;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggySwfVersionTest {

    @DataProvider(name = "versions")
    public Object[][] versions() {
        List<Object[]> result = new ArrayList<>();
        for (IggyFile.Variant variant : IggyFile.Variant.values()) {
            for (int version : new int[]{3, 9, 12, 43, 255}) {
                result.add(new Object[]{variant, version});
            }
        }
        return result.toArray(new Object[0][]);
    }

    @DataProvider(name = "preservedWords")
    public Object[][] preservedWords() {
        List<Object[]> result = new ArrayList<>();
        for (IggyFile.Variant variant : IggyFile.Variant.values()) {
            for (long version : new long[]{0, 1, 2, 12, 256, 0xffffffffL}) {
                result.add(new Object[]{variant, version});
            }
        }
        return result.toArray(new Object[0][]);
    }

    @DataProvider(name = "variants")
    public Object[][] variants() {
        return new Object[][]{{IggyFile.Variant.X86_LEGACY}, {IggyFile.Variant.X86}, {IggyFile.Variant.X64}};
    }

    private static SWF movie(int version) {
        SWF swf = new SWF();
        swf.version = version;
        swf.frameCount = 1;
        swf.frameRate = 24;
        swf.displayRect = new RECT(0, 10000, 0, 8000);
        swf.addTag(new ShowFrameTag(swf));
        swf.addTag(new EndTag(swf));
        return swf;
    }

    private static byte[] bytes(IggyFile file) throws Exception {
        file.updateFlashEntry();
        MemoryDataStream out = new MemoryDataStream();
        file.writeToDataStream(out);
        return out.getAllBytes();
    }

    private static byte[] bytes(SWF swf) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        swf.saveTo(out);
        return out.toByteArray();
    }

    private static long word(byte[] bytes, int offset) {
        return (bytes[offset] & 255L) | (bytes[offset + 1] & 255L) << 8
                | (bytes[offset + 2] & 255L) << 16 | (bytes[offset + 3] & 255L) << 24;
    }

    @Test(dataProvider = "versions")
    public void conversionPreservesSerializedVersion(IggyFile.Variant variant, int version) throws Exception {
        SWF source = movie(version);
        byte[] original = bytes(source);
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "version.swf");
        byte[] container = bytes(file);
        Assert.assertEquals(word(container, 12), (long) version);
        Assert.assertEquals(word(container, 4), 0x900L);
        IggyFile loaded = new IggyFile(new MemoryDataStream(container));
        Assert.assertEquals(loaded.getHeader().getSwfVersion(), (long) version);
        SWF exported = IggyToSwfConvertor.getSwf(loaded);
        Assert.assertEquals(bytes(exported)[3] & 255, version);
        Assert.assertEquals(new SWF(new ByteArrayInputStream(bytes(exported)), false, "UTF-8").version, version);
        Assert.assertEquals(bytes(source), original, "Conversion must not mutate the source SWF");
    }

    @Test(dataProvider = "preservedWords")
    @SuppressWarnings("deprecation")
    public void unknownVersionsHaveFallbackWithoutRewritingHeader(IggyFile.Variant variant, long version) throws Exception {
        IggyFile file = SwfToIggyConvertor.createIggy(movie(12), variant, "version.swf");
        file.getHeader().setSwfVersion(version);
        byte[] original = bytes(file);
        IggyFile loaded = new IggyFile(new MemoryDataStream(original));
        Assert.assertEquals(loaded.getHeader().getSwfVersion(), version);
        Assert.assertEquals(loaded.getHeader().getUnk_0C(), version, "Old public getter remains compatible");
        SWF exported = IggyToSwfConvertor.getSwf(loaded);
        Assert.assertEquals(exported.version, version >= 3 && version <= 255 ? (int) version : 10);
        Assert.assertEquals(bytes(loaded), original, "Export must not rewrite the stored header word");
        SwfToIggyConvertor.updateIggy(loaded, exported);
        Assert.assertEquals(bytes(loaded), original, "An unchanged fallback version must preserve the original container");
        exported.version = 20;
        SwfToIggyConvertor.updateIggy(loaded, exported);
        IggyFile updated = new IggyFile(new MemoryDataStream(bytes(loaded)));
        Assert.assertEquals(updated.getHeader().getSwfVersion(), 20L);
        Assert.assertEquals(bytes(IggyToSwfConvertor.getSwf(updated))[3] & 255, 20);
    }

    @Test(dataProvider = "variants")
    public void bundleSaveTransfersExplicitVersionChange(IggyFile.Variant variant) throws Exception {
        Path path = Files.createTempFile("iggy-version", ".iggy");
        try {
            IggyFile file = SwfToIggyConvertor.createIggy(movie(12), variant, "version.swf");
            file.getHeader().setSwfVersion(0);
            file.saveTo(path.toFile());
            byte[] original = Files.readAllBytes(path);
            IggySwfBundle bundle = new IggySwfBundle(path.toFile());
            String key = bundle.getKeys().iterator().next();
            SWF exported;
            try (java.io.InputStream input = bundle.getOpenable(key)) {
                exported = new SWF(input, false, "UTF-8");
            }
            Assert.assertEquals(exported.version, 10);
            Assert.assertTrue(bundle.putOpenable(key, new ByteArrayInputStream(bytes(exported))));
            Assert.assertEquals(Files.readAllBytes(path), original);
            exported.version = 18;
            Assert.assertTrue(bundle.putOpenable(key, new ByteArrayInputStream(bytes(exported))));
            Assert.assertEquals(new IggyFile(path.toFile()).getHeader().getSwfVersion(), 18L);
            try (java.io.InputStream input = bundle.getOpenable(key)) {
                Assert.assertEquals(new SWF(input, false, "UTF-8").version, 18);
            }
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test(dataProvider = "variants")
    public void warningConversionAlsoKeepsSourceVersion(IggyFile.Variant variant) throws Exception {
        SWF source = movie(15);
        source.addTag(0, new DoActionTag(source));
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "version.swf", warnings::add);
        Assert.assertFalse(warnings.isEmpty());
        Assert.assertEquals(word(bytes(file), 12), 15L);
        Assert.assertEquals(IggyToSwfConvertor.getSwf(file).version, 15);
    }
}

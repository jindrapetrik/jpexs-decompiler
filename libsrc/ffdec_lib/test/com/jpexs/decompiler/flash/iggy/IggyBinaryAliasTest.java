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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.iggy;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.iggy.conversion.IggyToSwfConvertor;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineShape4Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.ImportAssets2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.helpers.ByteArrayRange;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyBinaryAliasTest {

    @DataProvider(name = "aliases")
    public Object[][] aliases() {
        List<Object[]> result = new ArrayList<>();
        for (IggyFile.Variant variant : new IggyFile.Variant[]{IggyFile.Variant.X86, IggyFile.Variant.X64}) {
            for (boolean separate : new boolean[]{false, true}) {
                for (boolean empty : new boolean[]{false, true}) {
                    result.add(new Object[]{variant, separate, empty});
                }
            }
        }
        return result.toArray(new Object[0][]);
    }

    private SWF source(boolean separate, boolean empty) {
        SWF swf = new SWF();
        swf.version = 10;
        swf.displayRect = new RECT(0, 10000, 0, 8000);
        swf.frameRate = 24;
        swf.frameCount = 1;
        DefineBinaryDataTag binary = new DefineBinaryDataTag(swf);
        binary.tag = 5;
        binary.binaryData = new ByteArrayRange(empty ? new byte[0] : new byte[]{0, (byte) 255, 42, 0, 7});
        binary.setModified(true);
        swf.addTag(binary);
        DefineBinaryDataTag unrelated = new DefineBinaryDataTag(swf);
        unrelated.tag = 1;
        unrelated.binaryData = new ByteArrayRange(new byte[]{9});
        swf.addTag(unrelated);
        ImportAssets2Tag imports = new ImportAssets2Tag(swf);
        imports.url = "external.swf";
        imports.tags.add(2);
        imports.names.add("External");
        swf.addTag(imports);
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        sprite.spriteId = 3;
        sprite.frameCount = 1;
        sprite.addTag(new ShowFrameTag(swf));
        swf.addTag(sprite);
        SymbolClassTag symbols = new SymbolClassTag(swf);
        symbols.tags.add(5);
        symbols.names.add("BlobA");
        swf.addTag(symbols);
        if (separate) {
            symbols = new SymbolClassTag(swf);
            swf.addTag(symbols);
        }
        symbols.tags.addAll(Arrays.asList(5, 5));
        symbols.names.addAll(Arrays.asList("BlobB", "BlobC"));
        symbols.setModified(true);
        swf.addTag(new ShowFrameTag(swf));
        return swf;
    }

    private byte[] save(SWF swf) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        swf.saveTo(bytes);
        return bytes.toByteArray();
    }

    private byte[] save(IggyFile file) throws Exception {
        MemoryDataStream bytes = new MemoryDataStream();
        file.writeToDataStream(bytes);
        return bytes.getAllBytes();
    }

    private Map<String, Integer> checkAliases(IggyFile file, boolean empty) throws Exception {
        SWF swf = IggyToSwfConvertor.getSwf(file);
        Map<String, Integer> names = new HashMap<>();
        Map<Integer, DefineBinaryDataTag> binaries = new HashMap<>();
        for (Tag tag : swf.getTags()) {
            if (tag instanceof DefineBinaryDataTag) {
                DefineBinaryDataTag binary = (DefineBinaryDataTag) tag;
                binaries.put(binary.tag, binary);
            }
            if (tag instanceof SymbolClassTag) {
                SymbolClassTag symbols = (SymbolClassTag) tag;
                for (int i = 0; i < symbols.tags.size(); i++) {
                    names.put(symbols.names.get(i), symbols.tags.get(i));
                }
            }
        }
        Assert.assertEquals(names.size(), 3);
        Assert.assertEquals(new HashSet<>(names.values()).size(), 3);
        Assert.assertEquals((int) names.get("BlobA"), 5);
        Assert.assertEquals(binaries.size(), 4);
        for (int id : names.values()) {
            Assert.assertFalse(Arrays.asList(1, 2, 3).contains(id), "Definition and import IDs are reserved");
            Assert.assertEquals(binaries.get(id).binaryData.getRangeData(),
                    empty ? new byte[0] : new byte[]{0, (byte) 255, 42, 0, 7});
        }
        Assert.assertEquals(binaries.get(1).binaryData.getRangeData(), new byte[]{9});
        return names;
    }

    @Test(dataProvider = "aliases")
    public void preservesAliases(IggyFile.Variant variant, boolean separate, boolean empty) throws Exception {
        SWF source = source(separate, empty);
        byte[] before = save(source);
        IggyFile file = SwfToIggyConvertor.createIggy(source, variant, "aliases");
        Map<String, Integer> names = checkAliases(file, empty);
        Assert.assertEquals(save(source), before, "Strict conversion must not mutate the source SWF");
        SwfToIggyConvertor.updateIggy(file, source);
        file = new IggyFile(new MemoryDataStream(save(file)));
        Assert.assertEquals(checkAliases(file, empty), names, "Updates reuse alias IDs");
        Assert.assertEquals(save(source), before);
        List<String> warnings = new ArrayList<>();
        IggyFile tolerant = SwfToIggyConvertor.createIggy(source, variant, "aliases", warnings::add);
        Assert.assertTrue(warnings.isEmpty(), "Binary aliases must not be skipped");
        Assert.assertEquals(checkAliases(tolerant, empty), names);
        Assert.assertEquals(save(source), before, "Warning-mode conversion must not mutate the source SWF");
        SWF exported = IggyToSwfConvertor.getSwf(file);
        byte[] stable = save(file);
        SwfToIggyConvertor.updateIggy(file, exported);
        Assert.assertEquals(save(file), stable, "An exported movie remains stable on unchanged update");
    }

    @Test(expectedExceptions = IOException.class)
    public void rejectsDuplicateClassName() throws Exception {
        SWF source = source(false, false);
        for (Tag tag : source.getTags()) {
            if (tag instanceof SymbolClassTag) {
                ((SymbolClassTag) tag).names.set(1, "BlobA");
            }
        }
        SwfToIggyConvertor.createIggy(source, IggyFile.Variant.X64, "invalid");
    }

    @Test
    public void warningRetryKeepsAliases() throws Exception {
        SWF source = source(true, false);
        DefineShape4Tag winding = new DefineShape4Tag(source);
        winding.shapeId = 320;
        winding.usesFillWindingRule = true;
        source.addTag(0, winding);
        byte[] before = save(source);
        List<String> warnings = new ArrayList<>();
        IggyFile file = SwfToIggyConvertor.createIggy(source, IggyFile.Variant.X64, "warnings", warnings::add);
        checkAliases(file, false);
        Assert.assertEquals(warnings.size(), 1);
        Assert.assertTrue(warnings.get(0).contains("nonzero fill winding"));
        Assert.assertEquals(save(source), before);
    }
}

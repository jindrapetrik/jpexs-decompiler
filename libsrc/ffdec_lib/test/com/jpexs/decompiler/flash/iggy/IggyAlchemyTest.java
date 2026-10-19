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
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.iggy.conversion.SwfToIggyConvertor;
import com.jpexs.decompiler.flash.iggy.streams.MemoryDataStream;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DoABC2Tag;
import com.jpexs.decompiler.flash.tags.DoABCTag;
import com.jpexs.decompiler.flash.tags.DoActionTag;
import com.jpexs.decompiler.flash.tags.FileAttributesTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IggyAlchemyTest {

    private interface Conversion {
        void run() throws IOException;
    }

    @DataProvider(name = "alchemy")
    public Object[][] alchemy() {
        List<Object[]> result = new ArrayList<>();
        for (IggyFile.Variant variant : IggyFile.Variant.values()) {
            for (int opcode : new int[]{0x0a, 0x0b, 0x35, 0x36, 0x37, 0x38, 0x39,
                0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x50, 0x51, 0x52}) {
                result.add(new Object[]{variant, opcode});
            }
        }
        return result.toArray(new Object[0][]);
    }

    @DataProvider(name = "variants")
    public Object[][] variants() {
        return new Object[][]{{IggyFile.Variant.X86_LEGACY}, {IggyFile.Variant.X86}, {IggyFile.Variant.X64}};
    }

    @DataProvider(name = "modernVariants")
    public Object[][] modernVariants() {
        return new Object[][]{{IggyFile.Variant.X86}, {IggyFile.Variant.X64}};
    }

    private static SWF movie() {
        SWF swf = new SWF();
        swf.version = 15;
        swf.frameRate = 24;
        swf.frameCount = 1;
        FileAttributesTag attributes = new FileAttributesTag(swf);
        swf.addTag(attributes);
        swf.addTag(new ShowFrameTag(swf));
        return swf;
    }

    private static void method(ABC abc, byte[] code) {
        MethodBody body = new MethodBody();
        body.method_info = abc.method_info.size();
        body.max_stack = 8;
        body.max_regs = 1;
        body.setCodeBytes(code);
        abc.method_info.add(new MethodInfo());
        abc.bodies.add(body);
    }

    private static byte[] bytes(SWF swf) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        swf.saveTo(output);
        return output.toByteArray();
    }

    private static byte[] bytes(IggyFile file) throws IOException {
        MemoryDataStream output = new MemoryDataStream();
        file.updateFlashEntry();
        file.writeToDataStream(output);
        return output.getAllBytes();
    }

    private static void rejects(Conversion conversion, String instruction) throws IOException {
        try {
            conversion.run();
            Assert.fail("Conversion must reject " + instruction);
        } catch (IOException ex) {
            Assert.assertEquals(ex.getClass(), IOException.class, "Not a skippable unsupported-tag error");
            Assert.assertTrue(ex.getMessage().contains("Alchemy instruction " + instruction), ex.getMessage());
            Assert.assertTrue(ex.getMessage().contains("method 1, offset 1"), ex.getMessage());
        }
    }

    @Test(dataProvider = "alchemy")
    public void rejectsEveryAlchemyInstructionBeforeMutation(IggyFile.Variant variant, int opcode) throws Exception {
        String instruction = AVM2Code.instructionSet[opcode].instructionName;
        for (boolean oldTag : new boolean[]{false, true}) {
            for (boolean nested : new boolean[]{false, true}) {
                SWF swf = movie();
                ((FileAttributesTag) swf.getTags().get(0)).actionScript3 = true;
                DoABC2Tag first = new DoABC2Tag(swf);
                method(first.getABC(), new byte[]{0x47});
                swf.addTag(first);
                Tag tag = oldTag ? new DoABCTag(swf) : new DoABC2Tag(swf);
                ABC abc = ((ABCContainerTag) tag).getABC();
                method(abc, new byte[]{0x47});
                // Unreferenced method and an instruction after returnvoid:
                // rejection must not depend on reachability or script traits.
                method(abc, new byte[]{0x47, (byte) opcode, 0x47});
                if (nested) {
                    DefineSpriteTag sprite = new DefineSpriteTag(swf);
                    sprite.spriteId = 1;
                    sprite.frameCount = 1;
                    sprite.addTag(tag);
                    sprite.addTag(new ShowFrameTag(swf));
                    swf.addTag(sprite);
                } else {
                    swf.addTag(tag);
                }
                byte[] original = bytes(swf);
                rejects(() -> SwfToIggyConvertor.createIggy(swf, variant, "alchemy.swf"), instruction);
                IggyFile file = SwfToIggyConvertor.createIggy(movie(), variant, "original.swf");
                byte[] container = bytes(file);
                swf.frameRate = 30;
                rejects(() -> SwfToIggyConvertor.updateIggy(file, swf), instruction);
                rejects(() -> SwfToIggyConvertor.updateIggy(file.getSwf(), swf), instruction);
                Assert.assertEquals(bytes(file), container, "Rejected updates must not change the target");
                swf.frameRate = 24;
                Assert.assertEquals(bytes(swf), original, "Validation must not normalize source bytecode");
                swf.addTag(new DoActionTag(swf));
                original = bytes(swf);
                List<String> warnings = new ArrayList<>();
                rejects(() -> SwfToIggyConvertor.createIggy(swf, variant, "alchemy.swf", warnings::add), instruction);
                Assert.assertTrue(warnings.isEmpty(), "Fatal ABC validation precedes warning retries");
                Assert.assertEquals(bytes(swf), original);
            }
        }
    }

    @Test(dataProvider = "modernVariants")
    public void allowsOrdinaryCodeAndOpcodeValuesInOperands(IggyFile.Variant variant) throws Exception {
        SWF swf = movie();
        ((FileAttributesTag) swf.getTags().get(0)).actionScript3 = true;
        DoABC2Tag tag = new DoABC2Tag(swf);
        ABC abc = tag.getABC();
        abc.constants.getStringId("Alchemy li8 domainMemory", true);
        byte[] code = {0x24, 0x35, 0x29, 0x24, 0x36, 0x29, 0x24, 0x50, 0x29,
            0x24, 0, 0x24, 0, 0x13, 0, 0, 0, 0x47};
        method(abc, code);
        swf.addTag(tag);
        byte[] original = bytes(swf);
        IggyFile file = SwfToIggyConvertor.createIggy(swf, variant, "ordinary.swf");
        Assert.assertEquals(file.getSwf().getAbcBlocks().size(), 1);
        Assert.assertEquals(bytes(swf), original);
        Assert.assertEquals(abc.bodies.get(0).getCodeBytes(), code);
        Assert.assertEquals(SwfToIggyConvertor.createIggy(swf, variant, "ordinary.swf", warning -> {
            Assert.fail(warning);
        }).getSwf().getAbcBlocks().size(), 1);
    }

    @Test(dataProvider = "variants")
    public void failedExportPreservesOutput(IggyFile.Variant variant) throws Exception {
        SWF swf = movie();
        DoABC2Tag tag = new DoABC2Tag(swf);
        method(tag.getABC(), new byte[]{0x47});
        method(tag.getABC(), new byte[]{0x47, 0x35, 0x47});
        swf.addTag(tag);
        Path directory = Files.createTempDirectory("iggy-alchemy-");
        Path output = directory.resolve("output.iggy");
        try {
            rejects(() -> SwfToIggyConvertor.exportIggyToFile(swf, output.toFile(), variant, "alchemy.swf"), "li8");
            Assert.assertFalse(Files.exists(output));
            byte[] original = {1, 2, 3, 4};
            Files.write(output, original);
            rejects(() -> SwfToIggyConvertor.exportIggyToFile(swf, output.toFile(), variant, "alchemy.swf", warning -> {
                Assert.fail(warning);
            }), "li8");
            Assert.assertEquals(Files.readAllBytes(output), original);
        } finally {
            Files.deleteIfExists(output);
            Files.delete(directory);
        }
    }
}

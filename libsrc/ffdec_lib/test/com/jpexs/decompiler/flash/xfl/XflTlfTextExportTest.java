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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.tags.ABCContainerTag;
import com.jpexs.decompiler.flash.tags.DefineBinaryDataTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.RemoveTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XflTlfTextExportTest {

    private static final String NS = "http://ns.adobe.com/xfl/2008/";

    private SWF movie(String version) throws Exception {
        try (InputStream input = Files.newInputStream(Paths.get("testdata/tlf/tlf_" + version + ".swf"))) {
            return new SWF(input, false);
        }
    }

    private byte[] bytes(SWF swf) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        swf.saveTo(output);
        return output.toByteArray();
    }

    private void cleanup(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    private Map<String, byte[]> export(SWF swf, Path directory, boolean compressed, boolean scripts, FLAVersion version) throws Exception {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = compressed;
        settings.exportScript = scripts;
        swf.exportXfl(null, directory.resolve(compressed ? "text.fla" : "text.xfl").toString(),
                "text.swf", "Test", "Test", "1", false, version, settings, null);
        Map<String, byte[]> files = new LinkedHashMap<>();
        if (compressed) {
            try (ZipInputStream input = new ZipInputStream(Files.newInputStream(directory.resolve("text.fla")))) {
                ZipEntry entry;
                byte[] buffer = new byte[8192];
                while ((entry = input.getNextEntry()) != null) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    int count;
                    while ((count = input.read(buffer)) != -1) {
                        output.write(buffer, 0, count);
                    }
                    files.put(entry.getName(), output.toByteArray());
                }
            }
        } else {
            try (Stream<Path> paths = Files.walk(directory.resolve("text"))) {
                for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                    files.put(directory.resolve("text").relativize(path).toString().replace('\\', '/'), Files.readAllBytes(path));
                }
            }
        }
        return files;
    }

    @DataProvider(name = "formats")
    public Object[][] formats() {
        return new Object[][]{
            {"cs5", false, true}, {"cs5", true, true}, {"cs5", false, false}, {"cs5", true, false},
            {"cs6", false, true}, {"cs6", true, true}, {"cs6", false, false}, {"cs6", true, false}
        };
    }

    @Test(dataProvider = "formats")
    public void restoresEditableTextFromRslWrapper(String version, boolean compressed, boolean scripts) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-tlf-");
        try {
            SWF wrapper = movie(version);
            byte[] before = bytes(wrapper);
            Map<String, byte[]> files = export(wrapper, directory, compressed, scripts,
                    "cs5".equals(version) ? FLAVersion.CS5 : FLAVersion.CS6);
            Document document = ComponentConverter.parse(new String(files.get("DOMDocument.xml"), StandardCharsets.UTF_8));
            String xml = new String(files.get("DOMDocument.xml"), StandardCharsets.UTF_8);
            Assert.assertTrue(xml.contains("<markup><tlfTextObject type=\"Paragraph\" editPolicy="),
                    "Flash requires TLF attribute order and unformatted mixed content");
            NodeList texts = document.getElementsByTagNameNS(NS, "DOMTLFText");
            Assert.assertEquals(texts.getLength(), 2);
            Assert.assertEquals(document.getElementsByTagNameNS(NS, "DOMSymbolInstance").getLength(), 1,
                    "Only MyRect should remain; CS5 runtime duplicates must be removed");
            Assert.assertEquals(document.getElementsByTagNameNS(NS, "DOMLayer").getLength(), "cs5".equals(version) ? 3 : 2);
            for (int i = 0; i < texts.getLength(); i++) {
                Element text = (Element) texts.item(i);
                boolean hello = text.getTextContent().contains("HELLO");
                Assert.assertTrue(hello || text.getTextContent().contains("MYFRAME10"));
                Assert.assertEquals(text.getAttribute("right"), hello ? "2599" : "3619");
                Assert.assertEquals(text.getAttribute("bottom"), hello ? "600" : "540");
                Assert.assertFalse(text.hasAttribute("name"), "Generated runtime IDs are not authoring names");
                Element matrix = (Element) text.getElementsByTagNameNS(NS, "Matrix").item(0);
                Assert.assertEquals(Double.parseDouble(matrix.getAttribute("tx")), hello ? 155.0 : 178.0);
                Assert.assertEquals(Double.parseDouble(matrix.getAttribute("ty")), hello ? 104.0 : 223.0);
                Element frame = (Element) text.getParentNode().getParentNode();
                Assert.assertEquals(frame.getAttribute("index"), hello ? "0" : "9");
                Assert.assertEquals(frame.hasAttribute("duration") ? frame.getAttribute("duration") : "1", hello ? "9" : "1");
                Element span = (Element) text.getElementsByTagNameNS("http://ns.adobe.com/textLayout/2008", "span").item(0);
                Assert.assertEquals(span.getTextContent(), hello ? "HELLO" : "MYFRAME10");
                Assert.assertEquals(span.getAttribute("color"), hello ? "#33cc00" : "#ff0000");
                Element font = (Element) text.getElementsByTagNameNS(NS, "TLFFont").item(0);
                Assert.assertEquals(font.getAttribute("platformName"), "Times New Roman");
                Assert.assertEquals(font.getAttribute("psName"), "TimesNewRomanPSMT");
            }
            Assert.assertEquals(bytes(wrapper), before, "Export must not change the original wrapper");
            int nestedTexts = 0;
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")) {
                    Document symbol = ComponentConverter.parse(new String(file.getValue(), StandardCharsets.UTF_8));
                    NodeList nested = symbol.getElementsByTagNameNS(NS, "DOMTLFText");
                    nestedTexts += nested.getLength();
                    if (nested.getLength() != 0) {
                        Assert.assertEquals(symbol.getElementsByTagNameNS(NS, "DOMLayer").getLength(), 2);
                        Assert.assertEquals(symbol.getElementsByTagNameNS(NS, "DOMSymbolInstance").getLength(), 0);
                        Assert.assertTrue(nested.item(0).getTextContent().contains("XX"));
                        Element text = (Element) nested.item(0);
                        Assert.assertEquals(text.getAttribute("left"), "-40");
                        Assert.assertEquals(text.getAttribute("top"), "-40");
                        Assert.assertEquals(text.getAttribute("right"), "1400");
                        Assert.assertEquals(text.getAttribute("bottom"), "1416");
                        Element matrix = (Element) text.getElementsByTagNameNS(NS, "Matrix").item(0);
                        Assert.assertEquals(Double.parseDouble(matrix.getAttribute("tx")), 8.0);
                        Assert.assertEquals(Double.parseDouble(matrix.getAttribute("ty")), 4.2);
                    }
                }
            }
            Assert.assertEquals(nestedTexts, 1, "TLF text on frame 5 of MyRect must also be editable");
            for (Tag tag : wrapper.getTags()) {
                if (tag instanceof DefineBinaryDataTag) {
                    Assert.assertNull(((DefineBinaryDataTag) tag).innerSwf, "Unwrapping must not load into the input tag");
                }
            }
            if (scripts) {
                assertCleanSource(directory.resolve("MyMain.as"), "hello");
                assertCleanSource(directory.resolve("MyRect.as"), "hello from MyRect");
                try (Stream<Path> paths = Files.walk(directory)) {
                    Assert.assertEquals(paths.filter(path -> path.toString().endsWith(".as")).count(), 2L,
                            "TLF implementation and generated font classes must not accompany native authoring text");
                }
                Assert.assertFalse(Files.exists(directory.resolve("fl")), "Removed TLF classes must not leave empty package directories");
                Assert.assertFalse(Files.exists(directory.resolve("tlf_" + version + "_fla")), "Removed font classes must not leave an empty package");
                Assert.assertFalse(Files.exists(directory.resolve("_assets")), "Fonts regenerated by native TLF text need no external CFF asset");
            }
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void directInnerExportDoesNotChangeNormalScriptExport() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-tlf-inner-");
        try {
            SWF swf = TlfTextConverter.unwrap(movie("cs6"));
            byte[] before = bytes(swf);
            Map<String, byte[]> files = export(swf, directory, false, true, FLAVersion.CS6);
            Assert.assertTrue(new String(files.get("DOMDocument.xml"), StandardCharsets.UTF_8).contains("DOMTLFText"));
            Assert.assertEquals(bytes(swf), before);
            ScriptPack pack = swf.getScriptPacksByClassNames(java.util.Collections.singletonList(swf.getDocumentClass())).get(0);
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            pack.toSource(swf.getAbcIndex(), writer, pack.abc.script_info.get(pack.scriptIndex).traits.traits,
                    new ConvertData(), ScriptExportMode.AS, false, false, false);
            String source = writer.toString();
            Assert.assertTrue(source.contains(".addInstance("), source);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void restoresTextInsideLibrarySymbols() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-tlf-symbol-");
        try {
            SWF swf = TlfTextConverter.unwrap(movie("cs6"));
            DefineSpriteTag sprite = new DefineSpriteTag(swf);
            sprite.spriteId = swf.getNextCharacterId();
            sprite.frameCount = swf.frameCount;
            for (Tag tag : swf.getTags().toArrayList()) {
                if (tag instanceof SymbolClassTag) {
                    SymbolClassTag symbols = (SymbolClassTag) tag;
                    for (int i = 0; i < symbols.tags.size(); i++) {
                        if (symbols.tags.get(i) == 0) {
                            symbols.tags.set(i, sprite.spriteId);
                        }
                    }
                    tag.setModified(true);
                }
                if (tag instanceof PlaceObjectTypeTag || tag instanceof RemoveTag || tag instanceof ShowFrameTag) {
                    swf.removeTag(tag);
                    sprite.addTag(tag);
                } else if (tag instanceof DefineSceneAndFrameLabelDataTag) {
                    swf.removeTag(tag);
                }
            }
            swf.addTag(sprite);
            swf.addTag(new PlaceObject2Tag(swf, false, 1, sprite.spriteId, new MATRIX(), null, -1, "container", -1, null));
            swf.addTag(new ShowFrameTag(swf));
            swf.frameCount = 1;
            swf = new SWF(new ByteArrayInputStream(bytes(swf)), false);
            Map<String, byte[]> files = export(swf, directory, false, true, FLAVersion.CS6);
            int count = 0;
            StringBuilder symbols = new StringBuilder();
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")) {
                    Document symbol = ComponentConverter.parse(new String(file.getValue(), StandardCharsets.UTF_8));
                    symbols.append(new String(file.getValue(), StandardCharsets.UTF_8));
                    count += symbol.getElementsByTagNameNS(NS, "DOMTLFText").getLength();
                }
            }
            Assert.assertEquals(count, 3, symbols.toString());
        } finally {
            cleanup(directory);
        }
    }

    @DataProvider(name = "unsupported")
    public Object[][] unsupported() {
        return new Object[][]{{false}, {true}};
    }

    @Test(dataProvider = "unsupported")
    public void unrecognizedMarkupRemainsRuntimeCode(boolean partial) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-tlf-unknown-");
        try {
            SWF swf = TlfTextConverter.unwrap(movie("cs6"));
            for (Tag tag : swf.getTags()) {
                if (tag instanceof ABCContainerTag) {
                    com.jpexs.decompiler.flash.abc.ABC abc = ((ABCContainerTag) tag).getABC();
                    for (int i = 1; i < abc.constants.getStringCount(); i++) {
                        String value = abc.constants.getString(i);
                        if (value.startsWith("<tlfTextObject") && (!partial || value.contains("MYFRAME10"))) {
                            abc.constants.setString(i, value.replace("tlfTextObject", "unknownTextObject"));
                        }
                    }
                }
            }
            Map<String, byte[]> files = export(swf, directory, false, true, FLAVersion.CS6);
            Document document = ComponentConverter.parse(new String(files.get("DOMDocument.xml"), StandardCharsets.UTF_8));
            Assert.assertEquals(document.getElementsByTagNameNS(NS, "DOMTLFText").getLength(), partial ? 1 : 0);
            String source = new String(Files.readAllBytes(directory.resolve("MyMain.as")), StandardCharsets.UTF_8);
            Assert.assertTrue(source.contains(".addInstance("), source);
            Assert.assertTrue(source.contains(".addInstanceComplete("), source);
            Assert.assertTrue(source.contains("MYFRAME10"), source);
            Assert.assertTrue(source.contains("trace(\"hello\")"), source);
            Assert.assertTrue(Files.isRegularFile(directory.resolve("fl/text/TCMRuntimeManager.as")),
                    "Unrestored text still needs its runtime");
            if (partial) {
                Assert.assertFalse(source.contains("HELLO"), source);
            }
        } finally {
            cleanup(directory);
        }
    }

    private void assertCleanSource(Path path, String message) throws Exception {
        String source = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        Assert.assertTrue(source.contains("trace(\"" + message + "\")"), source);
        Assert.assertTrue(source.contains("super();"), source);
        Assert.assertFalse(source.contains("__"), source);
        Assert.assertFalse(source.contains("XML."), source);
        Assert.assertFalse(source.contains("import fl."), source);
        Assert.assertFalse(source.contains("import flash.text.Font"), source);
        Assert.assertFalse(source.contains("import flash.geom."), source);
        Assert.assertFalse(source.contains("import flash.events.Event"), source);
        Assert.assertFalse(source.contains("_fla.Font_"), source);
    }

    private AVM2Code code(int... values) {
        AVM2Code code = new AVM2Code();
        for (int i = 0; i < values.length;) {
            int opcode = values[i++];
            int[] operands = new int[AVM2Code.instructionSet[opcode].operands.length];
            for (int j = 0; j < operands.length; j++) {
                operands[j] = values[i++];
            }
            code.code.add(new AVM2Instruction(0, opcode, operands));
        }
        code.markOffsets();
        return code;
    }

    private void addUserMethod(SWF swf, String name, java.util.function.ToIntFunction<ABC> reference) throws Exception {
        ScriptPack pack = swf.getScriptPacksByClassNames(Collections.singletonList(swf.getDocumentClass())).get(0);
        ABC abc = pack.abc;
        InstanceInfo instance = abc.instance_info.get(abc.findClassByName(swf.getDocumentClass()));
        int method = abc.method_info.size();
        abc.method_info.add(new MethodInfo(new int[0], 0, 0, 0, null, null));
        MethodBody body = abc.findBody(instance.iinit_index).clone();
        body.method_info = method;
        body.exceptions = new ABCException[0];
        body.max_stack = 1;
        body.max_regs = 1;
        body.setCode(code(AVM2Instructions.GetLocal0, AVM2Instructions.PushScope,
                AVM2Instructions.GetLex, reference.applyAsInt(abc), AVM2Instructions.ReturnValue));
        abc.bodies.add(body);
        TraitMethodGetterSetter trait = new TraitMethodGetterSetter();
        trait.name_index = abc.constants.getPublicQnameId(name, true);
        trait.method_info = method;
        trait.kindType = 1;
        instance.instance_traits.traits.add(trait);
        abc.resetMethodIndexing();
    }

    @Test
    public void retainsMotionLibraryUsedByUserCode() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-tlf-user-motion-");
        try {
            SWF swf = TlfTextConverter.unwrap(movie("cs5"));
            addUserMethod(swf, "userMotion", abc -> {
                for (int i = 1; i < abc.constants.getMultinameCount(); i++) {
                    if ("fl.motion.Motion".equals(abc.constants.getMultiname(i).getNameWithNamespace(
                            new LinkedHashSet<>(), abc, abc.constants, true).toRawString())) {
                        return i;
                    }
                }
                throw new AssertionError("Missing Motion multiname");
            });
            export(swf, directory, true, true, FLAVersion.CS5);
            String source = new String(Files.readAllBytes(directory.resolve("MyMain.as")), StandardCharsets.UTF_8);
            Assert.assertTrue(source.contains("function userMotion"), source);
            Assert.assertTrue(source.contains("import fl.motion.Motion;"), source);
            Assert.assertTrue(Files.isRegularFile(directory.resolve("fl/motion/Motion.as")));
            Assert.assertTrue(Files.isRegularFile(directory.resolve("fl/motion/MotionBase.as")));
            Assert.assertFalse(Files.exists(directory.resolve("fl/text/RuntimeManager.as")));
            Assert.assertFalse(source.contains("__animFactory_"), source);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void retainsFontAssetUsedByUserCode() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-tlf-user-font-");
        try {
            SWF swf = TlfTextConverter.unwrap(movie("cs6"));
            addUserMethod(swf, "userFont", abc -> {
                for (int i = 1; i < abc.constants.getMultinameCount(); i++) {
                    String type = abc.constants.getMultiname(i).getNameWithNamespace(
                            new LinkedHashSet<>(), abc, abc.constants, true).toRawString();
                    if (swf.getCharacterByClass(type) instanceof com.jpexs.decompiler.flash.tags.DefineFont4Tag) {
                        return i;
                    }
                }
                throw new AssertionError("Missing font class multiname");
            });
            export(swf, directory, true, true, FLAVersion.CS6);
            String source = new String(Files.readAllBytes(directory.resolve("MyMain.as")), StandardCharsets.UTF_8);
            Assert.assertTrue(source.contains("function userFont"), source);
            Assert.assertTrue(Files.isDirectory(directory.resolve("tlf_cs6_fla")));
            try (Stream<Path> assets = Files.list(directory.resolve("_assets"))) {
                Assert.assertEquals(assets.filter(path -> path.toString().endsWith(".cff")).count(), 1L);
            }
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void preservesUnrelatedFilesInCleanedDirectories() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-tlf-existing-files-");
        try {
            Path notes = directory.resolve("fl/text/notes.txt");
            Path asset = directory.resolve("_assets/keep.bin");
            Files.createDirectories(notes.getParent());
            Files.createDirectories(asset.getParent());
            Files.write(notes, "user notes".getBytes(StandardCharsets.UTF_8));
            Files.write(asset, new byte[]{1, 2, 3});
            export(movie("cs6"), directory, true, true, FLAVersion.CS6);
            Assert.assertEquals(new String(Files.readAllBytes(notes), StandardCharsets.UTF_8), "user notes");
            Assert.assertEquals(Files.readAllBytes(asset), new byte[]{1, 2, 3});
            try (Stream<Path> assets = Files.list(asset.getParent())) {
                Assert.assertEquals(assets.count(), 1L, "Only the unrelated asset should remain");
            }
            try (Stream<Path> files = Files.list(notes.getParent())) {
                Assert.assertEquals(files.count(), 1L, "Only the user's notes should remain");
            }
        } finally {
            cleanup(directory);
        }
    }
}

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
import com.jpexs.flash.fla.reader.BinaryData;
import com.jpexs.flash.fla.reader.BinaryFlaDocument;
import com.jpexs.flash.fla.reader.BinaryFlaReader;
import com.jpexs.flash.fla.reader.FlaRecord;
import com.jpexs.decompiler.flash.abc.ScriptPack;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.ConvertData;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.HighlightedTextWriter;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.DefineBitsTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
import com.jpexs.decompiler.flash.tags.JPEGTablesTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ImageTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.helpers.ByteArrayRange;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class XflComponentExportTest {

    private SWF movie() throws Exception {
        return movie("button");
    }

    private SWF movie(String name) throws Exception {
        try (InputStream input = Files.newInputStream(Paths.get("testdata/components/" + name + ".swf"))) {
            return new SWF(input, false);
        }
    }

    private byte[] bytes(SWF swf) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        swf.saveTo(output);
        return output.toByteArray();
    }

    private Map<String, byte[]> export(SWF swf, Path directory, boolean compressed, boolean scripts) throws Exception {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = compressed;
        settings.exportScript = scripts;
        swf.exportXfl(null, directory.resolve(compressed ? "button.fla" : "button.xfl").toString(),
                "button.swf", "Test", "Test", "1", false, FLAVersion.CS6, settings, null);
        Map<String, byte[]> files = new LinkedHashMap<>();
        if (compressed) {
            try (ZipInputStream input = new ZipInputStream(Files.newInputStream(directory.resolve("button.fla")))) {
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
            try (Stream<Path> paths = Files.walk(directory.resolve("button"))) {
                for (Path path : (Iterable<Path>) paths.filter(Files::isRegularFile)::iterator) {
                    files.put(directory.resolve("button").relativize(path).toString().replace('\\', '/'), Files.readAllBytes(path));
                }
            }
        }
        return files;
    }

    private String text(byte[] data) {
        return new String(data, StandardCharsets.UTF_8);
    }

    private String library(Map<String, byte[]> files) {
        for (Map.Entry<String, byte[]> file : files.entrySet()) {
            if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")
                    && text(file.getValue()).contains("linkageClassName=\"fl.controls.Button\"")) {
                return text(file.getValue());
            }
        }
        throw new AssertionError("Missing Button library item");
    }

    private void cleanup(Path directory) throws Exception {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }

    @DataProvider(name = "formats")
    public Object[][] formats() {
        return new Object[][]{{true}, {false}};
    }

    @Test(dataProvider = "formats")
    public void restoresButtonParametersAndPreviewWithoutChangingSwf(boolean compressed) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-");
        try {
            SWF swf = movie();
            byte[] before = bytes(swf);
            Map<String, byte[]> files = export(swf, directory, compressed, true);
            String document = text(files.get("DOMDocument.xml"));
            String symbol = library(files);
            Assert.assertTrue(document.contains("DOMComponentInstance"));
            Assert.assertTrue(document.contains("defaultValue=\"Hello\""), document);
            Assert.assertTrue(document.contains("type=\"List\""));
            Assert.assertTrue(symbol.contains("DOMComponentItem"));
            Assert.assertTrue(symbol.contains("defaultValue=\"Label\""));
            Assert.assertTrue(symbol.contains("livePreview11ScmHRef=\"component_button_preview.swf\""));
            String source = text(Files.readAllBytes(directory.resolve(swf.getDocumentClass().replace('.', '/') + ".as")));
            Assert.assertFalse(source.contains("__setProp_"), "Original initializer would overwrite Inspector edits");
            SWF preview = new SWF(new ByteArrayInputStream(files.get("bin/component_button_preview.swf")), false);
            Assert.assertNotNull(preview.getCharacterByClass("fl.livepreview.LivePreviewParent"));
            Assert.assertNotNull(preview.getCharacterByClass("fl.controls.Button"));
            Assert.assertNotNull(preview.getCharacterByClass("Button_upSkin"));
            Assert.assertEquals(bytes(swf), before, "Export must not change input bytecode or tags");
            ScriptPack pack = swf.getScriptPacksByClassNames(java.util.Collections.singletonList(swf.getDocumentClass())).get(0);
            HighlightedTextWriter writer = new HighlightedTextWriter(Configuration.getCodeFormatting(), false);
            pack.toSource(swf.getAbcIndex(), writer, pack.abc.script_info.get(pack.scriptIndex).traits.traits,
                    new ConvertData(), ScriptExportMode.AS, false, false, false);
            Assert.assertTrue(writer.toString().contains("__setProp_"), "Normal AS export must retain the original method");
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void noScriptExportKeepsOrdinarySymbols() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-noscript-");
        try {
            Map<String, byte[]> files = export(movie(), directory, false, false);
            Assert.assertFalse(text(files.get("DOMDocument.xml")).contains("DOMComponentInstance"));
            Assert.assertTrue(library(files).contains("DOMSymbolItem"));
            Assert.assertFalse(files.containsKey("bin/component_button_preview.swf"));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void restoresComponentsInsideLinkedMovieClips() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-nested-");
        try {
            SWF swf = movie();
            DefineSpriteTag nested = new DefineSpriteTag(swf);
            nested.frameCount = 1;
            int firstFrame = -1;
            for (Tag tag : new ArrayList<>(swf.getTags().toArrayList())) {
                if (tag instanceof PlaceObjectTypeTag) {
                    swf.removeTag(tag);
                    nested.addTag(tag);
                } else if (tag instanceof SymbolClassTag) {
                    SymbolClassTag symbols = (SymbolClassTag) tag;
                    for (int i = 0; i < symbols.tags.size(); i++) {
                        if (symbols.tags.get(i) == 0) {
                            symbols.tags.set(i, nested.spriteId);
                        }
                    }
                    symbols.setModified(true);
                }
            }
            nested.addTag(new ShowFrameTag(swf));
            swf.addTag(1, nested);
            for (int i = 0; i < swf.getTags().size(); i++) {
                if (swf.getTags().get(i) instanceof ShowFrameTag) {
                    firstFrame = i;
                    break;
                }
            }
            swf.addTag(firstFrame, new PlaceObject2Tag(swf, false, 1, nested.spriteId, new MATRIX(), null, -1, "container", -1, null));
            swf = new SWF(new ByteArrayInputStream(bytes(swf)), false);
            Map<String, byte[]> files = export(swf, directory, false, true);
            boolean found = false;
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")) {
                    String xml = text(file.getValue());
                    if (xml.contains("DOMComponentInstance")) {
                        Assert.assertTrue(xml.contains("defaultValue=\"Hello\""));
                        Assert.assertTrue(xml.contains("uniqueID="));
                        found = true;
                    }
                }
            }
            Assert.assertTrue(found);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void unrecognizedInitializerIsNotRemoved() throws Exception {
        SWF swf = movie();
        MethodBody body = new ComponentConverter(swf).getIgnoredMethods().iterator().next();
        int visible = -1;
        int field = -1;
        for (AVM2Instruction instruction : body.getCode().code) {
            if (instruction.definition.instructionCode == AVM2Instructions.GetProperty) {
                field = instruction.operands[0];
            }
            if (instruction.definition.instructionCode == AVM2Instructions.SetProperty
                    && swf.getAbcList().get(0).getABC().constants.getMultiname(instruction.operands[0]).getName(
                            new java.util.LinkedHashSet<>(), swf.getAbcList().get(0).getABC(),
                            swf.getAbcList().get(0).getABC().constants, new ArrayList<>(), false, false).equals("visible")) {
                visible = instruction.operands[0];
            }
        }
        List<AVM2Instruction> extra = new ArrayList<>();
        extra.add(new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
        extra.add(new AVM2Instruction(0, AVM2Instructions.GetProperty, new int[]{field}));
        extra.add(new AVM2Instruction(0, AVM2Instructions.PushFalse, null));
        extra.add(new AVM2Instruction(0, AVM2Instructions.SetProperty, new int[]{visible}));
        body.getCode().code.addAll(body.getCode().code.size() - 1, extra);
        body.getCode().markOffsets();
        body.max_stack = Math.max(body.max_stack, 2);
        Assert.assertTrue(new ComponentConverter(swf).getIgnoredMethods().isEmpty());
    }

    private AVM2Code code(int... instructions) {
        AVM2Code code = new AVM2Code();
        for (int i = 0; i < instructions.length;) {
            int opcode = instructions[i++];
            int count = AVM2Code.instructionSet[opcode].operands.length;
            int[] operands = new int[count];
            for (int j = 0; j < count; j++) {
                operands[j] = instructions[i++];
            }
            code.code.add(new AVM2Instruction(0, opcode, operands));
        }
        code.markOffsets();
        return code;
    }

    @DataProvider(name = "scenes")
    public Object[][] scenes() {
        return new Object[][]{{false}, {true}};
    }

    @Test(dataProvider = "scenes")
    public void frameParameterChangesSplitHeldFramesAndRemoveOnlyGeneratedCalls(boolean multipleScenes) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-frames-");
        try {
            SWF swf = movie();
            ScriptPack pack = swf.getScriptPacksByClassNames(java.util.Collections.singletonList(swf.getDocumentClass())).get(0);
            int classIndex = ((TraitClass) pack.abc.script_info.get(pack.scriptIndex).traits.traits.get(pack.traitIndices.get(0))).class_info;
            InstanceInfo instance = pack.abc.instance_info.get(classIndex);
            TraitMethodGetterSetter initializer = (TraitMethodGetterSetter) instance.instance_traits.traits.get(1);
            int frameName = pack.abc.constants.getPublicQnameId("frame3", true);
            int frameMethod = pack.abc.method_info.size();
            pack.abc.method_info.add(new MethodInfo(new int[0], 0, 0, 0, null, null));
            MethodBody frameBody = pack.abc.findBody(initializer.method_info).clone();
            frameBody.method_info = frameMethod;
            frameBody.exceptions = new ABCException[0];
            frameBody.setCode(code(AVM2Instructions.GetLocal0, AVM2Instructions.PushScope,
                    AVM2Instructions.GetLocal0, AVM2Instructions.CallPropVoid, initializer.name_index, 0,
                    AVM2Instructions.ReturnVoid));
            pack.abc.bodies.add(frameBody);
            TraitMethodGetterSetter frameTrait = new TraitMethodGetterSetter();
            frameTrait.name_index = frameName;
            frameTrait.kindType = 1;
            frameTrait.method_info = frameMethod;
            instance.instance_traits.traits.add(frameTrait);
            MethodBody constructor = pack.abc.findBody(instance.iinit_index);
            constructor.setCode(code(AVM2Instructions.GetLocal0, AVM2Instructions.PushScope,
                    AVM2Instructions.GetLocal0, AVM2Instructions.ConstructSuper, 0,
                    AVM2Instructions.GetLocal0, AVM2Instructions.PushByte, 2,
                    AVM2Instructions.GetLocal0, AVM2Instructions.GetProperty, frameName,
                    AVM2Instructions.CallPropVoid, pack.abc.constants.getPublicQnameId("addFrameScript", true), 2,
                    AVM2Instructions.ReturnVoid));
            constructor.max_stack = 3;
            pack.abc.resetMethodIndexing();
            swf.frameCount = 4;
            for (int i = 1; i < 4; i++) {
                swf.addTag(new ShowFrameTag(swf));
            }
            if (multipleScenes) {
                Tag placement = null;
                for (Tag tag : swf.getTags()) {
                    if (tag instanceof PlaceObjectTypeTag) {
                        placement = tag;
                        break;
                    }
                }
                int frameNumber = 0;
                for (int i = 0; i < swf.getTags().size(); i++) {
                    if (swf.getTags().get(i) instanceof ShowFrameTag && frameNumber++ == 2) {
                        // Flash starts a new scene with explicit placements.
                        swf.addTag(i, placement.cloneTag());
                        break;
                    }
                }
                for (Tag tag : swf.getTags()) {
                    if (tag instanceof DefineSceneAndFrameLabelDataTag) {
                        DefineSceneAndFrameLabelDataTag scenes = (DefineSceneAndFrameLabelDataTag) tag;
                        scenes.sceneOffsets = new long[]{0, 2};
                        scenes.sceneNames = new String[]{"First", "Second"};
                    }
                }
            }
            Map<String, byte[]> files = export(swf, directory, false, true);
            String document = text(files.get("DOMDocument.xml"));
            Assert.assertTrue(document.contains(multipleScenes ? "name=\"Second\"" : "index=\"2\""));
            Assert.assertTrue(document.contains("defaultValue=\"Hello\""), document);
            Assert.assertTrue(document.contains("defaultValue=\"Label\""));
            Assert.assertFalse(document.contains("__setProp_"));
            Assert.assertFalse(text(Files.readAllBytes(directory.resolve(swf.getDocumentClass().replace('.', '/') + ".as"))).contains("__setProp_"));
        } finally {
            cleanup(directory);
        }
    }
    private String library(Map<String, byte[]> files, String className) {
        for (Map.Entry<String, byte[]> file : files.entrySet()) {
            if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")
                    && text(file.getValue()).contains("linkageClassName=\"" + className + "\"")) {
                return text(file.getValue());
            }
        }
        throw new AssertionError("Missing library item: " + className);
    }

    private org.w3c.dom.Document xml(String text) throws Exception {
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(text)));
    }

    @Test
    public void restoresEveryUiComponentIncludingUnchangedInstances() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-ui-");
        try {
            SWF swf = movie("all-ui");
            byte[] before = bytes(swf);
            Map<String, byte[]> files = export(swf, directory, true, true);
            org.w3c.dom.Document document = xml(text(files.get("DOMDocument.xml")));
            Assert.assertEquals(document.getElementsByTagName("DOMComponentInstance").getLength(), 17);
            for (String name : new String[]{"Button", "CheckBox", "ColorPicker", "ComboBox", "DataGrid", "Label", "List",
                "NumericStepper", "ProgressBar", "RadioButton", "Slider", "TextArea", "TextInput", "TileList", "UIScrollBar"}) {
                Assert.assertTrue(library(files, "fl.controls." + name).contains("<DOMComponentItem"), name);
            }
            for (String name : new String[]{"ScrollPane", "UILoader"}) {
                Assert.assertTrue(library(files, "fl.containers." + name).contains("<DOMComponentItem"), name);
            }
            String main = text(files.get("DOMDocument.xml"));
            Assert.assertTrue(main.contains("defaultValue=\"#336699\""), main);
            Assert.assertTrue(main.contains("defaultValue=\"Select an item\""));
            Assert.assertTrue(main.contains("fl.data.DataProvider, fl.data.SimpleCollectionItem, item, 0, 0"));
            Assert.assertFalse(text(Files.readAllBytes(directory.resolve(swf.getDocumentClass().replace('.', '/') + ".as"))).contains("__setProp_"));
            Assert.assertEquals(bytes(swf), before);
        } finally {
            cleanup(directory);
        }
    }

    @DataProvider(name = "videoVersions")
    public Object[][] videoVersions() {
        return new Object[][]{{"video", false, 15}, {"video25", true, 2}};
    }

    @Test(dataProvider = "videoVersions")
    public void restoresVideoVersionsAndLiteralCollections(String fixture, boolean dvr, int instances) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-video-");
        try {
            Map<String, byte[]> files = export(movie(fixture), directory, true, true);
            String main = text(files.get("DOMDocument.xml"));
            Assert.assertEquals(xml(main).getElementsByTagName("DOMComponentInstance").getLength(), instances, main);
            if (dvr) {
                assertComponentLibrary(files, "video25", "FLVPlayback 2.5", "ComboBox");
            } else {
                assertComponentLibrary(files, "video", "FLVPlayback", "FLVPlaybackCaptioning", "BackButton", "ForwardButton", "PauseButton",
                        "PlayButton", "PlayPauseButton", "StopButton", "VolumeBar", "SeekBar", "BufferingBar", "MuteButton", "FullScreenButton", "CaptionButton");
            }
            String playback = library(files, "fl.video.FLVPlayback");
            Assert.assertTrue(playback.contains("<DOMComponentItem"));
            Assert.assertEquals(playback.contains("id=\"isDVR\""), dvr);
            if (!dvr) {
                for (String control : new String[]{"BackButton", "ForwardButton", "PauseButton", "PlayButton", "PlayPauseButton",
                    "StopButton", "VolumeBar", "SeekBar", "BufferingBar", "MuteButton", "FullScreenButton", "CaptionButton"}) {
                    Assert.assertTrue(library(files, control).contains("<DOMComponentItem"), control);
                }
                Assert.assertTrue(library(files, "fl.video.FLVPlaybackCaptioning").contains("id=\"captionTargetName\""));
                String captionSource = text(Files.readAllBytes(directory.resolve("fl/video/TimedTextManager.as")));
                Assert.assertTrue(captionSource.contains("@[new QName(this.ttp,\"timeBase\")]"), captionSource);
                Assert.assertFalse(captionSource.contains(".this.ttp::@"));
            } else {
                Assert.assertTrue(main.contains("First&amp;amp;#2C"), main);
                Assert.assertTrue(main.contains("caf\u00e9"), main);
                Assert.assertTrue(main.contains("data&amp;amp;#2C 1"), main);
            }
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void restoresListAndTileListCollections() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-collections-");
        try {
            Map<String, byte[]> files = export(movie("collections"), directory, true, true);
            String main = text(files.get("DOMDocument.xml"));
            Assert.assertEquals(xml(main).getElementsByTagName("DOMComponentInstance").getLength(), 17);
            Assert.assertTrue(main.contains("List row, value"), main);
            Assert.assertTrue(main.contains("Tile row, photo.png"), main);
            Assert.assertTrue(main.contains("fl.data.TileListCollectionItem, item, 2, label, 5, , , source, 5"), main);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void modifiedCollectionLoopKeepsItsCodeAndOtherComponentTypes() throws Exception {
        SWF swf = movie("collections");
        ScriptPack pack = swf.getScriptPacksByClassNames(java.util.Collections.singletonList(swf.getDocumentClass())).get(0);
        int classIndex = ((TraitClass) pack.abc.script_info.get(pack.scriptIndex).traits.traits.get(pack.traitIndices.get(0))).class_info;
        MethodBody changed = null;
        for (Trait trait : pack.abc.instance_info.get(classIndex).instance_traits.traits) {
            if (trait instanceof TraitMethodGetterSetter && trait.getName(pack.abc).getName(new java.util.LinkedHashSet<>(),
                    pack.abc, pack.abc.constants, new ArrayList<>(), false, false).startsWith("__setProp_sample_List_")) {
                changed = pack.abc.findBody(((TraitMethodGetterSetter) trait).method_info);
                for (AVM2Instruction instruction : changed.getCode().code) {
                    if (instruction.definition.instructionCode == AVM2Instructions.CallPropVoid) {
                        instruction.operands[0] = pack.abc.constants.getPublicQnameId("sideEffect", true);
                    }
                }
            }
        }
        Assert.assertNotNull(changed);
        ComponentConverter converter = new ComponentConverter(swf);
        Assert.assertFalse(converter.getIgnoredMethods().contains(changed));
        Assert.assertFalse(converter.isComponent(swf.getCharacterByClass("fl.controls.List")));
        Assert.assertTrue(converter.isComponent(swf.getCharacterByClass("fl.controls.TileList")));
    }

    @Test
    public void restoresLinkedFlexContentWithoutInventingAnAbsentPlacement() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-flex-");
        try {
            SWF swf = movie("video");
            Assert.assertNull(swf.getCharacterByClass("mx.flash.FlexContentHolder"), "CS6 omits the empty authoring placeholder");
            DefineSpriteTag linked = new DefineSpriteTag(swf);
            linked.frameCount = 1;
            linked.addTag(new ShowFrameTag(swf));
            swf.addTag(1, linked);
            SymbolClassTag symbols = new SymbolClassTag(swf);
            symbols.tags.add(linked.spriteId);
            symbols.names.add("mx.flash.FlexContentHolder");
            swf.addTag(swf.getTags().size() - 1, symbols);
            swf.addTag(swf.getTags().size() - 1, new PlaceObject2Tag(swf, false, 100, linked.spriteId,
                    new MATRIX(), null, -1, "sample_Flex", -1, null));
            swf = new SWF(new ByteArrayInputStream(bytes(swf)), false);
            Map<String, byte[]> files = export(swf, directory, true, true);
            Assert.assertTrue(library(files, "mx.flash.FlexContentHolder").contains("<DOMComponentItem"));
            Assert.assertTrue(text(files.get("DOMDocument.xml")).contains("id=\"scaleContentWhenResized\""));
        } finally {
            cleanup(directory);
        }
    }

    private void assertComponentLibrary(Map<String, byte[]> files, String sourceFolder, String... componentNames) throws Exception {
        String ns = "http://ns.adobe.com/xfl/2008/";
        org.w3c.dom.Document main = xml(text(files.get("DOMDocument.xml")));
        java.util.Set<String> itemNames = new java.util.HashSet<>();
        for (String type : new String[]{"DOMBitmapItem", "DOMSoundItem", "DOMVideoItem", "DOMFontItem"}) {
            org.w3c.dom.NodeList items = main.getElementsByTagNameNS(ns, type);
            for (int i = 0; i < items.getLength(); i++) {
                itemNames.add(((org.w3c.dom.Element) items.item(i)).getAttribute("name"));
            }
        }
        List<org.w3c.dom.Document> documents = new ArrayList<>();
        documents.add(main);
        for (Map.Entry<String, byte[]> file : files.entrySet()) {
            if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")) {
                org.w3c.dom.Document item = xml(text(file.getValue()));
                documents.add(item);
                org.w3c.dom.Element root = item.getDocumentElement();
                itemNames.add(root.getAttribute("name"));
                if (!"DOMComponentItem".equals(root.getLocalName())) {
                    Assert.assertTrue(root.getAttribute("name").startsWith("Component Assets/"), root.getAttribute("name"));
                }
            }
        }
        for (org.w3c.dom.Document document : documents) {
            org.w3c.dom.NodeList elements = document.getElementsByTagName("*");
            for (int i = 0; i < elements.getLength(); i++) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) elements.item(i);
                for (String attribute : new String[]{"libraryItemName", "bitmapPath"}) {
                    if (element.hasAttribute(attribute)) {
                        Assert.assertTrue(itemNames.contains(element.getAttribute(attribute)), "Broken " + attribute + ": " + element.getAttribute(attribute));
                    }
                }
            }
        }
        for (String name : componentNames) {
            org.w3c.dom.Element root = xml(text(files.get("LIBRARY/" + name + ".xml"))).getDocumentElement();
            Assert.assertEquals(root.getAttribute("name"), name);
            Assert.assertEquals(root.getLocalName(), "DOMComponentItem");
            Path nativeFile = Paths.get("testdata/components/" + sourceFolder + "/LIBRARY/" + name + ".xml");
            org.w3c.dom.Element nativeRoot;
            if (Files.exists(nativeFile)) {
                nativeRoot = xml(text(Files.readAllBytes(nativeFile))).getDocumentElement();
            } else {
                org.w3c.dom.NodeList clips = xml(text(Files.readAllBytes(Paths.get("testdata/components/" + sourceFolder + "/DOMDocument.xml"))))
                        .getElementsByTagNameNS(ns, "DOMCompiledClipItem");
                nativeRoot = null;
                for (int i = 0; i < clips.getLength(); i++) {
                    org.w3c.dom.Element clip = (org.w3c.dom.Element) clips.item(i);
                    if (name.equals(clip.getAttribute("name"))) {
                        nativeRoot = clip;
                    }
                }
            }
            Assert.assertNotNull(nativeRoot, name);
            Assert.assertEquals(root.getAttribute("customIconID"), nativeRoot.getAttribute("customIconID"), name);
            org.w3c.dom.NodeList nativeIcons = nativeRoot.getElementsByTagNameNS(ns, "CustomIcon");
            org.w3c.dom.NodeList icons = root.getElementsByTagNameNS(ns, "CustomIcon");
            Assert.assertEquals(icons.getLength(), nativeIcons.getLength(), name);
            if (icons.getLength() == 1) {
                org.w3c.dom.Element original = (org.w3c.dom.Element) nativeIcons.item(0);
                org.w3c.dom.Element actual = (org.w3c.dom.Element) icons.item(0);
                Assert.assertEquals(root.getAttribute("customIconID"), "0");
                for (String attribute : new String[]{"rowByteCount", "colorDepth", "width", "height", "frameRight", "frameBottom", "isTransparent"}) {
                    Assert.assertEquals(actual.getAttribute(attribute), original.getAttribute(attribute), name + ": " + attribute);
                }
                Assert.assertEquals(files.get("bin/" + actual.getAttribute("href")), Files.readAllBytes(Paths.get("testdata/components/" + sourceFolder + "/bin/" + original.getAttribute("href"))), name);
            }
        }
        org.w3c.dom.NodeList includes = main.getElementsByTagNameNS(ns, "Include");
        for (int i = 0; i < includes.getLength(); i++) {
            org.w3c.dom.Element include = (org.w3c.dom.Element) includes.item(i);
            Assert.assertTrue(files.containsKey("LIBRARY/" + include.getAttribute("href")));
            if (java.util.Arrays.asList(componentNames).contains(include.getAttribute("href").replace(".xml", ""))) {
                Assert.assertFalse(include.hasAttribute("itemIcon"), "A generic movie-clip icon must not override the component icon");
            }
        }
    }

    @Test(dataProvider = "formats")
    public void usesNativeComponentNamesIconsAndAssetFolders(boolean compressed) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-library-");
        try {
            Map<String, byte[]> files = export(movie("all-ui"), directory, compressed, true);
            assertComponentLibrary(files, "all-ui", "Button", "CheckBox", "ColorPicker", "ComboBox", "DataGrid", "Label", "List",
                    "NumericStepper", "ProgressBar", "RadioButton", "ScrollPane", "Slider", "TextArea", "TextInput", "TileList", "UILoader", "UIScrollBar");
            for (String path : new String[]{"ButtonSkins/Button_upSkin", "CellRenderer", "ScrollBar", "DataGridSkins/HeaderRendererSkins/HeaderRenderer_upSkin"}) {
                Assert.assertTrue(files.containsKey("LIBRARY/Component Assets/" + path + ".xml"), path);
            }
            Assert.assertTrue(text(files.get("DOMDocument.xml")).contains("name=\"Component Assets/ButtonSkins\""));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void leavesUnrelatedLibrarySymbolsOutsideComponentAssets() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-library-unrelated-");
        try {
            SWF swf = movie();
            DefineSpriteTag artwork = new DefineSpriteTag(swf);
            artwork.frameCount = 1;
            artwork.addTag(new ShowFrameTag(swf));
            swf.addTag(1, artwork);
            SymbolClassTag symbols = new SymbolClassTag(swf);
            symbols.tags.add(artwork.spriteId);
            symbols.names.add("example.Artwork");
            swf.addTag(swf.getTags().size() - 1, symbols);
            swf.addTag(swf.getTags().size() - 1, new PlaceObject2Tag(swf, false, 100, artwork.spriteId,
                    new MATRIX(), null, -1, "artwork", -1, null));
            swf = new SWF(new ByteArrayInputStream(bytes(swf)), false);
            Map<String, byte[]> files = export(swf, directory, true, true);
            org.w3c.dom.Element root = xml(library(files, "example.Artwork")).getDocumentElement();
            Assert.assertFalse(root.getAttribute("name").contains("/"));
            Assert.assertEquals(root.getLocalName(), "DOMSymbolItem");
        } finally {
            cleanup(directory);
        }
    }

    private org.w3c.dom.Document document(Map<String, byte[]> files) throws Exception {
        return ComponentConverter.parse(text(files.get("DOMDocument.xml")));
    }

    private org.w3c.dom.Element named(org.w3c.dom.Document document, String type, String name) {
        for (org.w3c.dom.Element element : ComponentConverter.elements(document.getDocumentElement(), type)) {
            if (name.equals(element.getAttribute("name"))) {
                return element;
            }
        }
        throw new AssertionError("Missing " + type + " " + name);
    }

    @Test(dataProvider = "formats")
    public void restoresAllAs2UiComponentsAndOriginalBytecode(boolean compressed) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-as2-ui-");
        try {
            SWF swf = movie("as2-ui");
            byte[] original = bytes(swf);
            Map<String, byte[]> files = export(swf, directory, compressed, true);
            org.w3c.dom.Document document = document(files);
            Assert.assertEquals(ComponentConverter.elements(document.getDocumentElement(), "DOMCompiledClipItem").size(), 22);
            Assert.assertTrue(ComponentConverter.elements(document.getDocumentElement(), "DOMCompiledClipInstance").size() >= 21);
            org.w3c.dom.Element button = named(document, "DOMCompiledClipItem", "Button");
            Assert.assertEquals(button.getAttribute("actionscriptClass"), "mx.controls.Button");
            Assert.assertEquals(button.getAttribute("rootSymbolLinkageID"), "Button");
            Assert.assertTrue(ComponentConverter.elements(button, "AssetMapItem").size() > 30);
            Assert.assertEquals(ComponentConverter.elements(button, "customIcon").size(), 1);
            String source = text(files.get("DOMDocument.xml"));
            Assert.assertTrue(source.contains("Sample Button"));
            org.w3c.dom.Element instance = named(document, "DOMCompiledClipInstance", "sampleButton");
            Assert.assertEquals(ComponentConverter.elements(instance, "parametersAsXML").get(0).getFirstChild().getNodeType(),
                    org.w3c.dom.Node.CDATA_SECTION_NODE);
            Assert.assertTrue(source.contains("January,February,March"));
            Assert.assertFalse(source.contains("on(construct)"));
            SWF runtime = new SWF(new ByteArrayInputStream(files.get("bin/component_runtime.swf")), false);
            int preservedScripts = 0;
            for (Tag tag : swf.getTags()) {
                if (tag instanceof com.jpexs.decompiler.flash.tags.DoInitActionTag) {
                    com.jpexs.decompiler.flash.tags.DoInitActionTag originalScript = (com.jpexs.decompiler.flash.tags.DoInitActionTag) tag;
                    for (Tag runtimeTag : runtime.getTags()) {
                        if (runtimeTag instanceof com.jpexs.decompiler.flash.tags.DoInitActionTag
                                && ((com.jpexs.decompiler.flash.tags.DoInitActionTag) runtimeTag).spriteId == originalScript.spriteId) {
                            Assert.assertEquals(runtimeTag.getData(), tag.getData());
                            preservedScripts++;
                        }
                    }
                }
            }
            Assert.assertTrue(preservedScripts > 60);
            for (Tag tag : runtime.getTags()) {
                Assert.assertFalse(tag instanceof PlaceObjectTypeTag || tag instanceof com.jpexs.decompiler.flash.tags.DoActionTag);
            }
            SWF preview = new SWF(new ByteArrayInputStream(files.get("bin/" + button.getAttribute("livePreview11ScmHRef"))), false);
            Assert.assertFalse(preview.isAS3());
            Assert.assertEquals(preview.displayRect.getWidth(), 2000);
            Assert.assertTrue(preview.getTags().toArrayList().stream().anyMatch(tag -> tag instanceof PlaceObjectTypeTag));
            Assert.assertFalse(files.keySet().stream().anyMatch(path -> path.endsWith(".as")));
            Assert.assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void restoresAs2MediaAndVideoComponents() throws Exception {
        for (String fixture : new String[]{"as2-media", "as2-video"}) {
            Path directory = Files.createTempDirectory("ffdec-" + fixture);
            try {
                Map<String, byte[]> files = export(movie(fixture), directory, true, true);
                org.w3c.dom.Document document = document(files);
                if (fixture.equals("as2-media")) {
                    Assert.assertEquals(ComponentConverter.elements(document.getDocumentElement(), "DOMCompiledClipItem").size(), 4);
                    Assert.assertEquals(named(document, "DOMCompiledClipItem", "FLVPlayback").getAttribute("actionscriptClass"), "mx.video.FLVPlayback");
                    Assert.assertTrue(text(files.get("DOMDocument.xml")).contains("ClearOverPlaySeekMute.swf"));
                } else {
                    Assert.assertEquals(ComponentConverter.elements(document.getDocumentElement(), "DOMComponentInstance").size(), 10);
                    int components = 0;
                    for (Map.Entry<String, byte[]> file : files.entrySet()) {
                        if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")) {
                            org.w3c.dom.Document item = ComponentConverter.parse(text(file.getValue()));
                            if (item.getDocumentElement().getLocalName().equals("DOMComponentItem")) {
                                components++;
                                Assert.assertFalse(item.getDocumentElement().getAttribute("name").contains("/"));
                            } else {
                                Assert.assertTrue(item.getDocumentElement().getAttribute("name").startsWith("Component Assets/"));
                            }
                        }
                    }
                    Assert.assertEquals(components, 10);
                }
            } finally {
                cleanup(directory);
            }
        }
    }

    private PlaceObjectTypeTag as2ButtonInstance(SWF swf) {
        int buttonId = swf.getCharacterByExportName("Button").getCharacterId();
        for (Tag tag : swf.getTags()) {
            if (tag instanceof PlaceObjectTypeTag && ((PlaceObjectTypeTag) tag).getCharacterId() == buttonId) {
                return (PlaceObjectTypeTag) tag;
            }
        }
        throw new AssertionError("Missing AS2 Button instance");
    }

    @Test
    public void retainsAs2UserHandlersAndFrameScriptsOutsideCompiledRuntime() throws Exception {
        SWF swf = movie("as2-ui");
        PlaceObjectTypeTag button = as2ButtonInstance(swf);
        com.jpexs.decompiler.flash.types.CLIPACTIONS actions = button.getClipActions();
        com.jpexs.decompiler.flash.types.CLIPACTIONRECORD release = new com.jpexs.decompiler.flash.types.CLIPACTIONRECORD(swf, (Tag) button);
        release.setParentClipActions(actions);
        release.eventFlags.clipEventRelease = true;
        release.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, release)
                .actionsFromString("on(release) { trace(\"Keep user handler\"); }", swf.getCharset()));
        actions.clipActionRecords.add(release);
        com.jpexs.decompiler.flash.tags.DoActionTag frame = new com.jpexs.decompiler.flash.tags.DoActionTag(swf);
        frame.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, frame)
                .actionsFromString("trace(\"Keep frame code\");", swf.getCharset()));
        swf.addTag(0, frame);
        Path directory = Files.createTempDirectory("ffdec-as2-handlers-");
        try {
            Map<String, byte[]> files = export(swf, directory, true, true);
            String source = text(files.get("DOMDocument.xml"));
            Assert.assertTrue(source.contains("Keep user handler"));
            Assert.assertTrue(source.contains("Keep frame code"));
            Assert.assertTrue(source.contains("DOMCompiledClipInstance"));
            Assert.assertFalse(source.contains("on(construct)"));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void leavesUnknownAs2ConstructCodeIntact() throws Exception {
        SWF swf = movie("as2-ui");
        com.jpexs.decompiler.flash.types.CLIPACTIONRECORD record = as2ButtonInstance(swf).getClipActions().clipActionRecords.get(0);
        record.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, record)
                .actionsFromString("on(construct) { label = computeLabel(); }", swf.getCharset()));
        ComponentConverter converter = new ComponentConverter(swf);
        Assert.assertFalse(converter.isComponent(swf.getCharacterByExportName("Button")));
        Assert.assertFalse(converter.isConsumedClipAction(record));
        Assert.assertFalse(converter.isBundledAs2Character(swf.getCharacterByExportName("__Packages.mx.controls.Button").getCharacterId()));
    }

    @Test
    public void restoresNestedAs2Instances() throws Exception {
        SWF swf = movie("as2-ui");
        PlaceObjectTypeTag button = as2ButtonInstance(swf);
        swf.removeTag((Tag) button);
        DefineSpriteTag wrapper = new DefineSpriteTag(swf);
        wrapper.frameCount = 1;
        wrapper.addTag((Tag) button);
        wrapper.addTag(new ShowFrameTag(swf));
        swf.addTag(wrapper);
        swf.addTag(new PlaceObject2Tag(swf, false, 30, wrapper.spriteId, new MATRIX(), null, -1, "wrapper", -1, null));
        Path directory = Files.createTempDirectory("ffdec-as2-nested-");
        try {
            Map<String, byte[]> files = export(swf, directory, true, true);
            boolean found = false;
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")) {
                    String source = text(file.getValue());
                    if (source.contains("DOMCompiledClipInstance")) {
                        Assert.assertTrue(source.contains("Sample Button"));
                        found = true;
                    }
                }
            }
            Assert.assertTrue(found);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void ordinaryAs2SymbolsWithComponentNamesAreNotConverted() throws Exception {
        SWF swf = new SWF();
        DefineSpriteTag symbol = new DefineSpriteTag(swf);
        symbol.frameCount = 1;
        symbol.addTag(new ShowFrameTag(swf));
        swf.addTag(symbol);
        com.jpexs.decompiler.flash.tags.ExportAssetsTag exports = new com.jpexs.decompiler.flash.tags.ExportAssetsTag(swf);
        exports.tags.add(symbol.spriteId);
        exports.names.add("Button");
        DefineSpriteTag classSymbol = new DefineSpriteTag(swf);
        swf.addTag(classSymbol);
        exports.tags.add(classSymbol.spriteId);
        exports.names.add("__Packages.mx.controls.Button");
        swf.addTag(exports);
        Assert.assertFalse(new ComponentConverter(swf).isComponent(symbol));
    }

    @Test
    public void retainsMixedAs2EventsAndHonorsScriptExportSetting() throws Exception {
        SWF swf = movie("as2-ui");
        com.jpexs.decompiler.flash.types.CLIPACTIONRECORD record = as2ButtonInstance(swf).getClipActions().clipActionRecords.get(0);
        record.eventFlags.clipEventEnterFrame = true;
        Assert.assertFalse(new ComponentConverter(swf).isConsumedClipAction(record));
        Path directory = Files.createTempDirectory("ffdec-as2-noscript-");
        try {
            Map<String, byte[]> files = export(movie("as2-ui"), directory, true, false);
            Assert.assertFalse(text(files.get("DOMDocument.xml")).contains("DOMCompiledClipItem"));
        } finally {
            cleanup(directory);
        }
    }

    @DataProvider(name = "jpegComponentPreviews")
    public Object[][] jpegComponentPreviews() {
        return new Object[][]{
            {"v1-ui", "FScrollBarSymbol", "component_v1_scrollbar.swf"},
            {"as2-ui", "Button", "component_as2_button.swf"}
        };
    }

    @Test(dataProvider = "jpegComponentPreviews")
    public void componentPreviewsRetainJpegTablesAndDecodeTheirBitmaps(String fixture, String linkage, String previewFile) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-component-jpeg-");
        try {
            SWF swf = movie(fixture);
            BufferedImage image = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    image.setRGB(x, y, 0xD0D0D0);
                }
            }
            ByteArrayOutputStream jpeg = new ByteArrayOutputStream();
            Assert.assertTrue(ImageIO.write(image, "jpeg", jpeg));
            DefineBitsTag bitmap = new DefineBitsTag(swf);
            bitmap.jpegData = new ByteArrayRange(jpeg.toByteArray());
            swf.addTag(0, bitmap);
            // DefineBits requires the tag even when the JPEG has its own tables.
            JPEGTablesTag tables = new JPEGTablesTag(swf);
            swf.addTag(0, tables);
            DefineSpriteTag component = (DefineSpriteTag) swf.getCharacterByExportName(linkage);
            component.addTag(0, new PlaceObject2Tag(swf, false, 300, bitmap.getCharacterId(),
                    new MATRIX(), null, -1, "jpegSkin", -1, null));
            byte[] original = bytes(swf);
            Map<String, byte[]> files = export(swf, directory, true, true);
            SWF preview = new SWF(new ByteArrayInputStream(files.get("bin/" + previewFile)), false);
            Assert.assertNotNull(preview.getJtt(), "Missing JPEG tables turns the component skin red");
            Assert.assertEquals(preview.getJtt().jpegData.getRangeData(), tables.jpegData.getRangeData());
            ImageTag restored = (ImageTag) preview.getCharacter(bitmap.getCharacterId());
            Assert.assertNotNull(restored);
            BufferedImage expected = ImageIO.read(bitmap.getOriginalImageData());
            BufferedImage actual = ImageIO.read(restored.getOriginalImageData());
            Assert.assertEquals(actual.getWidth(), expected.getWidth());
            Assert.assertEquals(actual.getHeight(), expected.getHeight());
            Assert.assertEquals(actual.getRGB(0, 0, actual.getWidth(), actual.getHeight(), null, 0, actual.getWidth()),
                    expected.getRGB(0, 0, expected.getWidth(), expected.getHeight(), null, 0, expected.getWidth()));
            Assert.assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    @DataProvider(name = "v1Inputs")
    public Object[][] v1Inputs() {
        return new Object[][]{{"v1-ui", false}, {"v1-ui", true}, {"v1-ui-mx", false}, {"v1-ui-mx", true}};
    }

    @Test(dataProvider = "v1Inputs")
    public void restoresFlashMxComponentsAndInitializationOrder(String fixture, boolean compressed) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-v1-ui-");
        try {
            SWF swf = movie(fixture);
            Assert.assertEquals(swf.version, 6, "MX references must retain case-insensitive SWF 6 semantics");
            byte[] original = bytes(swf);
            Map<String, byte[]> files = export(swf, directory, compressed, true);
            org.w3c.dom.Document main = document(files);
            Assert.assertEquals(ComponentConverter.elements(main.getDocumentElement(), "DOMComponentInstance").size(), 7);
            Assert.assertTrue(ComponentConverter.elements(main.getDocumentElement(), "DOMCompiledClipItem").isEmpty());
            java.util.Set<String> folders = new java.util.HashSet<>();
            for (org.w3c.dom.Element folder : ComponentConverter.elements(main.getDocumentElement(), "DOMFolderItem")) {
                String name = folder.getAttribute("name");
                Assert.assertTrue(name.startsWith("Component Assets"), name);
                if (name.contains("/")) {
                    Assert.assertTrue(folders.contains(name.substring(0, name.lastIndexOf('/'))), "Missing parent before " + name);
                }
                folders.add(name);
            }
            Map<String, org.w3c.dom.Element> byExport = new LinkedHashMap<>();
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (!file.getKey().startsWith("LIBRARY/") || !file.getKey().endsWith(".xml")) {
                    continue;
                }
                org.w3c.dom.Element item = xml(text(file.getValue())).getDocumentElement();
                if (item.hasAttribute("linkageIdentifier")) {
                    byExport.put(item.getAttribute("linkageIdentifier"), item);
                }
                Assert.assertFalse(item.hasAttribute("linkageClassName"), item.getAttribute("name"));
                if (!"DOMComponentItem".equals(item.getLocalName())) {
                    Assert.assertTrue(item.getAttribute("name").startsWith("Component Assets/"), item.getAttribute("name"));
                }
            }
            Map<String, ComponentDefinition> schemas = ComponentDefinition.load(swf, "as1_component_definitions.xml");
            for (ComponentDefinition definition : schemas.values()) {
                org.w3c.dom.Element item = byExport.get(definition.exportName);
                Assert.assertNotNull(item, definition.name);
                Assert.assertEquals(item.getLocalName(), "DOMComponentItem");
                Assert.assertEquals(item.getAttribute("name"), definition.name);
                Assert.assertEquals(item.getAttribute("customIconID"), definition.iconId);
                SWF preview = new SWF(new ByteArrayInputStream(files.get("bin/" + item.getAttribute("livePreviewScmHRef"))), false);
                Assert.assertFalse(preview.isAS3());
                Assert.assertEquals(preview.version, swf.version);
                Assert.assertEquals(preview.displayRect.getWidth(), definition.width * 20);
                Assert.assertEquals(ComponentConverter.elements(item, "parametersAsXML").get(0).getFirstChild().getNodeType(),
                        org.w3c.dom.Node.CDATA_SECTION_NODE);
            }
            int priority = 1;
            for (Tag tag : swf.getTags()) {
                if (!(tag instanceof com.jpexs.decompiler.flash.tags.DoInitActionTag)) {
                    continue;
                }
                String name = swf.getExportName(((com.jpexs.decompiler.flash.tags.DoInitActionTag) tag).spriteId);
                org.w3c.dom.Element item = byExport.get(name);
                Assert.assertNotNull(item, name);
                String source = ComponentConverter.elements(item, "script").get(0).getTextContent();
                Assert.assertTrue(source.startsWith("#initclip " + priority++ + "\n"), source.substring(0, Math.min(50, source.length())));
                if ("DOMComponentItem".equals(item.getLocalName())) {
                    Assert.assertTrue(source.contains("Object.registerClass"), name);
                }
            }
            String source = text(files.get("DOMDocument.xml"));
            Assert.assertTrue(source.contains(fixture.equals("v1-ui") ? "Sample PushButton" : "Hello"));
            if (fixture.equals("v1-ui")) {
                Assert.assertTrue(source.contains("First,Second,Third"));
            }
            Assert.assertFalse(source.contains("on(initialize)"));
            Assert.assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    @Test(dataProvider = "v1Inputs")
    public void binaryMxExportRetainsEditableComponentDefinitionsAndInstanceValues(String fixture, boolean scripts) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-v1-binary-mx-");
        try {
            SWF swf = movie(fixture);
            byte[] original = bytes(swf);
            XFLExportSettings settings = new XFLExportSettings();
            settings.compressed = true;
            settings.exportScript = scripts;
            Path file = directory.resolve("components.fla");
            swf.exportXfl(null, file.toString(), "components.swf", "Test", "Test", "1", false, FLAVersion.MX, settings, null);
            BinaryFlaDocument model = new BinaryFlaReader().read(file.toFile());
            Assert.assertTrue(model.getIssues().isEmpty(), model.getIssues().toString());
            Map<Long, String> components = new LinkedHashMap<>();
            for (FlaRecord descriptor : model.getLibraryDescriptors()) {
                if (Long.valueOf(1).equals(descriptor.get("legacyComponentEnabled"))) {
                    components.put(((Number) descriptor.get("libraryId")).longValue(), (String) descriptor.get("name"));
                    Assert.assertTrue(((Number) descriptor.get("legacyComponentIconId")).intValue() > 0);
                    Assert.assertFalse(mxEntries((FlaRecord) descriptor.get("properties")).isEmpty());
                    @SuppressWarnings("unchecked")
                    List<FlaRecord> movies = (List<FlaRecord>) descriptor.get("legacyMovies");
                    Assert.assertTrue(((BinaryData) movies.get(1).get("data")).size() > 0);
                }
            }
            Assert.assertEquals(components.size(), scripts ? 7 : 0);
            if (scripts) {
                for (BinaryFlaDocument.Page page : model.getPages()) {
                    List<FlaRecord> nested = new ArrayList<>();
                    mxInstances(page.getRoot(), nested);
                    for (FlaRecord instance : nested) {
                        FlaRecord actions = (FlaRecord) instance.get("actions");
                        Assert.assertTrue(actions == null || !String.valueOf(actions.get("source")).contains("on(initialize)"),
                                "Empty private SmartClip initializers must not become invalid MX source");
                    }
                }
                List<FlaRecord> instances = new ArrayList<>();
                for (FlaRecord scene : model.getSceneDescriptors()) {
                    mxInstances(model.getStream((String) scene.get("streamName")).getRoot(), instances);
                }
                int count = 0;
                for (FlaRecord instance : instances) {
                    String name = components.get(((Number) instance.get("libraryId")).longValue());
                    if (name == null) {
                        continue;
                    }
                    count++;
                    for (FlaRecord parameter : mxEntries((FlaRecord) instance.get("properties"))) {
                        FlaRecord value = (FlaRecord) parameter.get("value");
                        if ("PushButton".equals(name) && "label".equals(parameter.get("variableName"))) {
                            Assert.assertEquals(mxEntries(value).get(0).get("value"), fixture.equals("v1-ui") ? "Sample PushButton" : "Hello");
                        }
                        if (fixture.equals("v1-ui") && "labels".equals(parameter.get("variableName"))) {
                            Assert.assertEquals(((Number) value.get("type")).intValue(), 2);
                            Assert.assertEquals(mxEntries(value).size(), 3);
                            Assert.assertEquals(mxEntries(value).get(1).get("value"), "Second");
                        }
                    }
                }
                Assert.assertEquals(count, 7);
            }
            Assert.assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void binaryMx2004ExportRetainsCompiledAs2ComponentsAndParameters() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-as2-binary-mx2004-");
        try {
            SWF swf = movie("as2-ui");
            byte[] original = bytes(swf);
            XFLExportSettings settings = new XFLExportSettings();
            settings.compressed = true;
            Path file = directory.resolve("components.fla");
            swf.exportXfl(null, file.toString(), "components.swf", "Test", "Test", "1", false, FLAVersion.MX2004, settings, null);
            BinaryFlaDocument model = new BinaryFlaReader().read(file.toFile());
            Assert.assertTrue(model.getIssues().isEmpty(), model.getIssues().toString());
            Assert.assertEquals(model.getMediaDescriptors().stream().filter(r -> "CSwfPage".equals(r.getClassName())).count(), 22L);
            List<FlaRecord> instances = new ArrayList<>();
            for (FlaRecord scene : model.getSceneDescriptors()) {
                compiledInstances(model.getStream((String) scene.get("streamName")).getRoot(), instances);
            }
            Assert.assertEquals(instances.size(), 22);
            FlaRecord button = instances.stream().filter(r -> "sampleButton".equals(r.get("instanceName"))).findFirst().get();
            FlaRecord label = mxEntries((FlaRecord) button.get("properties")).stream()
                    .filter(r -> "label".equals(r.get("variableName"))).findFirst().get();
            Assert.assertEquals(mxEntries((FlaRecord) label.get("value")).get(0).get("value"), "Sample Button");
            Assert.assertEquals(((Number) button.get("transformationPointX")).intValue(), button.getMatrix().getWords()[4]);
            Assert.assertEquals(((Number) button.get("transformationPointY")).intValue(), button.getMatrix().getWords()[5]);
            Assert.assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    private void compiledInstances(FlaRecord record, List<FlaRecord> instances) {
        if ("CPicSwf".equals(record.getClassName())) {
            instances.add(record);
        }
        for (FlaRecord child : record.getChildren()) {
            compiledInstances(child, instances);
        }
    }

    @SuppressWarnings("unchecked")
    private List<FlaRecord> mxEntries(FlaRecord r) {
        return (List<FlaRecord>) r.get("entries");
    }

    @Test
    public void privateMxInitializersAreRemovedOnlyWhenEmptyAndUnmixed() throws Exception {
        SWF swf = movie("v1-ui-mx");
        ComponentConverter converter = new ComponentConverter(swf);
        com.jpexs.decompiler.flash.types.CLIPACTIONRECORD empty = null;
        for (com.jpexs.decompiler.flash.tags.base.ASMSource source : swf.getASMs(false).values()) {
            if (source instanceof com.jpexs.decompiler.flash.types.CLIPACTIONRECORD) {
                com.jpexs.decompiler.flash.types.CLIPACTIONRECORD record = (com.jpexs.decompiler.flash.types.CLIPACTIONRECORD) source;
                if (record.eventFlags.clipEventInitialize && record.getActions().stream().allMatch(action -> action.getActionCode() == 0)) {
                    empty = record;
                    break;
                }
            }
        }
        Assert.assertNotNull(empty, "Native MX must exercise an empty private initializer");
        Assert.assertTrue(converter.isConsumedClipAction(empty));
        empty.eventFlags.clipEventLoad = true;
        Assert.assertFalse(new ComponentConverter(swf).isConsumedClipAction(empty));
        empty.eventFlags.clipEventLoad = false;
        empty.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, empty)
                .actionsFromString("on(initialize){trace('Keep private MX handler');}", swf.getCharset()));
        SWF.uncache(empty);
        Assert.assertFalse(new ComponentConverter(swf).isConsumedClipAction(empty));
    }

    private void mxInstances(FlaRecord r, List<FlaRecord> instances) {
        if (r.getClassName().equals("CPicSprite")) {
            instances.add(r);
        }
        for (FlaRecord child : r.getChildren()) {
            mxInstances(child, instances);
        }
    }

    @Test(dataProvider = "formats")
    public void restoresOriginalMxPushButton(boolean compressed) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-v1-button-");
        try {
            Map<String, byte[]> files = export(movie("v1-button"), directory, compressed, true);
            Assert.assertEquals(ComponentConverter.elements(document(files).getDocumentElement(), "DOMComponentInstance").size(), 1);
            org.w3c.dom.Element button = xml(text(files.get("LIBRARY/PushButton.xml"))).getDocumentElement();
            Assert.assertEquals(button.getLocalName(), "DOMComponentItem");
            Assert.assertEquals(button.getAttribute("customIconID"), "1");
            Assert.assertEquals(button.getAttribute("linkageIdentifier"), "FPushButtonSymbol");
            Assert.assertTrue(text(files.get("DOMDocument.xml")).contains("defaultValue=\"Hello\""));
            Assert.assertFalse(files.containsKey("LIBRARY/CheckBox.xml"));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void unknownMxInitializeCodeKeepsItsRegistrationAndUserCode() throws Exception {
        SWF swf = movie("v1-button");
        PlaceObjectTypeTag button = null;
        for (Tag tag : swf.getTags()) {
            if (tag instanceof PlaceObjectTypeTag) {
                button = (PlaceObjectTypeTag) tag;
                break;
            }
        }
        Assert.assertNotNull(button);
        com.jpexs.decompiler.flash.types.CLIPACTIONRECORD record = button.getClipActions().clipActionRecords.get(0);
        record.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, record)
                .actionsFromString("on(initialize){ label = 'Hello'; customMxInit(); }", swf.getCharset()));
        Path directory = Files.createTempDirectory("ffdec-v1-custom-init-");
        try {
            Map<String, byte[]> files = export(swf, directory, true, true);
            Assert.assertTrue(ComponentConverter.elements(document(files).getDocumentElement(), "DOMComponentInstance").isEmpty());
            Assert.assertTrue(text(files.get("DOMDocument.xml")).contains("customMxInit()"));
            boolean registered = false;
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")) {
                    org.w3c.dom.Element item = xml(text(file.getValue())).getDocumentElement();
                    if ("FPushButtonSymbol".equals(item.getAttribute("linkageIdentifier"))) {
                        Assert.assertFalse(item.hasAttribute("linkageClassName"));
                        Assert.assertTrue(text(file.getValue()).contains("Object.registerClass"));
                        registered = true;
                    }
                }
            }
            Assert.assertTrue(registered);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void v1RequiresMatchingClassRegistrationAndHonorsScriptExportSetting() throws Exception {
        SWF swf = movie("v1-button");
        int id = swf.getCharacterByExportName("FPushButtonSymbol").getCharacterId();
        for (Tag tag : swf.getTags()) {
            if (tag instanceof com.jpexs.decompiler.flash.tags.DoInitActionTag
                    && ((com.jpexs.decompiler.flash.tags.DoInitActionTag) tag).spriteId == id) {
                com.jpexs.decompiler.flash.tags.DoInitActionTag action = (com.jpexs.decompiler.flash.tags.DoInitActionTag) tag;
                action.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, action)
                        .actionsFromString("Object.registerClass('FPushButtonSymbol', CustomClass);", swf.getCharset()));
            }
        }
        Assert.assertFalse(new ComponentConverter(swf).isComponent(swf.getCharacter(id)));
        Path directory = Files.createTempDirectory("ffdec-v1-no-script-");
        try {
            Map<String, byte[]> files = export(movie("v1-button"), directory, true, false);
            Assert.assertTrue(ComponentConverter.elements(document(files).getDocumentElement(), "DOMComponentInstance").isEmpty());
            Assert.assertFalse(text(files.get("DOMDocument.xml")).contains("parametersAsXML"));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void nestedMxComponentKeepsUserHandlersOutsideItsPreview() throws Exception {
        SWF swf = movie("v1-button");
        PlaceObjectTypeTag button = null;
        for (Tag tag : swf.getTags()) {
            if (tag instanceof PlaceObjectTypeTag) {
                button = (PlaceObjectTypeTag) tag;
                break;
            }
        }
        Assert.assertNotNull(button);
        com.jpexs.decompiler.flash.types.CLIPACTIONS actions = button.getClipActions();
        com.jpexs.decompiler.flash.types.CLIPACTIONRECORD release = new com.jpexs.decompiler.flash.types.CLIPACTIONRECORD(swf, (Tag) button);
        release.setParentClipActions(actions);
        release.eventFlags.clipEventRelease = true;
        release.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, release)
                .actionsFromString("on(release){ trace('Keep MX handler'); }", swf.getCharset()));
        actions.clipActionRecords.add(release);
        swf.removeTag((Tag) button);
        DefineSpriteTag wrapper = new DefineSpriteTag(swf);
        wrapper.frameCount = 1;
        wrapper.addTag((Tag) button);
        wrapper.addTag(new ShowFrameTag(swf));
        swf.addTag(wrapper);
        swf.addTag(new PlaceObject2Tag(swf, false, 30, wrapper.spriteId, new MATRIX(), null, -1, "wrapper", -1, null));
        com.jpexs.decompiler.flash.tags.DoActionTag frame = new com.jpexs.decompiler.flash.tags.DoActionTag(swf);
        frame.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, frame)
                .actionsFromString("trace('Keep MX frame');", swf.getCharset()));
        swf.addTag(0, frame);
        Path directory = Files.createTempDirectory("ffdec-v1-nested-");
        try {
            Map<String, byte[]> files = export(swf, directory, true, true);
            Assert.assertTrue(text(files.get("DOMDocument.xml")).contains("Keep MX frame"));
            boolean nested = false;
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (file.getKey().startsWith("LIBRARY/") && file.getKey().endsWith(".xml")) {
                    String source = text(file.getValue());
                    if (source.contains("DOMComponentInstance") && source.contains("Keep MX handler")) {
                        Assert.assertTrue(source.contains("Hello"));
                        Assert.assertFalse(source.contains("on(initialize)"));
                        nested = true;
                    }
                }
            }
            Assert.assertTrue(nested);
            org.w3c.dom.Element item = xml(text(files.get("LIBRARY/PushButton.xml"))).getDocumentElement();
            SWF preview = new SWF(new ByteArrayInputStream(files.get("bin/" + item.getAttribute("livePreviewScmHRef"))), false);
            Assert.assertNull(preview.getCharacter(wrapper.spriteId));
            for (Tag tag : preview.getTags()) {
                if (tag instanceof com.jpexs.decompiler.flash.tags.DoActionTag) {
                    Assert.assertFalse(java.util.Arrays.equals(tag.getData(), frame.getData()));
                }
            }
        } finally {
            cleanup(directory);
        }
    }

    @Test(dataProvider = "formats")
    public void restoresFlash5SmartClipsFromNativeSwf(boolean compressed) throws Exception {
        Path directory = Files.createTempDirectory("ffdec-f5-smartclips-");
        try {
            SWF swf = movie("f5-smartclips");
            byte[] original = bytes(swf);
            Assert.assertEquals(swf.version, 5);
            Map<String, byte[]> files = export(swf, directory, compressed, true);
            org.w3c.dom.Element document = xml(text(files.get("DOMDocument.xml"))).getDocumentElement();
            List<org.w3c.dom.Element> instances = ComponentConverter.elements(document, "DOMComponentInstance");
            Assert.assertEquals(instances.size(), 3);
            for (String name : new String[]{"CheckBox", "Menu", "RadioButton"}) {
                org.w3c.dom.Element item = xml(text(files.get("LIBRARY/" + name + ".xml"))).getDocumentElement();
                Assert.assertEquals(item.getLocalName(), "DOMComponentItem");
                Assert.assertEquals(item.getAttribute("requiredMinimumPlayerVersion"), "5");
                Assert.assertTrue(item.getAttribute("symbolDescription").contains("Summary"));
                Assert.assertFalse(item.hasAttribute("linkageClassName"));
            }
            String scene = text(files.get("DOMDocument.xml"));
            Assert.assertTrue(scene.contains("checkbox1"));
            Assert.assertTrue(scene.contains("radioBox1"));
            Assert.assertTrue(scene.contains("defaultValue1,defaultValue2,defaultValue3,defaultValue4,defaultValue5"));
            Assert.assertTrue(scene.contains("enumeration=\"true,false\""));
            Assert.assertTrue(scene.contains("enumeration=\"Auto,Mac,Win\""));
            Assert.assertFalse(scene.contains("onClipEvent(load)"), "Do not duplicate the generated parameter initializer");
            int labels = 0;
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                if (!file.getKey().startsWith("LIBRARY/") || !file.getKey().endsWith(".xml")) {
                    continue;
                }
                org.w3c.dom.Element item = xml(text(file.getValue())).getDocumentElement();
                for (org.w3c.dom.Element field : ComponentConverter.elements(item, "DOMDynamicText")) {
                    String label = ComponentConverter.elements(field, "characters").get(0).getTextContent();
                    Assert.assertTrue(java.util.Arrays.asList("Checkbox", "Radio Button", "List Value").contains(label));
                    for (org.w3c.dom.Element format : ComponentConverter.elements(field, "DOMTextAttrs")) {
                        double size = Double.parseDouble(format.getAttribute("size"));
                        Assert.assertTrue(size == 12 || size == 13, "Editor label must retain its original font size: " + size);
                    }
                    labels++;
                }
            }
            Assert.assertTrue(labels >= 12, "All native text fields must remain visible, including repeated keyframes");
            Assert.assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void binaryFlash5ExportRestoresSmartClipParameters() throws Exception {
        Path directory = Files.createTempDirectory("ffdec-f5-binary-smartclips-");
        try {
            SWF swf = movie("f5-smartclips");
            byte[] original = bytes(swf);
            Path file = directory.resolve("smartclips.fla");
            swf.exportXfl(null, file.toString(), "smartclips.swf", "Test", "Test", "1", false,
                    FLAVersion.F5, new XFLExportSettings(), null);
            BinaryFlaDocument output = new BinaryFlaReader().read(file.toFile());
            Map<Long, String> names = new LinkedHashMap<>();
            int definitions = 0;
            for (FlaRecord descriptor : output.getLibraryDescriptors()) {
                names.put(((Number) descriptor.get("libraryId")).longValue(), (String) descriptor.get("name"));
                List<FlaRecord> properties = mxEntries((FlaRecord) descriptor.get("properties"));
                if (!properties.isEmpty()) {
                    definitions++;
                    Assert.assertEquals(properties.size(), "Menu".equals(descriptor.get("name")) ? 2 : 4);
                }
            }
            Assert.assertEquals(definitions, 3);
            List<FlaRecord> instances = new ArrayList<>();
            for (FlaRecord scene : output.getSceneDescriptors()) {
                mxInstances(output.getStream((String) scene.get("streamName")).getRoot(), instances);
            }
            Assert.assertEquals(instances.size(), 3);
            for (FlaRecord instance : instances) {
                String name = names.get(((Number) instance.get("libraryId")).longValue());
                FlaRecord properties = (FlaRecord) instance.get("properties");
                Assert.assertEquals(mxEntries(properties).size(), "Menu".equals(name) ? 2 : 4);
                Assert.assertEquals(((Number) properties.get("revision")).intValue(), 1);
                Assert.assertEquals(instance.get("legacyParametersAreLocked"), 1L);
                for (FlaRecord parameter : mxEntries(properties)) {
                    FlaRecord value = (FlaRecord) parameter.get("value");
                    if ("items".equals(parameter.get("name"))) {
                        Assert.assertEquals(((Number) value.get("type")).intValue(), 2);
                        Assert.assertEquals(mxEntries(value).size(), 5);
                        Assert.assertEquals(mxEntries(value).get(4).get("value"), "defaultValue5");
                    }
                    if ("checked".equals(parameter.get("name"))) {
                        Assert.assertEquals(((Number) value.get("type")).intValue(), 4);
                        Assert.assertEquals(((Number) value.get("selectedIndex")).intValue(), 1);
                    }
                }
            }
            Assert.assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void flash5SmartClipRecognitionPreservesUnknownRuntimeAndInitializers() throws Exception {
        SWF swf = movie("f5-smartclips");
        DefineSpriteTag checkbox = (DefineSpriteTag) swf.getCharacter(11);
        Assert.assertTrue(new ComponentConverter(swf).isComponent(checkbox));
        com.jpexs.decompiler.flash.tags.DoActionTag custom = new com.jpexs.decompiler.flash.tags.DoActionTag(swf);
        custom.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, custom)
                .actionsFromString("trace('Custom runtime');", swf.getCharset()));
        checkbox.addTag(0, custom);
        Assert.assertFalse(new ComponentConverter(swf).isComponent(checkbox));
        Path directory = Files.createTempDirectory("ffdec-f5-custom-smartclip-");
        try {
            Map<String, byte[]> files = export(swf, directory, false, true);
            Assert.assertTrue(text(files.get("DOMDocument.xml")).contains("checkbox1"));
            Assert.assertTrue(files.values().stream().anyMatch(data -> text(data).contains("Custom runtime")));
            Assert.assertFalse(text(files.get("DOMDocument.xml")).contains("libraryItemName=\"CheckBox\""));
            files = export(movie("f5-smartclips"), directory, true, false);
            Assert.assertFalse(text(files.get("DOMDocument.xml")).contains("DOMComponentInstance"));
        } finally {
            cleanup(directory);
        }
    }

    @Test
    public void flash5SmartClipsUseEditedInstanceValuesInsideNestedSymbols() throws Exception {
        SWF swf = movie("f5-smartclips");
        DefineSpriteTag wrapper = new DefineSpriteTag(swf);
        wrapper.frameCount = 1;
        for (Tag tag : new ArrayList<>(swf.getTags().toArrayList())) {
            if (!(tag instanceof PlaceObjectTypeTag)) {
                continue;
            }
            PlaceObjectTypeTag place = (PlaceObjectTypeTag) tag;
            if (place.getCharacterId() == 11) {
                com.jpexs.decompiler.flash.types.CLIPACTIONRECORD initializer = place.getClipActions().clipActionRecords.get(0);
                initializer.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, initializer)
                        .actionsFromString("onClipEvent(load){ _name='editedCheckbox'; checked='true'; label='Edited label'; style='Mac'; }", swf.getCharset()));
            }
            if (place.getCharacterId() == 40) {
                com.jpexs.decompiler.flash.types.CLIPACTIONRECORD initializer = place.getClipActions().clipActionRecords.get(0);
                initializer.setActions(new com.jpexs.decompiler.flash.action.parser.script.ActionScript2Parser(swf, initializer)
                        .actionsFromString("onClipEvent(load){ items=new Array(); items[0]='First'; items[1]='Second'; style='Win'; }", swf.getCharset()));
            }
            swf.removeTag(tag);
            wrapper.addTag(tag);
        }
        wrapper.addTag(new ShowFrameTag(swf));
        swf.addTag(wrapper);
        swf.addTag(0, new PlaceObject2Tag(swf, false, 1, wrapper.spriteId, new MATRIX(), null, -1, "wrapper", -1, null));
        Path directory = Files.createTempDirectory("ffdec-f5-edited-smartclips-");
        try {
            byte[] original = bytes(swf);
            Map<String, byte[]> files = export(swf, directory, true, true);
            String nested = files.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("LIBRARY/") && entry.getKey().endsWith(".xml"))
                    .map(entry -> text(entry.getValue())).filter(source -> source.contains("DOMComponentInstance"))
                    .findFirst().orElseThrow(() -> new AssertionError("Missing nested Smart Clips"));
            Assert.assertTrue(nested.contains("editedCheckbox"));
            Assert.assertTrue(nested.contains("Edited label"));
            Assert.assertTrue(nested.contains("defaultValue=\"true\""));
            Assert.assertTrue(nested.contains("defaultValue=\"Mac\""));
            Assert.assertTrue(nested.contains("defaultValue=\"First,Second\""));
            Assert.assertTrue(nested.contains("defaultValue=\"Win\""));
            Assert.assertFalse(nested.contains("onClipEvent(load)"));
            Assert.assertEquals(bytes(swf), original);
        } finally {
            cleanup(directory);
        }
    }

    @Test(dataProvider = "formats")
    public void deviceFontLabelsKeepTheirSizeDuringNormalization(boolean inPlace) throws Exception {
        SWF swf = movie("f5-smartclips");
        byte[] original = bytes(swf);
        Map<Integer, com.jpexs.decompiler.flash.tags.base.FontTag> fonts = new LinkedHashMap<>();
        Map<Integer, com.jpexs.decompiler.flash.tags.base.TextTag> texts = new LinkedHashMap<>();
        new com.jpexs.decompiler.flash.FontNormalizer().normalizeFonts(swf, inPlace, fonts, texts);
        int count = 0;
        for (com.jpexs.decompiler.flash.tags.base.CharacterTag character : swf.getCharacters(false).values()) {
            if (character instanceof com.jpexs.decompiler.flash.tags.DefineEditTextTag) {
                com.jpexs.decompiler.flash.tags.DefineEditTextTag field = (com.jpexs.decompiler.flash.tags.DefineEditTextTag)
                        texts.getOrDefault(character.getCharacterId(), (com.jpexs.decompiler.flash.tags.DefineEditTextTag) character);
                Assert.assertTrue(field.fontHeight == 240 || field.fontHeight == 260);
                count++;
            }
        }
        Assert.assertEquals(count, 12);
        Assert.assertEquals(bytes(swf), original);
    }

}

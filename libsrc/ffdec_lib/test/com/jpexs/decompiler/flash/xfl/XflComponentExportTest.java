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
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.DefineSceneAndFrameLabelDataTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.SymbolClassTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.types.MATRIX;
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

}

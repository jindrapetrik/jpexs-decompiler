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

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.DefineEditTextTag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.SetTabIndexTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.flash.fla.reader.BinaryFlaDocument;
import com.jpexs.flash.fla.reader.BinaryFlaReader;
import com.jpexs.flash.fla.reader.FlaRecord;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.testng.Assert.*;

public class XflVersionedExportTest {

    private SWF movie() {
        SWF swf = new SWF();
        swf.version = 10;
        swf.frameRate = 24;
        swf.frameCount = 1;
        swf.displayRect = new RECT(0, 11000, 0, 8000);
        return swf;
    }

    private DefineEditTextTag text(SWF swf) {
        DefineEditTextTag text = new DefineEditTextTag(swf);
        text.bounds = new RECT(0, 2000, 0, 600);
        text.hasText = true;
        text.initialText = "Moving text";
        text.readOnly = true;
        text.hasTextColor = true;
        text.textColor = new RGBA(0, 0, 0, 255);
        swf.addTag(text);
        return text;
    }

    private void export(SWF swf, Path file, FLAVersion version) throws Exception {
        XFLExportSettings settings = new XFLExportSettings();
        settings.compressed = true;
        settings.exportScript = false;
        swf.exportXfl(new AbortRetryIgnoreHandler() {
            @Override
            public int handle(Throwable error) {
                throw new AssertionError(error);
            }

            @Override
            public AbortRetryIgnoreHandler getNewInstance() {
                return this;
            }
        }, file.toString(), "test.swf", "Test", "Test", "1", false, version, settings, null);
    }

    private SWF animation(boolean curved) {
        SWF swf = movie();
        DefineEditTextTag text = text(swf);
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        sprite.frameCount = 1;
        sprite.addTag(new PlaceObject2Tag(swf, false, 1, text.characterID, new MATRIX(), null, -1, null, -1, null));
        sprite.addTag(new ShowFrameTag(swf));
        swf.addTag(sprite);
        swf.frameCount = 9;
        for (int i = 0; i < swf.frameCount; i++) {
            MATRIX matrix = new MATRIX();
            matrix.translateX = 200 * i;
            matrix.translateY = curved ? 60 * i * i : 0;
            swf.addTag(new PlaceObject2Tag(swf, i > 0, 1, i == 0 ? sprite.spriteId : -1,
                    matrix, null, -1, null, -1, null));
            swf.addTag(new ShowFrameTag(swf));
        }
        return swf;
    }

    private List<FlaRecord> mainFrames(Path file) throws Exception {
        BinaryFlaDocument document = new BinaryFlaReader().read(file.toFile());
        assertTrue(document.getIssues().isEmpty(), document.getIssues().toString());
        List<FlaRecord> frames = new ArrayList<>();
        for (FlaRecord layer : document.getPages().get(0).getRoot().getChildren()) {
            frames.addAll(layer.getChildren());
        }
        return frames;
    }

    private Document document(Path file, String entry) throws Exception {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(zip.getInputStream(zip.getEntry(entry)));
        }
    }

    @DataProvider(name = "textOptions")
    public Object[][] textOptions() {
        return new Object[][]{{true, true}, {true, false}, {false, true}, {false, false}};
    }

    @Test(dataProvider = "textOptions")
    public void keepsDynamicAndInputTextAutoSize(boolean readOnly, boolean autoSize) throws Exception {
        SWF swf = movie();
        DefineEditTextTag text = text(swf);
        text.readOnly = readOnly;
        text.autoSize = autoSize;
        swf.addTag(new PlaceObject2Tag(swf, false, 1, text.characterID, new MATRIX(), null, -1, null, -1, null));
        swf.addTag(new ShowFrameTag(swf));
        Path file = Files.createTempFile("text-auto-size-", ".fla");
        try {
            export(swf, file, FLAVersion.CS6);
            Element element = (Element) document(file, "DOMDocument.xml")
                    .getElementsByTagName(readOnly ? "DOMDynamicText" : "DOMInputText").item(0);
            assertNotNull(element);
            assertEquals("true".equals(element.getAttribute("autoExpand")), autoSize);
            export(swf, file, FLAVersion.MX);
            FlaRecord output = mainFrames(file).get(0).getChildren().get(0);
            assertEquals(((Number) output.get("autoExpand")).intValue(), autoSize ? 1 : 0);
            assertEquals(text.autoSize, autoSize, "Export must not modify the source flag");
        } finally {
            Files.deleteIfExists(file);
        }
    }

    private void tabIndex(SWF swf, int depth, int value) {
        SetTabIndexTag tag = new SetTabIndexTag(swf);
        tag.depth = depth;
        tag.tabIndex = value;
        swf.addTag(tag);
    }

    @DataProvider(name = "tabIndexInstances")
    public Object[][] tabIndexInstances() {
        return new Object[][]{{false, false}, {false, true}, {true, false}, {true, true}};
    }

    @Test(dataProvider = "tabIndexInstances")
    public void tabOrderFollowsThePlacedInstance(boolean symbol, boolean named) throws Exception {
        SWF swf = movie();
        swf.frameCount = 5;
        DefineEditTextTag text = text(swf);
        int id = text.characterID;
        if (symbol) {
            DefineSpriteTag sprite = new DefineSpriteTag(swf);
            sprite.frameCount = 1;
            sprite.addTag(new PlaceObject2Tag(swf, false, 1, id, new MATRIX(), null, -1, null, -1, null));
            sprite.addTag(new ShowFrameTag(swf));
            swf.addTag(sprite);
            id = sprite.spriteId;
        }
        swf.addTag(new PlaceObject2Tag(swf, false, 1, id, new MATRIX(), null, -1, named ? "first" : null, -1, null));
        tabIndex(swf, 1, 17);
        tabIndex(swf, 99, 99); // A different, empty depth must not change the instance.
        swf.addTag(new ShowFrameTag(swf));
        swf.addTag(new PlaceObject2Tag(swf, true, 1, -1, new MATRIX(), null, -1, null, -1, null));
        tabIndex(swf, 1, 5);
        swf.addTag(new ShowFrameTag(swf));
        swf.addTag(new ShowFrameTag(swf)); // Retain the index across a held frame.
        RemoveObject2Tag remove = new RemoveObject2Tag(swf);
        remove.depth = 1;
        swf.addTag(remove);
        swf.addTag(new PlaceObject2Tag(swf, false, 1, id, new MATRIX(), null, -1, null, -1, null));
        swf.addTag(new ShowFrameTag(swf));
        tabIndex(swf, 1, 0); // Zero is an explicit tab index, not an absent value.
        swf.addTag(new ShowFrameTag(swf));
        String[] expected = {"17", "5", "5", "", "0"};
        Path file = Files.createTempFile("tab-order-", ".fla");
        try {
            export(swf, file, FLAVersion.CS6);
            NodeList frames = document(file, "DOMDocument.xml").getElementsByTagName("DOMFrame");
            int count = 0;
            for (int i = 0; i < frames.getLength(); i++) {
                Element frame = (Element) frames.item(i);
                Element instance = (Element) frame.getElementsByTagName(symbol ? "DOMSymbolInstance" : "DOMDynamicText").item(0);
                assertNotNull(instance);
                int duration = frame.hasAttribute("duration") ? Integer.parseInt(frame.getAttribute("duration")) : 1;
                for (int j = 0; j < duration; j++) {
                    assertEquals(instance.getAttribute("tabIndex"), expected[count++]);
                }
            }
            assertEquals(count, expected.length);
            export(swf, file, FLAVersion.MX2004);
            count = 0;
            for (FlaRecord frame : mainFrames(file)) {
                FlaRecord accessibility = (FlaRecord) frame.getChildren().get(0).get("accessibility");
                for (int i = 0; i < ((Number) frame.get("duration")).intValue(); i++) {
                    Object value = accessibility == null ? null : accessibility.get("tabIndex");
                    assertEquals(value == null ? "" : value, expected[count++]);
                }
            }
            assertEquals(count, expected.length);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @DataProvider(name = "legacyTargets")
    public Object[][] legacyTargets() {
        return new Object[][]{{FLAVersion.F1}, {FLAVersion.F2}, {FLAVersion.F3}, {FLAVersion.F4},
            {FLAVersion.F5}, {FLAVersion.MX}, {FLAVersion.MX2004}, {FLAVersion.F8}, {FLAVersion.CS3}};
    }

    @Test(dataProvider = "legacyTargets")
    public void legacyTargetsKeepEverySampleOfModernMotion(FLAVersion version) throws Exception {
        Path file = Files.createTempFile("legacy-motion-", ".fla");
        try {
            export(animation(true), file, version);
            List<FlaRecord> frames = mainFrames(file);
            assertEquals(frames.size(), 9);
            for (int i = 0; i < frames.size(); i++) {
                FlaRecord frame = frames.get(i);
                assertEquals(((Number) frame.get("duration")).intValue(), 1);
                FlaRecord instance = frame.getChildren().get(0);
                assertEquals(instance.getMatrix().getTxTwips(), 200 * i);
                assertEquals(instance.getMatrix().getTyTwips(), 60 * i * i);
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void cs4StillRecoversModernMotion() throws Exception {
        Path file = Files.createTempFile("cs4-motion-", ".fla");
        try {
            export(animation(true), file, FLAVersion.CS4);
            List<FlaRecord> frames = mainFrames(file);
            assertEquals(frames.size(), 1);
            assertEquals(((Number) frames.get(0).get("duration")).intValue(), 9);
            assertNotNull(frames.get(0).get("motionXml"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void legacyTargetsStillRecoverClassicMotion() throws Exception {
        Path file = Files.createTempFile("classic-motion-", ".fla");
        try {
            export(animation(false), file, FLAVersion.CS3);
            List<FlaRecord> frames = mainFrames(file);
            assertEquals(frames.size(), 2);
            assertEquals(((Number) frames.get(0).get("duration")).intValue(), 8);
            assertEquals(((Number) frames.get(0).get("keyMode")).intValue() & 0xff, 1);
            assertEquals(frames.get(1).getChildren().get(0).getMatrix().getTxTwips(), 1600);
        } finally {
            Files.deleteIfExists(file);
        }
    }
}

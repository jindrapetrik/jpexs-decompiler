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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
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

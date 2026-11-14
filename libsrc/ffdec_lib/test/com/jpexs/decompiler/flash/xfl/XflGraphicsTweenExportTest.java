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
import com.jpexs.decompiler.flash.ReadOnlyTagList;
import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.PlaceObjectTypeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.MORPHGRADIENT;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
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

public class XflGraphicsTweenExportTest {
    private SWF fixture() throws Exception {
        // Only the seven regression intervals, their shape definitions and one
        // bitmap, extracted from graphics.swf. No fonts, scripts or other scenes.
        try (FileInputStream in = new FileInputStream("testdata/xfl/graphics-tweens.swf")) {
            return new SWF(in, false);
        }
    }

    @Test
    public void recoversEveryGraphicsTweenIncludingMergedGradientEndpoints() throws Exception {
        SWF swf = fixture();
        ByteArrayOutputStream before = new ByteArrayOutputStream();
        swf.saveTo(before);
        Path output = Files.createTempFile("graphics-tweens-", ".fla");
        try {
            swf.exportFla(new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable error) {
                    throw new AssertionError(error);
                }

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }
            }, output.toString(), "graphics.swf", "Test", "Test", "1", false, FLAVersion.CS6, null);
            try (ZipFile zip = new ZipFile(output.toFile())) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(zip.getInputStream(zip.getEntry("DOMDocument.xml")));
                NodeList frames = document.getElementsByTagName("DOMFrame");
                int[] starts = {17, 21, 25, 29, 33, 37, 61};
                int[] counts = new int[starts.length];
                for (int i = 0; i < frames.getLength(); i++) {
                    Element frame = (Element) frames.item(i);
                    if (!frame.hasAttribute("tweenType")) {
                        continue;
                    }
                    int index = Integer.parseInt(frame.getAttribute("index"));
                    int range = -1;
                    for (int k = 0; k < starts.length; k++) {
                        if (starts[k] == index) {
                            range = k;
                        }
                    }
                    assertTrue(range >= 0, "Unexpected tween at " + index);
                    counts[range]++;
                    assertEquals(frame.getAttribute("duration"), "3", "Include the real ordinary-shape endpoint");
                    assertEquals(frame.getAttribute("acceleration"), "0");
                    assertEquals(frame.getAttribute("tweenType"), index == 21 ? "motion" : "shape");
                    if (index == 21) {
                        assertEquals(frame.getAttribute("motionTweenRotate"), "auto");
                    } else {
                        assertTrue(frame.getElementsByTagName("MorphHint").getLength() > 0);
                    }
                }
                assertEquals(counts, new int[]{1, 1, 1, 1, 1, 1, 2}, "Keep both radial/focal tweens when their endpoints were merged");
                NodeList layers = document.getElementsByTagName("DOMLayer");
                for (int l = 0; l < layers.getLength(); l++) {
                    Element last = null;
                    NodeList layerFrames = ((Element) layers.item(l)).getElementsByTagName("DOMFrame");
                    for (int f = 0; f < layerFrames.getLength(); f++) {
                        Element frame = (Element) layerFrames.item(f);
                        if (Integer.parseInt(frame.getAttribute("index")) <= 65) {
                            last = frame;
                        }
                    }
                    assertNotNull(last);
                    assertEquals(last.getElementsByTagName("DOMShape").getLength(), 0, "Do not leak a split endpoint onto frame 66");
                    assertEquals(last.getElementsByTagName("DOMSymbolInstance").getLength(), 0);
                }
            }
            ByteArrayOutputStream after = new ByteArrayOutputStream();
            swf.saveTo(after);
            assertEquals(after.toByteArray(), before.toByteArray(), "Export must not modify the source display list or shape style blocks");
        } finally {
            Files.deleteIfExists(output);
        }
    }

    @DataProvider(name = "differentEndpoints")
    public Object[][] differentEndpoints() {
        return new Object[][]{{"color"}, {"geometry"}, {"matrix"}, {"merged"}};
    }

    @Test(dataProvider = "differentEndpoints")
    public void doesNotInventOrPartiallySplitAnEndpoint(String difference) throws Exception {
        SWF swf = fixture();
        int targetFrame = difference.equals("merged") ? 65 : 21;
        if (difference.equals("color")) {
            ((ShapeTag) swf.getCharacter(51)).getShapes().fillStyles.fillStyles[0].color.blue = 7;
        } else if (difference.equals("geometry")) {
            ((StyleChangeRecord) ((ShapeTag) swf.getCharacter(51)).getShapes().shapeRecords.get(0)).moveDeltaX += 100;
        } else if (difference.equals("merged")) {
            ShapeTag shape = (ShapeTag) swf.getCharacter(175);
            for (com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD record : shape.getShapes().shapeRecords) {
                if (record instanceof StyleChangeRecord && ((StyleChangeRecord) record).stateNewStyles) {
                    ((StyleChangeRecord) record).fillStyles.fillStyles[0].gradient.gradientRecords[0].color.blue = 100;
                }
            }
        } else {
            for (Tag tag : swf.getTags()) {
                if (tag instanceof PlaceObjectTypeTag && ((PlaceObjectTypeTag) tag).getCharacterId() == 51) {
                    ((PlaceObjectTypeTag) tag).getMatrix().translateX += 20;
                }
            }
        }
        ReadOnlyTagList restored = MorphShapeEndings.restore(swf.getTags(), new MorphShapeSymbols());
        int frame = 1, endings = 0;
        boolean original = false;
        for (Tag tag : restored) {
            if (frame == targetFrame) {
                endings += tag instanceof MorphShapeEndings.EndPlacement ? 1 : 0;
                original |= tag instanceof PlaceObjectTypeTag && ((PlaceObjectTypeTag) tag).getCharacterId() == (targetFrame == 21 ? 51 : 175);
            }
            if (tag instanceof ShowFrameTag) {
                frame++;
            }
        }
        assertEquals(endings, 0, "Only a complete matching endpoint may replace a visible shape");
        assertTrue(original);
    }

    @Test
    public void doesNotOverwriteAnUnrelatedReplacementAtTheOldMorphDepth() throws Exception {
        SWF swf = fixture();
        List<Tag> tags = new ArrayList<>();
        tags.add(new PlaceObject2Tag(swf, false, 2, 50, new MATRIX(), null, -1, null, -1, null));
        tags.add(new ShowFrameTag(swf));
        tags.add(new PlaceObject2Tag(swf, true, 2, 59, new MATRIX(), null, -1, null, -1, null));
        tags.add(new PlaceObject2Tag(swf, false, 3, 51, new MATRIX(), null, -1, null, -1, null));
        tags.add(new ShowFrameTag(swf));
        ReadOnlyTagList restored = MorphShapeEndings.restore(new ReadOnlyTagList(tags), new MorphShapeSymbols());
        for (Tag tag : restored) {
            assertFalse(tag instanceof MorphShapeEndings.EndPlacement);
        }
    }

    @Test
    public void morphColorsReachTheirExactEndpointsWithoutBlueBias() {
        RGBA black = new RGBA(0, 0, 0, 255), white = new RGBA(255, 255, 255, 255);
        assertEquals(MORPHGRADIENT.morphColor(white, black, 65535), black);
        assertEquals(MORPHGRADIENT.morphColor(black, white, 0), black);
        assertEquals(MORPHGRADIENT.morphColor(black, white, 21845), new RGBA(85, 85, 85, 255));
        assertEquals(MORPHGRADIENT.morphColor(white, black, 21845), new RGBA(170, 170, 170, 255));
    }
}

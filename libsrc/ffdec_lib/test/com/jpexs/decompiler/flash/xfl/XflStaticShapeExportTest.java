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
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.Tag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.testng.Assert.*;

public class XflStaticShapeExportTest {

    @DataProvider(name = "placement")
    public Object[][] placement() {
        String[] coat = {"#d90000", "#8c0000", "#bf0000", "#cc0000", "#aa0000", "#f0f0f0", "#e58508"};
        String[] gift = {"#fcd4ee", "#000000", "#ffb4db", "#fe8dc5", "#ce907b", "#efc2bc",
            "#f676a6", "#f5317b", "#ff57a5", "#ff68ab", "#ca9aa6"};
        String[] penguins = {"#000000", "#3399cc", "#66ccff", "#ff9900", "#ffffff", "#cccccc", "#fec643", "#993300",
            "#99ffff", "#000000", "#fec643", "#ff9900", "#cccccc", "#ffffff", "#ffffff",
            "#000000", "#3399cc", "#66ccff", "#ffffff", "#ff9900", "#cccccc",
            "#000000", "#fec643", "#ff9900", "#cccccc", "#ffffff"};
        return new Object[][]{
            {false, "overlapping-static-fills.swf", 113, coat, true},
            {true, "overlapping-static-fills.swf", 113, coat, true},
            {false, "coincident-static-fills.swf", 41, gift, false},
            {true, "coincident-static-fills.swf", 41, gift, false},
            {false, "multilayer-static-fills.swf", 15, penguins, true},
            {true, "multilayer-static-fills.swf", 15, penguins, true}
        };
    }

    @Test(dataProvider = "placement")
    public void problematicFillsKeepTheirOwnPlanesAndPaintOrder(boolean library, String fixture,
            int characterId, String[] colors, boolean overlapping) throws Exception {
        SWF swf;
        // Shape 113 from 289.swf. The first red paint overlaps the third one
        // at the left contour; merging them makes CS6 drop its largest region.
        // Shape 41 from ArtworkRoomsTown40.swf has no overlapping fills, but
        // coincident boundaries cause CS6 to drop the brown ribbon rectangle.
        // Shape 15 from badshape.swf has four style groups. Selecting its right
        // penguin in CS6 used to discard the blue fill.
        try (InputStream in = Files.newInputStream(Paths.get("testdata/xfl", fixture))) {
            swf = new SWF(in, false);
        }
        if (library) {
            for (Tag tag : swf.getTags()) {
                if (tag instanceof PlaceObject2Tag) {
                    PlaceObject2Tag place = (PlaceObject2Tag) tag;
                    place.placeFlagHasName = true;
                    place.name = "coat";
                }
            }
        }
        ByteArrayOutputStream before = new ByteArrayOutputStream();
        swf.saveTo(before);
        Path output = Files.createTempFile("ffdec-static-paints-", ".fla");
        try {
            ShapeTag shape = (ShapeTag) swf.getCharacter(characterId);
            assertEquals(ShapePaints.hasOverlappingFills(swf, shape.getShapeNum(), shape.getShapes(), shape.getWindingRule()), overlapping);
            if (!overlapping) {
                assertTrue(ShapePaints.hasCoincidentFillEdges(shape.getShapes()));
            }
            swf.exportFla(new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable error) {
                    throw new AssertionError(error);
                }

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }
            }, output.toString(), "coat.swf", "Test", "Test", "1", false, FLAVersion.CS6, null);
            try (ZipFile zip = new ZipFile(output.toFile())) {
                Document document;
                // Shapes with several style groups are automatically kept in a
                // library symbol, even when the placement has no instance name.
                boolean exportedAsSymbol = library || characterId == 15;
                String path = exportedAsSymbol ? "LIBRARY/Symbol " + characterId + ".xml" : "DOMDocument.xml";
                try (InputStream in = zip.getInputStream(zip.getEntry(path))) {
                    document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                }
                NodeList shapes = document.getElementsByTagName("DOMShape");
                assertEquals(shapes.getLength(), colors.length, "Each SWF paint needs an independent XFL plane");
                assertEquals(document.getElementsByTagName("DOMLayer").getLength(), exportedAsSymbol ? colors.length : 1);
                for (int i = 0; i < shapes.getLength(); i++) {
                    Element paint = (Element) shapes.item(i);
                    assertEquals(paint.getAttribute("isFloating"), "true");
                    assertEquals(paint.getElementsByTagName("FillStyle").getLength(), 1);
                    Element color = (Element) paint.getElementsByTagName("SolidColor").item(0);
                    assertEquals(color.getAttribute("color"), colors[exportedAsSymbol ? colors.length - 1 - i : i],
                            "Elements paint back to front, while timeline layers are listed front to back");
                    assertTrue(paint.getElementsByTagName("Edge").getLength() > 0, "Keep every paint's geometry");
                    if (characterId == 15) {
                        int sourcePaint = exportedAsSymbol ? colors.length - 1 - i : i;
                        assertEquals(Double.parseDouble(color.getAttribute("alpha")), sourcePaint == 14 ? 0.4 : 1.0,
                                "Keep the translucent white paint in its original group");
                    }
                }
            }
            ByteArrayOutputStream after = new ByteArrayOutputStream();
            swf.saveTo(after);
            assertEquals(after.toByteArray(), before.toByteArray(), "Do not alter the source SWF");
        } finally {
            Files.deleteIfExists(output);
        }
    }
}

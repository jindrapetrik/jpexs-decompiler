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
        return new Object[][]{{false}, {true}};
    }

    @Test(dataProvider = "placement")
    public void overlappingFillsKeepTheirOwnPlanesAndPaintOrder(boolean library) throws Exception {
        SWF swf;
        // Shape 113 from 289.swf. The first red paint overlaps the third one
        // at the left contour; merging them makes CS6 drop its largest region.
        try (InputStream in = Files.newInputStream(Paths.get("testdata/xfl/overlapping-static-fills.swf"))) {
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
            ShapeTag shape = (ShapeTag) swf.getCharacter(113);
            assertTrue(ShapePaints.hasOverlappingFills(swf, shape.getShapeNum(), shape.getShapes(), shape.getWindingRule()));
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
                String path = library ? "LIBRARY/Symbol 113.xml" : "DOMDocument.xml";
                try (InputStream in = zip.getInputStream(zip.getEntry(path))) {
                    document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                }
                NodeList shapes = document.getElementsByTagName("DOMShape");
                assertEquals(shapes.getLength(), 7, "Each overlapping SWF paint needs an independent XFL plane");
                assertEquals(document.getElementsByTagName("DOMLayer").getLength(), library ? 7 : 1);
                String[] colors = {"#d90000", "#8c0000", "#bf0000", "#cc0000", "#aa0000", "#f0f0f0", "#e58508"};
                for (int i = 0; i < shapes.getLength(); i++) {
                    Element paint = (Element) shapes.item(i);
                    assertEquals(paint.getAttribute("isFloating"), "true");
                    assertEquals(paint.getElementsByTagName("FillStyle").getLength(), 1);
                    Element color = (Element) paint.getElementsByTagName("SolidColor").item(0);
                    assertEquals(color.getAttribute("color"), colors[library ? colors.length - 1 - i : i],
                            "Elements paint back to front, while timeline layers are listed front to back");
                    assertTrue(paint.getElementsByTagName("Edge").getLength() > 0, "Keep every paint's geometry");
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

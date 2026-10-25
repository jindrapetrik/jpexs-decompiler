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
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.testng.Assert.*;

public class XflMaskFrameExportTest {

    @Test
    public void changingMaskRangesKeepMaskedAndUnmaskedFrameIntervals() throws Exception {
        SWF swf = new SWF();
        swf.frameRate = 24;
        swf.frameCount = 200;
        for (int id = 1; id <= 4; id++) {
            DefineShape3Tag shape = new DefineShape3Tag(swf);
            shape.shapeId = id;
            FILLSTYLE fill = new FILLSTYLE();
            fill.color = new RGBA(id * 50, 0, 0, 255);
            shape.shapes.fillStyles.fillStyles = new FILLSTYLE[]{fill};
            shape.shapes.shapeRecords.clear();
            StyleChangeRecord start = new StyleChangeRecord();
            start.stateMoveTo = start.stateFillStyle1 = true;
            start.fillStyle1 = 1;
            shape.shapes.shapeRecords.add(start);
            for (int[] delta : new int[][]{{2000, 0}, {0, 2000}, {-2000, 0}, {0, -2000}}) {
                StraightEdgeRecord edge = new StraightEdgeRecord();
                edge.generalLineFlag = true;
                edge.deltaX = delta[0];
                edge.deltaY = delta[1];
                shape.shapes.shapeRecords.add(edge);
            }
            shape.shapes.shapeRecords.add(new EndShapeRecord());
            shape.updateBounds();
            swf.addTag(shape);
        }
        for (int frame = 0; frame < swf.frameCount; frame++) {
            if (frame == 0) {
                for (int depth = 2; depth <= 6; depth += 2) {
                    swf.addTag(new PlaceObject2Tag(swf, false, depth, depth / 2 + 1, new MATRIX(), null, -1, "content" + depth, -1, null));
                }
            }
            if (frame == 0 || frame == 50 || frame == 150) {
                swf.addTag(new PlaceObject2Tag(swf, false, 1, 1, new MATRIX(), null, -1, "mask", frame == 50 ? 5 : 3, null));
            }
            if (frame == 40 || frame == 110) {
                RemoveObject2Tag remove = new RemoveObject2Tag(swf);
                remove.depth = 1;
                swf.addTag(remove);
            }
            swf.addTag(new ShowFrameTag(swf));
        }
        Path output = Files.createTempFile("ffdec-mask-ranges-", ".fla");
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
            }, output.toString(), "masks.swf", "Test", "Test", "1", false, FLAVersion.CS6, null);
            try (ZipFile zip = new ZipFile(output.toFile()); InputStream in = zip.getInputStream(zip.getEntry("DOMDocument.xml"))) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
                NodeList layers = document.getElementsByTagName("DOMLayer");
                for (int frame = 0; frame < swf.frameCount; frame++) {
                    Map<String, Boolean> masked = new HashMap<>();
                    for (int l = 0; l < layers.getLength(); l++) {
                        Element layer = (Element) layers.item(l);
                        NodeList frames = layer.getElementsByTagName("DOMFrame");
                        for (int f = 0; f < frames.getLength(); f++) {
                            Element key = (Element) frames.item(f);
                            int index = Integer.parseInt(key.getAttribute("index"));
                            int duration = key.hasAttribute("duration") ? Integer.parseInt(key.getAttribute("duration")) : 1;
                            if (frame < index || frame >= index + duration) {
                                continue;
                            }
                            NodeList instances = key.getElementsByTagName("DOMSymbolInstance");
                            for (int i = 0; i < instances.getLength(); i++) {
                                String name = ((Element) instances.item(i)).getAttribute("name");
                                if (!name.startsWith("content")) {
                                    continue;
                                }
                                boolean underMask = layer.hasAttribute("parentLayerIndex");
                                if (underMask) {
                                    Element parent = (Element) layers.item(Integer.parseInt(layer.getAttribute("parentLayerIndex")));
                                    assertEquals(parent.getAttribute("layerType"), "mask");
                                }
                                assertNull(masked.put(name, underMask), "Do not emit an instance twice on frame " + frame);
                            }
                        }
                    }
                    assertEquals(masked.size(), 3, "Keep every content instance on frame " + frame);
                    boolean maskActive = frame < 40 || frame >= 50 && frame < 110 || frame >= 150;
                    assertEquals(masked.get("content2"), Boolean.valueOf(maskActive), "Depth 2 on frame " + frame);
                    assertEquals(masked.get("content4"), Boolean.valueOf(frame >= 50 && frame < 110), "Depth 4 on frame " + frame);
                    assertEquals(masked.get("content6"), Boolean.FALSE, "Depth 6 on frame " + frame);
                }
            }
        } finally {
            Files.deleteIfExists(output);
        }
    }
}

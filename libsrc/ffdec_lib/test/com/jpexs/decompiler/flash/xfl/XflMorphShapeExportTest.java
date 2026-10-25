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
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.exporters.shape.PathExporter;
import com.jpexs.decompiler.flash.tags.DefineMorphShape2Tag;
import com.jpexs.decompiler.flash.tags.DefineMorphShapeTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.tags.RemoveObject2Tag;
import com.jpexs.decompiler.flash.tags.base.MorphShapeTag;
import com.jpexs.decompiler.flash.tags.base.ShapeTag;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.FILLSTYLEARRAY;
import com.jpexs.decompiler.flash.types.LINESTYLE;
import com.jpexs.decompiler.flash.types.LINESTYLEARRAY;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.MORPHFILLSTYLE;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE2;
import com.jpexs.decompiler.flash.types.MORPHLINESTYLE;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.SHAPEWITHSTYLE;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.CurvedEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.SHAPERECORD;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.InputStream;
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

public class XflMorphShapeExportTest {

    @DataProvider(name = "shapeFixing")
    public Object[][] shapeFixing() {
        return new Object[][]{{true, false}, {false, false}, {true, true}, {false, true}};
    }

    @Test(dataProvider = "shapeFixing")
    public void overlappingMorphPaintsKeepEverySwfPose(boolean fixShapes, boolean morph2) throws Exception {
        boolean previous = Configuration.flaExportFixShapes.get();
        Path output = Files.createTempFile("ffdec-morph-paints-", ".fla");
        try {
            Configuration.flaExportFixShapes.set(fixShapes);
            SWF swf = new SWF();
            swf.frameRate = 24;
            swf.frameCount = 3;
            MorphShapeTag morph = morph2 ? new DefineMorphShape2Tag(swf) : new DefineMorphShapeTag(swf);
            morph.characterId = 1;
            morph.morphFillStyles.fillStyles = new MORPHFILLSTYLE[2];
            for (int i = 0; i < 2; i++) {
                MORPHFILLSTYLE style = new MORPHFILLSTYLE();
                style.startColor = new RGBA(i == 0 ? 255 : 0, 0, i == 0 ? 0 : 255, 255);
                style.endColor = style.startColor;
                morph.morphFillStyles.fillStyles[i] = style;
            }
            MORPHLINESTYLE stroke = new MORPHLINESTYLE();
            stroke.startWidth = stroke.endWidth = 20;
            stroke.startColor = stroke.endColor = new RGBA(0, 0, 0, 255);
            if (morph2) {
                MORPHLINESTYLE2 stroke2 = new MORPHLINESTYLE2();
                stroke2.startWidth = stroke2.endWidth = 20;
                stroke2.startColor = stroke2.endColor = new RGBA(0, 0, 0, 255);
                morph.morphLineStyles.lineStyles2 = new MORPHLINESTYLE2[]{stroke2};
            } else {
                morph.morphLineStyles.lineStyles = new MORPHLINESTYLE[]{stroke};
            }
            morph.startEdges.shapeRecords = rectangles(false);
            morph.endEdges.shapeRecords = rectangles(true);
            assertTrue(MorphShapePaints.hasOverlappingFills(morph));
            swf.addTag(morph);
            ShapeTag endShape = morph.getEndShapeTag();
            endShape.setCharacterId(2);
            swf.addTag(endShape);
            PlaceObject2Tag first = new PlaceObject2Tag(swf);
            first.depth = 1;
            first.characterId = 1;
            first.placeFlagHasCharacter = true;
            first.placeFlagHasMatrix = true;
            first.matrix = new MATRIX();
            swf.addTag(first);
            swf.addTag(new ShowFrameTag(swf));
            PlaceObject2Tag middle = new PlaceObject2Tag(swf);
            middle.depth = 1;
            middle.placeFlagMove = true;
            middle.placeFlagHasRatio = true;
            middle.ratio = 32768;
            swf.addTag(middle);
            swf.addTag(new ShowFrameTag(swf));
            swf.resetTimeline();
            PlaceObject2Tag last = new PlaceObject2Tag(swf);
            last.depth = 1;
            last.placeFlagMove = true;
            last.placeFlagHasCharacter = true;
            last.characterId = 2;
            swf.addTag(last);
            swf.addTag(new ShowFrameTag(swf));
            swf.exportFla(new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable error) {
                    throw new AssertionError(error);
                }

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }
            }, output.toString(), "overlap.swf", "Test", "Test", "1", false, FLAVersion.CS6, null);
            try (ZipFile zip = new ZipFile(output.toFile()); InputStream xml = zip.getInputStream(zip.getEntry("DOMDocument.xml"))) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
                NodeList layers = document.getElementsByTagName("DOMLayer");
                assertEquals(layers.getLength(), 1);
                NodeList frames = document.getElementsByTagName("DOMFrame");
                assertEquals(frames.getLength(), 3, "Keep all changing poses, including the middle ratio");
                for (int f = 0; f < frames.getLength(); f++) {
                    Element frame = (Element) frames.item(f);
                    assertFalse(frame.hasAttribute("tweenType"), "A native tween would merge overlapping paints");
                    assertEquals(frame.getAttribute("index"), Integer.toString(f));
                    NodeList shapes = frame.getElementsByTagName("DOMShape");
                    if (f < 2) {
                        assertEquals(shapes.getLength(), 3, "Keep both fills and the overlaid stroke independent");
                        for (int i = 0; i < shapes.getLength(); i++) {
                            Element shape = (Element) shapes.item(i);
                            assertEquals(shape.getAttribute("isFloating"), "true");
                            assertEquals(shape.getElementsByTagName("FillStyle").getLength(), i == 2 ? 0 : 1);
                            assertEquals(shape.getElementsByTagName("StrokeStyle").getLength(), i == 2 ? 1 : 0);
                        }
                        assertEquals(((Element) ((Element) shapes.item(0)).getElementsByTagName("SolidColor").item(0)).getAttribute("color"), "#ff0000");
                        assertEquals(((Element) ((Element) shapes.item(1)).getElementsByTagName("SolidColor").item(0)).getAttribute("color"), "#0000ff");
                        Element edge = (Element) ((Element) shapes.item(0)).getElementsByTagName("Edge").item(0);
                        assertEquals(edge.getAttribute("fillStyle0"), "1", "Counterclockwise outer contour keeps its interior on side 0");
                        assertTrue(edge.getAttribute("edges").startsWith(f == 0 ? "! 0 0" : "! 50 0"), "Use the actual morph ratio");
                    }
                }
            }
            assertEquals(morph.startEdges.shapeRecords.size(), 11, "Do not rewrite source shape records");
        } finally {
            Configuration.flaExportFixShapes.set(previous);
            Files.deleteIfExists(output);
        }
    }

    @Test(dataProvider = "shapeFixing")
    public void nonOverlappingMorphsKeepPartialAndReversedPosesAndRemoval(boolean fixShapes, boolean morph2) throws Exception {
        boolean previous = Configuration.flaExportFixShapes.get();
        Path output = Files.createTempFile("ffdec-morph-poses-", ".fla");
        try {
            Configuration.flaExportFixShapes.set(fixShapes);
            SWF swf = new SWF();
            swf.frameRate = 24;
            int[] ratios = {8192, 49152, 49152, 16384, 63715};
            swf.frameCount = ratios.length + 1;
            MorphShapeTag morph = morph2 ? new DefineMorphShape2Tag(swf) : new DefineMorphShapeTag(swf);
            morph.characterId = 1;
            MORPHFILLSTYLE fill = new MORPHFILLSTYLE();
            fill.startColor = new RGBA(248, 185, 204, 255);
            fill.endColor = new RGBA(120, 80, 220, 255);
            morph.morphFillStyles.fillStyles = new MORPHFILLSTYLE[]{fill};
            if (morph2) {
                MORPHLINESTYLE2 line = new MORPHLINESTYLE2();
                line.startWidth = line.endWidth = 65;
                line.startColor = line.endColor = new RGBA(223, 120, 175, 255);
                morph.morphLineStyles.lineStyles2 = new MORPHLINESTYLE2[]{line};
            } else {
                MORPHLINESTYLE line = new MORPHLINESTYLE();
                line.startWidth = line.endWidth = 65;
                line.startColor = line.endColor = new RGBA(223, 120, 175, 255);
                morph.morphLineStyles.lineStyles = new MORPHLINESTYLE[]{line};
            }
            morph.startEdges.shapeRecords = curvedContour(false);
            morph.endEdges.shapeRecords = curvedContour(true);
            morph.updateBounds();
            swf.addTag(morph);
            ShapeTag replacement = morph.getEndShapeTag();
            replacement.setCharacterId(2);
            swf.addTag(replacement);
            assertFalse(MorphShapePaints.hasOverlappingFills(morph));
            for (int f = 0; f < ratios.length; f++) {
                PlaceObject2Tag place = new PlaceObject2Tag(swf);
                place.depth = 1;
                place.placeFlagMove = f > 0;
                place.placeFlagHasCharacter = f == 0;
                place.characterId = 1;
                place.placeFlagHasRatio = true;
                place.ratio = ratios[f];
                place.placeFlagHasMatrix = true;
                place.matrix = new MATRIX();
                place.matrix.translateX = f * 100 - 8;
                place.matrix.translateY = 70;
                swf.addTag(place);
                swf.addTag(new ShowFrameTag(swf));
            }
            // The source can place an end shape and remove it before ShowFrame.
            // It must not become an inferred 65535 endpoint in the output.
            PlaceObject2Tag end = new PlaceObject2Tag(swf);
            end.depth = 1;
            end.placeFlagMove = true;
            end.placeFlagHasCharacter = true;
            end.characterId = 2;
            swf.addTag(end);
            RemoveObject2Tag remove = new RemoveObject2Tag(swf);
            remove.depth = 1;
            swf.addTag(remove);
            swf.addTag(new ShowFrameTag(swf));
            swf.resetTimeline();
            swf.exportFla(new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable error) {
                    throw new AssertionError(error);
                }

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }
            }, output.toString(), "poses.swf", "Test", "Test", "1", false, FLAVersion.CS6, null);
            try (ZipFile zip = new ZipFile(output.toFile()); InputStream xml = zip.getInputStream(zip.getEntry("DOMDocument.xml"))) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
                NodeList frames = document.getElementsByTagName("DOMFrame");
                assertEquals(frames.getLength(), ratios.length + 1, "Do not replace distinct ratios or matrix changes with a guessed tween");
                for (int f = 0; f < frames.getLength(); f++) {
                    Element frame = (Element) frames.item(f);
                    assertEquals(frame.getAttribute("index"), Integer.toString(f));
                    assertFalse(frame.hasAttribute("tweenType"), "Native contour matching can rotate a shape during interpolation");
                    NodeList actual = frame.getElementsByTagName("DOMShape");
                    if (f == ratios.length) {
                        assertEquals(actual.getLength(), 0, "Preserve removal instead of adding a synthetic endpoint");
                        assertEquals(frame.getElementsByTagName("DOMSymbolInstance").getLength(), 0);
                    } else {
                        assertEquals(actual.getLength(), 2, "Normalize the fill separately from its stroke, including single-fill morphs");
                        assertEquals(((Element) actual.item(0)).getElementsByTagName("FillStyle").getLength(), 1);
                        assertEquals(((Element) actual.item(0)).getElementsByTagName("StrokeStyle").getLength(), 0);
                        assertEquals(((Element) actual.item(1)).getElementsByTagName("StrokeStyle").getLength(), 1);
                        SHAPEWITHSTYLE pose = morph.getShapeAtRatio(ratios[f]);
                        MATRIX matrix = new MATRIX();
                        matrix.translateX = f * 100 - 8;
                        matrix.translateY = 70;
                        XFLXmlWriter expected = new XFLXmlWriter();
                        java.lang.reflect.Method convert = XFLConverter.class.getDeclaredMethod("convertShape",
                                com.jpexs.helpers.Reference.class, java.util.Map.class, SWF.class, MATRIX.class, int.class,
                                List.class, FILLSTYLEARRAY.class, LINESTYLEARRAY.class, boolean.class, boolean.class,
                                XFLXmlWriter.class, int.class, boolean.class);
                        convert.setAccessible(true);
                        convert.invoke(null, new com.jpexs.helpers.Reference<Integer>(1), new java.util.HashMap<>(),
                                swf, matrix, morph2 ? 4 : 3, pose.shapeRecords, pose.fillStyles, pose.lineStyles,
                                true, false, expected, 1, false);
                        Document expectedDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                                new java.io.ByteArrayInputStream(("<elements>" + expected + "</elements>").getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                        NodeList expectedShapes = expectedDocument.getElementsByTagName("DOMShape");
                        assertEquals(actual.getLength(), expectedShapes.getLength());
                        for (int i = 0; i < actual.getLength(); i++) {
                            removeWhitespace(actual.item(i));
                            removeWhitespace(expectedShapes.item(i));
                            assertTrue(actual.item(i).isEqualNode(expectedShapes.item(i)), "Use the actual ratio and matrix on frame " + f);
                        }
                    }
                }
            }
        } finally {
            Configuration.flaExportFixShapes.set(previous);
            Files.deleteIfExists(output);
        }
    }

    @DataProvider(name = "hintedTweens")
    public Object[][] hintedTweens() {
        return new Object[][]{{false, false, false}, {false, true, false}, {true, false, false}, {true, true, false},
            {false, false, true}, {false, true, true}, {true, false, true}, {true, true, true}};
    }

    @Test(dataProvider = "hintedTweens")
    public void nativeMorphTweenKeepsVertexHintsPartialEndpointAndRemoval(boolean morph2, boolean reverse, boolean colored) throws Exception {
        SWF swf = new SWF();
        swf.frameRate = 24;
        int[] ratios = {0, 20025, 36408, 49152, 58254, 63715};
        swf.frameCount = ratios.length + 1;
        MorphShapeTag morph = compiledMorph(swf, morph2);
        swf.addTag(morph);
        for (int f = 0; f < ratios.length; f++) {
            PlaceObject2Tag place = new PlaceObject2Tag(swf);
            place.depth = 1;
            place.placeFlagMove = f > 0;
            place.placeFlagHasCharacter = f == 0;
            place.characterId = 1;
            place.placeFlagHasMatrix = f == 0;
            place.matrix = new MATRIX();
            place.matrix.translateX = -8;
            place.matrix.translateY = 70;
            place.placeFlagHasRatio = true;
            place.ratio = ratios[reverse ? ratios.length - 1 - f : f];
            if (colored && f == 0) {
                place.setColorTransform(morphColor());
            }
            swf.addTag(place);
            swf.addTag(new ShowFrameTag(swf));
        }
        RemoveObject2Tag remove = new RemoveObject2Tag(swf);
        remove.depth = 1;
        swf.addTag(remove);
        swf.addTag(new ShowFrameTag(swf));
        swf.resetTimeline();
        Path output = Files.createTempFile("ffdec-native-morph-", ".fla");
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
            }, output.toString(), "hinted.swf", "Test", "Test", "1", false, FLAVersion.CS6, null);
            try (ZipFile zip = new ZipFile(output.toFile()); InputStream xml = zip.getInputStream(zip.getEntry("DOMDocument.xml"))) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
                if (colored) {
                    NodeList outer = document.getElementsByTagName("DOMSymbolInstance");
                    assertEquals(outer.getLength(), ratios.length);
                    String symbol = ((Element) outer.item(0)).getAttribute("libraryItemName");
                    for (int f = 0; f < ratios.length; f++) {
                        Element instance = (Element) outer.item(f);
                        assertEquals(instance.getAttribute("loop"), "single frame");
                        assertEquals(instance.getAttribute("firstFrame"), Integer.toString(f));
                        Element color = (Element) instance.getElementsByTagName("Color").item(0);
                        assertEquals(Double.parseDouble(color.getAttribute("redMultiplier")), 217 / 256.0);
                        assertEquals(color.getAttribute("blueOffset"), "23");
                    }
                    Element empty = (Element) document.getElementsByTagName("DOMFrame").item(ratios.length);
                    assertEquals(empty.getElementsByTagName("DOMSymbolInstance").getLength(), 0);
                    try (InputStream inner = zip.getInputStream(zip.getEntry("LIBRARY/" + symbol + ".xml"))) {
                        document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inner);
                    }
                    assertEquals(document.getElementsByTagName("Color").getLength(), 0, "Apply the instance effect exactly once, outside the morph");
                }
                NodeList frames = document.getElementsByTagName("DOMFrame");
                assertEquals(frames.getLength(), colored ? 2 : 3, "A real tween and its actual endpoint; outer timeline owns removal");
                Element start = (Element) frames.item(0);
                assertEquals(start.getAttribute("tweenType"), "shape");
                assertEquals(start.getAttribute("duration"), "5");
                assertEquals(start.getAttribute("acceleration"), reverse ? "71" : "-71");
                NodeList hints = start.getElementsByTagName("MorphHint");
                assertEquals(hints.getLength(), 9, "Hint every vertex, including the initially collinear ones");
                for (int i = 0; i < hints.getLength(); i++) {
                    Element hint = (Element) hints.item(i);
                    assertEquals(hint.getAttribute("key"), Integer.toString(i));
                    assertEquals(hint.getParentNode().getParentNode(), start, "Hints belong to the frame, outside the disposable MorphShape cache");
                }
                assertEquals(((Element) hints.item(0)).getAttribute(reverse ? "endPoint" : "startPoint"), colored ? "#FFFFFD.39, #FFFFFF.C2" : "#FFFFFD.31, #0.08", "Preserve signed twips; wrapper geometry has no instance translation");
                assertEquals(start.getElementsByTagName("DOMShape").getLength(), 1);
                assertFalse(((Element) start.getElementsByTagName("DOMShape").item(0)).hasAttribute("isFloating"));
                Element endpoint = (Element) frames.item(1);
                assertEquals(endpoint.getAttribute("index"), "5");
                assertEquals(endpoint.getElementsByTagName("DOMShape").getLength(), 1);
                if (!colored) {
                    Element empty = (Element) frames.item(2);
                    assertEquals(empty.getAttribute("index"), "6");
                    assertEquals(empty.getElementsByTagName("DOMShape").getLength(), 0, "Do not invent a 65535 endpoint on removal");
                }
            }
            assertEquals(morph.startEdges.shapeRecords.size(), 21, "Do not mutate the SWF's paired records");
        } finally {
            Files.deleteIfExists(output);
        }
    }

    @Test
    public void narrowMorphStartsHintsAtDistinctCornerInBothEndpoints() throws Exception {
        // With the original first
        // vertex, CS6 reverses the endpoint contour despite all seven hints.
        SWF swf;
        try (InputStream in = Files.newInputStream(java.nio.file.Paths.get("testdata/xfl/narrow-morph.swf"))) {
            swf = new SWF(in, false);
        }
        Path output = Files.createTempFile("ffdec-narrow-morph-", ".fla");
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
            }, output.toString(), "narrow.swf", "Test", "Test", "1", false, FLAVersion.CS6, null);
            try (ZipFile zip = new ZipFile(output.toFile()); InputStream xml = zip.getInputStream(zip.getEntry("DOMDocument.xml"))) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
                NodeList frames = document.getElementsByTagName("DOMFrame");
                assertEquals(frames.getLength(), 2, "Retain an editable tween and its real partial endpoint");
                Element first = (Element) frames.item(0);
                assertEquals(first.getAttribute("tweenType"), "shape");
                assertEquals(first.getAttribute("duration"), "6");
                assertEquals(first.getAttribute("acceleration"), "100");
                NodeList hints = first.getElementsByTagName("MorphHint");
                assertEquals(hints.getLength(), 7);
                assertEquals(((Element) hints.item(0)).getAttribute("startPoint"), "#12.8F, #E.B7");
                String[] start = {"#12.69, #E.A8", "#12.71, #E.AC", "#12.8F, #E.B7",
                    "#6.4A, #1B.EE", "#2.AA, #1F.D6", "#1.F2, #1F.1E", "#5.90, #1B.80"};
                String[] end = {"#11.DA, #E.6E", "#12.3D, #E.78", "#13.AC, #E.A8",
                    "#9.9B, #22.17", "#4.5E, #20.4F", "#3.5A, #1F.93", "#0.37, #1B.EB"};
                for (int i = 0; i < hints.getLength(); i++) {
                    Element hint = (Element) hints.item(i);
                    int vertex = (i + 2) % hints.getLength();
                    assertEquals(hint.getAttribute("key"), Integer.toString(i));
                    assertEquals(hint.getAttribute("startPoint"), start[vertex]);
                    assertEquals(hint.getAttribute("endPoint"), end[vertex], "Keep the SWF's original vertex pairing");
                }
                Element last = (Element) frames.item(1);
                assertEquals(last.getAttribute("index"), "6");
                assertTrue(((Element) first.getElementsByTagName("Edge").item(0)).getAttribute("edges").startsWith("! 4751 3767| "));
                assertTrue(((Element) last.getElementsByTagName("Edge").item(0)).getAttribute("edges").startsWith("! 5036 3752| "));
            }
        } finally {
            Files.deleteIfExists(output);
        }
    }

    @Test
    public void reusedMorphInstancesKeepIndependentRatiosAndChangingColorEffects() throws Exception {
        SWF swf = new SWF();
        swf.frameRate = 24;
        swf.frameCount = 6;
        MorphShapeTag morph = compiledMorph(swf, true);
        swf.addTag(morph);
        int[][] ratios = {{0, 20025, 63715, 36408, 36408, 0}, {63715, 36408, 0, 20025, 63715, 49152}};
        int[] profileRatios = {0, 63715, 20025, 36408, 49152};
        for (int f = 0; f < 6; f++) {
            for (int d = 0; d < 2; d++) {
                PlaceObject2Tag place = new PlaceObject2Tag(swf);
                place.depth = d + 1;
                place.placeFlagMove = f > 0;
                place.placeFlagHasCharacter = f == 0;
                place.characterId = 1;
                place.placeFlagHasMatrix = f == 0;
                place.matrix = new MATRIX();
                place.matrix.translateX = d * 1000;
                place.placeFlagHasRatio = !(d == 0 && f == 4);
                place.ratio = ratios[d][f];
                if (f == 0) {
                    CXFORMWITHALPHA color = morphColor();
                    if (d == 1) {
                        color.redMultTerm = 128;
                        color.alphaMultTerm = 128;
                        color.redAddTerm = 19;
                        color.alphaAddTerm = 7;
                    }
                    place.setColorTransform(color);
                } else if (d == 0 && f == 4) {
                    // A color-only move must retain the current morph ratio.
                    place.setColorTransform(new CXFORMWITHALPHA());
                }
                swf.addTag(place);
            }
            swf.addTag(new ShowFrameTag(swf));
        }
        swf.resetTimeline();
        Path output = Files.createTempFile("ffdec-colored-reused-morph-", ".fla");
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
            }, output.toString(), "colors.swf", "Test", "Test", "1", false, FLAVersion.CS6, null);
            try (ZipFile zip = new ZipFile(output.toFile()); InputStream xml = zip.getInputStream(zip.getEntry("DOMDocument.xml"))) {
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml);
                NodeList layers = document.getElementsByTagName("DOMLayer");
                assertEquals(layers.getLength(), 2);
                String symbol = null;
                for (int l = 0; l < layers.getLength(); l++) {
                    Element layer = (Element) layers.item(l);
                    NodeList frames = layer.getElementsByTagName("DOMFrame");
                    Element firstInstance = (Element) ((Element) frames.item(0)).getElementsByTagName("DOMSymbolInstance").item(0);
                    Element matrix = (Element) firstInstance.getElementsByTagName("Matrix").item(0);
                    int d = matrix.hasAttribute("tx") && Double.parseDouble(matrix.getAttribute("tx")) == 50 ? 1 : 0;
                    if (symbol == null) {
                        symbol = firstInstance.getAttribute("libraryItemName");
                    }
                    assertEquals(firstInstance.getAttribute("libraryItemName"), symbol, "Both instances share untransformed morph geometry");
                    for (int f = 0; f < 6; f++) {
                        Element active = null;
                        for (int k = 0; k < frames.getLength(); k++) {
                            Element frame = (Element) frames.item(k);
                            if (Integer.parseInt(frame.getAttribute("index")) <= f) {
                                active = frame;
                            }
                        }
                        Element instance = (Element) active.getElementsByTagName("DOMSymbolInstance").item(0);
                        int expectedFrame = -1;
                        for (int k = 0; k < profileRatios.length; k++) {
                            if (profileRatios[k] == ratios[d][f]) {
                                expectedFrame = k;
                            }
                        }
                        assertEquals(instance.getAttribute("firstFrame"), Integer.toString(expectedFrame), "Select the actual ratio, including reversal, reuse and color-only moves");
                        assertEquals(instance.getAttribute("loop"), "single frame");
                        Element color = (Element) instance.getElementsByTagName("Color").item(0);
                        double red = color.hasAttribute("redMultiplier") ? Double.parseDouble(color.getAttribute("redMultiplier")) : 1;
                        assertEquals(red, d == 1 ? .5 : f < 4 ? 217 / 256.0 : 1);
                        if (d == 1) {
                            assertEquals(color.getAttribute("alphaMultiplier"), "0.5");
                            assertEquals(color.getAttribute("redOffset"), "19");
                            assertEquals(color.getAttribute("alphaOffset"), "7");
                        } else if (f >= 4) {
                            assertFalse(color.hasAttribute("blueOffset"), "An explicit identity color resets the inherited offset");
                        } else {
                            assertEquals(color.getAttribute("blueOffset"), "23");
                        }
                    }
                }
                try (InputStream inner = zip.getInputStream(zip.getEntry("LIBRARY/" + symbol + ".xml"))) {
                    Document library = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inner);
                    assertEquals(library.getElementsByTagName("DOMSymbolInstance").getLength(), 0, "Do not recursively wrap the morph");
                    assertEquals(library.getElementsByTagName("Color").getLength(), 0, "Do not apply either instance's effect inside the shared symbol");
                }
            }
        } finally {
            Files.deleteIfExists(output);
        }
    }

    private static MorphShapeTag compiledMorph(SWF swf, boolean morph2) {
        MorphShapeTag morph = morph2 ? new DefineMorphShape2Tag(swf) : new DefineMorphShapeTag(swf);
        morph.characterId = 1;
        MORPHFILLSTYLE fill = new MORPHFILLSTYLE();
        fill.startColor = fill.endColor = new RGBA(248, 185, 204, 255);
        morph.morphFillStyles.fillStyles = new MORPHFILLSTYLE[]{fill};
        if (morph2) {
            MORPHLINESTYLE2 line = new MORPHLINESTYLE2();
            line.startWidth = line.endWidth = 65;
            line.startColor = line.endColor = new RGBA(223, 120, 175, 255);
            morph.morphLineStyles.lineStyles2 = new MORPHLINESTYLE2[]{line};
        } else {
            MORPHLINESTYLE line = new MORPHLINESTYLE();
            line.startWidth = line.endWidth = 65;
            line.startColor = line.endColor = new RGBA(223, 120, 175, 255);
            morph.morphLineStyles.lineStyles = new MORPHLINESTYLE[]{line};
        }
        morph.startEdges.shapeRecords = compiledContour(false);
        morph.endEdges.shapeRecords = compiledContour(true);
        morph.updateBounds();
        return morph;
    }

    private static CXFORMWITHALPHA morphColor() {
        CXFORMWITHALPHA color = new CXFORMWITHALPHA();
        color.hasMultTerms = true;
        color.redMultTerm = color.greenMultTerm = color.blueMultTerm = 217;
        color.alphaMultTerm = 256;
        color.hasAddTerms = true;
        color.blueAddTerm = 23;
        return color;
    }

    // A compiled contour with a stroke-only pass and a repeated fill/stroke pass.
    // Three initially collinear vertices become corners in the endpoint. Inferring
    // their correspondence without all nine authoring hints rotates this tween.
    private static List<SHAPERECORD> compiledContour(boolean end) {
        int[][] curves = end ? new int[][]{{823, 67}, {529, 552, 139, 592}, {25, 106, 12, 107},
            {76, 656, -337, 576}, {-53, 87}, {-1082, -806}, {63, -121}, {254, -520, -88, -448}, {-100, -504, -261, -344}}
                : new int[][]{{915, -4}, {227, 523, -72, 627}, {-11, 93, -19, 92},
            {-107, 525, -363, 474}, {-68, -52}, {-922, -707}, {-94, -72}, {239, -320, 239, -415}, {247, -428, -211, -336}};
        List<SHAPERECORD> records = new ArrayList<>();
        StyleChangeRecord move = new StyleChangeRecord();
        move.stateMoveTo = true;
        move.moveDeltaX = end ? -1400 : -711;
        move.moveDeltaY = end ? -485 : -62;
        move.stateLineStyle = !end;
        move.lineStyle = 1;
        records.add(move);
        for (int pass = 0; pass < 2; pass++) {
            if (pass == 1 && !end) {
                StyleChangeRecord fill = new StyleChangeRecord();
                fill.stateFillStyle0 = true;
                fill.fillStyle0 = 1;
                records.add(fill);
            }
            for (int[] delta : curves) {
                if (delta.length == 2) {
                    StraightEdgeRecord edge = new StraightEdgeRecord();
                    edge.generalLineFlag = true;
                    edge.deltaX = delta[0];
                    edge.deltaY = delta[1];
                    records.add(edge);
                } else {
                    CurvedEdgeRecord edge = new CurvedEdgeRecord();
                    edge.controlDeltaX = delta[0];
                    edge.controlDeltaY = delta[1];
                    edge.anchorDeltaX = delta[2];
                    edge.anchorDeltaY = delta[3];
                    records.add(edge);
                }
            }
        }
        records.add(new EndShapeRecord());
        return records;
    }

    private static void removeWhitespace(org.w3c.dom.Node node) {
        for (org.w3c.dom.Node child = node.getFirstChild(); child != null;) {
            org.w3c.dom.Node next = child.getNextSibling();
            if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE && child.getTextContent().trim().isEmpty()) {
                node.removeChild(child);
            } else {
                removeWhitespace(child);
            }
            child = next;
        }
    }

    private static List<SHAPERECORD> curvedContour(boolean end) {
        List<SHAPERECORD> result = new ArrayList<>();
        StyleChangeRecord move = new StyleChangeRecord();
        move.stateMoveTo = true;
        move.moveDeltaX = end ? -500 : 0;
        move.moveDeltaY = end ? -100 : 0;
        move.stateFillStyle1 = !end;
        move.fillStyle1 = 1;
        move.stateLineStyle = !end;
        move.lineStyle = 1;
        result.add(move);
        CurvedEdgeRecord curve = new CurvedEdgeRecord();
        curve.controlDeltaX = end ? 350 : 200;
        curve.controlDeltaY = end ? 100 : -100;
        curve.anchorDeltaX = end ? 250 : 200;
        curve.anchorDeltaY = end ? 100 : 100;
        result.add(curve);
        for (int[] delta : end ? new int[][]{{-50, 400}, {-550, -100}, {0, -500}}
                : new int[][]{{0, 400}, {-400, 0}, {0, -400}}) {
            StraightEdgeRecord edge = new StraightEdgeRecord();
            edge.generalLineFlag = true;
            edge.deltaX = delta[0];
            edge.deltaY = delta[1];
            result.add(edge);
        }
        result.add(new EndShapeRecord());
        return result;
    }

    @Test
    public void normalizedMorphFillSidesAreNotSwappedAgain() throws Exception {
        boolean previous = Configuration.flaExportFixShapes.get();
        try {
            Configuration.flaExportFixShapes.set(true);
            List<SHAPERECORD> records = new ArrayList<>(rectangles(false).subList(0, 5));
            ((StyleChangeRecord) records.get(0)).stateLineStyle = false;
            records.add(new EndShapeRecord());
            FILLSTYLE fill = new FILLSTYLE();
            fill.color = new RGBA(255, 0, 0, 255);
            FILLSTYLEARRAY fills = new FILLSTYLEARRAY();
            fills.fillStyles = new FILLSTYLE[]{fill};
            LINESTYLEARRAY lines = new LINESTYLEARRAY();
            lines.lineStyles = new LINESTYLE[0];
            java.lang.reflect.Method convert = XFLConverter.class.getDeclaredMethod("getShapeLayers",
                    com.jpexs.helpers.Reference.class, java.util.Map.class, SWF.class, MATRIX.class, int.class,
                    List.class, FILLSTYLEARRAY.class, LINESTYLEARRAY.class, boolean.class, int.class, boolean.class);
            convert.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> layers = (List<String>) convert.invoke(null, new com.jpexs.helpers.Reference<Integer>(1),
                    new java.util.HashMap<>(), new SWF(), new MATRIX(), 3, records, fills, lines, true, 1, false);
            assertEquals(layers.size(), 1);
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new java.io.ByteArrayInputStream(layers.get(0).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            Element edge = (Element) document.getElementsByTagName("Edge").item(0);
            assertEquals(edge.getAttribute("fillStyle0"), "0");
            assertEquals(edge.getAttribute("fillStyle1"), "1", "Clockwise contour must keep its fill on side 1");
        } finally {
            Configuration.flaExportFixShapes.set(previous);
        }
    }

    @Test
    public void separatedFillsKeepTheirHolesAndStrokeGeometry() {
        SWF swf = new SWF();
        SHAPEWITHSTYLE shape = new SHAPEWITHSTYLE();
        shape.fillStyles = new FILLSTYLEARRAY();
        shape.fillStyles.fillStyles = new FILLSTYLE[]{
            new FILLSTYLE(), new FILLSTYLE()};
        shape.lineStyles = new LINESTYLEARRAY();
        shape.lineStyles.lineStyles = new LINESTYLE[]{new LINESTYLE()};
        shape.shapeRecords = rectangles(false);
        List<SHAPERECORD> hole = rectangles(false).subList(0, 5);
        ((StyleChangeRecord) hole.get(0)).moveDeltaX = 100;
        ((StyleChangeRecord) hole.get(0)).moveDeltaY = 100;
        for (int i = 1; i < hole.size(); i++) {
            StraightEdgeRecord edge = (StraightEdgeRecord) hole.get(i);
            edge.deltaX /= 4;
            edge.deltaY /= 4;
        }
        shape.shapeRecords.addAll(5, hole);
        List<SHAPEWITHSTYLE> paints = MorphShapePaints.paints(swf, 3, shape);
        assertEquals(paints.size(), 3);
        for (int i = 0; i < 2; i++) {
            Area expected = new Area(PathExporter.exportFillStyle(0, 3, swf, shape, i + 1));
            Area actual = new Area(PathExporter.exportFillStyle(0, 3, swf, paints.get(i), 1));
            actual.exclusiveOr(expected);
            assertTrue(actual.isEmpty(), "Independent paints retain the original filled region");
        }
        GeneralPath first = PathExporter.exportFillStyle(0, 3, swf, paints.get(0), 1);
        assertTrue(first.contains(50, 50));
        assertFalse(first.contains(150, 150), "Retain the even-odd hole");
        assertEquals(PathExporter.exportLineStyle(0, 3, swf, paints.get(2), 1).getBounds2D(),
                PathExporter.exportLineStyle(0, 3, swf, shape, 1).getBounds2D());
        assertEquals(shape.fillStyles.fillStyles.length, 2);
    }

    private static List<SHAPERECORD> rectangles(boolean end) {
        List<SHAPERECORD> result = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            StyleChangeRecord move = new StyleChangeRecord();
            move.stateMoveTo = true;
            move.moveDeltaX = i * 200 + (end ? 100 : 0);
            move.moveDeltaY = i * 200;
            move.stateFillStyle0 = !end;
            move.fillStyle0 = i + 1;
            move.stateLineStyle = !end;
            move.lineStyle = 1;
            result.add(move);
            for (int[] delta : new int[][]{{400, 0}, {0, 400}, {-400, 0}, {0, -400}}) {
                StraightEdgeRecord edge = new StraightEdgeRecord();
                edge.generalLineFlag = true;
                edge.deltaX = delta[0];
                edge.deltaY = delta[1];
                result.add(edge);
            }
        }
        result.add(new EndShapeRecord());
        return result;
    }
}

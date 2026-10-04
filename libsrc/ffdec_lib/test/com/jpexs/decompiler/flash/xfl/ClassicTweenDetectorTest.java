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

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ClassicTweenDetectorTest {

    private String frame(int index, int duration, String symbol, String matrix, String color) {
        return "<DOMFrame index=\"" + index + "\" keyMode=\"9728\""
                + (duration > 1 ? " duration=\"" + duration + "\"" : "")
                + "><elements><DOMSymbolInstance libraryItemName=\"" + symbol + "\">"
                + "<matrix><Matrix " + matrix + "/></matrix>"
                + "<transformationPoint><Point/></transformationPoint>"
                + (color.isEmpty() ? "" : "<color><Color " + color + "/></color>")
                + "</DOMSymbolInstance></elements></DOMFrame>";
    }

    private NodeList detect(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader("<frames>" + ClassicTweenDetector.detect(xml)
                        + "</frames>"))).getElementsByTagName("DOMFrame");
    }

    @Test
    public void preservesInstanceRestartsAndClipActions() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 10; f++) {
            xml.append(frame(f, 1, "Symbol 1", "tx=\"" + f * 10 + "\"", ""));
        }
        String detected = ClassicTweenDetector.detect(xml.toString(), java.util.Collections.singleton(5));
        NodeList frames = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader("<frames>" + detected + "</frames>")))
                .getElementsByTagName("DOMFrame");
        Assert.assertEquals(frames.getLength(), 4);
        Assert.assertEquals(((Element) frames.item(0)).getAttribute("duration"), "4");
        Assert.assertEquals(((Element) frames.item(2)).getAttribute("index"), "5");
        Assert.assertEquals(((Element) frames.item(2)).getAttribute("duration"), "5");
        String actions = xml.toString().replace("</DOMSymbolInstance>",
                "<Actionscript><script><![CDATA[trace('<DOMFrame>');]]></script></Actionscript></DOMSymbolInstance>");
        Assert.assertEquals(ClassicTweenDetector.detect(actions), actions);
    }
    @Test
    public void flaExportRecoversEasedSpriteTween() throws Exception {
        com.jpexs.decompiler.flash.SWF swf = new com.jpexs.decompiler.flash.SWF();
        swf.frameCount = 11;
        swf.frameRate = 24;
        com.jpexs.decompiler.flash.tags.DefineSpriteTag sprite =
                new com.jpexs.decompiler.flash.tags.DefineSpriteTag(swf);
        sprite.frameCount = 1;
        sprite.addTag(new com.jpexs.decompiler.flash.tags.ShowFrameTag(swf));
        swf.addTag(sprite);
        for (int f = 0; f <= 10; f++) {
            double t = f / 10.0;
            com.jpexs.decompiler.flash.types.MATRIX matrix = new com.jpexs.decompiler.flash.types.MATRIX();
            matrix.translateX = (int) Math.round(2000 * (t + 0.5 * t * (1 - t)));
            swf.addTag(new com.jpexs.decompiler.flash.tags.PlaceObject2Tag(swf,
                    f != 0, 1, f == 0 ? sprite.spriteId : -1, matrix, null, -1, null, -1, null));
            swf.addTag(new com.jpexs.decompiler.flash.tags.ShowFrameTag(swf));
        }
        java.nio.file.Path output = java.nio.file.Files.createTempDirectory("classic-tween-export");
        try {
            java.io.File fla = output.resolve("tween.fla").toFile();
            swf.exportFla(new com.jpexs.decompiler.flash.AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable thrown) {
                    throw new AssertionError(thrown);
                }
                @Override
                public com.jpexs.decompiler.flash.AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }
            }, fla.toString(), "tween.swf", "test", "test", "1", false, FLAVersion.CS6, null);
            try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(fla)) {
                org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(zip.getInputStream(zip.getEntry("DOMDocument.xml")));
                NodeList frames = document.getElementsByTagName("DOMFrame");
                Assert.assertEquals(frames.getLength(), 2);
                Element first = (Element) frames.item(0);
                Assert.assertEquals(first.getAttribute("tweenType"), "motion");
                Assert.assertEquals(first.getAttribute("acceleration"), "-50");
                Assert.assertEquals(first.getAttribute("duration"), "10");
                Assert.assertEquals(((Element) frames.item(1)).getAttribute("index"), "10");
            }
        } finally {
            try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(output)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
            }
        }
    }
    @Test
    public void linearTranslationPreservesEndpointAndHold() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 10; f++) {
            xml.append(frame(f, f == 10 ? 5 : 1, "Symbol 1", "tx=\"" + f * 10 + "\"", ""));
        }
        NodeList frames = detect(xml.toString());
        Assert.assertEquals(frames.getLength(), 2);
        Element first = (Element) frames.item(0);
        Assert.assertEquals(first.getAttribute("tweenType"), "motion");
        Assert.assertEquals(first.getAttribute("keyMode"), "22017");
        Assert.assertEquals(first.getAttribute("duration"), "10");
        Assert.assertEquals(first.getAttribute("acceleration"), "0");
        Assert.assertEquals(((Element) frames.item(1)).getAttribute("index"), "10");
        Assert.assertEquals(((Element) frames.item(1)).getAttribute("duration"), "5");
    }

    @Test
    public void detectsBothEasingDirectionsAndTransforms() throws Exception {
        for (int ease : new int[]{-100, -60, 0, 35, 100}) {
            StringBuilder xml = new StringBuilder();
            for (int f = 0; f <= 20; f++) {
                double t = f / 20.0;
                double p = t + ease / 100.0 * t * (1 - t);
                double angle = 0;
                double sx = 1 + p;
                double sy = 1 + p / 2;
                String matrix = "tx=\"" + Math.round(p * 2000) / 20.0 + "\" a=\""
                        + Math.round(sx * Math.cos(angle) * 65536) / 65536.0 + "\" b=\""
                        + Math.round(sx * Math.sin(angle) * 65536) / 65536.0 + "\" c=\""
                        + Math.round(-sy * Math.sin(angle) * 65536) / 65536.0 + "\" d=\""
                        + Math.round(sy * Math.cos(angle) * 65536) / 65536.0 + "\"";
                String color = f == 0 ? "" : "alphaMultiplier=\"" + Math.round((1 - p) * 256) / 256.0
                        + "\" redOffset=\"" + Math.round(p * 200) + "\"";
                xml.append(frame(f, 1, "Symbol 1", matrix, color));
            }
            NodeList frames = detect(xml.toString());
            Assert.assertEquals(frames.getLength(), 2, "ease " + ease);
            Assert.assertEquals(((Element) frames.item(0)).getAttribute("acceleration"), Integer.toString(-ease));
        }
    }

    @Test
    public void preservesRotatedAndSkewedMatricesWithoutClassicFallback() throws Exception {
        for (boolean animated : new boolean[]{false, true}) {
            StringBuilder xml = new StringBuilder();
            for (int f = 0; f <= 20; f++) {
                double t = f / 20.0;
                double angle = Math.toRadians(25 + (animated ? 90 * t : 0));
                String matrix = "tx=\"" + 100 * t + "\" a=\"" + Math.cos(angle)
                        + "\" b=\"" + Math.sin(angle) + "\" c=\"" + -Math.sin(angle - 0.2)
                        + "\" d=\"" + Math.cos(angle - 0.2) + "\"";
                xml.append(frame(f, 1, "Symbol 1", matrix, ""));
            }
            Assert.assertEquals(ClassicTweenDetector.detect(xml.toString()), xml.toString());
        }
    }
    @Test
    public void preservesChangesOfSymbolAndDiscreteProperties() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f < 6; f++) {
            xml.append(frame(f, 1, "Symbol " + f, "tx=\"" + f * 10 + "\"", ""));
        }
        Assert.assertEquals(detect(xml.toString()).getLength(), 6);
        xml.setLength(0);
        for (int f = 0; f < 6; f++) {
            xml.append(frame(f, 1, "Symbol 1", "tx=\"" + f * 10 + "\"", "")
                    .replace("libraryItemName=", "name=\"instance" + f + "\" libraryItemName="));
        }
        Assert.assertEquals(detect(xml.toString()).getLength(), 6);
    }

    @Test
    public void preservesIrregularMotionAndDifferentPropertyEasing() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 6; f++) {
            xml.append(frame(f, 1, "Symbol 1", "tx=\"" + (f % 2 == 0 ? 0 : 100) + "\"", ""));
        }
        Assert.assertEquals(detect(xml.toString()).getLength(), 7);
        xml.setLength(0);
        for (int f = 0; f <= 6; f++) {
            xml.append(frame(f, 1, "Symbol 1", "tx=\"" + f * 100 + "\"",
                    "redOffset=\"" + f * f * 20 + "\""));
        }
        Assert.assertEquals(detect(xml.toString()).getLength(), 7);
    }

    @Test
    public void doesNotBridgeEmptyFramesOrShapeTweens() throws Exception {
        String empty = "<DOMFrame index=\"3\" keyMode=\"9728\"><elements/></DOMFrame>";
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f < 7; f++) {
            xml.append(f == 3 ? empty : frame(f, 1, "Symbol 1", "tx=\"" + f * 10 + "\"", ""));
        }
        Assert.assertEquals(detect(xml.toString()).getLength(), 7);
        Assert.assertEquals(detect(xml.toString().replace("keyMode=\"9728\"", "keyMode=\"17922\" tweenType=\"shape\""))
                .getLength(), 7);
    }
}

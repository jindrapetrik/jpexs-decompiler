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
import java.io.FileInputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XflTweenQualityTest {

    private String frame(int index, double x) {
        return "<DOMFrame index=\"" + index + "\" keyMode=\"9728\"><elements>"
                + "<DOMSymbolInstance libraryItemName=\"Symbol 1\"><matrix><Matrix tx=\"" + x
                + "\" a=\"0.8660254037844387\" b=\"0.5\" c=\"-0.5\" d=\"0.8660254037844387\"/>"
                + "</matrix></DOMSymbolInstance></elements></DOMFrame>";
    }

    private String detect(String frames, int quality) throws Exception {
        return ClassicTweenDetector.detect(frames, Collections.emptySet(), 24,
                Collections.singletonMap("Symbol 1", new double[]{0, 0}), quality);
    }

    private NodeList frames(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader("<frames>" + xml + "</frames>")))
                .getElementsByTagName("DOMFrame");
    }

    @Test
    public void zeroSkipsTheDetectorEntirely() throws Exception {
        String xml = "<DOMSymbolInstance deliberately not parsed";
        Assert.assertSame(detect(xml, 0), xml);
    }

    @DataProvider(name = "qualities")
    public Object[][] qualities() {
        return new Object[][]{{1}, {10}, {25}, {50}, {75}, {99}, {100}};
    }

    @Test(dataProvider = "qualities")
    public void limitedSearchKeepsEveryObservationAndTheUnsearchedTail(int quality) throws Exception {
        StringBuilder xml = new StringBuilder();
        double[] positions = new double[300];
        Random random = new Random(0xF1A);
        for (int f = 0; f < positions.length; f++) {
            positions[f] = f < 20 ? 10 * f : random.nextInt(1000) - 500;
            xml.append(frame(f, positions[f]));
        }
        String result = detect(xml.toString(), quality);
        Assert.assertEquals(detect(xml.toString(), quality), result, "Search limits must be deterministic");
        NodeList frames = frames(result);
        int covered = 0;
        boolean tweenFound = false;
        for (int i = 0; i < frames.getLength(); i++) {
            Element key = (Element) frames.item(i);
            int first = Integer.parseInt(key.getAttribute("index"));
            int duration = key.hasAttribute("duration") ? Integer.parseInt(key.getAttribute("duration")) : 1;
            Assert.assertEquals(first, covered, "A budget must not drop or skip frames");
            Element matrix = (Element) key.getElementsByTagName("Matrix").item(0);
            double begin = Double.parseDouble(matrix.getAttribute("tx"));
            boolean tween = "motion".equals(key.getAttribute("tweenType"));
            tweenFound |= tween;
            double finish = begin;
            double ease = 0;
            if (tween) {
                Element endpoint = (Element) frames.item(i + 1);
                Assert.assertEquals(Integer.parseInt(endpoint.getAttribute("index")), first + duration);
                finish = Double.parseDouble(((Element) endpoint.getElementsByTagName("Matrix").item(0)).getAttribute("tx"));
                ease = -Integer.parseInt(key.getAttribute("acceleration")) / 100.0;
            }
            for (int f = first; f < first + duration; f++) {
                double t = (f - first) / (double) duration;
                double expected = begin + (finish - begin) * (t + ease * t * (1 - t));
                Assert.assertTrue(Math.abs(Math.round(expected * 20) - Math.round(positions[f] * 20)) <= 1,
                        "Changed source observation at frame " + f);
            }
            covered += duration;
        }
        Assert.assertTrue(tweenFound, "Even a small budget should recover the initial simple tween");
        Assert.assertEquals(covered, positions.length);
    }

    @Test
    public void windowsUseActualFrameIndicesRatherThanTheNumberOfKeyframes() {
        List<ClassicTweenDetector.Sample> samples = new ArrayList<>();
        for (int index : new int[]{0, 1, 2, 10000, 10001}) {
            ClassicTweenDetector.Sample sample = new ClassicTweenDetector.Sample();
            sample.index = index;
            samples.add(sample);
        }
        for (int quality = 1; quality < 100; quality++) {
            TweenDetectionBudget budget = new TweenDetectionBudget(quality, samples.size());
            Assert.assertEquals(budget.lastCandidate(samples, 0, 4), 2);
            Assert.assertFalse(budget.includes(0, 10000));
        }
    }

    @Test
    public void disabledInstanceDetectionStillExportsShapeTweens() throws Exception {
        SWF swf;
        try (FileInputStream input = new FileInputStream("testdata/xfl/graphics-tweens.swf")) {
            swf = new SWF(input, false);
        }
        Path output = Files.createTempFile("tween-quality-", ".fla");
        try {
            XFLExportSettings settings = new XFLExportSettings();
            settings.compressed = true;
            settings.tweenDetectionQuality = 0;
            swf.exportXfl(new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable error) {
                    throw new AssertionError(error);
                }

                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }
            }, output.toString(), "graphics.swf", "Test", "Test", "1", false, FLAVersion.CS6, settings, null);
            try (ZipFile zip = new ZipFile(output.toFile())) {
                NodeList frames = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(zip.getInputStream(zip.getEntry("DOMDocument.xml"))).getElementsByTagName("DOMFrame");
                int shapes = 0;
                for (int i = 0; i < frames.getLength(); i++) {
                    Element frame = (Element) frames.item(i);
                    if (frame.hasAttribute("tweenType")) {
                        Assert.assertEquals(frame.getAttribute("tweenType"), "shape");
                        shapes++;
                    }
                }
                Assert.assertEquals(shapes, 7);
            }
        } finally {
            Files.deleteIfExists(output);
        }
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void invalidQualityIsRejected() throws Exception {
        detect("", 101);
    }
}

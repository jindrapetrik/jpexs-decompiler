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
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MotionTweenDetectorTest {
    private String frame(int f, int duration, String matrix) {
        return "<DOMFrame index=\"" + f + "\" keyMode=\"9728\""
                + (duration > 1 ? " duration=\"" + duration + "\"" : "")
                + "><elements><DOMSymbolInstance libraryItemName=\"Symbol 1\">"
                + "<matrix><Matrix " + matrix + "/></matrix>"
                + "<transformationPoint><Point/></transformationPoint>"
                + "<color><Color alphaMultiplier=\"0.5\"/></color>"
                + "</DOMSymbolInstance></elements></DOMFrame>";
    }

    private Document detect(String xml, double frameRate, java.util.Set<Integer> starts) throws Exception {
        String result = ClassicTweenDetector.detect(xml, starts, frameRate,
                Collections.singletonMap("Symbol 1", new double[]{0.25, 0.75}));
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader("<frames>" + result + "</frames>")));
    }

    private String cubicFrames(int count, int lastDuration) {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f < count; f++) {
            double t = f / (double) (count - 1);
            double x = Math.round((30 + 200 * t * t * t) * 20) / 20.0;
            double y = 10;
            xml.append(frame(f, f == count - 1 ? lastDuration : 1,
                    "tx=\"" + x + "\" ty=\"" + y + "\""));
        }
        return xml.toString();
    }

    private double value(Document document, String id, double t) {
        NodeList properties = document.getElementsByTagName("Property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            if (!id.equals(property.getAttribute("id"))) {
                continue;
            }
            if ("0".equals(property.getAttribute("ignoreTimeMap"))) {
                int index = property.hasAttribute("TimeMapIndex")
                        ? Integer.parseInt(property.getAttribute("TimeMapIndex")) : 0;
                Element map = (Element) document.getElementsByTagName("TimeMap").item(index);
                if ("Quadratic".equals(map.getAttribute("type"))) {
                    t += Integer.parseInt(map.getAttribute("strength")) / 100.0 * t * (1 - t);
                } else if ("DualQuadratic".equals(map.getAttribute("type"))) {
                    t = dualProgress(t, Integer.parseInt(map.getAttribute("strength")));
                } else {
                    NativeTimeMap.Type type = null;
                    for (NativeTimeMap.Type candidate : NativeTimeMap.Type.values()) {
                        if (candidate.xmlName.equals(map.getAttribute("type"))) { type = candidate; break; }
                    }
                    Assert.assertNotNull(type, "Unknown native map");
                    Element core = (Element) document.getElementsByTagName("AnimationCore").item(0);
                    int intervals = (Integer.parseInt(core.getAttribute("duration")) - 1000) / 1000;
                    t = new NativeTimeMap(type, Integer.parseInt(map.getAttribute("strength")), intervals).progress(t);
                }
            }
            NodeList keys = property.getElementsByTagName("Keyframe");
            Element first = (Element) keys.item(0);
            double p0 = ordinate(first, "anchor");
            if (keys.getLength() == 1) {
                return p0;
            }
            double totalTime = Double.parseDouble(((Element) keys.item(keys.getLength() - 1)).getAttribute("timevalue"));
            double time = t * totalTime;
            int segment = 0;
            while (segment < keys.getLength() - 2
                    && time > Double.parseDouble(((Element) keys.item(segment + 1)).getAttribute("timevalue"))) {
                segment++;
            }
            first = (Element) keys.item(segment);
            Element last = (Element) keys.item(segment + 1);
            double firstTime = Double.parseDouble(first.getAttribute("timevalue"));
            double lastTime = Double.parseDouble(last.getAttribute("timevalue"));
            t = (time - firstTime) / (lastTime - firstTime);
            p0 = ordinate(first, "anchor");
            double p1 = ordinate(first, "next");
            double p2 = ordinate(last, "previous");
            double p3 = ordinate(last, "anchor");
            double u = 1 - t;
            return u * u * u * p0 + 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t * p3;
        }
        throw new AssertionError("Missing property " + id);
    }

    private double ordinate(Element key, String attribute) {
        return Double.parseDouble(key.getAttribute(attribute).split(",")[1]);
    }

    @Test
    public void recoversLogoDualQuadraticAlphaFadesExactly() throws Exception {
        // Alpha multipliers sampled from 2597/05_900_logo_fromsoft.swf.
        int[][] fades = {
            {5, 13, 18, 23, 28, 33, 41, 46, 51, 56, 59, 64, 69, 74, 79, 82, 87, 90, 95, 97, 102, 105, 108, 110, 115, 118, 120, 123, 125, 128, 128, 131, 133, 136, 138, 141, 143, 146, 148, 154, 156, 159, 161, 166, 169, 174, 177, 182, 187, 192, 195, 200, 205, 210, 215, 220, 225, 230, 238, 243, 248, 256},
            {253, 246, 241, 233, 228, 223, 215, 210, 205, 200, 195, 189, 184, 182, 177, 172, 169, 164, 159, 156, 154, 148, 146, 143, 138, 136, 133, 131, 128, 125, 125, 123, 120, 118, 115, 113, 108, 105, 102, 97, 95, 92, 87, 82, 79, 74, 69, 67, 61, 56, 51, 46, 41, 36, 28, 23, 18, 10, 5, 0}
        };
        for (int[] fade : fades) {
            StringBuilder xml = new StringBuilder();
            for (int f = 0; f < fade.length;) {
                int last = f + 1;
                while (last < fade.length && fade[last] == fade[f]) {
                    last++;
                }
                xml.append(frame(f, last - f, "a=\"1.99993896484375\" d=\"2\"")
                        .replace("alphaMultiplier=\"0.5\"", "alphaMultiplier=\"" + fade[f] / 256.0 + "\""));
                f = last;
            }
            Document document = detect(xml.toString(), 120, Collections.emptySet());
            NodeList frames = document.getElementsByTagName("DOMFrame");
            Assert.assertEquals(frames.getLength(), 1);
            Assert.assertEquals(((Element) frames.item(0)).getAttribute("tweenType"), "motion object");
            Assert.assertEquals(((Element) frames.item(0)).getAttribute("duration"), Integer.toString(fade.length));
            Element timeMap = (Element) document.getElementsByTagName("TimeMap").item(1);
            Assert.assertEquals(timeMap.getAttribute("type"), "DualQuadratic");
            int strength = Integer.parseInt(timeMap.getAttribute("strength"));
            Assert.assertEquals(strength, 50);
            NodeList properties = document.getElementsByTagName("Property");
            Element alpha = null;
            for (int i = 0; i < properties.getLength(); i++) {
                Element property = (Element) properties.item(i);
                if ("Alpha_Amount".equals(property.getAttribute("id"))) {
                    alpha = property;
                }
            }
            Assert.assertNotNull(alpha);
            Assert.assertEquals(alpha.getAttribute("TimeMapIndex"), "1");
            NodeList keys = alpha.getElementsByTagName("Keyframe");
            double begin = ordinate((Element) keys.item(0), "anchor");
            double finish = ordinate((Element) keys.item(1), "anchor");
            if (fade[0] == 253) {
                // Adobe still produced alpha 84 at frame 591 after adding a
                // float-safe margin. The original whole endpoints fit every
                // observation and avoid this fractional endpoint ambiguity.
                Assert.assertEquals(begin, 99.0);
                Assert.assertEquals(finish, 0.0);
            }
            for (int f = 0; f < fade.length; f++) {
                double t = f / (double) (fade.length - 1);
                double progress = t + strength / 100.0 * (t < 0.5 ? t : 1 - t) * (1 - 2 * t);
                long actual = Math.round(Math.floor(begin + (finish - begin) * progress + 1e-8) * 256 / 100.0);
                Assert.assertEquals(actual, (long) fade[f], "alpha at frame " + f);
                // Check that the fit does not rely on double-only precision
                // at an integer percentage boundary.
                float percent = (float) (begin + (finish - begin) * progress);
                long compiled = Math.round(Math.floor(percent) * 256 / 100.0);
                Assert.assertEquals(compiled, (long) fade[f], "single-precision alpha at frame " + f);
            }
            Assert.assertEquals(value(document, "Scale_X", 0), 199.993896484375, 1e-8);
            Assert.assertEquals(value(document, "Scale_Y", 0), 200.0, 1e-8);
        }
    }
    @Test
    public void fitsIndependentCurvesAndPreservesLastFrameHold() throws Exception {
        Document document = detect(cubicFrames(21, 5), 30, Collections.emptySet());
        NodeList frames = document.getElementsByTagName("DOMFrame");
        Assert.assertEquals(frames.getLength(), 2);
        Element first = (Element) frames.item(0);
        Assert.assertEquals(first.getAttribute("tweenType"), "motion object");
        Assert.assertEquals(first.getAttribute("keyMode"), "8195");
        Assert.assertEquals(first.getAttribute("isMotionObject"), "true");
        Assert.assertEquals(first.getAttribute("duration"), "21");
        Element hold = (Element) frames.item(1);
        Assert.assertEquals(hold.getAttribute("index"), "21");
        Assert.assertEquals(hold.getAttribute("duration"), "4");
        Element core = (Element) document.getElementsByTagName("AnimationCore").item(0);
        Assert.assertEquals(Double.parseDouble(core.getAttribute("TimeScale")), 30000.0);
        Assert.assertEquals(core.getAttribute("duration"), "21000");
        Element settings = (Element) document.getElementsByTagName("Settings").item(0);
        Assert.assertEquals(settings.getAttribute("xformPtXOffsetPct"), "0.25");
        Assert.assertEquals(settings.getAttribute("xformPtYOffsetPct"), "0.75");
        for (int f = 0; f <= 20; f++) {
            double t = f / 20.0;
            Assert.assertEquals(value(document, "Motion_X", t), 200 * t * t * t, 0.051);
            Assert.assertEquals(value(document, "Motion_Y", t), 0.0, 0.051);
        }
        NodeList colors = document.getElementsByTagName("Color");
        Assert.assertEquals(((Element) colors.item(0)).getAttribute("alphaMultiplier"), "0.5");
    }

    @Test
    public void writesIndependentAxisAnglesWithStableNumericValues() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 20; f++) {
            double t = f / 20.0;
            double angle = Math.toRadians(30 + 270 * t);
            double sx = 2 * (1 + t * t);
            double sy = 3 * (1 + t);
            String matrix = "a=\"" + Math.round(sx * Math.cos(angle) * 65536) / 65536.0
                    + "\" b=\"" + Math.round(sx * Math.sin(angle) * 65536) / 65536.0
                    + "\" c=\"" + Math.round(-sy * Math.sin(angle) * 65536) / 65536.0
                    + "\" d=\"" + Math.round(sy * Math.cos(angle) * 65536) / 65536.0 + "\"";
            xml.append(frame(f, 1, matrix));
        }
        Document document = detect(xml.toString(), 24, Collections.emptySet());
        assertMotionMatrixFidelity(document, xml.toString());
    }

    @Test
    public void writesValidatedWholeDegreeKeysExactlyForNativeCS6() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 60; f++) {
            double angle = Math.toRadians(30 + 9 * f);
            double a = Math.round(Math.cos(angle) * 65536) / 65536.0;
            double b = Math.round(Math.sin(angle) * 65536) / 65536.0;
            xml.append(frame(f, 1, "a=\"" + a + "\" b=\"" + b
                    + "\" c=\"" + -b + "\" d=\"" + a + "\""));
        }
        Document document = detect(xml.toString(), 30, Collections.emptySet());
        assertMotionMatrixFidelity(document, xml.toString());
        NodeList properties = document.getElementsByTagName("Property");
        int checked = 0;
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            if (!"Skew_X".equals(property.getAttribute("id"))
                    && !"Skew_Y".equals(property.getAttribute("id"))) {
                continue;
            }
            NodeList keys = property.getElementsByTagName("Keyframe");
            Assert.assertEquals(keys.getLength(), 61);
            for (int f = 0; f < keys.getLength(); f++) {
                Element key = (Element) keys.item(f);
                String expected = "0," + (30 + 9 * f);
                Assert.assertEquals(key.getAttribute("anchor"), expected, "native degree boundary at frame " + f);
                Assert.assertEquals(key.getAttribute("previous"), expected);
                Assert.assertEquals(key.getAttribute("next"), expected);
            }
            checked++;
        }
        Assert.assertEquals(checked, 2);
    }

    @Test
    public void preservesStaticAndAnimatedSkewMatrices() throws Exception {
        for (boolean animated : new boolean[]{false, true}) {
            StringBuilder xml = new StringBuilder();
            double[][] matrices = new double[31][4];
            for (int f = 0; f <= 30; f++) {
                double t = f / 30.0;
                double rotation = Math.toRadians(25 + 90 * t);
                double skew = Math.toRadians(-15 + (animated ? 15 * t : 0));
                double sx = 1.5 + t * t * t;
                double sy = 2 - 0.5 * t;
                double[] m = {sx * Math.cos(rotation), sx * Math.sin(rotation),
                    -sy * Math.sin(rotation + skew), sy * Math.cos(rotation + skew)};
                StringBuilder attrs = new StringBuilder();
                String[] names = {"a", "b", "c", "d"};
                for (int i = 0; i < m.length; i++) {
                    matrices[f][i] = Math.round(m[i] * 65536) / 65536.0;
                    attrs.append(names[i]).append("=\"").append(matrices[f][i]).append("\" ");
                }
                xml.append(frame(f, 1, attrs.toString()));
            }
            Document document = detect(xml.toString(), 30, Collections.emptySet());
            assertMotionMatrixFidelity(document, xml.toString());
        }
    }
    private double dualProgress(double t, int strength) {
        return t + strength / 100.0 * (t < 0.5 ? t : 1 - t) * (1 - 2 * t);
    }


    @Test
    public void validatesIndependentDualQuadraticColorsAtNativePrecision() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 40; f++) {
            double t = f / 40.0;
            double x = Math.round((30 + 200 * dualProgress(t, 50)) * 20) / 20.0;
            double y = Math.round((-10 + 100 * dualProgress(t, -75)) * 20) / 20.0;
            double sx = Math.round((1 + dualProgress(t, 40)) * 65536) / 65536.0;
            double[] colors = {0.25 + dualProgress(t, -60), 1, 0.75, 0.5,
                -80 + 160 * dualProgress(t, 80), 0, 0, 10};
            colors[0] = Math.round(colors[0] * 256) / 256.0;
            colors[4] = Math.round(colors[4]);
            xml.append(colorFrame(f, 1, "tx=\"" + x + "\" ty=\"" + y
                    + "\" a=\"" + sx + "\"", colors));
        }
        Document document = detect(xml.toString(), 40, Collections.emptySet());
        assertNativeColorFidelity(document, xml.toString());
    }

    @Test
    public void preservesDualQuadraticRotationWithIndependentSkew() throws Exception {
        StringBuilder xml = new StringBuilder();
        double[][] matrices = new double[17][4];
        for (int f = 0; f <= 16; f++) {
            double t = f / 16.0;
            double angle = Math.toRadians(20 + 128 * dualProgress(t, 50));
            double skew = Math.toRadians(-10 + 64 * dualProgress(t, -50));
            double[] m = {2 * Math.cos(angle), 2 * Math.sin(angle),
                -3 * Math.sin(angle + skew), 3 * Math.cos(angle + skew)};
            String[] names = {"a", "b", "c", "d"};
            StringBuilder matrix = new StringBuilder();
            for (int i = 0; i < m.length; i++) {
                matrices[f][i] = Math.round(m[i] * 65536) / 65536.0;
                matrix.append(names[i]).append("=\"").append(matrices[f][i]).append("\" ");
            }
            xml.append(frame(f, 1, matrix.toString()));
        }
        Document document = detect(xml.toString(), 40, Collections.emptySet());
        assertMotionMatrixFidelity(document, xml.toString());
    }

    @Test
    public void preservesReflectedMatricesWithRotationAndSkew() throws Exception {
        StringBuilder xml = new StringBuilder();
        double[][] matrices = new double[31][4];
        for (int f = 0; f <= 30; f++) {
            double t = f / 30.0;
            double angle = Math.toRadians(10 + 90 * t);
            double skew = Math.toRadians(-10 + 30 * t);
            double sx = 1 + t * t * t;
            double sy = -2 + 0.5 * t * t;
            double[] m = {sx * Math.cos(angle), sx * Math.sin(angle),
                -sy * Math.sin(angle + skew), sy * Math.cos(angle + skew)};
            String[] names = {"a", "b", "c", "d"};
            StringBuilder attrs = new StringBuilder();
            for (int i = 0; i < m.length; i++) {
                matrices[f][i] = Math.round(m[i] * 65536) / 65536.0;
                attrs.append(names[i]).append("=\"").append(matrices[f][i]).append("\" ");
            }
            xml.append(frame(f, 1, attrs.toString()));
        }
        Document document = detect(xml.toString(), 30, Collections.emptySet());
        assertMotionMatrixFidelity(document, xml.toString());
    }

    @Test
    public void preservesNativeUnsafeSkewFixturesAsOriginalFrames() throws Exception {
        for (String id : new String[]{"05_animated_skew", "16_rotation_skew_dual_quadratic"}) {
            java.util.List<String> rows = java.nio.file.Files.readAllLines(
                    java.nio.file.Paths.get("testdata", "motion_object", id, "expected.csv"),
                    java.nio.charset.StandardCharsets.UTF_8);
            StringBuilder xml = new StringBuilder();
            String[] names = {"a", "b", "c", "d", "tx", "ty"};
            for (int f = 1; f < rows.size(); f++) {
                String[] values = rows.get(f).split(",");
                StringBuilder matrix = new StringBuilder();
                for (int i = 0; i < names.length; i++) {
                    matrix.append(names[i]).append("=\"")
                            .append(Double.parseDouble(values[i + 1]) / (i < 4 ? 65536 : 20))
                            .append("\" ");
                }
                xml.append(frame(f - 1, f == rows.size() - 1 ? 5 : 1, matrix.toString()));
            }
            // Motion detection must not replace these with ideal sin/cos
            // curves: their existing CS6 publications differ by 201/644 units.
            String expected = ClassicTweenDetector.detect(xml.toString(), Collections.emptySet());
            String actual = ClassicTweenDetector.detect(xml.toString(), Collections.emptySet(), 30,
                    Collections.singletonMap("Symbol 1", new double[]{0.25, 0.75}));
            Assert.assertEquals(actual, expected, id + ": retain the lossless fallback and final hold");
            Assert.assertFalse(actual.contains("motion object"));
        }
    }

    @Test
    public void writesQuarterDegreeKeysExactlyForNativeCS6() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 20; f++) {
            double x = Math.toRadians(25 + f * 0.25);
            double y = Math.toRadians(-10 - f * 0.5);
            double sx = 1 + Math.pow(f / 20.0, 3);
            double[] m = {sx * Math.cos(x), sx * Math.sin(x), -Math.sin(y), Math.cos(y)};
            StringBuilder matrix = new StringBuilder();
            String[] names = {"a", "b", "c", "d"};
            for (int i = 0; i < m.length; i++) {
                matrix.append(names[i]).append("=\"")
                        .append(Math.round(m[i] * 65536) / 65536.0).append("\" ");
            }
            xml.append(frame(f, 1, matrix.toString()));
        }
        Document document = detect(xml.toString(), 30, Collections.emptySet());
        assertMotionMatrixFidelity(document, xml.toString());
        for (int f = 0; f <= 20; f++) {
            Assert.assertEquals(value(document, "Skew_Y", f / 20.0), 25 + f * 0.25, 1e-10);
            Assert.assertEquals(value(document, "Skew_X", f / 20.0), -10 - f * 0.5, 1e-10);
        }
    }

    @Test
    public void preservesCollapsedScaleKeyframe() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 20; f++) {
            double t = f / 20.0;
            xml.append(frame(f, 1, "a=\"" + (1 - 2 * t) + "\" tx=\"" + 200 * t * t * t + "\""));
        }
        Document document = detect(xml.toString(), 30, Collections.emptySet());
        NodeList frames = document.getElementsByTagName("DOMFrame");
        boolean preserved = false;
        for (int i = 0; i < frames.getLength(); i++) {
            Element keyframe = (Element) frames.item(i);
            if ("10".equals(keyframe.getAttribute("index"))) {
                Assert.assertFalse(keyframe.hasAttribute("tweenType"));
                preserved = true;
            }
        }
        Assert.assertTrue(preserved, "Collapsed scale must remain an explicit keyframe");
    }
    @Test
    public void keepsClassicTweensAndHonorsFormatAndInstanceBoundaries() throws Exception {
        StringBuilder linear = new StringBuilder();
        for (int f = 0; f <= 10; f++) {
            linear.append(frame(f, 1, "tx=\"" + f * 10 + "\""));
        }
        Document document = detect(linear.toString(), 24, Collections.emptySet());
        Assert.assertEquals(((Element) document.getElementsByTagName("DOMFrame").item(0)).getAttribute("tweenType"), "motion");
        Assert.assertEquals(detect(cubicFrames(11, 1), 0, Collections.emptySet()).getElementsByTagName("AnimationCore").getLength(), 0);
        document = detect(cubicFrames(21, 1), 24, Collections.singleton(10));
        NodeList frames = document.getElementsByTagName("DOMFrame");
        for (int i = 0; i < frames.getLength(); i++) {
            Element frame = (Element) frames.item(i);
            int start = Integer.parseInt(frame.getAttribute("index"));
            int duration = frame.hasAttribute("duration") ? Integer.parseInt(frame.getAttribute("duration")) : 1;
            Assert.assertTrue(start >= 10 || start + duration <= 10);
        }
    }

    @Test
    public void preservesIrregularMotionAndColorSamples() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f < 11; f++) {
            xml.append(frame(f, 1, "tx=\"" + (f % 2 == 0 ? 0 : 100) + "\""));
        }
        Assert.assertEquals(detect(xml.toString(), 24, Collections.emptySet()).getElementsByTagName("AnimationCore").getLength(), 0);
        xml.setLength(0);
        for (int f = 0; f <= 10; f++) {
            double t = f / 10.0;
            xml.append(frame(f, 1, "tx=\"" + 200 * t * t * t + "\"")
                    .replace("alphaMultiplier=\"0.5\"", "alphaMultiplier=\"" + (f % 2 == 0 ? 0.25 : 0.75) + "\""));
        }
        Assert.assertEquals(detect(xml.toString(), 24, Collections.emptySet())
                .getElementsByTagName("AnimationCore").getLength(), 0);
    }

    private static final String[] COLOR_ATTRIBUTES = {"redMultiplier", "greenMultiplier", "blueMultiplier",
        "alphaMultiplier", "redOffset", "greenOffset", "blueOffset", "alphaOffset"};
    private static final String[] COLOR_IDS = {"AdvClr_R_Pct", "AdvClr_G_Pct", "AdvClr_B_Pct",
        "AdvClr_A_Pct", "AdvClr_R_Offset", "AdvClr_G_Offset", "AdvClr_B_Offset", "AdvClr_A_Offset"};

    private double[] colorValues(double t) {
        // Signed offsets and multipliers outside 0..1 are legal SWF transforms.
        double[] colors = {0.25 + t * t, 1.5 - t * t * t, 0.75,
            0.5 + t / 2, -100 + 200 * t * t * t, 20 + 150 * t,
            80 - 160 * t * t, -40 + 80 * t * t * t};
        for (int i = 0; i < colors.length; i++) {
            colors[i] = i < 4 ? Math.round(colors[i] * 256) / 256.0 : Math.round(colors[i]);
        }
        return colors;
    }

    private String colorFrame(int f, int duration, String matrix, double[] colors) {
        StringBuilder attributes = new StringBuilder();
        for (int i = 0; i < colors.length; i++) {
            attributes.append(COLOR_ATTRIBUTES[i]).append("=\"").append(colors[i]).append("\" ");
        }
        return frame(f, duration, matrix).replace("alphaMultiplier=\"0.5\"", attributes.toString());
    }

    @Test
    public void validatesAdvancedColorsWithoutGeometricMotionAtNativePrecision() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 20; f++) {
            xml.append(colorFrame(f, 1, "", colorValues(f / 20.0)));
        }
        Document document = detect(xml.toString(), 24, Collections.emptySet());
        assertNativeColorFidelity(document, xml.toString());
    }

    @Test
    public void validatesAdvancedColorsWithMotionAndPreservesEndHold() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 20; f++) {
            double t = f / 20.0;
            xml.append(colorFrame(f, f == 20 ? 4 : 1,
                    "tx=\"" + Math.round(4000 * t * t * t) / 20.0 + "\"", colorValues(t)));
        }
        Document document = detect(xml.toString(), 30, Collections.emptySet());
        assertNativeColorFidelity(document, xml.toString());
    }

    @Test
    public void validatesQuantizedColorsWithCoalescedHoldsAtNativePrecision() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 60;) {
            int multiplier = (int) Math.round((0.25 + Math.pow(f / 60.0, 3)) * 256);
            int next = f + 1;
            while (next <= 60 && Math.round((0.25 + Math.pow(next / 60.0, 3)) * 256) == multiplier) {
                next++;
            }
            xml.append(colorFrame(f, next - f, "",
                    new double[]{multiplier / 256.0, 1.5, 0.75, 0.5, -20, 40, 0, 10}));
            f = next;
        }
        Document document = detect(xml.toString(), 60, Collections.emptySet());
        assertNativeColorFidelity(document, xml.toString());
    }
    private double piecewiseX(double t) {
        if (t <= 0.4) {
            double u = t / 0.4;
            return 100 * u * u * u;
        }
        double u = (t - 0.4) / 0.6;
        return 100 + 120 * u * u;
    }

    private double piecewiseY(double t) {
        if (t <= 0.6) {
            double u = t / 0.6;
            return 80 * u * u;
        }
        double u = (t - 0.6) / 0.4;
        return 80 - 60 * u * u * u;
    }

    private double piecewiseRed(double t) {
        if (t <= 0.5) {
            double u = t * 2;
            return 0.25 + 0.75 * u * u;
        }
        double u = (t - 0.5) * 2;
        return 1 - 0.5 * u * u * u;
    }


    @Test
    public void validatesMultipleSegmentsAtNativeColorPrecision() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 60; f++) {
            double t = f / 60.0;
            double red = Math.round(piecewiseRed(t) * 256) / 256.0;
            xml.append(colorFrame(f, f == 60 ? 4 : 1,
                    "tx=\"" + Math.round((30 + piecewiseX(t)) * 20) / 20.0
                    + "\" ty=\"" + Math.round((10 + piecewiseY(t)) * 20) / 20.0 + "\"",
                    new double[]{red, 1, 0.75, 0.5, -20, 0, 0, 0}));
        }
        Document document = detect(xml.toString(), 30, Collections.emptySet());
        assertNativeColorFidelity(document, xml.toString());
    }

    @Test
    public void fitsThreeSegmentsWithoutSplittingTheMotionObject() throws Exception {
        StringBuilder xml = new StringBuilder();
        double[] expected = new double[61];
        for (int f = 0; f <= 60; f++) {
            double t = f / 60.0;
            double u = f < 20 ? f / 20.0 : f < 40 ? (f - 20) / 20.0 : (f - 40) / 20.0;
            expected[f] = f < 20 ? 60 * u * u * u : f < 40 ? 60 - 40 * u * u : 20 + 120 * u * u * u;
            expected[f] = Math.round(expected[f] * 20) / 20.0;
            xml.append(frame(f, 1, "tx=\"" + (80 + expected[f]) + "\" ty=\""
                    + Math.round((100 + 60 * t * t) * 20) / 20.0 + "\""));
        }
        Document document = detect(xml.toString(), 30, Collections.emptySet());
        Assert.assertEquals(document.getElementsByTagName("DOMFrame").getLength(), 1);
        NodeList properties = document.getElementsByTagName("Property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            if ("Motion_X".equals(property.getAttribute("id"))) {
                Assert.assertTrue(property.getElementsByTagName("Keyframe").getLength() >= 4);
            }
        }
        for (int f = 0; f <= 60; f++) {
            Assert.assertEquals(value(document, "Motion_X", f / 60.0), expected[f], 0.051);
        }
    }
    private double attribute(Element matrix, String name, double fallback) {
        return matrix.hasAttribute(name) ? Double.parseDouble(matrix.getAttribute(name)) : fallback;
    }

    private void assertMotionMatrixFidelity(Document exported, String source) throws Exception {
        Document reference = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader("<frames>" + source + "</frames>")));
        NodeList originals = reference.getElementsByTagName("DOMFrame");
        NodeList frames = exported.getElementsByTagName("DOMFrame");
        Assert.assertTrue(exported.getElementsByTagName("AnimationCore").getLength() > 0);
        for (int i = 0; i < frames.getLength(); i++) {
            Element frame = (Element) frames.item(i);
            if (!"motion object".equals(frame.getAttribute("tweenType"))) {
                continue;
            }
            int start = Integer.parseInt(frame.getAttribute("index"));
            int end = start + Integer.parseInt(frame.getAttribute("duration")) - 1;
            Document core = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            core.appendChild(core.importNode(frame.getElementsByTagName("AnimationCore").item(0), true));
            Element initial = (Element) frame.getElementsByTagName("Matrix").item(0);
            for (int f = start; f <= end; f++) {
                Element original = null;
                for (int j = 0; j < originals.getLength(); j++) {
                    Element candidate = (Element) originals.item(j);
                    if (Integer.parseInt(candidate.getAttribute("index")) <= f) {
                        original = candidate;
                    }
                }
                Element matrix = (Element) original.getElementsByTagName("Matrix").item(0);
                double t = (f - start) / (double) (end - start);
                double rotation = Math.toRadians(value(core, "Rotation_Z", t) + value(core, "Skew_Y", t));
                double otherAngle = Math.toRadians(value(core, "Rotation_Z", t) + value(core, "Skew_X", t));
                double sx = value(core, "Scale_X", t) / 100;
                double sy = value(core, "Scale_Y", t) / 100;
                double[] actual = {sx * Math.cos(rotation), sx * Math.sin(rotation),
                    -sy * Math.sin(otherAngle), sy * Math.cos(otherAngle)};
                String[] names = {"a", "b", "c", "d"};
                for (int j = 0; j < actual.length; j++) {
                    Assert.assertEquals(actual[j], attribute(matrix, names[j], j == 0 || j == 3 ? 1 : 0),
                            2.0 / 65536, names[j] + " at frame " + f);
                }
                Assert.assertEquals(attribute(initial, "tx", 0) + value(core, "Motion_X", t),
                        attribute(matrix, "tx", 0), 0.051);
                Assert.assertEquals(attribute(initial, "ty", 0) + value(core, "Motion_Y", t),
                        attribute(matrix, "ty", 0), 0.051);
            }
            NodeList keys = core.getElementsByTagName("Keyframe");
            for (int j = 0; j < keys.getLength(); j++) {
                Element key = (Element) keys.item(j);
                for (String field : new String[]{"anchor", "previous", "next"}) {
                    Assert.assertFalse(key.getAttribute(field).contains("E"), "Motion coordinates use plain decimals");
                }
            }
        }
    }
    /**
     * Model the percentage quantization and clipping observed in native CS6
     * publishes, rather than testing only the ideal scalar Bezier curve.
     */
    private void assertNativeColorFidelity(Document exported, String source) throws Exception {
        Document reference = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                new InputSource(new StringReader("<frames>" + source + "</frames>")));
        NodeList originals = reference.getElementsByTagName("DOMFrame");
        NodeList frames = exported.getElementsByTagName("DOMFrame");
        int nextFrame = 0;
        for (int i = 0; i < frames.getLength(); i++) {
            Element frame = (Element) frames.item(i);
            int index = Integer.parseInt(frame.getAttribute("index"));
            int duration = frame.hasAttribute("duration") ? Integer.parseInt(frame.getAttribute("duration")) : 1;
            Assert.assertEquals(index, nextFrame, "No frame may be dropped or duplicated");
            nextFrame += duration;
            Element sourceFrame = null;
            for (int j = 0; j < originals.getLength(); j++) {
                Element candidate = (Element) originals.item(j);
                if (Integer.parseInt(candidate.getAttribute("index")) <= index) {
                    sourceFrame = candidate;
                }
            }
            Assert.assertTrue(frame.getElementsByTagName("elements").item(0).isEqualNode(
                    sourceFrame.getElementsByTagName("elements").item(0)),
                    "Keyframe elements must retain the original matrix and RGBA transform");
            if (!"motion object".equals(frame.getAttribute("tweenType"))) {
                continue;
            }
            int start = Integer.parseInt(frame.getAttribute("index"));
            int end = start + Integer.parseInt(frame.getAttribute("duration")) - 1;
            Document core = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            core.appendChild(core.importNode(frame.getElementsByTagName("AnimationCore").item(0), true));
            for (int f = start; f <= end; f++) {
                Element original = null;
                for (int j = 0; j < originals.getLength(); j++) {
                    Element candidate = (Element) originals.item(j);
                    if (Integer.parseInt(candidate.getAttribute("index")) <= f) {
                        original = candidate;
                    }
                }
                Element color = (Element) original.getElementsByTagName("Color").item(0);
                for (int channel = 0; channel < 8; channel++) {
                    double expected = color.hasAttribute(COLOR_ATTRIBUTES[channel])
                            ? Double.parseDouble(color.getAttribute(COLOR_ATTRIBUTES[channel]))
                            : channel < 4 ? 1 : 0;
                    double actual = value(core, COLOR_IDS[channel], (f - start) / (double) (end - start));
                    if (channel < 4) {
                        NodeList properties = core.getElementsByTagName("Property");
                        for (int j = 0; j < properties.getLength(); j++) {
                            Element property = (Element) properties.item(j);
                            boolean explicitKey = false;
                            NodeList keys = property.getElementsByTagName("Keyframe");
                            for (int k = 0; k < keys.getLength(); k++) {
                                explicitKey |= Long.parseLong(((Element) keys.item(k)).getAttribute("timevalue"))
                                        == (long) (f - start) * 1000;
                            }
                            if (COLOR_IDS[channel].equals(property.getAttribute("id"))) {
                                if (keys.getLength() > 2 && explicitKey) {
                                    actual = Math.floor(actual);
                                } else if (keys.getLength() > 1 && !explicitKey && f != start && f != end) {
                                    actual = Math.max(0, Math.min(100, Math.floor(actual)));
                                }
                            }
                        }
                        actual = Math.round(actual * 256 / 100) / 256.0;
                    }
                    Assert.assertEquals(actual, expected, channel < 4 ? 1.0 / 256 : 1.0,
                            COLOR_IDS[channel] + " after CS6 quantization at frame " + f);
                }
            }
        }
    }

    @Test
    public void preservesChangingMultipliersAboveOneHundredPercent() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 60; f++) {
            double red = Math.round((1.25 + 0.75 * Math.pow(f / 60.0, 3)) * 256) / 256.0;
            xml.append(colorFrame(f, 1, "", new double[]{red, 1, 1, 0.5, 0, 0, 0, 0}));
        }
        Document document = detect(xml.toString(), 30, Collections.emptySet());
        Assert.assertEquals(document.getElementsByTagName("AnimationCore").getLength(), 1);
        assertNativeColorFidelity(document, xml.toString());
    }
    @Test
    public void preservesPairedCubicPathTiming() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 60; f++) {
            double t = f / 60.0;
            xml.append(frame(f, 1, "tx=\"" + Math.round((80 + 300 * t * t * t) * 20) / 20.0
                    + "\" ty=\"" + Math.round((100 + 100 * t * t) * 20) / 20.0 + "\""));
        }
        Document document = detect(xml.toString(), 30, Collections.emptySet());
        assertMotionMatrixFidelity(document, xml.toString());
    }
    @Test
    public void sampledPathKeysPreserveRestartAndFinalHold() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 20; f++) {
            double t = f / 20.0;
            xml.append(frame(f, f == 20 ? 5 : 1,
                    "tx=\"" + Math.round((80 + 300 * t * t * t) * 20) / 20.0
                    + "\" ty=\"" + Math.round((100 + 100 * t * t) * 20) / 20.0 + "\""));
        }
        Document document = detect(xml.toString(), 30, Collections.singleton(10));
        Assert.assertEquals(document.getElementsByTagName("AnimationCore").getLength(), 2);
        NodeList frames = document.getElementsByTagName("DOMFrame");
        Assert.assertEquals(frames.getLength(), 3);
        Assert.assertEquals(((Element) frames.item(0)).getAttribute("duration"), "10");
        Assert.assertEquals(((Element) frames.item(1)).getAttribute("index"), "10");
        Assert.assertEquals(((Element) frames.item(2)).getAttribute("index"), "21");
        Assert.assertEquals(((Element) frames.item(2)).getAttribute("duration"), "4");
        assertMotionMatrixFidelity(document, xml.toString());
        NodeList properties = document.getElementsByTagName("Property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element property = (Element) properties.item(i);
            if (!"Motion_X".equals(property.getAttribute("id")) && !"Motion_Y".equals(property.getAttribute("id"))) {
                continue;
            }
            NodeList keys = property.getElementsByTagName("Keyframe");
            for (int key = 0; key < keys.getLength(); key++) {
                Element element = (Element) keys.item(key);
                Assert.assertEquals(element.getAttribute("timevalue"), Integer.toString(key * 1000));
                Assert.assertEquals(element.getAttribute("roving"), "0");
                Assert.assertEquals(element.getAttribute("previous"), element.getAttribute("anchor"));
                Assert.assertEquals(element.getAttribute("next"), element.getAttribute("anchor"));
            }
        }
    }
    @Test
    public void restartFixtureRemovesThePreviousInstanceBeforePlacing() throws Exception {
        com.jpexs.decompiler.flash.SWF generated = MotionTweenFixtureGenerator.movie(14);
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        generated.saveTo(bytes);
        com.jpexs.decompiler.flash.SWF swf = new com.jpexs.decompiler.flash.SWF(
                new java.io.ByteArrayInputStream(bytes.toByteArray()), true);
        Assert.assertEquals(swf.getTimeline().getFrame(29).layers.get(1).matrix.translateX, 2277);
        Assert.assertEquals(swf.getTimeline().getFrame(30).layers.get(1).matrix.translateX, 2350);
        Assert.assertEquals(swf.getTimeline().getFrame(30).layers.get(1).matrix.translateY, 2500);
        Assert.assertFalse(swf.getTimeline().getFrame(30).layers.get(1).placeObjectTag.flagMove());
    }
    @Test
    public void flaExportWritesMotionLayerAndFrame() throws Exception {
        com.jpexs.decompiler.flash.SWF swf = new com.jpexs.decompiler.flash.SWF();
        swf.frameCount = 21;
        swf.frameRate = 30;
        com.jpexs.decompiler.flash.tags.DefineSpriteTag sprite = new com.jpexs.decompiler.flash.tags.DefineSpriteTag(swf);
        sprite.frameCount = 1;
        sprite.addTag(new com.jpexs.decompiler.flash.tags.ShowFrameTag(swf));
        swf.addTag(sprite);
        for (int f = 0; f <= 20; f++) {
            double t = f / 20.0;
            com.jpexs.decompiler.flash.types.MATRIX matrix = new com.jpexs.decompiler.flash.types.MATRIX();
            matrix.hasScale = true;
            matrix.scaleX = (float) (Math.round((1 + t * t * t) * 65536) / 65536.0);
            matrix.scaleY = (float) (Math.round((1 + 0.5 * t * t) * 65536) / 65536.0);
            double[] colors = {1, 1, 1, 0.5, 0, 0, 0, 0};
            com.jpexs.decompiler.flash.types.CXFORMWITHALPHA color = new com.jpexs.decompiler.flash.types.CXFORMWITHALPHA();
            color.hasMultTerms = true;
            color.hasAddTerms = true;
            color.redMultTerm = (int) Math.round(colors[0] * 256);
            color.greenMultTerm = (int) Math.round(colors[1] * 256);
            color.blueMultTerm = (int) Math.round(colors[2] * 256);
            color.alphaMultTerm = (int) Math.round(colors[3] * 256);
            color.redAddTerm = (int) colors[4];
            color.greenAddTerm = (int) colors[5];
            color.blueAddTerm = (int) colors[6];
            color.alphaAddTerm = (int) colors[7];
            swf.addTag(new com.jpexs.decompiler.flash.tags.PlaceObject2Tag(swf,
                    f != 0, 1, f == 0 ? sprite.spriteId : -1, matrix, color, -1, null, -1, null));
            swf.addTag(new com.jpexs.decompiler.flash.tags.ShowFrameTag(swf));
        }
        java.nio.file.Path output = java.nio.file.Files.createTempDirectory("motion-tween-export");
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
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(zip.getInputStream(zip.getEntry("DOMDocument.xml")));
                Assert.assertEquals(((Element) document.getElementsByTagName("DOMLayer").item(0))
                        .getAttribute("animationType"), "motion object");
                Assert.assertEquals(((Element) document.getElementsByTagName("DOMFrame").item(0))
                        .getAttribute("duration"), "21");
                Assert.assertEquals(document.getElementsByTagName("AnimationCore").getLength(), 1);
                Assert.assertEquals(value(document, "Scale_X", 0.5), 112.5, 0.002);
            }
        } finally {
            try (java.util.stream.Stream<java.nio.file.Path> paths = java.nio.file.Files.walk(output)) {
                paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
            }
        }
    }
    @Test
    public void preservesPresetEasingSamplesAndIndependentChannels() throws Exception {
        for (PresetEasing.Family family : PresetEasing.Family.values()) {
            for (double blend : new double[]{0.4, 1}) {
                StringBuilder frames = new StringBuilder();
                for (int f = 0; f <= 60; f++) {
                    double t = f / 60.0;
                    double x = 160 + 260 * (t + blend * (PresetEasing.progress(family, 0, t) - t));
                    double y = 100 + 120 * (t + blend * (PresetEasing.progress(family, 1, t) - t));
                    double sx = 1 + 0.5 * (t + blend * (PresetEasing.progress(family, 2, t) - t));
                    frames.append(frame(f, 1, "tx=\"" + Math.round(x * 20) / 20.0
                            + "\" ty=\"" + Math.round(y * 20) / 20.0
                            + "\" a=\"" + Math.round(sx * 65536) / 65536.0 + "\""));
                }
                Document doc = detect(frames.toString(), 30, Collections.emptySet());
                Assert.assertEquals(doc.getElementsByTagName("motionObjectXML").getLength(), 1,
                        family + " blend=" + blend);
                for (int f = 0; f <= 60; f++) {
                    double t = f / 60.0;
                    double x = 260 * (t + blend * (PresetEasing.progress(family, 0, t) - t));
                    double y = 120 * (t + blend * (PresetEasing.progress(family, 1, t) - t));
                    Assert.assertEquals(value(doc, "Motion_X", t), x, 0.051, family.toString());
                    Assert.assertEquals(value(doc, "Motion_Y", t), y, 0.051, family.toString());
                }
            }
        }
    }

    @Test
    public void rejectsIrregularAnimationDespitePresetEndpoints() throws Exception {
        StringBuilder frames = new StringBuilder();
        for (int f = 0; f <= 60; f++) {
            double x = f == 0 ? 160 : f == 60 ? 420 : 160 + ((f * 73) % 251);
            frames.append(frame(f, 1, "tx=\"" + x + "\""));
        }
        Document doc = detect(frames.toString(), 30, Collections.emptySet());
        Assert.assertEquals(doc.getElementsByTagName("motionObjectXML").getLength(), 0);
    }

    @Test
    public void preservesCoalescedQuantizedPresetHolds() throws Exception {
        StringBuilder frames = new StringBuilder();
        int first = 0;
        while (first <= 60) {
            double x = Math.round((160 + 26 * Math.pow(2, 10 * (first / 60.0 - 1))) * 20) / 20.0;
            if (first == 0) {
                x = 160;
            }
            int end = first + 1;
            while (end <= 60 && Math.round((160 + 26 * Math.pow(2, 10 * (end / 60.0 - 1))) * 20) / 20.0 == x) {
                end++;
            }
            frames.append(frame(first, end - first, "tx=\"" + x + "\""));
            first = end;
        }
        Assert.assertTrue(frames.toString().contains("duration="), "Exercise coalesced held frames");
        Document doc = detect(frames.toString(), 30, Collections.emptySet());
        Assert.assertEquals(doc.getElementsByTagName("motionObjectXML").getLength(), 1);
        for (int f = 0; f <= 60; f++) {
            double expected = f == 0 ? 0 : Math.round((160 + 26 * Math.pow(2, 10 * (f / 60.0 - 1))) * 20) / 20.0 - 160;
            Assert.assertEquals(value(doc, "Motion_X", f / 60.0), expected, 0.051);
        }
    }

    @Test
    public void sharesNativeQuadraticMapsAcrossIndependentProperties() throws Exception {
        StringBuilder frames = new StringBuilder();
        for (int f = 0; f <= 60; f++) {
            double t = f / 60.0;
            double in = t - 0.4 * t * (1 - t), out = t + 0.65 * t * (1 - t);
            frames.append(frame(f, 1, "tx=\"" + Math.round((160 + 260 * in) * 20) / 20.0
                    + "\" ty=\"" + Math.round((100 + 120 * out) * 20) / 20.0
                    + "\" a=\"" + Math.round((1 + 0.5 * in) * 65536) / 65536.0 + "\""));
        }
        Document doc = detect(frames.toString(), 30, Collections.emptySet());
        Assert.assertEquals(doc.getElementsByTagName("TimeMap").getLength(), 3);
        Assert.assertEquals(((Element) doc.getElementsByTagName("TimeMap").item(1)).getAttribute("type"), "Quadratic");
        Assert.assertEquals(((Element) doc.getElementsByTagName("TimeMap").item(1)).getAttribute("strength"), "-40");
        Assert.assertEquals(((Element) doc.getElementsByTagName("TimeMap").item(2)).getAttribute("strength"), "65");
        FilterTweenDetectorTest helper = new FilterTweenDetectorTest();
        Element x = helper.property(doc, "Motion_X"), scale = helper.property(doc, "Scale_X");
        Assert.assertEquals(x.getAttribute("TimeMapIndex"), scale.getAttribute("TimeMapIndex"));
        Assert.assertEquals(x.getAttribute("ignoreTimeMap"), "0");
        Assert.assertEquals(x.getElementsByTagName("Keyframe").getLength(), 2);
        for (int f = 0; f <= 60; f++) {
            double t = f / 60.0;
            Assert.assertEquals(value(doc, "Motion_X", t), 260 * (t - 0.4 * t * (1 - t)), 0.051);
            Assert.assertEquals(value(doc, "Motion_Y", t), 120 * (t + 0.65 * t * (1 - t)), 0.051);
        }
    }

    @Test
    public void recognizesMeasuredNativeWavesWithOriginalUnderlyingEndpoints() throws Exception {
        java.nio.file.Path root = java.nio.file.Paths.get("testdata/motion_object/native_timemap_measurements");
        for (String type : new String[]{"spring", "bounce", "bounce_in"}) {
            for (int strength : new int[]{1, 2, 3, 4, 5, 6, 8}) {
                String id = type + "_s" + strength + (strength == 6 ? "_validation" : "_601");
                for (String publication : new String[]{"imported", "persisted"}) {
                    java.util.List<String> rows = java.nio.file.Files.readAllLines(
                            root.resolve(id + "/native_capture/samples_" + publication + ".csv"));
                    StringBuilder frames = new StringBuilder();
                    for (int i = 1; i < rows.size(); i++) {
                        String[] fields = rows.get(i).split(",");
                        frames.append(frame(i - 1, 1, "tx=\"" + Integer.parseInt(fields[2]) / 20.0 + "\""));
                    }
                    Document doc = detect(frames.toString(), strength == 6 ? 24 : 30, Collections.emptySet());
                    if (type.equals("bounce_in") && strength == 1 && doc.getElementsByTagName("motionObjectXML").getLength() == 0) {
                        Element classic = (Element) doc.getElementsByTagName("DOMFrame").item(0);
                        Assert.assertEquals(classic.getAttribute("tweenType"), "motion");
                        Assert.assertEquals(classic.getAttribute("acceleration"), "100");
                        continue;
                    }
                    Assert.assertEquals(doc.getElementsByTagName("motionObjectXML").getLength(), 1, id);
                    Element property = new FilterTweenDetectorTest().property(doc, "Motion_X");
                    Assert.assertEquals(property.getElementsByTagName("Keyframe").getLength(), 2, id);
                    Element map = (Element) doc.getElementsByTagName("TimeMap").item(
                            Integer.parseInt(property.getAttribute("TimeMapIndex")));
                    // BounceIn 1 is mathematically identical to Quadratic -100.
                    String expected = type.equals("spring") ? "Spring" : type.equals("bounce") ? "Bounce" : "BounceIn";
                    if (!type.equals("bounce_in") || strength != 1) {
                        Assert.assertEquals(map.getAttribute("type"), expected, id);
                        Assert.assertEquals(map.getAttribute("strength"), Integer.toString(strength), id);
                    }
                    for (int i = 1; i < rows.size(); i++) {
                        String[] fields = rows.get(i).split(",");
                        long predicted = Math.round(20 * value(doc, "Motion_X", (i - 1) / (double) (rows.size() - 2)));
                        Assert.assertTrue(Math.abs(predicted - (Integer.parseInt(fields[2]) - 2000)) <= 1,
                                id + " frame " + (i - 1));
                    }
                }
            }
        }
    }

    @Test
    public void recognizesCapturedNativePolynomialMaps() throws Exception {
        for (String[] capture : new String[][]{{"cubic_50", "Cubic"}, {"quartic_50", "Quartic"},
            {"quintic_50", "Quintic"}, {"dual_cubic_50", "DualCubic"},
            {"dual_quartic_50", "DualQuartic"}, {"dual_quintic_50", "DualQuintic"}}) {
            com.jpexs.decompiler.flash.SWF swf;
            try (java.io.InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(
                    "testdata/motion_object/native_timemap_captures/" + capture[0] + "/reference_native.swf"))) {
                swf = new com.jpexs.decompiler.flash.SWF(in, true);
            }
            StringBuilder frames = new StringBuilder();
            for (int f = 0; f < swf.frameCount; f++) {
                com.jpexs.decompiler.flash.types.MATRIX matrix = swf.getTimeline().getFrame(f).layers.get(1).matrix;
                frames.append(frame(f, 1, "tx=\"" + matrix.translateX / 20.0 + "\" ty=\"" + matrix.translateY / 20.0
                        + "\" a=\"" + matrix.getScaleXInteger() / 65536.0 + "\""));
            }
            Document doc = detect(frames.toString(), swf.frameRate, Collections.emptySet());
            Assert.assertEquals(doc.getElementsByTagName("motionObjectXML").getLength(), 1, capture[0]);
            Element x = new FilterTweenDetectorTest().property(doc, "Motion_X");
            Assert.assertEquals(x.getAttribute("ignoreTimeMap"), "0", capture[0]);
            Assert.assertEquals(x.getElementsByTagName("Keyframe").getLength(), 2, capture[0]);
            Element map = (Element) doc.getElementsByTagName("TimeMap").item(Integer.parseInt(x.getAttribute("TimeMapIndex")));
            Assert.assertEquals(map.getAttribute("type"), capture[1], capture[0]);
            Assert.assertEquals(map.getAttribute("strength"), "50", capture[0]);
            int firstX = swf.getTimeline().getFrame(0).layers.get(1).matrix.translateX;
            for (int f = 0; f < swf.frameCount; f++) {
                long predicted = Math.round(value(doc, "Motion_X", f / (double) (swf.frameCount - 1)) * 20);
                long actual = swf.getTimeline().getFrame(f).layers.get(1).matrix.translateX - firstX;
                Assert.assertTrue(Math.abs(predicted - actual) <= 1, capture[0] + " frame " + f);
            }
        }
    }

    @Test
    public void sharesPolynomialMapsForOppositeDirectionChannels() throws Exception {
        com.jpexs.decompiler.flash.SWF swf;
        try (java.io.InputStream in = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(
                "testdata/motion_object/native_timemap_captures/dual_quartic_50/reference_native.swf"))) {
            swf = new com.jpexs.decompiler.flash.SWF(in, true);
        }
        StringBuilder frames = new StringBuilder();
        for (int f = 0; f < swf.frameCount; f++) {
            double source = swf.getTimeline().getFrame(f).layers.get(1).matrix.translateX / 20.0;
            frames.append(frame(f, 1, "tx=\"" + (320 - source) + "\" ty=\"" + (source - 60) + "\""));
        }
        Document doc = detect(frames.toString(), 30, Collections.emptySet());
        Element x = new FilterTweenDetectorTest().property(doc, "Motion_X");
        Element y = new FilterTweenDetectorTest().property(doc, "Motion_Y");
        Assert.assertEquals(x.getAttribute("ignoreTimeMap"), "0");
        Assert.assertEquals(y.getAttribute("ignoreTimeMap"), "0");
        Assert.assertEquals(x.getAttribute("TimeMapIndex"), y.getAttribute("TimeMapIndex"));
        Assert.assertEquals(doc.getElementsByTagName("TimeMap").getLength(), 2);
        Assert.assertEquals(((Element) doc.getElementsByTagName("TimeMap").item(1)).getAttribute("type"), "DualQuartic");
        for (int f = 0; f < swf.frameCount; f++) {
            int delta = swf.getTimeline().getFrame(f).layers.get(1).matrix.translateX - 3200;
            Assert.assertTrue(Math.abs(Math.round(value(doc, "Motion_X", f / 60.0) * 20) + delta) <= 1);
            Assert.assertTrue(Math.abs(Math.round(value(doc, "Motion_Y", f / 60.0) * 20) - delta) <= 1);
        }
    }

    @Test
    public void doesNotGuessUnmeasuredPolynomialStrength() throws Exception {
        StringBuilder frames = new StringBuilder();
        for (int f = 0; f <= 60; f++) {
            double t = f / 60.0;
            double p = t + 0.25 * (1 - Math.pow(1 - t, 4) - t);
            frames.append(frame(f, 1, "tx=\"" + Math.round((160 + 260 * p) * 20) / 20.0 + "\""));
        }
        Document doc = detect(frames.toString(), 30, Collections.emptySet());
        for (int m = 0; m < doc.getElementsByTagName("TimeMap").getLength(); m++) {
            String name = ((Element) doc.getElementsByTagName("TimeMap").item(m)).getAttribute("type");
            Assert.assertFalse(name.equals("Cubic") || name.equals("Quartic") || name.equals("Quintic")
                    || name.equals("DualCubic") || name.equals("DualQuartic") || name.equals("DualQuintic"));
        }
    }

    @Test
    public void preservesFrameRoundedBounceLengthWithCoalescedFinalTail() throws Exception {
        java.util.List<String> rows = java.nio.file.Files.readAllLines(java.nio.file.Paths.get(
                "testdata/motion_object/native_timemap_measurements/bounce_s6_validation/native_capture/samples_imported.csv"));
        StringBuilder frames = new StringBuilder();
        for (int f = 0; f < rows.size() - 1;) {
            String x = rows.get(f + 1).split(",")[2];
            int next = f + 1;
            while (next < rows.size() - 1 && rows.get(next + 1).split(",")[2].equals(x)) { next++; }
            frames.append(frame(f, next - f, "tx=\"" + Integer.parseInt(x) / 20.0 + "\""));
            f = next;
        }
        Document doc = detect(frames.toString(), 24, Collections.emptySet());
        Assert.assertEquals(doc.getElementsByTagName("DOMFrame").getLength(), 1);
        Assert.assertEquals(((Element) doc.getElementsByTagName("DOMFrame").item(0)).getAttribute("duration"), "361");
        Assert.assertEquals(((Element) doc.getElementsByTagName("AnimationCore").item(0)).getAttribute("duration"), "361000");
        for (int f = 0; f <= 360; f++) {
            long expected = Integer.parseInt(rows.get(f + 1).split(",")[2]) - 2000;
            Assert.assertTrue(Math.abs(Math.round(value(doc, "Motion_X", f / 360.0) * 20) - expected) <= 1);
        }
    }

    @Test
    public void keepsMapTypesDistinctWhenStrengthsAreEqual() throws Exception {
        StringBuilder frames = new StringBuilder();
        for (int f = 0; f <= 60; f++) {
            double t = f / 60.0;
            double simple = t + 0.5 * t * (1 - t);
            frames.append(frame(f, 1, "tx=\"" + Math.round((160 + 260 * simple) * 20) / 20.0
                    + "\" ty=\"" + Math.round((100 + 120 * dualProgress(t, 50)) * 20) / 20.0
                    + "\" a=\"" + Math.round((1 + 0.5 * simple) * 65536) / 65536.0 + "\""));
        }
        Document doc = detect(frames.toString(), 30, Collections.emptySet());
        NodeList maps = doc.getElementsByTagName("TimeMap");
        Assert.assertEquals(maps.getLength(), 3);
        Assert.assertEquals(((Element) maps.item(1)).getAttribute("type"), "Quadratic");
        Assert.assertEquals(((Element) maps.item(2)).getAttribute("type"), "DualQuadratic");
        Assert.assertEquals(((Element) maps.item(1)).getAttribute("strength"), "50");
        Assert.assertEquals(((Element) maps.item(2)).getAttribute("strength"), "50");
        for (int f = 0; f <= 60; f++) {
            double t = f / 60.0;
            Assert.assertEquals(value(doc, "Motion_Y", t), 120 * dualProgress(t, 50), 0.051);
        }
    }

}

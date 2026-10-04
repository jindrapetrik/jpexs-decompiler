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

public class FilterTweenDetectorTest {
    String frame(int f, int duration, String filters) {
        return "<DOMFrame index=\"" + f + "\" keyMode=\"9728\""
                + (duration == 1 ? "" : " duration=\"" + duration + "\"")
                + "><elements><DOMSymbolInstance libraryItemName=\"Symbol 1\"><matrix><Matrix tx=\"160\" ty=\"160\"/>"
                + "</matrix><filters>" + filters + "</filters></DOMSymbolInstance></elements></DOMFrame>";
    }

    Document detect(String frames) throws Exception {
        String result = ClassicTweenDetector.detect(frames, Collections.emptySet(), 30,
                Collections.singletonMap("Symbol 1", new double[]{0, 0}));
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader("<frames>" + result + "</frames>")));
    }

    Element property(Document doc, String id) {
        NodeList properties = doc.getElementsByTagName("Property");
        for (int i = 0; i < properties.getLength(); i++) {
            Element p = (Element) properties.item(i);
            if (id.equals(p.getAttribute("id"))) {
                return p;
            }
        }
        Assert.fail("Missing native filter property " + id);
        return null;
    }

    private double fixed(double value) {
        return Math.round(value * 65536) / 65536.0;
    }

    @Test
    public void detectsFilterOnlyCubicAndIndependentEasing() throws Exception {
        StringBuilder xml = new StringBuilder();
        double[] values = new double[31];
        for (int f = 0; f <= 30; f++) {
            double t = f / 30.0;
            values[f] = fixed(2 + 24 * t * t * t);
            double eased = t + 0.5 * (t < .5 ? t : 1 - t) * (1 - 2 * t);
            xml.append(frame(f, 1, "<BlurFilter blurX=\"" + values[f] + "\" blurY=\"" + fixed(4 + 12 * eased) + "\" quality=\"2\"/>"));
        }
        Document doc = detect(xml.toString());
        Assert.assertEquals(doc.getElementsByTagName("DOMFrame").getLength(), 1);
        Assert.assertEquals(((Element) doc.getElementsByTagName("DOMFrame").item(0)).getAttribute("tweenType"), "motion object");
        NodeList keys = property(doc, "Blur_BlurX").getElementsByTagName("Keyframe");
        Assert.assertEquals(keys.getLength(), 31);
        for (int f = 0; f < keys.getLength(); f++) {
            Element key = (Element) keys.item(f);
            Assert.assertEquals(key.getAttribute("timevalue"), Integer.toString(f * 1000));
            Assert.assertEquals(Double.parseDouble(key.getAttribute("anchor").split(",")[1]), values[f]);
        }
        Assert.assertEquals(property(doc, "Blur_Quality").getAttribute("value"), "2");
    }

    @Test
    public void preservesRGBAByteOrderAndPercentStrength() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 30; f++) {
            int red = 10 + f * 3;
            int alpha = 100 + f;
            double strength = Math.floor((100 + f * 3) * 256.0 / 100) / 256.0;
            xml.append(frame(f, 1, "<GlowFilter color=\"" + String.format("#%02x4080", red)
                    + "\" alpha=\"" + alpha / 255.0 + "\" strength=\"" + strength
                    + "\" blurX=\"4\" blurY=\"4\" inner=\"true\" knockout=\"true\"/>"));
        }
        Document doc = detect(xml.toString());
        NodeList colors = property(doc, "Glow_Color").getElementsByTagName("Keyframe");
        NodeList strengths = property(doc, "Glow_Strength").getElementsByTagName("Keyframe");
        Assert.assertEquals(colors.getLength(), 31);
        for (int f = 0; f <= 30; f++) {
            Assert.assertEquals(((Element) colors.item(f)).getAttribute("value"), String.format("0x%02x4080%02x", 10 + f * 3, 100 + f));
            Assert.assertEquals(Double.parseDouble(((Element) strengths.item(f)).getAttribute("anchor").split(",")[1]),
                    (double) (100 + f * 3));
        }
        Assert.assertEquals(property(doc, "Glow_InnerGlow").getAttribute("value"), "1");
        Assert.assertEquals(property(doc, "Glow_Knockout").getAttribute("value"), "1");
    }

    @Test
    public void preservesStrengthThatNativeMotionCannotRepresent() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 30; f++) {
            double strength = (147 + f * 256) / 256.0;
            xml.append(frame(f, 1, "<GlowFilter strength=\"" + strength + "\"/>"));
        }
        Document doc = detect(xml.toString());
        Assert.assertEquals(doc.getElementsByTagName("AnimationCore").getLength(), 0);
        NodeList frames = doc.getElementsByTagName("DOMFrame");
        Assert.assertEquals(frames.getLength(), 31);
        for (int f = 0; f <= 30; f++) {
            Element filter = (Element) ((Element) frames.item(f)).getElementsByTagName("GlowFilter").item(0);
            Assert.assertEquals(Double.parseDouble(filter.getAttribute("strength")), (147 + f * 256) / 256.0);
        }
        for (int fixed = 0; fixed < 65536; fixed++) {
            int percent = FilterTweenDetector.strengthPercent(fixed * 100.0 / 256);
            if (percent >= 0) {
                Assert.assertEquals((int) Math.floor(percent * 256.0 / 100), fixed);
            } else {
                int candidate = (int) Math.ceil(fixed * 100.0 / 256);
                Assert.assertTrue(Math.floor(candidate * 256.0 / 100) > fixed);
            }
        }
    }

    @Test
    public void preservesOrderedStackAndNativeBevelColorSpelling() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 30; f++) {
            xml.append(frame(f, 1, "<BlurFilter blurX=\"" + fixed(4 + 20 * Math.pow(f / 30.0, 3))
                    + "\"/><BevelFilter distance=\"" + f + "\" type=\"inner\" shadowColor=\"#112233\" highlightColor=\"#ddeeff\"/>"
                    + "<DropShadowFilter distance=\"" + f + "\" color=\"#223344\" hideObject=\"true\"/>"));
        }
        Document doc = detect(xml.toString());
        Assert.assertEquals(property(doc, "Bevel_HilightColor").getElementsByTagName("Keyframe").item(0).getAttributes().getNamedItem("value").getNodeValue(), "0xddeeffff");
        Assert.assertEquals(property(doc, "Bevel_Type").getAttribute("value"), "0");
        Assert.assertEquals(property(doc, "DropShadow_HideObject").getAttribute("value"), "1");
        NodeList containers = doc.getElementsByTagName("PropertyContainer");
        StringBuilder order = new StringBuilder();
        for (int i = 0; i < containers.getLength(); i++) {
            String id = ((Element) containers.item(i)).getAttribute("id");
            if (id.endsWith("_Filter")) {
                order.append(id).append(';');
            }
        }
        Assert.assertEquals(order.toString(), "Blur_Filter;Bevel_Filter;DropShadow_Filter;");
    }

    @Test
    public void doesNotTweenAcrossDiscreteFilterChanges() throws Exception {
        for (String setting : new String[]{"quality", "inner", "knockout"}) {
            StringBuilder xml = new StringBuilder();
            for (int f = 0; f < 20; f++) {
                String value = "quality".equals(setting) ? f < 10 ? "1" : "2" : f < 10 ? "false" : "true";
                xml.append(frame(f, 1, "<GlowFilter blurX=\"" + f + "\" " + setting + "=\"" + value + "\"/>"));
            }
            NodeList frames = detect(xml.toString()).getElementsByTagName("DOMFrame");
            for (int i = 0; i < frames.getLength(); i++) {
                Element frame = (Element) frames.item(i);
                int first = Integer.parseInt(frame.getAttribute("index"));
                int count = frame.hasAttribute("duration") ? Integer.parseInt(frame.getAttribute("duration")) : 1;
                Assert.assertTrue(first >= 10 || first + count <= 10, "Filter flag boundary removed");
            }
        }
    }

    @Test
    public void preservesUnknownFiltersDuplicatesAndIrregularChanges() throws Exception {
        for (int variant = 0; variant < 3; variant++) {
            StringBuilder xml = new StringBuilder();
            for (int f = 0; f < 12; f++) {
                String filter = variant == 0 ? "<GradientGlowFilter distance=\"" + f + "\"/>"
                        : variant == 1 ? "<BlurFilter blurX=\"" + f + "\"/><BlurFilter blurX=\"4\"/>"
                        : "<BlurFilter blurX=\"" + (f % 2 == 0 ? 1 : 20) + "\"/>";
                xml.append(frame(f, 1, filter));
            }
            Assert.assertEquals(detect(xml.toString()).getElementsByTagName("AnimationCore").getLength(), 0);
        }
    }

    @Test
    public void preservesQuantizedColorHoldsAndFinalHold() throws Exception {
        StringBuilder xml = new StringBuilder();
        for (int f = 0; f <= 30; f++) {
            xml.append(frame(f, f == 30 ? 6 : 1, "<GlowFilter color=\"" + String.format("#%02x4080", Math.round(f / 3.0)) + "\"/>"));
        }
        Document doc = detect(xml.toString());
        NodeList keys = property(doc, "Glow_Color").getElementsByTagName("Keyframe");
        Assert.assertEquals(keys.getLength(), 31);
        Assert.assertEquals(((Element) keys.item(1)).getAttribute("value"), ((Element) keys.item(0)).getAttribute("value"));
        Element hold = (Element) doc.getElementsByTagName("DOMFrame").item(1);
        Assert.assertEquals(hold.getAttribute("index"), "31");
        Assert.assertEquals(hold.getAttribute("duration"), "5");
    }
    @Test
    public void preservesPresetEasingInFilterOnlyAnimation() throws Exception {
        for (PresetEasing.Family family : PresetEasing.Family.values()) {
            for (int direction = 0; direction < 3; direction++) {
                StringBuilder xml = new StringBuilder();
                double[] expected = new double[61];
                for (int f = 0; f <= 60; f++) {
                    expected[f] = fixed(20 + 10 * PresetEasing.progress(family, direction, f / 60.0));
                    xml.append(frame(f, 1, "<BlurFilter blurX=\"" + expected[f] + "\" blurY=\"4\"/>"));
                }
                Document doc = detect(xml.toString());
                Assert.assertEquals(doc.getElementsByTagName("motionObjectXML").getLength(), 1,
                        family + " direction=" + direction);
                NodeList keys = property(doc, "Blur_BlurX").getElementsByTagName("Keyframe");
                Assert.assertEquals(keys.getLength(), 61);
                for (int f = 0; f <= 60; f++) {
                    Assert.assertEquals(Double.parseDouble(((Element) keys.item(f)).getAttribute("anchor").split(",")[1]),
                            expected[f]);
                }
            }
        }
    }

}

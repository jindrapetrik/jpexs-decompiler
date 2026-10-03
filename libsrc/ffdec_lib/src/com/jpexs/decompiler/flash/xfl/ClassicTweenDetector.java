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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Recovers classic tweens from sampled symbol instances. Only replaces samples
 * which can be reproduced by a single classic tween, allowing SWF quantization.
 */
final class ClassicTweenDetector {

    private static final Pattern FRAME = Pattern.compile("<DOMFrame\\b[^>]*>.*?</DOMFrame>", Pattern.DOTALL);
    private static final String[] COLOR_ATTRIBUTES = {"redMultiplier", "greenMultiplier",
        "blueMultiplier", "alphaMultiplier", "redOffset", "greenOffset", "blueOffset", "alphaOffset"};
    private static final double MATRIX_TOLERANCE = 2.0 / 65536;
    private static final double POSITION_TOLERANCE = 0.051;
    private static final double MULTIPLIER_TOLERANCE = 1.0 / 256;

    private ClassicTweenDetector() {
    }

    private static final class Sample {
        String xml;
        int index;
        int duration;
        Element identity;
        // Translation, two scales, two axis angles, then color multipliers/offsets.
        double[] values = new double[14];
        double[] matrix = new double[4];
    }

    static String detect(String frames) throws XMLStreamException {
        return detect(frames, Collections.emptySet());
    }

    static String detect(String frames, Set<Integer> instanceStarts) throws XMLStreamException {
        // Do not remove frames carrying clip actions, including script CDATA
        // which may itself contain XML-looking text.
        if (!frames.contains("<DOMSymbolInstance") || frames.contains("<Actionscript")) {
            return frames;
        }
        List<Sample> samples = new ArrayList<>();
        Matcher matcher = FRAME.matcher(frames);
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Element root = factory.newDocumentBuilder().parse(
                    new InputSource(new StringReader("<frames>" + frames + "</frames>"))).getDocumentElement();
            for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
                if (!(node instanceof Element)) {
                    continue;
                }
                if (!matcher.find()) {
                    throw new XMLStreamException("Missing DOMFrame");
                }
                samples.add(readSample((Element) node, matcher.group()));
            }
        } catch (XMLStreamException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new XMLStreamException("Cannot detect classic tweens", ex);
        }
        // Find compatible runs once, rather than comparing whole instance XML
        // again for every possible start of an unrecognized animation.
        int[] runEnds = new int[samples.size()];
        for (int i = 0; i < samples.size(); i++) {
            if (i > 0) {
                Sample previous = samples.get(i - 1);
                Sample next = samples.get(i);
                if (previous.identity != null && next.identity != null
                        && previous.identity.isEqualNode(next.identity)) {
                    for (int axis = 4; axis < 6; axis++) {
                        next.values[axis] += Math.rint((previous.values[axis] - next.values[axis])
                                / (2 * Math.PI)) * 2 * Math.PI;
                    }
                }
            }
        }
        for (int i = samples.size() - 1; i >= 0; i--) {
            runEnds[i] = i;
            if (i + 1 < samples.size()) {
                Sample current = samples.get(i);
                Sample next = samples.get(i + 1);
                if (current.duration == 1 && current.identity != null && next.identity != null
                        && next.index == current.index + 1 && !instanceStarts.contains(next.index)
                        && current.identity.isEqualNode(next.identity)) {
                    runEnds[i] = runEnds[i + 1];
                }
            }
        }
        boolean changed = false;
        StringBuilder result = new StringBuilder();
        for (int start = 0; start < samples.size();) {
            Sample first = samples.get(start);
            int limit = runEnds[start];
            Integer acceleration = null;
            int end = limit;
            // At least two intermediate samples are required to distinguish a
            // tween from an arbitrary change between adjacent keyframes.
            while (end >= start + 3) {
                acceleration = fit(samples, start, end);
                if (acceleration != null) {
                    break;
                }
                end--;
            }
            if (acceleration == null) {
                result.append(first.xml).append('\n');
                start++;
            } else {
                changed = true;
                int duration = samples.get(end).index - first.index;
                String xml = first.xml.replaceFirst("keyMode=\"[0-9]+\"",
                        "keyMode=\"" + XFLConverter.KEY_MODE_CLASSIC_TWEEN + "\"");
                xml = xml.replaceFirst("<DOMFrame", "<DOMFrame tweenType=\"motion\" motionTweenScale=\"true\""
                        + " acceleration=\"" + acceleration + "\" duration=\"" + duration + "\"");
                result.append(xml).append('\n');
                start = end; // Keep the actual endpoint as a keyframe.
            }
        }
        return changed ? result.toString() : frames;
    }

    private static Sample readSample(Element frame, String xml) {
        Sample sample = new Sample();
        sample.xml = xml;
        sample.index = Integer.parseInt(frame.getAttribute("index"));
        sample.duration = frame.hasAttribute("duration") ? Integer.parseInt(frame.getAttribute("duration")) : 1;
        if (!Integer.toString(XFLConverter.KEY_MODE_NORMAL).equals(frame.getAttribute("keyMode"))
                || frame.getAttributes().getLength() > (frame.hasAttribute("duration") ? 3 : 2)) {
            return sample;
        }
        Element elements = child(frame, "elements");
        Element instance = elements == null ? null : child(elements, "DOMSymbolInstance");
        if (instance == null || elementCount(elements) != 1) {
            return sample;
        }
        // Clip actions and metadata must remain attached to their original frame.
        if (child(instance, "Actionscript") != null || child(instance, "persistentData") != null) {
            return sample;
        }
        Element matrixContainer = child(instance, "matrix");
        Element matrix = matrixContainer == null ? null : child(matrixContainer, "Matrix");
        double a = number(matrix, "a", 1);
        double b = number(matrix, "b", 0);
        double c = number(matrix, "c", 0);
        double d = number(matrix, "d", 1);
        // Singular and reflected matrices require a different decomposition.
        if (a * d - b * c <= 0) {
            return sample;
        }
        sample.matrix = new double[]{a, b, c, d};
        sample.values[0] = number(matrix, "tx", 0);
        sample.values[1] = number(matrix, "ty", 0);
        sample.values[2] = Math.hypot(a, b);
        sample.values[3] = Math.hypot(c, d);
        sample.values[4] = Math.atan2(b, a);
        sample.values[5] = Math.atan2(-c, d);
        Element colorContainer = child(instance, "color");
        Element color = colorContainer == null ? null : child(colorContainer, "Color");
        for (int i = 0; i < COLOR_ATTRIBUTES.length; i++) {
            sample.values[6 + i] = number(color, COLOR_ATTRIBUTES[i], i < 4 ? 1 : 0);
        }
        Element identity = (Element) instance.cloneNode(true);
        removeChild(identity, "matrix");
        removeChild(identity, "color");
        // Exported sprite centers follow translation; they are not instance identity.
        identity.removeAttribute("centerPoint3DX");
        identity.removeAttribute("centerPoint3DY");
        removeWhitespace(identity);
        sample.identity = identity;
        return sample;
    }

    private static Integer fit(List<Sample> samples, int start, int end) {
        double[] first = samples.get(start).values;
        double[] last = samples.get(end).values;
        // Automatic rotation uses the shortest path.
        if (Math.abs(last[4] - first[4]) >= Math.PI || Math.abs(last[5] - first[5]) >= Math.PI) {
            return null;
        }
        double largestChange = 0;
        for (int i = 0; i < first.length; i++) {
            double tolerance = i < 2 ? POSITION_TOLERANCE
                    : i < 6 ? MATRIX_TOLERANCE : i < 10 ? MULTIPLIER_TOLERANCE : 1;
            double change = Math.abs(last[i] - first[i]) / tolerance;
            if (change > largestChange) {
                largestChange = change;
            }
        }
        if (largestChange < 4) {
            return null;
        }
        // Intersect the easing intervals allowed by the quantized samples.
        // Unlike repeatedly fitting least squares, this rejects inconsistent
        // spans as soon as two samples disagree.
        double minimumEase = -100;
        double maximumEase = 100;
        for (int f = start + 1; f < end; f++) {
            double t = (f - start) / (double) (end - start);
            double q = t * (1 - t) / 100;
            for (int i = 0; i < first.length; i++) {
                double tolerance = i < 2 ? POSITION_TOLERANCE
                        : i < 4 ? 2 * MATRIX_TOLERANCE
                        : i < 6 ? 2 * MATRIX_TOLERANCE / samples.get(f).values[i - 2]
                        : i < 10 ? MULTIPLIER_TOLERANCE : 1;
                double delta = last[i] - first[i];
                double difference = samples.get(f).values[i] - first[i] - delta * t;
                if (Math.abs(delta) < 1e-12) {
                    if (Math.abs(difference) > tolerance) {
                        return null;
                    }
                    continue;
                }
                double lower = (difference - tolerance) / (delta * q);
                double upper = (difference + tolerance) / (delta * q);
                minimumEase = Math.max(minimumEase, Math.min(lower, upper));
                maximumEase = Math.min(maximumEase, Math.max(lower, upper));
                if (minimumEase > maximumEase) {
                    return null;
                }
            }
        }
        int ease = (int) Math.round((minimumEase + maximumEase) / 2);
        if (ease < minimumEase - 1e-7 || ease > maximumEase + 1e-7) {
            return null;
        }
        for (int f = start + 1; f < end; f++) {
            double t = (f - start) / (double) (end - start);
            double progress = t + ease / 100.0 * t * (1 - t);
            double[] expected = new double[first.length];
            for (int i = 0; i < expected.length; i++) {
                expected[i] = first[i] + (last[i] - first[i]) * progress;
            }
            Sample sample = samples.get(f);
            for (int i = 0; i < expected.length; i++) {
                if (i >= 2 && i < 6) {
                    continue;
                }
                double tolerance = i < 2 ? POSITION_TOLERANCE : i < 10 ? MULTIPLIER_TOLERANCE : 1;
                if (Math.abs(sample.values[i] - expected[i]) > tolerance) {
                    return null;
                }
            }
            double[] matrix = {expected[2] * Math.cos(expected[4]), expected[2] * Math.sin(expected[4]),
                -expected[3] * Math.sin(expected[5]), expected[3] * Math.cos(expected[5])};
            for (int i = 0; i < matrix.length; i++) {
                if (Math.abs(sample.matrix[i] - matrix[i]) > MATRIX_TOLERANCE) {
                    return null;
                }
            }
        }
        return -ease; // XFL acceleration has the opposite sign to SWF progress easing.
    }

    private static double number(Element element, String name, double defaultValue) {
        return element != null && element.hasAttribute(name)
                ? Double.parseDouble(element.getAttribute(name)) : defaultValue;
    }

    private static Element child(Element element, String name) {
        for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element && name.equals(node.getNodeName())) {
                return (Element) node;
            }
        }
        return null;
    }

    private static int elementCount(Element element) {
        int count = 0;
        for (Node node = element.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element) {
                count++;
            }
        }
        return count;
    }

    private static void removeChild(Element element, String name) {
        Element child = child(element, name);
        if (child != null) {
            element.removeChild(child);
        }
    }

    private static void removeWhitespace(Node parent) {
        for (Node node = parent.getFirstChild(); node != null;) {
            Node next = node.getNextSibling();
            if (node.getNodeType() == Node.TEXT_NODE && node.getTextContent().trim().isEmpty()) {
                parent.removeChild(node);
            } else {
                removeWhitespace(node);
            }
            node = next;
        }
    }
}

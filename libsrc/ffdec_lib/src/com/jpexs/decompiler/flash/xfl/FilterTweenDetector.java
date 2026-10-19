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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.stream.XMLStreamException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** Native CS6 filter channels; topology and discrete settings remain identity. */
final class FilterTweenDetector {
    static final class Filter {
        String prefix;
        String[] numeric;
        String[] colors;
        double[] values;
        double[] tolerances;
        int offset;
        int gradientStops;
        List<String[]> settings = new ArrayList<>();
    }

    private FilterTweenDetector() {
    }

    static List<Filter> read(Element container) {
        if (container == null) {
            return Collections.emptyList();
        }
        List<Filter> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int offset = 14;
        for (Node node = container.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (!(node instanceof Element)) {
                continue;
            }
            Element element = (Element) node;
            Filter filter = new Filter();
            switch (element.getNodeName()) {
                case "BlurFilter":
                    filter.prefix = "Blur";
                    break;
                case "GlowFilter":
                    filter.prefix = "Glow";
                    break;
                case "DropShadowFilter":
                    filter.prefix = "DropShadow";
                    break;
                case "BevelFilter":
                    filter.prefix = "Bevel";
                    break;
                case "GradientGlowFilter":
                    filter.prefix = "GradientGlow";
                    break;
                case "GradientBevelFilter":
                    filter.prefix = "GradientBevel";
                    break;
                case "AdjustColorFilter":
                    filter.prefix = "AdjustColor";
                    break;
                default:
                    return null;
            }
            // Native containers are identified by type, not stack index. Do not
            // collapse duplicate types until their native addressing is known.
            if (!seen.add(filter.prefix) || "false".equals(element.getAttribute("isEnabled"))) {
                return null;
            }
            if ("AdjustColor".equals(filter.prefix)) {
                filter.numeric = new String[]{"brightness", "contrast", "saturation", "hue"};
                filter.colors = new String[0];
                filter.offset = offset;
                filter.values = new double[4];
                filter.tolerances = new double[]{1, 1, 1, 1};
                Set<String> allowed = new HashSet<>();
                Collections.addAll(allowed, filter.numeric);
                allowed.add("isEnabled");
                for (int a = 0; a < element.getAttributes().getLength(); a++) {
                    if (!allowed.contains(element.getAttributes().item(a).getNodeName())) {
                        return null;
                    }
                }
                for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child instanceof Element) {
                        return null;
                    }
                }
                for (int c = 0; c < 4; c++) {
                    double value = number(element, filter.numeric[c], 0);
                    int limit = c == 3 ? 180 : 100;
                    if (!Double.isFinite(value) || value != Math.rint(value) || Math.abs(value) > limit) {
                        return null;
                    }
                    filter.values[c] = value;
                }
                offset += 4;
                result.add(filter);
                continue;
            }
            boolean blur = "Blur".equals(filter.prefix);
            boolean gradient = filter.prefix.startsWith("Gradient");
            boolean directional = gradient || "DropShadow".equals(filter.prefix) || "Bevel".equals(filter.prefix);
            boolean bevel = "Bevel".equals(filter.prefix);
            if (bevel && element.hasAttribute("type") && !"inner".equals(element.getAttribute("type"))) {
                return null;
            }
            filter.numeric = blur ? new String[]{"blurX", "blurY"}
                    : directional ? new String[]{"blurX", "blurY", "strength", "angle", "distance"}
                    : new String[]{"blurX", "blurY", "strength"};
            filter.colors = blur || gradient ? new String[0] : bevel ? new String[]{"shadowColor", "highlightColor"}
                    : new String[]{"color"};
            Set<String> allowed = new HashSet<>();
            Collections.addAll(allowed, filter.numeric);
            Collections.addAll(allowed, filter.colors);
            Collections.addAll(allowed, "quality", "isEnabled", "inner", "knockout", "hideObject", "type");
            if (gradient) {
                allowed.remove("inner");
                allowed.remove("hideObject");
            }
            for (String color : filter.colors) {
                allowed.add(alphaAttribute(color));
            }
            for (int i = 0; i < element.getAttributes().getLength(); i++) {
                if (!allowed.contains(element.getAttributes().item(i).getNodeName())) {
                    return null;
                }
            }
            List<Element> entries = new ArrayList<>();
            for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child instanceof Element) {
                    Element entry = (Element) child;
                    if (!gradient || !"GradientEntry".equals(entry.getNodeName()) || entry.getElementsByTagName("*").getLength() != 0) {
                        return null;
                    }
                    for (int a = 0; a < entry.getAttributes().getLength(); a++) {
                        String name = entry.getAttributes().item(a).getNodeName();
                        if (!"color".equals(name) && !"alpha".equals(name) && !"ratio".equals(name)) {
                            return null;
                        }
                    }
                    entries.add(entry);
                }
            }
            if (gradient && (entries.isEmpty() || entries.size() > 255)) {
                return null;
            }
            filter.gradientStops = entries.size();
            filter.offset = offset;
            filter.values = new double[filter.numeric.length + 4 * filter.colors.length + 5 * filter.gradientStops];
            filter.tolerances = new double[filter.values.length];
            for (int i = 0; i < filter.numeric.length; i++) {
                String name = filter.numeric[i];
                double value = number(element, name, "strength".equals(name) ? 1
                        : "angle".equals(name) ? 45 : "distance".equals(name) ? 5 : 4);
                double tolerance = 1.0 / 65536;
                if ("strength".equals(name)) {
                    value *= 100;
                    tolerance = 1; // Native integer-percent quantization.
                } else if ("angle".equals(name)) {
                    tolerance = Math.toDegrees(1.0 / 65536);
                }
                if (!Double.isFinite(value) || !"angle".equals(name) && value < 0) {
                    return null;
                }
                filter.values[i] = value;
                filter.tolerances[i] = tolerance;
            }
            for (int i = 0; i < filter.colors.length; i++) {
                String name = filter.colors[i];
                String hex = element.hasAttribute(name) ? element.getAttribute(name) :
                        "highlightColor".equals(name) ? "#FFFFFF" : "Glow".equals(filter.prefix) ? "#FF0000" : "#000000";
                if (!hex.matches("#[0-9a-fA-F]{6}")) {
                    return null;
                }
                int rgb = Integer.parseInt(hex.substring(1), 16);
                double alpha = number(element, alphaAttribute(name), 1);
                if (!Double.isFinite(alpha) || alpha < 0 || alpha > 1) {
                    return null;
                }
                int colorOffset = filter.numeric.length + 4 * i;
                filter.values[colorOffset] = rgb >> 16;
                filter.values[colorOffset + 1] = rgb >> 8 & 255;
                filter.values[colorOffset + 2] = rgb & 255;
                filter.values[colorOffset + 3] = Math.round(alpha * 255);
                for (int c = 0; c < 4; c++) {
                    filter.tolerances[colorOffset + c] = 1;
                }
            }
            int previousRatio = -1;
            for (int stop = 0; stop < entries.size(); stop++) {
                Element entry = entries.get(stop);
                String hex = entry.getAttribute("color");
                double alpha = number(entry, "alpha", 1);
                double ratio = number(entry, "ratio", 0);
                if (!hex.matches("#[0-9a-fA-F]{6}") || !Double.isFinite(alpha) || alpha < 0 || alpha > 1
                        || !Double.isFinite(ratio) || ratio < 0 || ratio > 1) {
                    return null;
                }
                int index = (int) Math.round(ratio * 255);
                if (index < previousRatio) {
                    return null;
                }
                previousRatio = index;
                int rgb = Integer.parseInt(hex.substring(1), 16);
                int base = filter.numeric.length + 5 * stop;
                filter.values[base] = rgb >> 16;
                filter.values[base + 1] = rgb >> 8 & 255;
                filter.values[base + 2] = rgb & 255;
                filter.values[base + 3] = Math.round(alpha * 255);
                filter.values[base + 4] = index;
                for (int c = 0; c < 5; c++) {
                    filter.tolerances[base + c] = 1;
                }
            }
            int quality = (int) number(element, "quality", 1);
            if (quality < 1 || quality > 3 || quality != number(element, "quality", 1)) {
                return null;
            }
            setting(filter, "Quality", quality);
            if (!blur) {
                setting(filter, "Knockout", flag(element, "knockout"));
                if (gradient) {
                    String type = element.hasAttribute("type") ? element.getAttribute("type") : "inner";
                    int value = "inner".equals(type) ? 0 : "outer".equals(type) ? 1 : "full".equals(type) ? 2 : -1;
                    if (value < 0) {
                        return null;
                    }
                    setting(filter, "Type", value);
                } else if (bevel) {
                    setting(filter, "Type", 0); // Inner: captured from native CS6.
                } else {
                    setting(filter, "Glow".equals(filter.prefix) ? "InnerGlow" : "InnerShadow", flag(element, "inner"));
                    if (directional) {
                        setting(filter, "HideObject", flag(element, "hideObject"));
                    }
                }
            }
            offset += filter.values.length;
            result.add(filter);
        }
        return result;
    }

    static void normalizeIdentity(Element container, List<Filter> filters) {
        int index = 0;
        for (Node node = container.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (!(node instanceof Element)) {
                continue;
            }
            Element element = (Element) node;
            Filter filter = filters.get(index++);
            for (String name : filter.numeric) {
                element.removeAttribute(name);
            }
            for (String name : filter.colors) {
                element.removeAttribute(name);
                element.removeAttribute(alphaAttribute(name));
            }
            if (filter.gradientStops > 0) {
                for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
                    if (child instanceof Element) {
                        Element entry = (Element) child;
                        entry.removeAttribute("color");
                        entry.removeAttribute("alpha");
                        entry.removeAttribute("ratio");
                    }
                }
            }
        }
    }

    static boolean changing(List<ClassicTweenDetector.Sample> samples, int start, int end) {
        for (int f = start + 1; f <= end; f++) {
            for (int c = 14; c < samples.get(start).values.length; c++) {
                if (samples.get(f).values[c] != samples.get(start).values[c]) {
                    return true;
                }
            }
        }
        return false;
    }

    static double tolerance(ClassicTweenDetector.Sample sample, int component) {
        for (Filter filter : sample.filters) {
            if (component >= filter.offset && component < filter.offset + filter.values.length) {
                return filter.tolerances[component - filter.offset];
            }
        }
        throw new IllegalArgumentException("Missing filter component " + component);
    }

    static boolean nativeStrengths(List<ClassicTweenDetector.Sample> samples, int start, int end) {
        for (int f = start; f <= end; f++) {
            ClassicTweenDetector.Sample sample = samples.get(f);
            for (Filter filter : sample.filters) {
                for (int p = 0; p < filter.numeric.length; p++) {
                    if ("strength".equals(filter.numeric[p])
                            && strengthPercent(sample.values[filter.offset + p]) < 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // CS6 reads motion-object strength as whole percent and publishes fixed8
    // by truncation. Some SWF values have no corresponding integer percent.
    static int strengthPercent(double value) {
        long fixed = Math.round(value * 256 / 100);
        int percent = (int) Math.ceil(fixed * 100.0 / 256);
        return (long) Math.floor(percent * 256.0 / 100) == fixed ? percent : -1;
    }

    static void write(XFLXmlWriter writer, List<ClassicTweenDetector.Sample> samples, int start, int end)
            throws XMLStreamException {
        writer.writeStartElement("PropertyContainer", new String[]{"id", "Filters"});
        for (Filter filter : samples.get(start).filters) {
            writer.writeStartElement("PropertyContainer", new String[]{"id", filter.prefix + "_Filter"});
            for (int p = 0; p < filter.numeric.length; p++) {
                String attribute = filter.numeric[p];
                String name = Character.toUpperCase(attribute.charAt(0)) + attribute.substring(1);
                String propertyPrefix = "AdjustColor".equals(filter.prefix) ? "AdjColor" : filter.prefix;
                writeKeys(writer, propertyPrefix + "_" + name, samples, start, end, filter.offset + p, false);
            }
            for (String[] setting : filter.settings) {
                writer.writeEmptyElement("Property", new String[]{"id", filter.prefix + "_" + setting[0],
                    "enabled", "1", "readonly", "0", "visible", "1", "value", setting[1]});
            }
            for (int p = 0; p < filter.colors.length; p++) {
                String name = filter.colors[p];
                name = "highlightColor".equals(name) ? "HilightColor" :
                        Character.toUpperCase(name.charAt(0)) + name.substring(1);
                writeKeys(writer, filter.prefix + "_" + name, samples, start, end,
                        filter.offset + filter.numeric.length + 4 * p, true);
            }
            if (filter.gradientStops > 0) {
                writeGradient(writer, filter, samples, start, end);
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

    private static void writeGradient(XFLXmlWriter writer, Filter filter, List<ClassicTweenDetector.Sample> samples,
            int start, int end) throws XMLStreamException {
        int base = filter.offset + filter.numeric.length;
        boolean changing = false;
        for (int f = start + 1; f <= end; f++) {
            for (int c = 0; c < 5 * filter.gradientStops; c++) {
                changing |= samples.get(f).values[base + c] != samples.get(start).values[base + c];
            }
        }
        writer.writeStartElement("Property", new String[]{"id", filter.prefix + "_Gradient", "enabled", "1",
            "readonly", "0", "visible", "1", "ignoreTimeMap", "1"});
        for (int f = start; f <= (changing ? end : start); f++) {
            ClassicTweenDetector.Sample sample = samples.get(f);
            int last = f == end || !changing ? sample.index : sample.index + sample.duration - 1;
            StringBuilder colors = new StringBuilder();
            StringBuilder indexes = new StringBuilder();
            for (int stop = 0; stop < filter.gradientStops; stop++) {
                if (stop > 0) {
                    colors.append(',');
                    indexes.append(',');
                }
                long packed = 0;
                for (int c = 0; c < 4; c++) {
                    packed = packed << 8 | (long) sample.values[base + 5 * stop + c];
                }
                colors.append(String.format(java.util.Locale.ROOT, "0x%08x", packed));
                indexes.append((int) sample.values[base + 5 * stop + 4]);
            }
            for (int frame = sample.index; frame <= last; frame++) {
                writer.writeEmptyElement("Keyframe", new String[]{"timevalue", Long.toString((long) (frame - samples.get(start).index) * 1000),
                    "roving", "0", "colors", colors.toString(), "indexes", indexes.toString()});
            }
        }
        writer.writeEndElement();
    }

    private static void writeKeys(XFLXmlWriter writer, String id, List<ClassicTweenDetector.Sample> samples,
            int start, int end, int component, boolean color) throws XMLStreamException {
        writer.writeStartElement("Property", new String[]{"id", id, "enabled", "1", "readonly", "0",
            "visible", "1", "ignoreTimeMap", "1"});
        boolean changing = false;
        for (int f = start + 1; f <= end; f++) {
            for (int c = 0; c < (color ? 4 : 1); c++) {
                changing |= samples.get(f).values[component + c] != samples.get(start).values[component + c];
            }
        }
        for (int f = start; f <= (changing ? end : start); f++) {
            ClassicTweenDetector.Sample sample = samples.get(f);
            int last = f == end || !changing ? sample.index : sample.index + sample.duration - 1;
            for (int frame = sample.index; frame <= last; frame++) {
                String time = Long.toString((long) (frame - samples.get(start).index) * 1000);
                if (color) {
                    long packed = 0;
                    for (int c = 0; c < 4; c++) {
                        packed = packed << 8 | (long) sample.values[component + c];
                    }
                    writer.writeEmptyElement("Keyframe", new String[]{"timevalue", time, "roving", "0",
                        "value", String.format(java.util.Locale.ROOT, "0x%08x", packed)});
                } else {
                    double numeric = id.endsWith("_Strength") ? strengthPercent(sample.values[component]) : sample.values[component];
                    String value = "0," + java.math.BigDecimal.valueOf(numeric).stripTrailingZeros().toPlainString();
                    writer.writeEmptyElement("Keyframe", new String[]{"timevalue", time, "roving", "0",
                        "anchor", value, "previous", value, "next", value});
                }
            }
        }
        writer.writeEndElement();
    }

    private static String alphaAttribute(String color) {
        return "color".equals(color) ? "alpha" : color.replace("Color", "Alpha");
    }

    private static double number(Element element, String name, double fallback) {
        return element.hasAttribute(name) ? Double.parseDouble(element.getAttribute(name)) : fallback;
    }

    private static int flag(Element element, String name) {
        return "true".equals(element.getAttribute(name)) ? 1 : 0;
    }

    private static void setting(Filter filter, String name, int value) {
        filter.settings.add(new String[]{name, Integer.toString(value)});
    }
}

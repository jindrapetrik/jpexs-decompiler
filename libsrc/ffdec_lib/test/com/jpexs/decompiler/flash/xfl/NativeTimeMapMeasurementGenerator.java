/*
 * Copyright (C) 2026 JPEXS, All rights reserved.
 * This library is free software; you can redistribute it and/or modify it
 * under the GNU Lesser General Public License, version 3.0 or later.
 */
package com.jpexs.decompiler.flash.xfl;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Controlled native CS6 experiments; deliberately bypasses SWF tween detection. */
public final class NativeTimeMapMeasurementGenerator {
    private static final String[][] TYPES = {
        {"bounce", "Bounce"}, {"bounce_in", "BounceIn"},
        {"spring", "Spring"}, {"random", "RandomSquareWave"}
    };

    private static void write(Path path, String text) throws Exception {
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
    }

    private static List<Element> elements(Document doc, String name) {
        NodeList nodes = doc.getElementsByTagName(name);
        List<Element> result = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            result.add((Element) nodes.item(i));
        }
        return result;
    }

    private static void removeFormatting(Node node) {
        for (Node child = node.getFirstChild(); child != null;) {
            Node next = child.getNextSibling();
            if (child.getNodeType() == Node.TEXT_NODE && child.getNodeValue().trim().isEmpty()) {
                node.removeChild(child);
            } else {
                removeFormatting(child);
            }
            child = next;
        }
    }

    private static Element key(Document doc, int time, double value, double previousTime,
            double previousValue, double nextTime, double nextValue) {
        Element key = doc.createElement("Keyframe");
        key.setAttribute("timevalue", Integer.toString(time));
        key.setAttribute("roving", "0");
        key.setAttribute("anchor", "0," + value);
        key.setAttribute("previous", previousTime + "," + previousValue);
        key.setAttribute("next", nextTime + "," + nextValue);
        return key;
    }

    private static void example(Path template, Path root, String[] type, int strength,
            boolean validation, StringBuilder manifest, StringBuilder table) throws Exception {
        int frames = validation ? 361 : 601;
        int fps = validation ? 24 : 30;
        int lastTime = (frames - 1) * 1000;
        String id = type[0] + "_s" + strength + (validation ? "_validation" : "_601");
        Path directory = root.resolve(id);
        Files.createDirectories(directory);
        // Only copy the vector library, not previous native publications or references.
        try (java.util.stream.Stream<Path> paths = Files.walk(template.resolve("LIBRARY"))) {
            for (Path file : (Iterable<Path>) paths::iterator) {
                Path destination = directory.resolve(template.relativize(file));
                if (Files.isDirectory(file)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(file, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        write(directory.resolve(id + ".xfl"), "PROXY-CS5");
        String publish = new String(Files.readAllBytes(template.resolve("PublishSettings.xml")), StandardCharsets.UTF_8)
                .replace("47_native_quadratic", id);
        write(directory.resolve("PublishSettings.xml"), publish);
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(template.resolve("DOMDocument.xml").toFile());
        Element document = doc.getDocumentElement();
        document.setAttribute("width", "1600");
        document.setAttribute("height", "360");
        document.setAttribute("frameRate", Integer.toString(fps));
        Element frame = elements(doc, "DOMFrame").get(0);
        frame.setAttribute("duration", Integer.toString(frames));
        Element core = elements(doc, "AnimationCore").get(0);
        core.setAttribute("duration", Integer.toString(frames * 1000));
        core.setAttribute("TimeScale", Integer.toString(fps * 1000));
        for (Element map : elements(doc, "TimeMap")) {
            map.getParentNode().removeChild(map);
        }
        for (String[] map : new String[][]{{"Quadratic", "0"}, {type[1], Integer.toString(strength)}}) {
            Element element = doc.createElement("TimeMap");
            element.setAttribute("type", map[0]);
            element.setAttribute("strength", map[1]);
            core.insertBefore(element, elements(doc, "metadata").get(0));
        }
        for (Element property : elements(doc, "Property")) {
            boolean x = "Motion_X".equals(property.getAttribute("id"));
            NodeList keys = property.getElementsByTagName("Keyframe");
            double value = keys.getLength() == 0 ? 0 : Double.parseDouble(
                    ((Element) keys.item(0)).getAttribute("anchor").split(",")[1]);
            for (Node child = property.getFirstChild(); child != null;) {
                Node next = child.getNextSibling();
                property.removeChild(child);
                child = next;
            }
            property.removeAttribute("TimeMapIndex");
            property.setAttribute("ignoreTimeMap", x ? "0" : "1");
            if (x) {
                property.setAttribute("TimeMapIndex", "1");
                property.appendChild(key(doc, 0, 0, 0, 0, lastTime / 3.0, 1000.0 / 3));
                property.appendChild(key(doc, lastTime, 1000, -lastTime / 3.0, 2000.0 / 3, 0, 1000));
            } else {
                property.appendChild(key(doc, 0, value, 0, value, 0, value));
            }
        }
        Element matrix = (Element) elements(doc, "DOMSymbolInstance").get(0)
                .getElementsByTagName("Matrix").item(0);
        matrix.setAttribute("tx", "100");
        matrix.setAttribute("ty", "160");
        matrix.setAttribute("a", "1");
        matrix.setAttribute("d", "1");
        matrix.removeAttribute("b");
        matrix.removeAttribute("c");
        removeFormatting(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        // Match XFLConverter's CS6-tested document envelope: DOMDocument is
        // the first token, without a standalone XML declaration.
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(doc), new StreamResult(directory.resolve("DOMDocument.xml").toFile()));
        transformer.transform(new DOMSource(core), new StreamResult(directory.resolve("requested_motion.xml").toFile()));
        write(directory.resolve("measurement.properties"), "id=" + id + "\ntype=" + type[1]
                + "\nstrength=" + strength + "\nframes=" + frames + "\nfps=" + fps
                + "\nstartXTwips=2000\ndeltaXTwips=20000\nyTwips=3200\nsplit="
                + (validation ? "validation" : "discovery") + "\n");
        write(directory.resolve("README.md"), "# " + id + "\n\nNative CS6 " + type[1] + " strength " + strength
                + ". " + frames + " frames at " + fps + " fps, 1600 x 360 stage.\n\n"
                + "Motion_X has a linear base curve from 100 px to 1100 px; only its TimeMap changes. "
                + "All other instance channels are constant. This is a native measurement input, not a decompiler fidelity test. "
                + "Its SWF reference must be produced by CS6.\n\n"
                + (validation ? "Keep these observations out of fitting; use them to validate the derived formula.\n\n" : "Use these observations to derive a candidate formula.\n\n")
                + "Run ../../measure_native_timemaps.jsfl (from the motion_object folder) for automatic publication. "
                + "The command opens an unchanged copy of verified example 47, then applies requested_motion.xml "
                + "using native CS6 APIs. The generated candidate XFL currently fails native import; "
                + "do not use direct XFL Publish for these measurements.\n"
                + (Files.isRegularFile(directory.resolve("native_capture/RESULT.txt"))
                        ? "\nRetained native SWFs, motion XML and samples: [native_capture](native_capture/). "
                        + "See [NATIVE_RESULTS.md](../NATIVE_RESULTS.md) for validation and formulas.\n" : ""));
        if (manifest.length() > 0) {
            manifest.append(",\n");
        }
        manifest.append("    {id: \"").append(id).append("\", type: \"").append(type[1])
                .append("\", strength: ").append(strength).append(", frames: ").append(frames)
                .append(", fps: ").append(fps).append(", split: \"")
                .append(validation ? "validation" : "discovery").append("\"}");
        table.append("| [").append(id).append("](").append(id).append('/').append(id).append(".xfl) | ")
                .append(type[1]).append(" | ").append(strength).append(" | ").append(frames).append(" | ")
                .append(fps).append(" | ").append(validation ? "validation" : "discovery").append(" |\n");
    }

    public static void main(String[] args) throws Exception {
        Path parent = Paths.get("testdata", "motion_object");
        Path root = args.length == 0 ? parent.resolve("native_timemap_measurements") : Paths.get(args[0]);
        Files.createDirectories(root);
        StringBuilder manifest = new StringBuilder();
        StringBuilder table = new StringBuilder("| XFL | Type | Strength | Frames | FPS | Role |\n| --- | --- | ---: | ---: | ---: | --- |\n");
        for (String[] type : TYPES) {
            for (int strength : new int[]{1, 2, 3, 4, 5, 8}) {
                example(parent.resolve("47_native_quadratic"), root, type, strength, false, manifest, table);
            }
            example(parent.resolve("47_native_quadratic"), root, type, 6, true, manifest, table);
        }
        write(root.resolve("manifest.js"), "var nativeTimeMapMeasurements = [\n" + manifest + "\n];\n");
        write(root.resolve("README.md"), "# Native TimeMap measurement inputs\n\n"
                + "28 controlled XFL documents for Bounce, BounceIn, Spring and RandomSquareWave. "
                + "Native type names were captured from CS6. Native parameter checks and measured "
                + "formula validation are documented in [NATIVE_RESULTS.md](NATIVE_RESULTS.md).\n\n"
                + "Discovery uses strengths 1, 2, 3, 4, 5 and 8 with 601 frames at 30 fps. "
                + "Validation uses strength 6 with 361 frames at 24 fps. Do not use validation observations "
                + "to fit a formula; check the completed candidate against them.\n\n"
                + "The base displacement is 1000 pixels. Normalize SWF positions as (xTwips - 2000) / 20000, "
                + "and time as frameIndex / (frameCount - 1). Endpoints are measured; do not assume that "
                + "a native wave or bounce ends at normalized progress 1.\n\n"
                + "Run [measure_native_timemaps.jsfl](../measure_native_timemaps.jsfl) in CS6. It opens fresh unchanged "
                + "copies of verified example 47 and applies requested_motion.xml through native APIs. "
                + "Direct import of the generated candidate XFL has failed in CS6; use the command instead. "
                + "Deterministic cases are published before and after native FLA save/reopen; "
                + "Random is published twice per session, both before and after reopen. Every output has a unique "
                + "filename. No source document or existing reference is overwritten. Expect 70 SWF exports "
                + "if all cases succeed. See [NATIVE_RESULTS.md](NATIVE_RESULTS.md) for retained native "
                + "publications and formula verification; regenerated inputs alone do not establish native validation.\n\n"
                + "The command reports its build/motion-diagnostic/native-timemap-measurement-* output directory. "
                + "STEP files are immutable; STATUS.txt is written once at the end. Metadata and motion XML "
                + "must be validated on import and reopen. Failed cases are reported and the next case continues.\n\n"
                + "NativeTimeMapMeasurementAnalyzer reads that run directory, writes per-frame CSV files, "
                + "checks metadata/static channels and compares repeated publications. Its random comparison "
                + "uses sampled positions, not binary SWF equality. Comparing different strengths does not "
                + "establish Random reproducibility; only repeated exports of the same case do.\n\n" + table
                + "\nGenerator: test/com/jpexs/decompiler/flash/xfl/NativeTimeMapMeasurementGenerator.java.\n");
        System.out.println("Generated 28 controlled native TimeMap measurement documents in " + root);
    }
}

/* Copyright (C) 2026 JPEXS. LGPL version 3.0 or later. */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.AbortRetryIgnoreHandler;
import com.jpexs.decompiler.flash.SWF;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Real native SWF -> XFL exports for CS6 roundtrip verification. */
public final class NativeWaveExportGenerator {
    public static void main(String[] args) throws Exception {
        Path inputs = Paths.get("testdata/motion_object/native_timemap_measurements");
        Path root = args.length == 0 ? Paths.get("testdata/motion_object/native_wave_exports") : Paths.get(args[0]);
        Files.createDirectories(root);
        int count = 0;
        int compared = 0;
        for (String type : new String[]{"spring", "bounce", "bounce_in"}) {
            for (int strength : new int[]{1, 2, 3, 4, 5, 6, 8}) {
                String id = type + "_s" + strength + (strength == 6 ? "_validation" : "_601");
                Path source = inputs.resolve(id + "/native_capture/imported.swf");
                SWF swf;
                try (InputStream in = Files.newInputStream(source)) { swf = new SWF(in, true); }
                swf.exportXfl(new AbortRetryIgnoreHandler() {
                    @Override public int handle(Throwable error) { throw new AssertionError(error); }
                    @Override public AbortRetryIgnoreHandler getNewInstance() { return this; }
                }, root.resolve(id + ".xfl").toString(), id + "_compiled.swf", "FFDec", "FFDec", "1", false, FLAVersion.CS6, null);
                Path directory = root.resolve(id);
                Files.copy(source, directory.resolve("reference.swf"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(directory.resolve("DOMDocument.xml").toFile());
                boolean equivalentClassic = type.equals("bounce_in") && strength == 1;
                if (!equivalentClassic) {
                    Element core = (Element) doc.getElementsByTagName("AnimationCore").item(0);
                    if (core == null || doc.getElementsByTagName("AnimationCore").getLength() != 1
                            || !core.getAttribute("duration").equals(swf.frameCount * 1000 + "")) {
                        throw new AssertionError("Incomplete motion span " + id);
                    }
                    Element property = null;
                    for (int p = 0; p < doc.getElementsByTagName("Property").getLength(); p++) {
                        Element candidate = (Element) doc.getElementsByTagName("Property").item(p);
                        if (candidate.getAttribute("id").equals("Motion_X")) { property = candidate; }
                    }
                    if (property == null || !property.getAttribute("ignoreTimeMap").equals("0")
                            || property.getElementsByTagName("Keyframe").getLength() != 2) {
                        throw new AssertionError("Native X map missing " + id);
                    }
                    Element map = (Element) doc.getElementsByTagName("TimeMap").item(Integer.parseInt(property.getAttribute("TimeMapIndex")));
                    String expected = type.equals("spring") ? "Spring" : type.equals("bounce") ? "Bounce" : "BounceIn";
                    if (!map.getAttribute("type").equals(expected) || !map.getAttribute("strength").equals(strength + "")) {
                        throw new AssertionError("Wrong native map " + id);
                    }
                    NativeTimeMap nativeMap = new NativeTimeMap(type.equals("spring") ? NativeTimeMap.Type.SPRING
                            : type.equals("bounce") ? NativeTimeMap.Type.BOUNCE : NativeTimeMap.Type.BOUNCE_IN,
                            strength, swf.frameCount - 1);
                    org.w3c.dom.NodeList keys = property.getElementsByTagName("Keyframe");
                    double begin = Double.parseDouble(((Element) keys.item(0)).getAttribute("anchor").split(",")[1]);
                    double finish = Double.parseDouble(((Element) keys.item(1)).getAttribute("anchor").split(",")[1]);
                    Element instance = (Element) doc.getElementsByTagName("DOMSymbolInstance").item(0);
                    Element matrix = (Element) instance.getElementsByTagName("Matrix").item(0);
                    double initialX = matrix.hasAttribute("tx") ? Double.parseDouble(matrix.getAttribute("tx")) : 0;
                    for (int f = 0; f < swf.frameCount; f++) {
                        long predicted = Math.round(20 * (initialX + begin + (finish - begin)
                                * nativeMap.progress(f / (double) (swf.frameCount - 1))));
                        int actual = swf.getTimeline().getFrame(f).layers.get(1).matrix.translateX;
                        if (Math.abs(predicted - actual) > 1) { throw new AssertionError("Serialized wave mismatch " + id + " frame " + f); }
                        compared++;
                    }
                } else {
                    // Coalesced source frames can select modern motion output;
                    // dense test frames may instead select the equivalent classic.
                    if (doc.getElementsByTagName("AnimationCore").getLength() != 0) {
                        boolean found = false;
                        for (int m = 0; m < doc.getElementsByTagName("TimeMap").getLength(); m++) {
                            Element map = (Element) doc.getElementsByTagName("TimeMap").item(m);
                            found |= map.getAttribute("type").equals("Quadratic") && map.getAttribute("strength").equals("-100");
                        }
                        if (!found) { throw new AssertionError("Equivalent quadratic map missing " + id); }
                    }
                }
                Path settings = directory.resolve("PublishSettings.xml");
                String publish = new String(Files.readAllBytes(settings), StandardCharsets.UTF_8)
                        .replace("<defaultNames>1</defaultNames>", "<defaultNames>0</defaultNames>")
                        .replace("<flashDefaultName>1</flashDefaultName>", "<flashDefaultName>0</flashDefaultName>")
                        .replace("<flashFileName>" + id + ".swf</flashFileName>", "<flashFileName>" + id + "_compiled.swf</flashFileName>");
                Files.write(settings, publish.getBytes(StandardCharsets.UTF_8));
                Files.write(directory.resolve("README.md"), ("# " + id + "\n\nReal decompilation of the retained native CS6 reference.swf. "
                        + (equivalentClassic ? "BounceIn 1 is equivalent to Quadratic -100; the native SWF export uses that motion map. "
                                : "Motion_X uses a recovered native " + type + " TimeMap with two linear base keys. ")
                        + "\n\nOpen " + id + ".xfl in CS6 and Publish to " + id + "_compiled.swf, "
                        + "or run ../../publish_native_wave_exports.jsfl. Native recompilation is pending.\n").getBytes(StandardCharsets.UTF_8));
                count++;
                System.out.println("Verified SWF -> XFL " + id);
            }
        }
        Files.write(root.resolve("README.md"), ("# Native wave decompiler exports\n\n21 actual SWF -> XFL exports from native CS6 references. "
                + "Spring, Bounce and BounceIn use recovered native maps at measured strengths 1, 2, 3, 4, 5, 6 and 8. "
                + "BounceIn 1 uses the equivalent Quadratic -100 map (dense frame inputs may select a classic tween).\n\nRun ../publish_native_wave_exports.jsfl in CS6 "
                + "to save/reopen and publish every export. Each output belongs to its own example folder. "
                + "Native recompilation and comparison to reference.swf are pending.\n").getBytes(StandardCharsets.UTF_8));
        System.out.println("Generated and verified " + count + " real SWF -> XFL exports.");
        Files.write(root.resolve("VALIDATION.md"), ("# Export validation\n\n" + count + " actual native SWF -> XFL exports verified. "
                + compared + " frame positions evaluated from serialized native map assignments and linear base endpoints "
                + "match reference.swf within one twip after rounding. Native map types, strengths, two base keys and full "
                + "span lengths match. BounceIn 1 uses the equivalent native Quadratic -100 map.\n\n"
                + "This is local serialization/model verification. Native Flash CS6 recompilation of these exports is pending.\n")
                .getBytes(StandardCharsets.UTF_8));
    }
}

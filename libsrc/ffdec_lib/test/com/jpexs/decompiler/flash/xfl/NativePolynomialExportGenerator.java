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

/** Actual captured polynomial SWFs decompiled for native CS6 validation. */
public final class NativePolynomialExportGenerator {
    public static void main(String[] args) throws Exception {
        Path captures = Paths.get("testdata/motion_object/native_timemap_captures");
        Path root = args.length == 0 ? Paths.get("testdata/motion_object/native_polynomial_exports") : Paths.get(args[0]);
        Files.createDirectories(root);
        int compared = 0;
        for (NativeTimeMap.Type type : new NativeTimeMap.Type[]{NativeTimeMap.Type.CUBIC,
            NativeTimeMap.Type.QUARTIC, NativeTimeMap.Type.QUINTIC, NativeTimeMap.Type.DUAL_CUBIC,
            NativeTimeMap.Type.DUAL_QUARTIC, NativeTimeMap.Type.DUAL_QUINTIC}) {
            String id = type.name().toLowerCase(java.util.Locale.ROOT) + "_50";
            Path reference = captures.resolve(id + "/reference_native.swf");
            SWF swf;
            try (InputStream in = Files.newInputStream(reference)) { swf = new SWF(in, true); }
            swf.exportXfl(new AbortRetryIgnoreHandler() {
                @Override public int handle(Throwable error) { throw new AssertionError(error); }
                @Override public AbortRetryIgnoreHandler getNewInstance() { return this; }
            }, root.resolve(id + ".xfl").toString(), id + "_compiled.swf", "FFDec", "FFDec", "1", false, FLAVersion.CS6, null);
            Path directory = root.resolve(id);
            Files.copy(reference, directory.resolve("reference.swf"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(directory.resolve("DOMDocument.xml").toFile());
            if (doc.getElementsByTagName("AnimationCore").getLength() != 1
                    || !((Element) doc.getElementsByTagName("AnimationCore").item(0)).getAttribute("duration").equals("61000")) {
                throw new AssertionError("Native full span missing " + id);
            }
            Element x = null;
            for (int p = 0; p < doc.getElementsByTagName("Property").getLength(); p++) {
                Element property = (Element) doc.getElementsByTagName("Property").item(p);
                if (property.getAttribute("id").equals("Motion_X")) { x = property; }
            }
            if (x == null || !x.getAttribute("ignoreTimeMap").equals("0")
                    || x.getElementsByTagName("Keyframe").getLength() != 2) { throw new AssertionError("Native base keys missing " + id); }
            Element map = (Element) doc.getElementsByTagName("TimeMap").item(Integer.parseInt(x.getAttribute("TimeMapIndex")));
            if (!map.getAttribute("type").equals(type.xmlName) || !map.getAttribute("strength").equals("50")) {
                throw new AssertionError("Native assignment incorrect " + id);
            }
            double first = Double.parseDouble(((Element) x.getElementsByTagName("Keyframe").item(0)).getAttribute("anchor").split(",")[1]);
            double last = Double.parseDouble(((Element) x.getElementsByTagName("Keyframe").item(1)).getAttribute("anchor").split(",")[1]);
            Element instance = (Element) doc.getElementsByTagName("DOMSymbolInstance").item(0);
            double initial = Double.parseDouble(((Element) instance.getElementsByTagName("Matrix").item(0)).getAttribute("tx"));
            NativeTimeMap curve = new NativeTimeMap(type, 50);
            for (int f = 0; f < swf.frameCount; f++) {
                long predicted = Math.round(20 * (initial + first + (last - first) * curve.progress(f / 60.0)));
                if (Math.abs(predicted - swf.getTimeline().getFrame(f).layers.get(1).matrix.translateX) > 1) {
                    throw new AssertionError("Serialized curve differs " + id + " frame " + f);
                }
                compared++;
            }
            Path settings = directory.resolve("PublishSettings.xml");
            String publish = new String(Files.readAllBytes(settings), StandardCharsets.UTF_8)
                    .replace("<defaultNames>1</defaultNames>", "<defaultNames>0</defaultNames>")
                    .replace("<flashDefaultName>1</flashDefaultName>", "<flashDefaultName>0</flashDefaultName>")
                    .replace("<flashFileName>" + id + ".swf</flashFileName>", "<flashFileName>" + id + "_compiled.swf</flashFileName>");
            Files.write(settings, publish.getBytes(StandardCharsets.UTF_8));
            Files.write(directory.resolve("README.md"), ("# " + id + "\n\nReal captured SWF -> XFL export. Motion_X uses native "
                    + type.xmlName + " strength 50 with two linear base keys. Motion_Y retains Quadratic 65; X scale retains Quadratic -40. "
                    + "Reference: reference.swf, 61 frames, 30 fps, 640 x 360 stage, Demo instance.\n\n"
                    + "Open " + id + ".xfl and Publish to " + id + "_compiled.swf, or run ../../publish_native_polynomial_exports.jsfl. "
                    + "All source X positions match the serialized model within one twip after rounding. Native CS6 recompilation is pending.\n")
                    .getBytes(StandardCharsets.UTF_8));
            System.out.println("Verified native SWF -> XFL " + id);
        }
        Files.write(root.resolve("README.md"), ("# Native polynomial exports\n\nSix actual SWF -> XFL exports for Cubic, Quartic, Quintic, "
                + "DualCubic, DualQuartic and DualQuintic at the measured strength 50. " + compared + " serialized X positions match "
                + "the captured references within one twip after rounding. Other strengths are not enabled as native polynomial maps.\n\n"
                + "Run [publish_native_polynomial_exports.jsfl](../publish_native_polynomial_exports.jsfl) in CS6. It checks map assignments "
                + "and two base keys, saves/reopens native FLA files and publishes each <id>_compiled.swf in its example folder. "
                + "Original references remain untouched. Native recompilation and full frame comparison are pending.\n")
                .getBytes(StandardCharsets.UTF_8));
        System.out.println("Verified 6 exports and " + compared + " native positions.");
    }
}

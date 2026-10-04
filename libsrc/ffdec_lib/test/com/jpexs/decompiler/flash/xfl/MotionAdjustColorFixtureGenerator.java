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
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.filters.FILTER;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Adjust Color references using the existing ColorMatrixConvertor. */
public final class MotionAdjustColorFixtureGenerator {
    private static final String[] IDS = {"31_adjust_brightness", "32_adjust_contrast", "33_adjust_saturation", "34_adjust_hue", "35_adjust_combined", "36_adjust_stack"};

    private MotionAdjustColorFixtureGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path root = args.length > 0 ? Paths.get(args[0]) : Paths.get("testdata", "motion_object");
        Files.createDirectories(root);
        for (int variant = 0; variant < IDS.length; variant++) {
            String id = IDS[variant];
            Path directory = root.resolve(id);
            Files.createDirectories(directory);
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            adjustMovie(variant).saveTo(bytes);
            Files.write(directory.resolve("reference.swf"), bytes.toByteArray());
            SWF swf;
            try (InputStream input = Files.newInputStream(directory.resolve("reference.swf"))) {
                swf = new SWF(input, true);
            }
            swf.exportXfl(new AbortRetryIgnoreHandler() {
                @Override
                public int handle(Throwable thrown) {
                    throw new AssertionError(thrown);
                }
                @Override
                public AbortRetryIgnoreHandler getNewInstance() {
                    return this;
                }
            }, root.resolve(id + ".xfl").toString(), id + "_compiled.swf", "FFDec", "FFDec", "1", false, FLAVersion.CS6, null);
            Path settings = directory.resolve("PublishSettings.xml");
            String publish = new String(Files.readAllBytes(settings), StandardCharsets.UTF_8)
                    .replace("<defaultNames>1</defaultNames>", "<defaultNames>0</defaultNames>")
                    .replace("<flashDefaultName>1</flashDefaultName>", "<flashDefaultName>0</flashDefaultName>")
                    .replace("<flashFileName>" + id + ".swf</flashFileName>", "<flashFileName>" + id + "_compiled.swf</flashFileName>");
            Files.write(settings, publish.getBytes(StandardCharsets.UTF_8));
            StringBuilder expected = new StringBuilder("frameIndex,filterIndex,type,property,value\n");
            for (int frame = 0; frame < swf.frameCount; frame++) {
                DepthState state = swf.getTimeline().getFrame(frame).layers.get(1);
                if (state == null || state.filters == null) {
                    throw new AssertionError("Missing filter instance at frame " + frame);
                }
                for (int index = 0; index < state.filters.size(); index++) {
                    FILTER filter = state.filters.get(index);
                    Field[] fields = filter.getClass().getFields();
                    Arrays.sort(fields, Comparator.comparing(Field::getName));
                    for (Field field : fields) {
                        if (Modifier.isStatic(field.getModifiers()) || "reserved".equals(field.getName())) {
                            continue;
                        }
                        Object value = field.get(filter);
                        if (value instanceof RGBA) {
                            RGBA rgba = (RGBA) value;
                            int[] channels = {rgba.red, rgba.green, rgba.blue, rgba.alpha};
                            for (int c = 0; c < 4; c++) {
                                row(expected, frame, index, filter, field.getName() + "." + "rgba".charAt(c), channels[c]);
                            }
                        } else if (value instanceof RGBA[]) {
                            RGBA[] colors = (RGBA[]) value;
                            for (int stop = 0; stop < colors.length; stop++) {
                                int[] channels = {colors[stop].red, colors[stop].green, colors[stop].blue, colors[stop].alpha};
                                for (int c = 0; c < 4; c++) {
                                    row(expected, frame, index, filter, field.getName() + "[" + stop + "]." + "rgba".charAt(c), channels[c]);
                                }
                            }
                        } else if (value instanceof float[]) {
                            float[] matrix = (float[]) value;
                            for (int c = 0; c < matrix.length; c++) {
                                row(expected, frame, index, filter, field.getName() + "[" + c + "]", matrix[c]);
                            }
                        } else if (value instanceof int[]) {
                            int[] ratios = (int[]) value;
                            for (int stop = 0; stop < ratios.length; stop++) {
                                row(expected, frame, index, filter, field.getName() + "[" + stop + "]", ratios[stop]);
                            }
                        } else if (value instanceof Number || value instanceof Boolean) {
                            row(expected, frame, index, filter, field.getName(), value);
                        }
                    }
                }
            }
            Files.write(directory.resolve("expected_filters.csv"), expected.toString().getBytes(StandardCharsets.UTF_8));
            String document = new String(Files.readAllBytes(directory.resolve("DOMDocument.xml")), StandardCharsets.UTF_8);
            java.util.regex.Matcher sourceFilters = java.util.regex.Pattern.compile("<filters>[\\s\\S]*?</filters>").matcher(document);
            if (!sourceFilters.find()) { throw new AssertionError("Missing filter pose"); }
            for (int pose = 0; pose < 3; pose++) {
                for (FILTER filter : swf.getTimeline().getFrame(pose * 30).layers.get(1).filters) {
                    if (!(filter instanceof com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER)) { continue; }
                    com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor params = new com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor(
                            ((com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER) filter).matrix);
                    String adjust = "<AdjustColorFilter brightness=\"" + params.getBrightness() + "\" contrast=\"" + params.getContrast()
                            + "\" saturation=\"" + params.getSaturation() + "\" hue=\"" + params.getHue() + "\"/>";
                    String filters = sourceFilters.group().replaceFirst("<AdjustColorFilter\\b[^>]*/>", adjust);
                    Files.write(directory.resolve("capture_pose_" + pose + ".xml"), filters.getBytes(StandardCharsets.UTF_8));
                }
            }
            String[] descriptions = {
                "Adjust Color: independently animated brightness.",
                "Adjust Color: independently animated contrast.",
                "Adjust Color: independently animated saturation.",
                "Adjust Color: independently animated hue.",
                "Adjust Color: all four parameters with independent cubic and DualQuadratic curves.",
                "Ordered Blur, Adjust Color and Glow stack with animated Adjust Color parameters."
            };
            StringBuilder parameters = new StringBuilder("frameIndex,brightness,contrast,saturation,hue\n");
            for (int f = 0; f < swf.frameCount; f++) {
                for (FILTER filter : swf.getTimeline().getFrame(f).layers.get(1).filters) {
                    if (filter instanceof com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER) {
                        com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor converted = new com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor(
                                ((com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER) filter).matrix);
                        parameters.append(f).append(',').append(converted.getBrightness()).append(',').append(converted.getContrast())
                                .append(',').append(converted.getSaturation()).append(',').append(converted.getHue()).append('\n');
                    }
                }
            }
            Files.write(directory.resolve("expected_parameters.csv"), parameters.toString().getBytes(StandardCharsets.UTF_8));
            NodeList frames = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(directory.resolve("DOMDocument.xml").toFile()).getElementsByTagName("DOMFrame");
            int motions = 0;
            for (int i = 0; i < frames.getLength(); i++) {
                if ("motion object".equals(((Element) frames.item(i)).getAttribute("tweenType"))) {
                    motions++;
                }
            }
            String readme = "# " + id + "\n\n" + descriptions[variant] + "\n\n"
                    + "Open \u0060" + id + ".xfl\u0060 in Flash CS6. Publish uses the unique \u0060" + id + "_compiled.swf\u0060 filename.\n\n"
                    + "The synthetic reference has 61 frames, 30 fps and a 640 x 360 stage. \u0060expected_filters.csv\u0060 records all 20 color-matrix elements and other filter fields.\n\n"
                    + "The decompiled XFL contains " + motions + " motion-object spans; Adjust Color scalar channels use the existing matrix conversion. Native channel spelling is confirmed by captured CS6 XML; see ../ADJUST_COLOR.md for native comparison results. "
                    + "Run ../native_adjust_color_filters.jsfl to capture native Adjust Color property encoding automatically. See ../ADJUST_COLOR.md.\n";
            Files.write(directory.resolve("README.md"), readme.getBytes(StandardCharsets.UTF_8));
            System.out.println(id + " motion spans=" + motions);
        }
    }

    static SWF adjustMovie(int variant) throws Exception {
        SWF swf = MotionTweenFixtureGenerator.movie(0);
        java.util.List<com.jpexs.decompiler.flash.tags.Tag> tags = new java.util.ArrayList<>();
        for (com.jpexs.decompiler.flash.tags.Tag tag : swf.getTags()) { tags.add(tag); }
        int frame = 0;
        for (com.jpexs.decompiler.flash.tags.Tag tag : tags) {
            if (tag instanceof com.jpexs.decompiler.flash.tags.ShowFrameTag) { frame++; }
            if (!(tag instanceof com.jpexs.decompiler.flash.tags.PlaceObject2Tag)) { continue; }
            com.jpexs.decompiler.flash.tags.PlaceObject2Tag place = (com.jpexs.decompiler.flash.tags.PlaceObject2Tag) tag;
            double t = frame / 60.0, q = t + .5 * (t < .5 ? t : 1 - t) * (1 - 2 * t);
            com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor parameters = new com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor();
            if (variant == 0 || variant >= 4) parameters.setBrightness((int) Math.round(-40 + 80 * t * t * t));
            if (variant == 1 || variant >= 4) parameters.setContrast((int) Math.round(-30 + 60 * t * t));
            if (variant == 2 || variant >= 4) parameters.setSaturation((int) Math.round(-40 + 80 * q));
            if (variant == 3 || variant >= 4) parameters.setHue((int) Math.round(-90 + 180 * t));
            com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER adjust = new com.jpexs.decompiler.flash.types.filters.COLORMATRIXFILTER();
            adjust.matrix = parameters.getMatrix();
            // Compare the existing inverse with the authored parameters. The
            // matrix remains the reference even if an equivalent or ambiguous
            // parameter representation is recovered.
            com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor inverse = new com.jpexs.decompiler.flash.types.filters.ColorMatrixConvertor(adjust.matrix);
            if (inverse.getBrightness() != parameters.getBrightness() || inverse.getContrast() != parameters.getContrast()
                    || inverse.getSaturation() != parameters.getSaturation() || inverse.getHue() != parameters.getHue()) {
                System.out.println("inverse difference variant=" + variant + " frame=" + frame + " authored=" + parameters + " recovered=" + inverse);
            }
            java.util.List<FILTER> filters = Collections.<FILTER>singletonList(adjust);
            if (variant == 5) {
                com.jpexs.decompiler.flash.types.filters.BLURFILTER blur = new com.jpexs.decompiler.flash.types.filters.BLURFILTER();
                blur.blurX = 4; blur.blurY = 6; blur.passes = 2;
                com.jpexs.decompiler.flash.types.filters.GLOWFILTER glow = new com.jpexs.decompiler.flash.types.filters.GLOWFILTER();
                glow.glowColor = new RGBA(64, 128, 224, 160); glow.strength = 1; glow.passes = 2;
                filters = Arrays.asList(blur, adjust, glow);
            }
            swf.replaceTag(tag, new com.jpexs.decompiler.flash.tags.PlaceObject3Tag(swf, frame != 0, 1, null,
                    place.getCharacterId(), place.getMatrix(), place.colorTransform, -1, place.getInstanceName(), -1,
                    filters, 0, 1, -1, null, null, false));
        }
        return swf;
    }

    private static double fixed(double value) { return Math.round(value * 65536) / 65536.0; }

    private static void row(StringBuilder csv, int frame, int index, FILTER filter, String property, Object value) {
        csv.append(frame).append(',').append(index).append(',').append(filter.getClass().getSimpleName())
                .append(',').append(property).append(',').append(value).append('\n');
    }
}

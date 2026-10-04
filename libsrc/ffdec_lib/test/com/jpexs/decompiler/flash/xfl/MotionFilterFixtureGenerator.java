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
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Exports captured native filter SWFs as reproducible decompiler fixtures. */
public final class MotionFilterFixtureGenerator {
    private static final String[] IDS = {"18_blur", "19_glow", "20_drop_shadow", "21_bevel", "22_filter_stack", "23_filter_parameters", "24_filter_integer_strength"};

    private MotionFilterFixtureGenerator() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Native capture directory required");
        }
        Path nativeRoot = Paths.get(args[0]);
        Path root = args.length > 1 ? Paths.get(args[1]) : Paths.get("testdata", "motion_object");
        Files.createDirectories(root);
        for (String id : IDS) {
            Path captured = nativeRoot.resolve(id);
            Path directory = root.resolve(id);
            Files.createDirectories(directory);
            boolean synthetic = id.startsWith("23_") || id.startsWith("24_");
            boolean fallback = id.startsWith("23_");
            byte[] reference;
            if (synthetic) {
                java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
                parameterMovie(!fallback).saveTo(bytes);
                reference = bytes.toByteArray();
            } else {
                reference = Files.readAllBytes(captured.resolve("reference_native.swf"));
            }
            Files.write(directory.resolve("reference.swf"), reference);
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
                        } else if (value instanceof Number || value instanceof Boolean) {
                            row(expected, frame, index, filter, field.getName(), value);
                        }
                    }
                }
            }
            Files.write(directory.resolve("expected_filters.csv"), expected.toString().getBytes(StandardCharsets.UTF_8));
            if (!synthetic) {
                Files.write(directory.resolve("native_motion.xml"), Files.readAllBytes(captured.resolve("persisted_motion.xml")));
            }
            NodeList frames = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(directory.resolve("DOMDocument.xml").toFile()).getElementsByTagName("DOMFrame");
            int motions = 0;
            for (int i = 0; i < frames.getLength(); i++) {
                if ("motion object".equals(((Element) frames.item(i)).getAttribute("tweenType"))) {
                    motions++;
                }
            }
            if ((!fallback && motions == 0) || (fallback && frames.getLength() < 2)) {
                throw new AssertionError("No motion object detected for native filter case " + id);
            }
            String readme = "# " + id + "\n\n"
                    + "Open `" + id + ".xfl` in Flash CS6 and publish to `" + id + "_compiled.swf` (already configured).\n\n"
                    + (synthetic ? "`reference.swf` is a synthetic four-filter reference covering numeric parameters, RGB, alpha and strength at SWF precision. "
                            : "`reference.swf` is the native CS6 authoring capture. `native_motion.xml` preserves its native property encoding. ")
                    + "The reference has 30 fps, a 640 x 360 stage and 61 frames. "
                    + "The decompiled XFL contains " + motions + " motion-object spans. "
                    + (fallback ? "CS6 motion-object strength uses integer percent. Nonrepresentable fixed8 samples retain ordinary filter keyframes; representable subspans may use motion objects.\n\n"
                            : "Filter channels use sampled property keys after independent curve fitting.\n\n")
                    + "`expected_filters.csv` records the actual SWF filter values by frame, stack index, type and property. "
                    + "Do not infer animated properties from the original command's requested poses: some CS6 setters retained their initial values. "
                    + "See ../FILTERS.md for the current native verification results.\n";
            Files.write(directory.resolve("README.md"), readme.getBytes(StandardCharsets.UTF_8));
            System.out.println(id + " motion spans=" + motions);
        }
    }

    static SWF parameterMovie(boolean integerStrength) throws Exception {
        SWF swf = MotionTweenFixtureGenerator.movie(0);
        java.util.List<com.jpexs.decompiler.flash.tags.Tag> tags = new java.util.ArrayList<>();
        for (com.jpexs.decompiler.flash.tags.Tag tag : swf.getTags()) {
            tags.add(tag);
        }
        int frame = 0;
        for (com.jpexs.decompiler.flash.tags.Tag tag : tags) {
            if (tag instanceof com.jpexs.decompiler.flash.tags.ShowFrameTag) {
                frame++;
            }
            if (!(tag instanceof com.jpexs.decompiler.flash.tags.PlaceObject2Tag)) {
                continue;
            }
            com.jpexs.decompiler.flash.tags.PlaceObject2Tag place = (com.jpexs.decompiler.flash.tags.PlaceObject2Tag) tag;
            double t = frame / 60.0;
            double q = t + .5 * (t < .5 ? t : 1 - t) * (1 - 2 * t);
            double blurX = Math.round((2 + 18 * t * t * t) * 65536) / 65536.0;
            double blurY = Math.round((4 + 12 * q) * 65536) / 65536.0;
            float strength = (float) (Math.round((.5 + q) * 256) / 256.0);
            if (integerStrength) {
                int percent = (int) Math.round(50 + q * 100);
                strength = (float) (Math.floor(percent * 256.0 / 100) / 256.0);
            }
            double distance = Math.round((3 + 20 * t * t) * 65536) / 65536.0;
            double angle = Math.round(Math.toRadians(45 + 90 * t) * 65536) / 65536.0;
            RGBA color = new RGBA(32 + (int) Math.round(160 * t), 64 + (int) Math.round(80 * q),
                    192 - (int) Math.round(96 * t), 128 + (int) Math.round(100 * t));
            com.jpexs.decompiler.flash.types.filters.BLURFILTER blur = new com.jpexs.decompiler.flash.types.filters.BLURFILTER();
            blur.blurX = blurX; blur.blurY = blurY; blur.passes = 2;
            com.jpexs.decompiler.flash.types.filters.GLOWFILTER glow = new com.jpexs.decompiler.flash.types.filters.GLOWFILTER();
            glow.blurX = blurX; glow.blurY = blurY; glow.strength = strength; glow.glowColor = color; glow.passes = 2;
            com.jpexs.decompiler.flash.types.filters.DROPSHADOWFILTER shadow = new com.jpexs.decompiler.flash.types.filters.DROPSHADOWFILTER();
            shadow.blurX = blurX; shadow.blurY = blurY; shadow.strength = strength; shadow.dropShadowColor = color;
            shadow.distance = distance; shadow.angle = angle; shadow.passes = 2;
            com.jpexs.decompiler.flash.types.filters.BEVELFILTER bevel = new com.jpexs.decompiler.flash.types.filters.BEVELFILTER();
            bevel.blurX = blurX; bevel.blurY = blurY; bevel.strength = strength; bevel.shadowColor = color;
            bevel.highlightColor = new RGBA(255 - color.red, 255 - color.green, 255 - color.blue, color.alpha);
            bevel.distance = distance; bevel.angle = angle; bevel.passes = 2;
            java.util.List<FILTER> filters = Arrays.asList(blur, glow, shadow, bevel);
            com.jpexs.decompiler.flash.tags.PlaceObject3Tag replacement = new com.jpexs.decompiler.flash.tags.PlaceObject3Tag(
                    swf, frame != 0, 1, null, place.getCharacterId(), place.getMatrix(), place.colorTransform,
                    -1, place.getInstanceName(), -1, filters, 0, 1, -1, null, null, false);
            swf.replaceTag(tag, replacement);
        }
        return swf;
    }

    private static void row(StringBuilder csv, int frame, int index, FILTER filter, String property, Object value) {
        csv.append(frame).append(',').append(index).append(',').append(filter.getClass().getSimpleName())
                .append(',').append(property).append(',').append(value).append('\n');
    }
}

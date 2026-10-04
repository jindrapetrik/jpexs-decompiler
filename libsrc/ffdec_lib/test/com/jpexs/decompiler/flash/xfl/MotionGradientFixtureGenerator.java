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

/** Gradient references and XFL fixtures for native motion-schema capture. */
public final class MotionGradientFixtureGenerator {
    private static final String[] IDS = {"25_gradient_glow", "26_gradient_bevel", "27_gradient_colors", "28_gradient_ratios", "29_gradient_topology", "30_gradient_stack"};

    private MotionGradientFixtureGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path root = args.length > 0 ? Paths.get(args[0]) : Paths.get("testdata", "motion_object");
        Files.createDirectories(root);
        for (int variant = 0; variant < IDS.length; variant++) {
            String id = IDS[variant];
            Path directory = root.resolve(id);
            Files.createDirectories(directory);
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            gradientMovie(variant).saveTo(bytes);
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
            String[] descriptions = {
                "Inner Gradient Glow: blur X/Y, strength, angle and distance.",
                "Inner Gradient Bevel: blur X/Y, strength, angle and distance.",
                "Gradient Glow: three independently changing RGB/alpha stops.",
                "Gradient Bevel: changing middle-stop position with fixed end stops.",
                "Gradient Glow: three stops become four at frame index 30; preserve that boundary.",
                "Ordered outer Gradient Glow and full Gradient Bevel with numeric animation."
            };
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
                    + "The synthetic reference has 61 frames, 30 fps and a 640 x 360 stage. \u0060expected_filters.csv\u0060 records every gradient stop and filter field.\n\n"
                    + "The decompiled XFL contains " + motions + " motion-object spans using native gradient channels. "
                    + "Run ../native_gradient_roundtrip.jsfl to publish diagnostic copies automatically for native verification. See ../GRADIENT_FILTERS.md.\n";
            Files.write(directory.resolve("README.md"), readme.getBytes(StandardCharsets.UTF_8));
            System.out.println(id + " motion spans=" + motions);
        }
    }

    static SWF gradientMovie(int variant) throws Exception {
        SWF swf = MotionTweenFixtureGenerator.movie(0);
        java.util.List<com.jpexs.decompiler.flash.tags.Tag> tags = new java.util.ArrayList<>();
        for (com.jpexs.decompiler.flash.tags.Tag tag : swf.getTags()) { tags.add(tag); }
        int frame = 0;
        for (com.jpexs.decompiler.flash.tags.Tag tag : tags) {
            if (tag instanceof com.jpexs.decompiler.flash.tags.ShowFrameTag) { frame++; }
            if (!(tag instanceof com.jpexs.decompiler.flash.tags.PlaceObject2Tag)) { continue; }
            com.jpexs.decompiler.flash.tags.PlaceObject2Tag place = (com.jpexs.decompiler.flash.tags.PlaceObject2Tag) tag;
            double t = frame / 60.0;
            double q = t + .5 * (t < .5 ? t : 1 - t) * (1 - 2 * t);
            boolean numeric = variant < 2 || variant == 5;
            double blurX = fixed(numeric ? 2 + 18 * t * t * t : 8);
            double blurY = fixed(numeric ? 4 + 12 * q : 8);
            int percent = numeric ? (int) Math.round(50 + 100 * q) : 100;
            float strength = (float) (Math.floor(percent * 256.0 / 100) / 256.0);
            double angle = fixed(Math.toRadians(numeric ? 45 + 90 * t : 45));
            double distance = fixed(numeric ? 3 + 20 * t * t : 8);
            RGBA[] colors = {new RGBA(32, 64, 192, 0), new RGBA(128, 192, 32, 160), new RGBA(224, 64, 32, 255)};
            int[] ratios = {0, variant == 3 ? 64 + (int) Math.round(128 * q) : 128, 255};
            if (variant == 2) {
                colors = new RGBA[]{new RGBA(32 + (int) Math.round(128 * t), 64, 192, (int) Math.round(96 * t)),
                    new RGBA(128, 192 - (int) Math.round(96 * q), 32 + (int) Math.round(128 * t), 160 + (int) Math.round(80 * q)),
                    new RGBA(224 - (int) Math.round(96 * q), 64 + (int) Math.round(128 * t), 32, 255 - (int) Math.round(64 * t))};
            }
            if (variant == 4 && frame >= 30) {
                colors = new RGBA[]{colors[0], colors[1], new RGBA(64, 192, 224, 220), colors[2]};
                ratios = new int[]{0, 96, 192, 255};
            }
            com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER glow = new com.jpexs.decompiler.flash.types.filters.GRADIENTGLOWFILTER();
            glow.gradientColors = colors; glow.gradientRatio = ratios; glow.blurX = blurX; glow.blurY = blurY;
            glow.strength = strength; glow.angle = angle; glow.distance = distance; glow.passes = 2; glow.innerShadow = variant != 5;
            com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER bevel = new com.jpexs.decompiler.flash.types.filters.GRADIENTBEVELFILTER();
            bevel.gradientColors = colors; bevel.gradientRatio = ratios; bevel.blurX = blurX; bevel.blurY = blurY;
            bevel.strength = strength; bevel.angle = angle; bevel.distance = distance; bevel.passes = 2;
            bevel.innerShadow = variant != 5; bevel.onTop = variant == 5;
            java.util.List<FILTER> filters = variant == 5 ? Arrays.asList(glow, bevel)
                    : Collections.<FILTER>singletonList(variant == 1 || variant == 3 ? bevel : glow);
            swf.replaceTag(tag, new com.jpexs.decompiler.flash.tags.PlaceObject3Tag(
                    swf, frame != 0, 1, null, place.getCharacterId(), place.getMatrix(), place.colorTransform,
                    -1, place.getInstanceName(), -1, filters, 0, 1, -1, null, null, false));
        }
        return swf;
    }

    private static double fixed(double value) { return Math.round(value * 65536) / 65536.0; }

    private static void row(StringBuilder csv, int frame, int index, FILTER filter, String property, Object value) {
        csv.append(frame).append(',').append(index).append(',').append(filter.getClass().getSimpleName())
                .append(',').append(property).append(',').append(value).append('\n');
    }
}

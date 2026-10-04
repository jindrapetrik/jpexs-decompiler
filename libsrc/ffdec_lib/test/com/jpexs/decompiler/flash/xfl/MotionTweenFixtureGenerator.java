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
import com.jpexs.decompiler.flash.tags.DefineShape3Tag;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.SetBackgroundColorTag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.FILLSTYLE;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;
import com.jpexs.decompiler.flash.types.RGB;
import com.jpexs.decompiler.flash.types.RGBA;
import com.jpexs.decompiler.flash.types.shaperecords.EndShapeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StraightEdgeRecord;
import com.jpexs.decompiler.flash.types.shaperecords.StyleChangeRecord;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Generates visible SWF -> XFL examples for manual Flash CS6 roundtrip tests. */
public final class MotionTweenFixtureGenerator {
    private static final String[][] CASES = {
        {"01_position", "Independent cubic curves for X/Y displacement."},
        {"02_scale", "Independent X/Y scale curves."},
        {"03_rotation", "Initial rotation of 30 degrees followed by a 540-degree turn."},
        {"04_static_skew", "Constant skew preserved during motion."},
        {"05_animated_skew", "Animated skew combined with changing scale."},
        {"06_reflection", "Reflected motion with a negative Y scale."},
        {"07_rgba_multipliers", "Independent RGB and alpha multipliers, including values above 100%."},
        {"08_rgba_offsets", "Independent positive and negative RGBA offsets."},
        {"09_alpha_dual_quadratic", "Alpha fade from 99% to 0%, DualQuadratic 50, with exact integer endpoints."},
        {"10_motion_dual_quadratic", "Different DualQuadratic easing strengths for X, Y and scale."},
        {"11_color_dual_quadratic", "Different DualQuadratic easing strengths for colors, alpha and an offset."},
        {"12_multiple_segments", "Multiple cubic segments with independent interior keys for X, Y and the red channel."},
        {"13_quantized_holds", "An RGB curve containing held frames caused by SWF quantization."},
        {"14_final_hold", "The final tween state remains held for ten additional frames."},
        {"15_instance_restart", "The old instance is removed before reinsertion at frame index 30; the new pose and tween boundary must be preserved."},
        {"16_rotation_skew_dual_quadratic", "Rotation and skew with opposing DualQuadratic easing strengths."},
        {"17_three_segments", "Three cubic motion segments with a reversal followed by acceleration."},
        {"37_ease_quad", "quad easing: X uses In, Y uses Out and scale uses InOut."},
        {"38_ease_cubic", "cubic easing: X uses In, Y uses Out and scale uses InOut."},
        {"39_ease_quart", "quart easing: X uses In, Y uses Out and scale uses InOut."},
        {"40_ease_quint", "quint easing: X uses In, Y uses Out and scale uses InOut."},
        {"41_ease_sine", "sine easing: X uses In, Y uses Out and scale uses InOut."},
        {"42_ease_circ", "circ easing: X uses In, Y uses Out and scale uses InOut."},
        {"43_ease_expo", "expo easing: X uses In, Y uses Out and scale uses InOut."},
        {"44_ease_back", "back easing: X uses In, Y uses Out and scale uses InOut."},
        {"45_ease_bounce", "bounce easing: X uses In, Y uses Out and scale uses InOut."},
        {"46_ease_elastic", "elastic easing: X uses In, Y uses Out and scale uses InOut."},
        {"47_native_quadratic", "Native Quadratic maps: X and scale share strength -40, Y uses strength 65."},
        {"48_native_mixed_maps", "Native Quadratic and DualQuadratic both use strength 50, with separate map indices."}
    };

    private MotionTweenFixtureGenerator() {
    }

    private static double dual(double t, int strength) {
        return t + strength / 100.0 * (t < 0.5 ? t : 1 - t) * (1 - 2 * t);
    }

    private static double[] values(int example, double t) {
        // X/Y pixels, scales, rotation/skew degrees, RGBA multipliers and offsets.
        double[] v = {160, 160, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0};
        if (example >= 27) {
            double simple = t + (example == 27 ? -0.4 : 0.5) * t * (1 - t);
            v[0] = 160 + 260 * simple;
            v[1] = 100 + 120 * (example == 27 ? t + 0.65 * t * (1 - t) : dual(t, 50));
            v[2] = 1 + 0.5 * simple;
            return v;
        }
        if (example >= 17) {
            PresetEasing.Family family = PresetEasing.Family.values()[example - 17];
            v[0] = 160 + 260 * PresetEasing.progress(family, 0, t);
            v[1] = 100 + 120 * PresetEasing.progress(family, 1, t);
            v[2] = 1 + 0.5 * PresetEasing.progress(family, 2, t);
            return v;
        }
        switch (example) {
            case 0:
            case 3:
            case 13:
            case 14:
                v[0] = 80 + 300 * t * t * t;
                v[1] = 100 + 100 * t * t;
                if (example == 3) {
                    v[4] = 25;
                    v[5] = -15;
                }
                break;
            case 1:
                v[2] = 1 + t * t * t;
                v[3] = 1 + 0.5 * t * t;
                break;
            case 2:
                v[0] = 320;
                v[4] = 30 + 540 * t;
                break;
            case 4:
                v[4] = 25;
                v[5] = -15 + 35 * t * t * t;
                v[2] = 1 + 0.25 * t * t;
                break;
            case 5:
                v[2] = 1 + 0.5 * t * t * t;
                v[3] = -1 - 0.5 * t * t;
                break;
            case 6:
                v[6] = 0.25 + t * t;
                v[7] = 1.5 - t * t * t;
                v[8] = 0.5 + 0.5 * t;
                v[9] = 0.5 + 0.5 * t;
                break;
            case 7:
                v[9] = 0.5;
                v[10] = -80 + 160 * t * t * t;
                v[11] = 20 + 80 * t * t;
                v[12] = 80 - 160 * t;
                v[13] = 40 * t * t;
                break;
            case 8:
                int percent = (int) Math.floor(99 * (1 - dual(t, 50)) + 1e-8);
                v[9] = Math.round(percent * 256 / 100.0) / 256.0;
                break;
            case 9:
                v[0] = 80 + 300 * dual(t, 50);
                v[1] = 100 + 100 * dual(t, -75);
                v[2] = 1 + 0.5 * dual(t, 40);
                break;
            case 10:
                v[6] = 0.25 + dual(t, -60);
                v[7] = 0.5 + 0.5 * dual(t, 50);
                v[9] = 0.5 + 0.5 * dual(t, 80);
                v[10] = -80 + 160 * dual(t, 70);
                break;
            case 11:
                double x = t <= 0.4 ? t / 0.4 : (t - 0.4) / 0.6;
                double y = t <= 0.6 ? t / 0.6 : (t - 0.6) / 0.4;
                double red = t <= 0.5 ? 2 * t : 2 * (t - 0.5);
                v[0] = 80 + (t <= 0.4 ? 100 * x * x * x : 100 + 120 * x * x);
                v[1] = 100 + (t <= 0.6 ? 80 * y * y : 80 - 60 * y * y * y);
                v[6] = t <= 0.5 ? 0.25 + 0.75 * red * red : 1 - 0.5 * red * red * red;
                break;
            case 12:
                v[6] = 0.25 + t * t * t;
                break;
            case 15:
                v[0] = 320;
                v[2] = 2;
                v[3] = 3;
                v[4] = 20 + 120 * dual(t, 50);
                v[5] = -10 + 25 * dual(t, -50);
                break;
            case 16:
                double u = t < 1.0 / 3 ? t * 3 : t < 2.0 / 3 ? t * 3 - 1 : t * 3 - 2;
                v[0] = 80 + (t < 1.0 / 3 ? 60 * u * u * u
                        : t < 2.0 / 3 ? 60 - 40 * u * u : 20 + 120 * u * u * u);
                v[1] = 100 + 60 * t * t;
                break;
            default:
                throw new IllegalArgumentException("Unknown example " + example);
        }
        return v;
    }

    private static DefineSpriteTag arrow(SWF swf) {
        DefineShape3Tag shape = new DefineShape3Tag(swf);
        shape.shapeBounds = new RECT(0, 1600, 0, 800);
        FILLSTYLE fill = new FILLSTYLE();
        fill.fillStyleType = FILLSTYLE.SOLID;
        fill.color = new RGBA(130, 100, 60, 255);
        shape.shapes.fillStyles.fillStyles = new FILLSTYLE[]{fill};
        shape.shapes.numFillBits = 1;
        shape.shapes.shapeRecords = new ArrayList<>();
        StyleChangeRecord style = new StyleChangeRecord();
        style.stateMoveTo = true;
        style.stateFillStyle1 = true;
        style.fillStyle1 = 1;
        style.calculateBits();
        shape.shapes.shapeRecords.add(style);
        int[][] points = {{0, 0}, {1200, 0}, {1600, 400}, {1200, 800}, {0, 800}, {300, 400}, {0, 0}};
        for (int i = 1; i < points.length; i++) {
            StraightEdgeRecord edge = new StraightEdgeRecord();
            edge.generalLineFlag = true;
            edge.deltaX = points[i][0] - points[i - 1][0];
            edge.deltaY = points[i][1] - points[i - 1][1];
            edge.simplify();
            edge.calculateBits();
            shape.shapes.shapeRecords.add(edge);
        }
        shape.shapes.shapeRecords.add(new EndShapeRecord());
        swf.addTag(shape);
        DefineSpriteTag sprite = new DefineSpriteTag(swf);
        sprite.frameCount = 1;
        sprite.addTag(new PlaceObject2Tag(swf, false, 1, shape.shapeId, new MATRIX(), null, -1, null, -1, null));
        sprite.addTag(new ShowFrameTag(swf));
        swf.addTag(sprite);
        return sprite;
    }

    static SWF movie(int example) {
        SWF swf = new SWF();
        swf.version = 10;
        swf.frameRate = 30;
        swf.frameCount = example == 13 ? 71 : 61;
        swf.displayRect = new RECT(0, 12800, 0, 7200);
        swf.addTag(new SetBackgroundColorTag(swf, new RGB(240, 240, 240)));
        DefineSpriteTag sprite = arrow(swf);
        for (int f = 0; f < swf.frameCount; f++) {
            double[] v = values(example, Math.min(f, 60) / 60.0);
            double angle = Math.toRadians(v[4]);
            double skew = Math.toRadians(v[5]);
            MATRIX matrix = new MATRIX();
            matrix.hasScale = true;
            matrix.hasRotate = true;
            matrix.scaleX = (float) (Math.round(v[2] * Math.cos(angle) * 65536) / 65536.0);
            matrix.rotateSkew0 = (float) (Math.round(v[2] * Math.sin(angle) * 65536) / 65536.0);
            matrix.rotateSkew1 = (float) (Math.round(-v[3] * Math.sin(angle + skew) * 65536) / 65536.0);
            matrix.scaleY = (float) (Math.round(v[3] * Math.cos(angle + skew) * 65536) / 65536.0);
            matrix.translateX = (int) Math.round(v[0] * 20);
            matrix.translateY = (int) Math.round(v[1] * 20);
            CXFORMWITHALPHA color = new CXFORMWITHALPHA();
            color.hasMultTerms = true;
            color.hasAddTerms = true;
            color.redMultTerm = (int) Math.round(v[6] * 256);
            color.greenMultTerm = (int) Math.round(v[7] * 256);
            color.blueMultTerm = (int) Math.round(v[8] * 256);
            color.alphaMultTerm = (int) Math.round(v[9] * 256);
            color.redAddTerm = (int) Math.round(v[10]);
            color.greenAddTerm = (int) Math.round(v[11]);
            color.blueAddTerm = (int) Math.round(v[12]);
            color.alphaAddTerm = (int) Math.round(v[13]);
            boolean restart = f == 0 || example == 14 && f == 30;
            if (restart && f != 0) {
                com.jpexs.decompiler.flash.tags.RemoveObject2Tag remove =
                        new com.jpexs.decompiler.flash.tags.RemoveObject2Tag(swf);
                remove.depth = 1;
                swf.addTag(remove);
            }
            swf.addTag(new PlaceObject2Tag(swf, !restart, 1, restart ? sprite.spriteId : -1,
                    matrix, color, -1, restart ? "Demo" : null, -1, null));
            swf.addTag(new ShowFrameTag(swf));
        }
        return swf;
    }

    public static void main(String[] args) throws Exception {
        Path root = args.length == 0 ? Paths.get("testdata", "motion_object") : Paths.get(args[0]);
        Files.createDirectories(root);
        StringBuilder overview = new StringBuilder("# Motion object examples for Flash CS6\n\n"
                + "Open a `.xfl` file from one of the folders below in Flash CS6. Each folder contains a complete XFL document, "
                + "a visible vector arrow and `reference.swf` with the sampled animation before decompilation.\n\n"
                + "SWF publishing uses a unique `<example>_compiled.swf` filename for each example. Keep the reference file intact, "
                + "retain 30 fps and the 640 x 360 stage, and publish without modifying the animation. "
                + "Compare the `Demo` instance frame by frame against `reference.swf`; binary equality of the files is not expected.\n\n"
                + "`expected.csv` contains the original root-timeline transform and color values for each frame. "
                + "Frame indices start at zero; matrix coefficients use fixed16, color multipliers use fixed8, "
                + "and translations use twips (20 twips per pixel). `preview.png` shows reference frame 31.\n\n"
                + "These documents are generated by the current XFLConverter targeting CS6. "
                + "Compilation and roundtrip fidelity in Adobe must be checked manually. Unsupported spans retain sampled frames or classic tweens. "
                + "Color effects are represented with Advanced Color components; separate Tint/Brightness mode recovery "
                + "is not implemented. Animated filters, 3D and orientToPath recovery are not implemented either.\n\n");
        for (int example = 0; example < CASES.length; example++) {
            String id = CASES[example][0];
            if (args.length > 1 && !java.util.Arrays.asList(args[1].split(",")).contains(id)) {
                continue;
            }
            Path directory = root.resolve(id);
            Files.createDirectories(directory);
            Path reference = directory.resolve("reference.swf");
            try (OutputStream out = Files.newOutputStream(reference)) {
                movie(example).saveTo(out);
            }
            SWF swf;
            try (InputStream in = Files.newInputStream(reference)) {
                swf = new SWF(in, true);
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
            Path publishPath = directory.resolve("PublishSettings.xml");
            String publish = new String(Files.readAllBytes(publishPath), StandardCharsets.UTF_8)
                    .replace("<defaultNames>1</defaultNames>", "<defaultNames>0</defaultNames>")
                    .replace("<flashDefaultName>1</flashDefaultName>", "<flashDefaultName>0</flashDefaultName>")
                    .replace("<flashFileName>" + id + ".swf</flashFileName>", "<flashFileName>" + id + "_compiled.swf</flashFileName>");
            Files.write(publishPath, publish.getBytes(StandardCharsets.UTF_8));
            java.awt.image.BufferedImage preview = SWF.frameToImageGet(swf.getTimeline(), 30, 30,
                    null, 0, swf.displayRect,
                    new com.jpexs.decompiler.flash.exporters.commonshape.Matrix(),
                    new com.jpexs.decompiler.flash.types.ColorTransform(), new java.awt.Color(240, 240, 240),
                    1, true, 1).getBufferedImage();
            boolean visible = false;
            for (int y = 0; y < preview.getHeight() && !visible; y++) {
                for (int x = 0; x < preview.getWidth(); x++) {
                    if ((preview.getRGB(x, y) & 0xffffff) != 0xf0f0f0) {
                        visible = true;
                        break;
                    }
                }
            }
            if (!visible) {
                throw new AssertionError("Invisible fixture " + id);
            }
            javax.imageio.ImageIO.write(preview, "png", directory.resolve("preview.png").toFile());
            StringBuilder csv = new StringBuilder("frameIndex,aFixed16,bFixed16,cFixed16,dFixed16,txTwips,tyTwips,"
                    + "rMultFixed8,gMultFixed8,bMultFixed8,aMultFixed8,rOffset,gOffset,bOffset,aOffset\n");
            for (int f = 0; f < swf.frameCount; f++) {
                com.jpexs.decompiler.flash.timeline.DepthState state = swf.getTimeline().getFrame(f).layers.get(1);
                MATRIX m = state.matrix;
                com.jpexs.decompiler.flash.types.ColorTransform c = state.colorTransForm;
                csv.append(f).append(',').append(m.getScaleXInteger()).append(',').append(m.getRotateSkew0Integer())
                        .append(',').append(m.getRotateSkew1Integer()).append(',').append(m.getScaleYInteger())
                        .append(',').append(m.translateX).append(',').append(m.translateY)
                        .append(',').append(c.getRedMulti()).append(',').append(c.getGreenMulti())
                        .append(',').append(c.getBlueMulti()).append(',').append(c.getAlphaMulti())
                        .append(',').append(c.getRedAdd()).append(',').append(c.getGreenAdd())
                        .append(',').append(c.getBlueAdd()).append(',').append(c.getAlphaAdd()).append('\n');
            }
            Files.write(directory.resolve("expected.csv"), csv.toString().getBytes(StandardCharsets.UTF_8));
            Path documentPath = directory.resolve("DOMDocument.xml");
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(documentPath.toFile());
            int motionFrames = 0;
            org.w3c.dom.NodeList frames = document.getElementsByTagName("DOMFrame");
            for (int i = 0; i < frames.getLength(); i++) {
                if ("motion object".equals(((Element) frames.item(i)).getAttribute("tweenType"))) {
                    motionFrames++;
                }
            }
            // These ideal fractional-angle samples cannot be published
            // faithfully by CS6's motion-object trigonometric lookup.
            boolean nativeUnsafeAngles = example == 4 || example == 15;
            if (motionFrames == 0 && !nativeUnsafeAngles) {
                throw new AssertionError("No motion object detected for " + id);
            }
            if (nativeUnsafeAngles && motionFrames != 0) {
                throw new AssertionError("Unsafe fractional-angle motion object for " + id);
            }
            String description = "# " + id + "\n\n" + CASES[example][1] + "\n\n"
                    + "Open `" + id + ".xfl` in Flash CS6 and publish to `" + id + "_compiled.swf` (already configured).\n\n"
                    + "Reference: `reference.swf`, 30 fps, 640 x 360 pixels, " + swf.frameCount + " frames. "
                    + "Instance to compare: `Demo`. The export contains " + motionFrames + " motion object spans. "
                    + (nativeUnsafeAngles
                        ? "The detector retains ordinary keyframes because CS6 cannot faithfully publish these fractional-angle motion properties."
                        : "Some channels use sampled property keys to preserve SWF-frame values.") + "\n\n"
                    + "`expected.csv` contains reference matrices and RGBA transforms for each frame. "
                    + "Indices start at zero; matrices use fixed16, multipliers use fixed8, and translations use twips. "
                    + "`preview.png` shows frame 31 of the reference SWF.\n\n"
                    + "The XFL document and reference SWF require no external images, fonts or scripts.\n";
            Files.write(directory.resolve("README.md"), description.getBytes(StandardCharsets.UTF_8));
            overview.append("- [").append(id).append("](").append(id).append('/').append(id).append(".xfl): ")
                    .append(CASES[example][1]).append('\n');
            System.out.println(id + " motion frames=" + motionFrames);
        }
        overview.append("\nGenerator: `test/com/jpexs/decompiler/flash/xfl/MotionTweenFixtureGenerator.java`. "
                + "Run its `main` from the ffdec_lib directory; the optional first argument selects the output folder, and the second selects comma-separated example IDs.\n");
        if (args.length < 2) {
            Files.write(root.resolve("README.md"), overview.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
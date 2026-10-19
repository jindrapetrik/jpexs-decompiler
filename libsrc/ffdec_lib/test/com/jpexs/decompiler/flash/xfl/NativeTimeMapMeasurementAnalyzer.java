/*
 * Copyright (C) 2026 JPEXS, All rights reserved.
 * This library is free software; you can redistribute it and/or modify it
 * under the GNU Lesser General Public License, version 3.0 or later.
 */
package com.jpexs.decompiler.flash.xfl;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.timeline.DepthState;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/** Extracts raw native measurements without fitting or assuming endpoint progress. */
public final class NativeTimeMapMeasurementAnalyzer {
    private static int number(Properties p, String key) {
        return Integer.parseInt(p.getProperty(key));
    }

    private static long[] staticChannels(DepthState state) {
        MATRIX m = state.matrix;
        ColorTransform c = state.colorTransForm == null ? new ColorTransform() : state.colorTransForm;
        return new long[]{m.getScaleXInteger(), m.getRotateSkew0Integer(), m.getRotateSkew1Integer(),
            m.getScaleYInteger(), m.translateY, c.getRedMulti(), c.getGreenMulti(), c.getBlueMulti(),
            c.getAlphaMulti(), c.getRedAdd(), c.getGreenAdd(), c.getBlueAdd(), c.getAlphaAdd(),
            state.isVisible ? 1 : 0, state.cacheAsBitmap ? 1 : 0,
            state.blendMode == 0 ? 1 : state.blendMode, state.clipDepth, state.ratio,
            state.depth, state.characterId};
    }

    private static int[] samples(Path directory, String name, Properties p, StringBuilder report) throws Exception {
        SWF swf;
        try (InputStream in = Files.newInputStream(directory.resolve(name + ".swf"))) {
            swf = new SWF(in, true);
        }
        int frames = number(p, "frames"), start = number(p, "startXTwips"), delta = number(p, "deltaXTwips");
        if (swf.frameCount != frames || swf.getTimeline().getFrameCount() != frames
                || swf.frameRate != number(p, "fps") || swf.displayRect.Xmin != 0 || swf.displayRect.Ymin != 0
                || swf.displayRect.Xmax != 32000 || swf.displayRect.Ymax != 7200) {
            throw new IllegalStateException("Unexpected SWF metadata in " + name);
        }
        int[] positions = new int[frames];
        long[] baseline = null;
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        // Preserve a failed extraction separately; only complete CSV files are named samples_*.
        Path partial = directory.resolve("partial_" + name + ".csv");
        try (PrintWriter csv = new PrintWriter(partial.toFile(), "UTF-8")) {
            csv.println("frameIndex,t,xTwips,progress,yTwips,scaleXFixed16");
            for (int f = 0; f < frames; f++) {
                if (swf.getTimeline().getFrame(f).layers.size() != 1) {
                    throw new IllegalStateException("Unexpected instance count at frame " + f + " in " + name);
                }
                DepthState state = swf.getTimeline().getFrame(f).layers.values().iterator().next();
                if (!"Demo".equals(state.instanceName) || state.matrix == null
                        || state.filters != null && !state.filters.isEmpty()
                        || state.clipActions != null || state.amfData != null || state.backGroundColor != null) {
                    throw new IllegalStateException("Unexpected instance/filters at frame " + f + " in " + name);
                }
                long[] channels = staticChannels(state);
                if (baseline == null) {
                    baseline = channels;
                    long[] expected = {65536, 0, 0, 65536, number(p, "yTwips"), 256, 256, 256, 256,
                        0, 0, 0, 0, 1, 0, 1};
                    if (!Arrays.equals(expected, Arrays.copyOf(channels, expected.length))) {
                        throw new IllegalStateException("Unexpected initial static channels in " + name);
                    }
                } else if (!Arrays.equals(baseline, channels)) {
                    throw new IllegalStateException("Static channel changed at frame " + f + " in " + name);
                }
                positions[f] = state.matrix.translateX;
                double progress = (positions[f] - start) / (double) delta;
                min = Math.min(min, progress); max = Math.max(max, progress);
                csv.println(f + "," + f / (double) (frames - 1) + "," + positions[f] + ","
                        + progress + "," + state.matrix.translateY + "," + state.matrix.getScaleXInteger());
            }
            if (csv.checkError()) { throw new IllegalStateException("Cannot write " + partial); }
        }
        Files.move(partial, directory.resolve("samples_" + name + ".csv"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        report.append("| ").append(p.getProperty("id")).append(" | ").append(p.getProperty("split"))
                .append(" | ").append(name).append(" | ").append(min).append(" | ").append(max)
                .append(" | ").append((positions[0] - start) / (double) delta)
                .append(" | ").append((positions[frames - 1] - start) / (double) delta).append(" |\n");
        return positions;
    }

    private static int compare(int[] a, int[] b, String label, StringBuilder comparisons) {
        int changed = 0, max = 0;
        for (int i = 0; i < a.length; i++) {
            int difference = Math.abs(a[i] - b[i]);
            if (difference != 0) { changed++; }
            max = Math.max(max, difference);
        }
        comparisons.append("- ").append(label).append(": ").append(changed)
                .append(" changed frames; maximum ").append(max).append(" twips difference.\n");
        return max;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) { throw new IllegalArgumentException("Usage: NativeTimeMapMeasurementAnalyzer <run-directory>"); }
        Path root = Paths.get(args[0]);
        List<Path> directories = new ArrayList<>();
        try (DirectoryStream<Path> children = Files.newDirectoryStream(root)) {
            for (Path child : children) {
                if (Files.isRegularFile(child.resolve("measurement.properties"))) { directories.add(child); }
            }
        }
        Collections.sort(directories);
        if (directories.isEmpty()) { throw new IllegalStateException("No measurement cases found in " + root); }
        StringBuilder rows = new StringBuilder("| Case | Role | Publication | Min progress | Max progress | First | Last |\n"
                + "| --- | --- | --- | ---: | ---: | ---: | ---: |\n");
        StringBuilder comparisons = new StringBuilder(), failures = new StringBuilder();
        int ok = 0;
        for (Path directory : directories) {
            Properties p = new Properties();
            try {
                try (InputStream in = Files.newInputStream(directory.resolve("measurement.properties"))) { p.load(in); }
                String result = new String(Files.readAllBytes(directory.resolve("RESULT.txt")), StandardCharsets.UTF_8).trim();
                if (!result.equals("OK " + p.getProperty("id"))) { throw new IllegalStateException(result); }
                if ("RandomSquareWave".equals(p.getProperty("type"))) {
                    int[] a = samples(directory, "same_session_1", p, rows);
                    int[] b = samples(directory, "same_session_2", p, rows);
                    int[] c = samples(directory, "reopened_1", p, rows);
                    int[] d = samples(directory, "reopened_2", p, rows);
                    compare(a, b, p.getProperty("id") + " same session", comparisons);
                    compare(c, d, p.getProperty("id") + " reopened session", comparisons);
                    compare(a, c, p.getProperty("id") + " across save/reopen", comparisons);
                } else {
                    int[] a = samples(directory, "imported", p, rows);
                    int[] b = samples(directory, "persisted", p, rows);
                    if (compare(a, b, p.getProperty("id") + " across save/reopen", comparisons) > 1) {
                        throw new IllegalStateException("Deterministic curve changed by more than one twip after save/reopen");
                    }
                }
                ok++;
            } catch (Exception error) {
                failures.append("- ").append(directory.getFileName()).append(": ").append(error).append('\n');
            }
        }
        if (directories.size() != 28) { failures.append("- Incomplete campaign: expected 28 cases, found ").append(directories.size()).append(".\n"); }
        String summary = "# Native TimeMap measurement analysis\n\n" + ok + " / " + directories.size()
                + " available cases passed metadata, static-channel and publication checks.\n\n"
                + "Validation rows must be excluded from formula fitting. No candidate formula is fitted here. "
                + "Random equality only describes the measured publications; it does not establish a universal seed rule.\n\n"
                + rows + "\n## Repeated publication comparisons\n\n" + comparisons
                + "\n## Failures\n\n" + (failures.length() == 0 ? "None.\n" : failures.toString());
        Files.write(root.resolve("ANALYSIS.md"), summary.getBytes(StandardCharsets.UTF_8));
        System.out.println("Analyzed " + ok + " / " + directories.size() + " cases: " + root.resolve("ANALYSIS.md"));
        if (failures.length() != 0) { throw new IllegalStateException("Measurement checks failed; see ANALYSIS.md"); }
    }
}

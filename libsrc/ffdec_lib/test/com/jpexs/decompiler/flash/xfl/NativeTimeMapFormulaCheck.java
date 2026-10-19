/*
 * Copyright (C) 2026 JPEXS, All rights reserved.
 * This library is free software; you can redistribute it and/or modify it
 * under the GNU Lesser General Public License, version 3.0 or later.
 */
package com.jpexs.decompiler.flash.xfl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/** Experimental formulas verified against measured native data, not exporter support. */
public final class NativeTimeMapFormulaCheck {
    private static double spring(double t, int strength) {
        double first = 0.4 / strength;
        if (t <= first) { return Math.sin(Math.PI * t / (2 * first)); }
        double u = 2 * strength * (t - first) / (1 - first);
        return 0.7 + 0.3 * Math.exp(-u / 2) * Math.cos(Math.PI * u);
    }

    private static double bounce(int frame, int intervals, int strength, boolean inward) {
        double harmonic = 0;
        for (int j = 1; j <= strength; j++) { harmonic += 1.0 / j; }
        int start = 0;
        for (int j = 1; j <= strength; j++) {
            // Native CS6 rounds individual segment widths to frames. The last
            // segment is truncated if necessary; undershoot leaves a flat tail.
            int width = (int) Math.round(intervals / (j * harmonic));
            width = Math.min(width, intervals - start);
            if (width <= 0) { break; }
            if (frame <= start + width) {
                double u = (frame - start) / (double) width;
                if (inward && j == 1) { return u * u; }
                double parabola = Math.exp(1 - j) * 4 * u * (1 - u);
                return inward ? 1 - parabola : parabola;
            }
            start += width;
        }
        return inward ? 1 : 0;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) { throw new IllegalArgumentException("Usage: NativeTimeMapFormulaCheck <analyzed-run-directory>"); }
        Path root = Paths.get(args[0]);
        List<Path> directories = new ArrayList<>();
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(root)) {
            for (Path entry : entries) {
                if (Files.isRegularFile(entry.resolve("measurement.properties"))) { directories.add(entry); }
            }
        }
        Collections.sort(directories);
        StringBuilder rows = new StringBuilder("| Case | Role | Publication | Compared frames | Max rounded error (twips) | Max raw error (px) |\n"
                + "| --- | --- | --- | ---: | ---: | ---: |\n");
        int publications = 0, totalFrames = 0, globalMax = 0, validations = 0;
        for (Path directory : directories) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(directory.resolve("measurement.properties"))) { p.load(in); }
            String type = p.getProperty("type");
            if (type.equals("RandomSquareWave")) { continue; }
            if (!type.equals("Spring") && !type.equals("Bounce") && !type.equals("BounceIn")) {
                throw new IllegalStateException("Unexpected type: " + type);
            }
            int strength = Integer.parseInt(p.getProperty("strength"));
            int frames = Integer.parseInt(p.getProperty("frames"));
            int start = Integer.parseInt(p.getProperty("startXTwips"));
            int delta = Integer.parseInt(p.getProperty("deltaXTwips"));
            for (String publication : new String[]{"imported", "persisted"}) {
                List<String> samples = Files.readAllLines(directory.resolve("samples_" + publication + ".csv"), StandardCharsets.UTF_8);
                if (samples.size() != frames + 1) { throw new IllegalStateException("Incomplete samples: " + directory); }
                int max = 0;
                double rawMax = 0;
                for (int f = 0; f < frames; f++) {
                    String[] fields = samples.get(f + 1).split(",");
                    if (Integer.parseInt(fields[0]) != f) { throw new IllegalStateException("Noncontiguous samples"); }
                    double value = type.equals("Spring") ? spring(f / (double) (frames - 1), strength)
                            : bounce(f, frames - 1, strength, type.equals("BounceIn"));
                    double expected = start + delta * value;
                    int actual = Integer.parseInt(fields[2]);
                    max = Math.max(max, (int) Math.abs(actual - Math.round(expected)));
                    rawMax = Math.max(rawMax, Math.abs(actual - expected) / 20);
                }
                rows.append("| ").append(directory.getFileName()).append(" | ").append(p.getProperty("split"))
                        .append(" | ").append(publication).append(" | ").append(frames).append(" | ")
                        .append(max).append(" | ").append(rawMax).append(" |\n");
                globalMax = Math.max(globalMax, max); publications++; totalFrames += frames;
                if (p.getProperty("split").equals("validation")) { validations++; }
            }
        }
        String report = "# Experimental native formula verification\n\n" + publications + " publications, " + totalFrames
                + " frame positions; maximum rounded residual " + globalMax + " twip(s). " + validations
                + " publications are held-out strength 6 with a different length/frame rate.\n\n"
                + "Bounce uses frame-rounded segment widths; a duration-independent harmonic formula fails these samples. "
                + "Spring uses an initial sine rise followed by a damped cosine about 0.7. "
                + "Only measured positive integer strengths are validated. Random is excluded; repeatability "
                + "does not establish its PRNG or arbitrary-strength formula. Exporter support is unchanged.\n\n" + rows;
        Files.write(root.resolve("FORMULA_CHECK.md"), report.getBytes(StandardCharsets.UTF_8));
        System.out.println("Formula checks: " + publications + " publications, " + totalFrames + " frames, max " + globalMax + " twip(s).");
        if (publications != 42 || validations != 6 || globalMax > 1) {
            throw new IllegalStateException("Formula validation incomplete or outside one-twip tolerance; see FORMULA_CHECK.md");
        }
    }
}

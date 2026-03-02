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
package com.jpexs.helpers;


import com.jpexs.decompiler.flash.types.RGBA;
import java.util.ArrayList;
import java.util.List;

public final class GradientUtil {

    private GradientUtil() {
    }

    /**
     * Matches SVG's color-interpolation attribute.
     */
    public enum ColorInterpolation {
        /**
         * Interpolate directly in sRGB-encoded component values (0..255).
         */
        SRGB,
        /**
         * Convert sRGB -> linear RGB, interpolate in linear space, convert back
         * to sRGB.
         */
        LINEAR_RGB
    }

    /**
     * Returns the gradient color at position k (0..255), where "ratio" defines
     * stop positions (0..255) and "colors" defines stop colors. The stop lookup
     * is identical for both modes; only interpolation differs.
     *
     * Assumptions: - ratio[] is sorted ascending (0..255). - colors.length ==
     * ratio.length.
     */
    public static RGBA colorAt(RGBA[] colors, int[] ratio, float k, ColorInterpolation mode) {
        if (colors == null || ratio == null || colors.length == 0 || colors.length != ratio.length) {
            throw new IllegalArgumentException("colors and ratio must be non-empty and have the same length.");
        }

        // Clamp query position into [0, 255]
        k = clamp(k, 0f, 255f);

        // Handle outside range quickly
        int n = ratio.length;
        if (k <= ratio[0]) {
            return colors[0];
        }
        if (k >= ratio[n - 1]) {
            return colors[n - 1];
        }

        // Find the segment [i, i+1] such that ratio[i] <= k <= ratio[i+1]
        int i = 0;
        while (i < n - 1 && k > ratio[i + 1]) {
            i++;
        }

        int p0 = ratio[i];
        int p1 = ratio[i + 1];
        RGBA c0 = colors[i];
        RGBA c1 = colors[i + 1];

        // Degenerate case: two stops at the same position
        if (p1 == p0) {
            return c1;
        }

        float t = (k - p0) / (float) (p1 - p0); // 0..1

        // Interpolate alpha linearly in all modes
        int a = lerp8(c0.alpha, c1.alpha, t);

        if (mode == ColorInterpolation.SRGB) {
            // SVG color-interpolation="sRGB": interpolate directly in sRGB component values
            int r = lerp8(c0.red, c1.red, t);
            int g = lerp8(c0.green, c1.green, t);
            int b = lerp8(c0.blue, c1.blue, t);
            return new RGBA(r, g, b, a);
        } else {
            // SVG color-interpolation="linearRGB": gamma-correct interpolation
            float r = lerpLinearRgbChannel(c0.red, c1.red, t);
            float g = lerpLinearRgbChannel(c0.green, c1.green, t);
            float b = lerpLinearRgbChannel(c0.blue, c1.blue, t);

            return new RGBA(
                    clamp255(Math.round(r * 255f)),
                    clamp255(Math.round(g * 255f)),
                    clamp255(Math.round(b * 255f)),
                    a
            );
        }
    }
    
    public static final class SplitResult {
        public final RGBA[] colorsA;
        public final int[] ratioA;
        public final RGBA[] colorsB;
        public final int[] ratioB;

        public SplitResult(RGBA[] colorsA, int[] ratioA, RGBA[] colorsB, int[] ratioB) {
            this.colorsA = colorsA;
            this.ratioA = ratioA;
            this.colorsB = colorsB;
            this.ratioB = ratioB;
        }
    }

    /**
     * Splits a gradient into two halves:
     *  - A: original positions 0..127 mapped to 0..255
     *  - B: original positions 128..255 mapped to 0..255
     *
     * Notes:
     * - Stop lookup is based on original ratio[].
     * - Boundary stops at 127 (A) and 128 (B) are ensured (interpolated if missing).
     * - ratio[] is assumed sorted ascending.
     */
    public static SplitResult splitIntoHalves(
            RGBA[] colors, int[] ratio, ColorInterpolation mode
    ) {
        // Build left half (0..127)
        Stops left = extractRange(colors, ratio, 0, 127, mode);

        // Build right half (128..255)
        Stops right = extractRange(colors, ratio, 128, 255, mode);

        // Remap ratios to 0..255 in each half
        int[] ratioA = remap(left.ratio, 0, 127);
        int[] ratioB = remap(right.ratio, 128, 255);

        return new SplitResult(
                left.colors.toArray(new RGBA[0]), ratioA,
                right.colors.toArray(new RGBA[0]), ratioB
        );
    }

    // ----- Internal representation of stops -----

    private static final class Stops {
        final List<RGBA> colors = new ArrayList<>();
        final List<Integer> ratio = new ArrayList<>();
    }

    /**
     * Extracts all stops within [from..to] (inclusive), and ensures stops at both ends exist.
     * If an endpoint stop is missing, it is computed via SvgGradient.colorAt(...).
     */
    private static Stops extractRange(RGBA[] colors, int[] ratio, int from, int to,
                                      ColorInterpolation mode) {
        Stops out = new Stops();

        // Ensure start stop
        addStop(out, from, stopColorAtOrExisting(colors, ratio, from, mode));

        // Add internal stops strictly inside (from, to)
        for (int i = 0; i < ratio.length; i++) {
            int p = ratio[i];
            if (p > from && p < to) {
                addStop(out, p, colors[i]);
            }
        }

        // Ensure end stop
        addStop(out, to, stopColorAtOrExisting(colors, ratio, to, mode));

        return out;
    }

    /**
     * If there is an existing stop exactly at pos, returns its color;
     * otherwise computes the color by interpolation at that position.
     */
    private static RGBA stopColorAtOrExisting(RGBA[] colors, int[] ratio, int pos,
                                              ColorInterpolation mode) {
        for (int i = 0; i < ratio.length; i++) {
            if (ratio[i] == pos) return colors[i];
        }
        return colorAt(colors, ratio, pos, mode);
    }

    /**
     * Adds a stop keeping order; if the same position already exists, it overwrites the color.
     */
    private static void addStop(Stops stops, int pos, RGBA color) {
        // Insert in ascending order (ratio is small, linear insert is fine)
        for (int i = 0; i < stops.ratio.size(); i++) {
            int existing = stops.ratio.get(i);
            if (existing == pos) {
                stops.colors.set(i, color);
                return;
            }
            if (existing > pos) {
                stops.ratio.add(i, pos);
                stops.colors.add(i, color);
                return;
            }
        }
        stops.ratio.add(pos);
        stops.colors.add(color);
    }

    /**
     * Remaps original integer positions in [from..to] to [0..255].
     * Uses rounding and clamps, guaranteeing endpoints map to 0 and 255.
     */
    private static int[] remap(List<Integer> original, int from, int to) {
        int span = to - from; // for 0..127 and 128..255 span is 127
        int[] out = new int[original.size()];

        for (int i = 0; i < original.size(); i++) {
            int p = original.get(i);
            float t = (p - from) / (float) span;     // 0..1
            int mapped = Math.round(t * 255f);       // 0..255
            out[i] = clamp255(mapped);
        }

        // Force exact endpoints (avoid any rounding surprises)
        if (out.length > 0) {
            out[0] = 0;
            out[out.length - 1] = 255;
        }
        return out;
    }

    // ----- Interpolation helpers -----
    private static int lerp8(int v0, int v1, float t) {
        return clamp255(Math.round(v0 + (v1 - v0) * t));
    }

    /**
     * Interpolates a single channel using linearRGB mode: sRGB (0..1) -> linear
     * (0..1) -> lerp -> sRGB (0..1)
     */
    private static float lerpLinearRgbChannel(int c0_8bit, int c1_8bit, float t) {
        float s0 = c0_8bit / 255f;
        float s1 = c1_8bit / 255f;

        float l0 = srgbToLinear(s0);
        float l1 = srgbToLinear(s1);

        float l = l0 + (l1 - l0) * t;

        return linearToSrgb(l);
    }

    // ----- sRGB transfer functions -----
    private static float srgbToLinear(float c) {
        // IEC 61966-2-1 sRGB
        if (c <= 0.04045f) {
            return c / 12.92f;
        }
        return (float) Math.pow((c + 0.055f) / 1.055f, 2.4);
    }

    private static float linearToSrgb(float c) {
        // IEC 61966-2-1 sRGB
        if (c <= 0.0031308f) {
            return 12.92f * c;
        }
        return 1.055f * (float) Math.pow(c, 1.0 / 2.4) - 0.055f;
    }

    // ----- Clamp helpers -----
    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static int clamp255(int v) {
        return Math.max(0, Math.min(255, v));
    }
}

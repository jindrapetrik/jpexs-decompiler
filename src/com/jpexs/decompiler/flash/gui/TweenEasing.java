/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.types.MATRIX;

/**
 * Cubic Bezier easing and matrix interpolation used by motion tweens.
 */
public final class TweenEasing {

    private TweenEasing() {
    }

    /**
     * Applies the classic Flash easing strength. Negative values ease in,
     * positive values ease out and zero is linear.
     *
     * @param progress Linear progress from zero to one
     * @param easing Easing strength from -100 to 100
     * @return Eased progress
     */
    public static double ease(double progress, int easing) {
        double x = Math.max(0, Math.min(1, progress));
        double strength = Math.max(-100, Math.min(100, easing)) / 100.0;
        if (strength < 0) {
            return x * (x * -strength + 1 + strength);
        }
        return x * ((2 - x) * strength + 1 - strength);
    }

    public static double ease(double progress, double x1, double y1, double x2, double y2) {
        double x = Math.max(0, Math.min(1, progress));
        double low = 0;
        double high = 1;
        double parameter = x;
        for (int i = 0; i < 20; i++) {
            parameter = (low + high) / 2;
            double currentX = cubic(parameter, x1, x2);
            if (currentX < x) {
                low = parameter;
            } else {
                high = parameter;
            }
        }
        return cubic(parameter, y1, y2);
    }

    private static double cubic(double t, double p1, double p2) {
        double inverse = 1 - t;
        return 3 * inverse * inverse * t * p1
                + 3 * inverse * t * t * p2 + t * t * t;
    }

    public static MATRIX interpolate(MATRIX start, MATRIX end, double progress) {
        MATRIX result = new MATRIX();
        result.scaleX = lerp(start.getScaleXFloat(), end.getScaleXFloat(), progress);
        result.scaleY = lerp(start.getScaleYFloat(), end.getScaleYFloat(), progress);
        result.rotateSkew0 = lerp(start.getRotateSkew0Float(), end.getRotateSkew0Float(), progress);
        result.rotateSkew1 = lerp(start.getRotateSkew1Float(), end.getRotateSkew1Float(), progress);
        result.translateX = (int) Math.round(lerp(start.translateX, end.translateX, progress));
        result.translateY = (int) Math.round(lerp(start.translateY, end.translateY, progress));
        result.hasScale = Float.compare(result.scaleX, 1f) != 0 || Float.compare(result.scaleY, 1f) != 0;
        result.hasRotate = Float.compare(result.rotateSkew0, 0f) != 0 || Float.compare(result.rotateSkew1, 0f) != 0;
        return result;
    }

    private static float lerp(float start, float end, double progress) {
        return (float) (start + (end - start) * progress);
    }

    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }
}

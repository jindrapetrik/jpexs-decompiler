/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.types.CXFORMWITHALPHA;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.decompiler.flash.types.MATRIX;
import com.jpexs.decompiler.flash.types.RECT;

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
        if (progress == 0) {
            return new MATRIX(start);
        }
        if (progress == 1) {
            return new MATRIX(end);
        }

        MatrixComponents first = new MatrixComponents(start);
        MatrixComponents last = new MatrixComponents(end);
        // A completely collapsed object has no angle of its own.
        if (first.scaleX == 0 && first.scaleY == 0) {
            first.rotation = last.rotation;
            first.skew = last.skew;
        }
        if (last.scaleX == 0 && last.scaleY == 0) {
            last.rotation = first.rotation;
            last.skew = first.skew;
        }
        double rotation = first.rotation + normalizeAngle(last.rotation - first.rotation) * progress;
        double skew = lerp(first.skew, last.skew, progress);
        double scaleX = lerp(first.scaleX, last.scaleX, progress);
        double scaleY = lerp(first.scaleY, last.scaleY, progress);

        // Interpolating matrix entries directly shortens the axes during a
        // rotation (and collapses them entirely halfway through a half turn).
        MATRIX result = new MATRIX();
        result.scaleX = (float) (scaleX * Math.cos(rotation));
        result.scaleY = (float) (scaleY * Math.cos(rotation + skew));
        result.rotateSkew0 = (float) (scaleX * Math.sin(rotation));
        result.rotateSkew1 = (float) (-scaleY * Math.sin(rotation + skew));
        result.translateX = (int) Math.round(lerp(start.translateX, end.translateX, progress));
        result.translateY = (int) Math.round(lerp(start.translateY, end.translateY, progress));
        result.hasScale = result.scaleX != 1 || result.scaleY != 1;
        result.hasRotate = result.rotateSkew0 != 0 || result.rotateSkew1 != 0;
        return result;
    }

    /**
     * Interpolates a transform around the center of the character's bounds.
     * The center follows the line between its transformed endpoint positions.
     *
     * @param start Start transform
     * @param end End transform
     * @param progress Eased progress from zero to one
     * @param bounds Character bounds in local twips, or null to use the origin
     * @return Interpolated transform
     */
    public static MATRIX interpolate(MATRIX start, MATRIX end, double progress, RECT bounds) {
        return interpolate(start, end, progress, bounds, RegistrationPointPosition.CENTER);
    }

    /**
     * Interpolates a transform around a selected point of the character's
     * bounds on the stage at the start of the tween. The point follows the
     * line between its endpoint positions.
     *
     * @param start Start transform
     * @param end End transform
     * @param progress Eased progress from zero to one
     * @param bounds Character bounds in local twips, or null to use the origin
     * @param position Transformation point position within the initial stage bounds
     * @return Interpolated transform
     */
    public static MATRIX interpolate(MATRIX start, MATRIX end, double progress, RECT bounds,
            RegistrationPointPosition position) {
        MATRIX result = interpolate(start, end, progress);
        if (bounds == null || progress == 0 || progress == 1) {
            return result;
        }
        double pointX = bounds.Xmin + ((double) bounds.Xmax - bounds.Xmin) * position.getPositionX();
        double pointY = bounds.Ymin + ((double) bounds.Ymax - bounds.Ymin) * position.getPositionY();
        double a = start.getScaleXFloat();
        double b = start.getRotateSkew0Float();
        double c = start.getRotateSkew1Float();
        double d = start.getScaleYFloat();
        double determinant = a * d - b * c;
        if (determinant != 0) {
            double width = (double) bounds.Xmax - bounds.Xmin;
            double height = (double) bounds.Ymax - bounds.Ymin;
            // Match the Transform panel's axis-aligned stage bounds. Express
            // the chosen point relative to their center, then invert only
            // the linear transform to recover a fixed local pivot.
            double offsetX = (position.getPositionX() - 0.5) * (Math.abs(a) * width + Math.abs(c) * height);
            double offsetY = (position.getPositionY() - 0.5) * (Math.abs(b) * width + Math.abs(d) * height);
            pointX = ((double) bounds.Xmin + bounds.Xmax) / 2 + (d * offsetX - c * offsetY) / determinant;
            pointY = ((double) bounds.Ymin + bounds.Ymax) / 2 + (a * offsetY - b * offsetX) / determinant;
        }
        // A collapsed start transform has no inverse; retain its local point.
        double startX = start.getScaleXFloat() * pointX + start.getRotateSkew1Float() * pointY + start.translateX;
        double startY = start.getRotateSkew0Float() * pointX + start.getScaleYFloat() * pointY + start.translateY;
        double endX = end.getScaleXFloat() * pointX + end.getRotateSkew1Float() * pointY + end.translateX;
        double endY = end.getRotateSkew0Float() * pointX + end.getScaleYFloat() * pointY + end.translateY;
        result.translateX = (int) Math.round(lerp(startX, endX, progress)
                - result.getScaleXFloat() * pointX - result.getRotateSkew1Float() * pointY);
        result.translateY = (int) Math.round(lerp(startY, endY, progress)
                - result.getRotateSkew0Float() * pointX - result.getScaleYFloat() * pointY);
        return result;
    }

    public static CXFORMWITHALPHA interpolate(ColorTransform start, ColorTransform end, double progress) {
        ColorTransform effectiveStart = start == null ? new ColorTransform() : start;
        ColorTransform effectiveEnd = end == null ? new ColorTransform() : end;
        CXFORMWITHALPHA result = new CXFORMWITHALPHA();
        result.redAddTerm = lerp(effectiveStart.getRedAdd(), effectiveEnd.getRedAdd(), progress);
        result.greenAddTerm = lerp(effectiveStart.getGreenAdd(), effectiveEnd.getGreenAdd(), progress);
        result.blueAddTerm = lerp(effectiveStart.getBlueAdd(), effectiveEnd.getBlueAdd(), progress);
        result.alphaAddTerm = lerp(effectiveStart.getAlphaAdd(), effectiveEnd.getAlphaAdd(), progress);
        result.redMultTerm = lerp(effectiveStart.getRedMulti(), effectiveEnd.getRedMulti(), progress);
        result.greenMultTerm = lerp(effectiveStart.getGreenMulti(), effectiveEnd.getGreenMulti(), progress);
        result.blueMultTerm = lerp(effectiveStart.getBlueMulti(), effectiveEnd.getBlueMulti(), progress);
        result.alphaMultTerm = lerp(effectiveStart.getAlphaMulti(), effectiveEnd.getAlphaMulti(), progress);
        result.hasAddTerms = result.redAddTerm != 0 || result.greenAddTerm != 0
                || result.blueAddTerm != 0 || result.alphaAddTerm != 0;
        result.hasMultTerms = result.redMultTerm != 256 || result.greenMultTerm != 256
                || result.blueMultTerm != 256 || result.alphaMultTerm != 256;
        return result;
    }

    public static int interpolate(int start, int end, double progress) {
        return lerp(start, end, progress);
    }

    private static double normalizeAngle(double angle) {
        if (angle > Math.PI) {
            return angle - 2 * Math.PI;
        }
        if (angle < -Math.PI) {
            return angle + 2 * Math.PI;
        }
        return angle;
    }

    private static double lerp(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    private static int lerp(int start, int end, double progress) {
        return (int) Math.round(start + (end - start) * progress);
    }

    private static final class MatrixComponents {

        private final double scaleX;
        private final double scaleY;
        private double rotation;
        private double skew;

        private MatrixComponents(MATRIX matrix) {
            double a = matrix.getScaleXFloat();
            double b = matrix.getRotateSkew0Float();
            double c = matrix.getRotateSkew1Float();
            double d = matrix.getScaleYFloat();
            scaleX = Math.hypot(a, b);
            // Keep reflections in the Y scale so both axes share the same
            // rotation, including at the -PI/PI boundary.
            scaleY = Math.hypot(c, d) * (a * d - b * c < 0 ? -1 : 1);
            rotation = scaleX == 0 ? Math.atan2(-c, d) : Math.atan2(b, a);
            skew = scaleX == 0 || scaleY == 0 ? 0
                    : normalizeAngle(Math.atan2(-c / scaleY, d / scaleY) - rotation);
        }
    }
}

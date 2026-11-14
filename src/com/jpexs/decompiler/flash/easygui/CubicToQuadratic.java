/*
 *  Copyright (C) 2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.easygui;

import com.jpexs.decompiler.flash.SWF;

/**
 * Reduces cubics to quadratic segments with a bounded positional error.
 *
 * @author JPEXS
 */
final class CubicToQuadratic {

    // One quarter of a pixel in shape coordinates, before SWF integer rounding.
    static final double MAX_ERROR = SWF.unitDivisor / 4;

    private CubicToQuadratic() {
    }

    static double[][] approximate(double[] cubic) {
        double dx = cubic[6] - cubic[0] + 3 * (cubic[2] - cubic[4]);
        double dy = cubic[7] - cubic[1] + 3 * (cubic[3] - cubic[5]);
        // Q = (3(P1 + P2) - P0 - P3) / 4 interpolates both ends and the
        // midpoint. Its error is D*t*(t-1/2)*(t-1), where D is the cubic's
        // third difference. The exact maximum is |D|/(12*sqrt(3)). Splitting
        // into n equal parameter intervals reduces it by n^3. Equal intervals
        // also keep the quadratic pieces C1-continuous at their shared ends.
        double error = Math.hypot(dx, dy) / (12 * Math.sqrt(3));
        int count = Math.max(1, (int) Math.ceil(Math.cbrt(error / MAX_ERROR)));
        double step = 1.0 / count;
        double[][] result = new double[count][4];
        double[] start = point(cubic, 0);
        double[] startDerivative = derivative(cubic, 0);
        for (int i = 0; i < count; i++) {
            double t = (i + 1.0) / count;
            double[] end = point(cubic, t);
            double[] endDerivative = derivative(cubic, t);
            for (int axis = 0; axis < 2; axis++) {
                result[i][axis] = (start[axis] + end[axis]) / 2
                        + step * (startDerivative[axis] - endDerivative[axis]) / 4;
                result[i][axis + 2] = end[axis];
            }
            start = end;
            startDerivative = endDerivative;
        }
        return result;
    }

    private static double[] point(double[] cubic, double t) {
        double u = 1 - t;
        double[] result = new double[2];
        for (int axis = 0; axis < 2; axis++) {
            result[axis] = u * u * u * cubic[axis] + 3 * u * u * t * cubic[axis + 2]
                    + 3 * u * t * t * cubic[axis + 4] + t * t * t * cubic[axis + 6];
        }
        return result;
    }

    private static double[] derivative(double[] cubic, double t) {
        double u = 1 - t;
        double[] result = new double[2];
        for (int axis = 0; axis < 2; axis++) {
            result[axis] = 3 * (u * u * (cubic[axis + 2] - cubic[axis])
                    + 2 * u * t * (cubic[axis + 4] - cubic[axis + 2]) + t * t * (cubic[axis + 6] - cubic[axis + 4]));
        }
        return result;
    }
}

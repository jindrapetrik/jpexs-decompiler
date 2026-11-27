/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

// Adapted for FFDec on 2026-11-17. See NOTICE.txt in this package.
package com.jpexs.decompiler.flash.math.area;

strictfp final class QuadraticEquation {
    private QuadraticEquation() {
    }

    static int solve(double[] eqn, double[] res) {
        double a = eqn[2];
        double b = eqn[1];
        double c = eqn[0];
        int roots = 0;
        if (a == 0.0) {
            // The quadratic parabola has degenerated to a line.
            if (b == 0.0) {
                // The line has degenerated to a constant.
                return -1;
            }
            res[roots++] = -c / b;
        } else {
            // From Numerical Recipes, 5.6, Quadratic and Cubic Equations
            double d = b * b - 4.0 * a * c;
            if (d < 0.0) {
                // If d < 0.0, then there are no roots
                return 0;
            }
            d = StrictMath.sqrt(d);
            // For accuracy, calculate one root using:
            //     (-b +/- d) / 2a
            // and the other using:
            //     2c / (-b +/- d)
            // Choose the sign of the +/- so that b+d gets larger in magnitude
            if (b < 0.0) {
                d = -d;
            }
            double q = (b + d) / -2.0;
            // We already tested a for being 0 above
            res[roots++] = q / a;
            if (q != 0.0) {
                res[roots++] = c / q;
            }
        }
        return roots;
    }
}

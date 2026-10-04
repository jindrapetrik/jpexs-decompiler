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

/** Standard normalized easing curves; not Adobe TimeMap identifiers. */
final class PresetEasing {
    enum Family {
        QUAD, CUBIC, QUART, QUINT, SINE, CIRC, EXPO, BACK, BOUNCE, ELASTIC
    }

    private PresetEasing() {
    }

    /** Direction: 0 = in, 1 = out, 2 = in/out. */
    static double progress(Family family, int direction, double t) {
        if (t <= 0 || t >= 1) {
            return t;
        }
        if (direction == 1) {
            return 1 - in(family, 1 - t, false);
        }
        if (direction == 2) {
            return t < 0.5 ? in(family, 2 * t, true) / 2
                    : 1 - in(family, 2 - 2 * t, true) / 2;
        }
        return in(family, t, false);
    }

    private static double in(Family family, double t, boolean dual) {
        switch (family) {
            case QUAD:
                return t * t;
            case CUBIC:
                return t * t * t;
            case QUART:
                return t * t * t * t;
            case QUINT:
                return t * t * t * t * t;
            case SINE:
                return 1 - Math.cos(Math.PI * t / 2);
            case CIRC:
                return 1 - Math.sqrt(Math.max(0, 1 - t * t));
            case EXPO:
                return t == 0 ? 0 : Math.pow(2, 10 * (t - 1));
            case BACK:
                double overshoot = 1.70158 * (dual ? 1.525 : 1);
                return t * t * ((overshoot + 1) * t - overshoot);
            case BOUNCE:
                return 1 - bounceOut(1 - t);
            case ELASTIC:
                if (t == 0 || t == 1) {
                    return t;
                }
                double period = dual ? 0.45 : 0.3;
                return -Math.pow(2, 10 * (t - 1))
                        * Math.sin((t - 1 - period / 4) * 2 * Math.PI / period);
            default:
                throw new AssertionError(family);
        }
    }

    private static double bounceOut(double t) {
        if (t < 4.0 / 11) {
            return 121.0 / 16 * t * t;
        }
        if (t < 8.0 / 11) {
            double u = t - 6.0 / 11;
            return 121.0 / 16 * u * u + 0.75;
        }
        if (t < 10.0 / 11) {
            double u = t - 9.0 / 11;
            return 121.0 / 16 * u * u + 0.9375;
        }
        double u = t - 21.0 / 22;
        return 121.0 / 16 * u * u + 0.984375;
    }
}

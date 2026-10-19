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

/** Adobe TimeMap identifiers supported by the exporter. */
final class NativeTimeMap {
    enum Type {
        QUADRATIC("Quadratic"), DUAL_QUADRATIC("DualQuadratic"),
        CUBIC("Cubic", 3, false), QUARTIC("Quartic", 4, false), QUINTIC("Quintic", 5, false),
        DUAL_CUBIC("DualCubic", 3, true), DUAL_QUARTIC("DualQuartic", 4, true), DUAL_QUINTIC("DualQuintic", 5, true),
        SPRING("Spring"), BOUNCE("Bounce"), BOUNCE_IN("BounceIn");

        final String xmlName;
        final int power;
        final boolean dual;

        Type(String xmlName) {
            this(xmlName, 0, false);
        }

        Type(String xmlName, int power, boolean dual) {
            this.xmlName = xmlName;
            this.power = power;
            this.dual = dual;
        }
    }

    final Type type;
    final int strength;
    private final int intervals;

    NativeTimeMap(Type type, int strength) {
        this(type, strength, 0);
    }

    NativeTimeMap(Type type, int strength, int intervals) {
        if (type == null || strength < -100 || strength > 100) {
            throw new IllegalArgumentException("Invalid native easing");
        }
        this.type = type;
        this.strength = strength;
        if ((type == Type.SPRING || type == Type.BOUNCE || type == Type.BOUNCE_IN) && strength <= 0
                || (type == Type.BOUNCE || type == Type.BOUNCE_IN) && intervals <= 0) {
            throw new IllegalArgumentException("Invalid native wave parameters");
        }
        this.intervals = type == Type.BOUNCE || type == Type.BOUNCE_IN ? intervals : 0;
    }

    double progress(double t) {
        if (isPolynomial()) {
            double q = type.dual ? t <= 0.5 ? 0.5 * (1 - Math.pow(1 - 2 * t, type.power))
                    : 0.5 * (1 + Math.pow(2 * t - 1, type.power))
                    : 1 - Math.pow(1 - t, type.power);
            return t + strength / 100.0 * (q - t);
        }
        if (type == Type.SPRING) {
            double first = 0.4 / strength;
            if (t <= first) {
                return Math.sin(Math.PI * t / (2 * first));
            }
            double u = 2 * strength * (t - first) / (1 - first);
            return 0.7 + 0.3 * Math.exp(-u / 2) * Math.cos(Math.PI * u);
        }
        if (type == Type.BOUNCE || type == Type.BOUNCE_IN) {
            double harmonic = 0;
            for (int j = 1; j <= strength; j++) {
                harmonic += 1.0 / j;
            }
            double frame = t * intervals;
            int start = 0;
            for (int j = 1; j <= strength; j++) {
                int width = Math.min((int) Math.round(intervals / (j * harmonic)), intervals - start);
                if (width <= 0) {
                    break;
                }
                if (frame <= start + width + 1e-9) {
                    double u = Math.max(0, Math.min(1, (frame - start) / width));
                    if (type == Type.BOUNCE_IN && j == 1) {
                        return u * u;
                    }
                    double parabola = Math.exp(1 - j) * 4 * u * (1 - u);
                    return type == Type.BOUNCE_IN ? 1 - parabola : parabola;
                }
                start += width;
            }
            return type == Type.BOUNCE_IN ? 1 : 0;
        }
        double basis = type == Type.QUADRATIC ? t * (1 - t)
                : (t < 0.5 ? t : 1 - t) * (1 - 2 * t);
        return t + strength / 100.0 * basis;
    }

    boolean isWave() {
        return type == Type.SPRING || type == Type.BOUNCE || type == Type.BOUNCE_IN;
    }

    boolean isPolynomial() {
        return type.power >= 3;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof NativeTimeMap)) {
            return false;
        }
        NativeTimeMap map = (NativeTimeMap) other;
        return type == map.type && strength == map.strength && intervals == map.intervals;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * type.hashCode() + strength) + intervals;
    }
}

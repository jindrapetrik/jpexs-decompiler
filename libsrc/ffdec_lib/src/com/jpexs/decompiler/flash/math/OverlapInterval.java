package com.jpexs.decompiler.flash.math;

import java.util.Locale;

/**
 *
 * @author JPEXS
 */
public class OverlapInterval {
    public final double t0, t1; // on curve A
    public final double u0, u1; // on curve B
    public OverlapInterval(double t0, double t1, double u0, double u1) {
        // normalize so t0<=t1 and u0<=u1
        this.t0 = Math.min(t0, t1);
        this.t1 = Math.max(t0, t1);
        this.u0 = Math.min(u0, u1);
        this.u1 = Math.max(u0, u1);
    }
    @Override public String toString() {
        return String.format(Locale.ENGLISH, "A:[%.6f, %.6f], B:[%.6f, %.6f]", t0, t1, u0, u1);
    }
}